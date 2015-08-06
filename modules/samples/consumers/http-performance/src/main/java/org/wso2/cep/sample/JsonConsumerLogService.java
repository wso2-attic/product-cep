package org.wso2.cep.sample;/*
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

import com.jayway.jsonpath.JsonPath;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class JsonConsumerLogService extends HttpServlet {

    private static Logger log = Logger.getLogger(JsonConsumerLogService.class);

    private DecimalFormat decimalFormat = new DecimalFormat("#");
    private static int elapsedCount = 50000;
    private static AtomicLong eventCount = new AtomicLong(0);
    private static long lastTime;

    private static AtomicLong latency = new AtomicLong(0);

    public void init() throws ServletException {
        log.info("Logger service initiated");
        eventCount.set(0);
        lastTime = System.currentTimeMillis();
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        this.inputStreamToString(request.getInputStream());
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {
        this.inputStreamToString(request.getInputStream());
    }

    private String inputStreamToString(InputStream in) throws IOException {
        long receivedTime = System.currentTimeMillis();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int i;
        while ((i = in.read(buff)) > 0) {
            out.write(buff, 0, i);
        }
        out.close();

        //TODO Do we need to use json path to get the timestamp, string manipulations might be faster
        JsonPath jsonPath = JsonPath.compile("$.event.metaData");
        Map<Object, Object> eventMap = jsonPath.read(out.toString());

        long sentTime = Long.parseLong(eventMap.get("timestamp").toString());

        latency.addAndGet(receivedTime - sentTime);

        eventCount.addAndGet(1);

        if (eventCount.get() % elapsedCount == 0) {

            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastTime;
            double throughputPerSecond = (((double)elapsedCount) / elapsedTime) * 1000;
            lastTime = currentTime;

            log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime +
                    " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond) +
                    " events per second.");

            log.info("Receiving Latency: " + (double)latency.get() / elapsedCount);
            latency = new AtomicLong(0);

        }

        return out.toString();
    }

}
