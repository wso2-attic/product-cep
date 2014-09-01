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
package org.wso2.carbon.sample.atmstats;

import org.apache.axiom.om.util.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

public class ATMTransactionStatsClient {

    private static final List<String> xmlMsgs = new ArrayList<String>();

    static {
        String xmlElement1 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                " <atmdata:ATMTransactionStat>\n" +
                " <atmdata:CardNo>ED936784773</atmdata:CardNo>\n" +
                " <atmdata:CardHolderName>Mohan</atmdata:CardHolderName>\n" +
                " <atmdata:AmountWithdrawed>80</atmdata:AmountWithdrawed>\n" +
                " <atmdata:TransactionTime>PDT 11:00</atmdata:TransactionTime>\n" +
                " <atmdata:Location>Newyork</atmdata:Location>\n" +
                " <atmdata:BankName>Bank of Newyork</atmdata:BankName>\n" +
                " <atmdata:AccountNo>8957836745</atmdata:AccountNo>\n" +
                " <atmdata:CardHolderMobile>9667339388</atmdata:CardHolderMobile>\n" +
                " </atmdata:ATMTransactionStat>\n" +
                " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement1);

        String xmlElement2 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                " <atmdata:ATMTransactionStat>\n" +
                " <atmdata:CardNo>BC78946623</atmdata:CardNo>\n" +
                " <atmdata:CardHolderName>John</atmdata:CardHolderName>\n" +
                " <atmdata:AmountWithdrawed>1000</atmdata:AmountWithdrawed>\n" +
                " <atmdata:TransactionTime>PDT 01:00</atmdata:TransactionTime>\n" +
                " <atmdata:Location>California</atmdata:Location>\n" +
                " <atmdata:BankName>Bank of Wonder</atmdata:BankName>\n" +
                " <atmdata:AccountNo>PDT 12:00</atmdata:AccountNo>\n" +
                " <atmdata:CardHolderMobile>94729327932</atmdata:CardHolderMobile>\n" +
                " </atmdata:ATMTransactionStat>\n" +
                " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement2);

        String xmlElement3 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                " <atmdata:ATMTransactionStat>\n" +
                " <atmdata:CardNo>GH679893232</atmdata:CardNo>\n" +
                " <atmdata:CardHolderName>Tom</atmdata:CardHolderName>\n" +
                " <atmdata:AmountWithdrawed>900</atmdata:AmountWithdrawed>\n" +
                " <atmdata:TransactionTime>PDT 02:00</atmdata:TransactionTime>\n" +
                " <atmdata:Location>Texas</atmdata:Location>\n" +
                " <atmdata:BankName>Bank of Greenwich</atmdata:BankName>\n" +
                " <atmdata:AccountNo>783233422</atmdata:AccountNo>\n" +
                " <atmdata:CardHolderMobile>98434345532</atmdata:CardHolderMobile>\n" +
                " </atmdata:ATMTransactionStat>\n" +
                " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement3);

        String xmlElement4 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                " <atmdata:ATMTransactionStat>\n" +
                " <atmdata:CardNo>ED936784773</atmdata:CardNo>\n" +
                " <atmdata:CardHolderName>Mohan</atmdata:CardHolderName>\n" +
                " <atmdata:AmountWithdrawed>15000</atmdata:AmountWithdrawed>\n" +
                " <atmdata:TransactionTime>PDT 03:00</atmdata:TransactionTime>\n" +
                " <atmdata:Location>Newyork</atmdata:Location>\n" +
                " <atmdata:BankName>Bank of Newyork</atmdata:BankName>\n" +
                " <atmdata:AccountNo>8957836745</atmdata:AccountNo>\n" +
                " <atmdata:CardHolderMobile>9667339388</atmdata:CardHolderMobile>\n" +
                " </atmdata:ATMTransactionStat>\n" +
                " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement4);

    }

    public static void main(String[] args) throws XMLStreamException {
        System.out.println(xmlMsgs.get(1));
        System.out.println(xmlMsgs.get(2));
        System.out.println(xmlMsgs.get(3));
        KeyStoreUtil.setTrustStoreParams();
        String url = args[0];
        String username = args[1];
        String password = args[2];

        HttpClient httpClient = new SystemDefaultHttpClient();

        try {
            HttpPost method = new HttpPost(url);

            for (String xmlElement : xmlMsgs) {
                StringEntity entity = new StringEntity(xmlElement);
                method.setEntity(entity);
                if (url.startsWith("https")) {
                    processAuthentication(method, username, password);
                }
                httpClient.execute(method).getEntity().getContent().close();
            }
            Thread.sleep(500); // We need to wait some time for the message to be sent

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void processAuthentication(HttpPost method, String username, String password) {
        if (username != null && username.trim().length() > 0) {
            method.setHeader("Authorization", "Basic " + Base64.encode(
                    (username + ":" + password).getBytes()));
        }
    }

}
