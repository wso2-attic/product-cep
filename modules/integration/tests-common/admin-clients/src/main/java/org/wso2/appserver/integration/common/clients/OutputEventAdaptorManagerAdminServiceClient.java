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
import org.wso2.carbon.event.output.adaptor.manager.stub.OutputEventAdaptorManagerAdminServiceStub;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorConfigurationInfoDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorFileDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertiesDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto;

import java.rmi.RemoteException;

public class OutputEventAdaptorManagerAdminServiceClient {
    private static final Log log = LogFactory.getLog(OutputEventAdaptorManagerAdminServiceClient.class);
    private final String serviceName = "OutputEventAdaptorManagerAdminService";
    private OutputEventAdaptorManagerAdminServiceStub outputEventAdaptorManagerAdminServiceStub;
    private String endPoint;

    public OutputEventAdaptorManagerAdminServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        outputEventAdaptorManagerAdminServiceStub = new OutputEventAdaptorManagerAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, outputEventAdaptorManagerAdminServiceStub);

    }

    public OutputEventAdaptorManagerAdminServiceClient(String backEndUrl, String userName,
                                                       String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        outputEventAdaptorManagerAdminServiceStub = new OutputEventAdaptorManagerAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, outputEventAdaptorManagerAdminServiceStub);

    }

    public ServiceClient _getServiceClient() {
        return outputEventAdaptorManagerAdminServiceStub._getServiceClient();
    }

    public String[] getAllOutputEventAdaptorNames() throws RemoteException {
        String[] inputTransportAdaptorNames = null;
        try {
            OutputEventAdaptorConfigurationInfoDto[] inputTransportAdaptorConfigurationInfoDtos = outputEventAdaptorManagerAdminServiceStub.getAllActiveOutputEventAdaptorConfiguration();
            inputTransportAdaptorNames = new String[inputTransportAdaptorConfigurationInfoDtos.length];
            for (int i = 0; i < inputTransportAdaptorConfigurationInfoDtos.length; i++) {
                inputTransportAdaptorNames[i] = inputTransportAdaptorConfigurationInfoDtos[i].getEventAdaptorName();
            }
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException("RemoteException", e);

        }
        return inputTransportAdaptorNames;
    }

    public OutputEventAdaptorPropertiesDto getOutputEventAdaptorProperties(
            String transportAdaptorName) throws RemoteException {
        try {
            return outputEventAdaptorManagerAdminServiceStub.getActiveOutputEventAdaptorConfiguration(transportAdaptorName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public int getActiveOutputEventAdaptorConfigurationCount()
            throws RemoteException {
        try {
            OutputEventAdaptorConfigurationInfoDto[] configs = outputEventAdaptorManagerAdminServiceStub.getAllActiveOutputEventAdaptorConfiguration();
            if (configs == null) {
                return 0;
            } else {
                return configs.length;
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }
    }

    public int getOutputEventAdaptorConfigurationCount()
            throws RemoteException {
        try {
            OutputEventAdaptorFileDto[] configs = outputEventAdaptorManagerAdminServiceStub.getAllInactiveOutputEventAdaptorConfiguration();
            if (configs == null) {
                return getActiveOutputEventAdaptorConfigurationCount();
            } else {
                return configs.length + getActiveOutputEventAdaptorConfigurationCount();
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }
    }

    public OutputEventAdaptorConfigurationInfoDto[] getActiveOutputEventAdaptorConfigurations()
            throws RemoteException {
        try {
            return outputEventAdaptorManagerAdminServiceStub.getAllActiveOutputEventAdaptorConfiguration();
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }
    }

    public void addOutputEventAdaptorConfiguration(String transportAdaptorName,
                                                   String transportAdaptorType,
                                                   OutputEventAdaptorPropertyDto[] transportAdaptorProperty)
            throws RemoteException {
        try {
            outputEventAdaptorManagerAdminServiceStub.deployOutputEventAdaptorConfiguration(transportAdaptorName, transportAdaptorType, transportAdaptorProperty);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeActiveOutputEventAdaptorConfiguration(String transportAdaptorName)
            throws RemoteException {
        try {
            outputEventAdaptorManagerAdminServiceStub.undeployActiveOutputEventAdaptorConfiguration(transportAdaptorName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeInactiveOutputEventAdaptorConfiguration(String filePath)
            throws RemoteException {
        try {
            outputEventAdaptorManagerAdminServiceStub.undeployInactiveOutputEventAdaptorConfiguration(filePath);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }
}
