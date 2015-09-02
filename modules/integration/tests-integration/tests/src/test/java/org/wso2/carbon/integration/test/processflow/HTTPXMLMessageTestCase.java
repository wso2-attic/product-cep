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

package org.wso2.carbon.integration.test.processflow;

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
import org.wso2.carbon.integration.test.client.PizzaOrderClient;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.rmi.RemoteException;

public class HTTPXMLMessageTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(HTTPXMLMessageTestCase.class);
    private ServerConfigurationManager serverManager = null;
    protected final String webAppFileName = "GenericLogService.war";
    private String webAppDirectoryPath = null;

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(cepServer);

        try {
            String warFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                                 "artifacts" + File.separator + "CEP" + File.separator + "war"
                                 + File.separator;

            webAppDirectoryPath = FrameworkPathUtil.getCarbonHome() + File.separator + "repository" + File.separator +
                                  "deployment" + File.separator + "server" + File.separator + "webapps" + File.separator;
            FileManager.copyResourceToFileSystem(warFilePath + webAppFileName, webAppDirectoryPath, webAppFileName);
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when deploying the war file into CEP server", e);
        }

        String loggedInSessionCookie = new LoginLogoutClient(cepServer).login();
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }


    @Test(groups = {"wso2.cep"}, description = "Test xml message with http transport")
    public void testHTTPXMLMessage() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        //Add StreamDefinition
        String streamDefinition = getJSONArtifactConfiguration("HTTPXMLMessageTestCase", "org.wso2.sample.pizza.order_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add HTTP EventReceiver
        String eventReceiverConfig = getXMLArtifactConfiguration("HTTPXMLMessageTestCase", "PizzaOrder.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add HTTP Publisher
        String eventPublisherConfig = getXMLArtifactConfiguration("HTTPXMLMessageTestCase", "PizzaDeliveryNotification.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        //Send events
        Thread.sleep(2000);
        try {
            PizzaOrderClient.sendPizzaOrder("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                            "/endpoints/httpInputEventAdaptor/PizzaOrder");
            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

        eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("PizzaDeliveryNotification");
        eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("PizzaOrder");
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order", "1.0.0");
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        if (webAppDirectoryPath != null) {
            FileManager.deleteFile(webAppDirectoryPath + webAppFileName);
        }
        super.cleanup();
    }
}
