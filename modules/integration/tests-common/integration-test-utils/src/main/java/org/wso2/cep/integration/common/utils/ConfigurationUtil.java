/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.cep.integration.common.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.appserver.integration.common.clients.*;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;

public class ConfigurationUtil {

    private static ConfigurationUtil configurationUtil;
    private EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    private EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;
    private EventReceiverAdminServiceClient eventReceiverAdminServiceClient;
    private EventPublisherAdminServiceClient eventPublisherAdminServiceClient;
    private ExecutionManagerAdminServiceClient executionManagerAdminServiceClient;

    private ConfigurationUtil() {
    }

    public static ConfigurationUtil getConfigurationUtil() {
        if (configurationUtil == null) {
            configurationUtil = new ConfigurationUtil();
        }
        return configurationUtil;
    }

    public EventReceiverAdminServiceClient getEventReceiverAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie) throws AxisFault {
        initEventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        return eventReceiverAdminServiceClient;
    }

    public EventPublisherAdminServiceClient getEventPublisherAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie) throws AxisFault {
        initEventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
        return eventPublisherAdminServiceClient;
    }

    public EventStreamManagerAdminServiceClient getEventStreamManagerAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie) throws AxisFault {

        initEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        return eventStreamManagerAdminServiceClient;
    }

    public EventProcessorAdminServiceClient getEventProcessorAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie)
            throws AxisFault {

        initEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        return eventProcessorAdminServiceClient;
    }

    public ExecutionManagerAdminServiceClient getExecutionManagerAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie)
            throws AxisFault {

        initExecutionManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        return executionManagerAdminServiceClient;
    }

    private void initEventProcessorAdminServiceClient(String backendURL,
                                                      String loggedInSessionCookie)
            throws AxisFault {
        eventProcessorAdminServiceClient = new EventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = eventProcessorAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    private void initEventStreamManagerAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie)
            throws AxisFault {
        eventStreamManagerAdminServiceClient = new EventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = eventStreamManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    private void initEventReceiverAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie)
            throws AxisFault {
        eventReceiverAdminServiceClient = new EventReceiverAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = eventReceiverAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    private void initEventPublisherAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie)
            throws AxisFault {
        eventPublisherAdminServiceClient = new EventPublisherAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = eventPublisherAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    private void initExecutionManagerAdminServiceClient(String backendURL,
                                                        String loggedInSessionCookie)
            throws AxisFault {
        executionManagerAdminServiceClient = new ExecutionManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = executionManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    public static EventStreamAttributeDto createEventStreamAttributeDto(String fieldName, String dataType) {
        EventStreamAttributeDto eventStreamAttribute = new EventStreamAttributeDto();
        eventStreamAttribute.setAttributeName(fieldName);
        eventStreamAttribute.setAttributeType(dataType);
        return eventStreamAttribute;
    }

    public void removeActiveEventProcessor() throws RemoteException {
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("TestExecutionPlan1");
    }

    public static String readFile(String path) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    /*
    public void configureForJms(EventBuilderConfigurationDto eventBuilderConfigurationDto, String topic) throws RemoteException {
        eventBuilderConfigurationDto.setInputEventAdaptorName("jmsEventReceiver");
        eventBuilderConfigurationDto.setInputEventAdaptorType("jms");
        eventBuilderConfigurationDto.setTraceEnabled(true);
        EventBuilderPropertyDto jmsDestination = new EventBuilderPropertyDto();
        jmsDestination.setKey("transport.jms.Destination_from");
        jmsDestination.setValue(topic);

        EventBuilderPropertyDto[] eventBuilderPropertyDtos = eventBuilderConfigurationDto.getEventBuilderProperties();
        int length = 1;
        if (eventBuilderPropertyDtos == null) {
            eventBuilderPropertyDtos = new EventBuilderPropertyDto[length];
        } else {
            length += eventBuilderPropertyDtos.length;
            EventBuilderPropertyDto[] temp = eventBuilderPropertyDtos.clone();
            eventBuilderPropertyDtos = new EventBuilderPropertyDto[length];
            System.arraycopy(temp, 0, eventBuilderPropertyDtos, 0, length - 1);
        }

        eventBuilderPropertyDtos[length - 1] = jmsDestination;
        eventBuilderConfigurationDto.setEventBuilderProperties(eventBuilderPropertyDtos);
    }
    */

    /*
    public void configureTextMapping(EventBuilderConfigurationDto eventBuilderConfigurationDto) throws RemoteException {

        eventBuilderConfigurationDto.setInputMappingType("text");

        String regex1 = "^([\\d.]+) \\S+ \\S+ \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \\\"(.+?)\\\" (\\d{3}) ([\\d.]+|-) .*";
        String regex2 = "fileName:\\s+(\\S+)";
        EventBuilderPropertyDto correlationIPClient = new EventBuilderPropertyDto();
        correlationIPClient.setKey("correlation_IP_CLIENT_mapping");
        correlationIPClient.setValue(regex1);
        correlationIPClient.setPropertyType("string");
        EventBuilderPropertyDto timestamp = new EventBuilderPropertyDto();
        timestamp.setKey("timestamp_mapping");
        timestamp.setValue(regex1);
        timestamp.setPropertyType("string");
        EventBuilderPropertyDto url = new EventBuilderPropertyDto();
        url.setKey("url_mapping");
        url.setValue(regex1);
        url.setPropertyType("string");
        EventBuilderPropertyDto httpCode = new EventBuilderPropertyDto();
        httpCode.setKey("http_code_mapping");
        httpCode.setValue(regex1);
        httpCode.setPropertyType("string");
        EventBuilderPropertyDto responseTime = new EventBuilderPropertyDto();
        responseTime.setKey("response_time_mapping");
        responseTime.setValue(regex1);
        responseTime.setPropertyType("int");

        EventBuilderPropertyDto filename = new EventBuilderPropertyDto();
        filename.setKey("meta_filename_mapping");
        filename.setValue(regex2);
        filename.setPropertyType("string");

        EventBuilderPropertyDto[] eventBuilderPropertyDtos = eventBuilderConfigurationDto.getEventBuilderProperties();
        int length = 6;
        if (eventBuilderPropertyDtos == null) {
            eventBuilderPropertyDtos = new EventBuilderPropertyDto[length];
        } else {
            length += eventBuilderPropertyDtos.length;
            EventBuilderPropertyDto[] temp = eventBuilderPropertyDtos.clone();
            eventBuilderPropertyDtos = new EventBuilderPropertyDto[length];
            System.arraycopy(temp, 0, eventBuilderPropertyDtos, 0, length - 6);
        }

        eventBuilderPropertyDtos[length - 6] = correlationIPClient;
        eventBuilderPropertyDtos[length - 5] = timestamp;
        eventBuilderPropertyDtos[length - 4] = url;
        eventBuilderPropertyDtos[length - 3] = httpCode;
        eventBuilderPropertyDtos[length - 2] = responseTime;
        eventBuilderPropertyDtos[length - 1] = filename;

        eventBuilderConfigurationDto.setEventBuilderProperties(eventBuilderPropertyDtos);

    }
    */
}
