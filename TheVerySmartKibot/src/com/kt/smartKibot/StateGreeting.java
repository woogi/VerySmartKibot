package com.kt.smartKibot;

import android.content.Context;

public class StateGreeting implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateGreeting";
	private volatile boolean isEnd=false;
	
	
	@Override
	public void onStart(Context ctx) {
		if(_DEBUG) {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED,TAG);
		}
		else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED);
		}
	
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
		try{
			int cnt=0;
			
			while(!isEnd) {
				if(cnt==0) {
					RobotMotion.getInstance(ctx).goForward(1,5);
				}
				else if(cnt==10) {
					RobotSpeech.getInstance(ctx).speak("소장님! 방가워요");
					RobotMotion.getInstance(ctx).goBack(1,5);
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
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
		
	}
	
	

}
