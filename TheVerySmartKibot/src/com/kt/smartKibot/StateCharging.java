package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateCharging implements IRobotState {

	private volatile boolean isEnd=false;
	
	static final String TAG="StateCharging";
	private boolean _DEBUG=true;
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=false;
		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_EATTING,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_EATTING);
		}
		
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
		
		while(!isEnd){
			try{
				RobotSpeech.getInstance(ctx).speak("냠냠");
				
				int rand=(int)(Math.random()*7)+2;
				Thread.sleep(rand*1000*10);
				RobotSpeech.getInstance(ctx).speak("냠냠");
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=true;
	}

}
