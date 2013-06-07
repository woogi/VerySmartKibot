package com.kt.smartKibot;

import android.content.Context;
import android.content.Intent;



public class RobotFace{
	
	private Context ctx;
	private static RobotFace _this=null;
	
	
	public static final int UTT_ID_FACE_ACT_FACE_OFF_IMM=0;
	public static final int UTT_ID_FACE_ACT_FACE_OFF_10_SEC=1;
	public static final int UTT_ID_FACE_ACT_FACE_OFF_20_SEC=2;
	public static final int UTT_ID_FACE_ACT_FACE_OFF_30_SEC=3;
	public static final int UTT_ID_FACE_ACT_FACE_OFF_60_SEC=6;

	public static final int MODE_UNKNOWN=-1;
	public static final int MODE_ATTENTION = 1;
	public static final int MODE_EATTING = 2;
	public static final int MODE_LOVE = 3;
	public static final int MODE_SAD = 4;
	public static final int MODE_EXCITED = 5;
	public static final int MODE_BRUSH = 6;
	public static final int MODE_YAWN = 7;
	public static final int MODE_FUN = 8;
	public static final int MODE_PLEASED = 9;
	public static final int MODE_WASHING = 10;
	public static final int MODE_SLEEP = 11;
	public static final int MODE_SERIOUS = 12;
	public static final int MODE_READING = 13;
	public static final int MODE_ANGRY = 14;
	public static final int MODE_WINK_LEFT = 15;
	public static final int MODE_WINK_RIGHT = 16;
	
	public void off(){
		Intent intent = new Intent();
		intent = new Intent(ctx, RobotActivity.class);
		intent.setAction("com.kt.kibot.FinishFace");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
		
	}
	
	public void on(){
		
		change(MODE_FUN);
	}
	
	public void change(int mode){
		
		Intent intent = new Intent();

		intent = new Intent(ctx, RobotActivity.class);
		intent.setAction(RobotActivity.ACTION_CHANGE_FACE);
		intent.putExtra("_id", mode);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
		
	
	}
	
	public void change(int mode,String message){
		
		Intent intent = new Intent();

		intent = new Intent(ctx, RobotActivity.class);
		intent.setAction(RobotActivity.ACTION_CHANGE_FACE);
		intent.putExtra("_id", mode);
		intent.putExtra("_toast_message", message);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	
	}

	
	private RobotFace(Context ctx){
		
		this.ctx=ctx;
		
	}
	

	
	static synchronized RobotFace getInstance(Context ctx){
		if(_this==null) _this=new RobotFace(ctx);
		
		return _this;
	}
	
	static synchronized void finish(){
		if(_this==null) return;
		
		_this=null;
	}
}
