package org.wso2.carbon.cep.broker.test;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.cep.BrokerManagerAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceBrokerManagerAdminServiceExceptionException;
import org.wso2.carbon.brokermanager.stub.types.BrokerConfigurationDetails;
import org.wso2.carbon.brokermanager.stub.types.BrokerProperty;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Check whether the BrokerManager properly initializes the Brokers
 */
public class BrokerInitializationTestCase {
    private ManageEnvironment environment;
    private BrokerManagerAdminServiceClient brokerManagerAdminServiceClient;
    private final int userID = 0;


    @BeforeClass(groups = {"wso2.cep.broker"})
    public void initialize() throws LoginAuthenticationExceptionException, RemoteException {
        UserInfo userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().cep(userID);
        environment = builder.build();
        String loggedInSessionCookie = environment.getCep().getSessionCookie();
        brokerManagerAdminServiceClient = new BrokerManagerAdminServiceClient(environment.getCep().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = brokerManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                            loggedInSessionCookie);

    }

    @Test(groups = {"wso2.cep.broker"})
    public void testBrokerInit() throws RemoteException,
                                        BrokerManagerAdminServiceBrokerManagerAdminServiceExceptionException {


        String[] brokerNames = brokerManagerAdminServiceClient.getBrokerNames();

        ArrayList<BrokerProperty> localBrokerPropertyArrayList = null;
        ArrayList<BrokerProperty> wsEventBrokerPropertyArrayList = null;
        ArrayList<BrokerProperty> jmsBrokerPropertyArrayList = null;
        ArrayList<BrokerProperty> localAgentBrokerPropertyList = null;
        ArrayList<BrokerProperty> externalAgentBrokerPropertyList = null;


        for (String brokerName : brokerNames) {
            if (brokerName.equalsIgnoreCase("local")) {
                BrokerProperty[] brokerProperties = brokerManagerAdminServiceClient.getBrokerProperties(brokerName);
                BrokerProperty[] localBrokerProperties = new BrokerProperty[2];
                localBrokerPropertyArrayList = new ArrayList<BrokerProperty>();

                localBrokerProperties[0] = new BrokerProperty();
                localBrokerProperties[0].setKey("name");
                localBrokerProperties[0].setValue("localBroker");
                localBrokerProperties[0].setRequired(true);
                localBrokerProperties[0].setDisplayName("Broker Name");
                localBrokerProperties[0].setSecured(false);
                localBrokerPropertyArrayList.add(localBrokerProperties[0]);

                localBrokerProperties[1] = new BrokerProperty();
                localBrokerProperties[1].setKey("type");
                localBrokerProperties[1].setValue("local");
                localBrokerProperties[1].setRequired(true);
                localBrokerProperties[1].setDisplayName("Broker Type");
                localBrokerProperties[1].setSecured(false);
                localBrokerPropertyArrayList.add(localBrokerProperties[1]);


            } else if (brokerName.equalsIgnoreCase("ws-event")) {
                BrokerProperty[] brokerProperties = brokerManagerAdminServiceClient.getBrokerProperties(brokerName);
                if (brokerProperties != null) {
                    wsEventBrokerPropertyArrayList = new ArrayList<BrokerProperty>();
                    for (BrokerProperty wsEventBrokerProperty : brokerProperties) {
                        if (wsEventBrokerProperty.getKey().equals("uri")) {
                            wsEventBrokerProperty.setValue("https://localhost:9443/services/EventBrokerService");
                            wsEventBrokerPropertyArrayList.add(wsEventBrokerProperty);
                        } else if (wsEventBrokerProperty.getKey().equals("username")) {
                            wsEventBrokerProperty.setValue("admin");
                            wsEventBrokerPropertyArrayList.add(wsEventBrokerProperty);
                        } else if (wsEventBrokerProperty.getKey().equals("password")) {
                            wsEventBrokerProperty.setValue("admin");
                            wsEventBrokerPropertyArrayList.add(wsEventBrokerProperty);
                        }
                    }
                    BrokerProperty[] wsEventBrokerProperties = new BrokerProperty[2];

                    wsEventBrokerProperties[0] = new BrokerProperty();
                    wsEventBrokerProperties[0].setKey("name");
                    wsEventBrokerProperties[0].setValue("wsEventBroker");
                    wsEventBrokerProperties[0].setRequired(true);
                    wsEventBrokerProperties[0].setDisplayName("Broker Name");
                    wsEventBrokerProperties[0].setSecured(false);

                    wsEventBrokerPropertyArrayList.add(wsEventBrokerProperties[0]);


                    wsEventBrokerProperties[1] = new BrokerProperty();
                    wsEventBrokerProperties[1].setKey("type");
                    wsEventBrokerProperties[1].setValue("ws-event");
                    wsEventBrokerProperties[1].setRequired(true);
                    wsEventBrokerProperties[1].setDisplayName("Broker Type");
                    wsEventBrokerProperties[1].setSecured(false);
                    wsEventBrokerPropertyArrayList.add(wsEventBrokerProperties[1]);
                }
            } else if (brokerName.equalsIgnoreCase("agent")) {

                BrokerProperty[] brokerProperties = brokerManagerAdminServiceClient.getBrokerProperties(brokerName);

                localAgentBrokerPropertyList = new ArrayList<BrokerProperty>();
                externalAgentBrokerPropertyList = new ArrayList<BrokerProperty>();
                if (brokerProperties != null) {

                    for (BrokerProperty localAgentEventBrokerProperty : brokerProperties) {

                        if (localAgentEventBrokerProperty.getKey().equals("username")) {
                            localAgentEventBrokerProperty.setValue("admin");
                            localAgentBrokerPropertyList.add(localAgentEventBrokerProperty);

                        } else if (localAgentEventBrokerProperty.getKey().equals("password")) {
                            localAgentEventBrokerProperty.setValue("admin");
                            localAgentBrokerPropertyList.add(localAgentEventBrokerProperty);
                        } else if (localAgentEventBrokerProperty.getKey().equals("receiverURL")) {
                            localAgentEventBrokerProperty.setValue("tcp://localhost:7611");
                            localAgentBrokerPropertyList.add(localAgentEventBrokerProperty);
                        } else if (localAgentEventBrokerProperty.getKey().equals("authenticatorURL")) {
                            localAgentEventBrokerProperty.setValue("ssl://localhost:7711");
                            localAgentBrokerPropertyList.add(localAgentEventBrokerProperty);
                        }
                    }
                    for (BrokerProperty externalAgentEventBrokerProperty : brokerProperties) {

                        if (externalAgentEventBrokerProperty.getKey().equals("username")) {
                            externalAgentEventBrokerProperty.setValue("admin");
                            externalAgentBrokerPropertyList.add(externalAgentEventBrokerProperty);

                        } else if (externalAgentEventBrokerProperty.getKey().equals("password")) {
                            externalAgentEventBrokerProperty.setValue("admin");
                            externalAgentBrokerPropertyList.add(externalAgentEventBrokerProperty);
                        } else if (externalAgentEventBrokerProperty.getKey().equals("receiverURL")) {
                            externalAgentEventBrokerProperty.setValue("tcp://localhost:7661");
                            externalAgentBrokerPropertyList.add(externalAgentEventBrokerProperty);
                        } else if (externalAgentEventBrokerProperty.getKey().equals("authenticatorURL")) {
                            externalAgentEventBrokerProperty.setValue("ssl://localhost:7761");
                            externalAgentBrokerPropertyList.add(externalAgentEventBrokerProperty);
                        }
                    }


                    BrokerProperty[] localAgentBrokerProperties = new BrokerProperty[2];
                    BrokerProperty[] externalAgentBrokerProperties = new BrokerProperty[2];
                    localAgentBrokerProperties[0] = new BrokerProperty();
                    localAgentBrokerProperties[0].setKey("name");
                    localAgentBrokerProperties[0].setValue("localAgentBroker");
                    localAgentBrokerProperties[0].setRequired(true);
                    localAgentBrokerProperties[0].setDisplayName("Broker Name");
                    localAgentBrokerProperties[0].setSecured(false);

                    localAgentBrokerPropertyList.add(localAgentBrokerProperties[0]);


                    localAgentBrokerProperties[1] = new BrokerProperty();
                    localAgentBrokerProperties[1].setKey("type");
                    localAgentBrokerProperties[1].setValue("agent");
                    localAgentBrokerProperties[1].setRequired(true);
                    localAgentBrokerProperties[1].setDisplayName("Type");
                    localAgentBrokerProperties[1].setSecured(false);
                    localAgentBrokerPropertyList.add(localAgentBrokerProperties[1]);


                    externalAgentBrokerProperties[0] = new BrokerProperty();
                    externalAgentBrokerProperties[0].setKey("name");
                    externalAgentBrokerProperties[0].setValue("externalAgentBroker");
                    externalAgentBrokerProperties[0].setRequired(true);
                    externalAgentBrokerProperties[0].setDisplayName("Broker Name");
                    externalAgentBrokerProperties[0].setSecured(false);

                    externalAgentBrokerPropertyList.add(externalAgentBrokerProperties[0]);


                    externalAgentBrokerProperties[1] = new BrokerProperty();
                    externalAgentBrokerProperties[1].setKey("type");
                    externalAgentBrokerProperties[1].setValue("agent");
                    externalAgentBrokerProperties[1].setRequired(true);
                    externalAgentBrokerProperties[1].setDisplayName("Type");
                    externalAgentBrokerProperties[1].setSecured(false);
                    externalAgentBrokerPropertyList.add(externalAgentBrokerProperties[1]);


                }
            }


            if (localBrokerPropertyArrayList != null) {
                BrokerProperty[] localBrokerProperties = new BrokerProperty[localBrokerPropertyArrayList.size()];
                localBrokerPropertyArrayList.toArray(localBrokerProperties);
                brokerManagerAdminServiceClient.addBrokerConfiguration("localBroker", "local", localBrokerProperties);
            }

            if (wsEventBrokerPropertyArrayList != null) {
                BrokerProperty[] wsEventBrokerProperties = new BrokerProperty[wsEventBrokerPropertyArrayList.size()];
                wsEventBrokerPropertyArrayList.toArray(wsEventBrokerProperties);
                brokerManagerAdminServiceClient.addBrokerConfiguration("wsEventBroker", "ws-event", wsEventBrokerProperties);
            }
            if (localAgentBrokerPropertyList != null) {
                BrokerProperty[] localAgentBrokerProperties = new BrokerProperty[localAgentBrokerPropertyList.size()];
                localAgentBrokerPropertyList.toArray(localAgentBrokerProperties);
                brokerManagerAdminServiceClient.addBrokerConfiguration("localAgentBroker", "agent", localAgentBrokerProperties);
            }
            if (externalAgentBrokerPropertyList != null) {
                BrokerProperty[] externalAgentBrokerProperties = new BrokerProperty[localAgentBrokerPropertyList.size()];
                externalAgentBrokerPropertyList.toArray(externalAgentBrokerProperties);
                brokerManagerAdminServiceClient.addBrokerConfiguration("externalAgentBroker", "agent", externalAgentBrokerProperties);
            }


            BrokerConfigurationDetails[] brokerConfigurationDetailList = brokerManagerAdminServiceClient.getAllBrokerConfigurationNamesAndTypes();
            for (BrokerConfigurationDetails brokerConfigurationDetails : brokerConfigurationDetailList) {
                brokerName = brokerConfigurationDetails.getBrokerName();
                if ("wsEventBroker".equals(brokerName)) {
                    Assert.assertEquals("ws-event", brokerConfigurationDetails.getBrokerType());
                    BrokerProperty[] createdBrokerProperties = brokerConfigurationDetails.getBrokerProperties();
                    for (BrokerProperty brokerProperty : createdBrokerProperties) {
                        String brokerPropertyKey = brokerProperty.getKey();
                        if ("username".equals(brokerPropertyKey)) {
                            Assert.assertNull(brokerProperty.getDisplayName());
                            Assert.assertEquals("admin", brokerProperty.getValue());
                            Assert.assertFalse(brokerProperty.getRequired());
                            Assert.assertFalse(brokerProperty.getSecured());
                        } else if ("name".equals(brokerPropertyKey)) {
                            Assert.assertNull(brokerProperty.getDisplayName());
                            Assert.assertEquals("wsEventBroker", brokerProperty.getValue());
                            Assert.assertFalse(brokerProperty.getRequired());
                            Assert.assertFalse(brokerProperty.getSecured());
                        } else if ("password".equals(brokerPropertyKey)) {
                            Assert.assertNull(brokerProperty.getDisplayName());
                            Assert.assertEquals("admin", brokerProperty.getValue());
                            Assert.assertFalse(brokerProperty.getRequired());
                            Assert.assertFalse(brokerProperty.getSecured());
                        } else if ("type".equals(brokerPropertyKey)) {
                            Assert.assertNull(brokerProperty.getDisplayName());
                            Assert.assertEquals("ws-event", brokerProperty.getValue());
                            Assert.assertFalse(brokerProperty.getRequired());
                            Assert.assertFalse(brokerProperty.getSecured());
                        } else if ("uri".equals(brokerPropertyKey)) {
                            Assert.assertNull(brokerProperty.getDisplayName());
                            Assert.assertEquals("https://localhost:9443/services/EventBrokerService", brokerProperty.getValue());
                            Assert.assertFalse(brokerProperty.getRequired());
                            Assert.assertFalse(brokerProperty.getSecured());
                        } else {
                            Assert.fail("Broker Property Key");
                        }
                    }
                } else if ("localBroker".equals(brokerName)) {
                    Assert.assertEquals("local", brokerConfigurationDetails.getBrokerType());
                    BrokerProperty[] createdBrokerProperties = brokerConfigurationDetails.getBrokerProperties();
                    for (BrokerProperty brokerProperty : createdBrokerProperties) {
                        String brokerPropertyKey = brokerProperty.getKey();
                        if ("name".equals(brokerPropertyKey)) {
                            Assert.assertNull(brokerProperty.getDisplayName());
                            Assert.assertEquals("localBroker", brokerProperty.getValue());
                            Assert.assertFalse(brokerProperty.getRequired());
                            Assert.assertFalse(brokerProperty.getSecured());
                        } else if ("type".equals(brokerPropertyKey)) {
                            Assert.assertNull(brokerProperty.getDisplayName());
                            Assert.assertEquals("local", brokerProperty.getValue());
                            Assert.assertFalse(brokerProperty.getRequired());
                            Assert.assertFalse(brokerProperty.getSecured());
                        } else {
                            Assert.fail("Broker Property Key");
                        }
                    }
                } else {
                    // Assert.fail("Unknown broker name");
                }
            }
        }
    }

    @AfterSuite
    public void clean() throws Exception {
        brokerManagerAdminServiceClient.removeBrokerConfiguration("localBroker");
        brokerManagerAdminServiceClient.removeBrokerConfiguration("wsEventBroker");
        brokerManagerAdminServiceClient.removeBrokerConfiguration("localAgentBroker");
        brokerManagerAdminServiceClient.removeBrokerConfiguration("externalAgentBroker");
    }

}