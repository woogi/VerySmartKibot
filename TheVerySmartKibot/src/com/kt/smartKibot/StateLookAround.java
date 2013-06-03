package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateLookAround implements IRobotState {

	
	private static final String TAG="StateLookAround";
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		RobotFace.getInstance(ctx).change(RobotFace.EMO_MODE_ATTENTION);
		Toast.makeText(ctx, TAG,Toast.LENGTH_LONG).show();
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub

		try{
		RobotMotion.getInstance(ctx).led(0,100,3);
		Thread.sleep(2000);
		RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT,0.1f);
		Thread.sleep(3000);
		
		RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT,0.1f);
		Thread.sleep(6000);
		
		RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_FRONT,0.1f);
		Thread.sleep(3000);
		
		}
		catch(Exception e){}
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		
	}

}
