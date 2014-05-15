package org.wso2.carbon.cep.sample.test;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.cep.BrokerManagerAdminServiceClient;
import org.wso2.carbon.automation.api.clients.cep.CEPAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPAdminException;
import org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPConfigurationException;
import org.wso2.carbon.cep.stub.admin.internal.xsd.BucketDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.ExpressionDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.InputTupleMappingDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.InputTuplePropertyDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.OutputDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.OutputTupleMappingDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.OutputTuplePropertyDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO;
import org.wso2.carbon.cep.util.PhoneRetailAgent;
import org.wso2.carbon.cep.util.TestAgentServer;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.RemoteException;

public class KPIAnalyzerTestCase {


    private static final Log log = LogFactory.getLog(KPIAnalyzerTestCase.class);
    private CEPAdminServiceClient cepAdminServiceClient;
    private BrokerManagerAdminServiceClient brokerManagerAdminServiceClient;
    private TestAgentServer testAgentServer;
    private static final String BUCKET_NAME = "KPIAnalyzer";


    @BeforeClass(groups = {"wso2.cep"})
    public void init() throws Exception {
        log.info("Initializing Tests for "+BUCKET_NAME);

        int userId = 0;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().cep(userId);
        ManageEnvironment environment = builder.build();

        cepAdminServiceClient =
                new CEPAdminServiceClient(environment.getCep().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
        brokerManagerAdminServiceClient = new BrokerManagerAdminServiceClient(environment.getCep().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = cepAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, environment.getCep().getSessionCookie());


    }


    @Test(groups = {"wso2.cep"}, dependsOnGroups = {"wso2.cep.broker"})
    public void siddhiBucketCreationForKPIAnalyzerTest()
            throws CEPAdminServiceCEPConfigurationException, RemoteException,
                   CEPAdminServiceCEPAdminException, InterruptedException, DataBridgeException,
                   AgentException, MalformedURLException, AuthenticationException,
                   javax.security.sasl.AuthenticationException, MalformedStreamDefinitionException,
                   SocketException, StreamDefinitionException, TransportException,
                   NoStreamDefinitionExistException,
                   DifferentStreamDefinitionAlreadyDefinedException {


        BucketDTO bucket = createBucket();

        InputDTO input = createInput();
        QueryDTO query = createQuery();
        OutputDTO output = createOutput();
        query.setOutput(output);
        bucket.setInputs(new InputDTO[]{input});
        bucket.setQueries(new QueryDTO[]{query});

        cepAdminServiceClient.addBucket(bucket);

        /* extra time for all the services to be properly deployed */
        Thread.sleep(25000);

        Assert.assertNotNull(cepAdminServiceClient.getBucket(BUCKET_NAME));
    }


    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"siddhiBucketCreationForKPIAnalyzerTest"})
    public void kpiAnalyzerTest() throws AgentException, MalformedURLException, AuthenticationException,
                                     javax.security.sasl.AuthenticationException,
                                     MalformedStreamDefinitionException, SocketException,
                                     StreamDefinitionException, TransportException,
                                     NoStreamDefinitionExistException,
                                     DifferentStreamDefinitionAlreadyDefinedException,
                                     InterruptedException {
        testAgentServer = new TestAgentServer();
        Thread thread = new Thread(testAgentServer);
        thread.start();

        Thread.sleep(5000);

        PhoneRetailAgent.publish();

        Thread.sleep(5000);

    }


    private BucketDTO createBucket() {
        BucketDTO bucket = new BucketDTO();

        bucket.setName(BUCKET_NAME);
        bucket.setDescription("Notifies when a user purchases more then 3 phones for the total price higher than $2500.");
        bucket.setEngineProvider("SiddhiCEPRuntime");

        return bucket;
    }

    private InputDTO createInput() {
        InputDTO input = new InputDTO();
        input.setTopic("org.wso2.phone.retail.store/1.2.0");
        input.setBrokerName("localAgentBroker");


        InputTupleMappingDTO mappingDTO = new InputTupleMappingDTO();
        mappingDTO.setQueryEventType("Tuple");
        mappingDTO.setStream("phoneRetailStream");

        InputTuplePropertyDTO tuplePropertyDTO = new InputTuplePropertyDTO();
        tuplePropertyDTO.setName("brand");
        tuplePropertyDTO.setType("java.lang.String");
        tuplePropertyDTO.setInputDataType("payloadData");
        tuplePropertyDTO.setInputName("brand");

        InputTuplePropertyDTO tuplePropertyDTOTwo = new InputTuplePropertyDTO();
        tuplePropertyDTOTwo.setName("quantity");
        tuplePropertyDTOTwo.setType("java.lang.Integer");
        tuplePropertyDTOTwo.setInputDataType("payloadData");
        tuplePropertyDTOTwo.setInputName("quantity");

        InputTuplePropertyDTO tuplePropertyDTOThree = new InputTuplePropertyDTO();
        tuplePropertyDTOThree.setName("totalPrice");
        tuplePropertyDTOThree.setType("java.lang.Integer");
        tuplePropertyDTOThree.setInputDataType("payloadData");
        tuplePropertyDTOThree.setInputName("total");

        InputTuplePropertyDTO tuplePropertyDTOFour = new InputTuplePropertyDTO();
        tuplePropertyDTOFour.setName("buyer");
        tuplePropertyDTOFour.setType("java.lang.String");
        tuplePropertyDTOFour.setInputDataType("payloadData");
        tuplePropertyDTOFour.setInputName("buyer");

        mappingDTO.addProperties(tuplePropertyDTO);
        mappingDTO.addProperties(tuplePropertyDTOTwo);
        mappingDTO.addProperties(tuplePropertyDTOThree);
        mappingDTO.addProperties(tuplePropertyDTOFour);
        input.setInputTupleMappingDTO(mappingDTO);
        return input;
    }

    private QueryDTO createQuery() {
        QueryDTO query = new QueryDTO();
        query.setName("KPIQuery");

        ExpressionDTO expression = new ExpressionDTO();
        expression.setType("inline");
        expression.setText("from phoneRetailStream[totalPrice>2500 and quantity>3]\n" +
                           "insert into highPurchaseStream\n" +
                           "buyer, brand, quantity, totalPrice;");

        query.setExpression(expression);
        return query;
    }

    OutputDTO output = new OutputDTO();

    private OutputDTO createOutput() {
        OutputDTO output = new OutputDTO();
        output.setTopic("org.wso2.high.purchase.buyers/1.5.0");
        output.setBrokerName("externalAgentBroker");

        OutputTupleMappingDTO outputTupleMappingDTO = new OutputTupleMappingDTO();
        OutputTuplePropertyDTO outputTuplePropertyDTO = new OutputTuplePropertyDTO();
        outputTuplePropertyDTO.setName("buyer");
        outputTuplePropertyDTO.setType("java.lang.String");
        outputTuplePropertyDTO.setValueOf("buyer");


        OutputTuplePropertyDTO outputTuplePropertyDTOOne = new OutputTuplePropertyDTO();
        outputTuplePropertyDTOOne.setName("brand");
        outputTuplePropertyDTOOne.setType("java.lang.String");
        outputTuplePropertyDTOOne.setValueOf("brand");


        OutputTuplePropertyDTO outputTuplePropertyDTOTwo = new OutputTuplePropertyDTO();
        outputTuplePropertyDTOTwo.setName("purchasePrice");
        outputTuplePropertyDTOTwo.setType("java.lang.Integer");
        outputTuplePropertyDTOTwo.setValueOf("totalPrice");

        OutputTuplePropertyDTO outputTuplePropertyDTOThree = new OutputTuplePropertyDTO();
        outputTuplePropertyDTOThree.setName("quantity");
        outputTuplePropertyDTOThree.setType("java.lang.Integer");
        outputTuplePropertyDTOThree.setValueOf("quantity");

        outputTupleMappingDTO.addMetaDataProperties(outputTuplePropertyDTO);
        outputTupleMappingDTO.addPayloadDataProperties(outputTuplePropertyDTOOne);
        outputTupleMappingDTO.addPayloadDataProperties(outputTuplePropertyDTOTwo);
        outputTupleMappingDTO.addPayloadDataProperties(outputTuplePropertyDTOThree);
        output.setOutputTupleMapping(outputTupleMappingDTO);
        return output;
    }

    @AfterClass(groups = {"wso2.cep"})
    public void removeBuckets() throws Exception {

        cepAdminServiceClient.removeBucket(BUCKET_NAME);
        cepAdminServiceClient = null;

        testAgentServer = null;
    }


}
