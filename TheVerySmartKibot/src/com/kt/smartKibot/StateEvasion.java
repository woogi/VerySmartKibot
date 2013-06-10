package com.kt.smartKibot;

import android.content.Context;

public class StateEvasion implements IRobotState {
	private boolean _DEBUG=true;
	private static final String TAG="StateEvasion";
	private volatile boolean isEnd=false;
	int cause=-1;
	
	public static final int CAUSE_BIG_NOISE=0;
	public static final int CAUSE_TOUCH_TOO_MUCH=1;
	
	
	StateEvasion(int cause){
		
		this.cause=cause;
	}
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		if(_DEBUG)
		{
			
				RobotFace.getInstance(ctx).change(RobotFace.MODE_SAD,TAG);
		}
		else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_SAD);
				
		}
	
		isEnd=false;
			
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
	
		if(cause==CAUSE_TOUCH_TOO_MUCH){
			RobotSpeech.getInstance(ctx).speak("계속 만지면 싫어 싫어",0.9f,1.0f);
		}
		
		if(cause==CAUSE_BIG_NOISE){
			RobotSpeech.getInstance(ctx).speak("아웅 시끄러워",0.9f,1.0f);
		}
		
		
		RobotMotion.getInstance(ctx).goBack(1, 2);
		
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
		
		try{
			Thread.sleep(500);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		/*
		while(!isEnd){
			
			try{
				Thread.sleep(100);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		*/

	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub
		RobotMotion.getInstance(ctx).stopWheel();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=true;

	}

}
