/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.test.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

public class Wso2EventServer implements Runnable {
    private static Log log = LogFactory.getLog(Wso2EventServer.class);
    private ThriftDataReceiver thriftDataReceiver;
    private boolean eventReceived = false;
    private AtomicLong msgCount = new AtomicLong(0);
    private final String FILE_STREAM_DEFINTIONS_EXT = ".json";
    private String testCaseResourceFolderName;
    private int listeningPort;
    private List<Event> preservedEventList = null;
    private boolean isPreservingEvents;

    AbstractStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();


    public Wso2EventServer(String testCaseResourceFolderName, int listeningPort, boolean isPreservingEvents){
        this.testCaseResourceFolderName = testCaseResourceFolderName;
        this.listeningPort = listeningPort;
        this.isPreservingEvents = isPreservingEvents;
    }
    public void startServer() throws DataBridgeException, StreamDefinitionStoreException {
        msgCount.set(0);
        start(listeningPort);
    }
    public void start(int receiverPort) throws DataBridgeException,StreamDefinitionStoreException {
        KeyStoreUtil.setKeyStoreParams();
        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName,
                    String password) {
                return true;// allays authenticate to true
            }

            @Override
            public String getTenantDomain(String userName) {
                return "admin";
            }

            @Override
            public int getTenantId(String s) throws UserStoreException {
                return -1234;
            }

            @Override
            public void initContext(AgentSession agentSession) {

            }

            @Override
            public void destroyContext(AgentSession agentSession) {

            }

        }, streamDefinitionStore, getResourceFilePath("TestAgentServer","data-bridge-config.xml"));
        thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);

        for (StreamDefinition streamDefinition : loadStreamDefinitions()) {
            streamDefinitionStore.saveStreamDefinitionToStore(streamDefinition, -1234);
            log.info("StreamDefinition of '"+streamDefinition.getStreamId()+"' added to store");
        }

        databridge.subscribe(new AgentCallback() {

            @Override
            public void definedStream(StreamDefinition streamDefinition, int tenantId) {
                System.out.println("Added StreamDefinition " + streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int tenantId) {
                System.out.println("Removed StreamDefinition " + streamDefinition);
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                System.out.println("eventListSize=" + eventList.size() + " eventList " + eventList + " for username "
                        + credentials.getUsername());
                eventReceived = true;
                msgCount.addAndGet(eventList.size());
                if (isPreservingEvents){
                    if(preservedEventList==null){
                        preservedEventList = new ArrayList<>();
                    }
                    preservedEventList.addAll(eventList);
                }
            }

        });
        thriftDataReceiver.start("0.0.0.0");
        System.out.println("Test Server Started");

    }

    public void stop() {
        Assert.assertTrue(eventReceived);
        thriftDataReceiver.stop();
        System.out.println("Test Server Stopped");
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (DataBridgeException e) {
            e.printStackTrace();
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
        }
    }

    public long getMsgCount() {
        return msgCount.get();
    }

    public List<Event> getPreservedEventList(){
        return preservedEventList;
    }

    public List<StreamDefinition> loadStreamDefinitions() {
        String relativeFilePath = FrameworkPathUtil.getSystemResourceLocation() + CEPIntegrationTestConstants
                .RELATIVE_PATH_TO_TEST_ARTIFACTS + testCaseResourceFolderName;
        String directoryPath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));

        GenericExtFilter filter = new GenericExtFilter(FILE_STREAM_DEFINTIONS_EXT);
        File directory = new File(directoryPath);
        List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();
        if (!directory.exists()) {
            log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " directory not exist");
            return streamDefinitions;
        }
        if (!directory.isDirectory()) {
            log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " not a directory");
            return streamDefinitions;
        }

        // list out all the file name and filter by the extension
        String[] listStreamDefinitionFiles = directory.list(filter);

        if(listStreamDefinitionFiles != null){
            for (final String fileEntry : listStreamDefinitionFiles) {

                BufferedReader bufferedReader = null;
                StringBuilder stringBuilder = new StringBuilder();
                String fullPathToStreamDefinitionFile = directoryPath + "/"+ fileEntry;

                try {
                    bufferedReader = new BufferedReader(new FileReader(fullPathToStreamDefinitionFile));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    StreamDefinition streamDefinition = EventDefinitionConverterUtils
                            .convertFromJson(stringBuilder.toString().trim());
                    streamDefinitions.add(streamDefinition);
                } catch (FileNotFoundException e) {
                    log.error("Error in reading file " + fullPathToStreamDefinitionFile, e);
                } catch (IOException e) {
                    log.error("Error in reading file " + fullPathToStreamDefinitionFile, e);
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
        return streamDefinitions;
    }

    public String getResourceFilePath(String testCaseFolderName, String resourceFileName) {
        String relativeFilePath = FrameworkPathUtil.getSystemResourceLocation() + CEPIntegrationTestConstants
                .RELATIVE_PATH_TO_TEST_ARTIFACTS + testCaseFolderName +"/"+ resourceFileName;
        return relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
    }

    // inner class, generic extension filter
    public class GenericExtFilter implements FilenameFilter {

        private String ext;

        public GenericExtFilter(String ext) {
            this.ext = ext;
        }

        public boolean accept(File dir, String name) {
            return (name.endsWith(ext));
        }
    }
}