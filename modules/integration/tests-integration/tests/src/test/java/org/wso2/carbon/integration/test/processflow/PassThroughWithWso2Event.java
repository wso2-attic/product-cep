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
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.test.client.StatPublisherAgent;
import org.wso2.carbon.integration.test.client.TestAgentServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.ConfigurationUtil;

import java.rmi.RemoteException;

/**
 * Sample 0001 - Simple Pass-through with WSO2Event
 */
public class PassThroughWithWso2Event extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(PassThroughWithWso2Event.class);
    private static final String INPUT_STREAM_NAME = "org.wso2.sample.service.data";
    private static final String OUTPUT_STREAM_NAME = "org.wso2.sample.service.response.time";
    private static final String STREAM_VERSION = "1.0.0";


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = new LoginLogoutClient(cepServer).login();
        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "This is a test by Sajith")
    public void sampleTest() throws Exception {
        final int messageCount = 5;
        // Adding input event adaptor
        configurationUtil.addWso2EventInputEventAdaptor("WSO2EventAdaptor");

        // Adding output wso2 event adaptor
        configurationUtil.addThriftOutputEventAdaptor();

        // Adding stream definition
        addStreamDefinition(INPUT_STREAM_NAME, STREAM_VERSION, "Statistics", "Service Statistics", false);
        addStreamDefinition(OUTPUT_STREAM_NAME, STREAM_VERSION, "Filtered Statistics", "Filtered Service Statistics", true);

        // Adding event builder
        String eventBuilderConfigPath = getTestArtifactLocation() + "/artifacts/CEP/ebconfigs/ServiceStats.xml";
        String eventBuilderConfig = getArtifactConfigurationFromClasspath(eventBuilderConfigPath);
        eventBuilderAdminServiceClient.addEventBuilderConfiguration(eventBuilderConfig);

        // Adding event formatter
        String eventFormatterConfigPath = getTestArtifactLocation() + "/artifacts/CEP/ebconfigs/StatResponseTime.xml";
        String eventFormatterConfig = getArtifactConfigurationFromClasspath(eventFormatterConfigPath);
        eventFormatterAdminServiceClient.addEventFormatterConfiguration(eventFormatterConfig);

        // The data-bridge receiver
        TestAgentServer agentServer = new TestAgentServer();
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(1000);

        StatPublisherAgent.start(messageCount);
        //wait while all stats are published
        Thread.sleep(10000);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");

            eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("StatsResponseTime");
            eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("ServiceStats");
            inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("WSO2EventAdaptor");
            configurationUtil.removeThriftOutputEventAdaptor();

            Thread.sleep(2000);
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    private void addStreamDefinition(String streamName, String version, String description, String nickName, boolean isOutput) throws RemoteException {
        EventStreamAttributeDto[] metaData;
        EventStreamAttributeDto[] payloadData;
        final String metaPrefix = (isOutput) ? "meta_" : "";

        EventStreamAttributeDto requestUrl = ConfigurationUtil.createEventStreamAttributeDto(metaPrefix + "request_url", "string");
        EventStreamAttributeDto remoteAddress = ConfigurationUtil.createEventStreamAttributeDto(metaPrefix + "remote_address", "string");
        EventStreamAttributeDto contentType = ConfigurationUtil.createEventStreamAttributeDto(metaPrefix + "content_type", "string");
        EventStreamAttributeDto userAgent = ConfigurationUtil.createEventStreamAttributeDto(metaPrefix + "user_agent", "string");
        EventStreamAttributeDto host = ConfigurationUtil.createEventStreamAttributeDto(metaPrefix + "host", "string");
        EventStreamAttributeDto referer = ConfigurationUtil.createEventStreamAttributeDto(metaPrefix + "referer", "string");
        metaData = new EventStreamAttributeDto[]{requestUrl, remoteAddress, contentType, userAgent, host, referer};

        EventStreamAttributeDto serviceName = ConfigurationUtil.createEventStreamAttributeDto("service_name", "string");
        EventStreamAttributeDto operationName = ConfigurationUtil.createEventStreamAttributeDto("operation_name", "string");
        EventStreamAttributeDto timestamp = ConfigurationUtil.createEventStreamAttributeDto("timestamp", "long");
        EventStreamAttributeDto responseTime = ConfigurationUtil.createEventStreamAttributeDto("response_time", "long");
        EventStreamAttributeDto requestCount = ConfigurationUtil.createEventStreamAttributeDto("request_count", "int");
        EventStreamAttributeDto responseCount = ConfigurationUtil.createEventStreamAttributeDto("response_count", "int");
        EventStreamAttributeDto faultCount = ConfigurationUtil.createEventStreamAttributeDto("fault_count", "int");
        payloadData = new EventStreamAttributeDto[]{serviceName, operationName, timestamp, responseTime, requestCount, responseCount, faultCount};

        eventStreamManagerAdminServiceClient.addEventStream(streamName, version, metaData, null, payloadData, description, nickName);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
