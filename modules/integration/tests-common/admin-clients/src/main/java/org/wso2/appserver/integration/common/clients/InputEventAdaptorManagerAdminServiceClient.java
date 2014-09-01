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
import org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorConfigurationInfoDto;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorFileDto;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertiesDto;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto;

import java.rmi.RemoteException;

public class InputEventAdaptorManagerAdminServiceClient {
    private static final Log log = LogFactory.getLog(InputEventAdaptorManagerAdminServiceClient.class);
    private final String serviceName = "InputEventAdaptorManagerAdminService";
    private InputEventAdaptorManagerAdminServiceStub inputEventAdaptorManagerAdminServiceStub;
    private String endPoint;

    public InputEventAdaptorManagerAdminServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        inputEventAdaptorManagerAdminServiceStub = new InputEventAdaptorManagerAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, inputEventAdaptorManagerAdminServiceStub);

    }

    public InputEventAdaptorManagerAdminServiceClient(String backEndUrl, String userName,
                                                      String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        inputEventAdaptorManagerAdminServiceStub = new InputEventAdaptorManagerAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, inputEventAdaptorManagerAdminServiceStub);

    }

    public ServiceClient _getServiceClient() {
        return inputEventAdaptorManagerAdminServiceStub._getServiceClient();
    }

    public String[] getAllInputEventAdaptorNames() throws RemoteException {
        String[] InputEventAdaptorNames = null;
        try {
            InputEventAdaptorConfigurationInfoDto[] InputEventAdaptorConfigurationInfoDtos = inputEventAdaptorManagerAdminServiceStub.getAllActiveInputEventAdaptorConfiguration();
            InputEventAdaptorNames = new String[InputEventAdaptorConfigurationInfoDtos.length];
            for (int i = 0; i < InputEventAdaptorConfigurationInfoDtos.length; i++) {
                InputEventAdaptorNames[i] = InputEventAdaptorConfigurationInfoDtos[i].getEventAdaptorName();
            }
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException("RemoteException", e);

        }
        return InputEventAdaptorNames;
    }

    public InputEventAdaptorPropertiesDto getInputEventAdaptorProperties(
            String transportAdaptorName) throws RemoteException {
        try {
            return inputEventAdaptorManagerAdminServiceStub.getActiveInputEventAdaptorConfiguration(transportAdaptorName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public int getActiveInputEventAdaptorConfigurationCount()
            throws RemoteException {
        try {
            InputEventAdaptorConfigurationInfoDto[] configs = inputEventAdaptorManagerAdminServiceStub.getAllActiveInputEventAdaptorConfiguration();
            if (configs == null) {
                return 0;
            } else {
                return configs.length;
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }
    }

    public int getInputEventAdaptorConfigurationCount()
            throws RemoteException {
        try {
            InputEventAdaptorFileDto[] configs = inputEventAdaptorManagerAdminServiceStub.getAllInactiveInputEventAdaptorConfigurationFile();
            if (configs == null) {
                return getActiveInputEventAdaptorConfigurationCount();
            } else {
                return configs.length + getActiveInputEventAdaptorConfigurationCount();
            }
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }
    }

    public InputEventAdaptorConfigurationInfoDto[] getActiveInputEventAdaptorConfigurations()
            throws RemoteException {
        try {
            return inputEventAdaptorManagerAdminServiceStub.getAllActiveInputEventAdaptorConfiguration();
        } catch (RemoteException e) {
            throw new RemoteException("RemoteException", e);
        }
    }

    public void addInputEventAdaptorConfiguration(String transportAdaptorName,
                                                  String transportAdaptorType,
                                                  InputEventAdaptorPropertyDto[] transportAdaptorProperty)
            throws RemoteException {
        try {
            inputEventAdaptorManagerAdminServiceStub.deployInputEventAdaptorConfiguration(transportAdaptorName, transportAdaptorType, transportAdaptorProperty);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeActiveInputEventAdaptorConfiguration(String transportAdaptorName)
            throws RemoteException {
        try {
            inputEventAdaptorManagerAdminServiceStub.undeployActiveInputEventAdaptorConfiguration(transportAdaptorName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

    public void removeInActiveInputEventAdaptorConfiguration(String filePath)
            throws RemoteException {
        try {
            inputEventAdaptorManagerAdminServiceStub.undeployInactiveInputEventAdaptorConfiguration(filePath);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }
}
