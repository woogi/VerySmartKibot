package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;



public class RobotBrain implements IRobotEvtHandler{

	private Context ctx;
	private ArrayList<RobotBehavior> history_behavior;
	private ArrayList<RobotEvent> history_evt;
	
	public static final int MAX_HISTORY_BEHAVIOR=5;
	public static final int MAX_HISTORY_EVT=10;
	

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
		batteryChecker.stop();
		batteryChecker=null;
		
	}
	
	public RobotBrain(Context ctx) {
		this.ctx = ctx;
		
		this.history_behavior=new ArrayList<RobotBehavior>(MAX_HISTORY_BEHAVIOR);
		this.history_evt=new ArrayList<RobotEvent>(MAX_HISTORY_EVT);
		
		dayTimeBehavior=new DayTimeBehavior();
		
		//timer handler 등록
		RobotTimer.getInstance().installHandler(this);
		
		//noise detector handler 등록
		NoiseDetector.getInstance().installHandler(this);
		
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
		/*
		switch(evt.getType())
		{
			case RobotEvent.EVT_ALARM_SCHEDULE_BREIF:
				ScheduleItem scheduleHandler=new ScheduleItem(ctx);
				item=scheduleHandler;
			break;
				
			case RobotEvent.EVT_ALARM_GOOD_MORNING:
				GoodMItem goodMorningHandler=new GoodMItem(ctx);
				item=goodMorningHandler;
			break;
				
			case RobotEvent.EVT_ALARM_GOOD_BYE:
				GoodBItem goodByeHandler=new GoodBItem(ctx);
				item=goodByeHandler;
			break;
		}
		
		// TODO Auto-generated method stub
		StateHandler handler= new StateHandler(item);
		handler.start();
		*/
		
		if(behavior!=null) behavior.handle(ctx!=null?ctx:this.ctx,evt);
			
	}

}

