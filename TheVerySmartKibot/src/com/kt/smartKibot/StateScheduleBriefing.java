package com.kt.smartKibot;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class StateScheduleBriefing implements IRobotState {

	static final int UNKNOWN=-1;
	static final int DAILY=1;
	static final int HOURLY=2;
	static final int HALF_DAY=3;
	
	static final boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	
	static final String TAG="StateScheduleBriefing";
	int type_brief=UNKNOWN;
	int targetHour=-1;
	
	public StateScheduleBriefing(int type)
	{
		type_brief=type;
		targetHour=-1;
	}
	
	public StateScheduleBriefing(int type,int hour)
	{
		type_brief=type;
		targetHour=hour;
	}
	
	@Override
	public void onStart(Context ctx) {
		
		if (_DEBUG) {
			
			String msg=TAG;
			if(type_brief==DAILY)
				msg+=":daily";
			
			if(type_brief==HOURLY)
				msg+=":hourly("+targetHour+")";
			
			RobotFace.getInstance(ctx).change(RobotFace.MODE_READING, msg);
		} else {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_READING);
		}
		
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		
		int type=type_brief;
		
		Schedule schedule =new Schedule(ctx,_DEBUG);
		String msg="ㅎㅎㅎㅎ";//initial value for debugging
		
		switch(type)
		{
			case DAILY:
			{
				msg=schedule.briefToday();
			}
			break;
			
			case HALF_DAY:
				break;
			case HOURLY:
				msg=schedule.briefHourly(targetHour);
				break;
		}
		
		try{
			
			int cnt=0;
			boolean moveBack=false;
			
			RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
			
			while(!isEnd)
			{
			
				if(cnt==0) RobotMotion.getInstance(ctx).goForward(1,5);
					
				if(cnt==10) RobotSpeech.getInstance(ctx).speak(msg);
				if(cnt>40 && RobotSpeech.getInstance(ctx).isSpeaking()==false && moveBack==false) {
					RobotMotion.getInstance(ctx).goBack(1,5);
					moveBack=true;
				}
				
					moveBack=true;
				Thread.sleep(100);
				++cnt;
			}
		}
		catch(Exception e){e.printStackTrace();}
		
	}

	@Override
	public void cleanUp(Context ctx) {
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);

	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;
		
	}
	
	

}

class Schedule{
	
	Uri calendarUri;
	String[] projection;
	int thisMonth;
	int thisDate;
	Context ctx;
	private boolean _DEBUG=true;
	
