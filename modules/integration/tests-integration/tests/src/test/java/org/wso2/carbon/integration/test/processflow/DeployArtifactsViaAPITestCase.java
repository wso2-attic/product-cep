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
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.receiver.stub.types.BasicInputAdapterPropertyDto;
import org.wso2.carbon.event.receiver.stub.types.EventMappingPropertyDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

/**
 * Deploying artifacts through API. They are deployed in typical order.
 */
public class DeployArtifactsViaAPITestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(DeployArtifactsViaAPITestCase.class);
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

        log.info("=======================Adding a stream definition====================");
        EventStreamDefinitionDto pizzaOrderStreamDefinitionDto = new EventStreamDefinitionDto();

        EventStreamAttributeDto orderNo = new EventStreamAttributeDto();
        orderNo.setAttributeName("OrderNo");
        orderNo.setAttributeType("int");
        EventStreamAttributeDto type = new EventStreamAttributeDto();
        type.setAttributeName("Type");
        type.setAttributeType("string");
        EventStreamAttributeDto size = new EventStreamAttributeDto();
        size.setAttributeName("Size");
        size.setAttributeType("string");
        EventStreamAttributeDto quantity = new EventStreamAttributeDto();
        quantity.setAttributeName("Quantity");
        quantity.setAttributeType("int");
        EventStreamAttributeDto contact = new EventStreamAttributeDto();
        contact.setAttributeName("Contact");
        contact.setAttributeType("string");
        EventStreamAttributeDto address = new EventStreamAttributeDto();
        address.setAttributeName("Address");
        address.setAttributeType("string");
        EventStreamAttributeDto[] metaData = null;
        EventStreamAttributeDto[] correlationData = new EventStreamAttributeDto[]{orderNo};
        EventStreamAttributeDto[] payloadData = new EventStreamAttributeDto[]{type, size, quantity, contact, address};

        pizzaOrderStreamDefinitionDto.setName("org.wso2.sample.pizza.order");
        pizzaOrderStreamDefinitionDto.setVersion("1.0.0");
        pizzaOrderStreamDefinitionDto.setMetaData(metaData);
        pizzaOrderStreamDefinitionDto.setCorrelationData(correlationData);
        pizzaOrderStreamDefinitionDto.setPayloadData(payloadData);
        pizzaOrderStreamDefinitionDto.setDescription("This is a  test stream");
        pizzaOrderStreamDefinitionDto.setNickName("pizzaOrder");

        eventStreamManagerAdminServiceClient.addEventStreamAsDTO(pizzaOrderStreamDefinitionDto);

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        log.info("=======================Adding another stream definition====================");

        EventStreamDefinitionDto outStreamDefinitionDto = new EventStreamDefinitionDto();

        outStreamDefinitionDto.setName("outStream");
        outStreamDefinitionDto.setVersion("1.0.0");
        outStreamDefinitionDto.setMetaData(metaData);
        outStreamDefinitionDto.setCorrelationData(correlationData);
        outStreamDefinitionDto.setPayloadData(payloadData);
        outStreamDefinitionDto.setDescription("This is a test stream");
        outStreamDefinitionDto.setNickName("");

        eventStreamManagerAdminServiceClient.addEventStreamAsDTO(outStreamDefinitionDto);

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

        log.info("=======================Adding an event receiver ======================= ");
        EventMappingPropertyDto namespace = new EventMappingPropertyDto();
        namespace.setName("mypizza");
        namespace.setValueOf("http://samples.wso2.org/");
        EventMappingPropertyDto[] namespaces = new EventMappingPropertyDto[]{namespace};

        EventMappingPropertyDto mapping0 = new EventMappingPropertyDto();
        mapping0.setName("//mypizza:PizzaOrder/mypizza:OrderNo");
        mapping0.setValueOf("correlation_OrderNo");
        mapping0.setType("int");
        EventMappingPropertyDto mapping1 = new EventMappingPropertyDto();
        mapping1.setName("//mypizza:PizzaOrder/mypizza:Type");
        mapping1.setValueOf("Type");
        mapping1.setType("string");
        EventMappingPropertyDto mapping2 = new EventMappingPropertyDto();
        mapping2.setName("//mypizza:PizzaOrder/mypizza:Size");
        mapping2.setValueOf("Size");
        mapping2.setType("string");
        EventMappingPropertyDto mapping3 = new EventMappingPropertyDto();
        mapping3.setName("//mypizza:PizzaOrder/mypizza:Quantity");
        mapping3.setValueOf("Quantity");
        mapping3.setType("int");
        mapping3.setDefaultValue("1");
        EventMappingPropertyDto mapping4 = new EventMappingPropertyDto();
        mapping4.setName("//mypizza:PizzaOrder/mypizza:Contact");
        mapping4.setValueOf("Contact");
        mapping4.setType("string");
        EventMappingPropertyDto mapping5 = new EventMappingPropertyDto();
        mapping5.setName("//mypizza:PizzaOrder/mypizza:Address");
        mapping5.setValueOf("Address");
        mapping5.setType("string");
        EventMappingPropertyDto[] mappings = new EventMappingPropertyDto[]{mapping0, mapping1, mapping2, mapping3, mapping4, mapping5};

        BasicInputAdapterPropertyDto propertyDTO1 = new BasicInputAdapterPropertyDto();
        propertyDTO1.setKey("transports");
        propertyDTO1.setValue("all");
        BasicInputAdapterPropertyDto[] propertyDTOArray = new BasicInputAdapterPropertyDto[]{propertyDTO1};

        eventReceiverAdminServiceClient.addXmlEventReceiverConfiguration(
                "PizzaOrder", "org.wso2.sample.pizza.order:1.0.0", "http", "", namespaces, mappings, propertyDTOArray, true);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), ++eventReceiverCount);


        log.info("=======================Adding an execution plan ======================= ");
        //The only way too add an execution plan is by sending the execution plan as a string (not using a DTO).

        String executionPlan = getExecutionPlanFromFile("DeployArtifactsTestCase", "testPlan.siddhiql");
        eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);

        Thread.sleep(1000);
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), ++executionPlanCount);

        log.info("=======================Adding an event publisher ======================= ");
        String textData =
                "            <pizzadata:PizzaOrderDataEvent xmlns:pizzadata=\"http://samples.wso2.org/\">\n" +
                "                <pizzadata:Name>{{Contact}}</pizzadata:Name>\n" +
                "                <pizzadata:Type>{{Type}}</pizzadata:Type>\n" +
                "                <pizzadata:Size>{{Size}}</pizzadata:Size>\n" +
                "                <pizzadata:Quantity>{{Quantity}}</pizzadata:Quantity>\n" +
                "                <pizzadata:Address>{{Address}}</pizzadata:Address>\n" +
                "            </pizzadata:PizzaOrderDataEvent>";

        BasicOutputAdapterPropertyDto url = new BasicOutputAdapterPropertyDto();
        url.setKey("http.url");
        url.setValue("http://localhost:" + CEPIntegrationTestConstants.HTTP_PORT + "/GenericLogService/log");
        url.set_static(false);
        BasicOutputAdapterPropertyDto username = new BasicOutputAdapterPropertyDto();
        username.setKey("http.username");
        username.setValue("");
        username.set_static(false);
        BasicOutputAdapterPropertyDto password = new BasicOutputAdapterPropertyDto();
        password.setKey("http.password");
        password.setValue("");
        password.set_static(false);
        BasicOutputAdapterPropertyDto headers = new BasicOutputAdapterPropertyDto();
        headers.setKey("http.headers");
        headers.setValue("");
        headers.set_static(false);
        BasicOutputAdapterPropertyDto proxyHost = new BasicOutputAdapterPropertyDto();
        proxyHost.setKey("http.proxy.host");
        proxyHost.setValue("");
        proxyHost.set_static(false);
        BasicOutputAdapterPropertyDto proxyPort = new BasicOutputAdapterPropertyDto();
        proxyPort.setKey("http.proxy.port");
        proxyPort.setValue("");
        proxyPort.set_static(false);
        BasicOutputAdapterPropertyDto clientMethod = new BasicOutputAdapterPropertyDto();
        clientMethod.setKey("http.proxy.port");
        clientMethod.setValue("");
        clientMethod.set_static(true);

        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration =
                new BasicOutputAdapterPropertyDto[]{url, username, password, headers, proxyHost, proxyPort, clientMethod};

        eventPublisherAdminServiceClient.addXMLEventPublisherConfiguration("PizzaDeliveryNotification", "outStream:1.0.0", "http",
                textData, outputPropertyConfiguration, "inline", true);

        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), ++eventPublisherCount);
    }

    @Test(groups = {"wso2.cep"}, description = "Removing artifacts.", dependsOnMethods = {"addArtifactsTestScenario"})
    public void removeArtifactsTestScenario() throws Exception {

        eventReceiverAdminServiceClient.removeActiveEventReceiverConfiguration("PizzaOrder");
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), eventReceiverCount - 1);

        eventPublisherAdminServiceClient.removeActiveEventPublisherConfiguration("PizzaDeliveryNotification");
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), eventPublisherCount - 1);

        eventProcessorAdminServiceClient.removeActiveExecutionPlan("testPlan");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), executionPlanCount - 1);

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.sample.pizza.order", "1.0.0");
        eventStreamManagerAdminServiceClient.removeEventStream("outStream", "1.0.0");
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount - 2);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
