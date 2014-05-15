Samples -  WSO2 Complex Event Processor
------------------------------------------------


There are six samples in WSO2 CEP Server.

 1. Stock Quote Analyzer sample using Siddhi Engine, Local broker and XML mapping,
 2. KPI Analyzer sample using Siddhi Engine, Agent broker and Tuple mapping,
 3. Twitter,StockQuote simple sample using Siddhi CEP Engine and JMS Broker
 4. Long Running Purchase Analyzer sample using Siddhi Engine in persistence mode, Agent Broker with Tuple mapping and JMS broker with XML Mapping
 5. Distributed Purchase Analyzer sample using Siddhi Engine in distributed mode, Agent Broker with Tuple mapping and JMS broker with XML Mapping

   1. Follow below steps to run StockQuote sample;

        Step 01: Unzip the CEP server (Do not start the server).

        Step 02: Run "ant deploy-xml" from wso2cep-3.1.0/samples directory to deploy required bucket.

        Step 03: Start the server.

        Step 04: Run "ant" from wso2cep-3.1.0/samples/services/FastMovingStockQuoteReceiverService directory to start the subscriber.

        Step 05: Go to Add menu under Topics menu to create a new topic. Provide the topic name as "FastMovingStockQuotes" and create the topic.
                     Then you will be redirected to Topic Browser page.

        Step 06: In the Topic browser page click on your topic name and then on subscribe link.

        Step 07: Then use following details to fill the form and click subscribe button.
                     Event Sink URL : http://localhost:9763/services/FastMovingStockQuoteService/getOMElement
                     Subscription Mode : Topic Only
                     Expiration Time : Some future date.

        Step 08: Run "ant xmlStockQuotePublisher" from wso2cep-3.1.0/samples directory to run the publisher to publish events to CEP.
                 Results will be displayed on CEP console.

                 <quotedata:StockQuoteDataEvent xmlns:quotedata="http://ws.cdyne.com/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                                     <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>
                                     <quotedata:AvgLastTradeAmount>20.68</quotedata:AvgLastTradeAmount>
                                     <quotedata:LastTradeAmount>15.0</quotedata:LastTradeAmount>
                                 </quotedata:StockQuoteDataEvent>
                 <quotedata:StockQuoteDataEvent xmlns:quotedata="http://ws.cdyne.com/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                                     <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>
                                     <quotedata:AvgLastTradeAmount>25.786666666666665</quotedata:AvgLastTradeAmount>
                                     <quotedata:LastTradeAmount>36.0</quotedata:LastTradeAmount>
                                 </quotedata:StockQuoteDataEvent>



   2. Follow below steps to run KPI Analyzer sample;

        Step 01: Unzip the CEP server (Do not start the server).

        Step 02: Run "ant deploy-agent" from wso2cep-3.1.0/samples directory to deploy required bucket.

        Step 03: Start the server.

        Step 04: Run "ant agentTestServer" in a separate terminal, from wso2cep-3.1.0/samples directory
                       to start the thrift output relieving server.

        Step 04: Run "ant agentPhoneRetailClient" in a separate terminal, from wso2cep-3.1.0/samples directory
                       to send access events via thrift to CEP server.

        Filtered Event results will be displayed on Test server CEP console.


   3. Follow below steps to run Twitter,StockQuote sample;

        Step 01: Configure and run ActiveMQ in your local machine

        Step 02: Unzip the CEP server (Do not start the server).

        Step 03: Copy paste activemq-all-xxx.jar from the ActiveMQ home directory to wso2cep-3.1.0/samples/lib directory

        Step 04: Copy paste activemq-core-xxx.jar and geronimo-j2ee-management_1.1_spec-1.0.1.jar from apache-activemq-xxx/lib
                to wso2cep-3.1.0/repository/components/lib directory

        Step 05: Run "ant deploy-jms" from wso2cep-3.1.0/samples directory to deploy required buckets.

        Step 06: Start the server.

        Step 07: Run "ant jmsSubscriber -Dtopic=PredictedStockQuotes" in a separate terminal, from wso2cep-3.1.0/samples directory
                to start the subscriber of "PredictedStockQuotes" topic.

        Step 08: Run "ant jmsAllStockQuotesPublisher" from wso2cep-3.1.0/samples directory
                to publish events to "AllStockQuotes" topic.

        Step 09: Run "ant jmsTwitterFeedPublisher" from wso2cep-3.1.0/samples directory
                to publish events to "TwitterFeed" topic.

        Output Events will be displayed on the JMS subscriber's console.


   4. Follow below steps to run Long Running Purchase Analyzer sample;
    
        Step 01: Configure and run ActiveMQ in your local machine
    
        Step 02: Unzip the CEP server (Do not start the server).
    
        Step 03: Copy paste activemq-all-xxx.jar from the ActiveMQ home directory to wso2cep-3.1.0/samples/lib directory
    
        Step 04: Copy paste activemq-core-xxx.jar and geronimo-j2ee-management_1.1_spec-1.0.1.jar from apache-activemq-xxx/lib
                to wso2cep-3.1.0/repository/components/lib directory

        Step 05: Run "ant deploy-persistence" from wso2cep-3.1.0/samples directory to deploy required buckets.

        Step 06: Start the server.

        Step 07: Run "ant jmsSubscriber -Dtopic=RetailSummary" in a separate terminal, from wso2cep-3.1.0/samples directory
                to start the subscriber of "RetailSummary" topic.

        Step 08: Run "ant agentPhoneRetailClient -Devents=5" from wso2cep-3.1.0/samples directory
                to publish events to CEP server.

        Step 09: Restart CEP

        Step 10: Run "ant agentPhoneRetailClient -Devents=1" from wso2cep-3.1.0/samples directory
                to publish events to CEP server.

        Step 11: Observe the output events in the JMS subscriber console, how the PurchaseOrders, QuantitySold and the RevenueEarned fields
                has incensed continuously from the last server start.

   5. Follow below steps to run Distributed Purchase Analyzer sample;

        Step 01: Configure and run ActiveMQ in your local machine

        Step 02: Unzip the 1st CEP server (Do not start the server).

        Step 03: Copy paste activemq-all-xxx.jar from the ActiveMQ home directory to wso2cep-3.1.0/samples/lib directory

        Step 04: Copy paste activemq-core-xxx.jar and geronimo-j2ee-management_1.1_spec-1.0.1.jar from apache-activemq-xxx/lib
                to wso2cep-3.1.0/repository/components/lib directory

        Step 05: Run "ant deploy-distributed" from wso2cep-3.1.0/samples directory to deploy required buckets.

        Step 06: Start the server.

        Step 07: Unzip the 2nd CEP server (Do not start the server).

        Step 08: Copy paste activemq-core-xxx.jar and geronimo-j2ee-management_1.1_spec-1.0.1.jar from apache-activemq-xxx/lib
                to wso2cep-3.1.0/repository/components/lib directory

        Step 09: Run "ant deploy-distributed" from wso2cep-3.1.0/samples directory to deploy required buckets.

        Step 10: Change the 2nd CEP server Offset from the file wso2cep-3.1.0-2/repository/conf/carbon.xml to 1 from 0, "e.g. <Offset>1</Offset>"

        Step 11: Start the 2nd server.

        Step 12: Run "ant jmsSubscriber -Dtopic=RetailSummary" in a separate terminal, from 1st server's wso2cep-3.1.0/samples directory
                to start the subscriber of "RetailSummary" topic.

        Step 13: Run "ant agentPhoneRetailClient -Dport=7611 -Devents=2" from the 1st server's wso2cep-3.1.0/samples directory
                to publish events to 1st CEP server.

        Step 14: Run "ant agentPhoneRetailClient -Dport=7612 -Devents=2" from the 1st server's wso2cep-3.1.0/samples directory
                to publish events to 2nd CEP server.

        Step 15: Observer the output events sent by both the CEP nodes in the JMS subscriber console and observe
                how the PurchaseOrders, QuantitySold and the RevenueEarned fields has incensed according to the input event.