	Schedule(Context ctx,boolean debug){
		this.ctx=ctx;
		this._DEBUG=debug;
		Calendar c = Calendar.getInstance();
		thisMonth=c.get(Calendar.MONTH)+1;
		thisDate=c.get(Calendar.DATE);
	}
	public String briefHourly(int targetHour)
	{
		String msg="";
		calendarUri = Uri.parse("content://com.android.calendar/events");
		projection = new String[] {
				"calendar_id",
				"title",
				"eventLocation",
				"description",
				"eventStatus",
				"selfAttendeeStatus",
				"dtstart",
				"dtend",
				"eventTimezone",
				"duration",
				"allDay",
				"hasAlarm",
				"hasExtendedProperties",
				"rrule",
				"rdate",
				"exrule",
				"exdate",
				"originalInstanceTime",
				"originalAllDay",
				"lastDate",
				"hasAttendeeData",
				"guestsCanModify",
				"guestsCanInviteOthers",
				"guestsCanSeeGuests",
				"organizer",
				"deleted"};
		
			 Calendar c = Calendar.getInstance();
	
			 
			 //set c to targetHour 
			 c.set(Calendar.HOUR_OF_DAY,targetHour);
			 c.set(Calendar.MINUTE, 0);
			 c.set(Calendar.SECOND, 0);
			 c.set(Calendar.MILLISECOND, 0);
	
			long minDate=c.getTimeInMillis();  /*start of targetHour*/
			long maxDate=minDate+1*60*60*1000; /* end of targetHour*/
			
			Cursor managedCursor = ctx.getContentResolver().query(calendarUri, projection,"dtstart>="+minDate+" AND dtstart<"+maxDate,
				null, null) ;
				
			if(managedCursor.moveToFirst()) {
				
				int[] calendar_id               = new int[managedCursor.getCount()];
				String[] title                  = new String[managedCursor.getCount()];
				String[] eventLocation          = new String[managedCursor.getCount()];
				String[] description            = new String[managedCursor.getCount()];
				int[] eventStatus               = new int[managedCursor.getCount()];
				int[] selfAttendeeStatus        = new int[managedCursor.getCount()];
				long[] dtstart                = new long[managedCursor.getCount()];
				long[] dtend                  = new long[managedCursor.getCount()];
				String[] eventTimezone          = new String[managedCursor.getCount()];
				String[] duration               = new String[managedCursor.getCount()];
				int[] allDay                    = new int[managedCursor.getCount()];
				int[] hasAlarm                  = new int[managedCursor.getCount()];
				int[] hasExtendedProperties     = new int[managedCursor.getCount()];
				String[] rrule                  = new String[managedCursor.getCount()];
				String[] rdate                  = new String[managedCursor.getCount()];
				String[] exrule                 = new String[managedCursor.getCount()];
				String[] exdate                 = new String[managedCursor.getCount()];
				int[] originalInstanceTime      = new int[managedCursor.getCount()];
				int[] originalAllDay            = new int[managedCursor.getCount()];
				int[] lastDate               = new int[managedCursor.getCount()];
				int[] hasAttendeeData           = new int[managedCursor.getCount()];
				int[] guestsCanModify           = new int[managedCursor.getCount()];
				int[] guestsCanInviteOthers     = new int[managedCursor.getCount()];
				int[] guestsCanSeeGuests        = new int[managedCursor.getCount()];
				String[] organizer              = new String[managedCursor.getCount()];
				int[] deleted                   = new int[managedCursor.getCount()];
				
				
				for (int i = 0 ; i < title.length ; i++) {
					calendar_id[i] = managedCursor.getInt(0);
					Log.i("Calendar", "ID : " + calendar_id[i]);
					title[i] = managedCursor.getString(1);
					Log.i("Calendar", "title : " + title[i]);
					eventLocation[i] = managedCursor.getString(2);
					Log.i("Calendar", "eventLocation : " + eventLocation[i]);
					description[i] = managedCursor.getString(3);
					Log.i("Calendar", "desc : " + description[i]);
					eventStatus[i] = managedCursor.getInt(4);
					selfAttendeeStatus[i] = managedCursor.getInt(5);
					dtstart[i] = managedCursor.getLong(6);
					Log.i("Calendar", "dtstart : " + dtstart[i]);
					dtend[i] = managedCursor.getLong(7);
					Log.i("Calendar", "dtend : " + dtend[i]);
					eventTimezone[i] = managedCursor.getString(8);
					duration[i] = managedCursor.getString(9);
					allDay[i] = managedCursor.getInt(10);
					hasAlarm[i] = managedCursor.getInt(11);
					hasExtendedProperties[i] = managedCursor.getInt(12);
					rrule[i] = managedCursor.getString(13);
					rdate[i] = managedCursor.getString(14);
					Log.i("Calendar", "rdate : " + rdate[i]);
					exrule[i] = managedCursor.getString(15);
					exdate[i] = managedCursor.getString(16);
					originalInstanceTime[i] = managedCursor.getInt(17);
					originalAllDay[i] = managedCursor.getInt(18);
					lastDate[i] = managedCursor.getInt(19);
					hasAttendeeData[i] = managedCursor.getInt(20);
					guestsCanModify[i] = managedCursor.getInt(21);
					guestsCanInviteOthers[i] = managedCursor.getInt(22);
					guestsCanSeeGuests[i] = managedCursor.getInt(23);
					organizer[i] = managedCursor.getString(24);
					deleted[i] = managedCursor.getInt(25);
					managedCursor.moveToNext() ;
				}
				
				msg="소장님! 일정을 알려 드릴께요 ";
				
				msg+=thisMonth+"월"+ thisDate+"일 오늘 ";
				msg+=""+targetHour%12 +"시 부터 "+ (targetHour+1)%12 +"시 까지 ";
				msg+="총 "+managedCursor.getCount()+" 건의 일정이 있습니다.";
				
				if(title.length>0)
				{
					for(int i=0;i<title.length;i++){
						
						if(i==0)
							msg+="첫번째 일정";
						if(i==1)
							msg+="두번째 일정";
						if(i==2)
							msg+="세번째 일정";
						if(i==3)
							msg+="네번째 일정";
						if(i==4)
							msg+="다섯번째 일정";
						
						msg+="제목 "+title[i];
						
						if(eventLocation[i]!=null && eventLocation[i].trim().length()!=0)
						{
							msg+=" 장소 "+ eventLocation[i];
						}
						if(description[i]!=null && description[i].trim().length()!=0)
						{
							msg+=" 내용 "+ description[i];
						}
						
					}
				
				msg+="이상 입니다.";
				}
			}
			else{
				//schedule is empty
				msg="소장님! 일정을 알려 드릴께요 ";
				msg+=thisMonth+"월"+ thisDate+"일 오늘 ";
				msg+=""+targetHour%12 +"시 부터 "+ (targetHour+1)%12 +"시 까지는 ";
				msg+="일정이 없습니다.";
			}
			
		return msg;
		
	}
	
