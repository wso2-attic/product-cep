/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cep.sample.client;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.net.SocketException;
import java.util.List;

public class TestAgentServer {
    static final TestAgentServer testServer = new TestAgentServer();
    Logger log = org.apache.log4j.Logger.getLogger(TestAgentServer.class);
    ThriftDataReceiver thriftDataReceiver;

    public static void main(String[] args) throws DataBridgeException {
        testServer.start(7661);
        synchronized (testServer) {
            try {
                testServer.wait();
            } catch (InterruptedException ignored) {
                System.out.println("Error: Thread interrupted; " + ignored.getMessage());
            }
        }
    }

    public void start(int receiverPort) throws DataBridgeException {
        KeyStoreUtil.setKeyStoreParams();
        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName,
                                        String password) {
                return true;// allays authenticate to true
            }

            @Override
            public String getTenantDomain(String userName) {
                return "admin";
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
        }, new InMemoryStreamDefinitionStore());

        thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);

        databridge.subscribe(new AgentCallback() {
            @Override
            public void definedStream(StreamDefinition streamDefinition, int i) {
                log.info("StreamDefinition " + streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int i) {

            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                long receivedTimeNanos = System.nanoTime();
                long receivedTimeMillis = System.currentTimeMillis();
                long producerIndex = (Long) eventList.get(0).getMetaData()[1];
                long rcvCount = producerIndex;
                int eventCount = 0;
                long startOfFirstEventMillis = (Long) eventList.get(0).getMetaData()[2];
                long startOfFirstEventNanos = (Long) eventList.get(0).getMetaData()[3];
                long firstEventLatency = receivedTimeMillis - startOfFirstEventMillis;
                long nanoOffSet = receivedTimeNanos - startOfFirstEventNanos + firstEventLatency;
                long totalLatency = 0L;
                for (Event event : eventList) {
                    producerIndex = (Long) event.getMetaData()[1];
                    long startOfEventNanos = (Long) event.getMetaData()[3];
                    if (producerIndex == rcvCount) {
                        long latency = (receivedTimeNanos - startOfEventNanos + nanoOffSet);
                        totalLatency += latency;
                        eventCount++;
                    } else {
                        log.warn("[" + Thread.currentThread().getName() + "]: Event not received in order. Dropped event??");
                    }
                    rcvCount++;
                }
                double throughput = (totalLatency / eventCount) / 1000000000D;
                log.info("[" + Thread.currentThread().getName() + "]: A batch of " + eventCount + " events received from index " + eventList.get(0).getMetaData()[1] + " to " + producerIndex + " with throughput " + throughput + " events per second.");
                //log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " + credentials.getUsername());
            }
        });


        try {
            String address = HostAddressFinder.findAddress("localhost");
            System.out.println("Test Server starting on " + address);
            log.info("Test Server starting on " + address);
            thriftDataReceiver.start(address);
            log.info("Test Server Started");
        } catch (SocketException e) {
            log.info("Test Server not started !" + e);
        }
    }

    public void stop() {
        thriftDataReceiver.stop();
        log.info("Test Server Stopped");
        System.out.println("Test Server Stopped");
    }
}