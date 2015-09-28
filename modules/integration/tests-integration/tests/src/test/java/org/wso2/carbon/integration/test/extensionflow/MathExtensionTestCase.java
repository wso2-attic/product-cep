/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.test.extensionflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Testing Math Ceil Extension
 */
public class MathExtensionTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(MathExtensionTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventSimulatorAdminServiceClient = configurationUtil
                .getEventSimulatorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil
                .getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil
                .getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil
                .getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {
            "wso2.cep"}, description = "Testing Math Ceil Siddhi Extension")
    public void siddhiMathExtensionTestScenario()
            throws Exception {
        final int messageCount = 3;
        final String sensorStreamId = "org.wso2.event.sensor.stream:1.0.0";
        final String sensorClassifyStreamId = "org.wso2.event.sensorClassifyCeil.stream:1.0.0";

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        int startEXPCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

        //Add StreamDefinition
        String sensorStreamDefinitionAsString = getJSONArtifactConfiguration("extensionflows" + File.separator + "math",
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(sensorStreamDefinitionAsString);
        String sensorClassifyCeilStreamDefinitionAsString = getJSONArtifactConfiguration("extensionflows" + File.separator + "math",
                "org.wso2.event.sensorClassifyCeil.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(sensorClassifyCeilStreamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(),
                startESCount + 2);

        //Add Execution Plan
        String executionPlanAsString =
                getExecutionPlanFromFile("extensionflows" + File.separator + "math", "MathSiddhiExtensionExecutionPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanAsString);
        Assert.assertEquals(
                eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(),
                startEXPCount + 1);

        //Add WSO2Event publisher
        String eventPublisherConfig =
                getXMLArtifactConfiguration("extensionflows" + File.separator + "math", "Wso2Publisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(),
                startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId(sensorStreamId);
        eventDto.setAttributeValues(new String[]{"001", "Temperature", "23.4545"});
        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId(sensorStreamId);
        eventDto2.setAttributeValues(new String[]{"002", "Wind", "100.5"});
        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId(sensorStreamId);
        eventDto3.setAttributeValues(new String[]{"003", "Temperature", "23.4545"});

        //The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer("extensionflows" + File.separator + "math", CEPIntegrationTestConstants
                .TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId(sensorClassifyStreamId);
        event.setPayloadData(new Object[]{24.0});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId(sensorClassifyStreamId);
        event2.setPayloadData(new Object[]{101.0});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId(sensorClassifyStreamId);
        event3.setPayloadData(new Object[]{24.0});
        eventList.add(event3);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);
        Thread.sleep(3000);

        eventStreamManagerAdminServiceClient
                .removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventStreamManagerAdminServiceClient
                .removeEventStream("org.wso2.event.sensorClassifyCeil.stream", "1.0.0");
        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("MathSiddhiExtensionExecutionPlan.siddhiql");
        eventPublisherAdminServiceClient
                .removeInactiveEventPublisherConfiguration("Wso2Publisher.xml");

        Thread.sleep(2000);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount,
                    "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList,
                    "Mismatch of value with ceil result!");
        } catch (Throwable e) {
            log.error("Exception occurred: " + e.getMessage(), e);
            Assert.fail("Exception e: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
