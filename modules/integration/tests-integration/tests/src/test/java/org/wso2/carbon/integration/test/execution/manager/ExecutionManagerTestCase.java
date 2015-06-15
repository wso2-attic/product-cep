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

package org.wso2.carbon.integration.test.execution.manager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ParameterDTOE;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.TemplateConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDomainDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.processflow.DeployArtifactsBasicTestCase;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;

public class ExecutionManagerTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(DeployArtifactsBasicTestCase.class);
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
                + "repository" + File.separator + "conf" + File.separator + "cep" + File.separator
                + "domain-template" + File.separator));
        serverManager.restartForcefully();


        String loggedInSessionCookie = getSessionCookie();
        eventProcessorAdminServiceClient = configurationUtil
                .getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil
                .getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        executionManagerAdminServiceClient = configurationUtil
                .getExecutionManagerAdminServiceClient(backendURL, loggedInSessionCookie);

    }


    @Test(groups = {"wso2.cep"}, description = "Testing the adding a configuration for a domain template")
    public void addTemplateConfigurationTestScenario1() throws Exception {

        TemplateDomainDTO[] domains = executionManagerAdminServiceClient.getAllDomains();

        if (domains == null) {
            Assert.fail("Domain is not loaded");
        } else {

            TemplateDomainDTO testDomain = domains[0];

            log.info("==================Testing the adding a configuration for a domain template==================== ");
            eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
            executionPlanCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
            eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();

            log.info("=======================Adding a configuration====================");
            TemplateConfigurationDTO configuration = new TemplateConfigurationDTO();

            configuration.setName("TestConfig");
            configuration.setFrom(testDomain.getName());
            configuration.setType(testDomain.getTemplateDTOs()[0].getName());
            configuration.setDescription("This is a test description");


            for (ParameterDTO parameterDTO : testDomain.getTemplateDTOs()[0].getParameterDTOs()) {
                ParameterDTOE parameterDTOE = new ParameterDTOE();
                parameterDTOE.setName(parameterDTO.getName());

                if (parameterDTO.getType().toLowerCase().equals("int")) {
                    parameterDTOE.setValue("99");
                } else if (parameterDTO.getType().toLowerCase().equals("string")) {
                    parameterDTOE.setValue("test");
                }

                configuration.addParameterDTOs(parameterDTOE);
            }

            executionManagerAdminServiceClient.saveConfiguration(configuration);
            //There is one execution plan for template, which will be deployed when a configuration added
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                    ++executionPlanCount);
            //Template Execution Plan has 2 streams which will be deployed when a configuration added
            eventStreamCount = eventStreamCount + 2;
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            //Number of configurations should be incremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(testDomain.getName()),
                    ++configurationCount);
            log.info("=======================Edit a configuration====================");
            configuration.setDescription("Description edited");
            executionManagerAdminServiceClient.saveConfiguration(configuration);
            //When existing configuration is been updated, the execution plan will be un-deployed and redeployed
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                    executionPlanCount);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(testDomain.getName()),
                    configurationCount);


            log.info("=======================Delete a configuration====================");
            executionManagerAdminServiceClient.deleteConfiguration(configuration.getFrom(), configuration.getName());
            //When configuration is deleted the execution plan will be un-deployed so count should be decremented
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                    --executionPlanCount);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            //When configuration is deleted the configuration count should be decremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(testDomain.getName()),
                    --configurationCount);
        }

    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}