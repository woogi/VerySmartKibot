package com.kt.smartKibot;

import android.content.Context;
import android.util.Log;

public class StateFollowing implements IRobotState {

    private boolean _DEBUG = true;
    private static final String TAG = "StateFollowing";
    private volatile boolean isEnd = false;

    private int angle;

    @Override
    public void onStart(Context ctx) {
	Log.i(TAG, "on start");
	if (_DEBUG) {
	    RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED, TAG);
	} else {
	    RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED);
	}
	FaceRecognizer.getInstance(ctx).start();
	RobotMotion.getInstance(ctx).stopAll();
	angle = 0;
	isEnd = false;
    }

    @Override
    public void doAction(final Context ctx) {
	Log.i(TAG, "do action");
	RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
	try {
	    while (!isEnd) {
		switch (FaceRecognizer.getInstance(ctx).getDirection()) {
		case CamConf.STOP:
		    Log.i(TAG, "STOP <following>");
		    new Thread(new Runnable() {
			@Override
			public void run() {
			    if (angle < 0) {
				RobotMotion.getInstance(ctx)
					.turnLeft(1, -angle);
			    } else {
				RobotMotion.getInstance(ctx)
					.turnRight(1, angle);
			    }
			    angle = 0;
			}
		    }).start();
		    RobotMotion.getInstance(ctx).stopAll();
		    break;
		case CamConf.FWD:
		    Log.i(TAG, "FWD <following>");
		    RobotMotion.getInstance(ctx).goForward(1);
		    break;
		case CamConf.BACK:
		    Log.i(TAG, "BACK <following>");
		    RobotMotion.getInstance(ctx).goBack(1);
		    break;
		case CamConf.LEFT:
		    Log.i(TAG, "LEFT <following> (" + angle + ")");
		    RobotMotion.getInstance(ctx).stopAll();
		    angle--;
		    RobotMotion.getInstance(ctx).headRoll(angle, 0.1f);
		    if (angle == -30) {
			new Thread(new Runnable() {
			    @Override
			    public void run() {
				RobotMotion.getInstance(ctx)
					.turnLeft(1, -angle);
				angle = 0;
			    }
			}).start();
			RobotMotion.getInstance(ctx).stopAll();
			try {
			    Thread.sleep(1000);
			} catch (InterruptedException e1) {
			    e1.printStackTrace();
			}
		    }
		    break;
		case CamConf.RIGHT:
		    Log.i(TAG, "RIGHT <following> (" + angle + ")");
		    RobotMotion.getInstance(ctx).stopAll();
		    angle++;
		    RobotMotion.getInstance(ctx).headRoll(angle, 0.1f);
		    if (angle == 30) {
			new Thread(new Runnable() {
			    @Override
			    public void run() {
				RobotMotion.getInstance(ctx)
					.turnRight(1, angle);
				angle = 0;
			    }
			}).start();
			RobotMotion.getInstance(ctx).stopAll();
			try {
			    Thread.sleep(1000);
			} catch (InterruptedException ie) {
			    ie.printStackTrace();
			}
		    }
		    break;
		case CamConf.LOST:
		    new Thread(new Runnable() {
			@Override
			public void run() {
			    if (angle < 0) {
				RobotMotion.getInstance(ctx)
					.turnLeft(1, -angle);
			    } else {
				RobotMotion.getInstance(ctx)
					.turnRight(1, angle);
			    }
			    angle = 0;
			}
		    }).start();
		    RobotMotion.getInstance(ctx).stopAll();
		    break;
		}
		Thread.sleep(100);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void cleanUp(Context ctx) {
	Log.i(TAG, "clean up");
	FaceRecognizer.getInstance(ctx).stop();
	RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	RobotMotion.getInstance(ctx).stopAll();

    }

    @Override
    public void onChanged(Context ctx) {
	Log.i(TAG, "on changed");
	isEnd = true;
    }
}
