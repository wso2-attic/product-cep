package org.wso2.carbon.cep.services.test;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;

/**
 * A test case which verifies that all admin services deployed on this Carbon server are properly
 * secured
 */
public class SecurityVerificationTestCase {
    private static final Log log = LogFactory.getLog(SecurityVerificationTestCase.class);
    ManageEnvironment environment;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        int userID = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().cep(userID);
        environment = builder.build();
    }

    @Test(description = "Ensures that all Admin services are exposed only via HTTPS")
    public void verifyAdminServiceSecurity() throws AxisFault {
        ServiceClient client = new ServiceClient(null, null);

        Options opts = new Options();
        String serviceName = "SecurityVerifierService";

        EndpointReference epr =
                new EndpointReference(environment.getCep().getBackEndUrl() + serviceName);
        opts.setTo(epr);

        client.setOptions(opts);
        client.sendRobust(createPayLoad());   // robust send. Will get reply only if there is a fault
        log.info("sent the message");
    }

    private OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs =
                fac.createOMNamespace("http://secverifier.integration.carbon.wso2.org", "ns");
        return fac.createOMElement("verifyAdminServices", omNs);
    }
}
