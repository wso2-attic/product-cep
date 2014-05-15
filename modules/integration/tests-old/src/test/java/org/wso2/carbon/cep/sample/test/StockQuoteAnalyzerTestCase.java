package org.wso2.carbon.cep.sample.test;

import junit.framework.Assert;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.cep.CEPAdminServiceClient;
import org.wso2.carbon.automation.api.clients.cep.CEPStatisticsAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPAdminException;
import org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPConfigurationException;
import org.wso2.carbon.cep.stub.admin.internal.xsd.BucketDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.ExpressionDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.InputXMLMappingDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.InputXMLPropertyDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.OutputDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.OutputXMLMappingDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO;
import org.wso2.carbon.cep.stub.admin.internal.xsd.XpathDefinitionDTO;
import org.wso2.carbon.cep.util.StockQuoteAgent;
import org.wso2.carbon.cep.util.TestAgentServer;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.RemoteException;

public class StockQuoteAnalyzerTestCase {
    private CEPAdminServiceClient cepAdminServiceClient;
    private CEPStatisticsAdminServiceClient cepStatisticsAdminServiceClient;
    private TestAgentServer testAgentServer;
    private static final String BUCKET_NAME = "StockQuoteAnalyzerSample";

    @BeforeClass(groups = {"wso2.cep"})
    public void initialize() throws LoginAuthenticationExceptionException, RemoteException {
        int userID = 0;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().cep(userID);
        ManageEnvironment environment = builder.build();
        String loggedInSessionCookie = environment.getCep().getSessionCookie();
        cepStatisticsAdminServiceClient =
                new CEPStatisticsAdminServiceClient(environment.getCep()
                        .getProductVariables()
                        .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        cepAdminServiceClient =
                new CEPAdminServiceClient(environment.getCep()
                        .getProductVariables()
                        .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        ServiceClient client = cepAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);

    }

    @Test(groups = {"wso2.cep"}, dependsOnGroups = {"wso2.cep.broker"})
    public void siddhiBucketCreationForStockQuoteAnalyzerTest()
            throws CEPAdminServiceCEPConfigurationException, RemoteException,
            CEPAdminServiceCEPAdminException, InterruptedException {
        Thread.sleep(25000);
        int numberOfBuckets = cepAdminServiceClient.getAllBucketCount();


        BucketDTO bucket = createBucket();

        InputDTO input = createInput();
        QueryDTO query = createQuery();
        OutputDTO output = createOutput();
        query.setOutput(output);
        bucket.setInputs(new InputDTO[]{input});
        bucket.setQueries(new QueryDTO[]{query});

        cepAdminServiceClient.addBucket(bucket);

        /* extra time for all the services to be properly deployed */
        Thread.sleep(30000);
        int bucketCount = cepAdminServiceClient.getAllBucketCount();
        Assert.assertEquals(++numberOfBuckets, bucketCount);
        BucketDTO bucketDTO = cepAdminServiceClient.getBucket(BUCKET_NAME);
        Assert.assertNotNull(bucketDTO);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"siddhiBucketCreationForStockQuoteAnalyzerTest"})
    public void stockQuoteAnalyzerTest() throws
            org.wso2.carbon.databridge.agent.thrift.exception.AgentException,
            MalformedURLException, AuthenticationException,
            javax.security.sasl.AuthenticationException,
            MalformedStreamDefinitionException,
            SocketException, StreamDefinitionException,
            TransportException,
            NoStreamDefinitionExistException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, RemoteException {
        Thread.sleep(10000);
        long initResponse = cepStatisticsAdminServiceClient.getGlobalCount().getResponseCount();
        StockQuoteAgent.publish();

        Thread.sleep(10000);
        long finalResponse = cepStatisticsAdminServiceClient.getGlobalCount().getResponseCount();
        Assert.assertTrue(finalResponse > initResponse);

    }


    private BucketDTO createBucket() {
        BucketDTO bucket = new BucketDTO();

        bucket.setName(BUCKET_NAME);
        bucket.setDescription("This bucket analyzes stock quotes and trigger an event if the last " +
                "traded amount vary by 2 percent with regards to the average traded " +
                "price within past 2 minutes.");
        bucket.setEngineProvider("SiddhiCEPRuntime");
        return bucket;
    }

    private InputDTO createInput() {
        InputDTO input = new InputDTO();
        input.setTopic("AllStockQuotes");
        input.setBrokerName("localBroker");

        InputXMLMappingDTO mapping = new InputXMLMappingDTO();
        mapping.setStream("allStockQuotesStream");

        XpathDefinitionDTO xpathDefinition = new XpathDefinitionDTO();
        xpathDefinition.setPrefix("quotedata");
        xpathDefinition.setNamespace("http://ws.cdyne.com/");

        mapping.setXpathDefinition(new XpathDefinitionDTO[]{xpathDefinition});

        InputXMLPropertyDTO propertySymbol = new InputXMLPropertyDTO();
        propertySymbol.setName("symbol");
        propertySymbol.setXpath("//quotedata:StockQuoteEvent/quotedata:StockSymbol");
        propertySymbol.setType("java.lang.String");

        InputXMLPropertyDTO propertyPrice = new InputXMLPropertyDTO();
        propertyPrice.setName("price");
        propertyPrice.setXpath("//quotedata:StockQuoteEvent/quotedata:LastTradeAmount");
        propertyPrice.setType("java.lang.Double");

        mapping.setProperties(new InputXMLPropertyDTO[]{propertySymbol, propertyPrice});

        input.setInputXMLMappingDTO(mapping);
        return input;
    }

    private QueryDTO createQuery() {
        QueryDTO query = new QueryDTO();
        query.setName("StockDetector");

        ExpressionDTO expression = new ExpressionDTO();
        expression.setType("inline");
        expression.setText("from allStockQuotesStream#window.time(120000) " +
                "insert into fastMovingStockQuotesStream " +
                "symbol,avg(price) as avgPrice, price " +
                "group by symbol " +
                "having ((price > (avgPrice*1.02)) or ((avgPrice*0.98)>price ));");

        query.setExpression(expression);
        return query;
    }

    private OutputDTO createOutput() {
        OutputDTO output = new OutputDTO();
        output.setTopic("FastMovingStockQuotes");
        output.setBrokerName("localBroker");

        OutputXMLMappingDTO xmlMapping = new OutputXMLMappingDTO();
        xmlMapping.setMappingXMLText("<quotedata:StockQuoteDataEvent xmlns:quotedata=\"http://ws.cdyne.com/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "\t\t\t\t\t\t\t\t\t <quotedata:StockSymbol>{symbol}</quotedata:StockSymbol>\n" +
                "\t\t\t\t\t\t\t\t\t<quotedata:AvgLastTradeAmount>{avgPrice}</quotedata:AvgLastTradeAmount>\n" +
                "\t\t\t\t\t\t\t\t\t <quotedata:LastTradeAmount>{price}</quotedata:LastTradeAmount>\n" +
                "\t\t\t\t\t\t\t\t</quotedata:StockQuoteDataEvent>");

        output.setOutputXmlMapping(xmlMapping);

        return output;
    }


    @AfterClass(groups = {"wso2.cep"})
    public void removeBuckets() throws CEPAdminServiceCEPAdminException, RemoteException {

        cepAdminServiceClient.removeBucket(BUCKET_NAME);
        cepAdminServiceClient = null;

        testAgentServer = null;
    }
}
