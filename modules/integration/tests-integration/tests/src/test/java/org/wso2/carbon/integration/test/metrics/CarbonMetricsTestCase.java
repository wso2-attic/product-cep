/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.test.metrics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.metrics.data.common.Metric;
import org.wso2.carbon.metrics.data.common.MetricAttribute;
import org.wso2.carbon.metrics.data.common.MetricList;
import org.wso2.carbon.metrics.data.common.MetricType;
import org.wso2.carbon.metrics.manager.jmx.MetricManagerMXBean;
import org.wso2.carbon.metrics.view.ui.MetricDataWrapper;
import org.wso2.carbon.metrics.view.ui.MetricsViewClient;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CarbonMetricsTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(CarbonMetricsTestCase.class);
    private static final String ARTIFACTS_FOLDER = "metrics";
    private ServerConfigurationManager serverManager;
    private String sessionCookie;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(cepServer);
        serverManager.applyConfiguration(
                new File(getTestArtifactLocation() + CEPIntegrationTestConstants.RELATIVE_PATH_TO_TEST_ARTIFACTS +
                        ARTIFACTS_FOLDER + File.separator + "carbon.xml"),
                new File(ServerConfigurationManager.getCarbonHome() + File.separator + "repository" + File.separator +
                        "conf" + File.separator + "carbon.xml"),
                true,
                true);

        sessionCookie = getSessionCookie();
        eventSimulatorAdminServiceClient = configurationUtil
                .getEventSimulatorAdminServiceClient(backendURL, sessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil
                .getEventStreamManagerAdminServiceClient(backendURL, sessionCookie);
        eventPublisherAdminServiceClient = configurationUtil
                .getEventPublisherAdminServiceClient(backendURL, sessionCookie);
        eventReceiverAdminServiceClient = configurationUtil
                .getEventReceiverAdminServiceClient(backendURL, sessionCookie);
        eventProcessorAdminServiceClient = configurationUtil
                .getEventProcessorAdminServiceClient(backendURL, sessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Test enabling CEP metrics.")
    public void testEnableCEPMetrics() throws Exception {
        int eventStreamCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int executionPlanCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        int eventReceiverCount = eventReceiverAdminServiceClient.getEventReceiverCount();
        int eventPublisherCount = eventPublisherAdminServiceClient.getEventPublisherCount();
        try {
            // Add stream definitions.
            String rawBusStream = getJSONArtifactConfiguration(ARTIFACTS_FOLDER, "rawBusStream-1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(rawBusStream);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            String busBatchStream = getJSONArtifactConfiguration(ARTIFACTS_FOLDER, "busBatchStream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(busBatchStream);
            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), ++eventStreamCount);

            // Add event receivers.
            String busDataReceiver = getXMLArtifactConfiguration(ARTIFACTS_FOLDER, "busDataReceiver.xml");
            eventReceiverAdminServiceClient.addEventReceiverConfiguration(busDataReceiver);
            Assert.assertEquals(eventReceiverAdminServiceClient.getEventReceiverCount(), ++eventReceiverCount);

            // Add event publishers.
            String busDataPublisher = getXMLArtifactConfiguration(ARTIFACTS_FOLDER, "busDataPublisher.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(busDataPublisher);
            Assert.assertEquals(eventPublisherAdminServiceClient.getEventPublisherCount(), ++eventPublisherCount);

            // Add execution plan.
            String executionPlan = getExecutionPlanFromFile(ARTIFACTS_FOLDER, "busBatchProcessor.siddhiql");
            eventProcessorAdminServiceClient.addExecutionPlan(executionPlan);
            Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(),
                    ++executionPlanCount);

            // Set statistics and tracing enable.
            eventProcessorAdminServiceClient.setStatisticsEnabled("busBatchProcessor", true);
            eventReceiverAdminServiceClient.setStatisticsEnabled("busDataReceiver", true);
            eventPublisherAdminServiceClient.setStatisticsEnabled("busDataPublisher", true);

            // Get active execution plans.
            ExecutionPlanConfigurationDto executionPlanConfigurationDto = eventProcessorAdminServiceClient.
                    getActiveExecutionPlanConfiguration("busBatchProcessor");
            Assert.assertEquals(executionPlanConfigurationDto.getStatisticsEnabled(), true);

            // Send test events.
            EventDto eventDto = new EventDto();
            eventDto.setEventStreamId("rawBusStream:1.0.0");
            eventDto.setAttributeValues(new String[]{"20", "B1", "2", "145", "51.5", "-0.4", "V", "8.6", "1.7"});

            EventDto eventDto1 = new EventDto();
            eventDto1.setEventStreamId("rawBusStream:1.0.0");
            eventDto1.setAttributeValues(new String[]{"21", "B2", "2", "145", "51.5", "-0.4", "V", "8.6", "1.7"});

            EventDto eventDto2 = new EventDto();
            eventDto2.setEventStreamId("rawBusStream:1.0.0");
            eventDto2.setAttributeValues(new String[]{"16", "LT", "2", "145", "51.5", "-0.1", "V", "1.4", "5.1"});

            EventDto eventDto3 = new EventDto();
            eventDto3.setEventStreamId("rawBusStream:1.0.0");
            eventDto3.setAttributeValues(new String[]{"27", "BN", "2", "145", "51.5", "-0.6", "V", "6.3", "1.6"});

            EventDto eventDto4 = new EventDto();
            eventDto4.setEventStreamId("rawBusStream:1.0.0");
            eventDto4.setAttributeValues(new String[]{"31", "LJ", "1", "145", "51.5", "-0.2", "V", "3.3", "1.7"});

            eventSimulatorAdminServiceClient.sendEvent(eventDto);
            Thread.sleep(500);
            eventSimulatorAdminServiceClient.sendEvent(eventDto1);
            Thread.sleep(500);
            eventSimulatorAdminServiceClient.sendEvent(eventDto2);
            Thread.sleep(500);
            eventSimulatorAdminServiceClient.sendEvent(eventDto3);
            Thread.sleep(500);
            eventSimulatorAdminServiceClient.sendEvent(eventDto4);
            Thread.sleep(500);

            // Invoke metrics reporting.
            long fromTime = System.currentTimeMillis();
            invokeJMXReportOperation();
            Thread.sleep(10000);

            MetricsViewClient metricsViewClient;
            MetricList metricList = new MetricList();
            MetricDataWrapper metricData;

            metricsViewClient = new MetricsViewClient(sessionCookie, backendURL, null);
            String source = metricsViewClient.getAllSources()[0];
            ArrayList<Metric> metrics = new ArrayList<>();
            metrics.add(new Metric(
                    MetricType.COUNTER, "WSO2_CEP.EventPublishers.busDataPublisher.PublishedEvents", "PublishedEvents",
                    MetricAttribute.COUNT, null));
            metrics.add(new Metric(
                    MetricType.METER, "WSO2_CEP.ExecutionPlans.busBatchProcessor.Siddhi.Streams.dataIn.throughput",
                    "Throughput", MetricAttribute.COUNT, null));
            metrics.add(new Metric(MetricType.METER,
                    "WSO2_CEP.ExecutionPlans.busBatchProcessor.Siddhi.Streams.dataOut.throughput",
                    "Throughput", MetricAttribute.COUNT, null));
            metrics.add(new Metric(MetricType.COUNTER,
                    "WSO2_CEP.ExecutionPlans.busBatchProcessor.Streams.busBatchStream:1_0_0.OutputEvents",
                    "OutputEvents", MetricAttribute.COUNT, null));
            metrics.add(new Metric(MetricType.COUNTER,
                    "WSO2_CEP.ExecutionPlans.busBatchProcessor.Streams.rawBusStream:1_0_0.InputEvents",
                    "InputEvents", MetricAttribute.COUNT, null));

            metricList.setMetric(metrics.toArray(new Metric[metrics.size()]));
            metricData = metricsViewClient.findLastMetrics(metricList, source, String.valueOf(fromTime));

            // Evaluating metrics data
            Assert.assertEquals(metricData.getData()[0][1].intValue(), 5,
                    metricData.getData()[0][1].intValue() + " events found.");
            Assert.assertEquals(metricData.getData()[0][2].intValue(), 5,
                    metricData.getData()[0][1].intValue() + " events found.");
            Assert.assertEquals(metricData.getData()[0][3].intValue(), 5,
                    metricData.getData()[0][1].intValue() + " events found.");
            Assert.assertEquals(metricData.getData()[0][4].intValue(), 5,
                    metricData.getData()[0][1].intValue() + " events found.");
            Assert.assertEquals(metricData.getData()[0][5].intValue(), 5,
                    metricData.getData()[0][1].intValue() + " events found.");

        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
        serverManager.restoreToLastConfiguration();
        serverManager.restartGracefully();
        log.info("Restored configuration and restarted gracefully...");
    }

    /**
     * This method will force metric manager to collect metrics by invoking report() method
     * using remote jmx
     * @throws IOException
     * @throws MalformedObjectNameException
     */
    private void invokeJMXReportOperation() throws IOException, MalformedObjectNameException, XPathExpressionException {
        int JMXServicePort = Integer.parseInt(cepServer.getInstance().getPorts().get("jmxserver"));
        int RMIRegistryPort = Integer.parseInt(cepServer.getInstance().getPorts().get("rmiregistry"));
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:" + JMXServicePort +
                "/jndi/rmi://localhost:" + RMIRegistryPort + "/jmxrmi");
        Map<String, String[]> env = new HashMap<>();
        String[] credentials = {"admin", "admin"};
        env.put(JMXConnector.CREDENTIALS, credentials);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(url, env);
        MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
        ObjectName mbeanName = new ObjectName("org.wso2.carbon:type=MetricManager");
        MetricManagerMXBean mbeanProxy =
                MBeanServerInvocationHandler.newProxyInstance(
                        mbeanServerConnection, mbeanName, MetricManagerMXBean.class, true);
        mbeanProxy.report();
        jmxConnector.close();
    }
}