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


import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.simulator.stub.types.EventDto;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class AttributeEncryptTestCase  extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(AttributeEncryptTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();

        eventReceiverAdminServiceClient = configurationUtil.getEventReceiverAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);

    }

    @Test(groups = {"wso2.cep"}, description = "Testing the deployed artifacts with attributes need to be encrypted is being encrypted after it gets deployed")
    public void attributeEncryptTestScenario() throws Exception {

        int startESCount = eventStreamManagerAdminServiceClient.getEventStreamCount();
        int startEPCount = eventPublisherAdminServiceClient.getActiveEventPublisherCount();

        EventDto eventDto = new EventDto();
        eventDto.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto.setAttributeValues(new String[]{"199008131245", "false", "100", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto2 = new EventDto();
        eventDto2.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto2.setAttributeValues(new String[]{"199008131245", "false", "101", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        EventDto eventDto3 = new EventDto();
        eventDto3.setEventStreamId("org.wso2.event.sensor.stream:1.0.0");
        eventDto3.setAttributeValues(new String[]{"199008131245", "false", "103", "temperature", "23.45656", "7.12324",
                "100.34", "23.4545"});

        //Add StreamDefinition
        String streamDefinitionAsString = getJSONArtifactConfiguration("AttributeEncryptTestCase",
                "org.wso2.event.sensor.stream_1.0.0.json");
        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), startESCount + 1);

        //Add ActiveMQ JMS EventPublisher
        String eventPublisherConfig = getXMLArtifactConfiguration("AttributeEncryptTestCase", "jmsPublisherText.xml");
        eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);
        Assert.assertEquals(eventPublisherAdminServiceClient.getActiveEventPublisherCount(), startEPCount + 1);

        String publisherConfiguration = eventPublisherAdminServiceClient.getActiveEventPublisherConfigurationContent(
                "jmsPublisherText");
        OMElement omePublisherConfig = AXIOMUtil.stringToOM(publisherConfiguration);
        Iterator propertyIter = omePublisherConfig.getChildrenWithName(new QName("property"));

        boolean isEncrypted = true;
        while (propertyIter.hasNext()) {
            OMElement propertyOMElement = (OMElement) propertyIter.next();
            String name = propertyOMElement.getAttributeValue(new QName("name"));
            if(name.equalsIgnoreCase("transport.jms.Password")){
                OMAttribute encryptedAttribute = propertyOMElement.getAttribute(new QName("encrypted"));
                if (encryptedAttribute == null || (!"true".equals(encryptedAttribute.getAttributeValue()))) {
                    isEncrypted = false;
                }
            }
        }

        eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
        eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("jmsPublisherText.xml");

        try {
            Assert.assertTrue(isEncrypted, "The properties have not encrypted properly");
        } catch (Throwable e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
