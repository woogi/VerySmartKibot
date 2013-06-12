package com.kt.smartKibot;

import android.content.Context;
import android.util.Log;

public class StateLookAround implements IRobotState{

	
	private static final String TAG="StateLookAround";
	private boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	private boolean faceDetected = false;
	
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		if(_DEBUG)
		{
			
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SERIOUS,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SERIOUS );
		}
		
		isEnd=false;
		
	FaceDetector.getInstance().start();
	
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
		
		int rand=(int) (Math.random()*5);
		int cnt=0;
		
		
		switch(rand){
		
		//	case 0:RobotSpeech.getInstance(ctx).speak("누구야? ",1.0f,1.1f);
		//	break;
			
			case 1:RobotSpeech.getInstance(ctx).speak("누구 있어요?",1.0f,1.1f);
			break;
			
			case 2:RobotSpeech.getInstance(ctx).speak("어디 있어? ",1.0f,1.1f);
			break;
			
		//	case 3:RobotSpeech.getInstance(ctx).speak("거기 누구신지?",0.8f,1.1f);
		//	break;
			
		//	case 4:RobotSpeech.getInstance(ctx).speak("어서오세요!",1.0f,0.9f);
		//	break;
		}
		
		
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
		
		try{
			
			while(!isEnd)
			{
				//RobotMotion.getInstance(ctx).led(0,100,3);
				++cnt;
				if(cnt==10){
				int direction=(int)(Math.random()*2l )+RobotMotion.HEAD_LEFT;
				Log.d(TAG,"head direction:"+direction);
				//RobotMotion.getInstance(ctx).headWithSpeed(direction,0.3f);
				RobotMotion.getInstance(ctx).headWithSpeed(direction,0.1f);
				Thread.sleep(500);
				
			if (FaceDetector.hasDetectedAFace()) {
			    RobotSpeech.getInstance(ctx).speak("안녕!",1.0f,1.1f);
			    
			    RobotMotion.getInstance(ctx).goForward(1, 5);
			    
			    isEnd=true;

				}
			
			if(cnt==10) cnt=0;
			
			Thread.sleep(200);
				
			}

			}
		
		}
		catch(Exception e){e.printStackTrace();}
		
		
		/*
		
		try{
		
			RobotMotion.getInstance(ctx).playRMM("lookAround.rmm");
			while(!isEnd)
			{
				Thread.sleep(100);
			}
			
		}
		catch(Exception e){e.printStackTrace();}
		*/
	}

	@Override
	public void cleanUp(Context ctx) {
		
		FaceDetector.getInstance().stop();
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_FRONT,1.0f);
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=true;
	}
	

}
