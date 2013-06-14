package com.kt.smartKibot;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.IRobotAsyncWorkListener;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmAgent{

	static final String TAG="AlarmAgent";
	private AlarmManager alarmManager;
	private Context ctx;
	//RobotController robotCtl;
	IRobotAsyncWorkListener.Stub listener;
	
	
	private void setAlarm(Date date,PendingIntent cb,boolean repeat)
	{
		if(repeat==false)
		{
			//use set method
			
		}else
		{
			Log.d(TAG,"Alram Set hour:"+date.getHours()+" minute:"+date.getMinutes());
			//use setRepeating
			//interval은 daily만 지원 
			//alarmManager.set(type, triggerAtMillis, operation)
			//test
			//alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,1000*10,cb);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,date.getTime(),AlarmManager.INTERVAL_DAY,cb);
		}
	}
	
	public void init(Context ctx){
		
		this.ctx=ctx;
		
		alarmManager=(AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
		
	}
	
	public void cleanUp(){
		
	}
	
	public void regSchedule(IRobotEvtHandler handler){
		
		//read schedule from a configuration file
		
		//register schedule to Alarm system
		
		new AlarmReceiver().installHandler(handler);
		
		Calendar ca=Calendar.getInstance();
		Intent ti_morning_brief=new Intent(ctx,AlarmReceiver.class);
		ti_morning_brief.addCategory("SCHEDULE");
		ti_morning_brief.putExtra("TYPE",ScheduleItem.BRIEF);
		
		
		//todo change --
		//test
		PendingIntent pi_morning=PendingIntent.getBroadcast(ctx, 0, ti_morning_brief, 0);
		ca.add(Calendar.SECOND,5);
		setAlarm(ca.getTime(),pi_morning,true);
		
		
		Intent ti_bye=new Intent(ctx,AlarmReceiver.class);
		ti_bye.addCategory("GOODBYE");

		PendingIntent pi=PendingIntent.getBroadcast(ctx, 0, ti_bye, 0);
		ca=Calendar.getInstance();
		ca.add(Calendar.SECOND,35);
		setAlarm(ca.getTime(),pi,true);
		
		// Toast.makeText(ctx,"alarms are registered!",Toast.LENGTH_SHORT).show();
		RobotActivity.writeLog("alarms are registered!");
		
	}
}
