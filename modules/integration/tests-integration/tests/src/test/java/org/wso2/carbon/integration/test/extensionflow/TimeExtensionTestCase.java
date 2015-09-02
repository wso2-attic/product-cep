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
import java.util.List;

/**
 * Testing RDBMS Event Table Extension Sample..
 */
public class TimeExtensionTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(TimeExtensionTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Time Extension")
    public void timeExtensionTestScenario() throws Exception {

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        int startEXPCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

        //Add StreamDefinition
        String streamDefinitionAsString1 = getJSONArtifactConfiguration("extensionflows" + File.separator + "time",
                                                                        "org.wso2.sample.stock.quote.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString1);
        String streamDefinitionAsString2 = getJSONArtifactConfiguration("extensionflows" + File.separator + "time",
                                                                        "org.wso2.sample.stockPriceWithTimeStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString2);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 2);


        //Add Execution Plan
        String executionPlanAsString = getExecutionPlanFromFile("extensionflows" + File.separator + "time", "ExecutionPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanAsString);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), startEXPCount + 1);

        //Add RDBMS publisher
        String eventPublisherConfig = getXMLArtifactConfiguration("extensionflows" + File.separator + "time", "Wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);


        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer("Wso2EventTestCase", CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(5000);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.sample.stock.quote.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"IBM", "50000", "50"});
        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(5000);

        Assert.assertEquals(agentServer.getMsgCount(), 1, "Incorrect number of messages consumed!");
        List<Event> eventList = agentServer.getPreservedEventList();
        Event event = eventList.get(0);
        Object timeValue = (event.getPayloadData()[1]);
        Assert.assertNotNull(timeValue, "Invalid Time Property Value Found");
        Thread.sleep(2000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.stock.quote.stream", "1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.stockPriceWithTimeStream", "1.0.0");
        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("ExecutionPlan.siddhiql");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("Wso2EventPublisher.xml");

        Thread.sleep(2000);

        agentServer.stop();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
