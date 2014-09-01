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
package org.wso2.carbon.integration.test.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.appserver.integration.common.clients.EventBuilderAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventFormatterAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventProcessorAdminServiceClient;
import org.wso2.appserver.integration.common.clients.EventStreamManagerAdminServiceClient;
import org.wso2.appserver.integration.common.clients.InputEventAdaptorManagerAdminServiceClient;
import org.wso2.appserver.integration.common.clients.OutputEventAdaptorManagerAdminServiceClient;
import org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.stub.types.EventInputPropertyConfigurationDto;
import org.wso2.carbon.event.builder.stub.types.PropertyDto;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.SiddhiConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto;
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto;

import java.rmi.RemoteException;

public class ConfigurationUtil {

    private static ConfigurationUtil configurationUtil;
    private EventFormatterAdminServiceClient eventFormatterAdminServiceClient;
    private EventBuilderAdminServiceClient eventBuilderAdminServiceClient;
    private EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    private InputEventAdaptorManagerAdminServiceClient inputEventAdaptorManagerAdminServiceClient;
    private OutputEventAdaptorManagerAdminServiceClient outputEventAdaptorManagerAdminServiceClient;
    private EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;

    private ConfigurationUtil() {
    }

    public static ConfigurationUtil getConfigurationUtil() {
        if (configurationUtil == null) {
            configurationUtil = new ConfigurationUtil();
        }

        return configurationUtil;
    }

