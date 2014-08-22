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
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationFileDto;

import java.rmi.RemoteException;

public class EventProcessorAdminServiceClient {
    private static final Log log = LogFactory.getLog(EventProcessorAdminServiceClient.class);
    private final String serviceName = "EventProcessorAdminService";
    private EventProcessorAdminServiceStub eventProcessorAdminServiceStub;
    private String endPoint;

    public EventProcessorAdminServiceClient(String backEndUrl, String sessionCookie) throws
                                                                                     AxisFault {
        this.endPoint = backEndUrl + serviceName;
        eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, eventProcessorAdminServiceStub);

    }

    public EventProcessorAdminServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, eventProcessorAdminServiceStub);
    }

    public ServiceClient _getServiceClient() {
        return eventProcessorAdminServiceStub._getServiceClient();
    }

    public int getAllActiveExecutionPlanConfigurationCount()
            throws RemoteException {
        try {
            ExecutionPlanConfigurationDto[] configs = eventProcessorAdminServiceStub.getAllActiveExecutionPlanConfigurations();
            if (configs == null) {
                return 0;
            } else {
                return configs.length;
            }
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public int getExecutionPlanConfigurationCount()
            throws RemoteException {
        try {
            ExecutionPlanConfigurationFileDto[] configs = eventProcessorAdminServiceStub.getAllInactiveExecutionPlanConigurations();
            if (configs == null) {
                return getAllActiveExecutionPlanConfigurationCount();
            } else {
                return configs.length + getAllActiveExecutionPlanConfigurationCount();
            }
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void addExecutionPlan(ExecutionPlanConfigurationDto executionPlanConfigurationDto)
            throws RemoteException {
        try {
            eventProcessorAdminServiceStub.deployExecutionPlanConfiguration(executionPlanConfigurationDto);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void addExecutionPlan(String executionPlanConfigurationXml)
            throws RemoteException {
        try {
            eventProcessorAdminServiceStub.deployExecutionPlanConfigurationFromConfigXml(executionPlanConfigurationXml);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeActiveExecutionPlan(String planName)
            throws RemoteException {
        try {
            eventProcessorAdminServiceStub.undeployActiveExecutionPlanConfiguration(planName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeInactiveExecutionPlan(String filePath)
            throws RemoteException {
        try {
            eventProcessorAdminServiceStub.undeployInactiveExecutionPlanConfiguration(filePath);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public ExecutionPlanConfigurationDto getExecutionPlan(String executionPlanName)
            throws RemoteException {
        try {
            return eventProcessorAdminServiceStub.getActiveExecutionPlanConfiguration(executionPlanName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }
}
