package com.kt.smartKibot;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ScheduleItem implements IRobotState {

	static final int BRIEF=1;
	static final int HOURLY=2;
	static final int HALF_DAY=3;
	
	static final String TAG="ScheduleItem";
	//Context ctx;
	Uri calendarUri;
	String[] projection;
	String ttsMsg;
	int thisMonth;
	int thisDate;
	
	public ScheduleItem()
	{
		//this.ctx=ctx;
	}
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		ttsMsg=new String();
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
			 thisMonth=c.get(Calendar.MONTH)+1;
			 thisDate=c.get(Calendar.DATE);

			 
		RobotFace.getInstance(ctx).on();
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
		
		//test
		int type=ScheduleItem.BRIEF; //param.getIntExtra("TYPE",ScheduleItem.BRIEF);
		
		switch(type)
		{
			case ScheduleItem.BRIEF:
			{
				
			 Calendar c = Calendar.getInstance();
	
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
				
				
				ttsMsg=""+thisMonth+"월"+ thisDate+"일 오늘 ";
				ttsMsg+="총 "+managedCursor.getCount()+" 건의 일정이 있습니다.";
				
				if(title.length>0)
				{
				for(int i=0;i<title.length;i++){
					ttsMsg+=title[i];
					if(i!=title.length-1)
						ttsMsg+="그리고 ";
				}
				
				ttsMsg+="입니다.";
				}
			}
			else{
				//schedule is empty
				ttsMsg=""+thisMonth+"월"+ thisDate+"일 오늘 ";
				ttsMsg+="일정이 없습니다.";
			}
			
			}
			break;
			
			case ScheduleItem.HALF_DAY:
				break;
			case ScheduleItem.HOURLY:
				break;
		
		}
		
		try{
			
		RobotMotion.getInstance(ctx).led(0,100,3);
		Thread.sleep(2000);
			
		
		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_LEFT);
		Thread.sleep(200);
		
		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_RIGHT);
		Thread.sleep(200);
		
		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
		Thread.sleep(200);
		
		RobotMotion.getInstance(ctx).goFoward(1, 1);
		
		Thread.sleep(200);
		RobotSpeech.getInstance(ctx).speakWithCmpNotification(ttsMsg,RobotFace.UTT_ID_FACE_ACT_FACE_OFF_10_SEC,null);
		
		
		}catch(Exception e){}
		

	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub

		RobotMotion.getInstance(ctx).offAllLed();
		//RobotFace.getSingleton(ctx).off();
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		
	}
	
	

}
