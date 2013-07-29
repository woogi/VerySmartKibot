package com.kt.smartKibot;

import android.graphics.Bitmap;
import android.text.format.Time;

public class User {
	
	public static final int CHECKIN_WITH_BT=1;
	public static final int CHECKIN_WITH_CAMEARA=2;
	
	private static int _id=1;
	
	public String name=null;
	public String btName=null;
	public int id=-1;
	public Bitmap picture=null;
	
	Time lastCheckIn=null;
	public int lastcheckInWith=-1;
	
	public User(String name,String btName){
		this.name=name;
		this.btName=btName;
		this.id=_id++;
	}
	
	public void setPicture(Bitmap pic){
		this.picture=pic;
	}

}
