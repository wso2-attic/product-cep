package org.wso2.carbon.integration.test.storm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;

import java.io.File;


public class StormManagerServiceTestCase extends CEPIntegrationTest {
    private static final String EVENT_PROCESSING_FILE = "event-processor.xml";
    private static final String AXIS2_XML_FILE = "axis2.xml";
    private static final String RESOURCE_LOCATION = TestConfigurationProvider.getResourceLocation()  + File.separator + "artifacts" + File.separator + "CEP"
            + File.separator + "StormTestCase"+ File.separator + "ManagerServiceTestCase";
    private static final String CARBON_HOME = FrameworkPathUtil.getCarbonHome();

    private static Log log = LogFactory.getLog(StormManagerServiceTestCase.class);
    private ServerConfigurationManager serverConfigManager;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        log.info("Initializing CEP server to act as storm manager");

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverConfigManager = new ServerConfigurationManager(cepServer);

        log.info("Replacing " + EVENT_PROCESSING_FILE);
        String eventProcessingFileLocation = RESOURCE_LOCATION + File.separator + EVENT_PROCESSING_FILE;
        String cepEventProcessorFileLocation = CARBON_HOME + File.separator + "repository" + File.separator
                + "conf" + File.separator + EVENT_PROCESSING_FILE;
        serverConfigManager.applyConfigurationWithoutRestart(new File(eventProcessingFileLocation), new File(cepEventProcessorFileLocation), true);

        log.info("Replacing " + AXIS2_XML_FILE);
        String axis2FileLocation = RESOURCE_LOCATION + File.separator + AXIS2_XML_FILE;
        String cepAxis2FileLocation = CARBON_HOME + File.separator + "repository" + File.separator + "conf"
                + File.separator + "axis2" + File.separator + AXIS2_XML_FILE;
        serverConfigManager.applyConfigurationWithoutRestart(new File(axis2FileLocation), new File(cepAxis2FileLocation), true);

        log.info("Restarting CEP server");
        serverConfigManager.restartGracefully();
        // Waiting for the server to restart
        Thread.sleep(5000);

