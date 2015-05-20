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

package org.wso2.carbon.integration.test.processflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

public class DeployArtifactsTestCase extends CEPIntegrationTest{
    private static final Log log = LogFactory.getLog(DeployArtifactsBasicTestCase.class);
    private static int eventStreamCount;
    private static int eventReceiverCount;
    private static int eventPublisherCount;
    private static int executionPlanCount;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing the order: EP, ER, ES-1, ES-2, ExP")
    public void addArtifactsTestScenario1() throws Exception {
        log.info("=======================Testing the order:  EP, ER, ES-1, ES-2, ExP======================= ");
        eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        eventReceiverCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        eventPublisherCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        executionPlanCount = eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount();

        log.info("=======================Adding an event receiver ======================= ");
        String eventReceiverConfig = getXMLArtifactConfiguration("DeployArtifactsTestCase", "PizzaOrder.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), eventReceiverCount);   //ER should be inactive

        log.info("=======================Adding an event publisher ======================= ");
        String eventPublisherConfig = getXMLArtifactConfiguration("DeployArtifactsTestCase", "PizzaDeliveryNotification.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), eventPublisherCount);   //EP should be inactive

        log.info("=======================Adding a stream definition====================");
        String pizzaStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsTestCase", "org.wso2.sample.pizza.order_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(pizzaStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        log.info("=======================Adding another stream definition====================");
        String outStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsTestCase", "outStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(outStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        log.info("=======================Adding an execution plan ======================= ");
        String executionPlan = getExecutionPlanFromFile("DeployArtifactsTestCase", "testPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), ++executionPlanCount);

        Thread.sleep(1000);

        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), ++eventReceiverCount);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), ++eventPublisherCount);
    }

    @Test(groups = {"wso2.cep"}, description = "Removing artifacts." ,dependsOnMethods = {"addArtifactsTestScenario1"})
    public void removeArtifactsTestScenario() throws Exception {
        eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("PizzaOrder");
        eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("PizzaDeliveryNotification");
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), eventReceiverCount - 1);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), eventPublisherCount - 1);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order","1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("outStream","1.0.0");
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount - 2);

        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("testPlan.siddhiql");
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), executionPlanCount - 1);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing the order: ES-1, ES-2, EP, ER, ExP", dependsOnMethods = {"removeArtifactsTestScenario"})
    public void addArtifactsTestScenario2() throws Exception {
        log.info("=======================Testing the order:  ES-1, ES-2, EP, ER, ExP======================= ");
        eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        eventReceiverCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        eventPublisherCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        executionPlanCount = eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount();

        log.info("=======================Adding a stream definition====================");
        String pizzaStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsTestCase", "org.wso2.sample.pizza.order_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(pizzaStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        log.info("=======================Adding another stream definition====================");
        String outStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsTestCase", "outStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(outStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        log.info("=======================Adding an event receiver ======================= ");
        String eventReceiverConfig = getXMLArtifactConfiguration("DeployArtifactsTestCase", "PizzaOrder.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), ++eventReceiverCount);

        log.info("=======================Adding an event publisher ======================= ");
        String eventPublisherConfig = getXMLArtifactConfiguration("DeployArtifactsTestCase", "PizzaDeliveryNotification.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), ++eventPublisherCount);

        log.info("=======================Adding an execution plan ======================= ");
        String executionPlan = getExecutionPlanFromFile("DeployArtifactsTestCase", "testPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), ++executionPlanCount);

        Thread.sleep(1000);

    }

    @Test(groups = {"wso2.cep"}, description = "Removing artifacts." ,dependsOnMethods = {"addArtifactsTestScenario2"})
    public void removeArtifactsTestScenario2() throws Exception {
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order","1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("outStream", "1.0.0");
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount - 2);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), eventReceiverCount - 1);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), eventPublisherCount - 1);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), executionPlanCount - 1);
        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("PizzaOrder.xml");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("PizzaDeliveryNotification.xml");
        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("testPlan.siddhiql");
    }

    @Test(groups = {"wso2.cep"}, description = "Testing the order: ExP, EP, ER, ES-1, ES-2", dependsOnMethods = {"removeArtifactsTestScenario2"})
    public void addArtifactsTestScenario3() throws Exception {
        log.info("=======================Testing the order: ExP, EP, ER, ES-1, ES-2======================= ");
        eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        eventReceiverCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        eventPublisherCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        executionPlanCount = eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount();

        log.info("=======================Adding an execution plan ======================= ");
        String executionPlan = getExecutionPlanFromFile("DeployArtifactsTestCase", "testPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), executionPlanCount);   //EP should be inactive

        log.info("=======================Adding an event receiver ======================= ");
        String eventReceiverConfig = getXMLArtifactConfiguration("DeployArtifactsTestCase", "PizzaOrder.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), eventReceiverCount);   //ER should be inactive

        log.info("=======================Adding an event publisher ======================= ");
        String eventPublisherConfig = getXMLArtifactConfiguration("DeployArtifactsTestCase", "PizzaDeliveryNotification.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), eventPublisherCount);   //EP should be inactive

        log.info("=======================Adding a stream definition====================");
        String pizzaStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsTestCase", "org.wso2.sample.pizza.order_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(pizzaStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        log.info("=======================Adding another stream definition====================");
        String outStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsTestCase", "outStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(outStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);


        Thread.sleep(1000);

        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), ++executionPlanCount);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), ++eventReceiverCount);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), ++eventPublisherCount);
    }

    @Test(groups = {"wso2.cep"}, description = "Removing artifacts." ,dependsOnMethods = {"addArtifactsTestScenario3"})
    public void removeArtifactsTestScenario3() throws Exception {
        eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("PizzaOrder");
        eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("PizzaDeliveryNotification");
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), eventReceiverCount - 1);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), eventPublisherCount - 1);

        eventProcessorAdminServiceClient.removeActiveExecutionPlan("testPlan");
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), executionPlanCount - 1);

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order","1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("outStream","1.0.0");
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount - 2);

          }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