	public String briefToday(){
		String msg="";
		calendarUri = Uri.parse("content://com.android.calendar/events");
		projection = new String[] {
				"calendar_id",
				"title",
				"eventLocation",
				"description",
				"eventStatus",
				"selfAttendeeStatus",
				"dtstart",
				"dtend",
				"eventTimezone",
				"duration",
				"allDay",
				"hasAlarm",
				"hasExtendedProperties",
				"rrule",
				"rdate",
				"exrule",
				"exdate",
				"originalInstanceTime",
				"originalAllDay",
				"lastDate",
				"hasAttendeeData",
				"guestsCanModify",
				"guestsCanInviteOthers",
				"guestsCanSeeGuests",
				"organizer",
				"deleted"};
		
			 Calendar c = Calendar.getInstance();
	
			 
			 //set c to start of today.
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 c.set(Calendar.MINUTE, 0);
			 c.set(Calendar.SECOND, 0);
			 c.set(Calendar.MILLISECOND, 0);
	
			long minDate=c.getTimeInMillis();  /*start of today*/
			long maxDate=minDate+24*60*60*1000; /* end of today*/
			
			Cursor managedCursor = ctx.getContentResolver().query(calendarUri, projection,"dtstart>"+minDate+" AND dtstart<"+maxDate,
				null, null) ;
				
			if(managedCursor.moveToFirst()) {
				
				int[] calendar_id               = new int[managedCursor.getCount()];
				String[] title                  = new String[managedCursor.getCount()];
				String[] eventLocation          = new String[managedCursor.getCount()];
				String[] description            = new String[managedCursor.getCount()];
				int[] eventStatus               = new int[managedCursor.getCount()];
				int[] selfAttendeeStatus        = new int[managedCursor.getCount()];
				long[] dtstart                = new long[managedCursor.getCount()];
				long[] dtend                  = new long[managedCursor.getCount()];
				String[] eventTimezone          = new String[managedCursor.getCount()];
				String[] duration               = new String[managedCursor.getCount()];
				int[] allDay                    = new int[managedCursor.getCount()];
				int[] hasAlarm                  = new int[managedCursor.getCount()];
				int[] hasExtendedProperties     = new int[managedCursor.getCount()];
				String[] rrule                  = new String[managedCursor.getCount()];
				String[] rdate                  = new String[managedCursor.getCount()];
				String[] exrule                 = new String[managedCursor.getCount()];
				String[] exdate                 = new String[managedCursor.getCount()];
				int[] originalInstanceTime      = new int[managedCursor.getCount()];
				int[] originalAllDay            = new int[managedCursor.getCount()];
				int[] lastDate               = new int[managedCursor.getCount()];
				int[] hasAttendeeData           = new int[managedCursor.getCount()];
				int[] guestsCanModify           = new int[managedCursor.getCount()];
				int[] guestsCanInviteOthers     = new int[managedCursor.getCount()];
				int[] guestsCanSeeGuests        = new int[managedCursor.getCount()];
				String[] organizer              = new String[managedCursor.getCount()];
				int[] deleted                   = new int[managedCursor.getCount()];
				
				
				for (int i = 0 ; i < title.length ; i++) {
					calendar_id[i] = managedCursor.getInt(0);
					Log.i("Calendar", "ID : " + calendar_id[i]);
					title[i] = managedCursor.getString(1);
					Log.i("Calendar", "title : " + title[i]);
					eventLocation[i] = managedCursor.getString(2);
					Log.i("Calendar", "eventLocation : " + eventLocation[i]);
					description[i] = managedCursor.getString(3);
					Log.i("Calendar", "desc : " + description[i]);
					eventStatus[i] = managedCursor.getInt(4);
					selfAttendeeStatus[i] = managedCursor.getInt(5);
					dtstart[i] = managedCursor.getLong(6);
					Log.i("Calendar", "dtstart : " + dtstart[i]);
					dtend[i] = managedCursor.getLong(7);
					Log.i("Calendar", "dtend : " + dtend[i]);
					eventTimezone[i] = managedCursor.getString(8);
					duration[i] = managedCursor.getString(9);
					allDay[i] = managedCursor.getInt(10);
					hasAlarm[i] = managedCursor.getInt(11);
					hasExtendedProperties[i] = managedCursor.getInt(12);
					rrule[i] = managedCursor.getString(13);
					rdate[i] = managedCursor.getString(14);
					Log.i("Calendar", "rdate : " + rdate[i]);
					exrule[i] = managedCursor.getString(15);
					exdate[i] = managedCursor.getString(16);
					originalInstanceTime[i] = managedCursor.getInt(17);
					originalAllDay[i] = managedCursor.getInt(18);
					lastDate[i] = managedCursor.getInt(19);
					hasAttendeeData[i] = managedCursor.getInt(20);
					guestsCanModify[i] = managedCursor.getInt(21);
					guestsCanInviteOthers[i] = managedCursor.getInt(22);
					guestsCanSeeGuests[i] = managedCursor.getInt(23);
					organizer[i] = managedCursor.getString(24);
					deleted[i] = managedCursor.getInt(25);
					managedCursor.moveToNext() ;
				}
				
				msg="소장님! 일정을 알려 드릴께요 ";
				
				msg+=thisMonth+"월"+ thisDate+"일 오늘 ";
				msg+="총 "+managedCursor.getCount()+" 건의 일정이 있습니다.";
				
				if(title.length>0)
				{
					for(int i=0;i<title.length;i++){
						msg+=title[i];
						if(i!=title.length-1)
							msg+="그리고 ";
					}
				
				msg+="입니다.";
				}
			}
			else{
				//schedule is empty
				msg="소장님! 일정을 알려 드릴께요 ";
				msg+=thisMonth+"월"+ thisDate+"일 오늘 ";
				msg+="일정이 없습니다.";
			}
			
		return msg;
	}
	
}