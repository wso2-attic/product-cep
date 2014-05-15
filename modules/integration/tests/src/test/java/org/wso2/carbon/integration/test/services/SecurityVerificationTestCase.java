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
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.integration.test.CEPIntegrationTest;

import java.rmi.RemoteException;

/**
 * A test case which verifies that all admin services deployed on this Carbon server are properly
 * secured
 */
public class SecurityVerificationTestCase extends CEPIntegrationTest {
    private static final Log log = LogFactory.getLog(SecurityVerificationTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, LoginAuthenticationExceptionException {
        super.init();
    }

    @Test(groups = "wso2.cep", description = "Ensures that all Admin services are exposed only via HTTPS")
    public void verifyAdminServiceSecurity() throws AxisFault {
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        String serviceName = "SecurityVerifierService";
        String endpointReference = getSecuredServiceEndpoint(serviceName);
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
