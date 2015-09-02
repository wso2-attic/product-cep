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

package org.wso2.carbon.integration.test.storm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Instance;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.test.client.AnalyticStatClient;
import org.wso2.carbon.integration.test.client.StockQuoteClient;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class StormTestCase extends CEPIntegrationTest {

    private static Log log = LogFactory.getLog(StormTestCase.class);
    private AutomationContext automationContext;
    private Map<String, Instance> instanceMap;
    private Map<String, AutomationContext> contextMap;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        automationContext = new AutomationContext("CEP", TestUserMode.SUPER_TENANT_ADMIN);
        instanceMap = automationContext.getProductGroup().getInstanceMap();
        contextMap = new HashMap<String, AutomationContext>();

        if (instanceMap != null && instanceMap.size() > 0) {
            for (Map.Entry<String, Instance> entry : instanceMap.entrySet()) {
                String instanceKey = entry.getKey();
                contextMap.put(instanceKey, new AutomationContext("CEP", instanceKey,
                                                                  TestUserMode.SUPER_TENANT_ADMIN));
                log.info(instanceKey);
            }
        }
        log.info("Cluster instance loading");

    }

    @Test(groups = {"wso2.cep"}, description = "Test CEP Storm integration for single query setup one member ")
    public void testSingleQueryTopologyOneMember() throws Exception {

        configureNode(contextMap.get("cep001"));
        Wso2EventServer wso2EventServer = new Wso2EventServer("StormTestCase", CEPIntegrationTestConstants
                .STORM_WSO2EVENT_SERVER_PORT, true);
        Thread thread = new Thread(wso2EventServer);
        thread.start();

        Thread.sleep(5000);

        AnalyticStatClient.publish(contextMap.get("cep001").getInstance().getHosts().get("default"),
                                   String.valueOf(CEPIntegrationTestConstants.THRIFT_RECEIVER_PORT), "admin", "admin", 100);
        StockQuoteClient.publish(contextMap.get("cep001").getInstance().getHosts().get("default"),
                                 String.valueOf(CEPIntegrationTestConstants.THRIFT_RECEIVER_PORT), "admin", "admin", 100);

        Thread.sleep(60000);
        Assert.assertTrue(wso2EventServer.getMsgCount() > 0);
        wso2EventServer.stop();
    }

    @Test(groups = {"wso2.cep"}, description = "Test CEP Storm integration for single query setup")
    public void testSingleQueryTopology() throws Exception {
        configureNode(contextMap.get("cep002"));
        configureNode(contextMap.get("cep003"));

        Wso2EventServer wso2EventServer = new Wso2EventServer("StormTestCase", CEPIntegrationTestConstants
                .STORM_WSO2EVENT_SERVER_PORT, true);
        Thread thread = new Thread(wso2EventServer);
        thread.start();

        Thread.sleep(5000);

        AnalyticStatClient.publish(contextMap.get("cep002").getInstance().getHosts().get("default"),
                                   contextMap.get("cep002").getInstance().getPorts().get("thrift_receiver"), "admin", "admin", 100);
        StockQuoteClient.publish(contextMap.get("cep002").getInstance().getHosts().get("default"),
                                 contextMap.get("cep002").getInstance().getPorts().get("thrift_receiver"), "admin", "admin", 100);

        Thread.sleep(60000);
        Assert.assertTrue(wso2EventServer.getMsgCount() > 0);
        wso2EventServer.stop();
    }

    private void configureNode(AutomationContext node) throws Exception {
        String backendURL = node.getContextUrls().getBackEndUrl();
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(node);
        String loggedInSessionCookie = loginLogoutClient.login();
        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);

        log.info("Adding stream definitions");
        defineStreams();
        log.info("Adding event receiver analyticsWso2EventReceiver");
        addEventReceiver("analyticsWso2EventReceiver.xml");
        log.info("Adding event receiver stockQuoteWso2EventReceiver");
        addEventReceiver("stockQuoteWso2EventReceiver.xml");
        log.info("Adding event publisher fortuneCompanyWSO2EventPublisher");
        addEventPublisher("fortuneCompanyWSO2EventPublisher.xml");
        log.info("Adding execution plan");
        addExecutionPlan("PreprocessStats.siddhiql");
    }

    private void addExecutionPlan(String config) throws Exception {
        int initialExecutionPlanCount = eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount();
        String executionPlanConfig = getExecutionPlanFromFile("StormTestCase", config);
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanConfig);
        Thread.sleep(3000);
        Assert.assertEquals(eventProcessorAdminServiceClient.getActiveExecutionPlanConfigurationCount(), initialExecutionPlanCount + 1);
    }

    private void addEventReceiver(String config) throws Exception {
        int startErCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        String eventReceiverConfig = getXMLArtifactConfiguration("StormTestCase", config);
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiverConfig);
        Thread.sleep(3000);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), startErCount + 1);
    }

    private void addEventPublisher(String config) throws Exception {
        int startEpCount = eventPublisherAdminServiceClient.getEventPublisherCount();
        String eventPublisherConfig = getXMLArtifactConfiguration("StormTestCase", config);
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Thread.sleep(3000);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEpCount + 1);
    }

    private void defineStreams() throws RemoteException, InterruptedException {
        log.info("Adding analytic statistic stream definition");

        int streamStartCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        EventStreamDefinitionDto eventStreamDefinitionDto = new EventStreamDefinitionDto();

        EventStreamAttributeDto[] metaEventStreamAttributeDtos = new EventStreamAttributeDto[4];
        EventStreamAttributeDto metaEventStreamAttributeDto1 = new EventStreamAttributeDto();
        metaEventStreamAttributeDto1.setAttributeName("ipAdd");
        metaEventStreamAttributeDto1.setAttributeType("string");
        metaEventStreamAttributeDtos[0] = metaEventStreamAttributeDto1;

        EventStreamAttributeDto metaEventStreamAttributeDto2 = new EventStreamAttributeDto();
        metaEventStreamAttributeDto2.setAttributeName("index");
        metaEventStreamAttributeDto2.setAttributeType("long");
        metaEventStreamAttributeDtos[1] = metaEventStreamAttributeDto2;

        EventStreamAttributeDto metaEventStreamAttributeDto3 = new EventStreamAttributeDto();
        metaEventStreamAttributeDto3.setAttributeName("timestamp");
        metaEventStreamAttributeDto3.setAttributeType("long");
        metaEventStreamAttributeDtos[2] = metaEventStreamAttributeDto3;

        EventStreamAttributeDto metaEventStreamAttributeDto4 = new EventStreamAttributeDto();
        metaEventStreamAttributeDto4.setAttributeName("nanoTime");
        metaEventStreamAttributeDto4.setAttributeType("long");
        metaEventStreamAttributeDtos[3] = metaEventStreamAttributeDto4;

        EventStreamAttributeDto payloadEventStreamAttributeDto1 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto1.setAttributeName("userID");
        payloadEventStreamAttributeDto1.setAttributeType("string");

        EventStreamAttributeDto payloadEventStreamAttributeDto2 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto2.setAttributeName("searchTerms");
        payloadEventStreamAttributeDto2.setAttributeType("string");

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos
                = new EventStreamAttributeDto[]{payloadEventStreamAttributeDto1, payloadEventStreamAttributeDto2};

        eventStreamDefinitionDto.setName("analytics_Statistics");
        eventStreamDefinitionDto.setVersion("1.3.0");
        eventStreamDefinitionDto.setMetaData(metaEventStreamAttributeDtos);
        eventStreamDefinitionDto.setCorrelationData(null);
        eventStreamDefinitionDto.setPayloadData(payloadEventStreamAttributeDtos);
        eventStreamDefinitionDto.setDescription("");
        eventStreamDefinitionDto.setNickName("");

        eventStreamManagerAdminServiceClient.addEventStreamAsDTO(eventStreamDefinitionDto);
        Thread.sleep(1000);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), streamStartCount + 1);

        log.info("Adding stock-quote stream definition");
        EventStreamAttributeDto payloadEventStreamAttributeDto21 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto21.setAttributeName("price");
        payloadEventStreamAttributeDto21.setAttributeType("int");

        EventStreamAttributeDto payloadEventStreamAttributeDto22 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto22.setAttributeName("symbol");
        payloadEventStreamAttributeDto22.setAttributeType("string");

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos2 =
                new EventStreamAttributeDto[]{payloadEventStreamAttributeDto21, payloadEventStreamAttributeDto22};

        eventStreamDefinitionDto = new EventStreamDefinitionDto();
        eventStreamDefinitionDto.setName("stock_quote");
        eventStreamDefinitionDto.setVersion("1.3.0");
        eventStreamDefinitionDto.setMetaData(null);
        eventStreamDefinitionDto.setCorrelationData(null);
        eventStreamDefinitionDto.setPayloadData(payloadEventStreamAttributeDtos2);
        eventStreamDefinitionDto.setDescription("");
        eventStreamDefinitionDto.setNickName("");
        eventStreamManagerAdminServiceClient.addEventStreamAsDTO(eventStreamDefinitionDto);
        Thread.sleep(1000);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), streamStartCount + 2);

        log.info("Adding fortune company stream definition");
        EventStreamAttributeDto payloadEventStreamAttributeDto31 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto31.setAttributeName("price");
        payloadEventStreamAttributeDto31.setAttributeType("int");

        EventStreamAttributeDto payloadEventStreamAttributeDto32 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto32.setAttributeName("symbol");
        payloadEventStreamAttributeDto32.setAttributeType("string");

        EventStreamAttributeDto payloadEventStreamAttributeDto33 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto33.setAttributeName("count");
        payloadEventStreamAttributeDto33.setAttributeType("long");

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos3 = new
                EventStreamAttributeDto[]{payloadEventStreamAttributeDto31, payloadEventStreamAttributeDto32, payloadEventStreamAttributeDto33};

        eventStreamDefinitionDto = new EventStreamDefinitionDto();
        eventStreamDefinitionDto.setName("fortuneCompanyStream");
        eventStreamDefinitionDto.setVersion("1.0.0");
        eventStreamDefinitionDto.setMetaData(null);
        eventStreamDefinitionDto.setCorrelationData(null);
        eventStreamDefinitionDto.setPayloadData(payloadEventStreamAttributeDtos3);
        eventStreamDefinitionDto.setDescription("");
        eventStreamDefinitionDto.setNickName("");
        eventStreamManagerAdminServiceClient.addEventStreamAsDTO(eventStreamDefinitionDto);
        Thread.sleep(1000);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), streamStartCount + 3);

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("fortuneCompanyWSO2EventPublisher");
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("PreprocessStats");
        eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("analyticsWso2EventReceiver");
        eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("stockQuoteWso2EventReceiver");
        eventStreamManagerAdminServiceClient.removeEventStream("analytics_Statistics", "1.3.0");
        eventStreamManagerAdminServiceClient.removeEventStream("stock_quote", "1.3.0");
        eventStreamManagerAdminServiceClient.removeEventStream("fortuneCompanyStream", "1.0.0");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
