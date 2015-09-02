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

package org.wso2.carbon.integration.test.inputflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.carbon.integration.test.client.WebSocketClient;
import org.wso2.carbon.integration.test.client.WebSocketServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;

/**
 * Sending different formatted events to the Websocket and Websocket local Receivers
 */
public class WebsocketTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(WebsocketTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing websocket local receiver with XML formatted event")
    public void websocketLocalReceiver() throws Exception {
        String samplePath = "inputflows" + File.separator + "sample0020";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Websocket Local XML EventReceiver without mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "WebsocketLocalReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(1000);

        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.send("ws://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                             "/inputwebsocket/WebsocketLocalReceiver", "<events>\n" +
                "    <event>\n" +
                "        <metaData>\n" +
                "            <timestamp>56783</timestamp>\n" +
                "            <isPowerSaverEnabled>true</isPowerSaverEnabled>\n" +
                "            <sensorId>4</sensorId>\n" +
                "            <sensorName>data2</sensorName>\n" +
                "        </metaData>\n" +
                "        <correlationData>\n" +
                "            <longitude>90.34344</longitude>\n" +
                "            <latitude>1.23434</latitude>\n" +
                "        </correlationData>\n" +
                "        <payloadData>\n" +
                "            <humidity>4.5</humidity>\n" +
                "            <sensorValue>90.34344</sensorValue>\n" +
                "        </payloadData>\n" +
                "    </event>\n" +
                "</events>");

        Thread.sleep(1000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("WebsocketLocalReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);


        try {
            Assert.assertEquals(agentServer.getMsgCount(), 1, "Incorrect number of messages consumed!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing websocket receiver with XML formatted event")
    public void websocketReceiver() throws Exception {
        String samplePath = "inputflows" + File.separator + "sample0019";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Websocket XML EventReceiver without mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "WebsocketReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(1000);

        WebSocketServer socketServer = new WebSocketServer();
        socketServer.start(9099);
        socketServer.send("<events>\n" +
                "    <event>\n" +
                "        <metaData>\n" +
                "            <timestamp>56783</timestamp>\n" +
                "            <isPowerSaverEnabled>true</isPowerSaverEnabled>\n" +
                "            <sensorId>4</sensorId>\n" +
                "            <sensorName>data2</sensorName>\n" +
                "        </metaData>\n" +
                "        <correlationData>\n" +
                "            <longitude>90.34344</longitude>\n" +
                "            <latitude>1.23434</latitude>\n" +
                "        </correlationData>\n" +
                "        <payloadData>\n" +
                "            <humidity>4.5</humidity>\n" +
                "            <sensorValue>90.34344</sensorValue>\n" +
                "        </payloadData>\n" +
                "    </event>\n" +
                "</events>", 30);

        Thread.sleep(1000);

        socketServer.stop();
        Thread.sleep(1000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("WebsocketReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);


        try {
            Assert.assertEquals(agentServer.getMsgCount(), 1, "Incorrect number of messages consumed!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