    public EventStreamManagerAdminServiceClient getEventStreamManagerAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie) throws AxisFault {

        initEventStreamManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        return eventStreamManagerAdminServiceClient;
    }

    public EventFormatterAdminServiceClient getEventFormatterAdminServiceClient(String backendURL,
                                                                                String loggedInSessionCookie)
            throws AxisFault {

        initEventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);

        return eventFormatterAdminServiceClient;
    }

    public EventBuilderAdminServiceClient getEventBuilderAdminServiceClient(String backendURL,
                                                                            String loggedInSessionCookie)
            throws AxisFault {

        initEventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        return eventBuilderAdminServiceClient;
    }

    public EventProcessorAdminServiceClient getEventProcessorAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie)
            throws AxisFault {

        initEventProcessorAdminServiceClient(backendURL, loggedInSessionCookie);
        return eventProcessorAdminServiceClient;
    }

    public InputEventAdaptorManagerAdminServiceClient getInputEventAdaptorManagerAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie) throws AxisFault {

        initInputEventAdaptorAdminServiceClient(backendURL, loggedInSessionCookie);
        return inputEventAdaptorManagerAdminServiceClient;
    }

    public OutputEventAdaptorManagerAdminServiceClient getOutputEventAdaptorManagerAdminServiceClient(
            String backendURL,
            String loggedInSessionCookie) throws AxisFault {

        initOutputEventAdaptorAdminServiceClient(backendURL, loggedInSessionCookie);
        return outputEventAdaptorManagerAdminServiceClient;
    }

    private void initEventFormatterAdminServiceClient(String backendURL,
                                                      String loggedInSessionCookie)
            throws AxisFault {
        eventFormatterAdminServiceClient = new EventFormatterAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = eventFormatterAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    private void initEventBuilderAdminServiceClient(String backendURL,
                                                    String loggedInSessionCookie) throws AxisFault {
        eventBuilderAdminServiceClient = new EventBuilderAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = eventBuilderAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
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

    private void initOutputEventAdaptorAdminServiceClient(String backendURL,
                                                          String loggedInSessionCookie)
            throws AxisFault {
        outputEventAdaptorManagerAdminServiceClient = new OutputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = outputEventAdaptorManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    private void initInputEventAdaptorAdminServiceClient(String backendURL,
                                                         String loggedInSessionCookie)
            throws AxisFault {
        inputEventAdaptorManagerAdminServiceClient = new InputEventAdaptorManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = inputEventAdaptorManagerAdminServiceClient._getServiceClient();
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

    public void addStream(String streamName, String version, String usecase)
            throws RemoteException {
        EventStreamAttributeDto[] metaData = null;
        EventStreamAttributeDto[] correlationData = null;
        EventStreamAttributeDto[] payloadData = null;
        if (usecase.equals("JMS_TEXT")) {
            EventStreamAttributeDto clientIp = new EventStreamAttributeDto();
            clientIp.setAttributeName("IP_CLIENT");
            clientIp.setAttributeType("string");
            EventStreamAttributeDto timestamp = new EventStreamAttributeDto();
            timestamp.setAttributeName("timestamp");
            timestamp.setAttributeType("string");
            EventStreamAttributeDto url = new EventStreamAttributeDto();
            url.setAttributeName("url");
            url.setAttributeType("string");
            EventStreamAttributeDto httpCode = new EventStreamAttributeDto();
            httpCode.setAttributeName("http_code");
            httpCode.setAttributeType("string");
            EventStreamAttributeDto responseTime = new EventStreamAttributeDto();
            responseTime.setAttributeName("response_time");
            responseTime.setAttributeType("int");
            EventStreamAttributeDto filename = new EventStreamAttributeDto();
            filename.setAttributeName("filename");
            filename.setAttributeType("string");
            metaData = new EventStreamAttributeDto[]{filename};
            correlationData = new EventStreamAttributeDto[]{clientIp};
            payloadData = new EventStreamAttributeDto[]{timestamp, url, httpCode, responseTime};
        } else if (usecase.equals("JMS_TEXT_OUT")) {
            EventStreamAttributeDto filename = new EventStreamAttributeDto();
            filename.setAttributeName("filename");
            filename.setAttributeType("string");
            EventStreamAttributeDto timestamp = new EventStreamAttributeDto();
            timestamp.setAttributeName("access_time");
            timestamp.setAttributeType("string");
            EventStreamAttributeDto url = new EventStreamAttributeDto();
            url.setAttributeName("access_url");
            url.setAttributeType("string");
            EventStreamAttributeDto responseTime = new EventStreamAttributeDto();
            responseTime.setAttributeName("response_time");
            responseTime.setAttributeType("int");
            metaData = new EventStreamAttributeDto[]{filename};
            payloadData = new EventStreamAttributeDto[]{timestamp, url, responseTime};
        } else if (usecase.equals("HTTP_XML")) {
            EventStreamAttributeDto orderNo = new EventStreamAttributeDto();
            orderNo.setAttributeName("OrderNo");
            orderNo.setAttributeType("int");
            EventStreamAttributeDto type = new EventStreamAttributeDto();
            type.setAttributeName("Type");
            type.setAttributeType("string");
            EventStreamAttributeDto size = new EventStreamAttributeDto();
            size.setAttributeName("Size");
            size.setAttributeType("string");
            EventStreamAttributeDto quantity = new EventStreamAttributeDto();
            quantity.setAttributeName("Quantity");
            quantity.setAttributeType("int");
            EventStreamAttributeDto contact = new EventStreamAttributeDto();
            contact.setAttributeName("Contact");
            contact.setAttributeType("string");
            EventStreamAttributeDto address = new EventStreamAttributeDto();
            address.setAttributeName("Address");
            address.setAttributeType("string");
            correlationData = new EventStreamAttributeDto[]{orderNo};
            payloadData = new EventStreamAttributeDto[]{type, size, quantity, contact, address};
        } else {
            throw new UnsupportedOperationException("The stream attributes for use case :" + usecase + " is not defined.");
        }
        eventStreamManagerAdminServiceClient.addEventStream(streamName, version, metaData, correlationData, payloadData, "This is a  test stream", "access.logs");
    }

    public void addInEventStream() throws RemoteException {
        EventStreamAttributeDto payloadDto = new EventStreamAttributeDto();
        payloadDto.setAttributeName("testProperty");
        payloadDto.setAttributeType("string");

        EventStreamAttributeDto[] payloadData = new EventStreamAttributeDto[]{payloadDto};
        eventStreamManagerAdminServiceClient.addEventStream("InStream", "1.0.0", null, null, payloadData, "This is a input test stream", "test");
    }

    public void addOutEventStream() throws RemoteException {
        EventStreamAttributeDto payloadDto = new EventStreamAttributeDto();
        payloadDto.setAttributeName("testProperty");
        payloadDto.setAttributeType("string");

        EventStreamAttributeDto[] payloadData = new EventStreamAttributeDto[]{payloadDto};
        eventStreamManagerAdminServiceClient.addEventStream("OutStream", "1.0.0", null, null, payloadData, "This is a output test stream", "test");
    }

    public void addAnalyticsStream() throws RemoteException {
        EventStreamAttributeDto payloadDto = new EventStreamAttributeDto();
        payloadDto.setAttributeName("testProperty");
        payloadDto.setAttributeType("string");

        EventStreamAttributeDto[] payloadData = new EventStreamAttributeDto[]{payloadDto};
        eventStreamManagerAdminServiceClient.addEventStream("OutStream", "1.0.0", null, null, payloadData, "This is a output test stream", "test");
    }

    public void addWso2EventInputEventAdaptor(String name) throws RemoteException {
        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration(name, "wso2event", null);
    }

    public void addThriftInputEventAdaptor() throws RemoteException {
        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration("wso2EventReceiver", "wso2event", new InputEventAdaptorPropertyDto[0]);
    }

    public void addJmsInputEventAdaptor(String adaptorName) throws RemoteException {
        InputEventAdaptorPropertyDto namingProviderURL = new InputEventAdaptorPropertyDto();
        namingProviderURL.setKey("java.naming.provider.url");
        namingProviderURL.setValue("tcp://localhost:61616");
        InputEventAdaptorPropertyDto initialFactory = new InputEventAdaptorPropertyDto();
        initialFactory.setKey("java.naming.factory.initial");
        initialFactory.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        InputEventAdaptorPropertyDto connFactoryJndiName = new InputEventAdaptorPropertyDto();
        connFactoryJndiName.setKey("transport.jms.ConnectionFactoryJNDIName");
        connFactoryJndiName.setValue("TopicConnectionFactory");
        InputEventAdaptorPropertyDto destinationType = new InputEventAdaptorPropertyDto();
        destinationType.setKey("transport.jms.DestinationType");
        destinationType.setValue("topic");

        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration(adaptorName, "jms", new InputEventAdaptorPropertyDto[]{namingProviderURL, initialFactory, connFactoryJndiName, destinationType});
    }

    public void addHTTPInputEventAdaptor(String adaptorName) throws RemoteException {
        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration(adaptorName, "http", new InputEventAdaptorPropertyDto[0]);
    }

    public void addEventBuilder() throws RemoteException {
        PropertyDto streamName = new PropertyDto();
        streamName.setKey("stream");
        streamName.setValue("InStream");
        PropertyDto version = new PropertyDto();
        version.setKey("version");
        version.setValue("1.0.0");
        PropertyDto[] eventBuilderPropertyDtos = new PropertyDto[]{streamName, version};

        eventBuilderAdminServiceClient.addWso2EventBuilderConfiguration("testEventBuilder", "InStream:1.0.0", "wso2EventReceiver", "wso2event",
                                                                        new EventInputPropertyConfigurationDto[0], new EventInputPropertyConfigurationDto[0], new EventInputPropertyConfigurationDto[0], eventBuilderPropertyDtos, false);
    }

    public EventBuilderConfigurationDto createBasicEventBuilderConfiguration(String ebName)
            throws RemoteException {
        EventBuilderConfigurationDto eventBuilderConfigurationDto = new EventBuilderConfigurationDto();
        eventBuilderConfigurationDto.setEventBuilderConfigName(ebName);
        eventBuilderConfigurationDto.setToStreamName("org.wso2.test.inflow");
        eventBuilderConfigurationDto.setToStreamVersion("1.0.0");
        return eventBuilderConfigurationDto;
    }

    public static EventStreamAttributeDto createEventStreamAttributeDto(String fieldName, String dataType){
        EventStreamAttributeDto eventStreamAttribute = new EventStreamAttributeDto();
        eventStreamAttribute.setAttributeName(fieldName);
        eventStreamAttribute.setAttributeType(dataType);
        return eventStreamAttribute;
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

    public void addEventProcessor() throws RemoteException {
        ExecutionPlanConfigurationDto executionPlanConfigurationDto = new ExecutionPlanConfigurationDto();
        executionPlanConfigurationDto.setName("TestExecutionPlan1");
        StreamConfigurationDto inStream = new StreamConfigurationDto();
        inStream.setSiddhiStreamName("InStream");
        inStream.setStreamId("InStream:1.0.0");
        executionPlanConfigurationDto.setImportedStreams(new StreamConfigurationDto[]{inStream});

        SiddhiConfigurationDto siddhiPersistenceConfigDto = new SiddhiConfigurationDto();
        siddhiPersistenceConfigDto.setKey("siddhi.persistence.snapshot.time.interval.minutes");
        siddhiPersistenceConfigDto.setValue("0");
        executionPlanConfigurationDto.addSiddhiConfigurations(siddhiPersistenceConfigDto);

        executionPlanConfigurationDto.setQueryExpressions("from InStream select * insert into OutStream; ");

        StreamConfigurationDto outStream = new StreamConfigurationDto();
        outStream.setSiddhiStreamName("OutStream");
        outStream.setStreamId("OutStream:1.0.0");
        executionPlanConfigurationDto.setExportedStreams(new StreamConfigurationDto[]{outStream});

        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanConfigurationDto);
    }

    public void addEventFormatter() throws RemoteException {
        org.wso2.carbon.event.formatter.stub.types.PropertyDto eventFormatterPropertyDto = new org.wso2.carbon.event.formatter.stub.types.PropertyDto();
        eventFormatterPropertyDto.setKey("url");
        eventFormatterPropertyDto.setValue("http://localhost:9763/soapservice");
        org.wso2.carbon.event.formatter.stub.types.PropertyDto[] eventFormatterPropertyDtos = new org.wso2.carbon.event.formatter.stub.types.PropertyDto[]{eventFormatterPropertyDto};

        String xmlMapping = "<testData>{{testProperty}}</testData>";
        eventFormatterAdminServiceClient.addXMLEventFormatterConfiguration("xmlEventFormatter", "OutStream:1.0.0", "soap-sender", "soap", xmlMapping, eventFormatterPropertyDtos, "inline", true);
    }

    public void addOutputEventAdaptor() throws RemoteException {
        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration("soap-sender", "soap", new OutputEventAdaptorPropertyDto[0]);
    }

    public void addThriftOutputEventAdaptor() throws RemoteException {
        OutputEventAdaptorPropertyDto username = new OutputEventAdaptorPropertyDto();
        username.setKey("username");
        username.setValue("admin");
        OutputEventAdaptorPropertyDto password = new OutputEventAdaptorPropertyDto();
        password.setKey("password");
        password.setValue("admin");
        OutputEventAdaptorPropertyDto receiverUrl = new OutputEventAdaptorPropertyDto();
        receiverUrl.setKey("receiverURL");
        receiverUrl.setValue("tcp://localhost:7661");
        OutputEventAdaptorPropertyDto authenticatorURL = new OutputEventAdaptorPropertyDto();
        authenticatorURL.setKey("authenticatorURL");
        authenticatorURL.setValue("ssl://localhost:7761");

        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration("DefaultWSO2EventOutputAdaptor", "wso2event", new OutputEventAdaptorPropertyDto[]{username, password, receiverUrl, authenticatorURL});
    }

    public void addHTTPOutputEventAdaptor(String name) throws RemoteException {
        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration(name, "http", new OutputEventAdaptorPropertyDto[0]);
    }

    public void removeThriftOutputEventAdaptor() throws RemoteException {
        outputEventAdaptorManagerAdminServiceClient.removeActiveOutputEventAdaptorConfiguration("DefaultWSO2EventOutputAdaptor");
    }

    public void removeActiveInputEventAdaptor() throws RemoteException {
        inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("wso2EventReceiver");
    }

    public void removeActiveEventBuilder() throws RemoteException {
        eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("testEventBuilder");
    }

    public void removeActiveEventProcessor() throws RemoteException {
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("TestExecutionPlan1");
    }

    public void removeActiveEventFormatter() throws RemoteException {
        eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("xmlEventFormatter");
    }

    public void removeActiveOutputEventAdaptor() throws RemoteException {
        outputEventAdaptorManagerAdminServiceClient.removeActiveOutputEventAdaptorConfiguration("soap-sender");
    }

    public void removeInActiveInputEventAdaptor() throws RemoteException {
        inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("wso2EventReceiver");
    }

    public void removeInActiveEventBuilder() throws RemoteException {
        eventBuilderAdminServiceClient.removeInactiveEventBuilderConfiguration("testEventBuilder.xml");
    }

    public void removeInActiveEventProcessor() throws RemoteException {
        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("TestExecutionPlan1.xml");
    }

    public void removeInActiveEventFormatter() throws RemoteException {
        eventFormatterAdminServiceClient.removeInactiveEventFormatterConfiguration("xmlEventFormatter.xml");
    }

    public void removeInActiveOutputEventAdaptor()
            throws RemoteException {
        outputEventAdaptorManagerAdminServiceClient.removeInactiveOutputEventAdaptorConfiguration("soap-sender.xml");
    }


}
