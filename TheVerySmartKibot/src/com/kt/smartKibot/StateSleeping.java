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
/*
		
		
		try{
			
			
			
			{
				
			int rand=(int) (Math.random()*10l);
			
			
			switch(rand){
				case 0:RobotSpeech.getInstance(ctx).speak("아이 졸려요",0.8f,0.8f);
				break;
				
				case 1:RobotSpeech.getInstance(ctx).speak("아웅! 이제 자야겠다",0.6f,0.9f);
				break;
				
				case 2:RobotSpeech.getInstance(ctx).speak("아이 졸려! 이제 자야지",0.8f,0.8f);
				break;
				
				case 3:RobotSpeech.getInstance(ctx).speak("음! 졸려! 자야겠다",0.6f,0.8f);
				break;
				
				case 4:RobotSpeech.getInstance(ctx).speak("난 이제 잘래요",0.8f,0.8f);
				break;
				case 0:RobotSpeech.getInstance(ctx).speak("졸려요",0.8f,0.8f);
				break;
				case 1:RobotSpeech.getInstance(ctx).speak("아웅!",0.6f,0.9f);
				break;
				case 4:RobotSpeech.getInstance(ctx).speak("잘래요",0.8f,0.8f);
				break;
			}
			
			}
			
	*/	
			
		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP,TAG);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP);
		}
		
		try{
			
			Thread.sleep(100);
			
			while(!isEnd){
				
			
			Thread.sleep(200);
			
			}
			
			
			}catch(Exception e){}
			
			
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub
		RobotMotion.getInstance(ctx).stopWheel();

	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		NoiseDetector.getInstance().stop();
		isEnd=true;
		
	}

}
