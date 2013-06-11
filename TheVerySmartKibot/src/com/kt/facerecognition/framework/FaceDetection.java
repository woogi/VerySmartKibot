/// @file     FaceDetection.java
/// @brief   FaceDetection class, it is implemented for only face detection.

package com.kt.facerecognition.framework;

import android.graphics.Rect;
import android.util.Log;

import com.kt.facerecognition.wrap.facedetectionJNI;

public class FaceDetection {
    private static final String TAG = "FaceDetection";
    private final static int MAX_FACE_NUM = 20;
    private byte[] mYUVCameraData = null;

    // / @brief detected face number.
    public int detectedFaceNumber;
    // / @brief deprecated.
    public boolean isRunningThread;
    // / @brief Rect array for detected face region.
    public Rect[] detectedFacePostion;
    // / @brief y position for Top Head, Array count is "detectedFaceNumber"
    // attribute.
    public int[] mVerticalPointTopHead;

    // / @brief constructor
    // / @param N/A
    // / @return N/A
    public FaceDetection() {
	facedetectionJNI.initializeFD();
	detectedFacePostion = new Rect[MAX_FACE_NUM];
	mVerticalPointTopHead = new int[MAX_FACE_NUM];
	isRunningThread = false;
    }

    // / @brief set xml file path from
    // /data/data/getPackageName()/detectiondata/.xml
    // / @param 1. String: face training file path
    // / @param 2. String: eye training file path
    // / @return void
    public void setTrainingFilePath(String faceDetectionPath,
	    String eyeDetectionPath) {
	facedetectionJNI.setTrainingFilePath(faceDetectionPath,
		eyeDetectionPath);
    }

    // / @brief set camera preview resolution
    // / @param 1. int: width
    // / @param 2. int: height
    // / @return void
    public void setCamPreviewSize(int width, int height) {
	facedetectionJNI.setPreviewSize(width, height);
    }

    // / @brief set the region of interest to detect face object
    // / @param 1. int: top
    // / @param 2. int: left
    // / @param 3. int: right
    // / @param 4. int: bottom
    // / @return void
    public void setROI(int left, int top, int right, int bottom) {
	facedetectionJNI.setImageROI(left, top, right, bottom);
    }

    // / @brief set minimum face size to be detect
    // / @param 1. int: width
    // / @param 2. int: height
    // / @return void
    public void setMinimumDetectionSize(int width) {
	facedetectionJNI.setMinimumDetectionSize(width);
    }

    // / @brief deprecated
    public void getFaceRectThreadStart(byte inputdata[]) {
	isRunningThread = true;
	if (mYUVCameraData == null)
	    mYUVCameraData = new byte[inputdata.length];
	System.arraycopy(inputdata, 0, mYUVCameraData, 0, inputdata.length);

	Thread background = new Thread(new Runnable() {
	    public void run() {
		getFaceRect();
	    }
	});
	background.start();
    }

    // / @brief deprecated
    public Rect[] getFaceRect() {
	return getFaceRect(mYUVCameraData);
    }

    // / @brief get detected face region
    // / @param 1. byte[]: yuv data from camera preview callback
    // / @return Rect[]; Rect array for detected face region. Refer to
    // MAX_FACE_NUM for maximum array count.
    // / And detected face number is "detectedFaceNumber" public attribute.
    public Rect[] getFaceRect(byte inputdata[]) {
	int positionRect[] = new int[4 * MAX_FACE_NUM];
	// int positionTopHead[] = new int[MAX_FACE_NUM];

	detectedFaceNumber = facedetectionJNI.getFaceRectFromYUV(inputdata,
		positionRect);

	Log.i(TAG, "getFaceRect(inpudata) :  detectedFaceNumber = "
		+ detectedFaceNumber);

	for (int loop = 0; loop < detectedFaceNumber; loop++) {
	    // Rect(int left, int top, int right, int bottom)
	    int offset = (loop * 4);
	    detectedFacePostion[loop] = new Rect(positionRect[offset],
		    positionRect[offset + 1], positionRect[offset + 2],
		    positionRect[offset + 3]);
	}
	return detectedFacePostion;
    }

    // / @brief get detected face region
    // / @param 1. String: input image file path
    // / @return Rect[]; Rect array for detected face region. Refer to
    // MAX_FACE_NUM for maximum array count.
    // / And detected face number is "detectedFaceNumber" public attribute.
    public Rect[] getFaceRect(String imageFilePath) {
	int positionRect[] = new int[4 * MAX_FACE_NUM];
	detectedFaceNumber = facedetectionJNI.getFaceRectFromFile(
		imageFilePath, positionRect);

	Log.i(TAG, "getFaceRect(filepath) :  detectedFaceNumber = "
		+ detectedFaceNumber);

	for (int loop = 0; loop < detectedFaceNumber; loop++) {
	    // Rect(int left, int top, int right, int bottom)
	    int offset = (loop * 4);
	    detectedFacePostion[loop] = new Rect(positionRect[offset],
		    positionRect[offset + 1], positionRect[offset + 2],
		    positionRect[offset + 3]);
	}
	return detectedFacePostion;
    }

    // / @brief draw face region and get rgb data array with it.
    // / @param 1. byte[]: yuv data from camera preview callback
    // / @param 1. int[]: rgba data with face region as rect...
    // / @return void
    public void drawFaceRect(byte inputYUV[], int[] outputRGBA) {
	facedetectionJNI.drawFaceRect(inputYUV, outputRGBA);
    }

    // / @brief get top point for faces to measure people's height
    // / @param: void
    // / @return int[]: y position value for face Rect[i] index, if eye is not
    // detected, this value will be less than 0.

    public int[] getTopHeadPosition() {
	facedetectionJNI.getTopHeadPosition(mVerticalPointTopHead);
	return mVerticalPointTopHead;
    }

    // / @brief close loaded training data when application is finished.
    // / @param N/A
    // / @return void
    public void close() {
	facedetectionJNI.finalizeFD();
    }

}
