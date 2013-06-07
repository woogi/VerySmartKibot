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
			
			{
				
			int rand=(int) (Math.random()*5);
			
			
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
			}
			
			}
			
		
			
			
			Thread.sleep(100);
			
			while(!isEnd){
				
			int rand=(int)(Math.random()*20);
				if(rand==0){
		
					RobotSpeech.getInstance(ctx).speak("쿨 쿨",0.5f,0.5f);
				}
			
				if(rand==1){
		
					RobotSpeech.getInstance(ctx).speak("음",0.5f,0.5f);
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
