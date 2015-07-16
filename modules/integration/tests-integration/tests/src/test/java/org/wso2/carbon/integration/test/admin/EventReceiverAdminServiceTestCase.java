package org.wso2.carbon.integration.test.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.receiver.stub.types.BasicInputAdapterPropertyDto;
import org.wso2.carbon.event.receiver.stub.types.EventMappingPropertyDto;
import org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationDto;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

public class EventReceiverAdminServiceTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(EventReceiverAdminServiceTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = new LoginLogoutClient(cepServer).login();
        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Test get active event receiver configuration")
    public void testGetActiveEventReceiverConfiguration() {
        String samplePath = "inputflows" + File.separator + "sample0003";
        String eventReceiverConfig = null;
        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
            eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);

            EventReceiverConfigurationDto eventReceiverConfigurationDto =
                    eventReceiverAdminServiceClient.getActiveEventReceiverConfiguration("httpReceiver");

            Assert.assertEquals(eventReceiverConfigurationDto.getEventReceiverName(), "httpReceiver");
            Assert.assertEquals(eventReceiverConfigurationDto.getToStreamNameWithVersion(),"org.wso2.event.sensor.stream:1.0.0");

            eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("httpReceiver");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Test deploy Wso2Event receiver configuration")
    public void testDeployWso2EventReceiverConfiguration() {

        String samplePath = "inputflows" + File.separator + "sample0008";

        EventMappingPropertyDto timestamp = new EventMappingPropertyDto();
        timestamp.setName("timestamp");
        timestamp.setType("long");
        timestamp.setValueOf("meta_timestamp");
        EventMappingPropertyDto id = new EventMappingPropertyDto();
        id.setName("sensorId");
        id.setType("int");
        id.setValueOf("meta_id");
        EventMappingPropertyDto isPowerSavingMode = new EventMappingPropertyDto();
        isPowerSavingMode.setName("isPowerSaverEnabled");
        isPowerSavingMode.setType("bool");
        isPowerSavingMode.setValueOf("meta_isPowerSavingMode");
        EventMappingPropertyDto [] metaData = new EventMappingPropertyDto[] {timestamp,id, isPowerSavingMode};

        EventMappingPropertyDto longitude = new EventMappingPropertyDto();
        longitude.setName("longitude");
        longitude.setType("double");
        longitude.setValueOf("correlation_longitude");
        EventMappingPropertyDto latitude = new EventMappingPropertyDto();
        latitude.setName("latitude");
        latitude.setType("double");
        latitude.setValueOf("correlation_latitude");
        EventMappingPropertyDto [] correlationData = new EventMappingPropertyDto[] {longitude, latitude};

        EventMappingPropertyDto humidity = new EventMappingPropertyDto();
        humidity.setName("humidity");
        humidity.setType("float");
        humidity.setValueOf("humidity");
        EventMappingPropertyDto value = new EventMappingPropertyDto();
        value.setName("sensorValue");
        value.setType("double");
        value.setValueOf("value");
        EventMappingPropertyDto[] payloadData = new EventMappingPropertyDto[] {humidity,value};

        BasicInputAdapterPropertyDto propertyDTO = new BasicInputAdapterPropertyDto();
        propertyDTO.setKey("receiving.events.duplicated.in.cluster");
        propertyDTO.setValue("false");

        try {
            String streamDefinitionAsString1 = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString1);
            String streamDefinitionAsString2 = getJSONArtifactConfiguration(samplePath, "org.wso2.mapped.sensor.data_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString2);

            eventReceiverAdminServiceClient.addWso2EventReceiverConfiguration("wso2eventReceiver",
                    "org.wso2.mapped.sensor.data:1.0.0","wso2event",metaData,correlationData,
                    payloadData,new BasicInputAdapterPropertyDto[] {propertyDTO},true,"org.wso2.event.sensor.stream:1.0.0");

            eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("wso2eventReceiver");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.mapped.sensor.data","1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }
    @Test(groups = {"wso2.cep"}, description = "Test get stream attributes")
    public void testGetStreamAttributes() {

    }

    @Test(groups = {"wso2.cep"}, description = "Test deploy text event receiver configuration")
    public void testDeployTextEventReceiverConfiguration() {
        
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
