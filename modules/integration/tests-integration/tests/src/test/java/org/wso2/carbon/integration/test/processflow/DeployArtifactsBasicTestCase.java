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

/**
 * Deploying artifacts using given config files (of type XML/JSON), in typical deployment order.
 */
public class DeployArtifactsBasicTestCase  extends CEPIntegrationTest {

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

    @Test(groups = {"wso2.cep"}, description = "Testing whether artifacts get activated properly upon deployment.")
    public void addArtifactsTestScenario() throws Exception {
        eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        eventReceiverCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        eventPublisherCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();
        executionPlanCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();

        //Add StreamDefinition
        String pizzaStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsBasicTestCase", "org.wso2.sample.pizza.order_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(pizzaStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        //Add another StreamDefinition
        String outStreamDefinition = getJSONArtifactConfiguration("DeployArtifactsBasicTestCase", "outStream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(outStreamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        //Add HTTP EventReceiver
        String eventReceiverConfig = getXMLArtifactConfiguration("DeployArtifactsBasicTestCase", "PizzaOrder.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), ++eventReceiverCount);

        //Add HTTP Publisher
        String eventPublisherConfig = getXMLArtifactConfiguration("DeployArtifactsBasicTestCase", "PizzaDeliveryNotification.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), ++eventPublisherCount);

        //Add execution plan
        String executionPlan = getExecutionPlanFromFile("DeployArtifactsBasicTestCase", "testPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), ++executionPlanCount);
    }

    @Test(groups = {"wso2.cep"}, description = "Removing artifacts." ,dependsOnMethods = {"addArtifactsTestScenario"} )
    public void removeArtifactsTestScenario() throws Exception {
        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order","1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("outStream","1.0.0");
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount - 2);

        eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("PizzaOrder.xml");
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), eventReceiverCount - 1);

        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("PizzaDeliveryNotification.xml");
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), eventPublisherCount - 1);

        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("testPlan.siddhiql");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), executionPlanCount - 1);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
