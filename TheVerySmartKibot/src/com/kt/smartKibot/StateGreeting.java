package com.kt.smartKibot;

import android.content.Context;

public class StateGreeting implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateGreeting";
	private volatile boolean isEnd=false;
	public static final int CAUSE_HELLO =1;
	public static final int CAUSE_NOTICE=2;
	
	int cause=CAUSE_HELLO;
	
	public StateGreeting()
	{
		 this.cause=CAUSE_HELLO;	
	}
	
	public StateGreeting(int cause)
	{
		 this.cause=cause;	
	}
	
	@Override
	public void onStart(Context ctx) {
		if(_DEBUG) {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED,TAG);
		}
		else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_EXCITED);
		}
	
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
		try{
			int cnt=0;
			
			while(!isEnd) {
				if(cnt==0) {
					RobotMotion.getInstance(ctx).goForward(1,1);
				}
				else if(cnt==10) {
					if(cause==CAUSE_HELLO){
						RobotSpeech.getInstance(ctx).speak("소장님,방가워요");
					}
					else{
						int random=(int)(Math.random()*3l);
						switch(random)
						{
							case 0:RobotSpeech.getInstance(ctx).speak("소장님 거기계셨네");
							break;
							case 1:RobotSpeech.getInstance(ctx).speak("찾았다!");
							break;
							case 2:RobotSpeech.getInstance(ctx).speak("하이!");
							break;
						}
					}
					
					RobotMotion.getInstance(ctx).goBack(1,1);
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
	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
		
	}
	
	

}
