/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sample.soap;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Http client reads a text file with multiple xml messages and post it to the given url.
 */
public class SoapClient {

    private static Logger log = Logger.getLogger(SoapClient.class);
    private static List<String> messagesList = new ArrayList<String>();
    private static BufferedReader bufferedReader = null;
    private static StringBuffer message = new StringBuffer("");
    private static final String messageEndLine = "*****";

    public static void main(String[] args) {

        String url = args[0];
        String sampleNumber = args[1];
        String filePath = args[2];

        log.info("Starting WSO2 Soap Client");
        ServiceClient serviceClient;
        try {
            serviceClient = new ServiceClient();
            Options options = new Options();
            options.setTo(new EndpointReference(url));
            serviceClient.setOptions(options);

            if (serviceClient != null) {
                filePath = SoapClientUtil.getMessageFilePath(sampleNumber, filePath, url);
                readMsg(filePath);
                OMElement messageOMElement;

                try {
                    log.info("Starting sending of events...");
                    messageOMElement = AXIOMUtil.stringToOM(message.toString());
                    serviceClient.fireAndForget(messageOMElement);
                    log.info("Message sent");

                } catch (XMLStreamException e) {
                    log.error("Error occurred when sending message " + message.toString(), e);
                } catch (AxisFault axisFault) {
                    log.error("Error occurred when sending message " + message.toString(), axisFault);
                }
            }
        } catch (Throwable t) {
            log.error("Error occurred when connecting to endpoint " + url, t);
        }
    }

    /**
     * Xml messages will be read from the given filepath and stored in the array list (messagesList)
     *
     * @param filePath Text file to be read
     */
    private static void readMsg(String filePath) {

        try {

            String line;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            while ((line = bufferedReader.readLine()) != null) {
                if ((line.equals(messageEndLine.trim()) && !"".equals(message.toString().trim()))) {
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

