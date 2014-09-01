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
package org.wso2.carbon.integration.test.client;

import org.testng.Assert;
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
import java.util.concurrent.atomic.AtomicLong;

public class TestAgentServer implements Runnable {
//    private Logger log = Logger.getLogger(TestAgentServer.class);
    private ThriftDataReceiver thriftDataReceiver;
    private boolean eventReceived = false;
    private AtomicLong msgCount = new AtomicLong(0);

    public void startServer() throws DataBridgeException {
        msgCount.set(0);
        start(7661);
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
            public void definedStream(StreamDefinition streamDefinition, int tenantId) {
                System.out.println("Added StreamDefinition " + streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int tenantId) {
                System.out.println("Removed StreamDefinition " + streamDefinition);
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                System.out.println("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " + credentials.getUsername());
                eventReceived = true;
                msgCount.addAndGet(eventList.size());
            }

        });

        try {
            String address = HostAddressFinder.findAddress("localhost");
            System.out.println("Test Server starting on " + address);
            thriftDataReceiver.start(address);
            System.out.println("Test Server Started");
        } catch (SocketException e) {
            System.out.println("Test Server not started !"+ e);
        }
    }

    public void stop() {
        Assert.assertTrue(eventReceived);
        thriftDataReceiver.stop();
        System.out.println("Test Server Stopped");
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (DataBridgeException e) {
            e.printStackTrace();
        }
    }

    public long getMsgCount() {
        return msgCount.get();
    }
}