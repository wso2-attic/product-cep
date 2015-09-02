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

public class HTTPTestCase extends CEPIntegrationTest {

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

    @Test(groups = {"wso2.cep"}, description = "Testing HTTP publisher with JSON formatted event with default mapping")
    public void httpJSONTestWithDefaultMappingScenario() throws Exception {
        String samplePath = "outputflows" + File.separator + "sample0062";
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
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "httpJson.xml");
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

        String sentEvent = "{\"event\":{\"metaData\":{\"timestamp\":199008131245,\"isPowerSaverEnabled\":false," +
                           "\"sensorId\":100,\"sensorName\":\"temperature\"},\"correlationData\":{\"longitude\":23.45656," +
                           "\"latitude\":7.12324},\"payloadData\":{\"humidity\":100.34,\"sensorValue\":23.4545}}}";

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("httpJson.xml");

        try {
            Assert.assertTrue(receivedEvent.contains(sentEvent), "Incorrect mapping has occurred!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

    }

    @Test(groups = {"wso2.cep"}, description = "Testing HTTP publisher with Text formatted event with custom mapping",
            dependsOnMethods = {"httpJSONTestWithDefaultMappingScenario"})
    public void httpTextTestWithDefaultMappingScenario() throws Exception {
        String samplePath = "outputflows" + File.separator + "sample0062";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.message.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "Lasantha Fernando", "2321.56", "BATA", "199008031245"});

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.message.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add ActiveMQ JMS EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "httpText.xml");
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

        String receivedEvent = wireMonitorServer.getCapturedMessage();
        log.info(receivedEvent);

        String sentEvent = "Hello Lasantha Fernando, " +
                           "You have done transaction with your credit card for an amount Rs. 2321.56 with vendor: BATA.";

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.message.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("httpText.xml");

        try {
            Assert.assertTrue(receivedEvent.contains(sentEvent), "Incorrect mapping has occurred!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

    }

    @Test(groups = {"wso2.cep"}, description = "Testing HTTP publisher with XML formatted event with default mapping",
            dependsOnMethods = {"httpTextTestWithDefaultMappingScenario"})
    public void httpXMLTestWithDefaultMappingScenario() throws Exception {
        String samplePath = "outputflows" + File.separator + "sample0062";
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
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "httpXml.xml");
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
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("httpXml.xml");

        try {
            Assert.assertTrue(receivedEvent.contains(sentEvent), "Incorrect mapping has occurred!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }

    }

    @Test(groups = {"wso2.cep"}, description = "Testing HTTP publisher connection", expectedExceptions = AxisFault.class)
    public void testConnection() throws AxisFault {
        BasicOutputAdapterPropertyDto url = new BasicOutputAdapterPropertyDto();
        url.setKey("http.url");
        url.setValue("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT + "/GenericLogService/log");
        url.set_static(false);
        BasicOutputAdapterPropertyDto username = new BasicOutputAdapterPropertyDto();
        username.setKey("http.username");
        username.setValue("");
        username.set_static(false);
        BasicOutputAdapterPropertyDto password = new BasicOutputAdapterPropertyDto();
        password.setKey("http.password");
        password.setValue("");
        password.set_static(false);
        BasicOutputAdapterPropertyDto headers = new BasicOutputAdapterPropertyDto();
        headers.setKey("http.headers");
        headers.setValue("Content-Type: application/json");
        headers.set_static(false);
        BasicOutputAdapterPropertyDto proxyHost = new BasicOutputAdapterPropertyDto();
        proxyHost.setKey("http.proxy.host");
        proxyHost.setValue("");
        proxyHost.set_static(false);
        BasicOutputAdapterPropertyDto proxyPort = new BasicOutputAdapterPropertyDto();
        proxyPort.setKey("http.proxy.port");
        proxyPort.setValue("");
        proxyPort.set_static(false);
        BasicOutputAdapterPropertyDto clientMethod = new BasicOutputAdapterPropertyDto();
        clientMethod.setKey("http.proxy.port");
        clientMethod.setValue("");
        clientMethod.set_static(true);
        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration = new BasicOutputAdapterPropertyDto[]
                {url, username, password, headers, proxyHost, proxyPort, clientMethod};

        try {
            eventPublisherAdminServiceClient.testConnection("httpJson", "http", outputPropertyConfiguration, "json");
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
