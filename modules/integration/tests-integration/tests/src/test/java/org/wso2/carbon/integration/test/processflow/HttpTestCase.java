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
package org.wso2.carbon.integration.test.processflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.integration.test.client.HttpEventPublisherClient;
import org.wso2.carbon.integration.test.client.TestAgentServer;
import org.wso2.carbon.integration.test.client.Wso2EventClient;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

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
        final int messageCount = 5;
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration("HttpTestCase","org.wso2.event.statistics.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        StreamDefinition streamDefinition = EventDefinitionConverterUtils
                .convertFromJson(streamDefinitionAsString);

        //Add Wso2event EventReceiver
        String eventReceiverConfig = getXMLArtifactConfiguration("HttpTestCase/JSONTest", "httpReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startERCount + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration("HttpTestCase", "httpLogger.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        // The data-bridge receiver
        TestAgentServer agentServer = new TestAgentServer("HttpTestCase",7661);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(1000);

        HttpEventPublisherClient.publish("http://localhost:9763/endpoints/httpReceiver", "admin", "admin",
                "HttpTestCase/JSONTest", "httpReceiver.txt");

        //wait while all stats are published
        Thread.sleep(30000);

        try {
            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");

            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.statistics.stream","1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("httpLogger");
            eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("httpReceiver");
            Thread.sleep(2000);
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
