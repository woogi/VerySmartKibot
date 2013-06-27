package com.kt.smartKibot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

public class NoiseDetector implements IRobotEvtDelegator ,NoiseCheckThread.OnNoiseListener{

	private static NoiseDetector _this;
	private IRobotEvtHandler handler=null;
	volatile private NoiseCheckThread noiseCheckThread=null;
	static final String TAG="NoiseDetector";
	
	// param1 definition for noise detection event
	public static final int PARAM_SMALL_NOISE=0;
	public static final int PARAM_BIG_NOISE=1;
	
	static final Lock lock= new ReentrantLock(); 
		
	static NoiseDetector getInstance(){
		
		if(_this==null) _this=new NoiseDetector();
		
		return _this;
		
	}
	
	@Override
	public void installHandler(IRobotEvtHandler handler) {
		
		this.handler=handler;

	
		
	}

	@Override
	public void  start(){
		lock.lock(); //not permitted using noise detector until stop previous one 
		noiseCheckThread=new NoiseCheckThread(null);
		noiseCheckThread.start();
		noiseCheckThread.setOnNoiseListener(this);
	}
	
	@Override
	public void stop(){
		
		if(noiseCheckThread!=null) noiseCheckThread.stopRunning();
		try {
			Thread.sleep(100);
		} catch(Exception e) {}
		
		noiseCheckThread=null;
		lock.unlock(); //now can use noise detector.
	}
	
	@Override
	public void uninstallHandler() {

		stop();
		handler=null;
		
	}
	
	public void onNoiseEvent(int vol, int dB) {
		
		Log.d(TAG,"vol: "+vol+", dB: "+dB);
		
		RobotEvent evt=new RobotEvent(RobotEvent.EVT_NOISE_DETECTION);
		
		if(vol<1000) {
			evt.setParam1(PARAM_SMALL_NOISE);
			
		}
		else {
			evt.setParam1(PARAM_BIG_NOISE);
		}
		
		handler.handle(null,evt);
	}
		
}
