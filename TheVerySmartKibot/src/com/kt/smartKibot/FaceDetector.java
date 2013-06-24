package com.kt.smartKibot;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

public class FaceDetector implements IRobotEvtDelegator, CameraSurface.OnFaceDetectListener {

    private static final String TAG = "FaceDetector";
    
    private static CameraSurface cameraSurface;
    private static FaceDetector instance;
    
    private boolean faceDetected;
    private IRobotEvtHandler handler;

    @Override
    public void onFaceDetected(Bitmap bitmap, int detectedFaceNumber, Rect[] detectedFacePostion) {
	Log.i(TAG, "onFaceDetected");
	if (cameraSurface != null) {
	    cameraSurface.stopSearch();
	}
	RobotEvent evt = new RobotEvent(RobotEvent.EVT_FACE_DETECTION);
	handler.handle(null, evt);
	faceDetected = true;
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
//	cameraSurface = RobotActivity.getCameraSurface();
	cameraSurface = CameraSurface.getInstance(RobotActivity.getContext());
	if (cameraSurface != null) {
	    cameraSurface.setOnFaceDetectListener(this);
	    cameraSurface.start();
	}
	faceDetected = false;
    }

    @Override
    public void stop() {
	Log.i(TAG, "stop");
	if (cameraSurface != null) {
	    cameraSurface.stopSample();
	}
    }

    public static FaceDetector getInstance() {
	Log.i(TAG, "instance is " + instance);
	if (instance == null) {
	    instance = new FaceDetector();
	}
	return instance;
    }

    public boolean isFaceDetected() {
	Log.i(TAG, "isFaceDetected ? " + faceDetected);
	return faceDetected;
    }
}