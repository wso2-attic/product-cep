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
 * Testing Geo Extension
 */
public class GeoExtensionTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(GeoExtensionTestCase.class);

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
            "wso2.cep"}, description = "Testing Geocode Stream Function Processor Siddhi Extension")
    public void siddhiGeoExtensionTestScenario()
            throws Exception {
        final int messageCount = 3;

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        int startEXPCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

        //Add StreamDefinition
        String streamDefinitionAsString1 = getJSONArtifactConfiguration("extensionflows" + File.separator + "geo",
                "geocodeInputStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString1);
        String streamDefinitionAsString2 = getJSONArtifactConfiguration("extensionflows" + File.separator + "geo",
                "geocodeOutputStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString2);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(),
                startESCount + 2);

        //Add Execution Plan
        String executionPlanAsString =
                getExecutionPlanFromFile("extensionflows" + File.separator + "geo", "geoCodeStreamExecutionPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanAsString);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), startEXPCount + 1);

        //Add WSO2Event publisher
        String eventPublisherConfig =
                getXMLArtifactConfiguration("extensionflows" + File.separator + "geo", "WSo2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(),
                startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("geocodeInputStream:1.0.0");
        eventDto.setAttributeValues(new String[]{"gunasekara mawatha", "Regular", "Sun Nov 02 13:36:05 +0000 2014"});
        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("geocodeInputStream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"hendala road", "Regular", "Sun Nov 12 13:36:05 +0000 2014"});
        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("geocodeInputStream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"mt lavinia", "Regular", "Sun Nov 10 13:36:05 +0000 2014"});

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer("extensionflows" + File.separator + "geo", CEPIntegrationTestConstants
                .TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("geocodeOutputStream:1.0.0");
        event.setPayloadData(new Object[]{5.946159100000001d, 80.4978628d, "Gunasekara Mawatha, Matara, Sri Lanka"});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("geocodeOutputStream:1.0.0");
        event2.setPayloadData(new Object[]{6.9954258d, 79.88272810000001d, "Hendala Rd, Wattala, Sri Lanka"});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("geocodeOutputStream:1.0.0");
        event3.setPayloadData(new Object[]{6.8390463d, 79.8646835d, "Mount Lavinia, Sri Lanka"});
        eventList.add(event3);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);
        Thread.sleep(3000);

        eventStreamManagerAdminServiceClient
                .removeEventStream("geocodeInputStream", "1.0.0");
        eventStreamManagerAdminServiceClient
                .removeEventStream("geocodeOutputStream", "1.0.0");
        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("geoCodeStreamExecutionPlan.siddhiql");
        eventPublisherAdminServiceClient
                .removeInactiveEventPublisherConfiguration("WSo2EventPublisher.xml");

        Thread.sleep(2000);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount,
                    "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                for (Event expectedEvent : eventList) {
                    if (aEvent.getPayloadData().length == 3 &&
                            ((String) aEvent.getPayloadData()[2]).equalsIgnoreCase((String) expectedEvent.getPayloadData()[2])) {
                        Assert.assertEquals((double) aEvent.getPayloadData()[0], (double) expectedEvent.getPayloadData()[0],
                                0.0001d, "Latitude mismatch of geocode processed data");
                        Assert.assertEquals((double) aEvent.getPayloadData()[1], (double) expectedEvent.getPayloadData()[1],
                                0.0001d, "Longitude mismatch of geocode processed data");
                    }
                }
            }
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
