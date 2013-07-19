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
    private volatile boolean isEnd = false;
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
	setTarget(ctx, 2, "target2.jpg");
	FaceDetector.getInstance().start();
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
	    int direction = 1;
	    int cnt = 0;

	    while (!isEnd) {
		if (cnt == 0) {
		    RobotMotion.getInstance(ctx).goForward(1, 3);
		} else if (cnt == 10) {
		    direction = (int) (Math.random() * 2) + 1;
		    RobotMotion.getInstance(ctx).headWithSpeed(direction, 0.1f);
		} else if ((cnt - 10) % 60 == 0) {
		    direction = (direction == 1 ? 2 : 1);
		    RobotMotion.getInstance(ctx).headWithSpeed(direction, 0.1f);
		    RobotSpeech.getInstance(ctx).speak("응?", 1.0f, 1.1f);
		}

		++cnt;
		Thread.sleep(100);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void cleanUp(Context ctx) {
	NoiseDetector.getInstance().stop();
	FaceDetector.getInstance().stop();
	RobotMotion.getInstance(ctx).stopAll();
	RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
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
	Bitmap target = null;
	target = getBitmapFromAsset(ctx, assetName);
	if (target != null) {
	    FaceDetector.getInstance().setTarget(id, target);
	}
    }

}
