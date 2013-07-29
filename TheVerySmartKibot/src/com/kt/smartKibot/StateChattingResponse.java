package com.kt.smartKibot;
import android.content.Context;



public class StateChattingResponse implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateChattingResponse";
	private volatile boolean isEnd=false;
	private String ask=null;
	private String response=null;

	


	public StateChattingResponse(String ask) { 
		this.ask=ask;
	
	} 	 	  

	
	@Override
	public void onStart(Context ctx) {
		if(_DEBUG) {
				
				if(ask!=null)
				{
					RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN,TAG+" Q:"+ask);
					response=RobotChatting.getInstance(ctx).getResponse(ask);
				}
				else{
					RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION,TAG+" Q:null");
					response="어?";
				}
		}
		else{
				if(ask!=null)
				{
					RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
					response=RobotChatting.getInstance(ctx).getResponse(ask);
				}
				else{
					RobotFace.getInstance(ctx).change(RobotFace.MODE_SAD);
					response="어?";
				}
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
				
						RobotSpeech.getInstance(ctx).speak(response);
			
				}else if(cnt==20){
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
	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
		
	}
}
