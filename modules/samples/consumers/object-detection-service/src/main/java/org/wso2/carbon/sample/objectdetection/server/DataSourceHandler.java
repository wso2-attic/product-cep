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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.sample.objectdetection.server.data.StreamData;
import org.wso2.carbon.sample.objectdetection.server.utilities.MapHelper;

/**
 * The HTTP servlet to receive data. Class DataSourceHandler.
 */
@WebServlet("/imageshow")
public class DataSourceHandler extends HttpServlet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The logger. */
	private static Logger log = Logger.getLogger(DataSourceHandler.class);

	/** The maximum map size. */
	private final int MAX_MAP_SIZE = 100;

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
		this.addToMap(request, response);
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
		this.addToMap(request, response);
	}

	/**
	 * Adds stream data to the map.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 */
	private synchronized void addToMap(HttpServletRequest request, HttpServletResponse response) {
		try {
			// capturing payloadData and parsing json
			JSONParser jsonParser = new JSONParser();
			JSONObject message = (JSONObject) jsonParser.parse(request.getReader());
			JSONObject event = (JSONObject) message.get("event");
			JSONObject payLoadData = (JSONObject) event.get("payloadData");

			// creating/updating stream data in map
			StreamData streamData;

			if (MapHelper.getMap()
			             .containsKey(Integer.parseInt((String) payLoadData.get("frame_id")))) {
				streamData =
				             MapHelper.getMap()
				                      .get(Integer.parseInt((String) payLoadData.get("frame_id")));

				// updating images list
				byte[] hexArr = Hex.decodeHex(((String) payLoadData.get("image")).toCharArray());
				streamData.getImages().add(Base64.encodeBase64String(hexArr));

				// updating object count
				streamData.setObjectCount(Long.parseLong((String) payLoadData.get("object_count")));

			} else {
				streamData = new StreamData();
				streamData.setTimestamp(Long.parseLong((String) payLoadData.get("timestamp")));
				streamData.setFrameID(Integer.parseInt((String) payLoadData.get("frame_id")));
				streamData.setCameraID((String) payLoadData.get("camera_id"));
				streamData.setCascade((String) payLoadData.get("cascade"));

				// updating images list
				byte[] hexArr = Hex.decodeHex(((String) payLoadData.get("image")).toCharArray());
				streamData.getImages().add(Base64.encodeBase64String(hexArr));

				// updating object count
				streamData.setObjectCount(Long.parseLong((String) payLoadData.get("object_count")));

				// clearing if maximum size exceeds
				if (MapHelper.getMap().size() >= MAX_MAP_SIZE) {
					MapHelper.getMap().clear();
				}

				MapHelper.getMap().put(streamData.getFrameID(), streamData);
			}

			log.info("Received frame ID : " + Integer.toString(streamData.getFrameID()));
		} catch (DecoderException exception) {
			log.error(exception);
			exception.printStackTrace();
		} catch (ParseException exception) {
			log.error(exception);
			exception.printStackTrace();
		} catch (IOException exception) {
			log.error(exception);
			exception.printStackTrace();
		} catch (Exception exception) {
			log.error(exception);
			exception.printStackTrace();
		}
	}

	/**
	 * Inits the servlet.
	 */
	@Override
	public void init() {
	}

	/**
	 * Destroys the servlet.
	 */
	@Override
	public void destroy() {
		super.destroy();

	}
}
