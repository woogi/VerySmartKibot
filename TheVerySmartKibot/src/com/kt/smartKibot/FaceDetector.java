package com.kt.smartKibot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

public class FaceDetector implements IRobotEvtDelegator,
	FaceCameraSurface.OnFaceDetectListener {

    private static final String TAG = "FaceDetector";
    private static boolean faceDetected;
    private static FaceDetector instance;
    private FaceCameraSurface cameraSurface;
    private IRobotEvtHandler handler;

	static final Lock lock= new ReentrantLock(); 
	
    @Override
    synchronized public void onFaceDetect() {
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
    synchronized public void start() {
	Log.i(TAG, "start");
	//lock.lock();
	cameraSurface = RobotActivity.addCameraSurface();
	if (cameraSurface != null) {
	    cameraSurface.setOnFaceDetectListener(this);
	    cameraSurface.start();
	}
	faceDetected = false;
    }

    @Override
    synchronized public void stop() {
	Log.i(TAG, "stop");
	if (cameraSurface != null) {
	    cameraSurface.stopSample();
	    RobotActivity.removeCameraSurface();
	    cameraSurface = null;
	 //   lock.unlock();
	}
    }

    public static FaceDetector getInstance() {
	Log.i(TAG, "instance is " + instance);
	if (instance == null) {
	    instance = new FaceDetector();
	}
	return instance;
    }

    public static boolean hasDetectedAFace() {
	Log.i(TAG, "hasDetectedAFace is " + faceDetected);
	return faceDetected;
    }
}