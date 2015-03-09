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
import org.wso2.appserver.integration.common.clients.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.rmi.RemoteException;

/**
 * Check whether CEPAdminService properly creates SiddhiBucket to be used with localBroker
 */
public class XMLBasedDeploymentTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(XMLBasedDeploymentTestCase.class);
    private EventFormatterAdminServiceClient eventFormatterAdminServiceClient;
    private EventBuilderAdminServiceClient eventBuilderAdminServiceClient;
    private EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    private InputEventAdaptorManagerAdminServiceClient inputEventAdaptorManagerAdminServiceClient;
    private OutputEventAdaptorManagerAdminServiceClient outputEventAdaptorManagerAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception, RemoteException {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);

        configurationUtil.addInEventStream();
        configurationUtil.addOutEventStream();
    }

    //Scenario 1 adding order

    @Test(groups = {"wso2.cep"}, description = "Test the XML EP configuration file deployment order ITA, EB, EP, OTA, EF")
    public void addInputEventAdaptorTestScenario()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount();
        configurationUtil.addThriftInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdaptorTestScenario"})
    public void addEventBuilderTestScenario() throws RemoteException, InterruptedException {

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        configurationUtil.addEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario"})
    public void addXMLEventProcessorTestScenario() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount();

        eventProcessorAdminServiceClient.addExecutionPlan(
                "<executionPlan name=\"TestExecutionPlan1\" statistics=\"enable\"\n" +
                "  trace=\"enable\" xmlns=\"http://wso2.org/carbon/eventprocessor\">\n" +
                "  <description>A sample passthru query 1</description>\n" +
                "  <siddhiConfiguration>\n" +
                "    <property name=\"siddhi.enable.distributed.processing\">false</property>\n" +
                "    <property name=\"siddhi.persistence.snapshot.time.interval.minutes\">0</property>\n" +
                "  </siddhiConfiguration>\n" +
                "  <importedStreams>\n" +
                "    <stream as=\"InStream\"\n" +
                "      name=\"InStream\" version=\"1.0.0\"/>\n" +
                "  </importedStreams>\n" +
                "  <queryExpressions><![CDATA[from InStream select * insert into OutStream;]]></queryExpressions>\n" +
                "  <exportedStreams>\n" +
                "    <stream name=\"OutStream\"\n" +
                "      valueOf=\"OutStream\" version=\"1.0.0\"/>\n" +
                "  </exportedStreams>\n" +
                "</executionPlan>"
        );

        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), 1 + startCount);
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlan("TestExecutionPlan1").getDescription(), "A sample passthru query 1");

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addXMLEventProcessorTestScenario"})
    public void addDuplicateXMLEventProcessorTestScenario() throws RemoteException, InterruptedException {
        log.info("=======================Adding duplicate execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount();

        boolean exceptionOccurred =false;
        try {

            eventProcessorAdminServiceClient.addExecutionPlan(
                    "<executionPlan name=\"TestExecutionPlan1\" statistics=\"enable\"\n" +
                            "  trace=\"enable\" xmlns=\"http://wso2.org/carbon/eventprocessor\">\n" +
                            "  <description>A sample passthru query 2</description>\n" +
                            "  <siddhiConfiguration>\n" +
                            "    <property name=\"siddhi.enable.distributed.processing\">false</property>\n" +
                            "    <property name=\"siddhi.persistence.snapshot.time.interval.minutes\">0</property>\n" +
                            "  </siddhiConfiguration>\n" +
                            "  <importedStreams>\n" +
                            "    <stream as=\"InStream\"\n" +
                            "      name=\"InStream\" version=\"1.0.0\"/>\n" +
                            "  </importedStreams>\n" +
                            "  <queryExpressions><![CDATA[from InStream select * insert into OutStream;]]></queryExpressions>\n" +
                            "  <exportedStreams>\n" +
                            "    <stream name=\"OutStream\"\n" +
                            "      valueOf=\"OutStream\" version=\"1.0.0\"/>\n" +
                            "  </exportedStreams>\n" +
                            "</executionPlan>"
            );
        }catch (RemoteException e){
            exceptionOccurred=true;
        }
        Assert.assertEquals(exceptionOccurred, true);

        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), startCount);
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlan("TestExecutionPlan1").getDescription(), "A sample passthru query 1");

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addXMLEventProcessorTestScenario"})
    public void addOutputEventAdaptorTestScenario()
            throws RemoteException, InterruptedException {
        int startCount = outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount();
        Assert.assertEquals(startCount, 1);

        log.info("=======================Adding a Output event adaptor ======================= ");
        configurationUtil.addOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdaptorTestScenario"})
    public void addEventFormatterTestScenario() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getActiveEventFormatterCount();
        Assert.assertEquals(startCount, 0);
        log.info("=======================Adding a event formatter ======================= ");
        configurationUtil.addEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1 + startCount);

    }

    //Scenario 1 removing order

    @Test(groups = {"wso2.cep"}, description = "Test the xml configuration file un-deployment order ITA, EB, EP, OTA, EF", dependsOnMethods = {"addEventFormatterTestScenario"})
    public void removeInputEventAdaptorTestScenario()
            throws RemoteException, InterruptedException {

        log.info("=======================Removing a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeInputEventAdaptorTestScenario"})
    public void removeEventBuilderTestScenario() throws RemoteException, InterruptedException {

        log.info("=======================Removing a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();
        configurationUtil.removeInActiveEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventBuilderTestScenario"})
    public void removeEventProcessorTestScenario() throws RemoteException, InterruptedException {
        log.info("=======================Removing a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.removeInActiveEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventProcessorTestScenario"})
    public void removeOutputEventAdaptorTestScenario()
            throws RemoteException, InterruptedException {
        log.info("=======================Removing a Output event adaptor ======================= ");
        int startCount = outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount(), startCount - 1);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeOutputEventAdaptorTestScenario"})
    public void removeEventFormatterTestScenario() throws RemoteException, InterruptedException {
        log.info("=======================Removing a event formatter ======================= ");
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        configurationUtil.removeInActiveEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), startCount - 1);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
//        inputEventAdaptorManagerAdminServiceClient.removeExecutionPlan(EXECUTION_PLAN_NAME);
        inputEventAdaptorManagerAdminServiceClient = null;
        outputEventAdaptorManagerAdminServiceClient = null;
        eventBuilderAdminServiceClient = null;
        eventProcessorAdminServiceClient = null;
        eventFormatterAdminServiceClient = null;
        eventStreamManagerAdminServiceClient.removeEventStream("InStream","1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("OutStream","1.0.0");
    }

}
