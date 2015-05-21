/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.integration.test.client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpEventReceiverClient {

    private static final Log log = LogFactory.getLog(HttpEventReceiverClient.class);

    private static String receivedMessage;

    public void receive(String url, String methodType
    ) {

        receivedMessage = null;
        final String urlValue = url;

        try {
            // Create an instance of HttpClient.
            HttpClient client = new SystemDefaultHttpClient();
            HttpRequestBase method = null;

            if(methodType.equals("GET")){
                method =new HttpGet(urlValue);
            } else{
                method =new HttpPost(urlValue);
            }

            HttpResponse httpResponse = client.execute(method);

            receivedMessage = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            log.info(receivedMessage);


        } catch (Throwable t) {
            log.error(t);
        }
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }
}
