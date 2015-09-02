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

package org.wso2.carbon.integration.test.admin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.simulator.stub.types.DataSourceTableAndStreamInfoDto;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.admin.client.NDataSourceAdminServiceClient;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.carbon.integration.test.client.util.BasicDataSource;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo_WSDataSourceDefinition;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;

public class EventSimulatorAdminServiceTestCase extends CEPIntegrationTest {

    protected static final Log log = LogFactory.getLog(EventSimulatorAdminServiceTestCase.class);


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();
        eventStreamManagerAdminServiceClient = configurationUtil
                .getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventSimulatorAdminServiceClient = configurationUtil
                .getEventSimulatorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil
                .getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);

        NDataSourceAdminServiceClient dataSourceAdminService =
                new NDataSourceAdminServiceClient(backendURL, loggedInSessionCookie);
        WSDataSourceMetaInfo dataSourceInfo = getDataSourceInformation("WSO2CEP_DB");
        dataSourceAdminService.addDataSource(dataSourceInfo);
    }

    @Test(groups = {"wso2.cep"}, description = "Test database connection, " +
                                               "table column information and stream attribute information is valid")
    public void testSimulateRDBMSDataSourceConnection() {
        try {
            String samplePath = "outputflows" + File.separator + "sample0072";
            int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
            int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

            //Add StreamDefinition
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                           "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

            //Add RDBMS publisher
            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "rdbmsEventPublisherForSimulator.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
            Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

            EventDto eventDto = new EventDto();
            eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
            eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656",
                                                     "7.12324", "100.34", "23.4545"});

            eventSimulatorAdminServiceClient.sendEvent(eventDto);
            Thread.sleep(1000);

            eventSimulatorAdminServiceClient.testSimulateRDBMSDataSourceConnection(
                    "{\n" +
                    "    \"dataSource\"                 : \"WSO2CEP_DB\",\n" +
                    "    \"eventStreamName\"            : \"org.wso2.event.sensor.stream\",\n" +
                    "    \"streamID\"                   : \"org.wso2.event.sensor.stream:1.0.0\",\n" +
                    "    \"name\"                       : \"testSimulator\",\n" +
                    "    \"tableName\"                  : \"sensordata2\",\n" +
                    "    \"delayBetweenEventsInMilies\" : 1000,\n" +
                    "    \"dataSourceColumnsAndTypes\"  : [\n" +
                    "        {\n" +
                    "            \"columnName\" : \"CORRELATION_LATITUDE\",\n" +
                    "            \"columnType\" : \"double\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"columnName\" : \"CORRELATION_LONGITUDE\",\n" +
                    "            \"columnType\" : \"double\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"columnName\" : \"HUMIDITY\",\n" +
                    "            \"columnType\" : \"double\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"columnName\" : \"META_ISPOWERSAVERENABLED\",\n" +
                    "            \"columnType\" : \"bool\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"columnName\" : \"META_SENSORID\",\n" +
                    "            \"columnType\" : \"string\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"columnName\" : \"META_SENSORNAME\",\n" +
                    "            \"columnType\" : \"string\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"columnName\" : \"META_TIMESTAMP\",\n" +
                    "            \"columnType\" : \"long\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"columnName\" : \"SENSORVALUE\",\n" +
                    "            \"columnType\" : \"double\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}"
            );
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("rdbmsEventPublisherForSimulator.xml");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
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

    @Test(groups = {"wso2.cep"}, description = "save and simulate the event stream, using database configuration",
            dependsOnMethods = {"testSimulateRDBMSDataSourceConnection"})
    public void saveAndSimulateRDBMSDataSourceConnection() {
        try {
            String samplePath = "outputflows" + File.separator + "sample0072";
            Wso2EventServer wso2EventServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
            int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
            int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
            int messageCount = 1;

            //Add StreamDefinition
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                           "org.wso2.event.sensor.stream2_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

            //Add eventPublisher
            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2Publisher.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
            Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

            String eventStreamDataSourceColumnNamesAndTypeInfo = eventSimulatorAdminServiceClient
                    .testSimulateRDBMSDataSourceConnection(
                            "{\"streamID\":\"org.wso2.event.sensor.stream2:1.0.0\"," +
                            "\"eventStreamName\":\"org.wso2.event.sensor.stream2\"," +
                            "\"dataSource\":\"WSO2CEP_DB\"," +
                            "\"tableName\":\"sensordata2\"," +
                            "\"name\":\"testSimulator\"," +
                            "\"delayBetweenEventsInMilies\":\"1000\"," +
                            "\"dataSourceColumnsAndTypes\":[" +
                            "{\"streamAttribute\":\"timestamp\",\"columnType\":\"LONG\",\"columnName\":\"META_TIMESTAMP\"}," +
                            "{\"streamAttribute\":\"isPowerSaverEnabled\",\"columnType\":\"BOOL\",\"columnName\":\"META_ISPOWERSAVERENABLED\"}," +
                            "{\"streamAttribute\":\"sensorId\",\"columnType\":\"INT\",\"columnName\":\"META_SENSORID\"}," +
                            "{\"streamAttribute\":\"sensorName\",\"columnType\":\"STRING\",\"columnName\":\"META_SENSORNAME\"}," +
                            "{\"streamAttribute\":\"longitude\",\"columnType\":\"DOUBLE\",\"columnName\":\"CORRELATION_LONGITUDE\"}," +
                            "{\"streamAttribute\":\"latitude\",\"columnType\":\"DOUBLE\",\"columnName\":\"CORRELATION_LATITUDE\"}," +
                            "{\"streamAttribute\":\"humidity\",\"columnType\":\"FLOAT\",\"columnName\":\"HUMIDITY\"}," +
                            "{\"streamAttribute\":\"sensorValue\",\"columnType\":\"DOUBLE\",\"columnName\":\"SENSORVALUE\"}" +
                            "]}"
                    );

            boolean configurationSaved = eventSimulatorAdminServiceClient.saveDataSourceConfigDetails(
                    eventStreamDataSourceColumnNamesAndTypeInfo);
            Thread.sleep(5000);
            if (configurationSaved) {
                int i = 0;
                boolean fileAvailable = false;
                DataSourceTableAndStreamInfoDto[] dataSourceTableAndStreamInfoDtos;
                while (i < 20 && !fileAvailable) {
                    dataSourceTableAndStreamInfoDtos = eventSimulatorAdminServiceClient.getAllDataSourceTableAndStreamInfo();
                    for (int j = 0; dataSourceTableAndStreamInfoDtos != null && j < dataSourceTableAndStreamInfoDtos.length; j++) {
                        if (dataSourceTableAndStreamInfoDtos[j].getConfigurationName().equals("testSimulator")) {
                            fileAvailable = true;
                        }
                    }
                    i++;
                    Thread.sleep(1000);
                }

                if (fileAvailable) {
                    wso2EventServer.startServer();
                    Thread.sleep(2000);
                    eventSimulatorAdminServiceClient.sendDBConfigFileNameToSimulate("testSimulator_datSourceStreamConfiguration.xml");
                    Thread.sleep(5000);
                    Assert.assertEquals(wso2EventServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
                    wso2EventServer.stop();
                } else {
                    throw new Exception("Database configuration file not found.");
                }
            }
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream2", "1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2Publisher.xml");
            eventSimulatorAdminServiceClient.deleteDBConfigFile("testSimulator_datSourceStreamConfiguration.xml");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
