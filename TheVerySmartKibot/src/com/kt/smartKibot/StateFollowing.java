package com.kt.smartKibot;

import android.content.Context;
import android.util.Log;

public class StateFollowing implements IRobotState {

	private boolean _DEBUG = true;
	private static final String TAG = "StateFollowing";
	private volatile boolean isEnd = false;

	@Override
	public void onStart(Context ctx) {
		Log.i(TAG, "on start");
		if (_DEBUG) {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED, TAG);
		} else {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED);
		}
		FaceRecognizer.getInstance().start();
		isEnd = false;
	}

	@Override
	public void doAction(Context ctx) {
		Log.i(TAG, "do action");
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
		try {
			while (!isEnd) {
				// TODO Robot Motion
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cleanUp(Context ctx) {
		Log.i(TAG, "clean up");
		FaceRecognizer.getInstance().stop();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		Log.i(TAG, "on changed");
		isEnd = true;
	}

}
