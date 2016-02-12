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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by isuru on 2/9/15.
 */
public class TrafficXMLHandler extends DefaultHandler {
    // SAX callback implementations from DocumentHandler, ErrorHandler, etc.
    private ArrayList<Disruption> list;
    private Disruption current = null;
    private StringBuilder sb = new StringBuilder();
    private boolean inLine = false;
    private boolean inPoly = false;
    private boolean startElement = true;

    public TrafficXMLHandler(ArrayList<Disruption> list) throws SAXException {
        this.list = list;
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes atts)
            throws SAXException {
        startElement = true;
        if (qName.equals("Disruption")) {
            //System.out.println("disruption");
            if (current != null) {
                current.end();
                list.add(current);
            }
            current = new Disruption();
            current.id = atts.getValue(0);
        } else if (qName.equals("Line")) {
            inLine = true;
        } else if (qName.equals("Polygon")) {
            inPoly = true;
        }
    }

    public void endElement(String uri, String localName,
                           String qName) throws SAXException {
        String string = sb.toString();
        if (qName.equals("severity")) {
            current.setSeverity(string);
        } else if (qName.equals("status")) {
            current.setState(string);
        } else if (qName.equals("location")) {
            current.setLocation(string);
        } else if (qName.equals("comments")) {
            current.setComments(string);
        } else if (qName.equals("coordinatesLL")) {
            if (inLine) {
                current.addCoordsLane(string);
            } else if (inPoly) {
                current.setCoordsPoly(string);
            }
            inLine = false;
            inPoly = false;
        }
        sb.setLength(0);
        startElement = false;
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
        if(startElement) {
            sb.setLength(0);
            sb.append(new String(ch, start, len));
            startElement = false;
        }
        else {
            sb.append(new String(ch, start, len));
        }

    }

}
