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
package org.wso2.appserver.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub;
import org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationFileDto;
import org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationInfoDto;
import org.wso2.carbon.event.builder.stub.types.EventInputPropertyConfigurationDto;
import org.wso2.carbon.event.builder.stub.types.PropertyDto;

import java.rmi.RemoteException;

public class EventBuilderAdminServiceClient {
    private static final Log log = LogFactory.getLog(EventBuilderAdminServiceClient.class);
    private final String serviceName = "EventBuilderAdminService";
    private EventBuilderAdminServiceStub eventBuilderAdminServiceStub;
    private String endPoint;

    public EventBuilderAdminServiceClient(String backEndUrl, String sessionCookie) throws
                                                                                   AxisFault {
        this.endPoint = backEndUrl + serviceName;
        eventBuilderAdminServiceStub = new EventBuilderAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, eventBuilderAdminServiceStub);

    }

    public EventBuilderAdminServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        eventBuilderAdminServiceStub = new EventBuilderAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, eventBuilderAdminServiceStub);
    }

    public ServiceClient _getServiceClient() {
        return eventBuilderAdminServiceStub._getServiceClient();
    }

    public int getActiveEventBuilderCount()
            throws RemoteException {
        try {
            EventBuilderConfigurationInfoDto[] configs = eventBuilderAdminServiceStub.getAllActiveEventBuilderConfigurations();
            if (configs == null) {
                return 0;
            } else {
                return configs.length;
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }

    }

    public int getEventBuilderCount()
            throws RemoteException {
        try {
            EventBuilderConfigurationFileDto[] configs = eventBuilderAdminServiceStub.getAllInactiveEventBuilderConfigurations();
            if (configs == null) {
                return getActiveEventBuilderCount();
            } else {
                return configs.length + getActiveEventBuilderCount();
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }

    }

    public void addWso2EventBuilderConfiguration(String eventBuilderName, String streamId,
                                                 String transportAdaptorName,
                                                 String transportAdaptorType,
                                                 EventInputPropertyConfigurationDto[] metaData,
                                                 EventInputPropertyConfigurationDto[] correlationData,
                                                 EventInputPropertyConfigurationDto[] payloadData,
                                                 PropertyDto[] eventBuilderPropertyDtos,
                                                 boolean mappingEnabled)
            throws RemoteException {
        try {
            eventBuilderAdminServiceStub.deployWso2EventBuilderConfiguration(eventBuilderName, streamId, transportAdaptorName, transportAdaptorType, metaData, correlationData, payloadData, eventBuilderPropertyDtos, mappingEnabled);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void addXmlEventBuilderConfiguration(String eventBuilderName,
                                                String streamNameWithVersion,
                                                String transportAdaptorName,
                                                String transportAdaptorType,
                                                EventInputPropertyConfigurationDto[] xpathExpressions,
                                                PropertyDto[] inputPropertyConfiguration,
                                                PropertyDto[] xpathDefinitions,
                                                String parentSelectorXpath, boolean mappingEnabled)
            throws RemoteException {
        try {
            eventBuilderAdminServiceStub.deployXmlEventBuilderConfiguration(eventBuilderName, streamNameWithVersion, transportAdaptorName, transportAdaptorType, xpathExpressions, inputPropertyConfiguration, xpathDefinitions, parentSelectorXpath, mappingEnabled);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void addEventBuilderConfiguration(String eventBuilderConfigurationXml)
            throws RemoteException {
        try {
            eventBuilderAdminServiceStub.deployEventBuilderConfiguration(eventBuilderConfigurationXml);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeActiveEventBuilderConfiguration(String eventBuilderName)
            throws RemoteException {
        try {
            eventBuilderAdminServiceStub.undeployActiveEventBuilderConfiguration(eventBuilderName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeInactiveEventBuilderConfiguration(String filePath)
            throws RemoteException {
        try {
            eventBuilderAdminServiceStub.undeployInactiveEventBuilderConfiguration(filePath);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public EventBuilderConfigurationDto getEventBuilderConfiguration(
            String eventBuilderConfiguration)
            throws RemoteException {
        try {
            return eventBuilderAdminServiceStub.getActiveEventBuilderConfiguration(eventBuilderConfiguration);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }
}
