package com.kt.smartKibot;

import android.content.Context;
import android.util.Log;

public class StateLookAround implements IRobotState, FaceDetector.OnFaceDetectListener {

	
	private static final String TAG="StateLookAround";
	private boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	private boolean faceDetected = false;
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		if(_DEBUG)
		{
			
			RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION);
		}
		
		isEnd=false;
		
		FaceDetector fd = RobotActivity.getFaceDetector();
		if (fd != null){
		    fd.setStop(false);
		    fd.setOnFaceDetectListener(this);
		}
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
		
		int rand=(int) (Math.random()*5);
		
		
		switch(rand){
		
			case 0:RobotSpeech.getInstance(ctx).speak("누구야? ",1.0f,1.1f);
			break;
			
			case 1:RobotSpeech.getInstance(ctx).speak("누구 있어요?",1.0f,1.1f);
			break;
			
			case 2:RobotSpeech.getInstance(ctx).speak("누구세요? ",1.0f,1.1f);
			break;
			
			case 3:RobotSpeech.getInstance(ctx).speak("거기 누구신지?",0.8f,1.1f);
			break;
			
			case 4:RobotSpeech.getInstance(ctx).speak("어서오세요!",1.0f,0.9f);
			break;
		}
		
		try{
			
			while(!isEnd)
			{
				//RobotMotion.getInstance(ctx).led(0,100,3);
				
				int direction=(int)(Math.random()*2l )+RobotMotion.HEAD_LEFT;
				Log.d(TAG,"head direction:"+direction);
				RobotMotion.getInstance(ctx).headWithSpeed(direction,0.3f);
				Thread.sleep(500);
			}
			if (faceDetected){
			    RobotSpeech.getInstance(ctx).speak("안녕하세요? ",1.0f,1.1f);
			    
			    RobotMotion.getInstance(ctx).goFoward(1, 1);
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
		// TODO Auto-generated method stub

	    	//RobotMotion.getInstance(ctx).stopRMM();
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_FRONT,1.0f);
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd=true;
	}
	
	@Override
	public void onFaceDetect(Context ctx, FaceDetector faceDetector) {
	    Log.i(TAG, "Face Detected");
	    faceDetector.setStop(true);
	    isEnd = true;
	    faceDetected = true;
	}

}
