package com.kt.smartKibot;

import java.util.ArrayList;

import com.kt.kibot.talkservice.IRemoteService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;


//speech recoginizer is working on the main activity only
//Chatting interface uses main activity's handler internally.

public class RobotChatting implements IRobotEvtDelegator {
	
	private static RobotChatting _this;
	private IRobotEvtHandler handler=null;
	private Handler robotActivityHandler=null;
	private Context ctx=null;
	private static final String TAG="RobotChatting";

	RobotEvent curEvent=null;
	IRemoteService mService = null;
	volatile protected boolean mIsBound = false;
	  /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = null;
    
    private void bindService() {
        // Establish a couple connections with the service, binding
        // by interface names.  This allows other applications to be
        // installed that replace the remote service by implementing
        // the same interface.
        if(ctx.bindService(new Intent("com.kt.kibot.talkservice.IRemoteService"),
                mConnection, Context.BIND_AUTO_CREATE)){
        	mIsBound = true;
        	Log.d(TAG, "bind succeed");
        }else{
        	mIsBound = false;
        	Log.e(TAG, "bind failed");
        }
        
    }
    
    private void unbindService(){
        if (mIsBound) {
            // Detach our existing connection.
            ctx.unbindService(mConnection);
            mIsBound = false;
        }
    }
    
	static RobotChatting getInstance(Context ctx){
		if(_this==null) _this=new RobotChatting(ctx);
		
		return _this;
	}
	
	
	private RobotChatting(Context ctx){
		this.ctx=ctx;
		
		mConnection = new ServiceConnection() {
	        public void onServiceConnected(ComponentName className,
	                IBinder service) {
	            // This is called when the connection with the service has been
	            // established, giving us the service object we can use to
	            // interact with the service.  We are communicating with our
	            // service through an IDL interface, so get a client-side
	            // representation of that from the raw service object.
	            mService = IRemoteService.Stub.asInterface(service);

	        }

	        public void onServiceDisconnected(ComponentName className) {
	            // This is called when the connection with the service has been
	            // unexpectedly disconnected -- that is, its process crashed.
	            mService = null;
	        }
	    };
	}
	
	
	
	public void startListen(){
		robotActivityHandler.sendMessage(
			robotActivityHandler.obtainMessage(RobotActivity.ACTION_START_LISTEN,-1,-1)
		);
		
	}
	
	public void stopListen(){
		
//		robotActivityHandler.sendMessage(
//			robotActivityHandler.obtainMessage(RobotActivity.END_ACTION_LISTEN,-1,-1)
//		);
		
	}
	
	public void startCheckNoise(int arg1,int arg2)
	{
		robotActivityHandler.sendMessage(
			robotActivityHandler.obtainMessage(RobotActivity.ACTION_CHECK_NOISE,arg1,arg2)
		);
	}
	
	public void onResultListen(int isOK,ArrayList<String> results){
		
		
		String ask=null;
		
		if(isOK==1) //sucess
		{
			
			ask=results.get(0);
			
		}
		
		Log.d(TAG,"isOK:"+isOK+"ask:"+ask);
		handler.handle(ctx,new RobotEvent(RobotEvent.EVT_ROBOT_CHATTING_ASK,isOK,0,ask));
		
		
	}
	
	
	public String getResponse(String strQuestion){
		String strAnswer=null;
		
		if (mIsBound) {
			try {
				if(mService != null){
					if(mService.isReady()){
						// 적어도 2초 이내에 답변이 온다. 
						strAnswer = mService.getResultTalk(strQuestion);
						
						//if(strAnswer.length()==0) return false;
						if(strAnswer.length()==0) return null;
						/* todo*/
						//mTTSControl.sendTTSFlush(strAnswer);
						//showToast(strAnswer);
						/*
						m_ttsControl.sendTTSOnCompleteProc(
								strAnswer,
								m_nCurrentRecognition);
								*/
						
						Log.d(TAG, "chatting result received : " + strAnswer);
						
						return strAnswer;
					}else{
						Log.e(TAG, "chatting service ready not yet");
						strAnswer="대화 서비스가 아직 준비 되지 않았어요 키봇이 대화 하기 힘들어요.";
						return strAnswer;
					}
				}else{
					Log.e(TAG, "chatting service not binded");
					strAnswer="대화 서비스가 연결되지 않았어요 키봇이 대화 하기 힘들어요.";
					return strAnswer;
					
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		strAnswer="대화 서비스에 문제가 생겼어요 키봇이 대화 하기 힘들어요.";
		return strAnswer;
		
		//return null;
	}
	
	public void onResultCheckNoise(int arg1,int arg2){
		NoiseDetector.getInstance(ctx).sendEvent(ctx, arg1, arg2);
	}
	
	public void setMainActivityHandler(Handler handler){
		this.robotActivityHandler=handler;
	}
	
	@Override
	public void installHandler(IRobotEvtHandler handler) {
		this.handler=handler;
		
	}

	@Override
	public void uninstallHandler() {
		this.handler=null;

	}

	@Override
	public void start() {
		
		bindService();

	}

	@Override
	public void stop() {
		unbindService();

	}

}
