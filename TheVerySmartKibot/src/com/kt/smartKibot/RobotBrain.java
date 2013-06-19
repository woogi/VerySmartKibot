package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;



public class RobotBrain implements IRobotEvtHandler{
	
	private static final String TAG="RobotBrain";

	private Context ctx;
	private ArrayList<RobotBehavior> history_behavior;
	private ArrayList<RobotEvent> history_evt;
	private ArrayList<RobotLog> history_log;
	
	public static final int MAX_HISTORY_BEHAVIOR=5;
	public static final int MAX_HISTORY_EVT=10;
	public static final int MAX_HISTORY_LOG=50;
	

	private BatteryChecker batteryChecker=null;
	
	private RobotBehavior dayTimeBehavior;
	
	private void changeBehavior(RobotBehavior behavior){
		RobotBehavior oldBehavior=getCurrentBehavior();
		RobotBehavior newBehavior=behavior;
		
		if(null!=oldBehavior)
			oldBehavior.onStop(this.ctx);
		
		//delete the oldest one.
		if(history_behavior.size()== MAX_HISTORY_BEHAVIOR) history_behavior.remove(0);
		
		history_behavior.add(behavior);
		newBehavior.onStart(this.ctx);
		
	}
	
	private RobotBehavior getCurrentBehavior(){
		if(0==history_behavior.size()){
			return null;
		}
		else{
			return history_behavior.get(history_behavior.size()-1);
		}
	}
	
	public void finalize(){
		
		Log.d(TAG,"brain finalize");
		
		batteryChecker.stop();
		batteryChecker=null;
		
		RobotMotion.getInstance(ctx).stopAll();
		
		getCurrentBehavior().onStop(ctx);
		
	}
	
	public RobotBrain(Context ctx) {
		this.ctx = ctx;
		
		this.history_behavior=new ArrayList<RobotBehavior>(MAX_HISTORY_BEHAVIOR);
		this.history_evt=new ArrayList<RobotEvent>(MAX_HISTORY_EVT);
		this.history_log=new ArrayList<RobotLog>(MAX_HISTORY_LOG);
		
		dayTimeBehavior=new DayTimeBehavior(history_log);
		
		//timer handler 등록
		RobotTimer.getInstance().installHandler(this);
		
		//noise detector handler 등록
		NoiseDetector.getInstance().installHandler(this);
		
		//face detector handler
    	FaceDetector.getInstance().installHandler(this);

		//body touch event handler 등록 
		TouchDetector.getInstance().installHandler(this);
		TouchDetector.getInstance().start();
		
		//battery checker handler 등
		batteryChecker=new BatteryChecker(ctx);
		batteryChecker.installHandler(this);
		batteryChecker.start();
						changeBehavior(dayTimeBehavior);
	}

	@Override
	public void handle(Context ctx,RobotEvent evt) {
				IRobotState item =null; 
		
		RobotBehavior behavior=getCurrentBehavior();
		
		if(history_evt.size()==MAX_HISTORY_EVT) history_evt.remove(0);
		history_evt.add(evt);
		
		RobotLog logData=new RobotLog();
		
		logData.setBehavior(behavior);
		
		logData.setEvent(evt);
		
		if(behavior!=null)
		{
			logData.setState(behavior.getCurrentState());
		}
		
		
		if(history_log.size()==MAX_HISTORY_LOG) history_log.remove(0);
		history_log.add(logData);
		
			
		
		if(behavior!=null) behavior.handle(ctx!=null?ctx:this.ctx,evt);
			
	}

}

