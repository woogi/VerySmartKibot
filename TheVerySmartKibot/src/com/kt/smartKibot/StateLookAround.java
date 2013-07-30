package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class StateLookAround implements IRobotState {

    private static final String TAG = "StateLookAround";
    private boolean _DEBUG = true;
    private volatile static boolean isEnd = false;
    private RobotEvent cause = null;

    @Override
    public void onStart(Context ctx) {
	// TODO Auto-generated method stub
	if (_DEBUG) {

	    RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION, TAG);
	} else {
	    RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION);
	}

	NoiseDetector.getInstance().start();
	FaceDetector.getInstance(ctx).start();
	isEnd = false;
    }

    StateLookAround(RobotEvent cause, ArrayList<RobotLog> log) {

	this.cause = cause;

    }

    @Override
    public void doAction(Context ctx) {

	if (cause != null && cause.getType() == RobotEvent.EVT_NOISE_DETECTION) {
	    try {
		Thread.sleep(200);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    RobotSpeech.getInstance(ctx).speak("어?", 1.0f, 1.0f);
	}

	RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
	try {
	    int angle = 0;
	    int direction = (int) (Math.random() * 2) + 1;
	    RobotMotion.getInstance(ctx).goForward(1, 3);
	    while (!isEnd) {
		if (!FaceDetector.getInstance(ctx).isDetected()) {
		    angle += direction;
		    if (direction > 0 && angle > 20) {
			direction = -1;
		    }
		    if (direction < 0 && angle < -20) {
			direction = +1;
		    }
		    RobotMotion.getInstance(ctx).headRoll(angle, 0.1f);
		}
		Thread.sleep(100);
	    }
	} catch (InterruptedException e) {
	    Log.e(TAG, e.getMessage());
	}
    }

    @Override
    public void cleanUp(Context ctx) {
	NoiseDetector.getInstance().stop();
	FaceDetector.getInstance(ctx).stop();
	RobotMotion.getInstance(ctx).stopAll();
	RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
    }

    @Override
    public void onChanged(Context ctx) {
	isEnd = true;
    }
}
