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

package org.wso2.appserver.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub;
import org.wso2.carbon.event.receiver.stub.types.*;

import java.rmi.RemoteException;

public class EventReceiverAdminServiceClient {
    private static final Log log = LogFactory.getLog(EventReceiverAdminServiceClient.class);
    private final String serviceName = "EventReceiverAdminService";
    private EventReceiverAdminServiceStub eventReceiverAdminServiceStub;
    private String endPoint;

    public EventReceiverAdminServiceClient(String backEndUrl, String sessionCookie) throws
            AxisFault {
        this.endPoint = backEndUrl + serviceName;
        eventReceiverAdminServiceStub = new EventReceiverAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, eventReceiverAdminServiceStub);
    }

    public EventReceiverAdminServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        eventReceiverAdminServiceStub = new EventReceiverAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, eventReceiverAdminServiceStub);
    }

    public ServiceClient _getServiceClient() {
        return eventReceiverAdminServiceStub._getServiceClient();
    }

    public int getActiveEventReceiverCount()
            throws RemoteException {
        try {
            EventReceiverConfigurationInfoDto[] configs = eventReceiverAdminServiceStub.getAllActiveEventReceiverConfigurations();
            if (configs == null) {
                return 0;
            } else {
                return configs.length;
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }

    }

    public int getInactiveEventReceiverCount()
            throws RemoteException {
        try {
            EventReceiverConfigurationFileDto[] configs = eventReceiverAdminServiceStub.getAllInactiveEventReceiverConfigurations();
            if (configs == null) {
                return 0;
            } else {
                return configs.length;
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }

    }

    public int getEventReceiverCount()
            throws RemoteException {
        try {
            EventReceiverConfigurationFileDto[] configs = eventReceiverAdminServiceStub.getAllInactiveEventReceiverConfigurations();
            if (configs == null) {
                return getActiveEventReceiverCount();
            } else {
                return configs.length + getActiveEventReceiverCount();
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }

    }

    public void addWso2EventReceiverConfiguration(String eventReceiverName, String streamNameWithVersion,
                                                  String eventAdapterType,
                                                  EventMappingPropertyDto[] metaData,
                                                  EventMappingPropertyDto[] correlationData,
                                                  EventMappingPropertyDto[] payloadData,
                                                  BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                  boolean mappingEnabled,
                                                  String fromStreamNameWithVersion)
            throws RemoteException {
        try {
            eventReceiverAdminServiceStub.deployWso2EventReceiverConfiguration(eventReceiverName, streamNameWithVersion,
                    eventAdapterType, metaData, correlationData, payloadData, inputPropertyConfiguration, mappingEnabled,
                    fromStreamNameWithVersion);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public void addXmlEventReceiverConfiguration(String eventReceiverName,
                                                 String streamNameWithVersion,
                                                 String eventAdapterType,
                                                 String parentXpath,
                                                 EventMappingPropertyDto[] namespaces,
                                                 EventMappingPropertyDto[] inputMappings,
                                                 BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                 boolean mappingEnabled)
            throws RemoteException {
        try {
            eventReceiverAdminServiceStub.deployXmlEventReceiverConfiguration(eventReceiverName, streamNameWithVersion,
                    eventAdapterType, parentXpath, namespaces, inputMappings, inputPropertyConfiguration, mappingEnabled);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public boolean addTextEventReceiverConfiguration(String eventReceiverName,
                                                     String streamNameWithVersion,
                                                     String eventAdapterType,
                                                     EventMappingPropertyDto[] inputMappings,
                                                     BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                     boolean mappingEnabled) throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.deployTextEventReceiverConfiguration(eventReceiverName,
                    streamNameWithVersion, eventAdapterType, inputMappings, inputPropertyConfiguration, mappingEnabled);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public boolean addMapEventReceiverConfiguration(String eventReceiverName,
                                                    String streamNameWithVersion,
                                                    String eventAdapterType,
                                                    EventMappingPropertyDto[] inputMappings,
                                                    BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                    boolean mappingEnabled) throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.deployMapEventReceiverConfiguration(eventReceiverName,
                    streamNameWithVersion, eventAdapterType, inputMappings, inputPropertyConfiguration, mappingEnabled);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public boolean addJsonEventReceiverConfiguration(String eventRecieverName, String streamNameWithVersion,
                                                     String eventAdaptorType, EventMappingPropertyDto[] inputMappings,
                                                     BasicInputAdapterPropertyDto[] inputPrortyConfiguration,
                                                     boolean mappingEnabled) throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.deployJsonEventReceiverConfiguration(eventRecieverName,
                    streamNameWithVersion, eventAdaptorType, inputMappings, inputPrortyConfiguration, mappingEnabled);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }


    public void addEventReceiverConfiguration(String eventReceiverConfigurationXml)
            throws RemoteException {
        try {
            eventReceiverAdminServiceStub.deployEventReceiverConfiguration(eventReceiverConfigurationXml);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public void setTracingEnabled(String eventReceiverConfiguration, boolean flag)
            throws RemoteException {
        try {
            eventReceiverAdminServiceStub.setTracingEnabled(eventReceiverConfiguration, flag);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public void setStatisticsEnabled(String eventReceiverConfiguration, boolean flag)
            throws RemoteException {
        try {
            eventReceiverAdminServiceStub.setStatisticsEnabled(eventReceiverConfiguration, flag);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public void removeActiveEventReceiverConfiguration(String eventReceiverName)
            throws RemoteException {
        try {
            eventReceiverAdminServiceStub.undeployActiveEventReceiverConfiguration(eventReceiverName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public void removeInactiveEventReceiverConfiguration(String fileName)
            throws RemoteException {
        try {
            eventReceiverAdminServiceStub.undeployInactiveEventReceiverConfiguration(fileName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public EventReceiverConfigurationDto getActiveEventReceiverConfiguration(String eventReceiverName)
            throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.getActiveEventReceiverConfiguration(eventReceiverName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public String getEventReceiverConfigurationContent(
            String eventReceiverConfiguration)
            throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.getActiveEventReceiverConfigurationContent(eventReceiverConfiguration);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public EventReceiverConfigurationInfoDto[] getAllStreamSpecificActiveEventReceiverConfigurations(
            String streamId)
            throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.getAllStreamSpecificActiveEventReceiverConfigurations(streamId);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public String[] getAllInputAdapterTypes()
            throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.getAllInputAdapterTypes();
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public boolean editEventReceiverConfiguration(String eventReceiverConfiguration, String eventReceiverName)
            throws RemoteException {
        try {
            return eventReceiverAdminServiceStub.editActiveEventReceiverConfiguration(eventReceiverConfiguration, eventReceiverName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

}
