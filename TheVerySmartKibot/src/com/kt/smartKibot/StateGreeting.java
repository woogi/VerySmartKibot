package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateGreeting implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateGreeting";
	private volatile boolean isEnd=false;
	
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub

		try{
			int cnt=1;
			
			//RobotMotion.getInstance(ctx).led(0,100,3);
			//Thread.sleep(2000);
				
			//RobotSpeech.getInstance(ctx).speak("영차 영차");
			
			RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
			Thread.sleep(200);
			
			RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_LEFT);
			Thread.sleep(400);
			
			RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_RIGHT);
			Thread.sleep(400);
			
			RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
			Thread.sleep(200);
			
			RobotMotion.getInstance(ctx).goForward(1, 5);
			
			Thread.sleep(200);
			
			RobotSpeech.getInstance(ctx).speak("안녕 방가워");
			
			RobotMotion.getInstance(ctx).goBack(1, 5);
			
		//	Thread.sleep(200);
			
			
			//test
			//RobotMotion.getInstance(ctx).playRMM("greeting.rmm");
			
			
			
			//RobotSpeech.getInstance(ctx).speak("준비 운동 시작");
			//RobotSpeech.getInstance(ctx).speak("영차 영차");
			
			
			while(!isEnd)
			{
				//cnt++;
				
				//RobotMotion.getInstance(ctx).setLogoLEDDimming(cnt);
				//if(cnt==63){cnt=1;}
				
				Thread.sleep(200);
			}
			
			}catch(Exception e){}
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub
		//RobotMotion.getInstance(ctx).stopRMM();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=true;
		
	}
	
	

}
