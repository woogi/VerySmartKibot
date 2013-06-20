package com.kt.smartKibot;

import android.content.Context;

public class StateSleeping implements IRobotState {
	
	private static final String TAG="StateSleeping";
	private volatile boolean isEnd=false;
	private boolean _DEBUG=true;
	
	@Override
	public void onStart(Context ctx) {
		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP);
		}
		
		NoiseDetector.getInstance().start();
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
		
		try{
			int cnt=0;
			
			while(!isEnd) {
				if(cnt==0) {
					RobotMotion.getInstance(ctx).goBack(1,1);
				}
				else if(cnt==10) {
					int rand=(int) (Math.random()*2l);
					if (rand==0){
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT,0.1f);}
					else{
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT,0.1f);
					}
					RobotSpeech.getInstance(ctx).speak("음",0.5f,0.8f);
				}
				
				++cnt;
				Thread.sleep(100);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void cleanUp(Context ctx) {
		NoiseDetector.getInstance().stop();
		RobotMotion.getInstance(ctx).stopWheel();
	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
	}

}
