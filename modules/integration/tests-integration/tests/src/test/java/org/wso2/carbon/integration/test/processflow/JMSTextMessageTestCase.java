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
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSTopicMessagePublisher;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.event.formatter.stub.types.EventOutputPropertyConfigurationDto;
import org.wso2.carbon.event.formatter.stub.types.PropertyDto;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.CEPIntegrationTest;
import org.wso2.carbon.integration.test.client.TestAgentServer;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

public class JMSTextMessageTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(JMSTextMessageTestCase.class);
    private final String ACTIVEMQ_CORE = "activemq-core-5.7.0.jar";
    private final String GERONIMO_J2EE_MANAGEMENT = "geronimo-j2ee-management_1.1_spec-1.0.1.jar";
    private final String JAR_LOCATION = "/artifacts/CEP/jar";
    private JMSBrokerController activeMqBroker = null;
    private ServerConfigurationManager serverManager = null;

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception, IOException, XMLStreamException,
                   SAXException, XPathExpressionException, URISyntaxException {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        try {
            serverManager = new ServerConfigurationManager(cepServer);
        } catch (MalformedURLException e) {
            throw new RemoteException("Malformed URL exception thrown when initializing JMS broker", e);
        }
        setupJmsBroker();
        //copying dependency jms jar files to component/lib
        try {
            serverManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + File.separator + ACTIVEMQ_CORE).toURI()));
            serverManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + File.separator + GERONIMO_J2EE_MANAGEMENT).toURI()));
            serverManager.restartGracefully();
        } catch (IOException e) {
            throw new RemoteException("IOException when initializing JMS broker", e);
        } catch (URISyntaxException e) {
            throw new RemoteException("URISyntaxException when initializing JMS broker", e);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when restarting server", e);
        }

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = new LoginLogoutClient(cepServer).login();
        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);

    }

    private void setupJmsBroker() {
        //starting jms broker
        activeMqBroker = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
        if (!JMSBrokerController.isBrokerStarted()) {
            Assert.assertTrue(activeMqBroker.start(), "JMS Broker(ActiveMQ) starting failed");
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Test text message with jms transport")
    public void testJMSTextMessage() throws Exception {
        String topicName = "AccessLogs";

        int startEbCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        configurationUtil.addJmsInputEventAdaptor("jmsEventReceiver");
        configurationUtil.addThriftOutputEventAdaptor();
        String eventBuilderConfigPath = getTestArtifactLocation()+"/artifacts/CEP/ebconfigs/AccessLogs.xml";
        String eventBuilderConfig = getArtifactConfigurationFromClasspath(eventBuilderConfigPath);
        configurationUtil.addStream("org.wso2.test.inflow", "1.0.0", "JMS_TEXT");
        configurationUtil.addStream("access.log.stream", "1.0.0", "JMS_TEXT_OUT");
        eventBuilderAdminServiceClient.addEventBuilderConfiguration(eventBuilderConfig);

        addEventFormatterForJmsText();

        TestAgentServer testAgentServer = new TestAgentServer();
        Thread agentServerThread = new Thread(testAgentServer);
        agentServerThread.start();

        Thread.sleep(1000);

        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), startEbCount + 1);

        Thread.sleep(1000 * 60);

        JMSTopicMessagePublisher sender = new JMSTopicMessagePublisher(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String[] inputMsgs = {"127.0.0.1 - - [18/Nov/2013:17:16:24 +0530] \"GET /carbon/admin/jsp/registry_styles_ajaxprocessor.jsp HTTP/1.1\" 200 123 \"https://localhost:9443/carbon/admin/index.jsp?loginStatus=true\" \"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:25.0) Gecko/20100101 Firefox/25.0\" fileName: http_access.log",
                              "127.0.0.1 - - [18/Nov/2013:17:16:23 +0530] \"POST /carbon/admin/login_action.jsp HTTP/1.1\" 302 456 \"https://localhost:9443/carbon/admin/login.jsp\" \"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:25.0) Gecko/20100101 Firefox/25.0\" fileName: http_access.log"
                , "127.0.0.1 - - [18/Nov/2013:17:16:24 +0530] \"GET /carbon/admin/index.jsp?loginStatus=true HTTP/1.1\" 200 4079 \"https://localhost:9443/carbon/admin/login.jsp\" \"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:25.0) Gecko/20100101 Firefox/25.0\" fileName: http_access.log"};

        try {
            sender.connect(topicName);
            for (int i = 0; i < 3; i++) {
                sender.publish(inputMsgs[i]);
            }
            Thread.sleep(1000);
            Assert.assertEquals(testAgentServer.getMsgCount(), 3, "Incorrect number of messages consumed!");

            eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("wso2eventformatter");
            eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("AccessLogs");
            inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("jmsEventReceiver");
            configurationUtil.removeThriftOutputEventAdaptor();

            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            testAgentServer.stop();
            sender.disconnect();
        }
    }

    private void addEventFormatterForJmsText() throws RemoteException {
        EventOutputPropertyConfigurationDto eventOutFilename = new EventOutputPropertyConfigurationDto();
        eventOutFilename.setName("filename");
        eventOutFilename.setValueOf("meta_filename");
        eventOutFilename.setType("string");
        EventOutputPropertyConfigurationDto[] metaEventOutputPropertyConfigurationDtos = new EventOutputPropertyConfigurationDto[]{eventOutFilename};

        EventOutputPropertyConfigurationDto eventOutTimestamp = new EventOutputPropertyConfigurationDto();
        eventOutTimestamp.setName("access_time");
        eventOutTimestamp.setValueOf("timestamp");
        eventOutTimestamp.setType("string");
        EventOutputPropertyConfigurationDto eventOutUrl = new EventOutputPropertyConfigurationDto();
        eventOutUrl.setName("access_url");
        eventOutUrl.setValueOf("url");
        eventOutUrl.setType("string");
        EventOutputPropertyConfigurationDto eventOutResponseTime = new EventOutputPropertyConfigurationDto();
        eventOutResponseTime.setName("response_time");
        eventOutResponseTime.setValueOf("response_time");
        eventOutResponseTime.setType("int");
        EventOutputPropertyConfigurationDto[] payloadEventOutputPropertyConfigurationDtos = new EventOutputPropertyConfigurationDto[]{eventOutTimestamp, eventOutUrl, eventOutResponseTime};


        PropertyDto streamName = new PropertyDto();
        streamName.setKey("stream");
        streamName.setValue("access.log.stream");

        PropertyDto streamVersion = new PropertyDto();
        streamVersion.setKey("version");
        streamVersion.setValue("1.0.0");

        PropertyDto eventFormatterPropertyDtos[] = new PropertyDto[]{streamName, streamVersion};

        eventFormatterAdminServiceClient.addWso2EventFormatterConfiguration("wso2eventformatter", "org.wso2.test.inflow:1.0.0", "DefaultWSO2EventOutputAdaptor", "wso2event", metaEventOutputPropertyConfigurationDtos, null, payloadEventOutputPropertyConfigurationDtos, eventFormatterPropertyDtos, true);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            Thread.sleep(5000);
            if (activeMqBroker != null) {
                activeMqBroker.stop();
            }
            Thread.sleep(5000); //let server to clear the artifact undeployment
        } finally {
            //reverting the changes done to cep sever
            if (serverManager != null) {
                serverManager.removeFromComponentLib(ACTIVEMQ_CORE);
                serverManager.removeFromComponentLib(GERONIMO_J2EE_MANAGEMENT);
                serverManager.restoreToLastConfiguration();
            }

        }
        super.cleanup();
    }

    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }

}
