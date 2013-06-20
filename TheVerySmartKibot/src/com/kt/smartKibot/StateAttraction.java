package com.kt.smartKibot;

import android.content.Context;

public class StateAttraction implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateAttraction";
	private volatile boolean isEnd=false;
	@Override
	public void onStart(Context ctx) {
		isEnd=false;
		if(_DEBUG) {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN,TAG);
		}
		else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
		}

	}

	@Override
	public void doAction(Context ctx) {

		RobotMotion.getInstance(ctx).stopAll();
		try {
			int cnt=0;

				while(!isEnd) {
					if(cnt==0) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
						RobotMotion.getInstance(ctx).headRoll(10f, 1.0f);
					}
					
					if(cnt==10) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
						RobotMotion.getInstance(ctx).headRoll(-10f, 1.0f);
					}
					
					if(cnt==20) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
						RobotMotion.getInstance(ctx).goForward(1, 1);
					}
					
					if(cnt==30) {
						RobotSpeech.getInstance(ctx).speak("하이", 1.1f, 1.0f);
						RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
					}
						
					if(cnt==40){ 
						RobotMotion.getInstance(ctx).setLogoLEDDimming(10);
						RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
					}
					
					++cnt;	
					Thread.sleep(100);
					}
		}catch(Exception e){e.printStackTrace();}

	}

	@Override
	public void cleanUp(Context ctx) {
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);

	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;

	}

}
