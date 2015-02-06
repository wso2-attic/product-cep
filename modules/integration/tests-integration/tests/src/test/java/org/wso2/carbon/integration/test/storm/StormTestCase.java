/*
 * Copyright (c) 2014 - 2015, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto;
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.test.client.AnalyticStatClient;
import org.wso2.carbon.integration.test.client.StockQuoteClient;
import org.wso2.carbon.integration.test.client.TestAgentServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class StormTestCase extends CEPIntegrationTest {

    private static Log log = LogFactory.getLog(StormTestCase.class);
    private AutomationContext automationContext;
    private Map<String,Instance> instanceMap;
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

    @Test(groups = {"wso2.cep"}, description = "Test CEP Storm integration for single query setup")
    public void testSingleQueryTopology() throws Exception {
        configureNode(contextMap.get("cep002"));
        configureNode(contextMap.get("cep003"));

        TestAgentServer testAgentServer = new TestAgentServer();
        Thread thread = new Thread(testAgentServer);
        thread.start();

        Thread.sleep(5000);

        AnalyticStatClient.publish(contextMap.get("cep002").getInstance().getHosts().get("default"),
                contextMap.get("cep002").getInstance().getPorts().get("thrift_receiver"), "admin", "admin", 100);
        StockQuoteClient.publish(contextMap.get("cep002").getInstance().getHosts().get("default"),
                contextMap.get("cep002").getInstance().getPorts().get("thrift_receiver"), "admin", "admin", 100);

        Thread.sleep(10000);
        Assert.assertTrue(testAgentServer.getMsgCount()>0);
        testAgentServer.stop();
    }

    private void configureNode(AutomationContext node) throws Exception {
        String backendURL = node.getContextUrls().getBackEndUrl();
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(node);
        String loggedInSessionCookie = loginLogoutClient.login();
        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);

        log.info("Adding stream definitions");
        defineStreams();
        log.info("Adding input event adaptors");
        //addInputEventAdaptors();
        log.info("Adding output event adaptors");
        addOutputEventAdaptor();
        log.info("Adding event builder-analytics_Statistics_1.3.0_builder");
        addEventBuilder("analytics_Statistics_1.3.0_builder.xml");
        log.info("Adding event builder-stock_quote_1.3.0_builder");
        addEventBuilder("stock_quote_1.3.0_builder.xml");
        log.info("Adding event formatter-fortuneCompanyStreamFormatter");
        addEventFormatter("fortuneCompanyStreamFormatter.xml");
        log.info("Adding execution plan");
        addExecutionPlan("preprocessStats.xml");
    }

    private void addExecutionPlan(String config) throws Exception {
        int initialExecutionPlanCount = eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount();
        String executionPlanConfigPath = getTestArtifactLocation() + "/artifacts/CEP/epconfigs/"+config;
        String executionPlanConfig = getArtifactConfigurationFromClasspath(executionPlanConfigPath);
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanConfig);
        Thread.sleep(3000);
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), initialExecutionPlanCount + 1);
    }

    private void addEventFormatter(String config) throws Exception {
        int initialEventFormatterCount = eventFormatterAdminServiceClient.getActiveEventFormatterCount();
        String eventFormatterConfigPath = getTestArtifactLocation() + "/artifacts/CEP/efconfigs/"+config;
        String eventFormatterConfig = getArtifactConfigurationFromClasspath(eventFormatterConfigPath);
        eventFormatterAdminServiceClient.addEventFormatterConfiguration(eventFormatterConfig);
        Thread.sleep(3000);
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), initialEventFormatterCount + 1);
    }

    private void addEventBuilder(String config) throws Exception {
        int startEbCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        String eventBuilderConfigPath = getTestArtifactLocation() + "/artifacts/CEP/ebconfigs/"+config;
        String eventBuilderConfig = getArtifactConfigurationFromClasspath(eventBuilderConfigPath);
        eventBuilderAdminServiceClient.addEventBuilderConfiguration(eventBuilderConfig);
        Thread.sleep(3000);
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), startEbCount + 1);
    }

    private void addOutputEventAdaptor() throws RemoteException, XPathExpressionException {
        OutputEventAdaptorPropertyDto username = new OutputEventAdaptorPropertyDto();
        username.setKey("username");
        username.setValue("admin");
        OutputEventAdaptorPropertyDto password = new OutputEventAdaptorPropertyDto();
        password.setKey("password");
        password.setValue("admin");
        OutputEventAdaptorPropertyDto receiverUrl = new OutputEventAdaptorPropertyDto();
        receiverUrl.setKey("receiverURL");
        int receiverPort = Integer.parseInt(contextMap.get("cep003").getInstance().getPorts().get("thrift_publisher"));
        receiverUrl.setValue("tcp://localhost:"+receiverPort);
        OutputEventAdaptorPropertyDto authenticatorURL = new OutputEventAdaptorPropertyDto();
        authenticatorURL.setKey("authenticatorURL");
        authenticatorURL.setValue("ssl://localhost:"+receiverPort+100);

        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration("WSO2EventAdaptor", "wso2event", new OutputEventAdaptorPropertyDto[]{username, password, receiverUrl, authenticatorURL});
    }

    private void addInputEventAdaptors() throws RemoteException, InterruptedException {
        log.info("Adding input event adaptor");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount();
        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration("DefaultWSO2EventInputAdaptor", "wso2event", new InputEventAdaptorPropertyDto[0]);
        Thread.sleep(1000);
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 1 + startCount);
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

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos = new EventStreamAttributeDto[]{payloadEventStreamAttributeDto1, payloadEventStreamAttributeDto2};

        eventStreamDefinitionDto.setName("analytics_Statistics");
        eventStreamDefinitionDto.setVersion("1.3.0");
        eventStreamDefinitionDto.setMetaData(metaEventStreamAttributeDtos);
        eventStreamDefinitionDto.setCorrelationData(null);
        eventStreamDefinitionDto.setPayloadData(payloadEventStreamAttributeDtos);
        eventStreamDefinitionDto.setDescription("");
        eventStreamDefinitionDto.setNickName("");

        eventStreamManagerAdminServiceClient.addEventStream(eventStreamDefinitionDto);
        Thread.sleep(1000);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), streamStartCount + 1);

        log.info("Adding stock-quote stream definition");
        EventStreamAttributeDto payloadEventStreamAttributeDto21 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto21.setAttributeName("price");
        payloadEventStreamAttributeDto21.setAttributeType("int");

        EventStreamAttributeDto payloadEventStreamAttributeDto22 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto22.setAttributeName("symbol");
        payloadEventStreamAttributeDto22.setAttributeType("string");

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos2 = new EventStreamAttributeDto[]{payloadEventStreamAttributeDto21, payloadEventStreamAttributeDto22};

        eventStreamDefinitionDto = new EventStreamDefinitionDto();
        eventStreamDefinitionDto.setName("stock_quote");
        eventStreamDefinitionDto.setVersion("1.3.0");
        eventStreamDefinitionDto.setMetaData(null);
        eventStreamDefinitionDto.setCorrelationData(null);
        eventStreamDefinitionDto.setPayloadData(payloadEventStreamAttributeDtos2);
        eventStreamDefinitionDto.setDescription("");
        eventStreamDefinitionDto.setNickName("");
        eventStreamManagerAdminServiceClient.addEventStream(eventStreamDefinitionDto);
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
        eventStreamManagerAdminServiceClient.addEventStream(eventStreamDefinitionDto);
        Thread.sleep(1000);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), streamStartCount + 3);

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("fortuneCompanyStreamFormatter");
        outputEventAdaptorManagerAdminServiceClient.removeActiveOutputEventAdaptorConfiguration("WSO2EventAdaptor");
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("preprocessStats.xml");
        eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("analytics_Statistics_1.3.0_builder");
        eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("stock_quote_1.3.0_builder");
        eventStreamManagerAdminServiceClient.removeEventStream("analytics_Statistics","1.3.0");
        eventStreamManagerAdminServiceClient.removeEventStream("stock_quote","1.3.0");
        eventStreamManagerAdminServiceClient.removeEventStream("fortuneCompanyStream","1.0.0");
        //inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("localEventReceiver");
    }
}
