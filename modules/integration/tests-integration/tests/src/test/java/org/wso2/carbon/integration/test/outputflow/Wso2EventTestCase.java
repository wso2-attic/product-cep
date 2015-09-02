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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sending different formatted events to the Wso2Event Publisher and consume using a client
 */
public class Wso2EventTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(Wso2EventTestCase.class);

    private ServerConfigurationManager serverManager = null;

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventSimulatorAdminServiceClient = configurationUtil.getEventSimulatorAdminServiceClient(
                backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing wso2event publisher with custom mapping formatting")
    public void wso2EventPublisherMapTestWithCustomMappingScenario() throws Exception {
        final int messageCount = 3;
        String samplePath = "outputflows" + File.separator + "sample0058";
        Wso2EventServer wso2EventServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        List<EventDto> events = new ArrayList<>();

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

        events.add(eventDto);
        events.add(eventDto2);
        events.add(eventDto3);

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                       "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);


        String streamDefinitionMapAsString = getJSONArtifactConfiguration(samplePath,
                                                                          "org.wso2.event.sensor.stream.map_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionMapAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 2);

        //Add eventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "eventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        wso2EventServer.startServer();

        Thread.sleep(1000);
        eventSimulatorAdminServiceClient.sendEvent(eventDto);
        eventSimulatorAdminServiceClient.sendEvent(eventDto2);
        eventSimulatorAdminServiceClient.sendEvent(eventDto3);
        Thread.sleep(1000);

        Assert.assertEquals(wso2EventServer.getMsgCount(), messageCount,
                            "Incorrect number of messages consumed!");

        int counter = 0;
        for (Event currentEvent : wso2EventServer.getPreservedEventList()) {
            Event event = new Event();
            //Mapped stream name is org.wso2.event.sensor.stream.map:1.0.0
            event.setStreamId("org.wso2.event.sensor.stream.map:1.0.0");
            //first four attributes are meta data
            event.setMetaData(Arrays.copyOfRange(events.get(counter).getAttributeValues(), 0, 4));
            //next two attributes are correlation data
            event.setCorrelationData(Arrays.copyOfRange(events.get(counter).getAttributeValues(), 4, 6));
            //final two attributes are payload
            event.setPayloadData(Arrays.copyOfRange(events.get(counter).getAttributeValues(), 6, 8));

            currentEvent.setTimeStamp(0);

            Assert.assertEquals(currentEvent.toString(), event.toString(), "Mapping is incorrect!");
            counter++;
        }

        wso2EventServer.stop();
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream.map", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("eventPublisher.xml");

    }

    @Test(groups = {"wso2.cep"}, description = "Testing WSO2Event publisher connection")
    public void testConnection() {

        String samplePath = "outputflows" + File.separator + "sample0058";

        BasicOutputAdapterPropertyDto username = new BasicOutputAdapterPropertyDto();
        username.setKey("username");
        username.setValue("admin");
        username.set_static(true);
        BasicOutputAdapterPropertyDto protocol = new BasicOutputAdapterPropertyDto();
        protocol.setKey("protocol");
        protocol.setValue("thrift");
        protocol.set_static(true);
        BasicOutputAdapterPropertyDto publishingMode = new BasicOutputAdapterPropertyDto();
        publishingMode.setKey("publishingMode");
        publishingMode.setValue("blocking");
        publishingMode.set_static(true);
        BasicOutputAdapterPropertyDto receiverURL = new BasicOutputAdapterPropertyDto();
        receiverURL.setKey("receiverURL");
        receiverURL.setValue("tcp://localhost:7611");
        receiverURL.set_static(true);
        BasicOutputAdapterPropertyDto password = new BasicOutputAdapterPropertyDto();
        password.setKey("password");
        password.setValue("admin");
        password.set_static(true);
        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration = new BasicOutputAdapterPropertyDto[]
                {username, protocol, publishingMode, receiverURL, password};
        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                                                                           "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);


            String streamDefinitionMapAsString = getJSONArtifactConfiguration(samplePath,
                                                                              "org.wso2.event.sensor.stream.map_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionMapAsString);

            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "eventPublisher.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);

            eventPublisherAdminServiceClient.testConnection("eventPublisher", "wso2event", outputPropertyConfiguration, "wso2event");

            eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("eventPublisher");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream.map", "1.0.0");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        //reverting the changes done to cep sever
        if (serverManager != null) {
            serverManager.restoreToLastConfiguration();
        }
        super.cleanup();
    }
}
