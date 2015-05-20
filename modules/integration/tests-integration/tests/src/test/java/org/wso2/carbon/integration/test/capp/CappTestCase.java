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
    protected final String cAppFailureFileName = "TestCAPPFailure.car";

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
        int startEPCCount = eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount();

        String carFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "CEP" + File.separator + "car"
                + File.separator;

        String cAppDirectoryPath = FrameworkPathUtil.getCarbonHome() + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server"
                + File.separator + "carbonapps" + File.separator;
        try {
            FileManager.copyResourceToFileSystem(carFilePath + cAppFileName, cAppDirectoryPath, cAppFileName);
        }  catch (Exception e) {
            throw new RemoteException("Exception caught when deploying the car file into CEP server", e);
        }
        log.info("deploying cApp...");
        Thread.sleep(35000);

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 2);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), startEPCCount + 1);

        try {
            FileManager.deleteFile(cAppDirectoryPath + cAppFileName);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when deleting the car file from CEP server", e);
        }
        log.info("undeploying cApp...");
        Thread.sleep(20000);

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), startEPCCount);

    }

    @Test(groups = {"wso2.cep"} ,description = "Test car file failure deployment")
    public void testFailureCAppDeployment() throws Exception {

        int startESCount  = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount  = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount  = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        int startEPCCount = eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount();

        int startInactiveERCount  = eventReceiverAdminServiceClient.getInactiveEventReceiverCount();
        int startInactiveEPCount  = eventPublisherAdminServiceClient.getInactiveEventPublisherCount();
        int startInactiveEPCCount = eventProcessorAdminServiceClient.getInactiveExecutionPlanConfigurationCount();

        String carFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "CEP" + File.separator + "car"
                + File.separator;

        String cAppDirectoryPath = FrameworkPathUtil.getCarbonHome() + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server"
                + File.separator + "carbonapps" + File.separator;
        try {
            FileManager.copyResourceToFileSystem(carFilePath + cAppFailureFileName, cAppDirectoryPath, cAppFailureFileName);
        }  catch (Exception e) {
            throw new RemoteException("Exception caught when deploying the car file into CEP server", e);
        }

        Thread.sleep(35000);

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), startEPCCount);

        Assert.assertEquals(eventReceiverAdminServiceClient.getInactiveEventReceiverCount(), startInactiveERCount);
        Assert.assertEquals(eventPublisherAdminServiceClient.getInactiveEventPublisherCount(), startInactiveEPCount);
        Assert.assertEquals(eventProcessorAdminServiceClient.getInactiveExecutionPlanConfigurationCount(), startInactiveEPCCount);

        try {
            FileManager.deleteFile(cAppDirectoryPath + cAppFailureFileName);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when deleting the car file from CEP server", e);
        }

        Thread.sleep(20000);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
