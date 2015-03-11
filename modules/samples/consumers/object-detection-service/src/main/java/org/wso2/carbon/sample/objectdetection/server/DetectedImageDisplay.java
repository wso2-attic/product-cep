/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.sample.objectdetection.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.wso2.carbon.sample.objectdetection.server.data.StreamData;
import org.wso2.carbon.sample.objectdetection.server.utilities.MapHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TreeMap;

/**
 *  This HTTP servlet used by the JSP page invoked through AJAX to show stream data in the map.
 */
@WebServlet("/displayImage")
public class DetectedImageDisplay extends HttpServlet {

    /**
     * Serial version UID for serialization in {@link HttpServlet}.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                                                            throws ServletException, IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String event = request.getParameter("event");
        if (event.equals("display")) {
            TreeMap<Integer, StreamData> streamDataMap = MapHelper.getStreamDataTreeMap();
            if (!streamDataMap.isEmpty()) {
                StreamData streamData = streamDataMap.firstEntry().getValue();
                Gson gson = new Gson();
                JsonElement element = gson.toJsonTree(streamData, new TypeToken<StreamData>() {
                }.getType());

                JsonObject jsonObject = element.getAsJsonObject();
                response.setContentType("application/json");
                response.getWriter().print(jsonObject);
            }
        } else if (event.equals("clear")) {
            MapHelper.getStreamDataTreeMap().clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        // Nothing to initialize
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
    }
}
