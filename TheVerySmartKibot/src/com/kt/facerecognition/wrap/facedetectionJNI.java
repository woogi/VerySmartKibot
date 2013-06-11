package com.kt.facerecognition.wrap;

import android.util.Log;

public class facedetectionJNI {

    static {
	try {
	    System.loadLibrary("facerecognition");
	} catch (Exception exception) {
	    Log.i("nicolas", "lib err load");
	    throw new RuntimeException("Can not load facedetection library",
		    exception);
	}
    }

    public static native void initializeFD();

    public static native void setTrainingFilePath(String faceTrainingPath,
	    String eyeTrainingPath);

    public static native void setPreviewSize(int width, int height);

    public static native void setImageROI(int left, int top, int right,
	    int bottom);

    public static native void setMinimumDetectionSize(int width);

    public static native void drawFaceRect(byte inputYUV[], int[] outputRGBA);

    public static native int getFaceRectFromYUV(byte inputdata[],
	    int[] positionRect);

    public static native void getTopHeadPosition(int[] positionTopHead);

    public static native int getFaceRectFromFile(String inputImagePath,
	    int[] positionRect);

    public static native void finalizeFD();
}
