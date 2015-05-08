package org.wso2.carbon.integration.test.capp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

public class CappTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(CappTestCase.class);
    private ServerConfigurationManager serverManager = null;
    protected final String cAppFileName = "TestCAPP.car";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(cepServer);


        String loggedInSessionCookie = new LoginLogoutClient(cepServer).login();
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Test car file deployment")
    public void testCAppDeployment() throws Exception {

        int startESCount  = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount  = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount  = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        int startEPCCount = eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount();

        try {
            String carFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                    "artifacts" + File.separator + "CEP" + File.separator + "car"
                    + File.separator;

            String webAppDirectoryPath = FrameworkPathUtil.getCarbonHome() + File.separator
                    + "repository" + File.separator + "deployment" + File.separator + "server"
                    + File.separator + "carbonapps" + File.separator;
            FileManager.copyResourceToFileSystem(carFilePath + cAppFileName, webAppDirectoryPath, cAppFileName);
            Thread.sleep(20000);
        }  catch (Exception e) {
            throw new RemoteException("Exception caught when deploying the car file into CEP server", e);
        }

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 2);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), startEPCCount + 1);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
        } finally {
            //reverting the changes done to cep sever
            if (serverManager != null) {
                serverManager.restoreToLastConfiguration();
            }
        }
        super.cleanup();
    }
}
