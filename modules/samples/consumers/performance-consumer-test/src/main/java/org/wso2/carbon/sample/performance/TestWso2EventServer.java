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
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
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

import java.util.List;

public class TestWso2EventServer {
    Logger log = Logger.getLogger(TestWso2EventServer.class);
    ThriftDataReceiver thriftDataReceiver;
    BinaryDataReceiver binaryDataReceiver;
    AbstractStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();
    static final TestWso2EventServer testServer = new TestWso2EventServer();


    public static void main(String[] args) throws DataBridgeException, StreamDefinitionStoreException {
        testServer.start(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]),
                Integer.parseInt(args[4]));
        synchronized (testServer) {
            try {
                testServer.wait();
            } catch (InterruptedException ignored) {


            }
        }
    }


    public void start(String host, int receiverPort, String protocol, final int eventCount, final int elapsedCount)
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

        }, streamDefinitionStore, WSO2EventServerUtil.getDataBridgeConfigPath());


        String stream = "{" +
                "  'name': 'org.wso2.event.sensor.stream'," +
                "  'version': '1.0.0'," +
                "  'nickName': ''," +
                "  'description': ''," +
                "  'metaData': [" +
                "    {" +
                "      'name': 'timestamp'," +
                "      'type': 'LONG'" +
                "    }," +
                "    {" +
                "      'name': 'isPowerSaverEnabled'," +
                "      'type': 'BOOL'" +
                "    }," +
                "    {" +
                "      'name': 'sensorId'," +
                "      'type': 'INT'" +
                "    }," +
                "    {" +
                "      'name': 'sensorName'," +
                "      'type': 'STRING'" +
                "    }" +
                "  ]," +
                "  'correlationData': [" +
                "    {" +
                "      'name': 'longitude'," +
                "      'type': 'DOUBLE'" +
                "    }," +
                "    {" +
                "      'name': 'latitude'," +
                "      'type': 'DOUBLE'" +
                "    }" +
                "  ]," +
                "  'payloadData': [" +
                "    {" +
                "      'name': 'humidity'," +
                "      'type': 'FLOAT'" +
                "    }," +
                "    {" +
                "      'name': 'sensorValue'," +
                "      'type': 'DOUBLE'" +
                "    }" +
                "  ]" +
                "}";

        try {
            streamDefinitionStore.saveStreamDefinitionToStore(EventDefinitionConverterUtils.convertFromJson(stream)
                    , -1234);
        } catch (MalformedStreamDefinitionException e) {
            log.error(e);
        }

        databridge.subscribe(new AgentCallback() {
            int counter = 0;
            int skippedcounter = 0;
            long startElapsedTime = System.nanoTime();

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
                counter = counter + eventList.size();
                if (((eventList.size()) % elapsedCount == 0) || counter == eventCount) {
                    int totalCalculatedCount = skippedcounter + eventList.size();
                    long currentTime = System.nanoTime();
                    long elapsedTime = currentTime - startElapsedTime;
                    double timeInSec = elapsedTime / 1000000000D;
                    double throughputPerSec = totalCalculatedCount / timeInSec;
                    startElapsedTime = currentTime;
                    skippedcounter = 0;
                    log.info("Received " + totalCalculatedCount + " sensor events in " + timeInSec
                            + " seconds with total throughput of " + throughputPerSec + " events per second.");
                } else {
                    skippedcounter = skippedcounter + eventList.size();

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