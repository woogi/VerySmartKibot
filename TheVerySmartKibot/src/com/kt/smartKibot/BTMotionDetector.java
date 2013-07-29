package com.kt.smartKibot;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

public class BTMotionDetector implements IRobotEvtDelegator, BTSignalCheckThread.IMovementListener{

	
	private static BTMotionDetector _this;
	private IRobotEvtHandler handler=null;
	private boolean isStart=false;
	private Context ctx=null;
	private String devName=null;
	private volatile int oldDir=-1;
	private BTSignalCheckThread signalCheckThread;
	static final String TAG="BTMotionDetector";
	

	
	public static final int PARAM_GOAWAY=2;
	public static final int PARAM_COMMING=1;
	public static final int PRAM_STAY=0;


	static public BTMotionDetector getInstance(Context ctx){
		
		if(_this==null){
		_this=new BTMotionDetector();
		}
		
		_this.ctx=ctx;
		
		_this.oldDir=-1;
		return _this;
	}
	
	@Override
	public void installHandler(IRobotEvtHandler handler) {
			this.handler=handler;
	}

	@Override
	public void uninstallHandler() {
			this.handler=null;
	}

	public void setTargetDevName(String devName){
		this.devName=devName;
		
	}
	
	@Override
	public void start() {
		signalCheckThread=new BTSignalCheckThread(ctx,devName, this);
		signalCheckThread.start();
		

	}

	@Override
	public void stop() {
		
		
		if(signalCheckThread!=null){
			signalCheckThread.finish();
			signalCheckThread=null;
		}

	}
	
	
	public void reset(){
		oldDir=-1;
		Log.d(TAG,"reset");
	}

	public void onDetectSignal(String btName,int signalStrength){
		
			Time curTime=new Time();
			curTime.setToNow();
			
			handler.handle(ctx, new RobotEvent(RobotEvent.EVT_BT_SIGNAL_DETECTED,signalStrength,0,btName));
			Log.d(TAG,"signal detected name:"+btName+" strenght:"+signalStrength);
			int dir=0;
			
			if(signalStrength>-60)
			{
				dir=BTMotionDetector.PARAM_COMMING;
			}
			
			if(signalStrength<-80){
			}
			
			if(dir==BTMotionDetector.PARAM_COMMING && LogIn.getInstance().whosLogIn()==null)
			{

				handler.handle(ctx, new RobotEvent(RobotEvent.EVT_BT_MOTION_DETECTION,dir,signalStrength,null));
				Log.d(TAG,"motion detected dir:"+btName+" strength:"+signalStrength);
			}
			
			
			if(dir==BTMotionDetector.PARAM_COMMING)
			{
				LogIn.getInstance().checkInWithBT(curTime, btName);
			}
		
	}
	
	@Override
	public void onDetectMovement(String btName,int dir, int[] signalStrength) {
		
		
		Log.d(TAG, "dir:"+dir+" signal(0):"+signalStrength[0]+ " signal(1):"+signalStrength[1]+ " signal(2):"+signalStrength[2]);
		
		if(oldDir!=dir)
		{
			oldDir=dir;
			Time curTime=new Time();
			curTime.setToNow();
			
			
			//LogIn.getInstance().checkInWithBT(curTime, btName);
			
			Log.d(TAG,"fire motion detection event");
			handler.handle(ctx, new RobotEvent(RobotEvent.EVT_BT_MOTION_DETECTION,dir,0,null));
		}
		
	}
	
	
	

}
