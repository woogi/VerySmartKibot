package com.kt.smartKibot;

import android.content.Context;
import android.text.format.Time;

public class StateListening implements IRobotState{
	
	private boolean _DEBUG=true;
	private static final String TAG="StateListening";
	private volatile boolean isEnd=false;
	static long lastTime=-1;

	


	public StateListening() { 
		
	
	} 	 	  

	
	@Override
	public void onStart(Context ctx) {
		if(_DEBUG) {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION,TAG);
		}
		else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION);
		}
	
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
		
		User loginUser=LogIn.getInstance().whosLogIn();
		boolean actJustBefore=true;
		
		Time date=new Time();
		date.setToNow();
		long curTime=date.toMillis(true);
	
		if(lastTime==-1 || lastTime+1000*60 < curTime/*1min*/)
		{
			
			actJustBefore=false;
			
		}
		
		lastTime=curTime;
		
		
		try{
			int cnt=0;
			
			while(!isEnd) {
				if(cnt==0) {
					RobotMotion.getInstance(ctx).goForward(1,1);
				}
				else if(cnt==15) {
				
						if(loginUser!=null && loginUser.name!=null)
						{
							
							if(!actJustBefore) //login 상태 & 최근에 대화를 시도한적 없는경우 
							{
								RobotSpeech.getInstance(ctx).speak(""+loginUser.name+"님" +" 할말 있으신지?");
							}else //login 상태 & 최근 대화를 한적 있는 경우
							{
								RobotSpeech.getInstance(ctx).speak(""+loginUser.name+"님" +"또 할말 있으세요?");
							}
						}
						else{
							
							User lastUser=LogIn.getInstance().getLastCheckInUser();
							
							
							
							if(lastUser!=null && !actJustBefore)
							{
								long lastMin= (curTime-lastUser.lastCheckIn.toMillis(true) ) / (1000*60);
								RobotSpeech.getInstance(ctx).speak("누구세요? "+ lastUser.name+"님 어디 갔어요?");
								//+lastMin+"분 전에는 있었는데");
							}
							else{
								RobotSpeech.getInstance(ctx).speak("할말 있으신지?");
							}
						}
						
			
				}else if(cnt==20){
						RobotChatting.getInstance(ctx).startListen();
				//	RobotMotion.getInstance(ctx).led(0,1,3);
					RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
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
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
		//RobotMotion.getInstance(ctx).offAllLed();
		RobotChatting.getInstance(ctx).stopListen();
	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
		
	}
	

}
