package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;

public class StateTouchResponse implements IRobotState {

	int touchWhere=-1;
	ArrayList<RobotLog> history=null;
	private boolean _DEBUG=true;
	private String TAG="StateTouchResponse";
	private volatile boolean isEnd=false;
	
	StateTouchResponse(int where,ArrayList<RobotLog> log){
		touchWhere=where;
		history=log;
		
	}
	@Override
	public void onStart(Context ctx) {
		
		RobotMotion.getInstance(ctx).stopAll();
		
		isEnd=false;
		
		switch(touchWhere){
			case TouchDetector.PARAM_LEFT_EAR_PATTED:
			case TouchDetector.PARAM_LEFT_FOOT_PRESSED:
				
			if(_DEBUG){
					RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_LEFT,TAG);
			}else{
					RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_LEFT);
			}
				break;
				
			case TouchDetector.PARAM_RIGHT_EAR_PATTED:
			case TouchDetector.PARAM_RIGHT_FOOT_PRESSED:
				
			if(_DEBUG){
					RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_RIGHT,TAG);
			}else{
					RobotFace.getInstance(ctx).change(RobotFace.MODE_WINK_RIGHT);
			}
				break;
				
		}
		
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);

	}

	@Override
	public void doAction(Context ctx) {
		
			
		RobotSpeech.getInstance(ctx).speak("하하",1.2f,1.1f);
		

		try{
			
			switch(touchWhere){
				case TouchDetector.PARAM_LEFT_EAR_PATTED:
				case TouchDetector.PARAM_LEFT_FOOT_PRESSED:
					
				RobotMotion.getInstance(ctx).headRoll(10f,1.0f);
				Thread.sleep(100);
				RobotMotion.getInstance(ctx).turnLeft(2);
				Thread.sleep(400);
				break;
				
				case TouchDetector.PARAM_RIGHT_EAR_PATTED:
				case TouchDetector.PARAM_RIGHT_FOOT_PRESSED:
				RobotMotion.getInstance(ctx).headRoll(-10f,1.0f);
				Thread.sleep(100);
				RobotMotion.getInstance(ctx).turnRight(2);
				Thread.sleep(400);
				break;
			}
		
			RobotMotion.getInstance(ctx).goForward(2, 5);
			Thread.sleep(400);
			
			RobotSpeech.getInstance(ctx).speak("간지러워",1.1f,1.1f);
			
			RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT,0.2f);
			
			Thread.sleep(400);
			RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT,0.2f);
			Thread.sleep(400);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	
	}

	@Override
	public void cleanUp(Context ctx) {

		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		//not use..
		//isEnd=true;

	}

}
