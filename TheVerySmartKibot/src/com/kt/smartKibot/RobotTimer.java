package com.kt.smartKibot;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class RobotTimer implements IRobotEvtDelegator {

	static final String TAG="RobotTimer";
	
	
	private IRobotEvtHandler handler;
	private Timer _t=null;
	
	private static RobotTimer _this;
	
	private static volatile int _cnt=0;
	
	static int lastHour=-1;
	
	static RobotTimer getInstance(){
		
		if(_this==null) _this=new RobotTimer();
		
		return _this;
		
	}
	
	synchronized public void reset(){
		_cnt=0;
	}
	
	@Override
	synchronized public void start(){
		_t=new Timer();
		_cnt=0;
		_t.scheduleAtFixedRate(new Scheduler(),/*1000*10*/0,5*1000); /* every 5 sec*/
	}
	
	@Override
	synchronized public void stop(){
		_t.cancel();
		try{
		Thread.sleep(100);
		}catch(Exception e){e.printStackTrace();}
		
		_t=null;
	}
	
	@Override
	public void installHandler(IRobotEvtHandler handler) {
		// TODO Auto-generated method stub
		
		this.handler=handler;
		
		
	}

	@Override
	public void uninstallHandler() {
		// TODO Auto-generated method stub

		handler=null;
		
	}
	
	
	class Scheduler extends TimerTask  {
	
		public void run(){
		
			
			RobotEvent evt=null;
			Calendar c = Calendar.getInstance();
			int thisHour=c.get(Calendar.HOUR_OF_DAY);
		
			if(lastHour!=thisHour)
			{
				evt=new RobotEvent(RobotEvent.EVT_TIMER_HOURLY,thisHour,-1,null);
				Log.d(TAG,"EVT_TIMER_HOURLY hour:"+thisHour);
				RobotActivity.writeLog("EVT_TIMER_HOURLY hour:"+thisHour);
				lastHour=thisHour;
				reset();
			}
			else{
				evt=new RobotEvent(RobotEvent.EVT_TIMER,++_cnt,-1,null);
				Log.d(TAG,"EVT_TIMER count:"+_cnt);
			}
			
			
			
			handler.handle(null,evt);
		}
	}
	  

}
