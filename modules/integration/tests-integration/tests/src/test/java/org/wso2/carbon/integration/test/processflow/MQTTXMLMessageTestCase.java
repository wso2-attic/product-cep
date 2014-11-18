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
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.SimpleMqttPublisher;
import org.wso2.carbon.integration.test.client.SimpleMqttSubscriber;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

public class MQTTXMLMessageTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(MQTTXMLMessageTestCase.class);
    private final String MQTT_CLIENT = "mqtt-client-0.4.0.jar";
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
            throw new RemoteException("Malformed URL exception thrown when initializing Mqtt broker", e);
        }
        setupMQTTBroker();
        //copying dependency mqtt jar files to component/lib
        try {
            serverManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + File.separator + MQTT_CLIENT).toURI()));
            serverManager.restartGracefully();
        } catch (IOException e) {
            throw new RemoteException("IOException when initializing Mqtt broker", e);
        } catch (URISyntaxException e) {
            throw new RemoteException("URISyntaxException when initializing Mqtt broker", e);
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

    private void setupMQTTBroker() {
        activeMqBroker = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
        if (!JMSBrokerController.isBrokerStarted()) {
            Assert.assertTrue(activeMqBroker.start(), "MQTT Broker(ActiveMQ) starting failed");
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Test text message with mqtt transport")
    public void testJMSTextMessage() throws Exception {
        String publisherTopicName = "sensorData";
        String subscriberTopicName = "sensorOutputStream";
        String url = "tcp://localhost:1883";
        addMQTTInputEventAdaptor("mqttInputAdaptor");
        addMQTTOutputEventAdaptor("mqttOutputAdaptor");

        int startEbCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        String eventBuilderConfigPath = getTestArtifactLocation() + "/artifacts/CEP/ebconfigs/mqttEventBuilder.xml";
        String eventBuilderConfig = getArtifactConfigurationFromClasspath(eventBuilderConfigPath);
        configurationUtil.addStream("org.wso2.sensorStream", "1.0.0", "MQTT_XML");
        eventBuilderAdminServiceClient.addEventBuilderConfiguration(eventBuilderConfig);

        String eventFormatterConfigPath = getTestArtifactLocation() + "/artifacts/CEP/efconfigs/mqttEventFormatter.xml";
        String eventFormatterConfig = getArtifactConfigurationFromClasspath(eventFormatterConfigPath);
        eventFormatterAdminServiceClient.addEventFormatterConfiguration(eventFormatterConfig);

        Thread.sleep(1000 * 60);

        SimpleMqttSubscriber simpleMqttSubscriber = new SimpleMqttSubscriber(publisherTopicName, url);
        simpleMqttSubscriber.runClient();

        SimpleMqttPublisher simpleMqttPublisher = new SimpleMqttPublisher(subscriberTopicName, url);

        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), startEbCount + 1);

        String[] inputMsgs = {"<events>\n" +
                              "    <event>\n" +
                              "        <metaData>\n" +
                              "            <sensorType>TemperatureSensor</sensorType>\n" +
                              "        </metaData>\n" +
                              "        <payloadData>\n" +
                              "            <sensorId>ID1</sensorId>\n" +
                              "            <sensorValue>4.504343</sensorValue>\n" +
                              "        </payloadData>\n" +
                              "    </event>\n" +
                              "</events>"};

        try {
            simpleMqttPublisher.runClient(inputMsgs);

            Thread.sleep(10000000);
            Assert.assertEquals(simpleMqttSubscriber.getCount(), 1, "Incorrect number of messages consumed!");

            eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("mqttEventFormatter");
            eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("mqttEventBuilder");
            inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("mqttInputAdaptor");
            outputEventAdaptorManagerAdminServiceClient.removeActiveOutputEventAdaptorConfiguration("mqttOutputAdaptor");

            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    /**
     * **************************************************************************************************************
     * Start of config deployment methods
     */
    private void addMQTTInputEventAdaptor(String adaptorName) throws RemoteException {
        InputEventAdaptorPropertyDto mqttServerUrl = new InputEventAdaptorPropertyDto();
        mqttServerUrl.setKey("url");
        mqttServerUrl.setValue("tcp://localhost:1883");

        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration(adaptorName, "mqtt", new InputEventAdaptorPropertyDto[]{mqttServerUrl});
    }

    private void addMQTTOutputEventAdaptor(String adaptorName) throws RemoteException {
        OutputEventAdaptorPropertyDto mqttServerUrl = new OutputEventAdaptorPropertyDto();
        mqttServerUrl.setKey("url");
        mqttServerUrl.setValue("tcp://localhost:1883");

        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration(adaptorName, "mqtt", new OutputEventAdaptorPropertyDto[]{mqttServerUrl});
    }

    /*
        End of config deployment methods
     */

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            Thread.sleep(5000);
            if (activeMqBroker != null) {
                activeMqBroker.stop();
            }

            //let server to clear the artifact undeployment
            Thread.sleep(5000);
        } finally {

            //reverting the changes done to cep sever
            if (serverManager != null) {
                serverManager.removeFromComponentLib(MQTT_CLIENT);
                serverManager.restoreToLastConfiguration();
            }

        }
        super.cleanup();
    }

    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }

}
