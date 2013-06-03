package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateGreeting implements IRobotState {


	private static final String TAG="StateGreeting";
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub

		RobotFace.getInstance(ctx).change(RobotFace.EMO_MODE_EXCITED);
		
		Toast.makeText(ctx.getApplicationContext(), TAG,Toast.LENGTH_LONG).show();
			
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub

		try{
			
			RobotMotion.getInstance(ctx).led(0,100,3);
			Thread.sleep(2000);
				
			
			RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_LEFT);
			Thread.sleep(200);
			
			RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_RIGHT);
			Thread.sleep(200);
			
			RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
			Thread.sleep(200);
			
			RobotMotion.getInstance(ctx).goFoward(1, 1);
			
			Thread.sleep(200);
			
			RobotSpeech.getInstance(ctx).speak("안녕 방가워");
			
			RobotMotion.getInstance(ctx).goBack(1, 1);
			
			Thread.sleep(200);
			
			}catch(Exception e){}
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub

		RobotMotion.getInstance(ctx).offAllLed();
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		
	}
	
	

}
