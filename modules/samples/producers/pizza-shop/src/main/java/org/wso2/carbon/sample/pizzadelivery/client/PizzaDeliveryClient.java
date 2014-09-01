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
package org.wso2.carbon.sample.pizzadelivery.client;

import org.apache.axiom.om.util.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;

public class PizzaDeliveryClient {
    public static void main(String[] args) {

        KeyStoreUtil.setTrustStoreParams();
        String url = args[0];
        String username = args[1];
        String password = args[2];

        HttpClient httpClient = new SystemDefaultHttpClient();

        try {
            HttpPost method = new HttpPost(url);

            if (httpClient != null) {
                String[] xmlElements = new String[]{"<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0023</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Card</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>",
                                                    "<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0024</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Card</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>2CYL Morris Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>",
                                                    "<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0025</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Cash</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>22RE Robinwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>",
                                                    "<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0026</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Card</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>"
                };

                try {
                    for (String xmlElement : xmlElements) {
                        StringEntity entity = new StringEntity(xmlElement);
                        method.setEntity(entity);
                        if (url.startsWith("https")) {
                            processAuthentication(method, username, password);
                        }
                        httpClient.execute(method).getEntity().getContent().close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(500); // We need to wait some time for the message to be sent

            }
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
