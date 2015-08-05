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
import org.wso2.carbon.event.simulator.stub.types.StreamDefinitionInfoDto;
import org.wso2.carbon.integration.common.admin.client.NDataSourceAdminServiceClient;
import org.wso2.carbon.integration.test.client.H2DatabaseClient;
import org.wso2.carbon.integration.test.client.util.BasicDataSource;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo_WSDataSourceDefinition;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.rmi.RemoteException;

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
                .getEventPublisherAdminServiceClient(backendURL,loggedInSessionCookie);

        NDataSourceAdminServiceClient dataSourceAdminService =
                new NDataSourceAdminServiceClient(backendURL, loggedInSessionCookie);
        WSDataSourceMetaInfo dataSourceInfo = getDataSourceInformation("WSO2CEP_DB");
        dataSourceAdminService.addDataSource(dataSourceInfo);
    }

    /*@Test(groups = {"wso2.cep"}, description = "Test get all event stream info dtos")
    public void testGetAllEventStreamInfoDto() {
        String samplePath = "inputflows" + File.separator + "sample0003";
        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            StreamDefinitionInfoDto[] allEventStreamInfoDto = eventSimulatorAdminServiceClient.getAllEventStreamInfoDto();
            Assert.assertEquals(allEventStreamInfoDto.length, 1);
            Assert.assertEquals(allEventStreamInfoDto[0].getStreamName(), "org.wso2.event.sensor.stream");
            Assert.assertEquals(allEventStreamInfoDto[0].getStreamVersion(), "1.0.0");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }*/


    @Test(groups = {"wso2.cep"}, description = "Test get all event stream info dtos")
    public void testSimulateRDBMSDataSourceConnection() {
        try {
            String samplePath = "outputflows" + File.separator + "sample0072";

            //Add StreamDefinition
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);

            //Add RDBMS publisher
            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "rdbmsEventPublisher.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);

            EventDto eventDto = new EventDto();
            eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
            eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324", "100.34", "23.4545"});

            eventSimulatorAdminServiceClient.sendEvent(eventDto);
            Thread.sleep(1000);

            eventSimulatorAdminServiceClient.testSimulateRDBMSDataSourceConnection(
                "{\n" +
                "    \"dataSource\"                 : \"WSO2CEP_DB\",\n" +
                "    \"eventStreamName\"            : \"org.wso2.event.sensor.stream\",\n" +
                "    \"streamID\"                   : \"org.wso2.event.sensor.stream:1.0.0\",\n" +
                "    \"name\"                       : \"testSimulator\",\n" +
                "    \"tableName\"                  : \"sensordata\",\n" +
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
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("rdbmsEventPublisher.xml");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    /*@Test(groups = {"wso2.cep"}, description = "Test get all dataSource table and stream info")
    public void testGetAllDataSourceTableAndStreamInfo() {
        try {
            DataSourceTableAndStreamInfoDto[] allDataSourceTableAndStreamInfo =
                    eventSimulatorAdminServiceClient.getAllDataSourceTableAndStreamInfo();

        } catch (RemoteException e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }*/

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

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
