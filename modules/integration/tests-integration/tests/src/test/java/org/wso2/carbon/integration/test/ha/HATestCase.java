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

package org.wso2.carbon.integration.test.ha;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.tests.CarbonTestServerManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.WireMonitorServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;


public class HATestCase{

    private static final Log log = LogFactory.getLog(HATestCase.class);
    private MultipleServersManager manager = new MultipleServersManager();
    private static final String EVENT_PROCESSING_FILE = "event-processor.xml";
    private static final String AXIS2_XML_FILE = "axis2.xml";
    private static final String MASTER_DATASOURCES = "master-datasources.xml";
    private static final String CARBON = "carbon.xml";
    private static final String RESOURCE_LOCATION1 = TestConfigurationProvider.getResourceLocation()  + File.separator + "artifacts" + File.separator + "CEP"
            + File.separator + "haTestCase"+ File.separator + "activeNodeConfig";
    private static final String RESOURCE_LOCATION2 = TestConfigurationProvider.getResourceLocation()  + File.separator + "artifacts" + File.separator + "CEP"
            + File.separator + "haTestCase"+ File.separator + "passiveNodeConfig";
    private static String CARBON_HOME1;
    private static String CARBON_HOME2;
    private ServerConfigurationManager serverConfigManager1;
    private ServerConfigurationManager serverConfigManager2;
    private AutomationContext cepServer1;
    private AutomationContext cepServer2;


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        log.info("--------Initialization Start--------");
        cepServer1 = new AutomationContext("CEP",TestUserMode.SUPER_TENANT_ADMIN);
        cepServer2 = new AutomationContext("CEP",TestUserMode.SUPER_TENANT_ADMIN);

        CarbonTestServerManager server1 = new CarbonTestServerManager(cepServer1,0);
        CarbonTestServerManager server2 = new CarbonTestServerManager(cepServer2,1);

        serverConfigManager1 = new ServerConfigurationManager(cepServer1);
        serverConfigManager2 = new ServerConfigurationManager(cepServer2);

        manager.startServers(server1,server2);

        CARBON_HOME1 = server1.getCarbonHome();
        CARBON_HOME2 = server2.getCarbonHome();

        log.info("Server 1 Replacing " + EVENT_PROCESSING_FILE);
        String eventProcessingFileLocation = RESOURCE_LOCATION1 + File.separator + EVENT_PROCESSING_FILE;
        String cepEventProcessorFileLocation = CARBON_HOME1 + File.separator + "repository" + File.separator
                + "conf" + File.separator + EVENT_PROCESSING_FILE;
        serverConfigManager1.applyConfigurationWithoutRestart(new File(eventProcessingFileLocation), new File(cepEventProcessorFileLocation), true);

        log.info("Server 1 Replacing " + AXIS2_XML_FILE);
        String axis2FileLocation = RESOURCE_LOCATION1 + File.separator + AXIS2_XML_FILE;
        String cepAxis2FileLocation = CARBON_HOME1 + File.separator + "repository" + File.separator + "conf"
                + File.separator + "axis2" + File.separator + AXIS2_XML_FILE;
        serverConfigManager1.applyConfigurationWithoutRestart(new File(axis2FileLocation), new File(cepAxis2FileLocation), true);

        log.info("Server 1 Replacing " + MASTER_DATASOURCES);
        String msDataFileLocation = RESOURCE_LOCATION1 + File.separator + MASTER_DATASOURCES;
        String cepMsFileLocation = CARBON_HOME1 + File.separator + "repository" + File.separator + "conf"
                + File.separator + "datasources" + File.separator + MASTER_DATASOURCES;
        serverConfigManager1.applyConfigurationWithoutRestart(new File(msDataFileLocation), new File(cepMsFileLocation), true);

        log.info("Restarting CEP server1");
        serverConfigManager1.restartGracefully();

        log.info("Server 2 Replacing " + EVENT_PROCESSING_FILE);
        String eventProcessingFileLocation2 = RESOURCE_LOCATION2 + File.separator + EVENT_PROCESSING_FILE;
        String cepEventProcessorFileLocation2 = CARBON_HOME2 + File.separator + "repository" + File.separator
                + "conf" + File.separator + EVENT_PROCESSING_FILE;
        serverConfigManager2.applyConfigurationWithoutRestart(new File(eventProcessingFileLocation2), new File(cepEventProcessorFileLocation2), true);

        log.info("Server 2 Replacing " + AXIS2_XML_FILE);
        String axis2FileLocation2 = RESOURCE_LOCATION2 + File.separator + AXIS2_XML_FILE;
        String cepAxis2FileLocation2 = CARBON_HOME2 + File.separator + "repository" + File.separator + "conf"
                + File.separator + "axis2" + File.separator + AXIS2_XML_FILE;
        serverConfigManager2.applyConfigurationWithoutRestart(new File(axis2FileLocation2), new File(cepAxis2FileLocation2), true);

        log.info("Server 2 Replacing " + MASTER_DATASOURCES);
        String msDataFileLocation2 = RESOURCE_LOCATION2 + File.separator + MASTER_DATASOURCES;
        String cepMsFileLocation2 = CARBON_HOME2 + File.separator + "repository" + File.separator + "conf"
                + File.separator + "datasources" + File.separator + MASTER_DATASOURCES;
        serverConfigManager2.applyConfigurationWithoutRestart(new File(msDataFileLocation2), new File(cepMsFileLocation2), true);

        log.info("Server 2 Replacing " + CARBON);
        String carbonDataFileLocation2 = RESOURCE_LOCATION2 + File.separator + CARBON;
        String cepCarbonFileLocation2 = CARBON_HOME2 + File.separator + "repository" + File.separator + "conf"
                + File.separator + CARBON;
        serverConfigManager2.applyConfigurationWithoutRestart(new File(carbonDataFileLocation2), new File(cepCarbonFileLocation2), true);


        log.info("Restarting CEP server2");
        serverConfigManager2.restartGracefully();
        Thread.sleep(5000);
        log.info("--------Initialization End--------");
    }

    @Test
    public void test() {
        log.info("Test server startup with system properties");
    }


    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        cepServer1 = null;
        cepServer2 = null;
        manager.stopAllServers();
    }
}
