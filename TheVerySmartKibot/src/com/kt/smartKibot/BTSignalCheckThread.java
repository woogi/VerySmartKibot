package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.kt.smartKibot.BTManager.ScanResult;

public class BTSignalCheckThread extends Thread implements BTManager.ScanResultHandler{
	static final String TAG="BTSignalCheckTrhead";
	IMovementListener listener=null;
	String targetDevName=null;
	volatile BTManager btMan=null;
	Context ctx=null;
	static final int SIGNAL_UNKNOWN=-999;
	static final int MAX_HISTORY=15;
	ArrayList <Integer> historySignals;
	
	
	//volatile int[] lastSignals={SIGNAL_UNKNOWN,SIGNAL_UNKNOWN,SIGNAL_UNKNOWN};
	volatile int curIdxSignals=0;
	
	@Override
	public void run() {
		
		curIdxSignals=0;
		while(btMan!=null)
		{
			
			try{
				Thread.sleep(1000);
				
				if(btMan!=null){
				btMan.scanWithName(targetDevName);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
	}
	
	
	public void finish() {
		
		synchronized(this){
		btMan.finish();
		btMan=null;
		curIdxSignals=0;
		}
			

	}
	
	public BTSignalCheckThread(Context ctx,String devName,IMovementListener listener){
		this.listener=listener;
		this.ctx=ctx;
		this.targetDevName=devName;
		btMan=new BTManager();
		btMan.init(ctx, this);
		setName("BTSignalCheckThread");
		
		historySignals=new ArrayList<Integer>(MAX_HISTORY);
		
	}
	
	@Override
	public void onReceive(ArrayList<ScanResult> result) {
	
		Log.d(TAG,"onReceive cnt:"+result.size());
		
		if(result.size()>0)
		{
			ScanResult res=result.get(0);
			if(res!=null)
			{
				if(historySignals.size()==MAX_HISTORY)
					historySignals.remove(0);
				
				historySignals.add(res.getCur_rssi());
				
				listener.onDetectSignal(targetDevName,res.getCur_rssi());
			
//				if(historySignals.size()==1)
//				{
//					int[] lastSignals=new int[3];
//					lastSignals[2]=historySignals.get(historySignals.size()-1);
//					lastSignals[1]=SIGNAL_UNKNOWN;
//					lastSignals[0]=SIGNAL_UNKNOWN;
//					listener.onDetectMovement(targetDevName,-2,lastSignals);
//					return;
//					
//				}
//				
//				if(historySignals.size()==2)
//				{
//					int[] lastSignals=new int[3];
//					lastSignals[2]=historySignals.get(historySignals.size()-1);
//					lastSignals[1]=historySignals.get(historySignals.size()-1);
//					lastSignals[0]=SIGNAL_UNKNOWN;
//					listener.onDetectMovement(targetDevName,-1,lastSignals);
//					return;
//					
//				}
//				
//				if(historySignals.size()>=3){
//					int[] lastSignals=new int[3];
//					lastSignals[2]=historySignals.get(historySignals.size()-1);
//					lastSignals[1]=historySignals.get(historySignals.size()-2);
//					lastSignals[0]=historySignals.get(historySignals.size()-3);
//					
//					/*
//					 * dir 0: 알수 없음
//					 * dir 1: 다가옴 
//					 * dir 2: 멀어짐  
//					 */
//					
//					int dir=0;
//					
//					if( (lastSignals[0]>lastSignals[1] && Math.abs(lastSignals[0]-lastSignals[1]) >3
//							&& lastSignals[1]>lastSignals[2] && Math.abs(lastSignals[1]-lastSignals[2]) >3
//						)
//							|| lastSignals[1]>lastSignals[2] && Math.abs(lastSignals[1]-lastSignals[2]) >17
//							)
//					{
//						dir=2;
//					}
//					
//					if( (lastSignals[2]>lastSignals[1] && Math.abs(lastSignals[1]-lastSignals[2]) >3
//							&& lastSignals[1]>lastSignals[0] && Math.abs(lastSignals[0]-lastSignals[1]) >3)
//							
//							|| lastSignals[2]>lastSignals[1] && Math.abs(lastSignals[1]-lastSignals[2]) >17
//							)
//					{
//						dir=1;
//					}
//					
//					
//					listener.onDetectMovement(targetDevName,dir,lastSignals);
					//return;
	//			}
			
			}
			
		}// ~: end of if(result.size()>0)
//		else{ //scan time out
//			//fail
//			if(null==LogIn.getInstance().whosLogIn()){ // 더이상 logIn 상태가 아니다.
//				BTMotionDetector.getInstance(ctx).reset(); 
//			}
//		}
		
	}
	
	public interface IMovementListener {
		void onDetectMovement(String btName,int dir,int[] signalStrength);
		void onDetectSignal(String btName,int signalStrength);
		
	}
	

}
