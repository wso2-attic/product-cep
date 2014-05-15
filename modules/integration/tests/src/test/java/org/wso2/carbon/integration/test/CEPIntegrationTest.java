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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.aar.services.AARServiceUploaderClient;
import org.wso2.carbon.automation.api.clients.cep.*;
import org.wso2.carbon.automation.api.clients.security.SecurityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.api.clients.service.mgt.ServiceAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.cep.CEPTestCaseUtils;
import org.wso2.carbon.automation.utils.services.ServiceDeploymentUtil;
import org.wso2.carbon.integration.test.util.ConfigurationUtil;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

import java.io.File;
import java.rmi.RemoteException;
import java.util.regex.Matcher;

public class CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(CEPIntegrationTest.class);
    protected EnvironmentVariables cepServer;
    protected UserInfo userInfo;
    protected SecurityAdminServiceClient securityAdminServiceClient;
    protected ConfigurationUtil configurationUtil;
    protected CEPTestCaseUtils cepUtils;
    protected EventBuilderAdminServiceClient eventBuilderAdminServiceClient;
    protected EventFormatterAdminServiceClient eventFormatterAdminServiceClient;
    protected EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    protected InputEventAdaptorManagerAdminServiceClient inputEventAdaptorManagerAdminServiceClient;
    protected OutputEventAdaptorManagerAdminServiceClient outputEventAdaptorManagerAdminServiceClient;
    protected EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;

    protected void init() throws RemoteException, LoginAuthenticationExceptionException {
        init(2);
    }

    protected void init(int userId) throws RemoteException, LoginAuthenticationExceptionException {
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().cep(userId);
        cepServer = builder.build().getCep();
        configurationUtil = ConfigurationUtil.getConfigurationUtil();
        cepUtils = new CEPTestCaseUtils();
    }

    protected void cleanup() {
        userInfo = null;
        cepServer = null;
        configurationUtil = null;
    }

    protected String getServiceUrl(String serviceName) {
        return cepServer.getServiceUrl() + "/" + serviceName;
    }

    protected String getServiceUrlHttps(String serviceName) {
        return cepServer.getSecureServiceUrl() + "/" + serviceName;
    }

    protected void deployAarService(String serviceName, String fileNameWithExtension,
                                    String filePath, String serviceHierarchy)
            throws Exception {
        AARServiceUploaderClient aarServiceUploaderClient =
                new AARServiceUploaderClient(cepServer.getBackEndUrl(), cepServer.getSessionCookie());
        aarServiceUploaderClient.uploadAARFile(fileNameWithExtension, filePath, serviceHierarchy);

        ServiceDeploymentUtil.isServiceDeployed(cepServer.getBackEndUrl(), cepServer.getSessionCookie(), serviceName);
        Assert.assertTrue(ServiceDeploymentUtil.
                isServiceDeployed(cepServer.getBackEndUrl(), cepServer.getSessionCookie(), serviceName),
                "Service file uploading failed withing given deployment time");
    }

    protected void deleteService(String serviceName) throws RemoteException {
        ServiceAdminClient adminServiceService =
                new ServiceAdminClient(cepServer.getBackEndUrl(), cepServer.getSessionCookie());
        if (ServiceDeploymentUtil.isFaultyService(cepServer.getBackEndUrl(),
                cepServer.getSessionCookie(), serviceName)) {
            adminServiceService.deleteFaultyServiceByServiceName(serviceName);

        } else if (ServiceDeploymentUtil.isServiceExist(cepServer.getBackEndUrl(),
                cepServer.getSessionCookie(), serviceName)) {
            adminServiceService.deleteService(new String[]{adminServiceService.getServiceGroup(serviceName)});
        }
        ServiceDeploymentUtil.isServiceDeleted(cepServer.getBackEndUrl(), cepServer.getSessionCookie(), serviceName);
    }

    protected void gracefullyRestartServer() throws Exception {
        ServerAdminClient serverAdminClient = new ServerAdminClient(cepServer.getBackEndUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.CEP_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);

    }

    protected boolean isServiceDeployed(String serviceName) throws RemoteException {
        return ServiceDeploymentUtil.isServiceDeployed(cepServer.getBackEndUrl(),
                cepServer.getSessionCookie(), serviceName);
    }

    protected boolean isServiceFaulty(String serviceName) throws RemoteException {
        return ServiceDeploymentUtil.isServiceFaulty(cepServer.getBackEndUrl(),
                cepServer.getSessionCookie(), serviceName);
    }

    protected String getSecuredServiceEndpoint(String serviceName) {
        return cepServer.getSecureServiceUrl() + "/" + serviceName;
    }

    protected void applySecurity(String scenarioNumber, String serviceName, String userGroup)
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException,
            InterruptedException {

        EnvironmentBuilder builder = new EnvironmentBuilder();
        securityAdminServiceClient =
                new SecurityAdminServiceClient(cepServer.getBackEndUrl(), cepServer.getSessionCookie());

        String path = builder.getFrameworkSettings().getEnvironmentVariables().getKeystorePath();
        String KeyStoreName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
        securityAdminServiceClient.applySecurity(serviceName, scenarioNumber, new String[]{userGroup},
                new String[]{KeyStoreName}, KeyStoreName);
        Thread.sleep(2000);
    }

    protected String getArtifactConfigurationFromClasspath(String relativeFilePath) throws Exception {
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        OMElement configElement = cepUtils.loadClasspathResource(relativeFilePath);
        return configElement.toString();
    }


}
