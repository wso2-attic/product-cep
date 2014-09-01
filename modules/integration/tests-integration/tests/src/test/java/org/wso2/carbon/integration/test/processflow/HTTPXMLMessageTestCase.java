/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
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
import org.wso2.carbon.integration.test.CEPIntegrationTest;
import org.wso2.carbon.integration.test.client.PizzaOrderClient;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

public class HTTPXMLMessageTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(HTTPXMLMessageTestCase.class);
    private ServerConfigurationManager serverManager = null;
    protected final String webAppFileName = "GenericLogService.war";

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception, IOException, XMLStreamException,
                   SAXException, XPathExpressionException, URISyntaxException {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(cepServer);

        try {
            String warFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                                    "artifacts" + File.separator + "CEP" + File.separator + "war"
                                    + File.separator;

            String webAppDirectoryPath = FrameworkPathUtil.getCarbonHome() + File.separator + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "webapps" + File.separator;
            FileManager.copyResourceToFileSystem(warFilePath+webAppFileName, webAppDirectoryPath, webAppFileName);
            Thread.sleep(5000);
        } catch (IOException e) {
            throw new RemoteException("IOException when initializing JMS broker", e);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when restarting server", e);
        }

        String loggedInSessionCookie = new LoginLogoutClient(cepServer).login();
        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);

    }

    @Test(groups = {"wso2.cep"}, description = "Test xml message with http transport")
    public void testHTTPXMLMessage() throws Exception {

        int startEbCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        configurationUtil.addHTTPInputEventAdaptor("httpInputEventAdaptor");
        configurationUtil.addHTTPOutputEventAdaptor("HttpOutputEventAdaptor");
        String eventBuilderConfigPath = getTestArtifactLocation() + "/artifacts/CEP/ebconfigs/PizzaOrder.xml";
        String eventBuilderConfig = getArtifactConfigurationFromClasspath(eventBuilderConfigPath);
        configurationUtil.addStream("org.wso2.sample.pizza.order", "1.0.0", "HTTP_XML");
        eventBuilderAdminServiceClient.addEventBuilderConfiguration(eventBuilderConfig);
        addEventFormatterForHTTPXML();
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), startEbCount + 1);
        Thread.sleep(2000);
        try {

            PizzaOrderClient.sendPizzaOrder("http://localhost:9763/endpoints/httpInputEventAdaptor/PizzaOrder");
            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }


    private void addEventFormatterForHTTPXML() throws Exception {
        int initialEventFormatterCount = eventFormatterAdminServiceClient.getActiveEventFormatterCount();
        String eventFormatterConfigPath = getTestArtifactLocation() + "/artifacts/CEP/efconfigs/PizzaDeliveryNofication.xml";
        String eventFormatterConfig = getArtifactConfigurationFromClasspath(eventFormatterConfigPath);
        configurationUtil.addStream("org.wso2.sample.pizza.order", "1.0.0", "HTTP_XML");
        eventFormatterAdminServiceClient.addEventFormatterConfiguration(eventFormatterConfig);
        Thread.sleep(5000);
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), initialEventFormatterCount + 1);
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
