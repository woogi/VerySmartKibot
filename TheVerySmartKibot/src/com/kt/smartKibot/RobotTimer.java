package com.kt.smartKibot;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class RobotTimer implements IRobotEvtDelegator {

	static final String TAG="RobotTimer";
	
	
	private IRobotEvtHandler handler;
	private Timer _t=null;
	
	private static RobotTimer _this;
	
	private static volatile int _cnt=0;
	
	
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
		_t.scheduleAtFixedRate(new Scheduler(),/*1000*10*/0,1000*10); /* every 10 sec*/
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
		
			
			RobotEvent evt=new RobotEvent(RobotEvent.EVT_TIMER,++_cnt,0,null);
			Log.d(TAG,"count:"+_cnt);
			
			handler.handle(null,evt);
		}
	}
	  

}
