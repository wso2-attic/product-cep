package org.wso2.carbon.integration.test.admin;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.simulator.stub.types.StreamDefinitionInfoDto;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

public class EventSimulatorAdminServiceTestCase extends CEPIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();
        eventStreamManagerAdminServiceClient = configurationUtil
                .getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventSimulatorAdminServiceClient = configurationUtil
                .getEventSimulatorAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Test get all event stream info dtos")
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
            e.printStackTrace();
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
