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
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.rmi.RemoteException;

/**
 * Testing Logger publisher in different formats (text, xml, json)
 */
public class EventSimulatorTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(EventSimulatorTestCase.class);

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

    }


    @Test(groups = {"wso2.cep"}, description = "Testing Event Simulation (Play, Pause and Resume)")
    public void EventSimulatorTestScenario() throws Exception {
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        Wso2EventServer wso2EventServer = new Wso2EventServer("eventsimulatorFiles", CEPIntegrationTestConstants.TCP_PORT, true);


        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration("eventsimulatorFiles",
                "TempStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add Text Logger
        String eventPublisherConfig = getXMLArtifactConfiguration("eventsimulatorFiles", "tempEventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        //Copy Event Simulator File
        String eventSimulatorFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "CEP" + File.separator + "eventsimulatorFiles"
                + File.separator;

        String eventSimulatorDirectoryPath = FrameworkPathUtil.getCarbonHome() + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server"
                + File.separator + "eventsimulatorfiles" + File.separator;
        try {
            FileManager.copyResourceToFileSystem(eventSimulatorFilePath + "events.csv", eventSimulatorDirectoryPath, "events.csv");
        } catch (Exception e) {
            throw new RemoteException("Exception caught when deploying the car file into CEP server", e);
        }
        log.info("deploying Event Simulator File...");
        Thread.sleep(35000);

        wso2EventServer.startServer();

        eventSimulatorAdminServiceClient.sendConfigDetails("events.csv", "TempStream:1.0.0", ",", 1000);
        Thread.sleep(10000);
        eventSimulatorAdminServiceClient.sendEventsViaFile("events.csv");
        Thread.sleep(3000);
        eventSimulatorAdminServiceClient.pauseEventsViaFile("events.csv");
        Thread.sleep(3000);
        eventSimulatorAdminServiceClient.resumeEventsViaFile("events.csv");
        Thread.sleep(10000);

        Assert.assertEquals(wso2EventServer.getMsgCount(), 13, "Incorrect number of messages consumed!");
        Thread.sleep(2000);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("logger.xml");
        eventSimulatorAdminServiceClient.deleteFile("events.csv");
        wso2EventServer.stop();

        Thread.sleep(2000);

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
