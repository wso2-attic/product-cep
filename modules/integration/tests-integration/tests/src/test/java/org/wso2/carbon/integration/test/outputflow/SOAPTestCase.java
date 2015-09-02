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
import org.wso2.carbon.integration.test.client.WireMonitorServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.rmi.RemoteException;

public class SOAPTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(SOAPTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        String loggedInSessionCookie = getSessionCookie();

        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(
                backendURL, loggedInSessionCookie);
        Thread.sleep(45000);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing SOAP publisher with XML formatted event with default mapping")
    public void soapXMLTestWithDefaultMappingScenario() throws Exception {

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0063";
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
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "soap.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        Thread.sleep(10000);
        WireMonitorServer wireMonitorServer = new WireMonitorServer(CEPIntegrationTestConstants.WIRE_MONITOR_PORT);
        Thread wireMonitorServerThread = new Thread(wireMonitorServer);
        wireMonitorServerThread.start();
        Thread.sleep(3000);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        //wait while all stats are published
        Thread.sleep(30000);

        wireMonitorServer.shutdown();
        String receivedEvent = wireMonitorServer.getCapturedMessage().replaceAll("\\s+", "");

        log.info(receivedEvent);

        String sentEvent = "<events><event><metaData><timestamp>199008131245</timestamp>" +
                           "<isPowerSaverEnabled>false</isPowerSaverEnabled><sensorId>100</sensorId>" +
                           "<sensorName>temperature</sensorName></metaData><correlationData><longitude>23.45656</longitude>" +
                           "<latitude>7.12324</latitude></correlationData><payloadData><humidity>100.34</humidity>" +
                           "<sensorValue>23.4545</sensorValue></payloadData></event></events>";

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("soap.xml");

        try {
            Assert.assertTrue(receivedEvent.contains(sentEvent), "Incorrect mapping has occurred!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

    }

    @Test(groups = {"wso2.cep"}, description = "Testing SOAP publisher with XML formatted event with custom mapping",
            dependsOnMethods = {"soapXMLTestWithDefaultMappingScenario"})
    public void soapXMLTestWithCustomMappingScenario() throws Exception {

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        String samplePath = "outputflows" + File.separator + "sample0063";
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
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "soapCustomXML.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        Thread.sleep(10000);
        WireMonitorServer wireMonitorServer = new WireMonitorServer(CEPIntegrationTestConstants.WIRE_MONITOR_PORT);
        Thread wireMonitorServerThread = new Thread(wireMonitorServer);
        wireMonitorServerThread.start();

        Thread.sleep(3000);

        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        //wait while all stats are published
        Thread.sleep(30000);
        wireMonitorServer.shutdown();
        String receivedEvent = wireMonitorServer.getCapturedMessage().replaceAll("\\s+", "");
        log.info(receivedEvent);

        String sentEvent = "<SensorData>" +
                           "<equipmentRelatedData>" +
                           "<timestamp>199008131245</timestamp>" +
                           "<isPowerSaverEnabled>false</isPowerSaverEnabled>" +
                           "<sensorId>100</sensorId>" +
                           "<sensorName>temperature</sensorName>" +
                           "</equipmentRelatedData>" +
                           "<locationData>" +
                           "<longitude>23.45656</longitude>" +
                           "<latitude>7.12324</latitude>" +
                           "</locationData>" +
                           "<sensorData>" +
                           "<humidity>100.34</humidity>" +
                           "<sensorValue>23.4545</sensorValue>" +
                           "</sensorData>" +
                           "</SensorData>";

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("soapCustomXML.xml");

        try {
            Assert.assertTrue(receivedEvent.contains(sentEvent), "Incorrect mapping has occurred!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

    }

    @Test(groups = {"wso2.cep"}, description = "Testing SOAP publisher connection", expectedExceptions = AxisFault.class)
    public void testConnection() throws AxisFault {
        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration = new BasicOutputAdapterPropertyDto[]{};
        try {
            eventPublisherAdminServiceClient.testConnection("soapCustomXML", "soap", outputPropertyConfiguration, "xml");
        } catch (AxisFault e) {
            throw new AxisFault(e.getMessage(), e);
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


