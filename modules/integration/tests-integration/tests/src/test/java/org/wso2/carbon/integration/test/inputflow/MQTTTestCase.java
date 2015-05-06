/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.test.inputflow;

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
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.MQTTEventPublisherClient;
import org.wso2.carbon.integration.test.client.TestAgentServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sending different formatted events to the MQTT Receiver according to the receivers mapping type
 */
public class MQTTTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(MQTTTestCase.class);
    private final String MQTT_CLIENT = "mqtt-client-0.4.0.jar";
    private final String JAR_LOCATION = "/artifacts/CEP/jar";
    private JMSBrokerController activeMqBroker = null;
    private ServerConfigurationManager serverManager = null;

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

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

        String loggedInSessionCookie = getSessionCookie();

        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }


    @Test(groups = {"wso2.cep"}, description = "Testing mqtt receiver with JSON formatted event")
    public void MQTTJSONTestScenario() throws Exception {
        final int messageCount = 3;

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration("inputflows/sample0013", "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add MQTT JSON EventReceiver without mapping
        String eventReceiverConfig = getXMLArtifactConfiguration("inputflows/sample0013", "mqttEventReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig2 = getXMLArtifactConfiguration("inputflows/sample0013", "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig2);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        TestAgentServer agentServer = new TestAgentServer("inputflows/sample0013", 7661, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        MQTTEventPublisherClient.publish("tcp://localhost:1883", "sensordata",
                "inputflows/sample0001", "mqttEvents.txt");

        //wait while all stats are published
        Thread.sleep(30000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("mqttEventReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event.setMetaData(new Object[]{4354643l, false, 701, "temperature"});
        event.setCorrelationData(new Object[]{4.504343, 20.44345});
        event.setPayloadData(new Object[]{2.3f, 4.504343});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event2.setMetaData(new Object[]{4354643l, false, 702, "temperature"});
        event2.setCorrelationData(new Object[]{4.504343, 20.44345});
        event2.setPayloadData(new Object[]{2.3f, 4.504343});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event3.setMetaData(new Object[]{4354643l, false, 703, "temperature"});
        event3.setCorrelationData(new Object[]{4.504343, 20.44345});
        event3.setPayloadData(new Object[]{2.3f, 4.504343});
        eventList.add(event3);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            Assert.assertEquals(agentServer.getPreservedEventList(), eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();

        }
    }

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

    //---- private methods --------

    private void setupMQTTBroker() {
        activeMqBroker = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
        if (!JMSBrokerController.isBrokerStarted()) {
            Assert.assertTrue(activeMqBroker.start(), "MQTT Broker(ActiveMQ) starting failed");
        }
    }

    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }
}
