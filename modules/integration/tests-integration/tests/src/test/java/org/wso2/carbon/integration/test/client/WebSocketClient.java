/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.integration.test.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.websocket.*;
import java.net.URI;

public class WebSocketClient {
    private static final Log log = LogFactory.getLog(WebSocketServer.class);

    private static String receivedMessage;

//    public static void main(String[] args) {
//        System.out.println(WebSocketClient.send("ws://localhost:8080/events/", "<events>\n" +
//                "    <event>\n" +
//                "        <metaData>\n" +
//                "            <timestamp>56783</timestamp>\n" +
//                "            <isPowerSaverEnabled>true</isPowerSaverEnabled>\n" +
//                "            <sensorId>4</sensorId>\n" +
//                "            <sensorName>data2</sensorName>\n" +
//                "        </metaData>\n" +
//                "        <correlationData>\n" +
//                "            <longitude>90.34344</longitude>\n" +
//                "            <latitude>1.23434</latitude>\n" +
//                "        </correlationData>\n" +
//                "        <payloadData>\n" +
//                "            <humidity>4.5</humidity>\n" +
//                "            <sensorValue>90.34344</sensorValue>\n" +
//                "        </payloadData>\n" +
//                "    </event>\n" +
//                "</events>"));
//
//    }

    public void send(String url, String message) {
        receivedMessage = null;
        URI uri = URI.create(url);

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            try {
                // Attempt Connect
                Session session = container.connectToServer(EventSocketClient.class, uri);
                // Send a message
                session.getBasicRemote().sendText(message);
                // Close session
                session.close();
            } finally {
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
                if (container instanceof LifeCycle) {
                    ((LifeCycle) container).stop();
                }
            }
        } catch (Throwable t) {
            log.error(t);
        }
    }

    public void receive(String url, int retryCount) {

        receivedMessage = null;
        final URI uri = URI.create(url);
        final int tryCount = retryCount;

        new Thread(new Runnable() {
            @Override

            public void run() {
                try {

                    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                    try {
                        // Attempt Connect
                        Session session = container.connectToServer(EventSocketClient.class, uri);

                        int count = 0;
                        while (count < tryCount && receivedMessage == null) {
                            log.info("Waiting for the sever to send message");
                            Thread.sleep(1000);
                        }

                        session.close();
                    } finally {
                        // Force lifecycle stop when done with container.
                        // This is to free up threads and resources that the
                        // JSR-356 container allocates. But unfortunately
                        // the JSR-356 spec does not handle lifecycles (yet)
                        if (container instanceof LifeCycle) {
                            ((LifeCycle) container).stop();
                        }
                    }
                } catch (Throwable t) {
                    log.error(t);
                }
            }
        }).start();

    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    @ClientEndpoint
    public static class EventSocketClient {
        @OnOpen
        public void onWebSocketConnect(Session sess) {
            log.info("Client Socket Connected: " + sess);
        }

        @OnMessage
        public void onWebSocketText(String message) {
            log.info("Client Received TEXT message: " + message);
            receivedMessage = message;
        }

        @OnClose
        public void onWebSocketClose(CloseReason reason) {
            log.info("Client Socket Closed: " + reason);
        }

        @OnError
        public void onWebSocketError(Throwable cause) {
            log.error("Client Socket Error", cause);
        }
    }

}
