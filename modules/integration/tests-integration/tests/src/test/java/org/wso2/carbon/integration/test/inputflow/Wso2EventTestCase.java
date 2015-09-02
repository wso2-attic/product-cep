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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationDto;
import org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationInfoDto;
import org.wso2.carbon.integration.test.client.Wso2EventClient;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sending different formatted events to the Wso2Event Receiver according to the receivers custom mapping type
 */
public class Wso2EventTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(Wso2EventTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL,
                                                                                               loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL,
                                                                                                         loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL,
                                                                                                 loggedInSessionCookie);
    }


    @Test(groups = {"wso2.cep"}, description = "Testing wso2event receiver with custom mapping formatting")
    public void wso2EventMapReceiverTestWithCustomMappingScenario() throws Exception {
        String samplePath = "inputflows" + File.separator + "sample0008";
        String eventReceiverName = "wso2eventReceiver";

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startActiveERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startAllERCount = eventReceiverAdminServiceClient.getEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventReceiverConfigurationInfoDto[] startEventReceiverConfigurationInfoDtos = eventReceiverAdminServiceClient
                .getAllStreamSpecificActiveEventReceiverConfigurations("org.wso2.event.sensor.stream:1.0.0");
        int startStreamSpecificActiveERCount = startEventReceiverConfigurationInfoDtos == null ? 0 : startEventReceiverConfigurationInfoDtos.length;

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        String streamDefinitionMapAsString = getJSONArtifactConfiguration(samplePath,
                                                                          "org.wso2.mapped.sensor.data_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionMapAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 2);

        //Add File EventReceiver without mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "wso2eventReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Thread.sleep(2000);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startActiveERCount + 1);
        Assert.assertEquals(eventReceiverAdminServiceClient.getEventReceiverCount(), startAllERCount + 1);
        EventReceiverConfigurationDto eventReceiverConfigurationDto = eventReceiverAdminServiceClient.
                getActiveEventReceiverConfiguration(eventReceiverName);
        Assert.assertTrue(eventReceiverConfigurationDto.getCustomMappingEnabled());
        String deployedEventReceiverConfig = eventReceiverAdminServiceClient.getEventReceiverConfigurationContent(eventReceiverName);
        Assert.assertNotNull(deployedEventReceiverConfig);
        OMElement omElement = AXIOMUtil.stringToOM(deployedEventReceiverConfig);
        String deployedERName = omElement.getAttributeValue(new QName("name"));
        Assert.assertEquals(deployedERName, eventReceiverName);

        eventReceiverAdminServiceClient.setTracingEnabled(eventReceiverName, false);
        eventReceiverAdminServiceClient.setStatisticsEnabled(eventReceiverName, true);
        EventReceiverConfigurationInfoDto[] eventReceiverConfigurationInfoDtos = eventReceiverAdminServiceClient
                .getAllStreamSpecificActiveEventReceiverConfigurations("org.wso2.mapped.sensor.data:1.0.0");
        Assert.assertEquals(eventReceiverConfigurationInfoDtos.length, startStreamSpecificActiveERCount + 1);
        EventReceiverConfigurationInfoDto deployedERInfoDto = null;
        for (EventReceiverConfigurationInfoDto eventReceiverConfigurationInfoDto : eventReceiverConfigurationInfoDtos) {
            if (eventReceiverConfigurationDto.getEventReceiverName().equals(eventReceiverName)) {
                deployedERInfoDto = eventReceiverConfigurationInfoDto;
                break;
            }
        }
        Assert.assertNotNull(deployedERInfoDto);
        Assert.assertFalse(deployedERInfoDto.getEnableTracing());
        Assert.assertTrue(deployedERInfoDto.getEnableStats());

        String[] supportedAdapterTypes = eventReceiverAdminServiceClient.getAllInputAdapterTypes();
        Assert.assertTrue(Arrays.asList(supportedAdapterTypes).contains(deployedERInfoDto.getInputAdapterType()));

        //Add Wso2event EventPublisher
        String eventPublisherConfig2 = getXMLArtifactConfiguration(samplePath, "wso2eventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig2);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(2000);

        StreamDefinition streamDefinition = EventDefinitionConverterUtils
                .convertFromJson(streamDefinitionAsString);

        Wso2EventClient.publish("thrift", "localhost", String.valueOf(CEPIntegrationTestConstants.TCP_PORT),
                                "admin", "admin", "org.wso2.event.sensor.stream:1.0.0", "wso2eventReceiver.csv",
                                samplePath, streamDefinition, 3, 1000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("wso2eventReceiver.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2eventPublisher.xml");

        Thread.sleep(1000);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event.setMetaData(new Object[]{4354643, false, 501, "temperature"});
        event.setCorrelationData(new Object[]{90.34344, 20.44345});
        event.setPayloadData(new Object[]{2.3f, 20.44345});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event2.setMetaData(new Object[]{4354653, false, 502, "temperature"});
        event2.setCorrelationData(new Object[]{90.34344, 20.44345});
        event2.setPayloadData(new Object[]{2.3f, 20.44345});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
        event3.setMetaData(new Object[]{4354343, false, 503, "temperature"});
        event3.setCorrelationData(new Object[]{90.34344, 20.44345});
        event3.setPayloadData(new Object[]{2.3f, 20.44345});
        eventList.add(event3);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), eventList.size(), "Incorrect number of messages consumed!");

            int counter = 0;
            for (Event currentEvent : agentServer.getPreservedEventList()) {
                //Time stamp is dynamically added at the receiver client so it cannot be tested
                currentEvent.setTimeStamp(0);
                Assert.assertEquals(currentEvent.toString(), eventList.get(counter).toString(), "Mapping is incorrect!");
                counter++;
            }
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
