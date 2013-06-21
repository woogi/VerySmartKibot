package com.kt.smartKibot;

import android.content.Context;
import android.util.Log;

public class StateFollowing implements IRobotState {

    private boolean _DEBUG = true;
    private static final String TAG = "StateFollowing";
    private volatile boolean isEnd = false;

    @Override
    public void onStart(Context ctx) {
	Log.i(TAG,"on start");
	if (_DEBUG) {
	    RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED, TAG);
	} else {
	    RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED);
	}
	isEnd = false;
    }

    @Override
    public void doAction(Context ctx) {
	Log.i(TAG, "do action");
	try {
	    int cnt = 0;
	    while (!isEnd) {
		if (cnt == 0) {
		    RobotMotion.getInstance(ctx).goForward(1, 5);
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
	Log.i(TAG, "clean up");
	RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
    }

    @Override
    public void onChanged(Context ctx) {
	Log.i(TAG, "on changed");
	isEnd = true;
    }

}
