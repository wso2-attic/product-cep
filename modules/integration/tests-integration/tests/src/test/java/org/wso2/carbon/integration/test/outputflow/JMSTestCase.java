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
package org.wso2.carbon.integration.test.outputflow;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.axis2.AxisFault;
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
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.JMSConsumerClient;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Sending different formatted events to the JMS Publisher and consume using a client
 */
public class JMSTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(JMSTestCase.class);
    private final String GERONIMO_J2EE_MANAGEMENT = "geronimo-j2ee-management_1.1_spec-1.0.1.jar";
    private final String ACTIVEMQ_CORE = "activemq-core-5.7.0.jar";
    private JMSBrokerController activeMqBroker = null;
    private ServerConfigurationManager serverManager = null;

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        try {
            serverManager = new ServerConfigurationManager(cepServer);
        } catch (MalformedURLException e) {
            throw new RemoteException("Malformed URL exception thrown when initializing ActiveMQ broker", e);
        }

        setupActiveMQBroker();
        //copying dependency activemq jar files to component/lib
        try {
            String JAR_LOCATION = CEPIntegrationTestConstants.RELATIVE_PATH_TO_TEST_ARTIFACTS + "jar";
            serverManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + File.separator +
                                                                             ACTIVEMQ_CORE).toURI()));
            serverManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + File.separator +
                                                                             GERONIMO_J2EE_MANAGEMENT).toURI()));
            serverManager.restartGracefully();
        } catch (IOException e) {
            throw new RemoteException("IOException when initializing ActiveMQ broker", e);
        } catch (URISyntaxException e) {
            throw new RemoteException("URISyntaxException when initializing ActiveMQ broker", e);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when restarting server", e);
        }

        String loggedInSessionCookie = getSessionCookie();

        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(
                backendURL, loggedInSessionCookie);
        Thread.sleep(45000);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing activemq jms publisher with Map formatted event with default mapping")
    public void jmsMapTestWithDefaultMappingScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "outputflows" + File.separator + "sample0059";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                                                 "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "101", "temperature", "23.45656", "7.12324",
                                                  "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "103", "temperature", "23.45656", "7.12324",
                                                  "100.34", "23.4545"});

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add ActiveMQ JMS EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "jmsPublisherMap.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        Thread.sleep(10000);
        JMSConsumerClient.startConsumer("topicMap");
        //Letting the JMS consumer start
        Thread.sleep(3000);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        //wait while all stats are published
        Thread.sleep(5000);
        try {
            Assert.assertEquals(JMSConsumerClient.getMessageCount(), messageCount,
                                "Incorrect number of messages consumed!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            JMSConsumerClient.shutdown();
        }

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("jmsPublisherMap.xml");

        try {
            Assert.assertEquals(JMSConsumerClient.getMessageCount(), messageCount,
                                "Incorrect number of messages consumed!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            JMSConsumerClient.shutdown();
        }

    }

    @Test(groups = {"wso2.cep"},
            description = "Testing activemq jms publisher with Text formatted event with default mapping",
            dependsOnMethods = {"jmsMapTestWithDefaultMappingScenario"})
    public void jmsTextTestWithDefaultMappingScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "outputflows" + File.separator + "sample0059";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                                                 "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "101", "temperature", "23.45656", "7.12324",
                                                  "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "103", "temperature", "23.45656", "7.12324",
                                                  "100.34", "23.4545"});

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add ActiveMQ JMS EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "jmsPublisherText.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        Thread.sleep(10000);
        JMSConsumerClient.startConsumer("topicText");
        //Letting the JMS consumer start
        Thread.sleep(3000);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        //wait while all stats are published
        Thread.sleep(5000);
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("jmsPublisherText.xml");

        try {
            Assert.assertEquals(JMSConsumerClient.getMessageCount(), messageCount,
                                "Incorrect number of messages consumed!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            JMSConsumerClient.shutdown();
        }

    }

    @Test(groups = {"wso2.cep"},
            description = "Testing activemq jms publisher with Text formatted event with custom mapping",
            dependsOnMethods = {"jmsTextTestWithDefaultMappingScenario"})
    public void jmsTextTestWithCustomMappingScenario() throws Exception {
        final int messageCount = 1;
        String samplePath = "outputflows" + File.separator + "sample0059";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                                                 "100.34", "23.4545"});


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add ActiveMQ JMS EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "jmsPublisherCustomText.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        Thread.sleep(10000);
        JMSConsumerClient.startConsumer("topicText");
        //Letting the JMS consumer start
        Thread.sleep(3000);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(2000);

        List<Object> preservedEventList = JMSConsumerClient.getPreservedEventList();
        String sentEvent = "Sensor Data Information\n" +
                           "temperature Sensor related data. \n" +
                           "- sensor id: 100\n" +
                           "- time-stamp: 199008131245\n" +
                           "- power saving enabled: false\n" +
                           "Location \n" +
                           "- longitude: 23.45656\n" +
                           "- latitude: 7.12324\n" +
                           "Values\n" +
                           "- temperature: 23.4545\n" +
                           "- humidity: 100.34";

        String preservedEvent = (String) preservedEventList.get(0);

        //wait while all stats are published
        Thread.sleep(5000);
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("jmsPublisherCustomText.xml");

        try {
            Assert.assertEquals(JMSConsumerClient.getMessageCount(), messageCount,
                                "Incorrect number of messages consumed!");
            Assert.assertEquals(preservedEvent, sentEvent, "Incorrect mapping has occurred!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            JMSConsumerClient.shutdown();
        }

    }

    @Test(groups = {"wso2.cep"},
            description = "Testing activemq jms publisher with Text formatted event with custom mapping",
            dependsOnMethods = {"jmsTextTestWithCustomMappingScenario"})
    public void jmsJSONTestWithDefaultMappingScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "outputflows" + File.separator + "sample0059";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                                                 "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "101", "temperature", "23.45656", "7.12324",
                                                  "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "103", "temperature", "23.45656", "7.12324",
                                                  "100.34", "23.4545"});


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add ActiveMQ JMS EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "jmsPublisherJSON.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        Thread.sleep(10000);
        JMSConsumerClient.startConsumer("topicJSON");
        //Letting the JMS consumer start
        Thread.sleep(3000);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        //wait while all stats are published
        Thread.sleep(5000);
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("jmsPublisherJSON.xml");

        try {
            Assert.assertEquals(JMSConsumerClient.getMessageCount(), messageCount,
                                "Incorrect number of messages consumed!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            JMSConsumerClient.shutdown();
        }

    }

    @Test(groups = {"wso2.cep"},
            description = "Testing activemq jms publisher with Text formatted event with custom mapping",
            dependsOnMethods = {"jmsJSONTestWithDefaultMappingScenario"})
    public void jmsJSONTestWithCustomMappingScenario() throws Exception {
        final int messageCount = 1;
        String samplePath = "outputflows" + File.separator + "sample0059";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                                                 "100.34", "23.4545"});


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add ActiveMQ JMS EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "jmsPublisherCustomJSON.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        Thread.sleep(10000);
        JMSConsumerClient.startConsumer("topicJSON");
        //Letting the JMS consumer start
        Thread.sleep(3000);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(2000);

        List<Object> preservedEventList = JMSConsumerClient.getPreservedEventList();
        String sentEvent = "{\"Sensor Data\":" +
                           "{\"equipment related data\":{\"timestamp\":199008131245," +
                           "\"isPowerSaverEnabled\":false,\"sensorId\":100,\"sensorName\":\"temperature\"}," +
                           "\"location data\":" +
                           "{\"longitude\":23.45656,\"latitude\":7.12324}," +
                           "\"sensor data\":" +
                           "{\"humidity\":100.34,\"sensorValue\":23.4545}}}";

        String preservedEvent = (String) preservedEventList.get(0);

        //wait while all stats are published
        Thread.sleep(5000);
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("jmsPublisherCustomJSON.xml");

        try {
            JsonParser jsonParser = new JsonParser();
            Assert.assertEquals(JMSConsumerClient.getMessageCount(), messageCount,
                                "Incorrect number of messages consumed!");
            // Comparing parsed JsonElement objects ensures that even if the string is different,
            // if the parsed Json is equivalent, the test passes.
            JsonElement preservedEventJson = jsonParser.parse(preservedEvent);
            JsonElement sentEventJson = jsonParser.parse(sentEvent);
            Assert.assertEquals(preservedEventJson, sentEventJson, "Incorrect mapping has occurred!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            JMSConsumerClient.shutdown();
        }

    }

    @Test(groups = {"wso2.cep"}, description = "Testing JMS publisher connection")
    public void testConnection() throws AxisFault {

        String samplePath = "outputflows" + File.separator + "sample0059";

        BasicOutputAdapterPropertyDto initial = new BasicOutputAdapterPropertyDto();
        initial.setKey("java.naming.factory.initial");
        initial.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        initial.set_static(true);
        BasicOutputAdapterPropertyDto url = new BasicOutputAdapterPropertyDto();
        url.setKey("java.naming.provider.url");
        url.setValue("tcp://localhost:61616");
        url.set_static(true);
        BasicOutputAdapterPropertyDto destinationType = new BasicOutputAdapterPropertyDto();
        destinationType.setKey("transport.jms.DestinationType");
        destinationType.setValue("topic");
        destinationType.set_static(true);
        BasicOutputAdapterPropertyDto destination = new BasicOutputAdapterPropertyDto();
        destination.setKey("transport.jms.Destination");
        destination.setValue("topicJSON");
        destination.set_static(true);
        BasicOutputAdapterPropertyDto connectionFactoryJNDIName = new BasicOutputAdapterPropertyDto();
        connectionFactoryJNDIName.setKey("transport.jms.ConnectionFactoryJNDIName");
        connectionFactoryJNDIName.setValue("TopicConnectionFactory");
        connectionFactoryJNDIName.set_static(true);
        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration = new BasicOutputAdapterPropertyDto[]
                {initial, url, destinationType, destination, connectionFactoryJNDIName};

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                           "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);

            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "jmsPublisherCustomJSON.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);

            eventPublisherAdminServiceClient.testConnection("jmsPublisherCustomJSON", "jms", outputPropertyConfiguration, "json");

            eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("jmsPublisherCustomJSON");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");


        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            Thread.sleep(5000);
            if (activeMqBroker != null) {
                activeMqBroker.stop();
            }
            //let server to clear the artifact un-deployment
            Thread.sleep(5000);
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

    //---- private methods --------
    private void setupActiveMQBroker() {
        activeMqBroker = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
        if (!JMSBrokerController.isBrokerStarted()) {
            Assert.assertTrue(activeMqBroker.start(), "ActiveMQ Broker starting failed");
        }
    }

    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }
}
