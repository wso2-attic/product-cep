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

package org.wso2.carbon.integration.test.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.StreamDefinitionDto;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

public class EventProcessorAdminServiceTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(EventProcessorAdminServiceTestCase.class);
    private static int eventStreamCount;
    private static int executionPlanCount;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();
        eventProcessorAdminServiceClient = configurationUtil.
                getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.
                getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing get active execution plan configuration")
    public void testActiveExecutionPlan() throws Exception {
        String samplePath = "admin" + File.separator + "EventProcessorAdminServiceTestCase";

        try {
            eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
            executionPlanCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

            //Add input stream definition
            String pizzaStreamDefinition = getJSONArtifactConfiguration(samplePath, "org.wso2.sample.pizza.order_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(pizzaStreamDefinition);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            //Add output stream definition
            String outStreamDefinition = getJSONArtifactConfiguration(samplePath, "outStream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(outStreamDefinition);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            //Add execution plan
            String executionPlan = getExecutionPlanFromFile(samplePath, "testPlan.siddhiql");
            eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), ++executionPlanCount);

            //Get active execution plan configuration
            ExecutionPlanConfigurationDto executionPlanConfigurationDto = eventProcessorAdminServiceClient.
                    getActiveExecutionPlanConfiguration("testPlan");
            Assert.assertEquals(executionPlanConfigurationDto.getName(), "testPlan");

            //Remove artifacts
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order", "1.0.0");
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), --eventStreamCount);
            eventStreamManagerAdminServiceClient.removeEventStream("outStream", "1.0.0");
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), --eventStreamCount);
            eventProcessorAdminServiceClient.removeInactiveExecutionPlan("testPlan.siddhiql");
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), --executionPlanCount);

        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            eventStreamCount = 0;
            executionPlanCount = 0;
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing edit and validate execution plan configuration")
    public void testEditAndValidateExecutionPlan() throws Exception {
        String samplePath = "admin" + File.separator + "EventProcessorAdminServiceTestCase";

        try {
            eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
            executionPlanCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

            //Add input stream definition
            String pizzaStreamDefinition = getJSONArtifactConfiguration(samplePath, "org.wso2.sample.pizza.order_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(pizzaStreamDefinition);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            //Add output stream definition
            String outStreamDefinition = getJSONArtifactConfiguration(samplePath, "outStreamNew_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(outStreamDefinition);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            //Add execution plan
            String executionPlan = getExecutionPlanFromFile(samplePath, "testPlan.siddhiql");
            eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), ++executionPlanCount);

            //Edit and validate execution plan
            String executionPlanEdited = getExecutionPlanFromFile(samplePath, "testPlanNew.siddhiql");
            eventProcessorAdminServiceClient.editActiveExecutionPlan(executionPlanEdited, "testPlan");
            Assert.assertEquals(eventProcessorAdminServiceClient.validateExecutionPlan(executionPlanEdited), "success");

            //Get active execution plans
            ExecutionPlanConfigurationDto executionPlanConfigurationDto = eventProcessorAdminServiceClient.
                    getActiveExecutionPlanConfiguration("testPlan");
            Assert.assertEquals(executionPlanConfigurationDto.getName(), "testPlan");

            //Remove artifacts
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order", "1.0.0");
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), --eventStreamCount);
            eventStreamManagerAdminServiceClient.removeEventStream("outStreamNew", "1.0.0");
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), --eventStreamCount);
            eventProcessorAdminServiceClient.removeInactiveExecutionPlan("testPlan.siddhiql");
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), --executionPlanCount);

        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            eventStreamCount = 0;
            executionPlanCount = 0;
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing get siddhi streams and set statistics/tracing enable")
    public void testGetSiddhiStreamsAndSetStatisticsAndTracingEnable() throws Exception {
        String samplePath = "admin" + File.separator + "EventProcessorAdminServiceTestCase";
        int siddhiEventStreamCount = 2;

        try {
            eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
            executionPlanCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

            //Add input stream definition
            String pizzaStreamDefinition = getJSONArtifactConfiguration(samplePath, "org.wso2.sample.pizza.order_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(pizzaStreamDefinition);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            //Add output stream definition
            String outStreamDefinition = getJSONArtifactConfiguration(samplePath, "outStream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(outStreamDefinition);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            //Add execution plan
            String executionPlan = getExecutionPlanFromFile(samplePath, "testPlan.siddhiql");
            eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), ++executionPlanCount);

            //Set statistics and tracing enable
            eventProcessorAdminServiceClient.setStatisticsEnabled("testPlan", true);
            eventProcessorAdminServiceClient.setTracingEnabled("testPlan", true);

            //Get active execution plans
            ExecutionPlanConfigurationDto executionPlanConfigurationDto = eventProcessorAdminServiceClient.
                    getActiveExecutionPlanConfiguration("testPlan");
            Assert.assertEquals(executionPlanConfigurationDto.getStatisticsEnabled(), true);
            Assert.assertEquals(executionPlanConfigurationDto.getTracingEnabled(), true);

            //Get siddhi stream count
            StreamDefinitionDto[] streamDefinitionDtos = eventProcessorAdminServiceClient.getSiddhiStreams(executionPlan);
            Assert.assertEquals(streamDefinitionDtos.length, siddhiEventStreamCount);

            //Remove artifacts
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order", "1.0.0");
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), --eventStreamCount);
            eventStreamManagerAdminServiceClient.removeEventStream("outStream", "1.0.0");
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), --eventStreamCount);
            eventProcessorAdminServiceClient.removeInactiveExecutionPlan("testPlan.siddhiql");
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), --executionPlanCount);

        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            eventStreamCount = 0;
            executionPlanCount = 0;
        }
    }

/* todo: incomplete test case
    @Test(groups = {"wso2.cep"}, description =
            "Testing get all exported stream specific active execution plan configuration")
    public void testGetAllExportedStreamSpecificActiveExecutionPlanConfiguration() {
        try {
            ExecutionPlanConfigurationDto[] allExportedStreamSpecificActiveExecutionPlanConfiguration
                    = eventProcessorAdminServiceClient
                    .getAllExportedStreamSpecificActiveExecutionPlanConfiguration("");
        } catch (RemoteException e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }*/

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
