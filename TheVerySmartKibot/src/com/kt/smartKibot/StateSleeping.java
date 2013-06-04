package com.kt.smartKibot;

import java.util.Random;

import android.content.Context;
import android.widget.Toast;

public class StateSleeping implements IRobotState {
	
	private static final String TAG="StateSleeping";
	private volatile boolean isEnd=false;
	private boolean _DEBUG=true;
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub

		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP);
		}
		
		NoiseDetector.getInstance().start();
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub

		try{
			
			while(!isEnd){
				
			int rand=(int)(Math.random()*4);
				if(rand==0){
		
					RobotSpeech.getInstance(ctx).speak("쿨 쿨");
				}
			
				if(rand==1){
		
					RobotSpeech.getInstance(ctx).speak("음");
				}
			
			Thread.sleep(2000);
			
			}
			
			
			}catch(Exception e){}
			
			
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		NoiseDetector.getInstance().stop();
		isEnd=true;
		
	}

}
