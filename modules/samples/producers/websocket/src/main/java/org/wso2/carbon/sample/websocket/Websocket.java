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

package org.wso2.carbon.sample.websocket;


import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Websocket client reads a text file with multiple xml messages and post it to the given url.
 */
public class Websocket {
    private static Logger log = Logger.getLogger(Websocket.class);
    private static List<String> messagesList = new ArrayList<String>();
    private static BufferedReader bufferedReader = null;
    private static StringBuffer message = new StringBuffer("");
    private static final String asterixLine = "*****";

    public static void main(String args[]) {


        //for server
        String port = args[0];

        //for client
        String url = args[1];

        String filePath = args[2];
        String sampleNumber = args[3];

        //for host
        String host=args[4];

        if (url.isEmpty()) {
            System.out.println("Starting Websocket publisher on Server Mode");

            WebSocketServer webSocketServer = new WebSocketServer();

            try {
                filePath = Util.getMessageFilePath(sampleNumber, filePath, url);
                webSocketServer.start(Integer.parseInt(port),host);

                readMsg(filePath);

                for (String message : messagesList) {
                    System.out.println("Sending message:");
                    System.out.println(message);
                    System.out.println();
                    webSocketServer.send(message, 50);
                }
                Thread.sleep(500); // Waiting time for the message to be sent

            } catch (Throwable t) {
                log.error("Error when sending the messages", t);
            } finally {
                webSocketServer.stop();
            }
        } else  {
            System.out.println("Starting Websocket publisher on Client Mode");

            WebSocketClient webSocketClient = new WebSocketClient();
            try {
                webSocketClient.connect(url);

                filePath = Util.getMessageFilePath(sampleNumber, filePath, url);
                readMsg(filePath);

                for (String message : messagesList) {
                    System.out.println("Sending message:");
                    System.out.println(message);
                    System.out.println();
                    webSocketClient.send(message);
                }
                Thread.sleep(500); // Waiting time for the message to be sent

            } catch (Throwable t) {
                log.error("Error when sending the messages", t);
            } finally {
                webSocketClient.stop();
            }
        }
    }

    /**
     * Messages will be read from the given filepath and stored in the array list (messagesList)
     *
     * @param filePath Text file to be read
     */
    private static void readMsg(String filePath) {

        try {

            String line;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            while ((line = bufferedReader.readLine()) != null) {
                if ((line.equals(asterixLine.trim()) && !"".equals(message.toString().trim()))) {
                    messagesList.add(message.toString());
                    message = new StringBuffer("");
                } else {
                    message = message.append(String.format("\n%s", line));
                }
            }
            if (!"".equals(message.toString().trim())) {
                messagesList.add(message.toString());
            }

        } catch (FileNotFoundException e) {
            log.error("Error in reading file " + filePath, e);
        } catch (IOException e) {
            log.error("Error in reading file " + filePath, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when closing the file : " + e.getMessage(), e);
            }
        }

    }
}

