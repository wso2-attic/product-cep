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

package org.wso2.carbon.integration.test.inputflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.integration.test.client.HttpEventPublisherClient;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Sending different formatted events to the Http Receiver according to the receivers mapping type
 */
public class HttpTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(HttpTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with JSON formatted event")
    public void httpJSONTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "inputflows" + File.separator + "sample0001";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http JSON EventReceiver without mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                         "/endpoints/httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(30000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver.xml");
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
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();

        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with JSON formatted event with mapping enabled")
    public void httpJSONMappingTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "inputflows" + File.separator + "sample0002";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http JSON EventReceiver with mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                         "/endpoints/httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(5000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event.setMetaData(new Object[]{19900813115534l, false, 501, "temperature"});
        event.setCorrelationData(new Object[]{90.34344, 20.44345});
        event.setPayloadData(new Object[]{2.3f, 20.44345});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event2.setMetaData(new Object[]{19900813115534l, false, 502, "temperature"});
        event2.setCorrelationData(new Object[]{90.34344, 20.44345});
        event2.setPayloadData(new Object[]{2.3f, 20.44345});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event3.setMetaData(new Object[]{19900813115534l, false, 503, "temperature"});
        event3.setCorrelationData(new Object[]{90.34344, 20.44345});
        event3.setPayloadData(new Object[]{2.3f, 20.44345});
        eventList.add(event3);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();

        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with XML formatted event")
    public void httpXMLTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "inputflows" + File.separator + "sample0003";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http XML EventReceiver
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                         "/endpoints/httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(5000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event.setMetaData(new Object[]{199008131245l, true, 401, "temperature"});
        event.setCorrelationData(new Object[]{4.504343, 1.23434});
        event.setPayloadData(new Object[]{6.6f, 20.44345});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event2.setMetaData(new Object[]{199008131245l, true, 402, "temperature"});
        event2.setCorrelationData(new Object[]{4.504343, 1.23434});
        event2.setPayloadData(new Object[]{6.6f, 20.44345});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event3.setMetaData(new Object[]{199008131245l, true, 403, "temperature"});
        event3.setCorrelationData(new Object[]{4.504343, 1.23434});
        event3.setPayloadData(new Object[]{6.6f, 20.44345});
        eventList.add(event3);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with XML formatted event with mapping enabled")
    public void httpXMLMappingTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "inputflows" + File.separator + "sample0004";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http XML EventReceiver with mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                         "/endpoints/httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(5000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event.setMetaData(new Object[]{19900813115534l, true, 502, "temperature"});
        event.setCorrelationData(new Object[]{4.504343, 1.23434});
        event.setPayloadData(new Object[]{6.6f, 20.44345});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event2.setMetaData(new Object[]{19900813115534l, true, 501, "temperature"});
        event2.setCorrelationData(new Object[]{4.504343, 1.23434});
        event2.setPayloadData(new Object[]{6.6f, 20.44345});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event3.setMetaData(new Object[]{19900813115534l, true, 503, "temperature"});
        event3.setCorrelationData(new Object[]{4.504343, 1.23434});
        event3.setPayloadData(new Object[]{6.6f, 20.44345});
        eventList.add(event3);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with Text formatted event")
    public void httpTextTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "inputflows" + File.separator + "sample0005";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http Text EventReceiver
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                         "/endpoints/httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(5000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event.setMetaData(new Object[]{19900813115534l, false, 100, "temperature"});
        event.setCorrelationData(new Object[]{20.44345, 5.443435});
        event.setPayloadData(new Object[]{8.9f, 1.23434});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event2.setMetaData(new Object[]{19900813115534l, false, 101, "temperature"});
        event2.setCorrelationData(new Object[]{20.44345, 5.443435});
        event2.setPayloadData(new Object[]{8.9f, 1.23434});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event3.setMetaData(new Object[]{19900813115534l, false, 102, "temperature"});
        event3.setCorrelationData(new Object[]{20.44345, 5.443435});
        event3.setPayloadData(new Object[]{8.9f, 1.23434});
        eventList.add(event3);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with Text formatted event with mapping enabled")
    public void httpTextMappingTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "inputflows" + File.separator + "sample0006";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http Text EventReceiver with mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT +
                                         "/endpoints/httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(5000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");

        Thread.sleep(2000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event.setMetaData(new Object[]{19900813115534l, false, 100, "temperature"});
        event.setCorrelationData(new Object[]{20.44345, 5.443435});
        event.setPayloadData(new Object[]{8.9f, 1.23434});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event2.setMetaData(new Object[]{19900813115534l, false, 101, "temperature"});
        event2.setCorrelationData(new Object[]{20.44345, 5.443435});
        event2.setPayloadData(new Object[]{8.9f, 1.23434});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event3.setMetaData(new Object[]{19900813115534l, false, 103, "temperature"});
        event3.setCorrelationData(new Object[]{20.44345, 5.443435});
        event3.setPayloadData(new Object[]{8.9f, 1.23434});
        eventList.add(event3);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing http receiver with secure http request")
    public void httpsTestScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "inputflows" + File.separator + "sample0021";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Http JSON EventReceiver without mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        HttpEventPublisherClient.publish("https://localhost:" + CEPIntegrationTestConstants.HTTPS_PORT +
                                         "/endpoints/httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(30000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver.xml");
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
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();

        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
