package com.kt.smartKibot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

//	NoiseDetector.getInstance(ctx).start();
	setTarget(ctx, 2, "target2.jpg");
	setTarget(ctx, 3, "target3.jpg");
	
	FaceDetector.getInstance(ctx).start();
		BTMotionDetector.getInstance(ctx).setTargetDevName("Woogic");
		BTMotionDetector.getInstance(ctx).start();
	isEnd = false;
    }

    StateLookAround(RobotEvent cause, ArrayList<RobotLog> log) {

	this.cause = cause;

    }

    @Override
    public void doAction(Context ctx) {

	    RobotSpeech.getInstance(ctx).speak("어? 무슨소리가 났는데", 1.0f, 1.0f);
//	if (cause != null && cause.getType() == RobotEvent.EVT_NOISE_DETECTION) {
//	    try {
//		//Thread.sleep(200);
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    }
//
//	    RobotSpeech.getInstance(ctx).speak("어? 무슨소리지?", 1.0f, 1.0f);
//	}

	RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
	try {
	    int angle = 0;
	    int direction = (int) (Math.random() * 2) + 1;
	    int cnt=0;
	    RobotMotion.getInstance(ctx).goForward(1, 3);
	    while (!isEnd) {
		if (FaceDetector.getInstance(ctx).isDetected()) {
			if(cnt++==0)
				 RobotSpeech.getInstance(ctx).speak("거기 있니?", 1.0f, 1.1f);
//		    RobotMotion.getInstance(ctx).headRoll(angle + direction * 5, 0.1f);
		} else {
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
	// try {
	// int direction = 1;
	// int cnt = 0;
	//
	// while (!isEnd && !FaceDetector.getInstance().isDetected()) {
	// if (cnt == 0) {
	// RobotMotion.getInstance(ctx).goForward(1, 3);
	// } else if (cnt == 10) {
	// direction = (int) (Math.random() * 2) + 1;
	// RobotMotion.getInstance(ctx).headWithSpeed(direction, 0.01f);
	// } else if ((cnt - 10) % 60 == 0) {
	// direction = (direction == 1 ? 2 : 1);
	// RobotMotion.getInstance(ctx).headWithSpeed(direction, 0.01f);
	// RobotSpeech.getInstance(ctx).speak("응?", 1.0f, 1.1f);
	// }
	// ++cnt;
	// Thread.sleep(100);
	// }
	// RobotMotion.getInstance(ctx).headWithSpeed(direction, 0.1f);
	// try { Thread.sleep(1000); } catch (InterruptedException e) {}
	// RobotMotion.getInstance(ctx).stopAll();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
    }

    @Override
    public void cleanUp(Context ctx) {
//	NoiseDetector.getInstance(ctx).stop();
	FaceDetector.getInstance(ctx).stop();
	RobotMotion.getInstance(ctx).stopAll();
	RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
		BTMotionDetector.getInstance(ctx).stop();
    }

    @Override
    public void onChanged(Context ctx) {
	isEnd = true;
    }

    private Bitmap getBitmapFromAsset(Context context, String strName) {
	AssetManager assetManager = context.getAssets();
	try {
	    InputStream is = assetManager.open("targets/" + strName);
	    return BitmapFactory.decodeStream(is);
	} catch (IOException e) {
	    Log.e(TAG, "bitmap target null");
	    return null;
	}
    }

    private void setTarget(Context ctx, int id, String assetName) {
	Bitmap target = getBitmapFromAsset(ctx, assetName);
	if (target != null) {
	    FaceDetector.getInstance(ctx).setTarget(id, target);
	}
    }

}
