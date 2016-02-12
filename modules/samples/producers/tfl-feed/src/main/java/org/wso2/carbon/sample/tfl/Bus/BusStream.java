/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sample.tfl.Bus;

import org.wso2.carbon.sample.tfl.BusStop.BusStop;
import org.wso2.carbon.sample.tfl.TflStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by isuru on 2/9/15.
 */
public class BusStream extends Thread {
    String url;

    public BusStream(String url) {
        super();
        this.url = url;
    }

    public void run() {
        // String url =
        // "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=1,2&ReturnList=StopID,LineID,VehicleID,EstimatedTime";
        try {
            long time = System.currentTimeMillis();
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            inputLine = in.readLine();
            inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
            String[] arr = inputLine.split(",");

            TflStream.lastTime = Long.parseLong(arr[2]) + TflStream.timeOffset;


            ArrayList<Bus> newBusses = new ArrayList<Bus>();
            while ((inputLine = in.readLine()) != null) {
                inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
                arr = inputLine.split(",");

                Bus bus = TflStream.busses.get(arr[3]);
                BusStop bs = TflStream.map.get(arr[1]);
                if(bs == null) {
                    continue;
                }
                if (bus == null) {
                    bus = new Bus(arr[3]);
                    TflStream.busses.put(arr[3], bus);
                    newBusses.add(bus);
                }
                bus.setData(bs, Long.parseLong(arr[4]));
            }
            for (Bus newBus : newBusses) {
                newBus.setNew();
            }

            in.close();
            System.out.println("Added busses to a hashmap. " + (System.currentTimeMillis() - time) + " millis");
        } catch (Exception e) {

        }
    }
}
