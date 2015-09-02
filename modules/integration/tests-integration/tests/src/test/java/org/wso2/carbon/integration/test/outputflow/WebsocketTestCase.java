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

package org.wso2.carbon.integration.test.outputflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.test.client.WebSocketClient;
import org.wso2.carbon.integration.test.client.WebSocketServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;

/**
 * Sending different from the Websocket and websocket local publishers
 */
public class WebsocketTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(WebsocketTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing websocket local publisher with Text formatted event")
    public void websocketLocalReceiver() throws Exception {

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0070";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add websocket local Text EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "WebsocketLocalPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.receive("ws://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                "/outputwebsocket/WebsocketLocalPublisher", 30);

        Thread.sleep(1000);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                                                 "100.34", "23.4545"});

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("WebsocketLocalPublisher.xml");

        Assert.assertNotNull(webSocketClient.getReceivedMessage(), "No message received from websocket local");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing websocket publisher with XML formatted event")
    public void websocketReceiver() throws Exception {

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0069";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add websocket text EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "WebsocketPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        WebSocketServer socketServer = new WebSocketServer();
        socketServer.start(CEPIntegrationTestConstants.WEB_SOCKET_SERVER_PORT);

        Thread.sleep(1000);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                                                 "100.34", "23.4545"});

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);

        socketServer.stop();

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("WebsocketPublisher.xml");

        Assert.assertNotNull(socketServer.getReceivedMessage(), "No message received from websocket");

    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
