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

package org.wso2.carbon.sample.tfl;

import org.wso2.carbon.sample.tfl.Bus.BusStream;
import org.wso2.carbon.sample.tfl.BusStop.BusStop;
import org.wso2.carbon.sample.tfl.Traffic.DisruptionStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class GetData extends Thread {

    public static final String RecordedBusStopURL = "http://localhost/TFL/stop.txt";
    public static final String RecordedTrafficURL = "http://localhost/TFL/tims_feed.xml";
    public static final String RecordedBusURL = "http://localhost/TFL/data";

    public static final String LiveTrafficURL = "https://data.tfl.gov.uk/tfl/syndication/feeds/tims_feed.xml";
    public static final String LiveBusStopURL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=61,62,63,64,65,66&ReturnList=StopID,Latitude,Longitude";
    public static final String LiveBusURL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=61,62,63,64,65,66&ReturnList=StopID,LineID,VehicleID,EstimatedTime";

    public static String TrafficURL;
    public static String BusURL;
    public static String BusStopURL;

    private boolean isbus;

    public GetData(boolean isbus, boolean playback) {
        super();
        this.isbus = isbus;

        if(playback){
            TrafficURL = RecordedTrafficURL;
            BusURL = RecordedBusURL;
            BusStopURL = RecordedBusStopURL;
        }else{
            TrafficURL = LiveTrafficURL;
            BusURL = LiveBusURL;
            BusStopURL = LiveBusStopURL;
        }
    }

    public void run() {

        if(isbus){
            getStops();
            getBus();
        }
        else {
            getDisruptions();
        }

    }

    private void getBus() {
        BusStream b;
        long time = System.currentTimeMillis();
        int i = 0;
        while (true){
            //System.out.println("Getting Data");
            String url = BusURL;
            if (BusURL.contains("localhost"))
                url += i + ".txt";
            System.out.println(url);
            b = new BusStream(url);
            b.start();
            try {
                time += 30000;
                Thread.sleep(time - System.currentTimeMillis());
            } catch (InterruptedException e) {
            }

            i = (i+1) % 100;
        }

    }

    private static void getDisruptions() {
        DisruptionStream ds;
        long time = System.currentTimeMillis();

        while(true){
            ds = new DisruptionStream(TrafficURL);
            //System.out.println("Getting Disruption Data ");
            ds.start();
            try{
                time += 300000;
                Thread.sleep(time - System.currentTimeMillis());
            }catch(InterruptedException e){
            }
        }

    }

    private static void getStops() {
        try {
            String[] arr;

            URL obj = new URL(BusStopURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + BusStopURL);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            long time = System.currentTimeMillis();
            //System.out.println(time);
            inputLine = in.readLine();
            inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
            arr = inputLine.split(",");
            TflStream.timeOffset = time - Long.parseLong(arr[2]);


            ArrayList<String> stopJsonList = new ArrayList<String>();

            while ((inputLine = in.readLine()) != null) {
                inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
                arr = inputLine.split(",");
                //System.out.println(Double.parseDouble(arr[3]));
                //System.out.println(Double.parseDouble(arr[2]));
                BusStop temp = new BusStop(arr[1], Double.parseDouble(arr[2]),
                        Double.parseDouble(arr[3]));
                //System.out.println(temp);
                TflStream.map.put(arr[1], temp);
                stopJsonList.add(temp.toString());
            }
            TflStream.send(stopJsonList, TflStream.endPointBus);
            in.close();
            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

