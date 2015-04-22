/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.sample.objectdetection;

import nu.pattern.OpenCV;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.ssl.util.Hex;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception
        .DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.MalformedURLException;

/**
 * Client for object detecting using OpenCV and publishing as thrift.
 */
public class ObjectDetectionClient {

    /**
     * The logger to log information, warnings and errors.
     */
    private static Logger log = Logger.getLogger(ObjectDetectionClient.class);

    /**
     * Thrift data publishing stream name.
     */
    public static final String STREAM_NAME = "org.wso2.sample.objectdetection.data";

    /**
     * Thrift data publishing stream version.
     */
    public static final String STREAM_VERSION = "1.0.0";

    /**
     * The encoding format used in converting the image to a hex string.
     */
    public static final String ENCODING_FORMAT = ".jpg";

    /**
     * Loading the OpenCV native libraries
     * @see <a href="https://github.com/PatternConsulting/opencv#api">OpenCV API loading.</a>
     * @see <a href="http://docs.opencv.org/doc/tutorials/introduction/desktop_java/java_dev_intro.html#java-sample-with-ant">Native OpenCV API loading.</a>
     */
    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError ex) {
            try {
                OpenCV.loadLibrary();
            } catch (UnsatisfiedLinkError error) {
                log.error("Unable to load OpenCV library.", error);
            }
        }
    }

    /**
     * The method uses OpenCV to detect objects of an video input. The following steps are followed.
     * 1. Get the input stream. This may be a device attached to the PC, a video file path, a RTSP
     * link.
     * 2. Break the input video stream in to frame using a loop.
     * 3. Loads the {@link org.opencv.objdetect.CascadeClassifier} with the given cascade file.
     * 4. Check for detected objects using the classifier in a frame.
     * 5. If a object is detected, crop the object from the image and publish it as thrift to a data
     * stream.
     *
     * @param args The arguments passed through ant script.
     * @throws MalformedURLException                            The malformed url exception
     * @throws AgentException                                   The agent exception
     * @throws AuthenticationException                          The authentication exception
     * @throws TransportException                               The transport exception
     * @throws MalformedStreamDefinitionException               The malformed stream definition
     *                                                          exception
     * @throws StreamDefinitionException                        The stream definition exception
     * @throws DifferentStreamDefinitionAlreadyDefinedException The different stream definition
     *                                                          already defined exception
     */
    public static void main(String[] args)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException, MalformedStreamDefinitionException,
                   StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException {

        KeyStoreUtil.setTrustStoreParams();
        KeyStoreUtil.setKeyStoreParams();

        String source = args[0];
        String cascadeFile = args[1];
        int maxFrameCount = Integer.parseInt(args[2]);
        boolean preProcess = Boolean.parseBoolean(args[3]);
        double skipFrames = Double.parseDouble(args[4]);
        String host = args[5];
        String port = args[6];
        String username = args[7];
        String password = args[8];

        // New thrift data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username,
                                                        password);
        // Getting stream ID to publish data
        String streamID = getStreamID(dataPublisher);

        /* Opening a video source for to capture frames.
        * - An integer would represent a source device attached to the machine.
        * - A file path can also be given.
        * - RTSP links can also be used at video source.
        */
        VideoCapture vCap = new VideoCapture();
        if (NumberUtils.isNumber(source)) {
            vCap.open(Integer.parseInt(source));
        } else {
            vCap.open(source);
        }

        // If video source is not valid
        if (!vCap.isOpened()) {
            log.error("Incorrect source provided : " + source);
        } else {
            int tempFrameCount = 0;
            Mat frame = new Mat();
            log.info("Valid source found : " + source);

            // Loading classifier
            CascadeClassifier cClassifier = new CascadeClassifier();
            log.info("Loading classifier : " + cascadeFile);
            cClassifier.load(cascadeFile);
            if (!cClassifier.empty()) {
                while (maxFrameCount == -1 || tempFrameCount < maxFrameCount) {
                    if (vCap.read(frame)) {
                        long currentTime = System.currentTimeMillis();
                        Mat frameClone = frame.clone();

                        /*
                         * Pre-processing of the image. This helps to identify objects.
                         * 1. Grayscaling of the image (Making the image black and white)
                         * 2. Equalizing of the image histogram.
                        */
                        if (preProcess) {
                            Imgproc.cvtColor(frame, frameClone, Imgproc.COLOR_RGB2GRAY);
                            Imgproc.equalizeHist(frameClone, frameClone);
                        }

                        // Detecting objects
                        MatOfRect imageRect = new MatOfRect();
                        cClassifier.detectMultiScale(frameClone, imageRect);

                        // Incrementing frame count
                        if (!imageRect.empty()) {
                            tempFrameCount++;
                        }

                        // Looping detected objects
                        for (Rect rect : imageRect.toArray()) {
                            // Cropping image
                            Mat croppedImage = frame.submat(rect);
                            String croppedImageHex = matToHex(croppedImage);
                            // Creating payload data
                            Object[] payloadData = new Object[]{currentTime, tempFrameCount,
                                                                source, croppedImageHex,
                                                                croppedImage.type(), croppedImage
                                    .width(), croppedImage.height(), ENCODING_FORMAT, cascadeFile};

                            // Logging frame ID and timestamp
                            log.info("Sending frame " + Integer.toString(tempFrameCount) +
                                     " : Found image at " + Long.toString(currentTime));

                            // Creating thrift event and publishing
                            Event eventOne = new Event(streamID, System.currentTimeMillis(),
                                                       null, null, payloadData);
                            dataPublisher.publish(eventOne);
                            System.out.println("published");
                        }

                        // Delaying to fit siddhi window size in execution plan
                        try {
                            Thread.sleep(500 - System.currentTimeMillis() % 500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("Unable to add sleep to synchronize with the siddhi " +
                                                                                    "extension");
                        }
                    } else {
                        log.error("Frame was empty!");
                    }

                    // Skipping frames. propId = 1 (CAP_PROP_POS_FRAMES)
                    vCap.set(1, vCap.get(1) + skipFrames);
                }
            } else {
                log.error("Cascade was empty!");
            }
        }

        dataPublisher.stop();
    }

    /**
     * Converts a {@link org.opencv.core.Mat} object to a hex string.
     *
     * @param croppedImage The cropped image
     * @return The image hex string
     */
    private static String matToHex(Mat croppedImage) {
        // mat to byte array
        MatOfByte byteMatrix = new MatOfByte();
        Highgui.imencode(ENCODING_FORMAT, croppedImage, byteMatrix);
        byte[] bytes = byteMatrix.toArray();

        return Hex.encode(bytes);
    }

    /**
     * Creates and returns the stream ID for the data should be published as thrift.
     *
     * @param dataPublisher The thrift data publisher.
     * @return The stream ID to publish thrift data.
     * @throws AgentException                                   The agent exception
     * @throws MalformedStreamDefinitionException               The malformed stream definition
     *                                                          exception
     * @throws StreamDefinitionException                        The stream definition exception
     * @throws DifferentStreamDefinitionAlreadyDefinedException The different stream definition
     *                                                          already defined exception
     */
    private static String getStreamID(DataPublisher dataPublisher)
            throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException {
        // Stream definition
        // // timestamp - time which the object was identified
        // // frame_id - unique id for the frame
        // // camera_id - source
        // // image - cropped image of the detected object sent as a hex
        // // image_type - image type in opencv
        // // image_width - mat width
        // // image_height - mat height
        // // encoding_format - encoding format used to convert from mat to byte
        // // cascade - cascade file used for object detection.
        log.info("Creating stream " + STREAM_NAME + ":" + STREAM_VERSION);

        return dataPublisher.defineStream("{" +
                                          "  'name':'" +
                                          STREAM_NAME +
                                          "'," +
                                          "  'version':'" +
                                          STREAM_VERSION +
                                          "'," +
                                          "  'nickName': 'Object_Detection_Monitoring'," +
                                          "  'description': 'A sample for Object Detection " +
                                          "Monitoring'," +
                                          "  'payloadData':[" +
                                          "          {'name':'timestamp','type':'LONG'}," +
                                          "          {'name':'frame_id','type':'INT'}," +
                                          "          {'name':'camera_id','type':'STRING'}," +
                                          "          {'name':'image','type':'STRING'}," +
                                          "          {'name':'image_type','type':'INT'}," +
                                          "          {'name':'image_width','type':'INT'}," +
                                          "          {'name':'image_height','type':'INT'}," +
                                          "          {'name':'encoding_format','type':'STRING'}," +
                                          "          {'name':'cascade','type':'STRING'}" +
                                          "  ]" + "}");
    }
}
