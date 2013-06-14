package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateGreeting implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateGreeting";
	private volatile boolean isEnd=false;
	
	
	@Override
	public void onStart(Context ctx) {
		
		if(_DEBUG)
		{
			
				RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED,TAG);
		}
		else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED);
				
		}
	
		isEnd=false;
			
	}

	@Override
	public void doAction(Context ctx) {
		try{
			
			RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
			Thread.sleep(200);
			
			
			RobotMotion.getInstance(ctx).goForward(1, 5);
			
			Thread.sleep(200);
			
			RobotSpeech.getInstance(ctx).speak("안녕 방가워");
			
			RobotMotion.getInstance(ctx).goBack(1, 5);
			
			while(!isEnd)
			{
				
				Thread.sleep(200);
			}
			
			}catch(Exception e){}
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
