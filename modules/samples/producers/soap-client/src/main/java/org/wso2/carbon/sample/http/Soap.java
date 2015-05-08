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

package org.wso2.carbon.sample.http;


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
public class Soap {

    private static Logger log = Logger.getLogger(Soap.class);
    private static List<String> messagesList = new ArrayList<String>();
    private static BufferedReader bufferedReader = null;
    private static StringBuffer message = new StringBuffer("");
    private static final String asterixLine = "*****";

    public static void main(String[] args) {


        String url = args[0];
        String sampleNumber = args[1];
        String filePath = args[2];

        System.out.println("Starting WSO2 Soap Client");

        long totalEventCount = 2000000L;
        if (args.length >= 1) {
            totalEventCount = Long.valueOf(args[0]);
        }

        ServiceClient serviceClient = null;
        try {
            serviceClient = new ServiceClient();
            Options options = new Options();
            options.setTo(new EndpointReference(url));
            serviceClient.setOptions(options);

            if (serviceClient != null) {


                filePath = SoapUtil.getMessageFilePath(sampleNumber, filePath);
                readMsg(filePath);


                OMElement omElement1;

                try {
                    System.out.println("Starting sending of events...");
                    long startTime = System.nanoTime();
                    for (int i = 0; i < totalEventCount; i++) {
                        omElement1 = AXIOMUtil.stringToOM(message.toString());
                        serviceClient.fireAndForget(omElement1);
                        if ((i + 1) % 100 == 0) {
                            long elapsedTime = System.nanoTime() - startTime;
                            double timeInSec = elapsedTime / 1000000000D;
                            double throughputPerSec = (i + 1) * 3 / timeInSec;
                            System.out.println("Sent " + (i + 1) * 3 + " events in " + timeInSec
                                    + " seconds with total throughput of " + throughputPerSec + " events per second.");
                        }
                    }
                    Thread.sleep(500); // We need to wait some time for the message to be sent
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (AxisFault axisFault) {
                    axisFault.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
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

