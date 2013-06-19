package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;

public class StateWandering implements IRobotState {

	private static final String TAG = "StateWandering";
	private boolean _DEBUG = true;
	private volatile boolean isEnd = false;
	private RobotEvent cause = null;

	StateWandering(RobotEvent cause, ArrayList<RobotLog> log) {
		this.cause = cause;
	}

	@Override
	public void onStart(Context ctx) {
		if (_DEBUG) {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN, TAG);
		} else {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
		}

		FaceDetector.getInstance().start();
		isEnd = false;
	}

	@Override
	public void doAction(Context ctx) {

		if (cause != null && cause.getType() == RobotEvent.EVT_NOISE_DETECTION) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			RobotSpeech.getInstance(ctx).speak("무슨.소리지?", 1.0f, 1.0f);
		}

		try {
			int cnt=0;
			
			RobotMotion.getInstance(ctx).setLogoLEDDimming(2);

			while (!isEnd) {
				int rand = (int) (Math.random() * 70l);
				if (rand == 1)
					RobotSpeech.getInstance(ctx).speak("고!", 1.0f, 1.3f);
				
				if(++cnt==10) {
					rand = (int) (Math.random() * 5l);
					if(rand==0) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).goForward(1, 3);
					}
					else if(rand==1) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).goBack(1, 3);
					}
					else if(rand==2) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).turnRight(1);
					}
					else if(rand==3) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).turnRight(1);
					}
					else if(rand==4) {
						RobotMotion.getInstance(ctx).stopWheel();
					}
					
					rand = (int) (Math.random() * 5l);
					if(rand==0) {
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT, 0.3f);
					}
					else if(rand==1) {
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT, 0.3f);
					}
					else if(rand==2) {
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT, 0.3f);
					}
					else if(rand==3) {
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT, 0.3f);
					}
					else if(rand==4) {
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT, 0.0f);
					}
					cnt=0;
				}

				Thread.sleep(100);
			}// :end of while

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cleanUp(Context ctx) {
		FaceDetector.getInstance().stop();
		// RobotMotion.getInstance(ctx).stopFreeMove();
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).offAllLed();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {

		isEnd = true;
	}

}
