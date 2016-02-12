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

package org.wso2.carbon.sample.tfl.Traffic.ML;

/**
 * Created by sanka on 2/6/15.
 */

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.xml.sax.SAXException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;

public class TrafficStream {

    private static GeometryFactory geometryFactory;

    public static void main(String[] args) {
        try {
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/isuru/data.csv")));
            bw.write("day,hour,latitude,longitude,traffic\n");
            geometryFactory = JTSFactoryFinder.getGeometryFactory();
            //double longitudeStart = -0.35, longitudeEnd = 0.25;
            //double latitudeStart = 51.3, latitudeEnd = 51.6;
            double longitudeStart = -0.095, longitudeEnd = -0.003;
            double latitudeStart = 51.496, latitudeEnd = 51.524;


            double unit = 0.005;
            double longitude, latitude;

            int rows = (int) Math.round((longitudeEnd - longitudeStart) / unit);
            int cols = (int) Math.round((latitudeEnd - latitudeStart) / unit);
            System.out.println(rows * cols);

            Geometry[][] geometries = new Geometry[rows][cols];
            Coordinate[] areaC = new Coordinate[5];

            areaC[0] = new Coordinate(longitudeStart, latitudeStart);
            areaC[1] = new Coordinate(longitudeStart, latitudeEnd);
            areaC[2] = new Coordinate(longitudeEnd, latitudeEnd);
            areaC[3] = new Coordinate(longitudeEnd, latitudeStart);
            areaC[4] = new Coordinate(longitudeStart, latitudeStart);
            Geometry area = geometryFactory.createPolygon(geometryFactory.createLinearRing(areaC), null);


            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    longitude = longitudeStart + i * unit;
                    latitude = latitudeStart + j * unit;
                    Coordinate[] coords = new Coordinate[5];
                    coords[0] = new Coordinate(longitude, latitude);
                    coords[1] = new Coordinate(longitude + unit, latitude);
                    coords[2] = new Coordinate(longitude + unit, latitude + unit);
                    coords[3] = new Coordinate(longitude, latitude + unit);
                    coords[4] = new Coordinate(longitude, latitude);
                    geometries[i][j] = geometryFactory.createPolygon(geometryFactory.createLinearRing(coords), null);
                    System.out.println(geometries[i][j]);
                    //geometries[i][j] = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                }
            }
            int period = 60;
            long time = System.currentTimeMillis();
            for (int day = 3; day < 16; day++) {
                //if(day==7||day==8)
                //    continue;
                for (int hour = 0; hour < 24; hour++) {
                    for (int minute = 0; minute < 60; minute += period) {
                        ArrayList<Disruption> disruptionsList = new ArrayList<Disruption>();
                        // Get SAX Parser Factory
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        // Turn on validation, and turn off namespaces
                        factory.setValidating(false);
                        factory.setNamespaceAware(false);
                        SAXParser parser = factory.newSAXParser();
                        for (int i = 0; i < 4; i++) {
                            String name = "2015-02-" + convert(day) + "-" + convert(hour) + "." + convert(minute + i * 5);
                            String file = "/home/isuru/data/" + "2015-02-" + convert(day) + "/" + name + ".txt";
                            //System.out.println(file);
                            InputStream in = new FileInputStream(new File(file));

                            try {
                                parser.parse(in, new TrafficXMLHandler(disruptionsList));
                                break;
                            } catch (Exception e) {
                                System.out.println("Error in parsing");
                                disruptionsList.clear();
                            }
                        }
                        ArrayList<Disruption> disruptionsWithin = new ArrayList<Disruption>();
                        for (Disruption d : disruptionsList) {
                            if (d.state.contains("Active") && d.geometry.intersects(area)) {
                                disruptionsWithin.add(d);
                            }
                        }

                        boolean found;
                        System.out.println((day - 3) + "," + ((hour * 60 + minute)/period) +
                                "," + disruptionsWithin.size() + "," + (System.currentTimeMillis() - time));
                        for (int i = 0; i < rows; i++) {
                            for (int j = 0; j < cols; j++) {
                                found = false;
                                for (Disruption d : disruptionsWithin) {
                                    if (geometries[i][j].intersects(d.geometry)) {
                                        bw.write((day - 3) + "," + ((hour * 60 + minute)/period) + "," + i+","+ j + ",1\n");
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    bw.write((day - 3) + "," + ((hour * 60 + minute) / period) + "," + i + "," + j + ",0\n");
                                }
                            }
                        }

                    }
                }
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String convert(int d) {
        return (d + 100 + "").substring(1);
    }
}

