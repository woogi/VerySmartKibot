package com.kt.smartKibot;

import android.content.Context;
import android.speech.SpeechRecognizer;

public class StateSleeping implements IRobotState {
	
	private static final String TAG="StateSleeping";
	private volatile boolean isEnd=false;
	private boolean _DEBUG=true;
	public static final int CAUSE_SLEEPY=1;
	public static final int CAUSE_ORDER=2;
	public static final int CAUSE_NOBODY_FOUND=3;
	private int cause=CAUSE_SLEEPY;
	
	public StateSleeping(){
		
		this.cause=CAUSE_SLEEPY;
		
	}
	
	public StateSleeping(int cause){
		
		this.cause=cause;
		
	}
	
	@Override
	public void onStart(Context ctx) {
		if(_DEBUG){
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP,TAG+":"+cause);
		}
		else{
			RobotFace.getInstance(ctx).change(RobotFace.MODE_SLEEP);
		}
		
		NoiseDetector.getInstance(ctx).start();
		
		BTMotionDetector.getInstance(ctx).setTargetDevName("Woogic");
		BTMotionDetector.getInstance(ctx).start();
		
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
		
		try{
			int cnt=0;
			
			while(!isEnd) {
				if(cnt==0) {
					RobotMotion.getInstance(ctx).goBack(1,1);
					if(cause==CAUSE_ORDER){
						RobotSpeech.getInstance(ctx).speak("알겠어요 잠이나 자야겠다",1.1f,1.0f);
					}else if(cause==CAUSE_NOBODY_FOUND){
						RobotSpeech.getInstance(ctx).speak("아무도 없네? 잠이나자자 ",1.1f,1.0f);
					}
					else if(cause==CAUSE_SLEEPY){
						RobotSpeech.getInstance(ctx).speak("아이 졸려요",0.6f,0.8f);
					}
				}
				else if(cnt==25) {
					int rand=(int) (Math.random()*2l);
					if (rand==0){
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_LEFT,0.1f);}
					else{
						RobotMotion.getInstance(ctx).headWithSpeed(RobotMotion.HEAD_RIGHT,0.1f);
					}
					RobotSpeech.getInstance(ctx).speak("음",0.5f,0.8f);
				}
				
				++cnt;
				Thread.sleep(100);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void cleanUp(Context ctx) {
		
		NoiseDetector.getInstance(ctx).stop();
		BTMotionDetector.getInstance(ctx).stop();
		RobotMotion.getInstance(ctx).stopWheel();
	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
	}

}
