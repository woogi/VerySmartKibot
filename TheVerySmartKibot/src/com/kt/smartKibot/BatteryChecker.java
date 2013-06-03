package com.kt.smartKibot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BatteryChecker implements IRobotEvtDelegator {
	
	BroadcastReceiver batteryStateRcv=null;
	IRobotEvtHandler handler=null;
	Context ctx=null;
	
	static final int PARAM_BATTERY_LOW=0;
	static final int PARAM_BATTERY_OKAY=1;
	static final int PARAM_POWER_CONNECTED=2;
	static final int PARAM_POWER_DISCONNECTED=3;
	
			
	public BatteryChecker(Context ctx){
		this.ctx=ctx;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
		batteryStateRcv=new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action=intent.getAction();
				
				/*
				if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
					
				}
				*/
				
				RobotEvent evt=new RobotEvent(RobotEvent.EVT_BATTERY_STATE);
				
				if(action.equals(Intent.ACTION_BATTERY_LOW)){
					evt.setParam1(PARAM_BATTERY_LOW);
				}
				
				if(action.equals(Intent.ACTION_BATTERY_OKAY)){
					evt.setParam1(PARAM_BATTERY_OKAY);
					
				}
				
				if(action.equals(Intent.ACTION_POWER_CONNECTED)){
					evt.setParam1(PARAM_POWER_CONNECTED);
					
				}
				
				if(action.equals(Intent.ACTION_POWER_DISCONNECTED)){
					evt.setParam1(PARAM_POWER_DISCONNECTED);
					
				}
				
				if(evt.getParam1()!=RobotEvent.PARAM_UNKNOWN){
					handler.handle(ctx, evt);
					
				}
				
			}
		};
		
		IntentFilter filter=new IntentFilter();
		//filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_OKAY);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		
		ctx.registerReceiver(batteryStateRcv,filter);
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
		ctx.unregisterReceiver(batteryStateRcv);
	}

	@Override
	public void installHandler(IRobotEvtHandler handler) {
		// TODO Auto-generated method stub
		this.handler=handler;

	}

	@Override
	public void uninstallHandler() {
		// TODO Auto-generated method stub
		this.handler=null;

	}

}
