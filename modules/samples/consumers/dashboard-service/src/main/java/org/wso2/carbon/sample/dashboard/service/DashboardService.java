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
package org.wso2.carbon.sample.dashboard.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DashboardService {

    private Queue<String> stockPriceQueue = new ConcurrentLinkedQueue<String>();

    public String getStockQuote() {
        String stockQuote = stockPriceQueue.poll();
        return stockQuote != null ? stockQuote : "";
    }

    public void log(OMElement omElement) {
        System.out.println("Event received : " + omElement.toString());
    }

    public void addStockQuote(OMElement stockQuote) {
        try {
            AXIOMXPath symbolXPath = new AXIOMXPath("//stockQuoteEvent/stockSymbol");
            AXIOMXPath priceXPath = new AXIOMXPath("//stockQuoteEvent/stockPrice");
            String symbol = ((OMElement)symbolXPath.selectSingleNode(stockQuote)).getText();
            String price = ((OMElement)priceXPath.selectSingleNode(stockQuote)).getText();
            stockPriceQueue.offer(symbol + ":" + price);
        } catch (JaxenException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
