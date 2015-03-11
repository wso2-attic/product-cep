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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.sample.objectdetection.server.data.StreamData;
import org.wso2.carbon.sample.objectdetection.server.utilities.MapHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The class uses to capture HTTP request and to
 */
@WebServlet("/imageshow")
public class ObjectDetectionDataSourceHandler extends HttpServlet {

    /**
     * Serial version UID for serialization in {@link HttpServlet}.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger to log information, warnings and errors.
     */
    private static Logger log = Logger.getLogger(ObjectDetectionDataSourceHandler.class);

    /**
     * The maximum size of the map. The map is used to store received stream data from CEP.
     */
    private static final int MAX_MAP_SIZE = 100;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.addToMap(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.addToMap(request);
    }

    /**
     * The following method converts the data from the HTTP request and store them in a map based on
     * the received frame ID. If the frame ID already exists, the added stream data is updated, else
     * a new stream data is added.
     *
     * @param request The HTTP request.
     */
    private synchronized void addToMap(HttpServletRequest request) {
        try {
            // Capturing payloadData and parsing the json content.
            JSONParser jsonParser = new JSONParser();
            JSONObject message = (JSONObject) jsonParser.parse(request.getReader());
            JSONObject event = (JSONObject) message.get("event");
            JSONObject payLoadData = (JSONObject) event.get("payloadData");

            // Creating/updating stream data in map.
            StreamData streamData;

            if (MapHelper.getMap().containsKey(Integer.parseInt((String) payLoadData.
                                                                                get("frame_id")))) {

                // Use existing data to modify when frame ID already exists in map.
                streamData = MapHelper.getMap().get(Integer.parseInt((String) payLoadData.
                                                                                get("frame_id")));

                // Updating the images list.
                byte[] hexArr = Hex.decodeHex(((String) payLoadData.get("image")).toCharArray());
                streamData.getImages().add(Base64.encodeBase64String(hexArr));

                // Updating the object count.
                streamData.setObjectCount(Long.parseLong((String) payLoadData.get("object_count")));

            } else {
                // Create new stream data when frame ID does not exist in map.
                streamData = new StreamData();
                streamData.setTimestamp(Long.parseLong((String) payLoadData.get("timestamp")));
                streamData.setFrameID(Integer.parseInt((String) payLoadData.get("frame_id")));
                streamData.setCameraID((String) payLoadData.get("camera_id"));
                streamData.setCascade((String) payLoadData.get("cascade"));

                // Updating the images list.
                byte[] hexArr = Hex.decodeHex(((String) payLoadData.get("image")).toCharArray());
                streamData.getImages().add(Base64.encodeBase64String(hexArr));

                // Updating the object count detected.
                streamData.setObjectCount(Long.parseLong((String) payLoadData.get("object_count")));

                // Clearing the map if maximum size exceeds as a caution.
                if (MapHelper.getMap().size() >= MAX_MAP_SIZE) {
                    MapHelper.getMap().clear();
                }

                // Add the processed stream data to map.
                MapHelper.getMap().put(streamData.getFrameID(), streamData);
            }

            log.info("Received frame ID : " + Integer.toString(streamData.getFrameID()));
        } catch (DecoderException e) {
            log.error("Unable to convert the hex image string to byte array.");
        } catch (IOException e) {
            log.error("Unable to read contents from HTTP request.");
        } catch (ParseException e) {
            log.error("Unable to convert HTTP request content to a JSON string.");
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
