package com.kt.smartKibot;

import android.util.Log;

public class FaceDetector implements IRobotEvtDelegator,
	FaceCameraSurface.OnFaceDetectListener {

    private static final String TAG = "FaceDetector";
    private static boolean faceDetected;
    private static FaceDetector instance;
    private FaceCameraSurface faceSurface;
    private IRobotEvtHandler handler;

    @Override
    public void onFaceDetect() {
	Log.i(TAG, "onFaceDetected");
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
	faceSurface = RobotActivity.getFaceSurface();
	if (faceSurface != null) {
	    faceSurface.start();
	    faceSurface.setOnFaceDetectListener(this);
	}
	faceDetected = false;
    }

    @Override
    public void stop() {
	Log.i(TAG, "stop");
	if (faceSurface != null) {
	    faceSurface.stop();
	}
	faceSurface = null;
    }

    public static FaceDetector getInstance() {
	Log.i(TAG, "instance " + instance);
	if (instance == null) {
	    instance = new FaceDetector();
	}
	return instance;
    }

    public static boolean hasDetectedAFace() {
	Log.i(TAG, "hasDetectedAFace " + faceDetected);
	return faceDetected;
    }
}