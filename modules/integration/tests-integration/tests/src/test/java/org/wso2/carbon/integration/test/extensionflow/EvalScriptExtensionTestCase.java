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
 * Testing Eval Java Script Extension
 */
public class EvalScriptExtensionTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(EvalScriptExtensionTestCase.class);

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
            "wso2.cep"}, description = "Testing Eval Java Script Siddhi Extension")
    public void siddhiEvalJavaScriptExtensionTestScenario()
            throws Exception {
        final int messageCount = 3;

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        int startEXPCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

        //Add StreamDefinition
        String streamDefinitionAsString1 = getJSONArtifactConfiguration("extensionflows" + File.separator + "evalscript",
                "org.wso2.sample.stock.quote.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString1);
        String streamDefinitionAsString2 = getJSONArtifactConfiguration("extensionflows" + File.separator + "evalscript",
                "org.wso2.sample.stock.quote.stream.results_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString2);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(),
                startESCount + 2);

        //Add Execution Plan
        String executionPlanAsString =
                getExecutionPlanFromFile("extensionflows" + File.separator + "evalscript", "ExecutionPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanAsString);
        Assert.assertEquals(
                eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(),
                startEXPCount + 1);

        //Add WSO2Event publisher
        String eventPublisherConfig =
                getXMLArtifactConfiguration("extensionflows" + File.separator + "evalscript", "Wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(),
                startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.sample.stock.quote.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"WSO2", "50", "50"});
        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.sample.stock.quote.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"IBM", "60", "50"});
        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.sample.stock.quote.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"Microsoft", "70", "50"});

        //The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer("extensionflows" + File.separator + "evalscript", CEPIntegrationTestConstants
                .TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.sample.stock.quote.stream.results:1.0.0");
        event.setPayloadData(new Object[]{"WSO2 50"});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.sample.stock.quote.stream.results:1.0.0");
        event2.setPayloadData(new Object[]{"IBM 60"});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.sample.stock.quote.stream.results:1.0.0");
        event3.setPayloadData(new Object[]{"Microsoft 70"});
        eventList.add(event3);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);
        Thread.sleep(3000);

        eventStreamManagerAdminServiceClient
                .removeEventStream("org.wso2.sample.stock.quote.stream", "1.0.0");
        eventStreamManagerAdminServiceClient
                .removeEventStream("org.wso2.sample.stock.quote.stream.results", "1.0.0");
        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("ExecutionPlan.siddhiql");
        eventPublisherAdminServiceClient
                .removeInactiveEventPublisherConfiguration("Wso2EventPublisher.xml");

        Thread.sleep(2000);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount,
                    "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList,
                    "Mismatch of value with eval java script result!");
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

