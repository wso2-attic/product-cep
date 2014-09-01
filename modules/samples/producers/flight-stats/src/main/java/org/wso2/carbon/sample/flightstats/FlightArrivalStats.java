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
package org.wso2.carbon.sample.flightstats;

import javax.jms.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;

public class FlightArrivalStats {
    private static QueueConnectionFactory queueConnectionFactory = null;

    private static final List<String> xmlMsgs = new ArrayList();

    static {
        String xmlElement1 = "<flightdata:FlightArrivalStatsStream xmlns:flightdata=\"http://samples.wso2.org/\">\n" +
                             " <flightdata:FlightArrivalStat>\n" +
                             " <flightdata:FlightName>Airbus A09</flightdata:FlightName>\n" +
                             " <flightdata:FlightId>AEI891</flightdata:FlightId>\n" +
                             " <flightdata:FlightType>Airbus</flightdata:FlightType>\n" +
                             " <flightdata:From>California</flightdata:From>\n" +
                             " <flightdata:Airline>California Airlines</flightdata:Airline>\n" +
                             " <flightdata:Destination>Australia</flightdata:Destination>\n" +
                             " <flightdata:ArrivalTime>PDT 11:00</flightdata:ArrivalTime>\n" +
                             " <flightdata:TrackNo>Track 02</flightdata:TrackNo>\n" +
                             " <flightdata:Status>Delayed</flightdata:Status>\n" +
                             " </flightdata:FlightArrivalStat>\n" +
                             " </flightdata:FlightArrivalStatsStream>";

        xmlMsgs.add(xmlElement1);

        String xmlElement2 = "<flightdata:FlightArrivalStatsStream xmlns:flightdata=\"http://samples.wso2.org/\">\n" +
                             " <flightdata:FlightArrivalStat>\n" +
                             " <flightdata:FlightName>Airbus A10</flightdata:FlightName>\n" +
                             " <flightdata:FlightId>AEI856</flightdata:FlightId>\n" +
                             " <flightdata:FlightType>Airbus</flightdata:FlightType>\n" +
                             " <flightdata:From>Canbero</flightdata:From>\n" +
                             " <flightdata:Airline>National Airlines</flightdata:Airline>\n" +
                             " <flightdata:Destination>CapeTown</flightdata:Destination>\n" +
                             " <flightdata:ArrivalTime>PDT 12:00</flightdata:ArrivalTime>\n" +
                             " <flightdata:TrackNo>Track 03</flightdata:TrackNo>\n" +
                             " <flightdata:Status>On-Time</flightdata:Status>\n" +
                             " </flightdata:FlightArrivalStat>\n" +
                             " </flightdata:FlightArrivalStatsStream>";

        xmlMsgs.add(xmlElement2);

        String xmlElement3 = "<flightdata:FlightArrivalStatsStream xmlns:flightdata=\"http://samples.wso2.org/\">\n" +
                             " <flightdata:FlightArrivalStat>\n" +
                             " <flightdata:FlightName>Airbus B10</flightdata:FlightName>\n" +
                             " <flightdata:FlightId>BEI854</flightdata:FlightId>\n" +
                             " <flightdata:FlightType>Airbus</flightdata:FlightType>\n" +
                             " <flightdata:From>New-Delhi</flightdata:From>\n" +
                             " <flightdata:Airline>Colombo Airlines</flightdata:Airline>\n" +
                             " <flightdata:Destination>Newyork</flightdata:Destination>\n" +
                             " <flightdata:ArrivalTime>PDT 09:00</flightdata:ArrivalTime>\n" +
                             " <flightdata:TrackNo>Track 01</flightdata:TrackNo>\n" +
                             " <flightdata:Status>On-Time</flightdata:Status>\n" +
                             " </flightdata:FlightArrivalStat>\n" +
                             " </flightdata:FlightArrivalStatsStream>";

        xmlMsgs.add(xmlElement3);

    }

    public static void main(String[] args) throws XMLStreamException {

        queueConnectionFactory = JNDIContext.getInstance().getQueueConnectionFactory();
        FlightArrivalStats publisher = new FlightArrivalStats();
        String queueName = "";
        if (args.length == 0 || args[0] == null || args[0].trim().equals("")) {
            queueName = "FlightStats";
        } else {
            queueName = args[0];
        }

        publisher.publish(queueName, xmlMsgs);
        System.out.println("All Flight Messages sent");
    }

    /**
     * Publish message to given queue
     *
     * @param queueName - queue name to publish messages
     * @param msgList   - message to send
     */

    public void publish(String queueName, List<String> msgList) throws XMLStreamException {
        // create queue connection
        QueueConnection queueConnection = null;
        try {
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueConnection.start();
        } catch (JMSException e) {
            System.out.println("Can not create queue connection." + e);
            return;
        }
        // create session, producer, message and send message to given destination(queue)
        // OMElement message text is published here.
        Session session = null;
        try {
            session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);
            System.out.println("Sending XML messages on '" + queueName + "' queue");
            for (int i = 0, msgsLength = msgList.size(); i < msgsLength; i++) {
                String xmlMessage = msgList.get(i);
                XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                        xmlMessage.getBytes()));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement OMMessage = builder.getDocumentElement();
                TextMessage jmsMessage = session.createTextMessage(OMMessage.toString());
                producer.send(jmsMessage);
                System.out.println("Flight Arrival stat " + (i + 1) + " sent");
            }
            producer.close();
            session.close();
            queueConnection.stop();
            queueConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }
}