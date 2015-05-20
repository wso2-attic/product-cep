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
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.*;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

public class WebSocketServer {

    private static String receivedMessage;
    private static Session session;
    private static final Log log = LogFactory.getLog(WebSocketServer.class);
    private Server server = null;

//    public static void main(String[] args) {
//        WebSocketServer webSocketServer = new WebSocketServer();
//        webSocketServer.start(9099);
//        webSocketServer.send("<events>\n" +
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
//                "</events>",30);
//        webSocketServer.stop();
////        System.out.println("-->"+webSocketServer.getReceivedMessage());
//    }

    public void start(int port) {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        try {
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            // Add WebSocket endpoint to javax.websocket layer
            wscontainer.addEndpoint(EventSocket.class);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.start();
                        server.join();
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            }).start();

        } catch (Throwable t) {
            log.error(t);
        }
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                log.error(e);
            }
            server = null;
        }
    }

    public void send(String message, int retryCount) {
        int count = 0;
        while (session == null && count < retryCount) {
            try {
                log.info("Waiting for the client to connect");
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("Error server sending message", e);
            }
        } else {
            log.error("Session not available to send message");
        }

    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    @ServerEndpoint(value = "/events/")
    public static class EventSocket {
        @OnOpen
        public void onWebSocketConnect(Session sess) {
            log.info("Server Socket Connected: " + sess);
            session = sess;
        }

        @OnMessage
        public void onWebSocketText(String message) {
            log.info("Server Received TEXT message: " + message);
            receivedMessage = message;
        }

        @OnClose
        public void onWebSocketClose(CloseReason reason) {
            log.info("Server Socket Closed: " + reason);
            session = null;
        }

        @OnError
        public void onWebSocketError(Throwable cause) {
            log.error("Server Socket Error", cause);
            session = null;
        }
    }


}