        log.info("Initialization completed");
    }

    private StormManagerService.Client createManagerServiceClient() throws TTransportException {
        TTransport transport = new TSocket("localhost", 8904);
        TProtocol protocol = new TBinaryProtocol(transport);
        transport.open();
        StormManagerService.Client client = new StormManagerService.Client(protocol);
        return client;

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        serverConfigManager.restoreToLastConfiguration();
        super.cleanup();
    }



    @Test(groups = {"wso2.cep"}, description = "Single Storm Publisher and CEP publisher for a single execution plan on same host")
    public void singleReceiverAndPublisherInSameHost() throws Exception {
        final String ExecutionPlan = "Test1ExecutionPlan";
        StormManagerService.Client client = createManagerServiceClient();

        /*CEP Publishers*/
        client.registerCEPPublisher(1234, ExecutionPlan, "127.0.0.1", 15000);
        String publisherIpPort = client.getCEPPublisher(1234,  ExecutionPlan, "127.0.0.1");

        String[]  publisherIpAndPort = publisherIpPort.split(":");
        Assert.assertEquals(publisherIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(publisherIpAndPort[1], "15000");

        /*Storm Receivers*/
        client.registerStormReceiver(1234, ExecutionPlan, "127.0.0.1", 16000);
        String stormReceiverIpPort = client.getStormReceiver(1234, ExecutionPlan, "127.0.0.1");

        String[] stormReceiverIpAndPort = stormReceiverIpPort.split(":");
        Assert.assertEquals(stormReceiverIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(stormReceiverIpAndPort[1], "16000");
    }

    @Test(groups = {"wso2.cep"}, description = "Single Receiver and Publisher for a single execution plan on different hosts")
    public void singleReceiverAndPublisherInDifferentHosts() throws Exception {
        /*
            Two CEP publishers for the same execution plan of same tenant but in different hosts. Client must get the CEP publisher
            in the same host as Storm Publisher.
         */
        final String ExecutionPlan = "Test2ExecutionPlan";
        StormManagerService.Client client = createManagerServiceClient();

        /*CEP publishers*/
        client.registerCEPPublisher(1234, ExecutionPlan, "127.0.0.1", 15000);
        client.registerCEPPublisher(1234, ExecutionPlan, "127.10.0.1", 15001);

        String publisherIpPort = client.getCEPPublisher(1234,  ExecutionPlan, "127.0.0.1");
        String[] publisherIpAndPort = publisherIpPort.split(":");
        Assert.assertEquals(publisherIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(publisherIpAndPort[1], "15000");

        String publisherIpPort2 = client.getCEPPublisher(1234, ExecutionPlan, "127.10.0.1");
        String[] publisherIpAndPort2 = publisherIpPort2.split(":");
        Assert.assertEquals(publisherIpAndPort2[0], "127.10.0.1");
        Assert.assertEquals(publisherIpAndPort2[1], "15001");

        /*
            Two Storm receivers for the same execution plan of same tenant but in different hosts. Client must get the storm receiver
            in the same host as Storm Publisher.
        */
        /*Storm Receivers*/
        client.registerStormReceiver(1234, ExecutionPlan, "127.0.0.1", 16000);
        client.registerStormReceiver(1234, ExecutionPlan, "127.10.0.1", 16001);

        String stormReceiverIpPort = client.getStormReceiver(1234,  ExecutionPlan, "127.0.0.1");
        String[] stormReceiverIpAndPort = stormReceiverIpPort.split(":");
        Assert.assertEquals(stormReceiverIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(stormReceiverIpAndPort[1], "16000");

        String stormReceiverIpPort2 = client.getStormReceiver(1234, ExecutionPlan, "127.10.0.1");
        String[] stormReceiverIpAndPort2 = stormReceiverIpPort2.split(":");
        Assert.assertEquals(stormReceiverIpAndPort2[0], "127.10.0.1");
        Assert.assertEquals(stormReceiverIpAndPort2[1], "16001");
    }

    @Test(groups = {"wso2.cep"}, description = "Multiple storm receivers and multiple CEP publishers for a single execution plan on same host")
    public void twoReceiversAndTwoPublishersInSameHost() throws Exception {
        /*
            Two CEP publishers in same same host listening on different ports. Two consequent requests must not return the same CEP Publisher
            twice so that load is balanced among the two publishers
         */
        final String ExecutionPlan = "Test3ExecutionPlan";
        StormManagerService.Client client = createManagerServiceClient();

        client.registerCEPPublisher(4321, ExecutionPlan, "127.0.0.1", 25000);
        client.registerCEPPublisher(4321, ExecutionPlan, "127.0.0.1", 25001);

        String publisherIpPort1 = client.getCEPPublisher(4321, ExecutionPlan, "127.0.0.1");
        String[] ipAndPort1 = publisherIpPort1.split(":");

        String publisherIpPort2 = client.getCEPPublisher(4321, ExecutionPlan, "127.0.0.1");
        String[] ipAndPort2 = publisherIpPort2.split(":");

        Assert.assertNotEquals(ipAndPort1[1], ipAndPort2[1]);

         /*
            Two Storm Receivers in same same host listening on different ports. Two consequent requests must not return the same storm receiver
            twice so that load is balanced among the two publishers
         */
        client.registerStormReceiver(1234, ExecutionPlan, "127.0.0.1", 26000);
        client.registerStormReceiver(1234, ExecutionPlan, "127.0.0.1", 26001);

        String stormReceiverIpPort = client.getStormReceiver(1234,  ExecutionPlan, "127.0.0.1");
        String[] stormReceiverIpAndPort = stormReceiverIpPort.split(":");

        String stormReceiverIpPort2 = client.getStormReceiver(1234, ExecutionPlan, "127.0.0.1");
        String[] stormReceiverIpAndPort2 = stormReceiverIpPort2.split(":");

        Assert.assertNotEquals(stormReceiverIpAndPort[1], stormReceiverIpAndPort2[1]);

    }

    @Test(groups = {"wso2.cep"}, description = "Single Receiver and Publisher for a Two execution plans on different hosts")
    public void singleReceiverAndPublisherForDifferentExecutionPlans() throws Exception {
        /*
            Two CEP publishers for the different execution plans of same tenant but in different hosts. Client must get the CEP publisher
            for the relevant execution plan.
         */
        final String ExecutionPlan1 = "Test4ExecutionPlan1";
        final String ExecutionPlan2 = "Test4ExecutionPlan2";
        StormManagerService.Client client = createManagerServiceClient();

        /*Storm publishers*/
        client.registerCEPPublisher(1234, ExecutionPlan1, "127.0.0.1", 15000);
        client.registerCEPPublisher(1234, ExecutionPlan2, "127.0.0.1", 15001);

        String publisherIpPort = client.getCEPPublisher(1234,  ExecutionPlan1, "127.0.0.1");
        String[] publisherIpAndPort = publisherIpPort.split(":");
        Assert.assertEquals(publisherIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(publisherIpAndPort[1], "15000");

        String publisherIpPort2 = client.getCEPPublisher(1234, ExecutionPlan2, "127.0.0.1");
        String[] publisherIpAndPort2 = publisherIpPort2.split(":");
        Assert.assertEquals(publisherIpAndPort2[0], "127.0.0.1");
        Assert.assertEquals(publisherIpAndPort2[1], "15001");

        /*
            Two Storm receivers for the different execution plan of same tenant but in different hosts. Client must get the storm receiver
            for the relevant execution plan.
        */
        /*Storm Receivers*/
        client.registerStormReceiver(1234, ExecutionPlan1, "127.0.0.1", 16000);
        client.registerStormReceiver(1234, ExecutionPlan2, "127.0.0.1", 16001);

        String stormReceiverIpPort = client.getStormReceiver(1234,  ExecutionPlan1, "127.0.0.1");
        String[] stormReceiverIpAndPort = stormReceiverIpPort.split(":");
        Assert.assertEquals(stormReceiverIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(stormReceiverIpAndPort[1], "16000");

        String stormReceiverIpPort2 = client.getStormReceiver(1234, ExecutionPlan2, "127.0.0.1");
        String[] stormReceiverIpAndPort2 = stormReceiverIpPort2.split(":");
        Assert.assertEquals(stormReceiverIpAndPort2[0], "127.0.0.1");
        Assert.assertEquals(stormReceiverIpAndPort2[1], "16001");
    }

    @Test(groups = {"wso2.cep"}, description = "Single Receiver and Publisher for a Two execution plans on different hosts")
    public void singleReceiverAndPublisherForDifferentTenants() throws Exception {
        final String ExecutionPlan = "Test5ExecutionPlan";
        StormManagerService.Client client = createManagerServiceClient();
        /*
            Two CEP publishers for the same execution plans of different tenant but in different hosts. Client must get the CEP publisher
            for the relevant tenant.
         */
        /*Storm publishers*/
        client.registerCEPPublisher(1234, ExecutionPlan, "127.0.0.1", 15000);
        client.registerCEPPublisher(-1234, ExecutionPlan, "127.0.0.1", 15001);

        String publisherIpPort = client.getCEPPublisher(1234,  ExecutionPlan, "127.0.0.1");
        String[] publisherIpAndPort = publisherIpPort.split(":");
        Assert.assertEquals(publisherIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(publisherIpAndPort[1], "15000");

        String publisherIpPort2 = client.getCEPPublisher(-1234, ExecutionPlan, "127.0.0.1");
        String[] publisherIpAndPort2 = publisherIpPort2.split(":");
        Assert.assertEquals(publisherIpAndPort2[0], "127.0.0.1");
        Assert.assertEquals(publisherIpAndPort2[1], "15001");

        /*
            Two Storm receivers for the same execution plan of different tenant but in different hosts. Client must get the storm receiver
            for the relevant tenant.
        */
        /*Storm Receivers*/
        client.registerStormReceiver(1234, ExecutionPlan, "127.0.0.1", 36000);
        client.registerStormReceiver(-1234, ExecutionPlan, "127.0.0.1", 36001);

        String stormReceiverIpPort = client.getStormReceiver(1234,  ExecutionPlan, "127.0.0.1");
        String[] stormReceiverIpAndPort = stormReceiverIpPort.split(":");
        Assert.assertEquals(stormReceiverIpAndPort[0], "127.0.0.1");
        Assert.assertEquals(stormReceiverIpAndPort[1], "36000");

        String stormReceiverIpPort2 = client.getStormReceiver(-1234, ExecutionPlan, "127.0.0.1");
        String[] stormReceiverIpAndPort2 = stormReceiverIpPort2.split(":");
        Assert.assertEquals(stormReceiverIpAndPort2[0], "127.0.0.1");
        Assert.assertEquals(stormReceiverIpAndPort2[1], "36001");
    }

}
