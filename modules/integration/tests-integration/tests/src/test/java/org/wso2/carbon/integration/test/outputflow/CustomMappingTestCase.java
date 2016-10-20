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

package org.wso2.carbon.integration.test.outputflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;

public class CustomMappingTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(CustomMappingTestCase.class);
    private static LogViewerClient logViewerClient;
    private final String samplePath = "outputflows" + File.separator + "sample0073";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
        logViewerClient = new LogViewerClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(
                backendURL, loggedInSessionCookie);
        resourceServiceClient = configurationUtil.getResourceServiceClient(
                backendURL, loggedInSessionCookie);

        //Add StreamDefinition
        String inputStreamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        String outputStreamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.statistics.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(inputStreamDefinitionAsString);
        eventStreamManagerAdminServiceClient.addEventStreamAsString(outputStreamDefinitionAsString);

        // Add execution plan
        String executionPlan = getExecutionPlanFromFile(samplePath, "ArbitraryMapExecutionPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("ArbitraryMapExecutionPlan");
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.statistics.stream", "1.0.0");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Text Inline Logger with arbitrary mapping")
    public void loggerTextWithArbitraryMappingTestScenario() throws Exception {
        executeArbitraryMappingTest("logger_inline_text.xml", "Arbitrary Alert: false");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Xml Inline Logger with arbitrary mapping")
    public void loggerXmlWithArbitraryMappingTestScenario() throws Exception {
        executeArbitraryMappingTest("logger_inline_xml.xml", "<alert>false</alert>");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Json Inline Logger with arbitrary mapping")
    public void loggerJsonWithArbitraryMappingTestScenario() throws Exception {
        executeArbitraryMappingTest("logger_inline_json.xml", "{\"arbitraryAlert\": false}");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Text Custom Registry Path Mapping Logger with arbitrary mapping")
    public void loggerTextWithCustomRegistryPathMappingTestScenario() throws Exception {
        // Create registry resource
        resourceServiceClient.addCollection("/_system/config/template/temperature", "text", "", "");
        resourceServiceClient.addTextResource("/_system/config/template/temperature/text", "message", "text/plain", "", "Temperature is {{sensorValue}}");

        executeArbitraryMappingTest("logger_registry_text.xml", "Temperature is 23.4545");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Json Custom Registry Path Mapping Logger with arbitrary mapping")
    public void loggerJsonWithCustomRegistryPathMappingTestScenario() throws Exception {
        // Create registry resource
        resourceServiceClient.addCollection("/_system/config/template/temperature", "json", "", "");
        resourceServiceClient.addTextResource("/_system/config/template/temperature/json", "message", "text/plain", "", "{\"temperature\": {{sensorValue}}}");

        executeArbitraryMappingTest("logger_registry_json.xml", "{\"temperature\": 23.4545}");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Xml Custom Registry Path Mapping Logger with arbitrary mapping")
    public void loggerXmlWithCustomRegistryPathMappingTestScenario() throws Exception {
        // Create registry resource
        resourceServiceClient.addCollection("/_system/config/template/temperature", "xml", "", "");
        resourceServiceClient.addTextResource("/_system/config/template/temperature/xml", "message", "text/plain", "", "<temperature>{{sensorValue}}</temperature>");

        executeArbitraryMappingTest("logger_registry_xml.xml", "<temperature>23.4545</temperature>");
    }

    private void executeArbitraryMappingTest(String loggerName, String expectedOutput) throws Exception {
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        //Add Logger
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, loggerName);
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "203", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "203", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "203", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        int beforeCount = logViewerClient.getAllRemoteSystemLogs().length;
        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);
        Thread.sleep(1000);
        try {
            boolean mappingPortionFound = false;
            Thread.sleep(2000);
            LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();
            for (int i = 0; i < (logs.length - beforeCount); i++) {
                System.out.println(logs[i].getMessage());
                if (logs[i].getMessage().contains(expectedOutput)) {
                    mappingPortionFound = true;
                    break;
                }
            }
            Assert.assertTrue(mappingPortionFound, "Incorrect mapping has occurred! ");
            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

        eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("logger");

        Thread.sleep(2000);
    }
}
