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
package org.wso2.carbon.integration.test.client;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.net.MalformedURLException;

public class PhoneRetailAgent {


    public static void publish()
            throws DataBridgeException, AgentException, MalformedURLException,
                   AuthenticationException, TransportException, MalformedStreamDefinitionException,
                   StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException {

        KeyStoreUtil.setTrustStoreParams();

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same

        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7611", "admin", "admin");

        String streamId;
        try {
            streamId = dataPublisher.findStream("analytics_Statistics", "1.3.0");
        } catch (NoStreamDefinitionExistException e) {
            streamId = dataPublisher.defineStream("{" +
                                                  "  'name':'analytics_Statistics'," +
                                                  "  'version':'1.3.0'," +
                                                  "  'nickName': 'Analytics Statistics Information'," +
                                                  "  'description': 'Details of Analytics Statistics'," +
                                                  "  'metaData':[" +
                                                  "          {'name':'ipAddress','type':'STRING'}" +
                                                  "  ]," +
                                                  "  'payloadData':[" +
                                                  "          {'name':'userID','type':'STRING'}," +
                                                  "          {'name':'searchTerms','type':'STRING'}" +
                                                  "  ]" +
                                                  "}");

        }
        Thread.sleep(1000);
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"192.168.1.1"}, null, new Object[]{"abc@org1.com", "CEP"});
        dataPublisher.publish(streamId, new Object[]{"192.168.1.1"}, null, new Object[]{"anne@org2.com", "CEP"});
        dataPublisher.publish(streamId, new Object[]{"192.168.1.3"}, null, new Object[]{"sam@org1.com", "CEP"});
        dataPublisher.publish(streamId, new Object[]{"192.168.1.2"}, null, new Object[]{"anne@org1.com", "CEP"});
        dataPublisher.publish(streamId, new Object[]{"192.168.1.3"}, null, new Object[]{"ann@org3.com", "CEP"});

        Thread.sleep(3000);
        dataPublisher.stop();
    }
}
