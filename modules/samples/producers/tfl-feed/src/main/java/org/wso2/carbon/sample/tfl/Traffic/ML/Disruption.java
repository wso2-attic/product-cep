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

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sanka on 2/6/15.
 */
public class Disruption {
    String id;
    String state;
    String severity;
    String location;
    String comments;
    String coordinates = null;
    static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    Geometry geometry;
    public boolean isMultiPolygon = true;
    final static double tolerance = 0.0005;
    ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
    ArrayList<Polygon> polygon = new ArrayList<Polygon>();

    public Disruption() {

    }

    public Disruption(String id, String severity, String location, String comments) {
        this.id = id;
        this.state = severity;
        this.comments = comments;
        this.location = location;
    }

    public void setCoordsPoly(String coords) {
        isMultiPolygon = false;
        String[] temp = coords.split(",");
        StringBuilder sb = new StringBuilder("{ \n 'type': 'Polygon', \n 'coordinates': [[");
        for (int i = 0; i < temp.length - 1; i += 2) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append("[").append(Double.parseDouble(temp[i])).append(",").append(Double.parseDouble(temp[i + 1])).append("]");
        }
        //sb.append("]] \n }");
        //coordinates = sb.toString();
        boolean created = false;
        try {
            geometry = createGeometry(sb.toString()+"]]}");
            created = true;
        }catch(Exception e){

        }
        if(!created){
            sb.append(",");
            sb.append("[").append(Double.parseDouble(temp[0])).append(",").append(Double.parseDouble(temp[1])).append("]");

            geometry = createGeometry(sb.toString()+"]]}");
        }
    }
    public static Geometry createGeometry(String str) {
        GeometryJSON j = new GeometryJSON();
        try {
            return j.read(str.replace("'", "\""));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a geometry from given str " + str, e);
        }
    }

    public void addCoordsLane(String co) {
        String[] temp = co.split(",");
        if (temp.length != 4) {
            System.out.println(co);
            return;
        }
        try {
            double x1, x2, y1, y2;
            x1 = Double.parseDouble(temp[0]);
            y1 = Double.parseDouble(temp[1]);
            x2 = Double.parseDouble(temp[2]);
            y2 = Double.parseDouble(temp[3]);

            double f = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow(y1 - y2, 2));
            if(f==0)
                return;
            /*coords.add(new Coordinate(Double.parseDouble(temp[0]) - tolerance * (y2 - y1) / f, Double.parseDouble(temp[1]) - tolerance * (x1 - x2) / f));
            coords.add(new Coordinate(Double.parseDouble(temp[0]) + tolerance * (y2 - y1) / f, Double.parseDouble(temp[1]) + tolerance * (x1 - x2) / f));
            coords.add(new Coordinate(Double.parseDouble(temp[2]) + tolerance * (y2 - y1) / f, Double.parseDouble(temp[3]) + tolerance * (x1 - x2) / f));
            coords.add(new Coordinate(Double.parseDouble(temp[2]) - tolerance * (y2 - y1) / f, Double.parseDouble(temp[3]) - tolerance * (x1 - x2) / f));
*/
            //coords.add(new Coordinate(Double.parseDouble(temp[0]), Double.parseDouble(temp[1])));
            //coords.add(new Coordinate(Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));

            Coordinate[] poly = new Coordinate[5];

            poly[0] = new Coordinate(Double.parseDouble(temp[0]) - tolerance * (y2 - y1) / f, Double.parseDouble(temp[1]) - tolerance * (x1 - x2) / f);
            poly[1] = new Coordinate(Double.parseDouble(temp[0]) + tolerance * (y2 - y1) / f, Double.parseDouble(temp[1]) + tolerance * (x1 - x2) / f);
            poly[2] = new Coordinate(Double.parseDouble(temp[2]) + tolerance * (y2 - y1) / f, Double.parseDouble(temp[3]) + tolerance * (x1 - x2) / f);
            poly[3] = new Coordinate(Double.parseDouble(temp[2]) - tolerance * (y2 - y1) / f, Double.parseDouble(temp[3]) - tolerance * (x1 - x2) / f);
            poly[4] = new Coordinate(Double.parseDouble(temp[0]) - tolerance * (y2 - y1) / f, Double.parseDouble(temp[1]) - tolerance * (x1 - x2) / f);

            polygon.add(geometryFactory.createPolygon(geometryFactory.createLinearRing(poly), null));
        } catch (NumberFormatException e) {
            System.out.println("NFE " + co);
        }
    }
    public void end() {
        if (isMultiPolygon) {
            Polygon[] c = new Polygon[polygon.size()];
            c = polygon.toArray(c);
            geometry = geometryFactory.createMultiPolygon(c);
        }

    }/*
    public void end() {
        if (isMultiPolygon) {

            Coordinate[] c = new Coordinate[coords.size()];
            c = coords.toArray(c);

            //System.out.println(c.length);
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            ConvexHull ch = new ConvexHull(c, geometryFactory);
            //System.out.println(ch.getConvexHull().toString());
            geometry = ch.getConvexHull();
        }

    }*/

    @Override
    public String toString() {
        return "{'id': "+id+", \n"
                +"'properties': { \n"
                +" 'timeStamp': "+System.currentTimeMillis()+", \n"
                +" 'state': '"+severity+"', \n"
                +" 'information': "+"'Location- "+location+" Comments- "+comments+"'"+"\n"
                +" }, \n"
                +"'geometry' : "+coordinates+"\n}";
    }

    public void setComments(String comments) {
        this.comments = comments.replaceAll("'", "").replaceAll("\"","");
    }

    public void setLocation(String location) {
        this.location = location.replaceAll("'", "").replaceAll("\"","");
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getState() {
        return state;
    }

}
