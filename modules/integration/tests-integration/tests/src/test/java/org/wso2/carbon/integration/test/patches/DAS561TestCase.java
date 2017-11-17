/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.integration.test.patches;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appserver.integration.common.clients.EventPublisherAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventSimulatorAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventStreamManagerAdminServiceClient;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.admin.client.NDataSourceAdminServiceClient;
import org.wso2.carbon.integration.common.tests.CarbonTestServerManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.H2DatabaseClient;
import org.wso2.carbon.integration.test.client.util.BasicDataSource;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo_WSDataSourceDefinition;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;

import javax.xml.stream.XMLStreamException;

public class DAS561TestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(DAS561TestCase.class);
    private MultipleServersManager manager = new MultipleServersManager();
    private static final String OUTPUT_EVENT_ADAPTER_CONF_FILE = "output-event-adapters.xml";
    private static final String RESOURCE_LOCATION1 = TestConfigurationProvider.getResourceLocation()  +
            "artifacts" + File.separator + "CEP" + File.separator + "patches" + File.separator + "DAS561";
    private AutomationContext cepServer;
    private CarbonTestServerManager server;
    private EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;
    private EventSimulatorAdminServiceClient eventSimulatorAdminServiceClient;
    private EventPublisherAdminServiceClient eventPublisherAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        cepServer = new AutomationContext("CEP", "cep002", TestUserMode.SUPER_TENANT_ADMIN);
        server = new CarbonTestServerManager(cepServer, 801);
        ServerConfigurationManager serverConfigManager1 = new ServerConfigurationManager(cepServer);
        manager.startServers(server);
        String CARBON_HOME = server.getCarbonHome();

        String outputEventAdapterConfFileLocation = RESOURCE_LOCATION1 + File.separator +
                OUTPUT_EVENT_ADAPTER_CONF_FILE;
        String cepoutputEventAdapterConfFileLocation = CARBON_HOME + File.separator + "repository" + File.separator
                + "conf" + File.separator + OUTPUT_EVENT_ADAPTER_CONF_FILE;
        serverConfigManager1.applyConfigurationWithoutRestart(new File(outputEventAdapterConfFileLocation),
                new File(cepoutputEventAdapterConfFileLocation), true);

        log.info("Restarting CEP server");
        serverConfigManager1.restartGracefully();
        Thread.sleep(5000);

        String backendURL = cepServer.getContextUrls().getBackEndUrl();
        String loggedInSessionCookie = getSessionCookie(cepServer);
        eventStreamManagerAdminServiceClient = configurationUtil
                .getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil
                .getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
        eventSimulatorAdminServiceClient = configurationUtil
                .getEventSimulatorAdminServiceClient(backendURL, loggedInSessionCookie);

        NDataSourceAdminServiceClient dataSourceAdminService =
                new NDataSourceAdminServiceClient(backendURL, loggedInSessionCookie);
        WSDataSourceMetaInfo dataSourceInfo = getDataSourceInformation("WSO2CEP_DB");
        dataSourceAdminService.addDataSource(dataSourceInfo);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing batch insertion in RDBMS output event adapter")
    public void test1() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0072";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add RDBMS publisher
        String eventPublisherConfig = getXMLArtifactConfiguration("patches" + File.separator + "DAS561",
                "rdbmsEventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        log.info("batch-wise events publishing to rdbms database started.");
        for (int i=0;i<1050;i++) {
            EventDto eventDto = new EventDto();
            eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
            eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656",
                    "7.12324", "100.34", "23.4545"});
            eventSimulatorAdminServiceClient.sendEvent(eventDto);
        }
        log.info("batch-wise events publishing to rdbms database stopped.");

        Thread.sleep(5000);

        int latestCount = H2DatabaseClient.getTableEntryCount("sensordatabatch");
        Assert.assertEquals(latestCount, 1050, "Events are not reached the H2 database");

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("rdbmsEventPublisher.xml");

        Thread.sleep(2000);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        super.cleanup();
        cepServer = null;
        manager.stopAllServers();
    }

    protected String getSessionCookie(AutomationContext serverContext) throws Exception {
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(serverContext);
        return loginLogoutClient.login();
    }

    private WSDataSourceMetaInfo getDataSourceInformation(String dataSourceName)
            throws XMLStreamException {
        WSDataSourceMetaInfo dataSourceInfo = new WSDataSourceMetaInfo();
        dataSourceInfo.setName(dataSourceName);
        WSDataSourceMetaInfo_WSDataSourceDefinition dataSourceDefinition =
                new WSDataSourceMetaInfo_WSDataSourceDefinition();
        dataSourceDefinition.setType("RDBMS");

        OMElement dsConfig = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<configuration>\n" +
                "<driverClassName>" + BasicDataSource.H2_DRIVER_CLASS + "</driverClassName>\n" +
                "<url>" + BasicDataSource.H2_CONNECTION_URL + "</url>\n" +
                "<username>" + BasicDataSource.H2USERNAME + "</username>\n" +
                "<password encrypted=\"true\">" + BasicDataSource.H2PASSWORD + "</password>\n" +
                "</configuration>");

        dataSourceDefinition.setDsXMLConfiguration(dsConfig.toString());
        dataSourceInfo.setDefinition(dataSourceDefinition);
        return dataSourceInfo;
    }
}