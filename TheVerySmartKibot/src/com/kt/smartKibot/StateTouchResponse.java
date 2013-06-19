package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;

public class StateTouchResponse implements IRobotState {

	int touchWhere = -1;
	ArrayList<RobotLog> history = null;
	private boolean _DEBUG = true;
	private String TAG = "StateTouchResponse";
	private volatile boolean isEnd = false;

	StateTouchResponse(int where, ArrayList<RobotLog> log) {
		touchWhere = where;
		history = log;
	}

	@Override
	public void onStart(Context ctx) {
		switch (touchWhere) {
		case TouchDetector.PARAM_LEFT_EAR_PATTED:
		case TouchDetector.PARAM_LEFT_FOOT_PRESSED:

			if (_DEBUG) {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_LEFT, TAG);
			} else {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_LEFT);
			}
			break;

		case TouchDetector.PARAM_RIGHT_EAR_PATTED:
		case TouchDetector.PARAM_RIGHT_FOOT_PRESSED:

			if (_DEBUG) {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_RIGHT,TAG);
			} else {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_RIGHT);
			}
			break;

		}
		
		isEnd = false;
	}

	@Override
	public void doAction(Context ctx) {
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
		RobotSpeech.getInstance(ctx).speak("어머!", 1.2f, 1.1f);

		try {
			int cnt=0;

			switch (touchWhere) {
			case TouchDetector.PARAM_LEFT_EAR_PATTED:
			case TouchDetector.PARAM_LEFT_FOOT_PRESSED:
				while(!isEnd) {
					if(cnt==0) {
						RobotMotion.getInstance(ctx).headRoll(10f, 1.0f);
					}
					else if(cnt==20) {
						RobotMotion.getInstance(ctx).turnLeft(1);
					}
					else if(cnt==30) {
						break;
					}
					++cnt;
					Thread.sleep(100);
				}
				break;

			case TouchDetector.PARAM_RIGHT_EAR_PATTED:
			case TouchDetector.PARAM_RIGHT_FOOT_PRESSED:
				while(!isEnd) {
					if(cnt==0) {
						RobotMotion.getInstance(ctx).headRoll(-10f, 1.0f);
					}
					else if(cnt==20) {
						RobotMotion.getInstance(ctx).turnRight(1);
					}
					else if(cnt==30) {
						break;
					}
					++cnt;
					Thread.sleep(100);
				}
				break;
			}
			
			cnt=0;
			while(!isEnd) {
				if(cnt==0) {
					RobotMotion.getInstance(ctx).goForward(1, 3);
					RobotSpeech.getInstance(ctx).speak("아하!", 1.1f, 1.1f);
				}
				else if(cnt%40==0) {
					RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT,0.2f);
				}
				else if(cnt%20==0) {
					RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT,0.2f);
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
		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
	}

}
