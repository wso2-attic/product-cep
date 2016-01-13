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

package org.wso2.carbon.integration.test.ha;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.appserver.integration.common.clients.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.tests.CarbonTestServerManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.test.client.HttpEventPublisherClient;
import org.wso2.carbon.integration.test.client.Wso2EventServer;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.xml.sax.SAXException;

public class HATestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(HATestCase.class);
    private MultipleServersManager manager = new MultipleServersManager();
    private static final String EVENT_PROCESSING_FILE = "event-processor.xml";
    private static final String AXIS2_XML_FILE = "axis2.xml";
    private static final String RESOURCE_LOCATION1 = TestConfigurationProvider.getResourceLocation()  + "artifacts" + File.separator + "CEP"
            + File.separator + "HATestCase" + File.separator + "activeNodeConfigs";
    private static final String RESOURCE_LOCATION2 = TestConfigurationProvider.getResourceLocation()  + "artifacts" + File.separator + "CEP"
            + File.separator + "HATestCase" + File.separator + "passiveNodeConfigs";
    private AutomationContext cepServer1;
    private AutomationContext cepServer2;
    private CarbonTestServerManager server1;
    private CarbonTestServerManager server2;
    private EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient1;
    private EventReceiverAdminServiceClient eventReceiverAdminServiceClient1;
    private EventPublisherAdminServiceClient eventPublisherAdminServiceClient1;
    private EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient2;
    private EventReceiverAdminServiceClient eventReceiverAdminServiceClient2;
    private EventPublisherAdminServiceClient eventPublisherAdminServiceClient2;
    private static String machineIP;

    /*
        1. Start Server1 as an Active Member and Server2 as a Passive Member
        2. Publish 3 events to Server2(Passive node) -> 3 events were received by Server1(Active Node)
        3. Shutdown Server1(Active Node) -> Server2(Passive node) became Active Member
        4. Publish another 6 events to Server2(Active node) -> 6 events were received by Server2(Active node)
        5. Start Server1 again -> Server1 became Passive Member
        6. Publish another 3 events to Server1(Passive node) -> 3 events were received by Server2(Active node)
        7. Publish 3 events to Server2(Active node) -> 3 events were received by Server2(Active Node)
        8. Shutdown Server2(Active Node) -> Server1(Passive node) became Active Member
        9. Publish another 6 events to Server1(Active node) -> 6 events were received by Server1(Active node)
       10. Start Server2 again -> Server2 became Passive Member
       11. Publish another 3 events to Server2(Passive node) -> 3 events were received by Server1(Active node)
       12. Shutdown Server1 and Server2
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        machineIP = findAddress("localhost");
        cepServer1 = new AutomationContext("CEP", "cep002", TestUserMode.SUPER_TENANT_ADMIN);
        cepServer2 = new AutomationContext("CEP", "cep003", TestUserMode.SUPER_TENANT_ADMIN);
        server1 = new CarbonTestServerManager(cepServer1, 801);
        server2 = new CarbonTestServerManager(cepServer2, 802);
        ServerConfigurationManager serverConfigManager1 = new ServerConfigurationManager(cepServer1);
        ServerConfigurationManager serverConfigManager2 = new ServerConfigurationManager(cepServer2);
        manager.startServers(server1, server2);
        String CARBON_HOME1 = server1.getCarbonHome();
        String CARBON_HOME2 = server2.getCarbonHome();

        String eventProcessingFileLocation = RESOURCE_LOCATION1 + File.separator + EVENT_PROCESSING_FILE;
        String cepEventProcessorFileLocation = CARBON_HOME1 + File.separator + "repository" + File.separator
                + "conf" + File.separator + EVENT_PROCESSING_FILE;
        serverConfigManager1.applyConfigurationWithoutRestart(new File(eventProcessingFileLocation), new File(cepEventProcessorFileLocation), true);
        replaceIP(cepEventProcessorFileLocation);
        String axis2FileLocation = RESOURCE_LOCATION1 + File.separator + AXIS2_XML_FILE;
        String cepAxis2FileLocation = CARBON_HOME1 + File.separator + "repository" + File.separator + "conf"
                + File.separator + "axis2" + File.separator + AXIS2_XML_FILE;
        serverConfigManager1.applyConfigurationWithoutRestart(new File(axis2FileLocation), new File(cepAxis2FileLocation), true);
        replaceIP(cepAxis2FileLocation);

        log.info("Restarting CEP server1");
        serverConfigManager1.restartGracefully();
        // Waiting for the server to restart
        Thread.sleep(5000);

        String eventProcessingFileLocation2 = RESOURCE_LOCATION2 + File.separator + EVENT_PROCESSING_FILE;
        String cepEventProcessorFileLocation2 = CARBON_HOME2 + File.separator + "repository" + File.separator
                + "conf" + File.separator + EVENT_PROCESSING_FILE;
        serverConfigManager2.applyConfigurationWithoutRestart(new File(eventProcessingFileLocation2), new File(cepEventProcessorFileLocation2), true);
        replaceIP(cepEventProcessorFileLocation2);
        String axis2FileLocation2 = RESOURCE_LOCATION2 + File.separator + AXIS2_XML_FILE;
        String cepAxis2FileLocation2 = CARBON_HOME2 + File.separator + "repository" + File.separator + "conf"
                + File.separator + "axis2" + File.separator + AXIS2_XML_FILE;
        serverConfigManager2.applyConfigurationWithoutRestart(new File(axis2FileLocation2), new File(cepAxis2FileLocation2), true);
        replaceIP(cepAxis2FileLocation2);

        serverConfigManager2.restartGracefully();
        // Waiting for the server to restart
        Thread.sleep(5000);

        String backendURL1 = cepServer1.getContextUrls().getBackEndUrl();
        String loggedInSessionCookie = getSessionCookie(cepServer1);
        eventReceiverAdminServiceClient1 = configurationUtil.getEventReceiverAdminServiceClient(backendURL1, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient1 = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL1, loggedInSessionCookie);
        eventPublisherAdminServiceClient1 = configurationUtil.getEventPublisherAdminServiceClient(backendURL1, loggedInSessionCookie);
        String backendURL2 = cepServer2.getContextUrls().getBackEndUrl();
        String loggedInSessionCookie2 = getSessionCookie(cepServer2);
        eventReceiverAdminServiceClient2 = configurationUtil.getEventReceiverAdminServiceClient(backendURL2, loggedInSessionCookie2);
        eventStreamManagerAdminServiceClient2 = configurationUtil.getEventStreamManagerAdminServiceClient(backendURL2, loggedInSessionCookie2);
        eventPublisherAdminServiceClient2 = configurationUtil.getEventPublisherAdminServiceClient(backendURL2, loggedInSessionCookie2);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing CEP HA for two cluster nodes")
    public void test1() throws Exception {
        String samplePath = "HATestCase" + File.separator + "HAArtifacts";
        int startESCount1 = eventStreamManagerAdminServiceClient1.getEventStreamCount();
        int startERCount1 = eventReceiverAdminServiceClient1.getActiveEventReceiverCount();
        int startEPCount1 = eventPublisherAdminServiceClient1.getActiveEventPublisherCount();
        int startESCount2 = eventStreamManagerAdminServiceClient2.getEventStreamCount();
        int startERCount2 = eventReceiverAdminServiceClient2.getActiveEventReceiverCount();
        int startEPCount2 = eventPublisherAdminServiceClient2.getActiveEventPublisherCount();
        int server1MsgCount = 12;
        int server2MsgCount = 12;

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath, "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient1.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient1.getEventStreamCount(), startESCount1 + 1);
        eventStreamManagerAdminServiceClient2.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient2.getEventStreamCount(), startESCount2 + 1);

        //Add Http JSON EventReceiver without mapping
        String eventReceiverConfig = getXMLArtifactConfiguration(samplePath, "httpReceiver.xml");
        eventReceiverAdminServiceClient1.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient1.getActiveEventReceiverCount(), startERCount1 + 1);
        eventReceiverAdminServiceClient2.addEventReceiverConfiguration(eventReceiverConfig);
        Assert.assertEquals(eventReceiverAdminServiceClient2.getActiveEventReceiverCount(), startERCount2 + 1);

        //Add Wso2event EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher.xml");
        eventPublisherAdminServiceClient1.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient1.getActiveEventPublisherCount(), startEPCount1 + 1);
        eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "wso2EventPublisher2.xml");
        eventPublisherAdminServiceClient2.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient2.getActiveEventPublisherCount(), startEPCount2 + 1);

        // The data-bridge receiver
        Wso2EventServer agentServer1 = new Wso2EventServer(samplePath, Integer.parseInt(cepServer1.getInstance().getPorts().get("thrift_publisher"))+1, false);
        Thread agentServerThread = new Thread(agentServer1);
        agentServerThread.start();
        // Let the server start
        Thread.sleep(10000);

        // The data-bridge receiver
        Wso2EventServer agentServer2 = new Wso2EventServer(samplePath, Integer.parseInt(cepServer2.getInstance().getPorts().get("thrift_publisher"))+2, false);
        Thread agentServerThread2 = new Thread(agentServer2);
        agentServerThread2.start();
        // Let the server start
        Thread.sleep(10000);

        for (int i = 0; i < 3; i++) {
            if (i == 1) {
                log.info("Shutting down CEP Server1(Active Node)");
                server1.stopServer();
            }
            HttpEventPublisherClient.publish("http://localhost:" + cepServer2.getInstance().getPorts().get("http") +
                    File.separator+"endpoints"+File.separator+"httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");
            Thread.sleep(5000);
        }
        log.info("Starting CEP Server1");
        server1.startServer();
        HttpEventPublisherClient.publish("http://localhost:" + cepServer1.getInstance().getPorts().get("http") +
                File.separator + "endpoints" + File.separator + "httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");
        Thread.sleep(5000);
        for (int i = 0; i < 3; i++) {
            if (i == 1) {
                log.info("Shutting down CEP Server2(Active Node)");
                server2.stopServer();
            }
            HttpEventPublisherClient.publish("http://localhost:" + cepServer1.getInstance().getPorts().get("http") +
                    File.separator+"endpoints"+File.separator+"httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");
            Thread.sleep(5000);
        }
        log.info("Starting CEP Server2");
        server2.startServer();
        HttpEventPublisherClient.publish("http://localhost:" + cepServer2.getInstance().getPorts().get("http") +
                File.separator + "endpoints" + File.separator + "httpReceiver", "admin", "admin", samplePath, "httpReceiver.txt");
        Thread.sleep(5000);

        try {
            Assert.assertEquals(agentServer1.getMsgCount(), server1MsgCount, "Incorrect number of messages consumed by CEP Server1!");
            Assert.assertEquals(agentServer2.getMsgCount(), server2MsgCount, "Incorrect number of messages consumed by CEP server2!");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        } finally {
            agentServer1.stop();
            agentServer2.stop();
        }

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        super.cleanup();
        cepServer1 = null;
        cepServer2 = null;
        manager.stopAllServers();
    }


    protected String getSessionCookie(AutomationContext serverContext) throws Exception {
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(serverContext);
        return loginLogoutClient.login();
    }

    public static String findAddress(String hostname) throws SocketException {
        if (hostname.trim().equals("localhost") || hostname.trim().equals("127.0.0.1") || hostname.trim().equals("::1")) {
            Enumeration<NetworkInterface> ifaces =
                    NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return "127.0.0.1";
        } else {
            return hostname;
        }
    }

    public void replaceIP(String inputFilePath) throws ParserConfigurationException, XPathExpressionException, TransformerException, IOException, SAXException {
        String exp = "//*[text()='host-ip-address']";
        try {
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(inputFilePath));

            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression xPathExpression = xPath.compile(exp);
            NodeList nodes = (NodeList) xPathExpression.
                    evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                nodes.item(i).setTextContent(machineIP);
            }

            //Save the result to a new XML doc
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(new DOMSource(xmlDocument), new StreamResult(new File(inputFilePath)));
        } catch (Exception ex) {
            log.info("Error while replacing IP address");
        }
    }

}