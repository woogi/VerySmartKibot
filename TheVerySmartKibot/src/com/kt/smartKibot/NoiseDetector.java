package com.kt.smartKibot;

import android.util.Log;

public class NoiseDetector implements IRobotEvtDelegator ,NoiseCheckThread.OnNoiseListener{

	private static NoiseDetector _this;
	private IRobotEvtHandler handler=null;
	private NoiseCheckThread noiseCheckThread=null;
	static final String TAG="NoiseDetector";
	
	// param1 definition for noise detection event
	public static final int PARAM_SMALL_NOISE=0;
	public static final int PARAM_BIG_NOISE=1;
	
	static NoiseDetector getInstance(){
		
		if(_this==null) _this=new NoiseDetector();
		
		return _this;
		
	}
	
	@Override
	public void installHandler(IRobotEvtHandler handler) {
		// TODO Auto-generated method stub
		
		this.handler=handler;

	
		
	}

	@Override
	public void start(){
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
	}
	
	@Override
	public void uninstallHandler() {
		// TODO Auto-generated method stub

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
