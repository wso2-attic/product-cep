/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.websocket;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Http client reads a text file with multiple xml messages and post it to the given url.
 */
public class Websocket {
    private static Log log = LogFactory.getLog(Websocket.class);

    public static void main(String args[]) {


        //for server
        String host = args[0];
        String port = args[1];

        //for client
        String url = args[2];

        if (url.isEmpty()) {
            System.out.println("Starting Websocket receiver on Server Mode");

            WebSocketServer webSocketServer = new WebSocketServer();

            try {
                webSocketServer.start(Integer.parseInt(port), host);

                //To receive all the messages
                Thread.sleep(5000000);

            } catch (Throwable t) {
                log.error("Error when receiving the messages", t);
            } finally {
                webSocketServer.stop();
            }
        } else {
            System.out.println("Starting Websocket receiver on Client Mode");

            WebSocketClient webSocketClient = new WebSocketClient();
            try {
                webSocketClient.connect(url);

                //To receive all the messages
                Thread.sleep(5000000);

            } catch (Throwable t) {
                log.error("Error when receiver the messages", t);
            } finally {
                webSocketClient.stop();
            }
        }
    }
}

