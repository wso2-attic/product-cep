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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.integration.test.CEPIntegrationTest;

import javax.xml.xpath.XPathExpressionException;

/**
 * A test case which verifies that all admin services deployed on this Carbon server are properly
 * secured
 */
public class SecurityVerificationTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(SecurityVerificationTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception, LoginAuthenticationExceptionException {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    @Test(groups = "wso2.cep", description = "Ensures that all Admin services are exposed only via HTTPS")
    public void verifyAdminServiceSecurity() throws AxisFault, XPathExpressionException {
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        String serviceName = "SecurityVerifierService";
        String endpointReference = getServiceUrlHttps(serviceName);
        axisServiceClient.sendRobust(createPayLoad(), endpointReference, "echoInt");   // robust send. Will get reply only if there is a fault
        log.info("Sent the message");
    }

    private OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs =
                fac.createOMNamespace("http://secverifier.integration.carbon.wso2.org", "ns");
        return fac.createOMElement("verifyAdminServices", omNs);
    }
}
