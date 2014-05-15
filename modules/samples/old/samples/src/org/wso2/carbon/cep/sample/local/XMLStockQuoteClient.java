/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cep.sample.local;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import javax.xml.stream.XMLStreamException;

public class XMLStockQuoteClient {
    public static void main(String[] args) {
        ServiceClient serviceClient = null;
        try {
            serviceClient = new ServiceClient();
            Options options = new Options();
            options.setTo(new EndpointReference("http://localhost:9763/services/localBrokerService/AllStockQuotes"));
            serviceClient.setOptions(options);

            if (serviceClient != null) {
                String xmlElement1 = "<quotedata:AllStockQuoteStream xmlns:quotedata=\"http://ws.cdyne.com/\">\n"
                                     + "        <quotedata:StockQuoteEvent>\n"
                                     + "              <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>\n"
                                     + "              <quotedata:LastTradeAmount>26.36</quotedata:LastTradeAmount>\n"
                                     + "              <quotedata:StockChange>0.05</quotedata:StockChange>\n"
                                     + "              <quotedata:OpenAmount>25.05</quotedata:OpenAmount>\n"
                                     + "              <quotedata:DayHigh>25.46</quotedata:DayHigh>\n"
                                     + "              <quotedata:DayLow>25.01</quotedata:DayLow>\n"
                                     + "              <quotedata:StockVolume>20452658</quotedata:StockVolume>\n"
                                     + "              <quotedata:PrevCls>25.31</quotedata:PrevCls>\n"
                                     + "              <quotedata:ChangePercent>0.20</quotedata:ChangePercent>\n"
                                     + "              <quotedata:FiftyTwoWeekRange>22.73 - 31.58</quotedata:FiftyTwoWeekRange>\n"
                                     + "              <quotedata:EarnPerShare>2.326</quotedata:EarnPerShare>\n"
                                     + "              <quotedata:PE>10.88</quotedata:PE>\n"
                                     + "              <quotedata:CompanyName>Microsoft Corpora</quotedata:CompanyName>\n"
                                     + "              <quotedata:QuoteError>false</quotedata:QuoteError>\n"
                                     + "        </quotedata:StockQuoteEvent>\n"
                                     + "</quotedata:AllStockQuoteStream>";

                String xmlElement2 = "<quotedata:AllStockQuoteStream xmlns:quotedata=\"http://ws.cdyne.com/\">\n"
                                     + "        <quotedata:StockQuoteEvent>\n"
                                     + "              <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>\n"
                                     + "              <quotedata:LastTradeAmount>15</quotedata:LastTradeAmount>\n"
                                     + "              <quotedata:StockChange>0.05</quotedata:StockChange>\n"
                                     + "              <quotedata:OpenAmount>25.05</quotedata:OpenAmount>\n"
                                     + "              <quotedata:DayHigh>25.46</quotedata:DayHigh>\n"
                                     + "              <quotedata:DayLow>25.01</quotedata:DayLow>\n"
                                     + "              <quotedata:StockVolume>20452658</quotedata:StockVolume>\n"
                                     + "              <quotedata:PrevCls>25.31</quotedata:PrevCls>\n"
                                     + "              <quotedata:ChangePercent>0.20</quotedata:ChangePercent>\n"
                                     + "              <quotedata:FiftyTwoWeekRange>22.73 - 31.58</quotedata:FiftyTwoWeekRange>\n"
                                     + "              <quotedata:EarnPerShare>2.326</quotedata:EarnPerShare>\n"
                                     + "              <quotedata:PE>10.88</quotedata:PE>\n"
                                     + "              <quotedata:CompanyName>Microsoft Corpora</quotedata:CompanyName>\n"
                                     + "              <quotedata:QuoteError>false</quotedata:QuoteError>\n"
                                     + "        </quotedata:StockQuoteEvent>\n"
                                     + "</quotedata:AllStockQuoteStream>";

                String xmlElement3 = "<quotedata:AllStockQuoteStream xmlns:quotedata=\"http://ws.cdyne.com/\">\n"
                                     + "          <quotedata:StockQuoteEvent>\n"
                                     + "              <quotedata:StockSymbol>MSFT</quotedata:StockSymbol>\n"
                                     + "              <quotedata:LastTradeAmount>36</quotedata:LastTradeAmount>\n"
                                     + "              <quotedata:StockChange>0.05</quotedata:StockChange>\n"
                                     + "              <quotedata:OpenAmount>25.05</quotedata:OpenAmount>\n"
                                     + "              <quotedata:DayHigh>25.46</quotedata:DayHigh>\n"
                                     + "              <quotedata:DayLow>25.01</quotedata:DayLow>\n"
                                     + "              <quotedata:StockVolume>20452658</quotedata:StockVolume>\n"
                                     + "              <quotedata:PrevCls>25.31</quotedata:PrevCls>\n"
                                     + "              <quotedata:ChangePercent>0.20</quotedata:ChangePercent>\n"
                                     + "              <quotedata:FiftyTwoWeekRange>22.73 - 31.58</quotedata:FiftyTwoWeekRange>\n"
                                     + "              <quotedata:EarnPerShare>2.326</quotedata:EarnPerShare>\n"
                                     + "              <quotedata:PE>10.88</quotedata:PE>\n"
                                     + "              <quotedata:CompanyName>Microsoft Corpora</quotedata:CompanyName>\n"
                                     + "              <quotedata:QuoteError>false</quotedata:QuoteError>\n"
                                     + "         </quotedata:StockQuoteEvent>\n"
                                     + " </quotedata:AllStockQuoteStream>";

                OMElement omElement1 = null;
                OMElement omElement2 = null;
                OMElement omElement3 = null;
                try {
                    omElement1 = AXIOMUtil.stringToOM(xmlElement1);
                    omElement2 = AXIOMUtil.stringToOM(xmlElement2);
                    omElement3 = AXIOMUtil.stringToOM(xmlElement3);
                    serviceClient.fireAndForget(omElement1);
                    serviceClient.fireAndForget(omElement2);
                    serviceClient.fireAndForget(omElement3);
                    Thread.sleep(500); // We need to wait some time for the message to be sent
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (AxisFault axisFault) {
                    axisFault.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
