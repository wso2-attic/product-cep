/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cep.sample;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.broker.BrokerClientException;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationAdminServiceStub;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.stream.XMLStreamException;
import java.net.SocketException;
import java.rmi.RemoteException;

public class EsperLocalBrokerClient {

    private BrokerClient brokerClient;
    private String subscriptionID;


    public void getAccessKeys() {

        try {

            System.setProperty("javax.net.ssl.trustStore", "../../repository/resources/security/wso2carbon.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            //first login to the server
            String servicesString = "https://localhost:9443/services/";
            AuthenticationAdminServiceStub stub =
                    new AuthenticationAdminServiceStub(servicesString + "AuthenticationAdmin");
            stub._getServiceClient().getOptions().setManageSession(true);
            stub.login("admin", "admin", NetworkUtils.getLocalHostname());

            ServiceContext serviceContext = stub._getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);


            //create the broker client
            this.brokerClient = new BrokerClient("https://localhost:9443/services/EventBrokerService", "admin", "admin");

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (AuthenticationExceptionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    private void subscribe() {
        // subscribe with message box.
        try {
            this.subscriptionID = this.brokerClient.subscribe("FastMovingStockQuotesEsperLocal", "http://127.0.0.1:9763/services/FastMovingStockQuoteService/getOMElement");
        } catch (BrokerClientException e) {
            e.printStackTrace();
        }
    }


    private void unsubscribe() {

        try {
            this.brokerClient.unsubscribe(this.subscriptionID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }




    public static void main(String[] args) {

        EsperLocalBrokerClient messageBoxSubClient = new EsperLocalBrokerClient();
        messageBoxSubClient.getAccessKeys();
        messageBoxSubClient.subscribe();
        messageBoxSubClient.publishMessages();
        messageBoxSubClient.unsubscribe();
    }


    public void publishMessages() {
        ServiceClient serviceClient = null;
        try {
            serviceClient = new ServiceClient();
            serviceClient.setTargetEPR(new EndpointReference("http://localhost:9763/services/localBrokerService/AllStockQuotesEsperLocal"));
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        if (serviceClient != null) {
            String xmlElement1 = "<quotedata:AllStockQuoteStream xmlns:quotedata=\"http://ws.cdyne.com/\">\n" +
                    "                    <quotedata:StockQuoteEvent>\n" +
                    "              <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>\n" +
                    "              <quotedata:LastTradeAmount>99.55</quotedata:LastTradeAmount>\n" +
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
                    "              <quotedata:LastTradeAmount>151.36</quotedata:LastTradeAmount>\n" +
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
                    "              <quotedata:LastTradeAmount>69.98</quotedata:LastTradeAmount>\n" +
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


            OMElement omElement1 = null;
            OMElement omElement2 = null;
            OMElement omElement3 = null;
            try {
                omElement1 = AXIOMUtil.stringToOM(xmlElement1);
                omElement2 = AXIOMUtil.stringToOM(xmlElement2);
                omElement3 = AXIOMUtil.stringToOM(xmlElement3);
                for (int i = 0; i < 5; i++) {
                    serviceClient.fireAndForget(omElement1);
                    serviceClient.fireAndForget(omElement2);
                    serviceClient.fireAndForget(omElement3);
                    System.out.println(" element count = " + i * 3);
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }

    }


}
