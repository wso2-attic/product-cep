/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.sample.jmsclient;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JMSClientUtil {
    private static Logger log = Logger.getLogger(JMSClientUtil.class);

    static String sampleDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + "samples" + File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator;

    static String configDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "eventstreams";

    public static String getEventFilePath(String sampleNumber, String format, String filePath) throws Exception {
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
            if(format.equalsIgnoreCase("csv")){
                resultingFilePath = sampleDirectoryPath.replace("sampleNumber", sampleNumber)+ "jmsReceiver.csv";
            }else{
                resultingFilePath = sampleDirectoryPath.replace("sampleNumber", sampleNumber)+ "jmsReceiver.txt";
            }
            //resultingFilePath = sampleDirectoryPath.replace("sampleNumber", sampleNumber); // + topic.replaceAll(":", "_").replaceAll("\\.", "_") + ".csv";
        } else {
            throw new Exception("In sampleNumber:'" + sampleNumber + "' and filePath:'" + filePath + "' one must be null and other not null");
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
     * Xml messages will be read from the given filepath and stored in the array list (messagesList)
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

    public static String readCSVFile(String filePath) {
        BufferedReader bufferedReader = null;
        StringBuilder builder = new StringBuilder("");
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return builder.toString();
    }

    public static List<Map<String, Object>> convertToMap(String fileContent) throws IOException {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        BufferedReader bufferedReader = null;
        try {
            for (String line : fileContent.split("\n")) {
                String[] data = line.split(",");
                Map<String, Object> dataMap = new HashMap<String, Object>();
                for (String entry : data) {
                    String[] keyValue = entry.trim().split(":");
                    if (keyValue.length == 2) {
                        dataMap.put(keyValue[0].trim(), keyValue[1].trim());
                    } else {
                        System.out.println("Wrong entry found:" + entry);
                    }
                }
                mapList.add(dataMap);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return mapList;
    }

    public static Map<String, StreamDefinition> loadStreamDefinitions(String sampleNumber) {
        String directoryPath;
        if (sampleNumber.length() != 0) {
            directoryPath = sampleDirectoryPath.replace("sampleNumber", sampleNumber)+ "eventstreams";
        } else {
            directoryPath = configDirectoryPath;
        }
        File directory = new File(directoryPath);
        Map<String, StreamDefinition> streamDefinitions = new HashMap<String, StreamDefinition>();
        if (!directory.exists()) {
            log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " directory not exist");
            return streamDefinitions;
        }
        if (!directory.isDirectory()) {
            log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " not a directory");
            return streamDefinitions;
        }
        File[] defFiles = directory.listFiles();

        if (defFiles != null) {
            for (final File fileEntry : defFiles) {
                if (!fileEntry.isDirectory()) {
                    BufferedReader bufferedReader = null;
                    StringBuilder stringBuilder = new StringBuilder();
                    try {
                        bufferedReader = new BufferedReader(new FileReader(fileEntry));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(stringBuilder.toString().trim());
                        streamDefinitions.put(streamDefinition.getStreamId(), streamDefinition);
                    } catch (FileNotFoundException e) {
                        log.error("Error in reading file " + fileEntry.getName(), e);
                    } catch (IOException e) {
                        log.error("Error in reading file " + fileEntry.getName(), e);
                    } catch (MalformedStreamDefinitionException e) {
                        log.error("Error in converting Stream definition " + e.getMessage(), e);
                    } finally {
                        try {
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                        } catch (IOException e) {
                            log.error("Error occurred when reading the file : " + e.getMessage(), e);
                        }
                    }
                }
            }
        }

        return streamDefinitions;

    }

    public static List<Map<String, Object>> convertFileToMap(StreamDefinition streamDefinition, String fileContent) {

        List<Map<String, Object>> eventList = new ArrayList<Map<String, Object>>();
        try {
            for (String line : fileContent.split("\n")) {
                String[] data = line.split(",");
                Map<String, Object> dataMap = new HashMap<String, Object>();

                int i = 0;
                if (streamDefinition.getMetaData() != null) {
                    for (Attribute at : streamDefinition.getMetaData()) {
                        dataMap.put(at.getName(), parseAttributeValue(at, data[i]));
                        i++;
                    }
                }

                if (streamDefinition.getCorrelationData() != null) {
                    for (Attribute at : streamDefinition.getCorrelationData()) {
                        dataMap.put(at.getName(), parseAttributeValue(at, data[i]));
                        i++;
                    }
                }

                if (streamDefinition.getPayloadData() != null) {
                    for (Attribute at : streamDefinition.getPayloadData()) {
                        dataMap.put(at.getName(), parseAttributeValue(at, data[i]));
                        i++;
                    }
                }
                eventList.add(dataMap);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return eventList;

    }

    private static Object parseAttributeValue(Attribute at, String value) {

        switch (at.getType()) {
            case BOOL:
                return Boolean.parseBoolean(value);
            case INT:
                return Integer.parseInt(value);
            case LONG:
                return Long.parseLong(value);
            case FLOAT:
                return Float.parseFloat(value);
            case DOUBLE:
                return Double.parseDouble(value);
        }
        return value;
    }

}
