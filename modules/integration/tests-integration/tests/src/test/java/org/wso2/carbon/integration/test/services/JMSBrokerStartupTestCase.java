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
package org.wso2.carbon.integration.test.services;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSTopicMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSTopicMessagePublisher;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.io.File;

public class JMSBrokerStartupTestCase {
    private final String ACTIVEMQ_CORE = "activemq-core-5.7.0.jar";
    private final String GERONIMO_J2EE_MANAGEMENT = "geronimo-j2ee-management_1.1_spec-1.0.1.jar";
    private JMSBrokerController activeMqBroker;
    private ServerConfigurationManager serverManager = null;

    @BeforeTest(alwaysRun = true)
    public void startJMSBrokerAndConfigureCEP() throws Exception {

        AutomationContext cepServer = new AutomationContext("CEP", TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(cepServer);


            //starting jms broker
            activeMqBroker = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
            if (!JMSBrokerController.isBrokerStarted()) {
                Assert.assertTrue(activeMqBroker.start(), "JMS Broker(ActiveMQ) starting failed");
            }

            //copying dependency jms jar files to component/lib
            serverManager.copyToComponentLib(new File(FrameworkPathUtil.getSystemResourceLocation() +
                                                      "artifacts" + File.separator + "CEP" +
                                                      File.separator + "jar" + File.separator + ACTIVEMQ_CORE));

            serverManager.copyToComponentLib(new File(FrameworkPathUtil.getSystemResourceLocation() +
                                                      "artifacts" + File.separator + "CEP" +
                                                      File.separator + "jar" + File.separator + GERONIMO_J2EE_MANAGEMENT));

            //enabling jms transport with ActiveMQ
//            serverManager.applyConfiguration(new File(ProductConstant.getResourceLocations(ProductConstant.CEP_SERVER_NAME)
//                                                      + File.separator + "jms" + File.separator + "transport"
//                                                      + File.separator + "axis2config" + File.separator
//                                                      + "activemq" + File.separator + "axis2.xml"));


            }

    @AfterTest(alwaysRun = true)
    public void stopJMSBrokerRevertCEPConfiguration() throws Exception {

            try {
                //reverting the changes done to cep sever
                if (serverManager != null) {
                    serverManager.removeFromComponentLib(ACTIVEMQ_CORE);
                    serverManager.removeFromComponentLib(GERONIMO_J2EE_MANAGEMENT);
                    serverManager.restoreToLastConfiguration();
                }

            } finally {
                if (activeMqBroker != null) {
                    Assert.assertTrue(activeMqBroker.stop(), "JMS Broker(ActiveMQ) Stopping failed");
                    Thread.sleep(3000);
                }
            }
    }

    @Test(groups = {"wso2.cep"}, description = "Test JMS broker queue clients with popMessage(java.lang.Class<T> clzz)")
    public void JMSBrokerQueueTest1() throws Exception {
        int numberOfMsgToExpect = 10;
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String queueName = "JmsBrokerTestQueue1";
        String message = "<?xml version='1.0' encoding='UTF-8'?>" +
                         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                         " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
                         "  <soapenv:Header/>" +
                         "  <soapenv:Body>" +
                         "   <ser:placeOrder>" +
                         "     <ser:order>" +
                         "      <xsd:price>100</xsd:price>" +
                         "      <xsd:quantity>2000</xsd:quantity>" +
                         "      <xsd:symbol>JMSTransport</xsd:symbol>" +
                         "     </ser:order>" +
                         "   </ser:placeOrder>" +
                         "  </soapenv:Body>" +
                         "</soapenv:Envelope>";
        try {
            sender.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                if (consumer.popMessage(javax.jms.Message.class) == null) {
                    Assert.fail("Unable to pop the expected number of message in the queue" + queueName);
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Test JMS broker queue clients with popMessage()")
    public void JMSBrokerQueueTest2() throws Exception {
        int numberOfMsgToExpect = 10;
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String queueName = "JmsBrokerTestQueue2";
        String message = "<?xml version='1.0' encoding='UTF-8'?>" +
                         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                         " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
                         "  <soapenv:Header/>" +
                         "  <soapenv:Body>" +
                         "   <ser:placeOrder>" +
                         "     <ser:order>" +
                         "      <xsd:price>100</xsd:price>" +
                         "      <xsd:quantity>2000</xsd:quantity>" +
                         "      <xsd:symbol>JMSTransport</xsd:symbol>" +
                         "     </ser:order>" +
                         "   </ser:placeOrder>" +
                         "  </soapenv:Body>" +
                         "</soapenv:Envelope>";
        try {
            sender.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                if (consumer.popMessage() == null) {
                    Assert.fail("Unable to pop the expected number of message in the queue" + queueName);
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Test JMS broker queue clients with popRawMessage()")
    public void JMSBrokerQueueTest3() throws Exception {
        int numberOfMsgToExpect = 10;
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String queueName = "JmsBrokerTestQueue3";
        String message = "<?xml version='1.0' encoding='UTF-8'?>" +
                         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                         " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
                         "  <soapenv:Header/>" +
                         "  <soapenv:Body>" +
                         "   <ser:placeOrder>" +
                         "     <ser:order>" +
                         "      <xsd:price>100</xsd:price>" +
                         "      <xsd:quantity>2000</xsd:quantity>" +
                         "      <xsd:symbol>JMSTransport</xsd:symbol>" +
                         "     </ser:order>" +
                         "   </ser:placeOrder>" +
                         "  </soapenv:Body>" +
                         "</soapenv:Envelope>";
        try {
            sender.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                if (consumer.popRawMessage() == null) {
                    Assert.fail("Unable to pop the expected number of message in the queue" + queueName);
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Test JMS broker topic clients")
    public void JMSBrokerTopicTest() throws Exception {
        int numberOfMsgToExpect = 10;
        JMSTopicMessagePublisher sender = new JMSTopicMessagePublisher(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        JMSTopicMessageConsumer consumer = new JMSTopicMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String topicName = "JmsBrokerTestTopic";
        String message = "<?xml version='1.0' encoding='UTF-8'?>" +
                         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                         " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
                         "  <soapenv:Header/>" +
                         "  <soapenv:Body>" +
                         "   <ser:placeOrder>" +
                         "     <ser:order>" +
                         "      <xsd:price>100</xsd:price>" +
                         "      <xsd:quantity>2000</xsd:quantity>" +
                         "      <xsd:symbol>JMSTransport</xsd:symbol>" +
                         "     </ser:order>" +
                         "   </ser:placeOrder>" +
                         "  </soapenv:Body>" +
                         "</soapenv:Envelope>";

        try {
            // First subscribe
            consumer.subscribe(topicName);
            //Now send
            sender.connect(topicName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                sender.publish(message);
            }
            Thread.sleep(1000);
            int rcvCount = 0;
            for (String messageContent : consumer.getMessages()) {
                Assert.assertNotNull(messageContent, "Received message from topic " + topicName + " with content: " + messageContent);
                rcvCount++;
            }
            Assert.assertEquals(rcvCount, numberOfMsgToExpect, "Unable to get the expected number of messages from the topic " + topicName);
        } finally {
            sender.disconnect();
            consumer.stopConsuming();
        }
    }

    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }
}

