package com.kt.smartKibot;

import android.content.Context;

public class TouchDetector implements IRobotEvtDelegator {

	private static TouchDetector _this;
	private IRobotEvtHandler handler=null;
	private boolean isStart=false;
	static final String TAG="TouchDetector";
	
	public static final int PARAM_HEAD_LONG_PRESSED=0;
	public static final int PARAM_HEAD_PRESSED=1;
	public static final int PARAM_LEFT_EAR_PATTED=2;
	public static final int PARAM_RIGHT_EAR_PATTED=3;
	public static final int PARAM_LEFT_FOOT_PRESSED=4;
	public static final int PARAM_RIGHT_FOOT_PRESSED=5;
	
	
	static TouchDetector getInstance(){
		
		if(_this==null) _this=new TouchDetector();
		
		return _this;
		
	}
	
	public IRobotEvtHandler getHandler(){
		return handler;
	}
	
	public void sendEvent(Context ctx, int param1){
		
		if(handler!=null && isStart==true){
			RobotEvent evt=new RobotEvent(RobotEvent.EVT_TOUCH_BODY);
			evt.setParam1(param1);
			handler.handle(ctx, evt);
		}
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
		isStart=true;
	}

	@Override
	public void stop() {
		isStart=false;

	}

}
