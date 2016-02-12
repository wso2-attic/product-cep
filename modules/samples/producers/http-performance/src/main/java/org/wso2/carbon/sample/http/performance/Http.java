/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.http.performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.axiom.om.util.Base64;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Http implements Runnable {
    private static Log log = LogFactory.getLog(Http.class);
    static String url;
    static String username;
    static String password;
    static long noOfEvents;
    long count;
    static int elapsedCount;
    static int noOfPublishers;

    public Http() {
        count = 0;
    }

    public static void main(String args[]) {
        log.info("Starting WSO2 Http Client");
        url = args[0];
        username = args[1];
        password = args[2];
        try {
            noOfEvents = Long.parseLong(args[3]);
            elapsedCount = Integer.parseInt(args[4]);
            noOfPublishers = Integer.parseInt(args[5]);
            ExecutorService executor = Executors.newFixedThreadPool(noOfPublishers);
            for (int i = 0; i < noOfPublishers; i++) {
                Runnable publisher = new Http();
                executor.execute(publisher);
            }
            executor.shutdown();

        } catch (NumberFormatException e) {
            log.error("Entered value for no of events is invalid. Please enter an integer", e);
        }
    }

    private static void processAuthentication(HttpPost method, String username, String password) {
        if (username != null && username.trim().length() > 0) {
            method.setHeader("Authorization",
                             "Basic " + Base64.encode((username + ":" + password).getBytes()));
        }
    }

    private static JsonObject getRandomEvent(long count) {
        JsonObject event = new JsonObject();
        JsonObject metaData = new JsonObject();
        JsonObject correlationData = new JsonObject();
        JsonObject payLoadData = new JsonObject();
        metaData.addProperty("timestamp", System.currentTimeMillis());
        metaData.addProperty("isPowerSaverEnabled", false);
        metaData.addProperty("sensorId", count);
        metaData.addProperty("sensorName", "temperature");
        correlationData.addProperty("longitude", 2332.424);
        correlationData.addProperty("latitude", 2323.23232);
        payLoadData.addProperty("humidity", 2.3f);
        payLoadData.addProperty("sensorValue", 23423.234);
        event.add("metaData", metaData);
        event.add("correlationData", correlationData);
        event.add("payloadData", payLoadData);
        return event;
    }

    @Override
    public void run() {
        HttpClient httpClient = new SystemDefaultHttpClient();
        try {
            HttpPost method = new HttpPost(url);
            log.info("Sending messages..");
            long lastTime = System.currentTimeMillis();
            DecimalFormat decimalFormat = new DecimalFormat("#");
            while (count < noOfEvents) {
                count++;
                String temp = "{\"event\": " + getRandomEvent(count).toString() + "}";
                StringEntity entity = new StringEntity(temp);
                method.setEntity(entity);
                if (url.startsWith("https")) {
                    processAuthentication(method, username, password);
                }
                httpClient.execute(method).getEntity().getContent().close();
                if (count % elapsedCount == 0) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - lastTime;
                    double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                    lastTime = currentTime;
                    log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime +
                             " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond) +
                             " events per second.");
                }
            }
        } catch (Throwable t) {
            log.error("Error when sending the messages", t);
        }
    }
}

