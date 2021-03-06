package com.kt.smartKibot;

import android.text.format.Time;

public class RobotEvent {
	private int type=EVT_UNKNOWN;
	private int param1=PARAM_UNKNOWN;
	private int param2=PARAM_UNKNOWN;
	private String extParam=null;
	private Time timeStamp=null;
	
	
	
	
	//even type definition
	public static final int EVT_UNKNOWN=-1111;
	public static final int PARAM_UNKNOWN=-1111;
	public static final int EVT_ALARM_GOOD_MORNING=0;
	public static final int EVT_ALARM_GOOD_BYE=1;
	public static final int EVT_ALARM_SCHEDULE_BREIF=2;
	
	public static final int EVT_TIMER=3;
	public static final int EVT_NOISE_DETECTION=4;
	public static final int EVT_BATTERY_STATE=5;
	public static final int EVT_TOUCH_BODY=6;
	public static final int EVT_FACE_DETECTION=7;
	public static final int EVT_FACE_RECOGNITION=8;
	public static final int EVT_TOUCH_SCREEN=9;
	public static final int EVT_FACE_LOST=10;

	
	
	
	public RobotEvent(int type, int param1, int param2, String extParam) {
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
		this.extParam = extParam;
		this.timeStamp=new Time();
		this.timeStamp.setToNow();
	}
	
	

	public RobotEvent(int type) {
		this.type = type;
		this.param1 = PARAM_UNKNOWN;
		this.param2 = PARAM_UNKNOWN;
		this.extParam = null;
		this.timeStamp=new Time();
		this.timeStamp.setToNow();
	}



	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the param1
	 */
	public int getParam1() {
		return param1;
	}

	/**
	 * @param param1 the param1 to set
	 */
	public void setParam1(int param1) {
		this.param1 = param1;
	}

	/**
	 * @return the param2
	 */
	public int getParam2() {
		return param2;
	}

	/**
	 * @param param2 the param2 to set
	 */
	public void setParam2(int param2) {
		this.param2 = param2;
	}

	/**
	 * @return the extParam
	 */
	public String getExtParam() {
		return extParam;
	}

	/**
	 * @param extParam the extParam to set
	 */
	public void setExtParam(String extParam) {
		this.extParam = extParam;
	}



	public Time getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(Time timeStamp) {
		this.timeStamp = timeStamp;
	}
	
}
