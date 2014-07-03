/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.integration.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appserver.integration.common.clients.EventBuilderAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventFormatterAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventProcessorAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventStreamManagerAdminServiceClient;
import org.wso2.appserver.integration.common.clients.InputEventAdaptorManagerAdminServiceClient;
import org.wso2.appserver.integration.common.clients.OutputEventAdaptorManagerAdminServiceClient;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.util.ConfigurationUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;

public class CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(CEPIntegrationTest.class);
    protected AutomationContext cepServer;
    protected String backendURL;
    protected ConfigurationUtil configurationUtil;
//    protected CEPTestCaseUtils cepUtils;
    protected EventBuilderAdminServiceClient eventBuilderAdminServiceClient;
    protected EventFormatterAdminServiceClient eventFormatterAdminServiceClient;
    protected EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    protected InputEventAdaptorManagerAdminServiceClient inputEventAdaptorManagerAdminServiceClient;
    protected OutputEventAdaptorManagerAdminServiceClient outputEventAdaptorManagerAdminServiceClient;
    protected EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    protected void init(TestUserMode testUserMode) throws Exception {
        cepServer = new AutomationContext("CEP", testUserMode);
        configurationUtil = ConfigurationUtil.getConfigurationUtil();
        backendURL = cepServer.getContextUrls().getBackEndUrl();
    }

    protected String getSessionCookie() throws Exception {
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(cepServer);
        return loginLogoutClient.login();
    }

    protected void cleanup() {
        cepServer = null;
        configurationUtil = null;
    }

    protected String getServiceUrl(String serviceName) throws XPathExpressionException {
        return cepServer.getContextUrls().getServiceUrl() + "/" + serviceName;
    }

    protected String getServiceUrlHttps(String serviceName) throws XPathExpressionException {
        return cepServer.getContextUrls().getSecureServiceUrl() + "/" + serviceName;
    }

    protected String getTestArtifactLocation() {
        return FrameworkPathUtil.getSystemResourceLocation();
    }

//    protected void deployAarService(String serviceName, String fileNameWithExtension,
//                                    String filePath, String serviceHierarchy)
//            throws Exception {
//        AARServiceUploaderClient aarServiceUploaderClient =
//                new AARServiceUploaderClient(cepServer.getBackEndUrl(), cepServer.getSessionCookie());
//        aarServiceUploaderClient.uploadAARFile(fileNameWithExtension, filePath, serviceHierarchy);
//
//        ServiceDeploymentUtil.isServiceDeployed(cepServer.getBackEndUrl(), cepServer.getSessionCookie(), serviceName);
//        Assert.assertTrue(ServiceDeploymentUtil.
//                isServiceDeployed(cepServer.getBackEndUrl(), cepServer.getSessionCookie(), serviceName),
//                          "Service file uploading failed withing given deployment time");
//    }
//
//    protected void deleteService(String serviceName) throws RemoteException {
//        ServiceAdminClient adminServiceService =
//                new ServiceAdminClient(cepServer.getBackEndUrl(), cepServer.getSessionCookie());
//        if (ServiceDeploymentUtil.isFaultyService(cepServer.getBackEndUrl(),
//                                                  cepServer.getSessionCookie(), serviceName)) {
//            adminServiceService.deleteFaultyServiceByServiceName(serviceName);
//
//        } else if (ServiceDeploymentUtil.isServiceExist(cepServer.getBackEndUrl(),
//                                                        cepServer.getSessionCookie(), serviceName)) {
//            adminServiceService.deleteService(new String[]{adminServiceService.getServiceGroup(serviceName)});
//        }
//        ServiceDeploymentUtil.isServiceDeleted(cepServer.getBackEndUrl(), cepServer.getSessionCookie(), serviceName);
//    }

    protected void gracefullyRestartServer() throws Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(cepServer);
        serverConfigurationManager.restartGracefully();
    }

//    protected boolean isServiceDeployed(String serviceName) throws RemoteException {
//        return ServiceDeploymentUtil.isServiceDeployed(cepServer.getBackEndUrl(),
//                                                       cepServer.getSessionCookie(), serviceName);
//    }
//
//    protected boolean isServiceFaulty(String serviceName) throws RemoteException {
//        return ServiceDeploymentUtil.isServiceFaulty(cepServer.getBackEndUrl(),
//                                                     cepServer.getSessionCookie(), serviceName);
//    }
//
//    protected String getSecuredServiceEndpoint(String serviceName) {
//        return cepServer.getSecureServiceUrl() + "/" + serviceName;
//    }
//
//    protected void applySecurity(String scenarioNumber, String serviceName, String userGroup)
//            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException,
//                   InterruptedException {
//
//        EnvironmentBuilder builder = new EnvironmentBuilder();
//        securityAdminServiceClient =
//                new SecurityAdminServiceClient(cepServer.getBackEndUrl(), cepServer.getSessionCookie());
//
//        String path = builder.getFrameworkSettings().getEnvironmentVariables().getKeystorePath();
//        String KeyStoreName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
//        securityAdminServiceClient.applySecurity(serviceName, scenarioNumber, new String[]{userGroup},
//                                                 new String[]{KeyStoreName}, KeyStoreName);
//        Thread.sleep(2000);
//    }
//
    protected String getArtifactConfigurationFromClasspath(String relativeFilePath)
            throws Exception {
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        OMElement configElement = loadClasspathResource(relativeFilePath);
        return configElement.toString();
    }

    public OMElement loadClasspathResource(String path) throws FileNotFoundException,
                                                               XMLStreamException {
        OMElement documentElement = null;
        FileInputStream inputStream = null;
        XMLStreamReader parser = null;
        StAXOMBuilder builder = null;
        File file = new File(path);
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
                parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
                //create the builder
                builder = new StAXOMBuilder(parser);
                //get the root element (in this case the envelope)
                documentElement = builder.getDocumentElement().cloneOMElement();
            } finally {
                if (builder != null) {
                    builder.close();
                }
                if (parser != null) {
                    try {
                        parser.close();
                    } catch (XMLStreamException e) {
                        //ignore
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }

            }
        } else {
            throw new FileNotFoundException("File does not exist at " + path);
        }
        return documentElement;
    }





}
