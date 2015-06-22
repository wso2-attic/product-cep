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

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class can be used to capture wire messages
 */
public class WireMonitorServer implements Runnable {

    private boolean active = false;
    private String response;

    private static final int TIMEOUT_VALUE_FOR_SERVER = 60000;
    private int port;
    private ServerSocket providerSocket;
    private Socket connection = null;

    private StringBuilder builder = null;
    private InputStream inputStream = null;

    private Log log = LogFactory.getLog(WireMonitorServer.class);

    /**
     * Start listening to a port
     *
     * @param port to be listened
     */
    public WireMonitorServer(int port) {
        this.port = port;
        this.active = true;
    }

    public void run() {
        try {
            // creating a server socket
            providerSocket = new ServerSocket(port, 10);

            log.info("Waiting for connection");
            connection = providerSocket.accept();
            log.info("Connection received from " +
                    connection.getInetAddress().getHostName());
            inputStream = connection.getInputStream();

            int ch;
            builder = new StringBuilder("");
            Long time = System.currentTimeMillis();
            while ((ch = inputStream.read()) != -1) {
                builder.append((char) ch);
                // In this case no need of reading more than timeout value
                if (System.currentTimeMillis() > (time + TIMEOUT_VALUE_FOR_SERVER)) {
                    break;
                }
            }

        } catch (IOException ioException) {
            if (active) {
                log.error("IO Exception: " + ioException.getMessage());
            }
        } finally {
            try {
                connection.close();
                providerSocket.close();
            } catch (Exception e) {
                log.error("Error when closing socket connection. " + e.getMessage(), e);
            }
        }
    }

    /**
     * Wait until response is received and returns
     *
     * @return will return empty string if response is not received
     */
    public String getCapturedMessage() {
        return response;
    }

    public void shutdown() {
        active = false;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            log.error("Error when closing socket connection. " + e.getMessage(), e);
        }
        response = builder.toString();
    }
}