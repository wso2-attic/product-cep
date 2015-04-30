/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.cep.integration.common.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.JSONParser;
import org.wso2.appserver.integration.common.clients.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.regex.Matcher;

public abstract class CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(CEPIntegrationTest.class);
    protected AutomationContext cepServer;
    protected String backendURL;
    protected ConfigurationUtil configurationUtil;
    protected EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    protected EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;

    protected EventReceiverAdminServiceClient eventReceiverAdminServiceClient;
    protected EventPublisherAdminServiceClient eventPublisherAdminServiceClient;

    protected ExecutionManagerAdminServiceClient executionManagerAdminServiceClient;

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

    protected void gracefullyRestartServer() throws Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(cepServer);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * @param testCaseFolderName Name of the folder created under /artifacts/CEP for the particular test case.
     * @param configFileName Name of the XML config-file created under above folder.
     * @return The above XML-configuration, as a string
     * @throws Exception
     */
    protected String getXMLArtifactConfiguration(String testCaseFolderName, String configFileName)
            throws Exception {
        String relativeFilePath = getTestArtifactLocation() + "/artifacts/CEP/"+testCaseFolderName+"/"+configFileName;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        OMElement configElement = loadClasspathResourceXML(relativeFilePath);
        return configElement.toString();
    }

    /**
     * @param testCaseFolderName testCaseFolderName Name of the folder created under /artifacts/CEP for the particular test case.
     * @param configFileName Name of the JSON config-file created under above folder.
     * @return The above JSON-configuration, as a string
     * @throws Exception
     */
    protected String getJSONArtifactConfiguration(String testCaseFolderName, String configFileName)
            throws Exception {
        String relativeFilePath = getTestArtifactLocation() + "/artifacts/CEP/"+testCaseFolderName+"/"+configFileName;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        JSONParser jsonParser = new JSONParser();
        return jsonParser.parse(new FileReader(relativeFilePath)).toString();
    }

    /**
     * Returns the execution plan, read from the given file path.
     * @param testCaseFolderName testCaseFolderName Name of the folder created under /artifacts/CEP for the particular test case.
     * @param executionPlanFileName Execution plan file name, relative to the test artifacts folder.
     * @return execution plan as a string.
     * @throws Exception
     */
    protected String getExecutionPlanFromFile(String testCaseFolderName, String executionPlanFileName)
            throws Exception {
        String relativeFilePath = getTestArtifactLocation() + "/artifacts/CEP/"+testCaseFolderName+"/"+executionPlanFileName;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        return ConfigurationUtil.readFile(relativeFilePath) ;
    }

    public OMElement loadClasspathResourceXML(String path) throws FileNotFoundException, XMLStreamException {
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
