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

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import java.net.MalformedURLException;


public class PizzaPublisherClient {


    public static void main(String[] args)
            throws AgentException, MalformedURLException,
                   AuthenticationException, TransportException, MalformedStreamDefinitionException,
                   StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException {

        KeyStoreUtil.setTrustStoreParams();

        String host = args[0];
        String port = args[1];
        String username = args[2];
        String password = args[3];

        DataPublisher dataPublisher1 = new DataPublisher("tcp://" + host + ":" + port, username, password);

        String streamId1;
        try {
            streamId1 = dataPublisher1.findStream("orderStream", "1.0.0");
        } catch (NoStreamDefinitionExistException e) {
            streamId1 = dataPublisher1.defineStream("{" +
                                                  "  'name':'orderStream'," +
                                                  "  'version':'1.0.0'," +
                                                  "  'nickName': 'Analytics Statistics Information'," +
                                                  "  'description': 'Details of Analytics Statistics'," +
                                                  "  'payloadData':[" +
                                                  "          {'name':'orderNo','type':'STRING'}," +
                                                  "          {'name':'customerName','type':'STRING'}," +
                                                  "          {'name':'telephoneNo','type':'STRING'}," +
                                                  "          {'name':'orderInfo','type':'STRING'}," +
                                                  "          {'name':'orderedTime','type':'STRING'}" +
                                                  "  ]" +
                                                  "}");

        }
        Thread.sleep(1000);
        //In this case correlation data is null
        dataPublisher1.publish(streamId1, null, null, new Object[]{"Order1", "Tom","0122","Info1","Test"});
        dataPublisher1.publish(streamId1, null, null, new Object[]{"Order2", "John","07878","Info1","test"});
        dataPublisher1.publish(streamId1, null, null, new Object[]{"Order3", "Ratha","0784","Info1","test"});
        dataPublisher1.publish(streamId1, null, null, new Object[]{"Order4", "Isabelle","09921","Info1","test"});
        dataPublisher1.publish(streamId1, null, null, new Object[]{"Order5", "Paul","0989","Info1","test"});

        Thread.sleep(3000);
        dataPublisher1.stop();


        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same

        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username, password);

        String streamId;
        try {
            streamId = dataPublisher.findStream("deliveryStream", "1.0.0");
        } catch (NoStreamDefinitionExistException e) {
            streamId = dataPublisher.defineStream("{" +
                                                  "  'name':'deliveryStream'," +
                                                  "  'version':'1.0.0'," +
                                                  "  'nickName': 'Analytics Statistics Information'," +
                                                  "  'description': 'Details of Analytics Statistics'," +
                                                  "  'metaData':[" +
                                                  "          {'name':'deliveryNo','type':'STRING'}" +
                                                  "  ]," +
                                                  "  'payloadData':[" +
                                                  "          {'name':'orderNo','type':'STRING'}," +
                                                  "          {'name':'areaName','type':'STRING'}," +
                                                  "          {'name':'time','type':'LONG'}," +
                                                  "          {'name':'totalBill','type':'DOUBLE'}," +
                                                  "          {'name':'deliveredBy','type':'STRING'}" +
                                                  "  ]" +
                                                  "}");

        }
        Thread.sleep(2000);
        //In this case correlation data is null

        dataPublisher.publish(streamId, new Object[]{"Delivery1"}, null, new Object[]{"Order1", "Colombo",100l,50d,"Mohan"});
        Thread.sleep(8000);
        dataPublisher.publish(streamId, new Object[]{"Delivery2"}, null, new Object[]{"Order2", "Kandy",50l,70d,"Rajeev"});
        Thread.sleep(8000);
        dataPublisher.publish(streamId, new Object[]{"Delivery3"}, null, new Object[]{"Order3", "Galle",20l,700d,"Lasantha"});
        Thread.sleep(10000);
        dataPublisher.publish(streamId, new Object[]{"Delivery4"}, null, new Object[]{"Order4", "Colombo",200l,1000d,"Mohan"});
        Thread.sleep(10000);
        dataPublisher.publish(streamId, new Object[]{"Delivery5"}, null, new Object[]{"Order5", "Kandy",50l,600d,"Suho"});

        Thread.sleep(3000);
        dataPublisher.stop();

    }
}
