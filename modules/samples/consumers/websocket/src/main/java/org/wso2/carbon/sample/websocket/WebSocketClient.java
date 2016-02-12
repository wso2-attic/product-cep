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
import org.eclipse.jetty.util.component.LifeCycle;

import javax.websocket.*;
import java.net.URI;

public class WebSocketClient {
    private static Log log = LogFactory.getLog(WebSocketClient.class);
    private WebSocketContainer container;
    private Session session;

    public void connect(String url) {
        URI uri = URI.create(url);

        try {
            container = ContainerProvider.getWebSocketContainer();
            // Attempt Connect
            session = container.connectToServer(EventSocketClient.class, uri);

            if (session == null) {
                throw new RuntimeException("Cannot connect to url :" + url);
            }
        } catch (Throwable t) {
            log.error(t);
            if (container != null) {
                if (container instanceof LifeCycle) {
                    try {
                        ((LifeCycle) container).stop();
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            }
        }
    }

    public void stop() {

        if (container != null) {
            if (container instanceof LifeCycle) {
                try {
                    ((LifeCycle) container).stop();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
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
