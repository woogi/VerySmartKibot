package com.kt.smartKibot;

import android.content.Context;

public class StateSleeping implements IRobotState {
	
	private static final String TAG="StateSleeping";
	private volatile boolean isEnd=false;
	private boolean _DEBUG=true;
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
	/*	
		try{
			
			int rand=(int) (Math.random()*2l);
		   if (rand==0){
			   RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_LEFT);}
		   else{
			   RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_RIGHT);
		   }
			
		Thread.sleep(400);
			
		//RobotMotion.getInstance(ctx).goBack(1, 5);
		
		Thread.sleep(400);
		}catch(Exception e){
			e.printStackTrace();
		}

		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP);
		}
		
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
			
		NoiseDetector.getInstance().start();
		*/
		
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);

		NoiseDetector.getInstance().start();
		
		try{
			
		RobotMotion.getInstance(ctx).stopAll();
		Thread.sleep(400);
		
		
		RobotMotion.getInstance(ctx).turnLeft(2, 1);
		Thread.sleep(600);
		
		RobotMotion.getInstance(ctx).goBack(1, 3);
		
		Thread.sleep(400);
		
			int rand=(int) (Math.random()*2l);
		   if (rand==0){
			   RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT,0.1f);}
		   else{
			   RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT,0.1f);
		   }
		   
		RobotSpeech.getInstance(ctx).speak("음",0.5f,0.8f);
		Thread.sleep(400);
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub

		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP);
		}
		
		try{
			
			Thread.sleep(100);
			int cnt=0;
			
			while(!isEnd){
			
				Thread.sleep(200);
			
			}
		
		}catch(Exception e){e.printStackTrace();}
			
			
	}

	@Override
	public void cleanUp(Context ctx) {
		NoiseDetector.getInstance().stop();
		RobotMotion.getInstance(ctx).stopWheel();

	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
		
	}

}
