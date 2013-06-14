package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class StateLookAround implements IRobotState{

    private static final String TAG = "StateLookAround";
    private boolean _DEBUG = true;
    private volatile boolean isEnd = false;
    private RobotEvent cause=null;

    @Override
    public void onStart(Context ctx) {
	// TODO Auto-generated method stub
	if (_DEBUG) {

	    RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION, TAG);
	} else {
	    RobotFace.getInstance(ctx).change(RobotFace.MODE_ATTENTION);
	}

	isEnd = false;

		FaceDetector.getInstance().start();
    }

	StateLookAround(RobotEvent cause,ArrayList<RobotLog> log){
		
		this.cause=cause;
		
	}
	
    @Override
    public void doAction(Context ctx) {

	if(cause!=null && cause.getType()==RobotEvent.EVT_NOISE_DETECTION)
	{
		try{
		    Thread.sleep(200);
		}catch(Exception e){e.printStackTrace();}
		
		RobotSpeech.getInstance(ctx).speak("어?",1.0f,1.0f);
	}

	RobotMotion.getInstance(ctx).setLogoLEDDimming(2);

	try {

		int oldDirection=-1;
		int cnt = 0;
		
	    while (!isEnd) {
		
		if (++cnt == 10) 
		{
		    int direction = (int) (Math.random() * 2l)+ RobotMotion.HEAD_LEFT;
			    Log.d(TAG, "head direction:" + direction);
			    
		    RobotMotion.getInstance(ctx).headWithSpeed(direction, 0.1f);
		    
		    Thread.sleep(200);
		    
		    if(oldDirection!=direction){
		  //  RobotSpeech.getInstance(ctx).speak("오", 1.0f, 1.1f);
		    oldDirection=direction;
		    }
		    
			cnt = 0;
		}
		
	    }//:end of while
		
	 }
	catch (Exception e) {
	    e.printStackTrace();
	}
	
	}

	@Override
	public void cleanUp(Context ctx) {
		
		FaceDetector.getInstance().stop();
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx)
			.headWithSpeed(RobotMotion.HEAD_FRONT, 1.0f);
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
    }

    @Override
    public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		isEnd = true;
    }
    
}
