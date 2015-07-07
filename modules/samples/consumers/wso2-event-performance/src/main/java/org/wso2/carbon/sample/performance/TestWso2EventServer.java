/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sample.performance;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.binary.internal.BinaryDataReceiver;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TestWso2EventServer {
    private static Logger log = Logger.getLogger(TestWso2EventServer.class);
    ThriftDataReceiver thriftDataReceiver;
    BinaryDataReceiver binaryDataReceiver;
    AtomicLong counter = new AtomicLong(0);
    AbstractStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();
    static final TestWso2EventServer testServer = new TestWso2EventServer();


    public static void main(String[] args) throws DataBridgeException, StreamDefinitionStoreException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    log.info("Final event count: " + testServer.counter.get());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        log.info("Shutdown hook added.");
        testServer.start(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
        synchronized (testServer) {
            try {
                testServer.wait();
            } catch (InterruptedException ignored) {
                //ignore
            }
        }
    }


    public void start(String host, int receiverPort, String protocol, final int elapsedCount)
            throws DataBridgeException, StreamDefinitionStoreException {
        WSO2EventServerUtil.setKeyStoreParams();

        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName,
                                        String password) {
                return true;
            }

            @Override
            public String getTenantDomain(String userName) {
                return "carbon.super";
            }

            @Override
            public int getTenantId(String s) throws UserStoreException {
                return -1234;
            }

            @Override
            public void initContext(AgentSession agentSession) {

            }

            @Override
            public void destroyContext(AgentSession agentSession) {

            }

        }, streamDefinitionStore, WSO2EventServerUtil.getDataBridgeConfigPath());

        streamDefinitionStore.saveStreamDefinitionToStore(WSO2EventServerUtil.loadStream(), -1234);


        databridge.subscribe(new AgentCallback() {

            AtomicLong totalDelay = new AtomicLong(0);
            AtomicLong lastIndex = new AtomicLong(0);
            AtomicLong lastCounter = new AtomicLong(0);
            AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            public void definedStream(StreamDefinition streamDefinition,
                                      int tenantID) {
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int tenantID) {
                //To change body of implemented methods use File | Settings | File Templates.
                log.info("Test");
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                long currentTime = System.currentTimeMillis();
                long currentBatchTotalDelay = 0;
                for (Event event : eventList) {
                    currentBatchTotalDelay = currentBatchTotalDelay + (currentTime - event.getTimeStamp());
                }
                /** Following section should ideally be atomic **/
                long localTotalDelay = totalDelay.addAndGet(currentBatchTotalDelay);
                long localCounter = counter.addAndGet(eventList.size());
                /** End of wish for atomic section **/

                long index = localCounter / elapsedCount;

                if (lastIndex.compareAndSet(index - 1, index)) {
                    long currentWindowEventsReceived = localCounter - lastCounter.getAndSet(localCounter);
                    //log.info("Current time: " + System.currentTimeMillis() + ", Event received time: " + currentTime + ", Last calculation time: " + lastTime.get());
                    long elapsedTime = currentTime - lastTime.getAndSet(currentTime);
                    double throughputPerSecond = (((double) currentWindowEventsReceived) / elapsedTime) * 1000;

                    log.info("[" + Thread.currentThread().getName() + "] Received " + currentWindowEventsReceived + " sensor events in " + elapsedTime
                            + " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond)
                            + " events per second. Average delay is " + decimalFormat.format(localTotalDelay / (double) currentWindowEventsReceived));
                    totalDelay.addAndGet(-localTotalDelay);
                }
            }
        });


        if (protocol.equalsIgnoreCase("binary")) {
            binaryDataReceiver = new BinaryDataReceiver(
                    new BinaryDataReceiverConfiguration(receiverPort + 100, receiverPort), databridge);
        } else {
            thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);
        }
        thriftDataReceiver.start(host);
        log.info("Test Server Started");
    }


    public void stop() {
        if (thriftDataReceiver != null) {
            thriftDataReceiver.stop();
        }
        if (binaryDataReceiver != null) {
            binaryDataReceiver.stop();
        }
        log.info("Test Server Stopped");
    }
}