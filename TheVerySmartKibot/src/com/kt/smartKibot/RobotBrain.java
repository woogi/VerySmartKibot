package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
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
	private RobotBehavior testBehavior;
	private RobotBehavior activeBehavior;
	private RobotBehavior calmBehavior;
	private RobotBehavior nightBehavior;
	private Handler activityHandler=null;
	
	
	private static final boolean _DEBUG=true;
	private static int cnt_swipeForDbgWindow=0;
	private static int cnt_swipeForTestBehavior=0;
	
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
		
		RobotChatting.getInstance(ctx).stop();
//		BTMotionDetector.getInstance(ctx).stop();
	}
	
	public RobotBrain(Context ctx,Handler handler) {
		this.ctx = ctx;
		this.activityHandler=handler;
		
		this.history_behavior=new ArrayList<RobotBehavior>(MAX_HISTORY_BEHAVIOR);
		this.history_evt=new ArrayList<RobotEvent>(MAX_HISTORY_EVT);
		this.history_log=new ArrayList<RobotLog>(MAX_HISTORY_LOG);
		
		dayTimeBehavior=new DayTimeBehavior(history_log);
		testBehavior=new TestBehavior();
		activeBehavior=new ActiveBehavior(history_log);
		calmBehavior=new CalmBehavior(history_log);
		nightBehavior=new NightTimeBehavior(history_log,true);
		
		//timer handler 등록
		RobotTimer.getInstance().installHandler(this);
		
		//noise detector handler 등록
		NoiseDetector.getInstance(ctx).installHandler(this);
		BTMotionDetector.getInstance(ctx).installHandler(this);
		RobotChatting.getInstance(ctx).installHandler(this);
		RobotChatting.getInstance(ctx).start();
		
		//face detector handler
    	FaceDetector.getInstance(ctx).installHandler(this);

		//body touch event handler 등록 
		TouchDetector.getInstance().installHandler(this);
		TouchDetector.getInstance().start();
		
		//battery checker handler 등
		batteryChecker=new BatteryChecker(ctx);
		batteryChecker.installHandler(this);
		batteryChecker.start();
		
		
		//test
		User woogi=new User("이상욱","Woogic");
		woogi.id=3;
		User nicolas=new User("니콜라","Nicolas");
		nicolas.id=2;
		
		LogIn.getInstance().addUser(woogi);
		LogIn.getInstance().addUser(nicolas);
		
//		BTMotionDetector.getInstance(ctx).setTargetDevName("Woogic");
//		BTMotionDetector.getInstance(ctx).start();
		
				
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
		
		
		if(evt.getType()==RobotEvent.EVT_TIMER)
		{
			activityHandler.sendEmptyMessage(RobotActivity.UPDATE_LOGIN_UI);
		}
		
		
		
		
		if(evt.getType()==RobotEvent.EVT_ROBOT_CHATTING_ASK)
		{
			Log.d(TAG,"EVT_ROBOT_CHATTING_ASK param1:"+evt.getParam1()+" ext:"+evt.getExtParam());
		
			
			
			String whatSay=evt.getExtParam();
			
			if(evt.getParam1()==1 && whatSay!=null)
			{
			
				if(whatSay.equals("시끄러")||whatSay.equals("씨끄러")  
					||whatSay.indexOf("조용히")!=-1
					||whatSay.indexOf("조용이")!=-1
					||whatSay.equals("쉿"))
				{
					if (DayTimeBehavior.class.isInstance(behavior) ){
						changeBehavior(nightBehavior);
						return;
					}
					
				}
				
			}
		}
		
		if(evt.getType()==RobotEvent.EVT_TIMER_HOURLY)
		{
			if( (evt.getParam1() <7 || evt.getParam1()>=19 ) && !NightTimeBehavior.class.isInstance(behavior) ) 
			{
				//forecely change to NightTime behavior
				//test
			//	changeBehavior(nightBehavior);
				
			}
		}
		
		if(evt.getType()==RobotEvent.EVT_LONG_PRESS_SCREEN){
			
//			if (DayTimeBehavior.class.isInstance(behavior) ){
//				changeBehavior(calmBehavior);
//				return;
//			}
//			
//			if (CalmBehavior.class.isInstance(behavior) ){
//				changeBehavior(activeBehavior);
//				return;
//			}
			
			
			if (DayTimeBehavior.class.isInstance(behavior) ){
				changeBehavior(nightBehavior);
				return;
			}
			
			if (NightTimeBehavior.class.isInstance(behavior) ){
					changeBehavior(dayTimeBehavior);
				return;
				}
			
			if (TestBehavior.class.isInstance(behavior) ){
					changeBehavior(dayTimeBehavior);
				return;
			}
		}
		
		
		if(evt.getType()==RobotEvent.EVT_SWIPE_SCREEN)
		{
			switch(evt.getParam1())
			{
				case 0://right to left
					if(cnt_swipeForTestBehavior++>4){
					changeBehavior(testBehavior);// no need to send events to behavior.
					}
					return;
					//break;
					
				case 1:// left to right
					cnt_swipeForTestBehavior=0;
					changeBehavior(dayTimeBehavior);
					return;
					//break;
					
				case 2: //down to up
					if(cnt_swipeForDbgWindow++ >4){
					RobotActivity.showDbgLogScreen(true);
					}
					break;
					
				case 3: //up to down
					cnt_swipeForDbgWindow=0;
					RobotActivity.showDbgLogScreen(false);
					break;
			}
			
		}
			
		if(evt.getType()==RobotEvent.EVT_TOUCH_SCREEN)
		{
			
			
		}
		
		if(behavior!=null) behavior.handle(ctx!=null?ctx:this.ctx,evt);
			
	}

}

