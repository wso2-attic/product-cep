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

package org.wso2.carbon.integration.test.outputflow;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.admin.client.NDataSourceAdminServiceClient;
import org.wso2.carbon.integration.test.client.H2DatabaseClient;
import org.wso2.carbon.integration.test.client.util.BasicDataSource;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo_WSDataSourceDefinition;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;

/**
 * Testing RDBMS publisher in different formats (text, xml, json)
 */
public class RDBMSTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(RDBMSTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);

        NDataSourceAdminServiceClient dataSourceAdminService =
                new NDataSourceAdminServiceClient(backendURL, loggedInSessionCookie);
        WSDataSourceMetaInfo dataSourceInfo = getDataSourceInformation("WSO2CEP_DB");
        dataSourceAdminService.addDataSource(dataSourceInfo);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing RDBMS Adapter")
    public void rdbmsPublisherTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0072";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add RDBMS publisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "rdbmsEventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324", "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "101", "temperature", "23.45656", "7.12324", "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "103", "temperature", "23.45656", "7.12324", "100.34", "23.4545"});

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        int initialCount1 = H2DatabaseClient.getTableEntryCount("sensordata");
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        int initialCount2 = H2DatabaseClient.getTableEntryCount("sensordata");
        Assert.assertEquals(initialCount2, initialCount1 + 1, "Events are not reached the H2 database");
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);
        Thread.sleep(3000);

        int latestCount = H2DatabaseClient.getTableEntryCount("sensordata");
        Assert.assertEquals(latestCount, initialCount2 + 1, "Events are not reached the H2 database");

        Thread.sleep(2000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("rdbmsEventPublisher.xml");

        Thread.sleep(2000);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing RDBMS publisher connection")
    public void testConnection() {
        String samplePath = "outputflows" + File.separator + "sample0072";
        BasicOutputAdapterPropertyDto dName = new BasicOutputAdapterPropertyDto();
        dName.setKey("datasource.name");
        dName.setValue("WSO2CEP_DB");
        dName.set_static(true);
        BasicOutputAdapterPropertyDto tName = new BasicOutputAdapterPropertyDto();
        tName.setKey("table.name");
        tName.setValue("sensordata");
        tName.set_static(true);
        BasicOutputAdapterPropertyDto mode = new BasicOutputAdapterPropertyDto();
        mode.setKey("execution.mode");
        mode.setValue("");
        mode.set_static(true);
        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration = new BasicOutputAdapterPropertyDto[]
                {dName, tName, mode};

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);

            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "rdbmsEventPublisher.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
            eventPublisherAdminServiceClient.testConnection("rdbmsEventPublisher","rdbms",
                    outputPropertyConfiguration, "map");
            eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("rdbmsEventPublisher");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");

        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    private WSDataSourceMetaInfo getDataSourceInformation(String dataSourceName)
            throws XMLStreamException {
        WSDataSourceMetaInfo dataSourceInfo = new WSDataSourceMetaInfo();

        dataSourceInfo.setName(dataSourceName);

        WSDataSourceMetaInfo_WSDataSourceDefinition dataSourceDefinition = new WSDataSourceMetaInfo_WSDataSourceDefinition();

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
