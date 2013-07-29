package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import android.text.format.Time;
import android.util.Log;

public class LogIn {
	
	private static LogIn _this;
	public static final String TAG="LogIn";
	
	private ArrayList<User> registered_users;

	static public LogIn getInstance(){
		if(_this==null)
		{
			_this=new LogIn();
		}
		
		return _this;
		
	}
	
	private LogIn(){
		registered_users=new ArrayList<User>(3);
	}
	
	public void addUser(User user){registered_users.add(user);};
	
	public void checkInWithBT(Time timeStamp,String btName){
		
		Iterator<User> it= registered_users.iterator();
		while(it.hasNext()){
			User user=it.next();
			if(user.btName.equals(btName)){
				user.lastcheckInWith=User.CHECKIN_WITH_BT;
				user.lastCheckIn=new Time();
				user.lastCheckIn.setToNow();
			}
		}
		
		
	Log.d(TAG,"checkInWithBT"+ timeStamp.minute+"m "+timeStamp.second+"s");
		
		
		
	};
	
	public void checkInWithCamera(Time timeStamp,int id){
		
		Iterator<User> it= registered_users.iterator();
		while(it.hasNext()){
			User user=it.next();
			if(user.id==id){
				user.lastcheckInWith=User.CHECKIN_WITH_CAMEARA;
				user.lastCheckIn=new Time();
				user.lastCheckIn.setToNow();
				
			}
		}
		
		
	}
	
	public User getUserById(int id){
		
		User targetUser=null;
		Iterator<User> it= registered_users.iterator();
		while(it.hasNext()){
			User user=it.next();
			
			if(user.id==id)
			{
				targetUser=user;
				break;
			}
		}
		
		
		return targetUser;
		
	}
	
	public User getLastCheckInUser(){
		
		User lastCheckInUser=null;
		
		
		Iterator<User> it= registered_users.iterator();
		while(it.hasNext()){
			User user=it.next();
			
			if(lastCheckInUser!=null){
				
				if(user!=null && user.lastCheckIn!=null) //checkin 한적이 있다면 비교   
				{
					if(user.lastCheckIn.toMillis(true) > lastCheckInUser.lastCheckIn.toMillis(true))
					{
						lastCheckInUser=user;
					}
				}
			}
			else{
				
				if(user!=null && user.lastCheckIn!=null) //checkin 한적이 있다면 
				{
					lastCheckInUser=user;
				}
			}
		}
		
		if(lastCheckInUser!=null){
			Log.d(TAG,"last login  name:"+ lastCheckInUser.name+" bt name:"+lastCheckInUser.btName+ " time:"+lastCheckInUser.lastCheckIn.minute+"m "+lastCheckInUser.lastCheckIn.second);
		}
		else{
			Log.d(TAG,"no one login");
		}
		
		return lastCheckInUser;
		
	}
	
	public User whosLogIn(){
		
	
		Time curTime=new Time();
		curTime.setToNow();
		
		User lastCheckInUser=getLastCheckInUser();
	
		if(lastCheckInUser!=null && lastCheckInUser.lastCheckIn!=null 
				&&curTime.toMillis(true)< lastCheckInUser.lastCheckIn.toMillis(true)+1000*60*3  /*test*/) // 20 min
		{
			Log.d(TAG,"it's passed about " + (curTime.toMillis(true)-lastCheckInUser.lastCheckIn.toMillis(true) )/1000 +" sec after last login");
			
			return lastCheckInUser;
		}
		else{
			
			return null;
		}
		
	};

}
