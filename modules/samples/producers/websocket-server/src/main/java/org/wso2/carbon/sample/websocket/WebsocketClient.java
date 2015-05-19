/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.websocket;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;

@WebSocket
public class WebsocketClient {

    static RemoteEndpoint subscriber;
    static int subCount =0;

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
      public void onConnect(Session session) {
        subCount++;
        System.out.println("Connect: " + session.getRemoteAddress());
        if (subCount ==1) {
            subscriber = session.getRemote();
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        System.out.println("Message: " + message);
        try {
            subscriber.sendString("{\"event\": {\"metaData\":{\"timestamp\": 4354643,\"isPowerSaverEnabled\": false,\"sensorId\": 701,\"sensorName\": temperature},\"correlationData\": {\"longitude\": 4.504343,\"latitude\": 20.44345},\"payloadData\": {\"humidity\": 2.3,\"sensorValue\": 4.504343}}}");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t){
            t.printStackTrace();
        }
    }
}
