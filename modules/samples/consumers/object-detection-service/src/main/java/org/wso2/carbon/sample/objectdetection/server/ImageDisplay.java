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

import java.io.IOException;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wso2.carbon.sample.objectdetection.server.data.StreamData;
import org.wso2.carbon.sample.objectdetection.server.utilities.MapHelper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@WebServlet("/displayImage")
public class ImageDisplay extends HttpServlet {

	/**
	 * Version number for serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Do get.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	                                                                              throws ServletException,
	                                                                              IOException {

	}

	/**
	 * Do post.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	                                                                            throws ServletException,
	                                                                            IOException {
		String event = request.getParameter("event");
		if (event.equals("display")) {
			TreeMap<Integer, StreamData> streamDataMap = MapHelper.getMap();
			if (!streamDataMap.isEmpty()) {
				StreamData streamData = streamDataMap.remove(streamDataMap.keySet().toArray()[0]);
				Gson gson = new Gson();
				JsonElement element = gson.toJsonTree(streamData, new TypeToken<StreamData>() {
				}.getType());
				
				JsonObject jsonObject = element.getAsJsonObject();
				response.setContentType("application/json");
				response.getWriter().print(jsonObject);
			}
		} else if (event.equals("clear")) {
			MapHelper.getMap().clear();
		}
	}

	/**
	 * Inits the servlet.
	 */
	@Override
	public void init() {
	}

	/**
	 * Destroy the servlet.
	 */
	@Override
	public void destroy() {
		super.destroy();

	}
}
