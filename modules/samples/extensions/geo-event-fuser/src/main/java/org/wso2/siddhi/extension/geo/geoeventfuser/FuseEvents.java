/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.siddhi.extension.geo.geoeventfuser;

import org.apache.commons.logging.LogFactory;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;

public class FuseEvents extends WindowProcessor {

    private String variable = "";
    private int variablePosition = 0;
    private HashMap<String, ArrayList<StreamEvent>> eventsBuffer = null;

    private static final Log log = LogFactory.getLog(FuseEvents.class);

    @Override

    /**
     * Method called when initialising the extension
     */

    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length != 1) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to geo:fuseEvents(eventId) " +
                    "function, required 1, but found " + attributeExpressionExecutors.length);
        }
        variable = ((VariableExpressionExecutor) attributeExpressionExecutors[0]).getAttribute().getName();
        eventsBuffer = new HashMap<String, ArrayList<StreamEvent>>();
        variablePosition = inputDefinition.getAttributePosition(variable);
    }

    @Override
    /**
     *This method called when processing an event list
     */

    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
            StreamEventCloner streamEventCloner) {

        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = streamEventChunk.next();
            String eventId = (String) streamEvent.getOutputData()[variablePosition];

            if (eventsBuffer.containsKey(eventId)) {
                eventsBuffer.get(eventId).add(streamEvent);

                if (eventsBuffer.get(eventId).size() == getDeployedExecutionPlansCount()) {
                    // Do the fusion here and return combined event
                    fuseEvent(streamEvent);
                    eventsBuffer.remove(eventId);
                } else{
                    streamEventChunk.remove();
                }

            } else if (getDeployedExecutionPlansCount().equals(1)) {
                // This is a special case,
                // where we do not need to fuse(combine) multiple events(because actually we don't get multiple events) so just doing a pass through
                nextProcessor.process(streamEventChunk);
            } else {
                ArrayList<StreamEvent> buffer = new ArrayList<StreamEvent>();
                buffer.add(streamEvent);
                eventsBuffer.put(eventId, buffer);
                streamEventChunk.remove();
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    public Integer getDeployedExecutionPlansCount() {
        return ExecutionPlansCount.getNumberOfExecutionPlans();
    }

    public void fuseEvent(StreamEvent event) {

    /*
        * --For reference--
        * -Precedence of states-
        * goes low to high from LHS to RHS
        *
        * OFFLINE < NORMAL < WARNING < ALERT
        * */


    /*
        * --For reference--
        *   Higher the index higher the Precedence
        *   States are in all caps to mimics that states not get on the way
        *
        * */

        String[] statesArray = new String[] { "OFFLINE", "NORMAL", "WARNING", "ALERTED" };
        List<String> states = Arrays.asList(statesArray);

        Object[] data = event.getOutputData();

        String finalState = "";
        String information = "";

        String eventId = (String) event.getOutputData()[variablePosition];
        ArrayList<StreamEvent> receivedEvents = eventsBuffer.get(eventId);

        String alertStrings = "";
        String warningStrings = "";

        Integer currentStateIndex = -1;

        for (StreamEvent receivedEvent : receivedEvents) {
            String thisState = (String) receivedEvent.getOutputData()[8];
            Integer thisStateIndex = states.indexOf(thisState);

            if (thisStateIndex > currentStateIndex) {
                finalState = thisState;
                currentStateIndex = thisStateIndex;
            }

            if (thisState.equals("ALERTED")) {
                alertStrings += "," + receivedEvent.getOutputData()[9];
            } else if (thisState.equals("WARNING")) {
                warningStrings += "," + receivedEvent.getOutputData()[9];
            }
        }

        if (finalState.equals("NORMAL")) {
            information = "Normal driving pattern";
        } else {
            if (!alertStrings.isEmpty()) {
                information = "Alerts: " + alertStrings;
            }
            if (!warningStrings.isEmpty()) {
                information += " | " + "Warnings: " + warningStrings;
            }
        }

        Object[] dataOut = new Object[] {
                data[0], // id
                Double.parseDouble(data[1].toString()), // Latitude
                Double.parseDouble(data[2].toString()), // Longitude
                Long.parseLong(data[3].toString()), // TimeStamp
                data[4],//type
                Float.parseFloat(data[5].toString()), // Speed
                Float.parseFloat(data[6].toString()), // Heading
                eventId,
                finalState,
                information
        };

        event.setOutputData(dataOut);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[] { eventsBuffer };

    }

    @Override
    public void restoreState(Object[] state) {

    }
}


