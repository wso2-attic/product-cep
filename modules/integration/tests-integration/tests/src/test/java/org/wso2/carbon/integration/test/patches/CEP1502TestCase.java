/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.test.patches;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.integration.test.client.HttpEventPublisherClient;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CEP1502TestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(CEP1502TestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with JSON formatted event with mapping enabled and default values set")
    public void httpJSONMappingTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "patches" + File.separator + "CEP1502";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.json.mapped.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http JSON EventReceiver with mapping enabled and default values set
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "jsonDefaultMapper.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add WSO2Event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                "/endpoints/jsonDefaultMapper", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(5000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.json.mapped.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("jsonDefaultMapper.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.json.mapped.stream:1.0.0");
        event.setMetaData(new Object[]{"P6", 1});
        event.setCorrelationData(new Object[]{"NF525004", 4094});
        event.setPayloadData(new Object[]{"2016-04-08 11:52:42", 1356, 10, "2016-04-08 11:52:48", 1356, 1, "2016-04-08 11:52:54", 0, 0});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.json.mapped.stream:1.0.0");
        event2.setMetaData(new Object[]{"P6", 1});
        event2.setCorrelationData(new Object[]{"NF525005", 4094});
        event2.setPayloadData(new Object[]{"2016-04-08 11:52:42", 1356, 10, "2016-04-08 11:52:48", 1356, 1, "2016-04-08 11:52:54", 0, 0});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.json.mapped.stream:1.0.0");
        event3.setMetaData(new Object[]{"P6", 1});
        event3.setCorrelationData(new Object[]{"NF525005", 4096});
        event3.setPayloadData(new Object[]{"2016-04-08 11:52:42", 1356, 10, "2016-04-08 11:52:48", 1356, 1, "2016-04-08 11:53:20", 0, 20});
        eventList.add(event3);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Default value not set properly!");
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