package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class NoiseDetector implements IRobotEvtDelegator ,NoiseCheckThread.OnNoiseListener{

	private static NoiseDetector _this;
	private IRobotEvtHandler handler=null;
	private Context ctx=null;
	private volatile NoiseCheckThread noiseCheckThread=null;
	SpeechRecognizer speechRecognizer=null;
	RobotEvent curEvent=null;
	volatile int cntPauseResume=1;
	
	
	static final String TAG="NoiseDetector";
	//private Handler robotActivityHandler=null;
	
	// param1 definition of noise detection event
	public static final int PARAM_SMALL_NOISE=0;
	public static final int PARAM_BIG_NOISE=1;
	
	//param2 definition of noise detection event
	public static final int PARAM_KIND_HUMAN_VOICE=0;
	public static final int PARAM_KIND_NOT_FROM_HUMAN=1;
	
	static final Lock lock= new ReentrantLock(); 
		
    
	static NoiseDetector getInstance(Context ctx){
		
		if(_this==null) _this=new NoiseDetector();
		
		_this.ctx=ctx;
		
		return _this;
		
	}
	
//	public void setMainActivityHandler(Handler handler){
//		robotActivityHandler=handler;
//	}
	
	public void sendEvent(Context ctx, int param1,int param2){
		
		
		if(handler!=null){
			RobotEvent evt=new RobotEvent(RobotEvent.EVT_NOISE_DETECTION);
			evt.setParam1(param1);
			evt.setParam2(param2);
			handler.handle(ctx, evt);
			Log.d(TAG,"EVT_NOISE_DETECTION :"+param1+" "+param2);
		}
		
		
		start();	
	}
	
	
	@Override
	public void installHandler(IRobotEvtHandler handler) {
		
		this.handler=handler;

	
		
	}

//	synchronized public void pause(){
//		
//		if(noiseCheckThread!=null)
//		{
//			noiseCheckThread.pause();
//			//			--cntPauseResume;
//			Log.d(TAG,"try to pause count:"+cntPauseResume);
//		}
//		else
//		{
//			Log.d(TAG,"try to pause but noiseCheckThread is null");
//		}
//	}
//	
//	synchronized public void resume(){
//		if(noiseCheckThread!=null){
//			noiseCheckThread.resumeIt();
//			
//			++cntPauseResume;
//			Log.d(TAG,"try to resume count:"+cntPauseResume);
//		}
//		else{
//			
//			Log.d(TAG,"try to resume but noiseCheckThread is null");
//		}
//	}
	
	@Override
	public void  start(){
		//lock.lock(); //not permitted using noise detector until stop previous one 
		//noiseCheckThread=null;
		noiseCheckThread=new NoiseCheckThread(null);
		noiseCheckThread.start();
		noiseCheckThread.setOnNoiseListener(this);
	}
	
	@Override
	public void stop(){
		
		Log.d(TAG,"stop noiseCheckThread ...");
		if(noiseCheckThread!=null) noiseCheckThread.stopRunning();
		
		try {
			//noiseCheckThread.interrupt();
			Thread.sleep(200);
		} catch(Exception e) {}
		
		noiseCheckThread=null;
		
		Log.d(TAG,"end of stopping noiseCheckThread ...");
	//	lock.unlock(); //now can use noise detector.
			
		
		
	}
	
	@Override
	public void uninstallHandler() {

		//stop();
		handler=null;
		
	}
	
	public void onNoiseEvent(int vol, int dB) {
		
		Log.d(TAG,"vol: "+vol+", dB: "+dB);
			//	if(robotActivityHandler==null) return;
		
		//stop();
		
		if(vol<2000) {
			
			sendEvent(ctx, PARAM_SMALL_NOISE, -1);
			//RobotChatting.getInstance(ctx).startCheckNoise(PARAM_SMALL_NOISE, -1);
		}
		else {
			sendEvent(ctx, PARAM_BIG_NOISE, -1);
			//RobotChatting.getInstance(ctx).startCheckNoise(PARAM_BIG_NOISE, -1);
		
		}
		
		
		//handler.handle(null,evt);
		
	}
		
}
