package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateSleeping implements IRobotState {
	
	private static final String TAG="StateSleeping";
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub

		RobotFace.getInstance(ctx).change(RobotFace.EMO_MODE_SLEEP);
		
		NoiseDetector.getInstance().start();
		Toast.makeText(ctx, TAG,Toast.LENGTH_LONG).show();
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub

		try{
			
			RobotSpeech.getInstance(ctx).speak("음");
			
			
			Thread.sleep(2000);
			
			
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
		NoiseDetector.getInstance().stop();
		
	}

}
