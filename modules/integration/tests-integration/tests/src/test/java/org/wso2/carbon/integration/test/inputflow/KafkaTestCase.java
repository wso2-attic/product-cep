/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.test.inputflow;

import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.test.TestingServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.KafkaEventPublisherClient;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Test sending different types of events to the Kafka receiver
 */
public class KafkaTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(KafkaTestCase.class);
    private final String[] LIBS_JARS = new String[]{
            "kafka-clients-0.8.2.1.jar", "kafka_2.10-0.8.2.1.jar",
            "metrics-core-2.2.0.jar", "scala-library-2.10.4.jar",
            "zkclient-0.3.jar", "zookeeper-3.4.6.jar"
    };
    private ServerConfigurationManager serverConfigManager = null;
    private String cookie = null;
    private TestingServer zkTestServer;
    private KafkaServerStartable kafkaServer;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        try {
            setupKafkaBroker();
            serverConfigManager = new ServerConfigurationManager(cepServer);
            copyJarsToComponentLib();
        } catch (MalformedURLException e) {
            throw new RemoteException("Malformed URL exception thrown when initializing Kafka broker", e);
        } catch (IOException e) {
            throw new RemoteException("IOException when initializing Kafka broker", e);
        } catch (URISyntaxException e) {
            throw new RemoteException("URISyntaxException when initializing Kafka broker", e);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when restarting server", e);
        }
        cookie = getSessionCookie();
        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(backendURL, cookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL, cookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(backendURL, cookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing Kafka receiver with invalid events")
    public void KafkaInvalidEventTestScenario() throws Exception {
        final int messageCount = 6;
        String samplePath = "inputflows" + File.separator + "sample0018";
        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startERCount = eventReceiverAdminServiceClient.getActiveEventReceiverCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        // Add StreamDefinition.
        String streamDefinition = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinition);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++startESCount);

        // Add Kafka JSON EventReceiver without mapping.
        String eventReceiver = getXMLArtifactConfiguration(samplePath, "kafkaReceiver.xml");
        eventReceiverAdminServiceClient.addEventReceiverConfiguration(eventReceiver);
        Assert.assertEquals(eventReceiverAdminServiceClient.getActiveEventReceiverCount(), ++startERCount);

        // Add Wso2event EventPublisher.
        String eventPublisher = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisher);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), ++startEPCount);

        // The data-bridge receiver
        Wso2EventServer agentServer = new Wso2EventServer(samplePath, CEPIntegrationTestConstants.TCP_PORT, true);
        Thread agentServerThread = new Thread(agentServer);
        agentServerThread.start();

        // Wait till the server to completely start (until Starting polling event receivers & Kafka Connection).
        Thread.sleep(60000);

        try {
            KafkaEventPublisherClient.publish("localhost:9092", "sensordata", samplePath, "validEvents001.txt");
            KafkaEventPublisherClient.publish("localhost:9092", "sensordata", samplePath, "invalidEvents.txt");
            KafkaEventPublisherClient.publish("localhost:9092", "sensordata", samplePath, "validEvents002.txt");

            // Wait while all stats are published
            Thread.sleep(10000);

            List<Event> eventList = new ArrayList<>();
            Event event = new Event();
            event.setStreamId("org.wso2.event.sensor.stream:1.0.0");
            event.setMetaData(new Object[]{4354643L, false, 701, "temperature"});
            event.setCorrelationData(new Object[]{4.504343, 20.44345});
            event.setPayloadData(new Object[]{2.3f, 4.504343});
            eventList.add(event);
            Event event2 = new Event();
            event2.setStreamId("org.wso2.event.sensor.stream:1.0.0");
            event2.setMetaData(new Object[]{4354643L, false, 702, "temperature"});
            event2.setCorrelationData(new Object[]{4.504343, 20.44345});
            event2.setPayloadData(new Object[]{2.3f, 4.504343});
            eventList.add(event2);
            Event event3 = new Event();
            event3.setStreamId("org.wso2.event.sensor.stream:1.0.0");
            event3.setMetaData(new Object[]{4354643L, false, 703, "temperature"});
            event3.setCorrelationData(new Object[]{4.504343, 20.44345});
            event3.setPayloadData(new Object[]{2.3f, 4.504343});
            eventList.add(event3);
            Event event4 = new Event();
            event4.setStreamId("org.wso2.event.sensor.stream:1.0.0");
            event4.setMetaData(new Object[]{4354643L, false, 704, "temperature"});
            event4.setCorrelationData(new Object[]{4.504343, 20.44345});
            event4.setPayloadData(new Object[]{2.3f, 4.504343});
            eventList.add(event4);
            Event event5 = new Event();
            event5.setStreamId("org.wso2.event.sensor.stream:1.0.0");
            event5.setMetaData(new Object[]{4354643L, false, 705, "temperature"});
            event5.setCorrelationData(new Object[]{4.504343, 20.44345});
            event5.setPayloadData(new Object[]{2.3f, 4.504343});
            eventList.add(event5);
            Event event6 = new Event();
            event6.setStreamId("org.wso2.event.sensor.stream:1.0.0");
            event6.setMetaData(new Object[]{4354643L, false, 706, "temperature"});
            event6.setCorrelationData(new Object[]{4.504343, 20.44345});
            event6.setPayloadData(new Object[]{2.3f, 4.504343});
            eventList.add(event6);

            Assert.assertEquals(agentServer.getMsgCount(), messageCount, "Incorrect number of messages consumed!");
            List<Event> preservedEventList = agentServer.getPreservedEventList();
            for (Event aEvent : preservedEventList) {
                aEvent.setTimeStamp(0);
            }
            Assert.assertEquals(preservedEventList, eventList, "Mapping is incorrect!");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
            eventReceiverAdminServiceClient.removeInactiveEventReceiverConfiguration("kafkaReceiver.xml");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("wso2EventPublisher.xml");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer.stop();
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            Thread.sleep(5000);
            if (kafkaServer != null) {
                kafkaServer.shutdown();
            }
            Thread.sleep(5000);
            if (zkTestServer != null) {
                zkTestServer.stop();
            }
            Thread.sleep(5000);
        } finally {
            // reverting the changes done to cep sever.
            removeJarsFromComponentLib();
        }
        super.cleanup();
    }

    //---- private methods --------
    private void setupKafkaBroker() {
        try {
            // mock zookeeper
            zkTestServer = new TestingServer(2181);
            // mock kafka
            Properties props = new Properties();
            props.put("broker.id", "0");
            props.put("host.name", "localhost");
            props.put("port", "9092");
            props.put("log.dir", "/tmp/tmp_kafka_dir");
            props.put("zookeeper.connect", zkTestServer.getConnectString());
            props.put("replica.socket.timeout.ms", "1500");
            KafkaConfig config = new KafkaConfig(props);
            kafkaServer = new KafkaServerStartable(config);
            kafkaServer.startup();

            // create "sensordata" topic
            ZkClient zkClient = new ZkClient(zkTestServer.getConnectString(), 10000, 10000, ZKStringSerializer$.MODULE$);
            AdminUtils.createTopic(zkClient, "sensordata", 1, 1, new Properties());
            zkClient.close();
        } catch (Exception e) {
            log.error("Error running local Kafka broker / Zookeeper", e);
        }
    }

    private void copyJarsToComponentLib() throws URISyntaxException, IOException, AutomationUtilException {
        // copying kafka dependency jar files to components/lib.
        String JAR_LOCATION = File.separator + "artifacts" + File.separator + "CEP" + File.separator +
                "jar" + File.separator + "kafka" + File.separator;
        if (serverConfigManager != null) {
            for (String jar : LIBS_JARS) {
                serverConfigManager.copyToComponentLib(new File(getClass().getResource(JAR_LOCATION + jar).toURI()));
            }
            serverConfigManager.restartGracefully();
        }
    }

    private void removeJarsFromComponentLib() throws IOException, URISyntaxException, AutomationUtilException {
        // remove kafka dependency jar files to components/lib.
        if (serverConfigManager != null) {
            for (String jar : LIBS_JARS) {
                serverConfigManager.removeFromComponentLib(jar);
            }
            serverConfigManager.restoreToLastConfiguration();
        }
    }
}
