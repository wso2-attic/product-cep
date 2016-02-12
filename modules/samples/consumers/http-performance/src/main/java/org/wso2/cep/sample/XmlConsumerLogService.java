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

package org.wso2.cep.sample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlConsumerLogService extends HttpServlet {

    private static Log log = LogFactory.getLog(XmlConsumerLogService.class);

    private DecimalFormat decimalFormat = new DecimalFormat("#");
    private static int elapsedCount = 50000;
    private static AtomicLong eventCount = new AtomicLong(0);
    private static long lastTime;
    private long sentTime;
    private Pattern eventPattern;

    private static AtomicLong latency = new AtomicLong(0);

    public void init() throws ServletException {
        log.info("Logger service initiated");
        eventCount.set(0);
        lastTime = System.currentTimeMillis();
        eventPattern = Pattern.compile("(<timestamp>(\\d+))");
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
        String message = out.toString();

        Matcher eventPatternMatcher = eventPattern.matcher(message);
        if (eventPatternMatcher.find()) {
            sentTime = Long.parseLong(eventPatternMatcher.group(2));
        } else {
            log.error("unable to extract timestamp from received event");
        }

        latency.addAndGet(receivedTime - sentTime);

        eventCount.addAndGet(1);

        if (eventCount.get() % elapsedCount == 0) {

            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastTime;
            double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
            lastTime = currentTime;

            log.info("Sent " + elapsedCount + " sensor events in time (ms) for throughput of : " + elapsedTime +
                    ", " + decimalFormat.format(throughputPerSecond) + ", Latency: " + (double) latency.get() / elapsedCount);
            latency = new AtomicLong(0);
        }

        return message;
    }

}
