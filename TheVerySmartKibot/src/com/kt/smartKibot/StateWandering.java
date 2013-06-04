package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateWandering implements IRobotState {
	
	private static final String TAG="StateWandering";
	private boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
		}
		
		isEnd=false;
		
		
	}	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub

		try{
			RobotMotion.getInstance(ctx).startFreeMove();
			
			while(!isEnd){Thread.sleep(100);}
		
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
	
		isEnd=true;
		//RobotMotion.getInstance(ctx).stopFreeMove();
	}

}
