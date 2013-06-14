package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;

public class StateWandering implements IRobotState {
	
	private static final String TAG="StateWandering";
	private boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	private RobotEvent cause=null;
	
	StateWandering(RobotEvent cause,ArrayList<RobotLog> log){
		this.cause=cause;
	}
	
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
	
		if(cause!=null && cause.getType()==RobotEvent.EVT_NOISE_DETECTION)
		{
			try{
		    Thread.sleep(200);
			}catch(Exception e){e.printStackTrace();}
			RobotSpeech.getInstance(ctx).speak("무슨.소리지?",1.0f,1.0f);
		}
		
			
		try{
			RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
			
		    
			while(!isEnd){
				
				int rand=(int)(Math.random()*10l);
				if(rand==1)
					RobotSpeech.getInstance(ctx).speak("고!",1.0f,1.3f);
					
				rand=(int)(Math.random()*6l);
			
				switch(rand){
				//case 0:RobotMotion.getInstance(ctx).goForward(1, 5);
				//break;
				case 1:RobotMotion.getInstance(ctx).turnLeft(2);
				break;
				case 2:RobotMotion.getInstance(ctx).turnRight(2);
				break;
				//case 3:RobotMotion.getInstance(ctx).goBack(1, 5);
				//break;
				case 4:RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT,0.3f);
				break;
				case 5:RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT,0.3f);
				break;
				}
				
				Thread.sleep(200);
				
			}//:end of while
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void cleanUp(Context ctx) {
		//RobotMotion.getInstance(ctx).stopFreeMove();
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).offAllLed();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
	
		isEnd=true;
	}

}
