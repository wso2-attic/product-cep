package org.wso2.carbon.cep.bucket.test;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.cep.CEPAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPAdminException;
import org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPConfigurationException;
import org.wso2.carbon.cep.stub.admin.internal.xsd.*;

import java.rmi.RemoteException;

//import org.wso2.carbon.cep.stub.admin.internal.xsd.*;

/**
 * Check whether CEPAdminService properly creates SiddhiBucket to be used with wsEventBroker
 */
public class WSEventSiddhiBucketCreationTestCase {
    private static final Log log = LogFactory.getLog(WSEventSiddhiBucketCreationTestCase.class);
    private CEPAdminServiceClient cepAdminServiceClient;

    private ManageEnvironment environment;
    private int userId = 0;
    private UserInfo userInfo;


    @BeforeClass(groups = {"wso2.cep"})
    public void init() throws Exception {
        log.info("Initializing Tests for WSEventSiddhiBucketCreationTestCase");

        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().cep(userId);
        environment = builder.build();

        cepAdminServiceClient =
                new CEPAdminServiceClient(environment.getCep().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());

        ServiceClient client = cepAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, environment.getCep().getSessionCookie());


    }


    @Test(groups = {"wso2.cep"}, dependsOnGroups = {"wso2.cep.broker"})
    public void wsEventSiddhiBucketCreationTest()
            throws CEPAdminServiceCEPConfigurationException, RemoteException,
                   CEPAdminServiceCEPAdminException, InterruptedException {
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
        Thread.sleep(25000);
        Assert.assertEquals(numberOfBuckets+1, cepAdminServiceClient.getAllBucketCount());
    }

    private BucketDTO createBucket() {
        BucketDTO bucket = new BucketDTO();

        bucket.setName("StockQuoteAnalyzerWSEvent");
        bucket.setDescription("his bucket analyzes stock quotes and trigger an event if the last\n" +
                              "\t\t\t\t\t\t  traded amount vary by 2 percent with regards to the average traded\n" +
                              "\t\t\t\t\t\t  price within past 2 minutes.");
        bucket.setEngineProvider("SiddhiCEPRuntime");
        return bucket;
    }

    private InputDTO createInput() {
        InputDTO input = new InputDTO();
        input.setTopic("AllStockQuotes");
        input.setBrokerName("localBroker");

        InputXMLMappingDTO mapping = new InputXMLMappingDTO();
        mapping.setStream("allStockQuotes");

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
        query.setName("Conditional Stocks Detector");

        ExpressionDTO expression = new ExpressionDTO();
        expression.setType("inline");
        expression.setText("from allStockQuotes[price>100]" +
                           "insert into outStream symbol, price, avg(price) as avgPrice;");

        query.setExpression(expression);
        return query;
    }

    private OutputDTO createOutput() {
        OutputDTO output = new OutputDTO();
        output.setTopic("FastMovingStockQuotes");
        output.setBrokerName("wsEventBroker");

        OutputXMLMappingDTO xmlMapping = new OutputXMLMappingDTO();
        xmlMapping.setMappingXMLText("<quotedata:StockQuoteDataEvent xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                     "\t\t\t\t\t\t\t\txmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                     "\t\t\t\t\t\t\t\txmlns:quotedata=\"http://ws.cdyne.com/\">\n" +
                                     "\t\t\t\t\t\t\t\t\t<quotedata:StockSymbol>{symbol}</quotedata:StockSymbol>\n" +
                                     "\t\t\t\t\t\t\t\t\t<quotedata:AvgLastTradeAmount>{average}</quotedata:AvgLastTradeAmount>\n" +
                                     "\t\t\t\t\t\t\t\t\t<quotedata:LastTradeAmount>{price}</quotedata:LastTradeAmount>\n" +
                                     "\t\t\t\t\t\t\t\t</quotedata:StockQuoteDataEvent>");

        output.setOutputXmlMapping(xmlMapping);


        return output;
    }

    @AfterClass(groups = {"wso2.cep"})
    public void removeBuckets() throws Exception {
        cepAdminServiceClient.removeBucket("StockQuoteAnalyzerWSEvent");
        cepAdminServiceClient = null;
    }


}
