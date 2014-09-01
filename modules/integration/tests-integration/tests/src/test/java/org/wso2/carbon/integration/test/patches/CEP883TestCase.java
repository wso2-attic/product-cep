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
package org.wso2.carbon.integration.test.patches;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSTopicMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.stub.types.EventInputPropertyConfigurationDto;
import org.wso2.carbon.event.formatter.stub.types.PropertyDto;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.SiddhiConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.CEPIntegrationTest;
import org.wso2.carbon.integration.test.client.KeyStoreUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

public class CEP883TestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(CEP883TestCase.class);
    private final String ACTIVEMQ_CORE = "activemq-core-5.7.0.jar";
    private final String GERONIMO_J2EE_MANAGEMENT = "geronimo-j2ee-management_1.1_spec-1.0.1.jar";
    private final String JAR_LOCATION = "/artifacts/CEP/jar";
    private JMSBrokerController activeMqBroker = null;
    private ServerConfigurationManager serverManager = null;


    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception, RemoteException {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        try {
            serverManager = new ServerConfigurationManager(cepServer);
        } catch (MalformedURLException e) {
            throw new RemoteException("Malformed URL exception thrown when initializing JMS broker", e);
        }
        setupJmsBroker();
        //copying dependency jms jar files to component/lib
        try {
            serverManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + File.separator + ACTIVEMQ_CORE).toURI()));
            serverManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + File.separator + GERONIMO_J2EE_MANAGEMENT).toURI()));
            serverManager.restartGracefully();
        } catch (IOException e) {
            throw new RemoteException("IOException when initializing JMS broker", e);
        } catch (URISyntaxException e) {
            throw new RemoteException("URISyntaxException when initializing JMS broker", e);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when restarting server", e);
        }

        String loggedInSessionCookie = getSessionCookie();
        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);

    }


    private void setupJmsBroker() {
        //starting jms broker
        activeMqBroker = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
        if (!JMSBrokerController.isBrokerStarted()) {
            Assert.assertTrue(activeMqBroker.start(), "JMS Broker(ActiveMQ) starting failed");
        }
    }


    @Test(groups = {"wso2.cep"}, description = "Test the configuration file deployment order ITA, EB, EP, OTA, EF")
    public void addInputEventAdaptorTestScenario1()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount();
        //configurationUtil.addThriftInputEventAdaptor();

        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration("localEventReceiver", "wso2event", new InputEventAdaptorPropertyDto[0]);

        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdaptorTestScenario1"})
    public void addEventBuilderTestScenario1() throws RemoteException, InterruptedException {

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        //configurationUtil.addEventBuilder();

        EventBuilderConfigurationDto eventBuilderConfigurationDto = new EventBuilderConfigurationDto();
        eventBuilderConfigurationDto.setEventBuilderConfigName("wso2eventbuilder");
        eventBuilderConfigurationDto.setInputMappingType("wso2event");
        eventBuilderConfigurationDto.setToStreamName("summarizedStatistics");
        eventBuilderConfigurationDto.setToStreamVersion("1.0.0");
        eventBuilderConfigurationDto.setInputEventAdaptorName("localEventReceiver");
        eventBuilderConfigurationDto.setInputEventAdaptorType("wso2event");
        org.wso2.carbon.event.builder.stub.types.PropertyDto streamName = new org.wso2.carbon.event.builder.stub.types.PropertyDto();
        streamName.setKey("stream");
        streamName.setValue("analytics_Statistics");
        org.wso2.carbon.event.builder.stub.types.PropertyDto version = new org.wso2.carbon.event.builder.stub.types.PropertyDto();
        version.setKey("version");
        version.setValue("1.3.0");

        EventInputPropertyConfigurationDto metaProperty1 = new EventInputPropertyConfigurationDto();
        metaProperty1.setName("ipAddress");
        metaProperty1.setValueOf("meta_ipAddress");
        metaProperty1.setType("string");

        EventInputPropertyConfigurationDto payloadProperty1 = new EventInputPropertyConfigurationDto();
        payloadProperty1.setName("userID");
        payloadProperty1.setValueOf("user");
        payloadProperty1.setType("string");

        EventInputPropertyConfigurationDto payloadProperty2 = new EventInputPropertyConfigurationDto();
        payloadProperty2.setName("searchTerms");
        payloadProperty2.setValueOf("keywords");
        payloadProperty2.setType("string");

        org.wso2.carbon.event.builder.stub.types.PropertyDto[] propertyDtos = new org.wso2.carbon.event.builder.stub.types.PropertyDto[]{streamName, version};

        eventBuilderAdminServiceClient.addWso2EventBuilderConfiguration("wso2eventbuilder", "summarizedStatistics:1.0.0",
                                                                        "localEventReceiver", "wso2event", new EventInputPropertyConfigurationDto[]{metaProperty1}, new EventInputPropertyConfigurationDto[0],
                                                                        new EventInputPropertyConfigurationDto[]{payloadProperty1, payloadProperty2}, propertyDtos, true);


        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario1"})
    public void addEventProcessorTestScenario1() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount();
        //configurationUtil.addEventProcessor();

        ExecutionPlanConfigurationDto executionPlanConfigurationDto = new ExecutionPlanConfigurationDto();
        executionPlanConfigurationDto.setName("statsProcessor");
        StreamConfigurationDto inStream = new StreamConfigurationDto();
        inStream.setSiddhiStreamName("summarizedStatistics");
        inStream.setStreamId("summarizedStatistics:1.0.0");

        executionPlanConfigurationDto.setImportedStreams(new StreamConfigurationDto[]{inStream});

        SiddhiConfigurationDto siddhiPersistenceConfigDto = new SiddhiConfigurationDto();
        siddhiPersistenceConfigDto.setKey("siddhi.persistence.snapshot.time.interval.minutes");
        siddhiPersistenceConfigDto.setValue("0");
        executionPlanConfigurationDto.addSiddhiConfigurations(siddhiPersistenceConfigDto);

        executionPlanConfigurationDto.setQueryExpressions("from summarizedStatistics insert into statisticsStream; ");

        StreamConfigurationDto outStream = new StreamConfigurationDto();
        outStream.setSiddhiStreamName("statisticsStream");
        outStream.setStreamId("statisticsStream:1.0.0");
        executionPlanConfigurationDto.setExportedStreams(new StreamConfigurationDto[]{outStream});

        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanConfigurationDto);

        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventProcessorTestScenario1"})
    public void addOutputEventAdaptorTestScenario1()
            throws RemoteException, InterruptedException {
        int startCount = outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount();
        Assert.assertEquals(startCount, 1);

        log.info("=======================Adding a Output event adaptor ======================= ");
        //configurationUtil.addOutputEventAdaptor();

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty1 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty1.setKey("java.naming.provider.url");
        inputEventAdaptorProperty1.setValue("tcp://localhost:61616");

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty2 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty2.setKey("java.naming.factory.initial");
        inputEventAdaptorProperty2.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty3 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty3.setKey("transport.jms.ConnectionFactoryJNDIName");
        inputEventAdaptorProperty3.setValue("TopicConnectionFactory");

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty4 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty4.setKey("transport.jms.DestinationType");
        inputEventAdaptorProperty4.setValue("topic");


        OutputEventAdaptorPropertyDto[] outputEventAdaptorPropertyDtos = new OutputEventAdaptorPropertyDto[]{inputEventAdaptorProperty1, inputEventAdaptorProperty2, inputEventAdaptorProperty3, inputEventAdaptorProperty4};

        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration("jmsSender", "jms", outputEventAdaptorPropertyDtos);

        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdaptorTestScenario1"})
    public void addEventFormatterTestScenario1() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getActiveEventFormatterCount();
        Assert.assertEquals(startCount, 0);
        log.info("=======================Adding a event formatter ======================= ");
        //configurationUtil.addEventFormatter();

        PropertyDto topic = new PropertyDto();
        topic.setKey("transport.jms.Destination");
        topic.setValue("analyticStats");

        PropertyDto eventFormatterPropertyDtos[] = new PropertyDto[]{topic};

        String jsonEvent = "{\"meta_ipAddress\" : {{meta_ipAddress}} ,  \n" +
                           "\"user\" : {{user}} ,\n" +
                           "\"keywords\" : {{keywords}} }";

        eventFormatterAdminServiceClient.addJSONEventFormatterConfiguration("AnalyticsFormatter", "statisticsStream:1.0.0", "jmsSender", "jms", jsonEvent, eventFormatterPropertyDtos, "inline", true);

        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventFormatterTestScenario1"})
    public void JMSJSONSenderTest() throws Exception {


        JMSTopicMessageConsumer consumer = new JMSTopicMessageConsumer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.subscribe("analyticStats");
            publishEvents();
            Thread.sleep(5000);

            for (int i = 0; i < 30; i++) {
                if (consumer.getMessages().size() == 1) {
                    break;
                }
                Thread.sleep(1000);
            }
        } finally {
            consumer.stopConsuming();
        }
        log.info("=======================Check json message count======================= ");
        Assert.assertEquals(consumer.getMessages().size(), 1, "Message count mismatched in Topic." +
                                                              " JSON message not received");

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("AnalyticsFormatter");
        outputEventAdaptorManagerAdminServiceClient.removeActiveOutputEventAdaptorConfiguration("jmsSender");
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("statsProcessor");
        eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("wso2eventbuilder");
        inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("localEventReceiver");
    }

    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }


    private void publishEvents()
            throws DataBridgeException, AgentException, MalformedURLException,
                   AuthenticationException, TransportException, MalformedStreamDefinitionException,
                   StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException {

        KeyStoreUtil.setTrustStoreParams();

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same

        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7611", "admin", "admin");

        String streamId;
        try {
            streamId = dataPublisher.findStream("analytics_Statistics", "1.3.0");
        } catch (NoStreamDefinitionExistException e) {
            streamId = dataPublisher.defineStream("{" +
                                                  "  'name':'analytics_Statistics'," +
                                                  "  'version':'1.3.0'," +
                                                  "  'nickName': 'Analytics Statistics Information'," +
                                                  "  'description': 'Details of Analytics Statistics'," +
                                                  "  'metaData':[" +
                                                  "          {'name':'ipAddress','type':'STRING'}" +
                                                  "  ]," +
                                                  "  'payloadData':[" +
                                                  "          {'name':'userID','type':'STRING'}," +
                                                  "          {'name':'searchTerms','type':'STRING'}" +
                                                  "  ]" +
                                                  "}");

        }
        Thread.sleep(1000);
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"192.168.1.1"}, null, new Object[]{"abc@org1.com", null});

        Thread.sleep(3000);
        dataPublisher.stop();
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            Thread.sleep(5000);
            if (activeMqBroker != null) {
                activeMqBroker.stop();
            }
            Thread.sleep(5000); //let server to clear the artifact undeployment
        } finally {
            //reverting the changes done to cep sever
            if (serverManager != null) {
                serverManager.removeFromComponentLib(ACTIVEMQ_CORE);
                serverManager.removeFromComponentLib(GERONIMO_J2EE_MANAGEMENT);
                serverManager.restoreToLastConfiguration();
            }

        }
        super.cleanup();
    }

}
