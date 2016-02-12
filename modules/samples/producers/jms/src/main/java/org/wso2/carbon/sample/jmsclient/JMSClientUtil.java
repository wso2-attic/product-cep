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
package org.wso2.carbon.sample.jmsclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JMSClientUtil {
    private static Log log = LogFactory.getLog(JMSClientUtil.class);
    static String sampleDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator +
                                        "samples" + File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator;

    /**
     * This method will construct the directory path of the data file
     *
     * @param sampleNumber Number of the sample which is running currently
     * @param format       Format of the file (ex: csv, txt)
     * @param topic        topic of the message to be sent (the data file should be named with the topic)
     * @param filePath     file path if a sample if not running
     */
    public static String getEventFilePath(String sampleNumber, String format, String topic, String filePath)
            throws Exception {
        if (sampleNumber != null && sampleNumber.length() == 0) {
            sampleNumber = null;
        }

        if (filePath != null && filePath.length() == 0) {
            filePath = null;
        }

        String resultingFilePath;
        if (filePath != null && sampleNumber == null) {
            resultingFilePath = filePath;
        } else if (filePath == null && sampleNumber != null) {
            if (format.equalsIgnoreCase("csv")) {
                resultingFilePath = sampleDirectoryPath.replace("sampleNumber", sampleNumber) + topic + ".csv";
            } else {
                resultingFilePath = sampleDirectoryPath.replace("sampleNumber", sampleNumber) + topic + ".txt";
            }
        } else {
            throw new Exception("In sampleNumber:'" + sampleNumber + "' and filePath:'" + filePath
                                + "' one must be null and other not null");
        }
        File file = new File(resultingFilePath);
        if (!file.isFile()) {
            throw new Exception("'" + resultingFilePath + "' is not a file");
        }
        if (!file.exists()) {
            throw new Exception("file '" + resultingFilePath + "' does not exist");
        }
        return resultingFilePath;
    }

    /**
     * Messages will be read from the given filepath and stored in the array list (messagesList)
     *
     * @param filePath Text file to be read
     */
    public static List<String> readFile(String filePath) {
        BufferedReader bufferedReader = null;
        StringBuffer message = new StringBuffer("");
        final String asterixLine = "*****";
        List<String> messagesList = new ArrayList<String>();
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
        return messagesList;
    }

    /**
     * Each message will be divided into groups and create the map message
     *
     * @param producer     Used for sending messages to a destination
     * @param session      Used to produce the messages to be sent
     * @param messagesList List of messages to be sent
     *                     individual message event data should be in
     *                     "attributeName(attributeType):attributeValue" format
     */
    public static void publishMapMessage(MessageProducer producer, Session session, List<String> messagesList)
            throws IOException, JMSException {
        String regexPattern = "(.*)\\((.*)\\):(.*)";
        Pattern pattern = Pattern.compile(regexPattern);
        for (String message : messagesList) {
            MapMessage mapMessage = session.createMapMessage();
            for (String line : message.split("\\n")) {
                if (line != null && !line.equalsIgnoreCase("")) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        mapMessage.setObject(matcher.group(1), parseAttributeValue(matcher.group(2), matcher.group(3)));
                    }
                }
            }
            producer.send(mapMessage);
        }
    }

    /**
     * Each message will be divided into groups and create the map message
     *
     * @param producer     Used for sending messages to a destination
     * @param session      Used to produce the messages to be sent
     * @param messagesList List of messages to be sent
     */
    public static void publishTextMessage(MessageProducer producer, Session session, List<String> messagesList)
            throws JMSException {
        for (String message : messagesList) {
            TextMessage jmsMessage = session.createTextMessage();
            jmsMessage.setText(message);
            producer.send(jmsMessage);
        }
    }

    private static Object parseAttributeValue(String type, String value) {
        switch (type) {
            case "bool":
                return Boolean.parseBoolean(value);
            case "int":
                return Integer.parseInt(value);
            case "long":
                return Long.parseLong(value);
            case "float":
                return Float.parseFloat(value);
            case "double":
                return Double.parseDouble(value);
        }
        return value;
    }

}
