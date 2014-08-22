/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.sample.server;

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

public class TestWso2EventServer {
    Logger log = Logger.getLogger(TestWso2EventServer.class);
    ThriftDataReceiver thriftDataReceiver;
    static final TestWso2EventServer testServer = new TestWso2EventServer();


    public static void main(String[] args) throws DataBridgeException {
        testServer.start(args[0],Integer.parseInt(args[1]));
        synchronized (testServer) {
            try {
                testServer.wait();
            } catch (InterruptedException ignored) {


            }
        }
    }


    public void start(String host,int receiverPort) throws DataBridgeException {
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

            public void setThreadLocalContext(AgentSession agentSession) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }, new InMemoryStreamDefinitionStore());


        thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);


        databridge.subscribe(new AgentCallback() {
            int totalSize = 0;


            public void definedStream(StreamDefinition streamDefinition,
                                      int tenantID) {
                log.info("StreamDefinition " + streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition,int tenantID) {
                //To change body of implemented methods use File | Settings | File Templates.
            }


            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " + credentials.getUsername());
            }


        });


        try {
            String address = HostAddressFinder.findAddress(host);
            log.info("Test Server starting on " + address);
            thriftDataReceiver.start(address);
            log.info("Test Server Started");
        } catch (SocketException e) {
            log.error("Test Server not started !", e);
        }
    }


    public void stop() {
        thriftDataReceiver.stop();
        log.info("Test Server Stopped");
    }
}