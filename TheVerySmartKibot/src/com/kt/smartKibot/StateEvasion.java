package com.kt.smartKibot;

import android.content.Context;

public class StateEvasion implements IRobotState {
	private boolean _DEBUG=true;
	private static final String TAG="StateEvasion";
	private volatile boolean isEnd=false;
	
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
	
		RobotSpeech.getInstance(ctx).speak(" 싫어 싫어 ",0.9f,1.0f);
		
		RobotMotion.getInstance(ctx).goBack(1, 2);
		
		while(!isEnd){
			
			try{
				Thread.sleep(100);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}

	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub
		RobotMotion.getInstance(ctx).stopWheel();
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=true;

	}

}
