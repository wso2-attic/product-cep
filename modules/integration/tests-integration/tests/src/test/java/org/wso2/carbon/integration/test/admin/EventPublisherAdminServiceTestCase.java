package org.wso2.carbon.integration.test.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationDto;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;

public class EventPublisherAdminServiceTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(EventPublisherAdminServiceTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing get active event publisher configuration")
    public void testGetActiveEventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0062";

        //Add StreamDefinition
        String streamDefinitionAsString = null;
        try {
            streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "httpJson.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);

            EventPublisherConfigurationDto eventPublisherConfigurationDto =
                    eventPublisherAdminServiceClient.getActiveEventPublisherConfiguration("httpJson");
            Assert.assertEquals(eventPublisherConfigurationDto.getEventPublisherName(),"httpJson");
            Assert.assertEquals(eventPublisherConfigurationDto.getFromStreamNameWithVersion(),"org.wso2.event.sensor.stream:1.0.0");
            Assert.assertEquals(eventPublisherConfigurationDto.getMessageFormat(),"json");
            Assert.assertEquals(eventPublisherConfigurationDto.getCustomMappingEnabled(),false);

            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("httpJson.xml");
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
