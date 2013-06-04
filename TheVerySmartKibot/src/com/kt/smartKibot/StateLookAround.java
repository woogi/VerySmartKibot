package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateLookAround implements IRobotState {

	
	private static final String TAG="StateLookAround";
	private boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		if(_DEBUG)
		{
			
			RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION);
		}
		
		isEnd=false;
		
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
/*
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
		*/
		
		try{
		
			RobotMotion.getInstance(ctx).playRMM("lookAround.rmm");
			while(!isEnd)
			{
				Thread.sleep(100);
			}
			
		}
		catch(Exception e){e.printStackTrace();}
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub

		RobotMotion.getInstance(ctx).stopRMM();
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=true;
	}

}
