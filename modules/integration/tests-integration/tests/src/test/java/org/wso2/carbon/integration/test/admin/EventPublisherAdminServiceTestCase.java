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

package org.wso2.carbon.integration.test.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto;
import org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationDto;
import org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationInfoDto;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;

public class EventPublisherAdminServiceTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(EventPublisherAdminServiceTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing get active event publisher configuration")
    public void testGetActiveEventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0062";

        //Add StreamDefinition
        String streamDefinitionAsString = null;
        try {
            streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "httpJson.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);

            EventPublisherConfigurationDto eventPublisherConfigurationDto =
                    eventPublisherAdminServiceClient.getActiveEventPublisherConfiguration("httpJson");
            Assert.assertEquals(eventPublisherConfigurationDto.getEventPublisherName(),"httpJson");
            Assert.assertEquals(eventPublisherConfigurationDto.getFromStreamNameWithVersion(),"org.wso2.event.sensor.stream:1.0.0");
            Assert.assertEquals(eventPublisherConfigurationDto.getMessageFormat(),"json");
            Assert.assertEquals(eventPublisherConfigurationDto.getCustomMappingEnabled(),false);

            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("httpJson.xml");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing deploy WSO2 event publisher configuration")
    public void testDeployWSO2EventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0058";

        EventMappingPropertyDto timestamp = new EventMappingPropertyDto();
        timestamp.setName("time");
        timestamp.setType("long");
        timestamp.setValueOf("meta_timestamp");
        EventMappingPropertyDto isPowerSaverEnabled = new EventMappingPropertyDto();
        isPowerSaverEnabled.setName("isPowerSaving");
        isPowerSaverEnabled.setType("bool");
        isPowerSaverEnabled.setValueOf("meta_isPowerSaverEnabled");
        EventMappingPropertyDto id = new EventMappingPropertyDto();
        id.setName("id");
        id.setType("int");
        id.setValueOf("meta_sensorId");
        EventMappingPropertyDto name = new EventMappingPropertyDto();
        name.setName("name");
        name.setType("string");
        name.setValueOf("meta_sensorName");

        EventMappingPropertyDto longitude = new EventMappingPropertyDto();
        longitude.setName("longitude");
        longitude.setType("double");
        longitude.setValueOf("correlation_longitude");
        EventMappingPropertyDto latitude = new EventMappingPropertyDto();
        latitude.setName("latitude");
        latitude.setType("double");
        latitude.setValueOf("correlation_latitude");

        EventMappingPropertyDto humidity = new EventMappingPropertyDto();
        humidity.setName("humidityLevel");
        humidity.setType("float");
        humidity.setValueOf("humidity");
        EventMappingPropertyDto sensorValue = new EventMappingPropertyDto();
        sensorValue.setName("sensorReading");
        sensorValue.setType("double");
        sensorValue.setValueOf("sensorValue");

        BasicOutputAdapterPropertyDto username = new BasicOutputAdapterPropertyDto();
        username.setKey("username");
        username.setValue("admin");
        BasicOutputAdapterPropertyDto protocol = new BasicOutputAdapterPropertyDto();
        protocol.setKey("protocol");
        protocol.setValue("thrift");
        BasicOutputAdapterPropertyDto publishingMode = new BasicOutputAdapterPropertyDto();
        publishingMode.setKey("publishingMode");
        publishingMode.setValue("blocking");
        BasicOutputAdapterPropertyDto receiverURL = new BasicOutputAdapterPropertyDto();
        receiverURL.setKey("receiverURL");
        receiverURL.setValue("tcp://localhost:" + CEPIntegrationTestConstants.TCP_PORT);
        BasicOutputAdapterPropertyDto password = new BasicOutputAdapterPropertyDto();
        password.setKey("password");
        password.setValue("admin");

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);

            String streamDefinitionMapAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream.map_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionMapAsString);
            eventPublisherAdminServiceClient.addWso2EventPublisherConfiguration("eventPublisher",
                    "org.wso2.event.sensor.stream:1.0.0","wso2event",
                    new EventMappingPropertyDto[]{timestamp, isPowerSaverEnabled, id, name},
                    new EventMappingPropertyDto[]{longitude, latitude},
                    new EventMappingPropertyDto[]{humidity, sensorValue},
                    new BasicOutputAdapterPropertyDto[]{username, protocol, publishingMode,receiverURL, password},
                    true,"org.wso2.event.sensor.stream.map:1.0.0");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream.map","1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("eventPublisher.xml");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing deploy map event publisher configuration")
    public void testDeployMapEventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0051";

        EventMappingPropertyDto timestamp = new EventMappingPropertyDto();
        timestamp.setName("timestamp:([a-z0-9.]*),*");
        timestamp.setType("long");
        timestamp.setValueOf("meta_timestamp");
        EventMappingPropertyDto isPowerSaverEnabled = new EventMappingPropertyDto();
        isPowerSaverEnabled.setName("isPowerSaverEnabled:([a-z0-9.]*),*");
        isPowerSaverEnabled.setType("bool");
        isPowerSaverEnabled.setValueOf("meta_isPowerSaverEnabled");
        EventMappingPropertyDto sensorId = new EventMappingPropertyDto();
        sensorId.setName("sensorId:([a-z0-9.]*),*");
        sensorId.setType("int");
        sensorId.setValueOf("meta_sensorId");
        EventMappingPropertyDto sensorName = new EventMappingPropertyDto();
        sensorName.setName("sensorName:([a-z0-9.]*),*");
        sensorName.setType("string");
        sensorName.setValueOf("meta_sensorName");
        EventMappingPropertyDto longitude = new EventMappingPropertyDto();
        longitude.setName("longitude:([a-z0-9.]*),*");
        longitude.setType("double");
        longitude.setValueOf("correlation_longitude");
        EventMappingPropertyDto latitude = new EventMappingPropertyDto();
        latitude.setName("latitude:([a-z0-9.]*),*");
        latitude.setType("double");
        latitude.setValueOf("correlation_latitude");
        EventMappingPropertyDto humidity = new EventMappingPropertyDto();
        humidity.setName("humidity:([a-z0-9.]*),*");
        humidity.setType("float");
        humidity.setValueOf("humidity");
        EventMappingPropertyDto sensorValue = new EventMappingPropertyDto();
        sensorValue.setName("sensorValue:([a-z0-9.]*),*");
        sensorValue.setType("double");
        sensorValue.setValueOf("sensorValue");

        BasicOutputAdapterPropertyDto initial = new BasicOutputAdapterPropertyDto();
        initial.setKey("java.naming.factory.initial");
        initial.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        BasicOutputAdapterPropertyDto duplicated = new BasicOutputAdapterPropertyDto();
        duplicated.setKey("receiving.events.duplicated.in.cluster");
        duplicated.setValue("false");
        BasicOutputAdapterPropertyDto startFromEnd = new BasicOutputAdapterPropertyDto();
        startFromEnd.setKey("java.naming.provider.url");
        startFromEnd.setValue("tcp://localhost:61616");
        BasicOutputAdapterPropertyDto destinationType = new BasicOutputAdapterPropertyDto();
        destinationType.setKey("transport.jms.DestinationType");
        destinationType.setValue("topic");
        BasicOutputAdapterPropertyDto destination = new BasicOutputAdapterPropertyDto();
        destination.setKey("transport.jms.Destination");
        destination.setValue("topic");
        BasicOutputAdapterPropertyDto ConnectionFactory = new BasicOutputAdapterPropertyDto();
        ConnectionFactory.setKey("transport.jms.ConnectionFactoryJNDIName");
        ConnectionFactory.setValue("TopicConnectionFactory");

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            eventPublisherAdminServiceClient.addMapEventPublisherConfiguration("mapPublisher",
                    "org.wso2.event.sensor.stream:1.0.0",
                    "jms",
                    new EventMappingPropertyDto[]{timestamp, isPowerSaverEnabled, sensorId,
                            sensorName, longitude, latitude, humidity, sensorValue},
                    new BasicOutputAdapterPropertyDto[]{initial, duplicated, startFromEnd, destinationType, destination,
                            ConnectionFactory}, true);
            eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("mapPublisher");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing deploy text event publisher configuration")
    public void testDeployTextEventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0059";

        EventMappingPropertyDto timestamp = new EventMappingPropertyDto();
        timestamp.setName("timestamp:([a-z0-9.]*),*");
        timestamp.setType("long");
        timestamp.setValueOf("meta_timestamp");
        EventMappingPropertyDto isPowerSaverEnabled = new EventMappingPropertyDto();
        isPowerSaverEnabled.setName("isPowerSaverEnabled:([a-z0-9.]*),*");
        isPowerSaverEnabled.setType("bool");
        isPowerSaverEnabled.setValueOf("meta_isPowerSaverEnabled");
        EventMappingPropertyDto sensorId = new EventMappingPropertyDto();
        sensorId.setName("sensorId:([a-z0-9.]*),*");
        sensorId.setType("int");
        sensorId.setValueOf("meta_sensorId");
        EventMappingPropertyDto sensorName = new EventMappingPropertyDto();
        sensorName.setName("sensorName:([a-z0-9.]*),*");
        sensorName.setType("string");
        sensorName.setValueOf("meta_sensorName");
        EventMappingPropertyDto longitude = new EventMappingPropertyDto();
        longitude.setName("longitude:([a-z0-9.]*),*");
        longitude.setType("double");
        longitude.setValueOf("correlation_longitude");
        EventMappingPropertyDto latitude = new EventMappingPropertyDto();
        latitude.setName("latitude:([a-z0-9.]*),*");
        latitude.setType("double");
        latitude.setValueOf("correlation_latitude");
        EventMappingPropertyDto humidity = new EventMappingPropertyDto();
        humidity.setName("humidity:([a-z0-9.]*),*");
        humidity.setType("float");
        humidity.setValueOf("humidity");
        EventMappingPropertyDto sensorValue = new EventMappingPropertyDto();
        sensorValue.setName("sensorValue:([a-z0-9.]*),*");
        sensorValue.setType("double");
        sensorValue.setValueOf("sensorValue");

        BasicOutputAdapterPropertyDto initial = new BasicOutputAdapterPropertyDto();
        initial.setKey("java.naming.factory.initial");
        initial.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        BasicOutputAdapterPropertyDto duplicated = new BasicOutputAdapterPropertyDto();
        duplicated.setKey("receiving.events.duplicated.in.cluster");
        duplicated.setValue("false");
        BasicOutputAdapterPropertyDto startFromEnd = new BasicOutputAdapterPropertyDto();
        startFromEnd.setKey("java.naming.provider.url");
        startFromEnd.setValue("tcp://localhost:61616");
        BasicOutputAdapterPropertyDto destinationType = new BasicOutputAdapterPropertyDto();
        destinationType.setKey("transport.jms.DestinationType");
        destinationType.setValue("topic");
        BasicOutputAdapterPropertyDto destination = new BasicOutputAdapterPropertyDto();
        destination.setKey("transport.jms.Destination");
        destination.setValue("topic");
        BasicOutputAdapterPropertyDto connectionFactory = new BasicOutputAdapterPropertyDto();
        connectionFactory.setKey("transport.jms.ConnectionFactoryJNDIName");
        connectionFactory.setValue("TopicConnectionFactory");

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            eventPublisherAdminServiceClient.addTextEventPublisherConfiguration("textPublisher",
                    "org.wso2.event.sensor.stream:1.0.0",
                    "jms",
                    "Sensor Data Information\n" +
                            "{{meta_sensorName}} Sensor related data. \n" +
                            "- sensor id: {{meta_sensorId}}\n" +
                            "- time-stamp: {{meta_timestamp}}\n" +
                            "- power saving enabled: {{meta_isPowerSaverEnabled}}\n" +
                            "Location \n" +
                            "- longitude: {{correlation_longitude}}\n" +
                            "- latitude: {{correlation_latitude}}\n" +
                            "Values\n" +
                            "- {{meta_sensorName}}: {{sensorValue}}\n" +
                            "- humidity: {{humidity}}",
                    new BasicOutputAdapterPropertyDto[]{initial, duplicated, startFromEnd, destinationType, destination,
                            connectionFactory },"inline" ,true);
            eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("textPublisher");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing deploy json event publisher configuration")
    public void testDeployJSONEventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0059";

        EventMappingPropertyDto timestamp = new EventMappingPropertyDto();
        timestamp.setName("timestamp:([a-z0-9.]*),*");
        timestamp.setType("long");
        timestamp.setValueOf("meta_timestamp");
        EventMappingPropertyDto isPowerSaverEnabled = new EventMappingPropertyDto();
        isPowerSaverEnabled.setName("isPowerSaverEnabled:([a-z0-9.]*),*");
        isPowerSaverEnabled.setType("bool");
        isPowerSaverEnabled.setValueOf("meta_isPowerSaverEnabled");
        EventMappingPropertyDto sensorId = new EventMappingPropertyDto();
        sensorId.setName("sensorId:([a-z0-9.]*),*");
        sensorId.setType("int");
        sensorId.setValueOf("meta_sensorId");
        EventMappingPropertyDto sensorName = new EventMappingPropertyDto();
        sensorName.setName("sensorName:([a-z0-9.]*),*");
        sensorName.setType("string");
        sensorName.setValueOf("meta_sensorName");
        EventMappingPropertyDto longitude = new EventMappingPropertyDto();
        longitude.setName("longitude:([a-z0-9.]*),*");
        longitude.setType("double");
        longitude.setValueOf("correlation_longitude");
        EventMappingPropertyDto latitude = new EventMappingPropertyDto();
        latitude.setName("latitude:([a-z0-9.]*),*");
        latitude.setType("double");
        latitude.setValueOf("correlation_latitude");
        EventMappingPropertyDto humidity = new EventMappingPropertyDto();
        humidity.setName("humidity:([a-z0-9.]*),*");
        humidity.setType("float");
        humidity.setValueOf("humidity");
        EventMappingPropertyDto sensorValue = new EventMappingPropertyDto();
        sensorValue.setName("sensorValue:([a-z0-9.]*),*");
        sensorValue.setType("double");
        sensorValue.setValueOf("sensorValue");

        BasicOutputAdapterPropertyDto initial = new BasicOutputAdapterPropertyDto();
        initial.setKey("java.naming.factory.initial");
        initial.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        BasicOutputAdapterPropertyDto duplicated = new BasicOutputAdapterPropertyDto();
        duplicated.setKey("receiving.events.duplicated.in.cluster");
        duplicated.setValue("false");
        BasicOutputAdapterPropertyDto startFromEnd = new BasicOutputAdapterPropertyDto();
        startFromEnd.setKey("java.naming.provider.url");
        startFromEnd.setValue("tcp://localhost:61616");
        BasicOutputAdapterPropertyDto destinationType = new BasicOutputAdapterPropertyDto();
        destinationType.setKey("transport.jms.DestinationType");
        destinationType.setValue("topic");
        BasicOutputAdapterPropertyDto destination = new BasicOutputAdapterPropertyDto();
        destination.setKey("transport.jms.Destination");
        destination.setValue("topic");
        BasicOutputAdapterPropertyDto connectionFactory = new BasicOutputAdapterPropertyDto();
        connectionFactory.setKey("transport.jms.ConnectionFactoryJNDIName");
        connectionFactory.setValue("TopicConnectionFactory");

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            eventPublisherAdminServiceClient.addJSONEventPublisherConfiguration("jmsPublisherCustomJSON",
                    "org.wso2.event.sensor.stream:1.0.0",
                    "jms",
                    "Sensor Data Information\n" +
                            "{{meta_sensorName}} Sensor related data. \n" +
                            "- sensor id: {{meta_sensorId}}\n" +
                            "- time-stamp: {{meta_timestamp}}\n" +
                            "- power saving enabled: {{meta_isPowerSaverEnabled}}\n" +
                            "Location \n" +
                            "- longitude: {{correlation_longitude}}\n" +
                            "- latitude: {{correlation_latitude}}\n" +
                            "Values\n" +
                            "- {{meta_sensorName}}: {{sensorValue}}\n" +
                            "- humidity: {{humidity}}",
                    new BasicOutputAdapterPropertyDto[]{initial, duplicated, startFromEnd, destinationType, destination,
                            connectionFactory}, "inline" ,true);
            eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("jmsPublisherCustomJSON");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing get all stream specific active event publisher configurations")
    public void testGetAllStreamSpecificActiveEventPublisherConfigurations() {
        String samplePath = "outputflows" + File.separator + "sample0059";

        EventMappingPropertyDto timestamp = new EventMappingPropertyDto();
        timestamp.setName("timestamp:([a-z0-9.]*),*");
        timestamp.setType("long");
        timestamp.setValueOf("meta_timestamp");
        EventMappingPropertyDto isPowerSaverEnabled = new EventMappingPropertyDto();
        isPowerSaverEnabled.setName("isPowerSaverEnabled:([a-z0-9.]*),*");
        isPowerSaverEnabled.setType("bool");
        isPowerSaverEnabled.setValueOf("meta_isPowerSaverEnabled");
        EventMappingPropertyDto sensorId = new EventMappingPropertyDto();
        sensorId.setName("sensorId:([a-z0-9.]*),*");
        sensorId.setType("int");
        sensorId.setValueOf("meta_sensorId");
        EventMappingPropertyDto sensorName = new EventMappingPropertyDto();
        sensorName.setName("sensorName:([a-z0-9.]*),*");
        sensorName.setType("string");
        sensorName.setValueOf("meta_sensorName");
        EventMappingPropertyDto longitude = new EventMappingPropertyDto();
        longitude.setName("longitude:([a-z0-9.]*),*");
        longitude.setType("double");
        longitude.setValueOf("correlation_longitude");
        EventMappingPropertyDto latitude = new EventMappingPropertyDto();
        latitude.setName("latitude:([a-z0-9.]*),*");
        latitude.setType("double");
        latitude.setValueOf("correlation_latitude");
        EventMappingPropertyDto humidity = new EventMappingPropertyDto();
        humidity.setName("humidity:([a-z0-9.]*),*");
        humidity.setType("float");
        humidity.setValueOf("humidity");
        EventMappingPropertyDto sensorValue = new EventMappingPropertyDto();
        sensorValue.setName("sensorValue:([a-z0-9.]*),*");
        sensorValue.setType("double");
        sensorValue.setValueOf("sensorValue");

        BasicOutputAdapterPropertyDto initial = new BasicOutputAdapterPropertyDto();
        initial.setKey("java.naming.factory.initial");
        initial.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        BasicOutputAdapterPropertyDto duplicated = new BasicOutputAdapterPropertyDto();
        duplicated.setKey("receiving.events.duplicated.in.cluster");
        duplicated.setValue("false");
        BasicOutputAdapterPropertyDto startFromEnd = new BasicOutputAdapterPropertyDto();
        startFromEnd.setKey("java.naming.provider.url");
        startFromEnd.setValue("tcp://localhost:61616");
        BasicOutputAdapterPropertyDto destinationType = new BasicOutputAdapterPropertyDto();
        destinationType.setKey("transport.jms.DestinationType");
        destinationType.setValue("topic");
        BasicOutputAdapterPropertyDto destination = new BasicOutputAdapterPropertyDto();
        destination.setKey("transport.jms.Destination");
        destination.setValue("topic");
        BasicOutputAdapterPropertyDto connectionFactory = new BasicOutputAdapterPropertyDto();
        connectionFactory.setKey("transport.jms.ConnectionFactoryJNDIName");
        connectionFactory.setValue("TopicConnectionFactory");

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            eventPublisherAdminServiceClient.addJSONEventPublisherConfiguration("jmsPublisherCustomJSON",
                    "org.wso2.event.sensor.stream:1.0.0",
                    "jms",
                    "Sensor Data Information\n" +
                            "{{meta_sensorName}} Sensor related data. \n" +
                            "- sensor id: {{meta_sensorId}}\n" +
                            "- time-stamp: {{meta_timestamp}}\n" +
                            "- power saving enabled: {{meta_isPowerSaverEnabled}}\n" +
                            "Location \n" +
                            "- longitude: {{correlation_longitude}}\n" +
                            "- latitude: {{correlation_latitude}}\n" +
                            "Values\n" +
                            "- {{meta_sensorName}}: {{sensorValue}}\n" +
                            "- humidity: {{humidity}}",
                    new BasicOutputAdapterPropertyDto[]{initial, duplicated, startFromEnd, destinationType, destination,
                            connectionFactory}, "inline", true);
            EventPublisherConfigurationInfoDto[] allStreamSpecificActiveEventPublisherConfiguration
                    = eventPublisherAdminServiceClient
                    .getAllStreamSpecificActiveEventPublisherConfigurations("org.wso2.event.sensor.stream:1.0.0");
            Assert.assertEquals(allStreamSpecificActiveEventPublisherConfiguration.length,1);
            Assert.assertEquals(allStreamSpecificActiveEventPublisherConfiguration[0].getEventPublisherName(), "jmsPublisherCustomJSON");
            eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("jmsPublisherCustomJSON");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
