package org.wso2.carbon.cep.sample;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;

import javax.naming.InitialContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.Properties;
import javax.naming.NamingException;
import javax.jms.*;

/**
 * Publish message to the topic created when defining Input
 */
public class Publisher {
    private static InitialContext initContext = null;
    private static TopicConnectionFactory topicConnectionFactory = null;


    public void publishMessages(String topic) {

        System.out.println("************");
        String xmlElement1 = "<quotedata:AllStockQuoteStream xmlns:quotedata=\"http://ws.cdyne.com/\">\n" +
                "                    <quotedata:StockQuoteEvent>\n" +
                "              <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>\n" +
                "              <quotedata:LastTradeAmount>126.36</quotedata:LastTradeAmount>\n" +
                "              <quotedata:StockChange>0.05</quotedata:StockChange>\n" +
                "              <quotedata:OpenAmount>25.05</quotedata:OpenAmount>\n" +
                "              <quotedata:DayHigh>25.46</quotedata:DayHigh>\n" +
                "              <quotedata:DayLow>25.01</quotedata:DayLow>\n" +
                "              <quotedata:StockVolume>20452658</quotedata:StockVolume>\n" +
                "              <quotedata:PrevCls>25.31</quotedata:PrevCls>\n" +
                "              <quotedata:ChangePercent>0.20</quotedata:ChangePercent>\n" +
                "              <quotedata:FiftyTwoWeekRange>22.73 - 31.58</quotedata:FiftyTwoWeekRange>\n" +
                "              <quotedata:EarnPerShare>2.326</quotedata:EarnPerShare>\n" +
                "              <quotedata:PE>10.88</quotedata:PE>\n" +
                "              <quotedata:CompanyName>Microsoft Corpora</quotedata:CompanyName>\n" +
                "              <quotedata:QuoteError>false</quotedata:QuoteError>\n" +
                "                    </quotedata:StockQuoteEvent>\n" +
                "                </quotedata:AllStockQuoteStream>";

        String xmlElement2 = "<quotedata:AllStockQuoteStream xmlns:quotedata=\"http://ws.cdyne.com/\">\n" +
                "                    <quotedata:StockQuoteEvent>\n" +
                "              <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>\n" +
                "              <quotedata:LastTradeAmount>36.36</quotedata:LastTradeAmount>\n" +
                "              <quotedata:StockChange>0.05</quotedata:StockChange>\n" +
                "              <quotedata:OpenAmount>25.05</quotedata:OpenAmount>\n" +
                "              <quotedata:DayHigh>25.46</quotedata:DayHigh>\n" +
                "              <quotedata:DayLow>25.01</quotedata:DayLow>\n" +
                "              <quotedata:StockVolume>20452658</quotedata:StockVolume>\n" +
                "              <quotedata:PrevCls>25.31</quotedata:PrevCls>\n" +
                "              <quotedata:ChangePercent>0.20</quotedata:ChangePercent>\n" +
                "              <quotedata:FiftyTwoWeekRange>22.73 - 31.58</quotedata:FiftyTwoWeekRange>\n" +
                "              <quotedata:EarnPerShare>2.326</quotedata:EarnPerShare>\n" +
                "              <quotedata:PE>10.88</quotedata:PE>\n" +
                "              <quotedata:CompanyName>Microsoft Corpora</quotedata:CompanyName>\n" +
                "              <quotedata:QuoteError>false</quotedata:QuoteError>\n" +
                "                    </quotedata:StockQuoteEvent>\n" +
                "                </quotedata:AllStockQuoteStream>";

        String xmlElement3 = "<quotedata:AllStockQuoteStream xmlns:quotedata=\"http://ws.cdyne.com/\">\n" +
                "                    <quotedata:StockQuoteEvent>\n" +
                "              <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>\n" +
                "              <quotedata:LastTradeAmount>236.36</quotedata:LastTradeAmount>\n" +
                "              <quotedata:StockChange>0.05</quotedata:StockChange>\n" +
                "              <quotedata:OpenAmount>25.05</quotedata:OpenAmount>\n" +
                "              <quotedata:DayHigh>25.46</quotedata:DayHigh>\n" +
                "              <quotedata:DayLow>25.01</quotedata:DayLow>\n" +
                "              <quotedata:StockVolume>20452658</quotedata:StockVolume>\n" +
                "              <quotedata:PrevCls>25.31</quotedata:PrevCls>\n" +
                "              <quotedata:ChangePercent>0.20</quotedata:ChangePercent>\n" +
                "              <quotedata:FiftyTwoWeekRange>22.73 - 31.58</quotedata:FiftyTwoWeekRange>\n" +
                "              <quotedata:EarnPerShare>2.326</quotedata:EarnPerShare>\n" +
                "              <quotedata:PE>10.88</quotedata:PE>\n" +
                "              <quotedata:CompanyName>Microsoft Corpora</quotedata:CompanyName>\n" +
                "              <quotedata:QuoteError>false</quotedata:QuoteError>\n" +
                "                    </quotedata:StockQuoteEvent>\n" +
                "                </quotedata:AllStockQuoteStream>";



        try {
            for (int i = 0; i < 4; i++) {
                XMLStreamReader reader1 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                        xmlElement1.getBytes()));
                StAXOMBuilder builder1 = new StAXOMBuilder(reader1);
                OMElement OMMessage1 = builder1.getDocumentElement();
                publish(topic, OMMessage1);

                XMLStreamReader reader2 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                        xmlElement2.getBytes()));
                StAXOMBuilder builder2 = new StAXOMBuilder(reader2);
                OMElement OMMessage2 = builder2.getDocumentElement();
                publish(topic, OMMessage2);

                XMLStreamReader reader3 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                        xmlElement3.getBytes()));
                StAXOMBuilder builder3 = new StAXOMBuilder(reader3);
                OMElement OMMessage3 = builder3.getDocumentElement();
                publish(topic, OMMessage3);

                System.out.println("Count :" + i);
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Publish message to given topic
     *
     * @param topicName - topic name to publish messages
     * @param message   - message to send
     */
    public void publish(String topicName, OMElement message) {

        Properties initialContextProperties = new Properties();
        initialContextProperties.put("java.naming.factory.initial",
                "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
        String connectionString = "amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:5672'";
        initialContextProperties.put("connectionfactory.qpidConnectionfactory", connectionString);

        try {
            InitialContext initialContext = new InitialContext(initialContextProperties);
            TopicConnectionFactory topicConnectionFactory =
                    (TopicConnectionFactory) initialContext.lookup("qpidConnectionfactory");
            TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
            TopicSession topicSession =
                    topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = topicSession.createTopic(topicName);
            TopicPublisher topicPublisher = topicSession.createPublisher(topic);

            TextMessage textMessage =
                    topicSession.createTextMessage(message.toString());

            topicPublisher.publish(textMessage);
            topicPublisher.close();


            topicSession.close();
            topicConnection.stop();
            topicConnection.close();


        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }



       /* // create topic connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            System.out.println("Can not create topic connection." + e);
            return;
        }

        // create session, producer, message and send message to given destination(topic)
        // OMElement message text is published here.
        Session session = null;
        try {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            TextMessage jmsMessage = session.createTextMessage(message.toString());
            producer.send(jmsMessage);
            producer.close();
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);

        }*/
    }


}
