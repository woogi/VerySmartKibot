package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;

public class StateWandering implements IRobotState {

	private static final String TAG = "StateWandering";
	private boolean _DEBUG = true;
	private volatile boolean isEnd = false;
	private RobotEvent cause = null;
	public static int MODE_CALM=0;
	public static int MODE_ACTIVE=1;
	private int mode=MODE_CALM;

	StateWandering(RobotEvent cause, ArrayList<RobotLog> log) {
		this.cause = cause;
		this.mode=MODE_CALM;
	}
	
	StateWandering(int mode,RobotEvent cause, ArrayList<RobotLog> log) {
		this.cause = cause;
		this.mode=mode;
	}

	@Override
	public void onStart(Context ctx) {
		if (_DEBUG) {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN, TAG);
		} else {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
		}

		//FaceDetector.getInstance().start();
		
		FaceDetector.getInstance(ctx).start();
		isEnd = false;
	}

	@Override
	public void doAction(Context ctx) {

			RobotSpeech.getInstance(ctx).speak("아이쿠 시끄러워", 1.0f, 1.0f);
//		if (cause != null && cause.getType() == RobotEvent.EVT_NOISE_DETECTION) {
//			try {
//				Thread.sleep(200);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			RobotSpeech.getInstance(ctx).speak("아이쿠 시끄러워", 1.0f, 1.0f);
//		}

		try {
			int cnt=0;
			
			RobotMotion.getInstance(ctx).setLogoLEDDimming(2);

			while (!isEnd) {
				int rand = (int) (Math.random() * 70l);
				int lastDirection=-1;
				if (rand == 1 && (mode==MODE_ACTIVE))
					RobotSpeech.getInstance(ctx).speak("어디있니?", 1.0f, 1.3f);
				
				if(++cnt==10) {
					rand = (int) (Math.random() * 5l);
					if(rand==0 && (mode==MODE_ACTIVE) ) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).goForward(1, 1);
					}
					else if(rand==1 && (mode==MODE_ACTIVE)) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).goBack(1, 1);
					}
					else if(rand==2 && lastDirection!=2) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).turnRight(1,20);
						lastDirection=2;
					}
					else if(rand==3 && lastDirection!=3) {
						RobotMotion.getInstance(ctx).stopWheel();
						RobotMotion.getInstance(ctx).turnLeft(1,20);
						lastDirection=3;
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
		//FaceDetector.getInstance().stop();
		// RobotMotion.getInstance(ctx).stopFreeMove();
		FaceDetector.getInstance(ctx).stop();
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).offAllLed();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {

		isEnd = true;
	}
}
