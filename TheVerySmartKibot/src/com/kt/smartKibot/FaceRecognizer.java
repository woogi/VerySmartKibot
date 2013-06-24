package com.kt.smartKibot;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

public class FaceRecognizer implements IRobotEvtDelegator, CameraSurface.OnFaceDetectListener {

    private static final String TAG = "FaceRecognizer";

    private static CameraSurface cameraSurface;
    private static FaceRecognizer instance;

    private IRobotEvtHandler handler;

    @Override
    public void onFaceDetected(Bitmap bitmap, int detectedFaceNumber, Rect[] detectedFacePostion) {
	Log.i(TAG, "onFaceDetected");
	if (cameraSurface != null) {
	    cameraSurface.stopSearch();
	}
	RobotEvent evt = new RobotEvent(RobotEvent.EVT_FACE_RECOGNITION);
	handler.handle(null, evt);
    }

    @Override
    public void installHandler(IRobotEvtHandler handler) {
	Log.i(TAG, "installHandler");
	this.handler = handler;
    }

    @Override
    public void uninstallHandler() {
	Log.i(TAG, "uninstallHandler");
	stop();
	handler = null;	
    }

    @Override
    public void start() {
	Log.i(TAG, "start");
	cameraSurface = RobotActivity.getCameraSurface();
	if (cameraSurface != null) {
	    cameraSurface.setOnFaceDetectListener(this);
	    cameraSurface.start();
	}
    }

    @Override
    public void stop() {
	Log.i(TAG, "stop");
	if (cameraSurface != null) {
	    cameraSurface.stopSample();
	}
    }

    public static FaceRecognizer getInstance(){
	Log.i(TAG, "instance is " + instance);
	if (instance == null) {
	    instance = new FaceRecognizer();
	}
	return instance;
    }

    private class DetectedFaceProperties {
	    public Rect rect;
	    public String tagName;
	    public int friendLevel;
	    public Uri imageUri;

	    public DetectedFaceProperties() {
		rect = null;
		tagName = null;
		friendLevel = 0;
		imageUri = null;
	    }
	}
}
