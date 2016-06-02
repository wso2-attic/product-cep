/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.test.execution.manager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.AttributeMappingDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ParameterDTOE;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ScenarioConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.StreamMappingDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ExecutionManagerTemplateInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ParameterDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.processflow.DeployArtifactsBasicTestCase;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;

public class ExecutionManagerTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(DeployArtifactsBasicTestCase.class);
    private static final String DEVICE_STREAM_ID = "DeviceStream:1.0.0";
    private static final String TO_STREAM_ID = "org.wso2.event.sensor.stream:1.0.0";
    private static final int TO_STREAM_ATTRIBUTE_COUNT = 2;

    private int eventStreamCount;
    private int executionPlanCount;
    private int configurationCount;
    private ServerConfigurationManager serverManager;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(cepServer);


        File newFile = new File(getClass().getResource(File.separator + "artifacts" + File.separator + "CEP"
                + File.separator + "execution" + File.separator + "manager" + File.separator
                + "TestDomain.xml").toURI());
        FileUtils.copyFileToDirectory(newFile, new File(ServerConfigurationManager.getCarbonHome() + File.separator
                + "repository" + File.separator + "conf" + File.separator + "execution-manager" + File.separator
                + "domain-template" + File.separator));
        serverManager.restartForcefully();


        String loggedInSessionCookie = getSessionCookie();
        eventProcessorAdminServiceClient = configurationUtil
                .getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil
                .getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        executionManagerAdminServiceClient = configurationUtil
                .getExecutionManagerAdminServiceClient(backendURL, loggedInSessionCookie);

        //~~Adding user's stream definition~~
        //It is a pre-requisite to have this stream defined.
        //It should be in 'deployed' state when we add the scenario configuration in following tests.
        eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        String deviceStreamDefinition = getJSONArtifactConfiguration("execution" + File.separator + "manager", "DeviceStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(deviceStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        //todo: remove this after the StreamTemplateDeployer has being implemented. Until it is implemented, toStream is deployed this way.
        //~~Adding toStream definition~~
        //It is a pre-requisite to have this stream defined.
        //It should be in 'deployed' state when we save stream mappings in following tests.
        String sensorStreamDefinition = getJSONArtifactConfiguration("execution" + File.separator + "manager", "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(sensorStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        //todo: remove this after the StreamTemplateDeployer has being implemented.
        String statStreamDefinition = getJSONArtifactConfiguration("execution" + File.separator + "manager", "org.wso2.event.current.statistics.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(statStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);
    }


    @Test(groups = {"wso2.cep"}, description = "Testing the adding a configuration for a domain template")
    public void addTemplateConfigurationTestScenario1() throws Exception {
        String configName = "TestConfig";

        ExecutionManagerTemplateInfoDTO executionManagerTemplate = executionManagerAdminServiceClient
                .getExecutionManagerTemplateInfo("TestDomain");

        if (executionManagerTemplate == null) {
            Assert.fail("Domain is not loaded");
        } else {

            log.info("==================Testing the adding a configuration for a domain template==================== ");
            eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
            executionPlanCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
            eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();

            log.info("=======================Adding a configuration====================");
            ScenarioConfigurationDTO configuration = new ScenarioConfigurationDTO();

            configuration.setName(configName);
            configuration.setDomain(executionManagerTemplate.getDomain());
            configuration.setScenario(executionManagerTemplate.getScenarioInfoDTOs()[0].getName());
            configuration.setDescription("This is a test description");

            for (ParameterDTO parameterDTO : executionManagerTemplate.getScenarioInfoDTOs()[0].getParameterDTOs()) {
                ParameterDTOE parameterDTOE = new ParameterDTOE();
                parameterDTOE.setName(parameterDTO.getName());
                parameterDTOE.setValue(parameterDTO.getDefaultValue());

                configuration.addParameterDTOs(parameterDTOE);
            }

            String[] streamIDsToBeMapped = executionManagerAdminServiceClient.saveConfiguration(configuration);
            Assert.assertEquals(TO_STREAM_ID, streamIDsToBeMapped[0]);

            //After saveConfiguration() is called, the execution plan in the TestDomain.xml templated should have being deployed,
            //hence count should get incremented bby one.
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                                ++executionPlanCount);
            //todo: After StreamTemplateDeployer is added, eventStreamCount should get incremented by one.
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            //Number of configurations should be incremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(executionManagerTemplate.getDomain()),
                                ++configurationCount);

            StreamMappingDTO streamMappingDTO = new StreamMappingDTO();
            streamMappingDTO.setFromStream(DEVICE_STREAM_ID);
            streamMappingDTO.setToStream(TO_STREAM_ID);

            AttributeMappingDTO[] attributeMappingDTOs = new AttributeMappingDTO[TO_STREAM_ATTRIBUTE_COUNT];

            AttributeMappingDTO idAttributeMappingDTO = new AttributeMappingDTO();
            idAttributeMappingDTO.setFromAttribute("id");
            idAttributeMappingDTO.setToAttribute("sensor_id");
            idAttributeMappingDTO.setAttributeType("string");

            AttributeMappingDTO valueAttributeMappingDTO = new AttributeMappingDTO();
            valueAttributeMappingDTO.setFromAttribute("reading");
            valueAttributeMappingDTO.setToAttribute("sensor_value");
            valueAttributeMappingDTO.setAttributeType("double");

            attributeMappingDTOs[0] = idAttributeMappingDTO;
            attributeMappingDTOs[1] = valueAttributeMappingDTO;

            streamMappingDTO.setAttributeMappingDTOs(attributeMappingDTOs);

            StreamMappingDTO[] streamMappingDTOs = {streamMappingDTO};


            boolean streamMappingSaved = executionManagerAdminServiceClient.saveStreamMapping(streamMappingDTOs, configName, executionManagerTemplate.getDomain());
            Assert.assertEquals(streamMappingSaved, true);

            //After saveStreamMapping() is called, the streamMapping exection plan should have being deployed.
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                    ++executionPlanCount);

            log.info("=======================Edit a configuration====================");
            configuration.setDescription("Description edited");
            executionManagerAdminServiceClient.saveConfiguration(configuration);
            //When existing configuration is been updated, the execution plan will be un-deployed and redeployed
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                    executionPlanCount);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(executionManagerTemplate.getDomain()),
                                configurationCount);


            log.info("=======================Delete a configuration====================");
            executionManagerAdminServiceClient.deleteConfiguration(configuration.getDomain(), configuration.getName());
            //When configuration is deleted two execution plans will get un-deployed. 1. the one in the template and the stream mapping execution plan.
            executionPlanCount = executionPlanCount -2;
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                    executionPlanCount);
            //todo: after StreamTemplateDeployer is implemented, this count should get decremented by one
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            //When configuration is deleted the configuration count should be decremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(executionManagerTemplate.getDomain()),
                    --configurationCount);
        }

    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}