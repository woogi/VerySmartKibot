package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;

public class StateTouchResponse implements IRobotState {

	int touchWhere=-1;
	ArrayList<RobotLog> history=null;
	private boolean _DEBUG=true;
	private String TAG="StateTouchResponse";
	
	StateTouchResponse(int where,ArrayList<RobotLog> log){
		touchWhere=where;
		history=log;
		
	}
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		
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

	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
		
		
		int rand=(int) (Math.random()*3l);
		
		
		switch(rand){
		
			case 0:RobotSpeech.getInstance(ctx).speak("아이 간지러워",1.1f,1.1f);
			break;
			
			case 1:RobotSpeech.getInstance(ctx).speak("하하하,",1.2f,1.0f);
			break;
			
			case 2:RobotSpeech.getInstance(ctx).speak("아이 좋아",1.0f,1.0f);
			break;
		}

		try{
			
			RobotMotion.getInstance(ctx).headRoll(10f,1.0f);
			Thread.sleep(1000);
			RobotMotion.getInstance(ctx).headRoll(-10f,1.0f);
					
		//	RobotMotion.getInstance(ctx).goBack(1, 1);
			Thread.sleep(1000);
		//	RobotMotion.getInstance(ctx).goFoward(1, 1);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	
	}

	@Override
	public void cleanUp(Context ctx) {

		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub

	}

}
