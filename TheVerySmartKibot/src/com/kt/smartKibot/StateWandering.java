package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateWandering implements IRobotState {
	
	private static final String TAG="StateWandering";
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		RobotFace.getInstance(ctx).change(RobotFace.EMO_MODE_FUN);
		Toast.makeText(ctx, TAG,Toast.LENGTH_LONG).show();
		
		
	}	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub

		try{
			
			Thread.sleep(1000);
		
			RobotMotion.getInstance(ctx).startFreeMove();
		
			Thread.sleep(1000*10);
		
		}
		catch(Exception e){}
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub
		RobotMotion.getInstance(ctx).stopFreeMove();
		RobotMotion.getInstance(ctx).offAllLed();
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		
	}

}
