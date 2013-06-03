package com.kt.smartKibot;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class RobotBehavior implements IRobotEvtHandler{


	protected ArrayList<IRobotState> history_state;
	public static final int MAX_HISTORY_STATE=10;
	protected Context ctx=null;
	
	public void onStart(Context ctx){
		this.ctx=ctx;
		
	}
	
	abstract public void handle(Context ctx,RobotEvent evt);
	abstract public void onStop(Context ctx);
	
	public Context getContext(){
		return ctx;
	}
	
	abstract protected void onStateActionEnd(IRobotState state);
	
	protected void changeState(IRobotState state){
		StateHandler handler= new StateHandler(state,this);
		
		if(history_state.size()==MAX_HISTORY_STATE) history_state.remove(0);
			
		IRobotState oldState=getCurrentState();
		
		if(oldState!=null)
			oldState.onChanged(getContext());
		
		history_state.add(state);
		handler.start();
	}
	
	protected IRobotState getLastState()
	{
		if(history_state.size()<2){
			return null;
		}
		else{
			
			return history_state.get(history_state.size()-2); 
		}
	}
	
	protected IRobotState getCurrentState()
	{
		if(history_state.size()<1){
			return null;
		}
		else{
		
			return history_state.get(history_state.size()-1);
		}
	}
	
	
	public RobotBehavior(){
		history_state=new ArrayList<IRobotState>(MAX_HISTORY_STATE);
	}
	
}

class StateHandler extends Thread{
	public Handler mHandler;
	public int state=0;
	static final int DO_PREPARE=0;
	static final int DO_ACTION=1;
	static final int DO_CLEAN_UP=2;
	
	IRobotState item;
	RobotBehavior behavior;
	 
	StateHandler(IRobotState item,RobotBehavior behavior){
		setName("Robot Action Hanlder");
		this.item=item;
		this.behavior=behavior;	
	}
	
	public void run(){
		Looper.prepare();
		
		mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what){
                
	                case DO_PREPARE:
	                	item.onStart(behavior.getContext());
	                	sendEmptyMessage(DO_ACTION);
	                	break;
	                case DO_ACTION:
	                	item.doAction(behavior.getContext());
	                	sendEmptyMessage(DO_CLEAN_UP);
	                	break;
	                case DO_CLEAN_UP:
	                	item.cleanUp(behavior.getContext());
	                	Looper.myLooper().quit();
	                	
	                	behavior.onStateActionEnd(item);
	                	break;
                }
            }
        };
       
        mHandler.sendEmptyMessage(DO_PREPARE); 
        Looper.loop();
        
	}
	
}
