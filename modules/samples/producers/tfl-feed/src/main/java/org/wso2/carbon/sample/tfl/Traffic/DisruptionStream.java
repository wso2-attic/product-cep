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

package org.wso2.carbon.sample.tfl.Traffic;

import org.wso2.carbon.sample.tfl.Bus.Bus;
import org.wso2.carbon.sample.tfl.BusStop.BusStop;
import org.wso2.carbon.sample.tfl.TflStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by isuru on 2/9/15.
 */
public class DisruptionStream extends Thread {
    String TrafficURL;

    public DisruptionStream(String url) {
        super();
        this.TrafficURL = url;
    }

    public void run() {
        try {
            URL obj = new URL(TrafficURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + TrafficURL);
            System.out.println("Response Code : " + responseCode);

            //BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            ArrayList<Disruption> disruptionsList = new ArrayList<Disruption>();
            try {
                double t = System.currentTimeMillis();
                //System.out.println("TrafficStream");
                // Get SAX Parser Factory
                SAXParserFactory factory = SAXParserFactory.newInstance();
                // Turn on validation, and turn off namespaces
                factory.setValidating(true);
                factory.setNamespaceAware(false);
                SAXParser parser = factory.newSAXParser();
                parser.parse(con.getInputStream(), new TrafficXMLHandler(disruptionsList));
                System.out.println("Number of Disruptions added to the list: " + disruptionsList.size());
                System.out.println("Time taken for parsing: " + (System.currentTimeMillis() - t));
            } catch (ParserConfigurationException e) {
                System.out.println("The underlying parser does not support " +
                        " the requested features.");
            } catch (FactoryConfigurationError e) {
                System.out.println("Error occurred obtaining SAX Parser Factory.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            con.disconnect();

            //System.out.println(disruptionsList.get(0));
            ArrayList<String> list = new ArrayList<String>();
            int count = 0;
            for (Disruption disruption : disruptionsList) {
                if (!disruption.isMultiPolygon)
                    System.out.println(disruption.toString());
                if(disruption.state.contains("Active")) {
                //list.add(disruption.toStringSeverityMinimal());
                list.add(disruption.toString());
                }
                count++;
            }
            //System.out.println(list.get(0));
            TflStream.send(list, TflStream.endPointTraffic);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
