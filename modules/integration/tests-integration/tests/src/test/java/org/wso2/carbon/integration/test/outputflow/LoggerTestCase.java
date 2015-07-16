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

package org.wso2.carbon.integration.test.outputflow;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

/**
 * Testing Logger publisher in different formats (text, xml, json)
 */
public class LoggerTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(LoggerTestCase.class);
    private static LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
        logViewerClient = new LogViewerClient(backendURL, loggedInSessionCookie);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing Text Logger")
    public void loggerTextTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0055";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Text Logger
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "logger.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

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

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("logger.xml");

        Thread.sleep(2000);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Text Logger with mapping")
    public void loggerTextWithMappingTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0056";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Text Logger
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "logger.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "200", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "201", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "203", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        int beforeCount = logViewerClient.getAllRemoteSystemLogs().length;
        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);
        Thread.sleep(1000);
        try {
            boolean mappingPortionFound = false;
            Thread.sleep(2000);
            LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();
            for (int i = 0; i < (logs.length - beforeCount); i++) {
                if (logs[i].getMessage().contains("temperature Sensor related data. \n- sensor id: 200\n- time-stamp: 199008131245\n- power saving enabled: false\nLocation \n- longitude: 23.45656\n- latitude: 7.12324\nValues\n- temperature: 23.4545\n- humidity: 100.34")) {
                    mappingPortionFound = true;
                    break;
                }
            }
            Assert.assertTrue(mappingPortionFound, "Incorrect mapping has occurred! ");
            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("logger.xml");

        Thread.sleep(2000);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing JSON Logger")
    public void loggerJSONTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0051";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Text Logger
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "logger.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "300", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "301", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "303", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("logger.xml");

        Thread.sleep(2000);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing JSON Mapping Logger")
    public void loggerJSONWithMappingTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0052";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Text Logger
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "logger.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "400", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "401", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "403", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        int beforeCount = logViewerClient.getAllRemoteSystemLogs().length;

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        try {
            boolean mappingPortionFound = false;
            Thread.sleep(2000);
            LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();
            for (int i = 0; i < (logs.length - beforeCount); i++) {
                if (logs[i].getMessage().contains("{\"Sensor Data\":{\"equipment related data\":" +
                        "{\"timestamp\":199008131245,\"isPowerSaverEnabled\":false,\"sensorId\":400," +
                        "\"sensorName\":\"temperature\"},\"location data\":{\"longitude\":23.45656,\"latitude\":7.12324}," +
                        "\"sensor data\":{\"humidity\":100.34,\"sensorValue\":23.4545}}}")) {
                    mappingPortionFound = true;
                    break;
                }
            }
            Assert.assertTrue(mappingPortionFound, "Incorrect mapping has occurred! ");
            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("logger.xml");

        Thread.sleep(2000);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing XML Logger")
    public void loggerXMLTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0053";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Text Logger
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "logger.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "500", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "501", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "503", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("logger.xml");

        Thread.sleep(2000);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing XML mapping Logger")
    public void loggerXMLWithMappingTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0054";

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Text Logger
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "logger.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "500", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "501", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "503", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        int beforeCount = logViewerClient.getAllRemoteSystemLogs().length;

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);

        try {
            boolean mappingPortionFound = false;
            Thread.sleep(2000);
            LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();
            for (int i = 0; i < (logs.length - beforeCount); i++) {
                if (logs[i].getMessage().contains("<SensorData>\n" +
                        "        <equipmentRelatedData>\n" +
                        "          <timestamp>199008131245</timestamp>\n" +
                        "          <isPowerSaverEnabled>false</isPowerSaverEnabled>\n" +
                        "          <sensorId>503</sensorId>\n" +
                        "          <sensorName>temperature</sensorName>\n" +
                        "        </equipmentRelatedData>\n" +
                        "        <locationData>\n" +
                        "          <longitude>23.45656</longitude>\n" +
                        "          <latitude>7.12324</latitude>\n" +
                        "        </locationData>\n" +
                        "        <sensorData>\n" +
                        "          <humidity>100.34</humidity>\n" +
                        "          <sensorValue>23.4545</sensorValue>\n" +
                        "        </sensorData>\n" +
                        "      </SensorData>")) {
                    mappingPortionFound = true;
                    break;
                }
            }
            Assert.assertTrue(mappingPortionFound, "Incorrect mapping has occurred! ");
            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("logger.xml");

        Thread.sleep(2000);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing logger connection", expectedExceptions = AxisFault.class)
    public void testConnection() throws AxisFault {
        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration = new BasicOutputAdapterPropertyDto[] {};
        try {
            eventPublisherAdminServiceClient.testConnection("logger","logger",outputPropertyConfiguration,"xml");
        } catch (AxisFault e) {
            throw new AxisFault(e.getMessage(),e);
        } catch (RemoteException e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
