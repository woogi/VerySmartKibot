package com.kt.smartKibot;

import android.content.Context;
import android.widget.Toast;

public class StateWandering implements IRobotState {
	
	private static final String TAG="StateWandering";
	private boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	
	@Override
	public void onStart(Context ctx) {
		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
		}
		
		isEnd=false;
		
		
	}	@Override
	public void doAction(Context ctx) {
		

		int rand=(int) (Math.random()*10l);
		
		
		switch(rand){
		
			case 0: RobotSpeech.getInstance(ctx).speak("앗  무슨 소리지?",1.0f,1.3f);
			break;
			
			case 1: RobotSpeech.getInstance(ctx).speak("누구야?",1.0f,1.3f);
			break;
		}
			
		try{
			RobotMotion.getInstance(ctx).startFreeMove();
			RobotMotion.getInstance(ctx).setLogoLEDDimming(20);
			
			while(!isEnd){
				
				rand=(int) (Math.random()*100l);
				
				switch(rand){
				
				case 2: RobotSpeech.getInstance(ctx).speak("어디있어?",1.0f,1.3f);
				break;
				case 3: RobotSpeech.getInstance(ctx).speak("어디있니?",1.0f,1.3f);
				break;
				case 4: RobotSpeech.getInstance(ctx).speak("나랑 놀자!",1.0f,1.3f);
				break;
				}
				
				Thread.sleep(200);}
		
		}
		catch(Exception e){}
	}

	@Override
	public void cleanUp(Context ctx) {
		RobotMotion.getInstance(ctx).stopFreeMove();
		RobotMotion.getInstance(ctx).offAllLed();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
	
		isEnd=true;
		//RobotMotion.getInstance(ctx).stopFreeMove();
	}

}
