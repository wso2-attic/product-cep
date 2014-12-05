/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.sample.objectdetection.server.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to Store Stream Data.
 */
public class StreamData implements Comparable<StreamData> {

	/** The timestamp. */
	private long timestamp;

	/** The frame id. */
	private int frameID;

	/** The camera id. */
	private String cameraID;

	/** The image. */
	private List<String> images;

	/** The image type. */
	private int imageType;

	/** The image width. */
	private int imageWidth;

	/** The image height. */
	private int imageHeight;

	/** The encoding type. */
	private String encodingType;

	/** The cascade. */
	private String cascade;

	/** The object count. */
	private long objectCount;

	/**
	 * Instantiates a new stream data.
	 */
	public StreamData() {
		images = new ArrayList<String>();
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp
	 *            the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the frame id.
	 *
	 * @return the frame id
	 */
	public int getFrameID() {
		return frameID;
	}

	/**
	 * Sets the frame id.
	 *
	 * @param frameID
	 *            the new frame id
	 */
	public void setFrameID(int frameID) {
		this.frameID = frameID;
	}

	/**
	 * Gets the camera id.
	 *
	 * @return the camera id
	 */
	public String getCameraID() {
		return cameraID;
	}

	/**
	 * Sets the camera id.
	 *
	 * @param cameraID
	 *            the new camera id
	 */
	public void setCameraID(String cameraID) {
		this.cameraID = cameraID;
	}

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public List<String> getImages() {
		return images;
	}

	/**
	 * Sets the image.
	 *
	 * @param images
	 *            the new image
	 */
	public void setImage(List<String> images) {
		this.images = images;
	}

	/**
	 * Gets the image type.
	 *
	 * @return the image type
	 */
	public int getImageType() {
		return imageType;
	}

	/**
	 * Sets the image type.
	 *
	 * @param imageType
	 *            the new image type
	 */
	public void setImageType(int imageType) {
		this.imageType = imageType;
	}

	/**
	 * Gets the image width.
	 *
	 * @return the image width
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * Sets the image width.
	 *
	 * @param imageWidth
	 *            the new image width
	 */
	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	/**
	 * Gets the image height.
	 *
	 * @return the image height
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * Sets the image height.
	 *
	 * @param imageHeight
	 *            the new image height
	 */
	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	/**
	 * Gets the encoding type.
	 *
	 * @return the encoding type
	 */
	public String getEncodingType() {
		return encodingType;
	}

	/**
	 * Sets the encoding type.
	 *
	 * @param encodingType
	 *            the new encoding type
	 */
	public void setEncodingType(String encodingType) {
		this.encodingType = encodingType;
	}

	/**
	 * Gets the cascade.
	 *
	 * @return the cascade
	 */
	public String getCascade() {
		return cascade;
	}

	/**
	 * Sets the cascade.
	 *
	 * @param cascade
	 *            the new cascade
	 */
	public void setCascade(String cascade) {
		this.cascade = cascade;
	}

	/**
	 * Gets the object count.
	 *
	 * @return the object count
	 */
	public long getObjectCount() {
		return objectCount;
	}

	/**
	 * Sets the object count.
	 *
	 * @param objectCount
	 *            the new object count
	 */
	public void setObjectCount(long objectCount) {
		this.objectCount = objectCount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(StreamData o) {
		if (this.frameID > o.frameID) {
			return 1;
		} else if (this.frameID < o.frameID) {
			return -1;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Integer.toString(frameID);
	}
}
