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
package org.wso2.carbon.sample.videoframes;

import java.net.MalformedURLException;

import org.apache.commons.lang.NumberUtils;
import org.apache.commons.ssl.util.Hex;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

/**
 * Object detecting client using OpenCV
 */
public class VideoFrameProducer {

	/** The logger. */
	private static Logger log = Logger.getLogger(VideoFrameProducer.class);

	/** Stream name. */
	public static final String STREAM_NAME = "org.wso2.sample.video.frames";

	/** Stream version. */
	public static final String VERSION1 = "1.0.0";

	/** The Constant encodingFormat. */
	public static final String encodingFormat = ".jpg";

	// loading native libraries for opencv
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws MalformedURLException
	 *             the malformed url exception
	 * @throws AgentException
	 *             the agent exception
	 * @throws AuthenticationException
	 *             the authentication exception
	 * @throws TransportException
	 *             the transport exception
	 * @throws MalformedStreamDefinitionException
	 *             the malformed stream definition exception
	 * @throws StreamDefinitionException
	 *             the stream definition exception
	 * @throws DifferentStreamDefinitionAlreadyDefinedException
	 *             the different stream definition already defined exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static void main(String[] args) throws MalformedURLException, AgentException,
	AuthenticationException, TransportException,
	MalformedStreamDefinitionException,
	StreamDefinitionException,
	DifferentStreamDefinitionAlreadyDefinedException,
	InterruptedException {

		KeyStoreUtil.setTrustStoreParams();
		String source = args[0];
		String cascadeFile = args[1];
		int maxFrameCount = Integer.parseInt(args[2]);
		double skipFrames = Double.parseDouble(args[3]);
		String host = args[4];
		String port = args[5];
		String username = args[6];
		String password = args[7];

		// new data publisher
		DataPublisher dataPublisher =
				new DataPublisher("tcp://" + host + ":" + port, username,
				                  password);
		// getting stream id
		String streamID = getStreamID(dataPublisher);

		// opening a video source for to capture frames.
		// an integer would represent a source device attached to the
		// machine.
		// a file path can also be given.
		// rtsp links can also be used at video source.
		VideoCapture vCap = new VideoCapture();
		if (NumberUtils.isNumber(source)) {
			vCap.open(Integer.parseInt(source));
		} else {
			vCap.open(source);
		}

		// if video source is valid
		if (!vCap.isOpened()) {
			log.error("Incorrect source provided : " + source);
		} else {
			int tempFrameCount = 0;
			Mat frame = new Mat();
			log.info("Valid source found : " + source);
			while (maxFrameCount == -1 || tempFrameCount < maxFrameCount) {
				if (vCap.read(frame)) {
					tempFrameCount++;
					long currentTime = System.currentTimeMillis();
					String croppedImageHex = matToHex(frame);
					// payload data
					Object[] payloadData =
							new Object[] { currentTime, tempFrameCount, source,
							               croppedImageHex, cascadeFile };

					// logging
					log.info("Sending frame " + Integer.toString(tempFrameCount) +
					         " : Found frame at " + Long.toString(currentTime));

					// creating event and publishing
					Event eventOne =
							new Event(streamID, System.currentTimeMillis(), null, null,
							          payloadData);
					dataPublisher.publish(eventOne);

				} else {
					log.error("Frame was empty!");
				}

				// skipping frames. propId = 1 (CAP_PROP_POS_FRAMES)
				vCap.set(1, vCap.get(1) + skipFrames);
			}
		}

		dataPublisher.stop();
	}

	/**
	 * Mat to hex string.
	 *
	 * @param croppedImage
	 *            the cropped image
	 * @return the hex string
	 */
	private static String matToHex(Mat croppedImage) {
		// mat to byte array
		MatOfByte bytemat = new MatOfByte();
		Highgui.imencode(encodingFormat, croppedImage, bytemat);
		byte[] bytes = bytemat.toArray();

		return Hex.encode(bytes);
	}

	/**
	 * Gets the stream id.
	 *
	 * @param dataPublisher
	 *            the data publisher
	 * @return the stream id
	 * @throws AgentException
	 *             the agent exception
	 * @throws MalformedStreamDefinitionException
	 *             the malformed stream definition exception
	 * @throws StreamDefinitionException
	 *             the stream definition exception
	 * @throws DifferentStreamDefinitionAlreadyDefinedException
	 *             the different stream definition already defined exception
	 */
	private static String getStreamID(DataPublisher dataPublisher)
			throws AgentException,
			MalformedStreamDefinitionException,
			StreamDefinitionException,
			DifferentStreamDefinitionAlreadyDefinedException {
		// Stream definition
		// // timestamp - time which the object was identified
		// // frame_id - unique id for the frame
		// // camera_id - source
		// // image - cropped image of the detected object sent as a hex
		log.info("Creating stream " + STREAM_NAME + ":" + VERSION1);
		String streamId =
				dataPublisher.defineStream("{" +
						"  'name':'" +
						STREAM_NAME +
						"'," +
						"  'version':'" +
						VERSION1 +
						"'," +
						"  'nickName': 'Video_Frame_Feeding'," +
						"  'description': 'A sample for Video Frame Feeding'," +
						"  'payloadData':[" +
						"          {'name':'timestamp','type':'LONG'}," +
						"          {'name':'frame_id','type':'INT'}," +
						"          {'name':'camera_id','type':'STRING'}," +
						"          {'name':'image','type':'STRING'}," +
						"          {'name':'cascade','type':'STRING'}" +
						"  ]" + "}");

		return streamId;
	}
}
