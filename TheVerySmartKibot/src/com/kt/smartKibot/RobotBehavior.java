package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class RobotBehavior implements IRobotEvtHandler{


	protected ArrayList<IRobotState> history_state;
	protected ArrayList<RobotLog> history_log;
	
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
		StatePresenter presenter= new StatePresenter(state,this);
		
		if(history_state.size()==MAX_HISTORY_STATE) history_state.remove(0);
			
		IRobotState oldState=getCurrentState();
		
		if(oldState!=null)
			oldState.onChanged(getContext());
		
		history_state.add(state);
		presenter.start();
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
	
	public IRobotState getCurrentState()
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
	
	public RobotBehavior(ArrayList<RobotLog> logHistory){
		history_state=new ArrayList<IRobotState>(MAX_HISTORY_STATE);
		history_log=logHistory;
	}
	
}


/**
 * StatePresenter
 *
 * 
 * State의 onSart,doAction,cleanUp method를 순서대로 호출하는 역할 
 * 각 method 에서 UI관련 코드나 다른 activity 등으로 Intent를 보내거나 이벤트를 보내는 방식으로 동작하는 경우를 위해 
 * 각 method 내부에서 직접 다음 method를 호출하지 않고 다음 동작에 해당하는 메세지만 보내고 즉각 method를 종료하여 context changing 되도록 함
 */
class StatePresenter extends Thread{
	public Handler mHandler;
	public int state=0;
	static final int DO_PREPARE=0;
	static final int DO_ACTION=1;
	static final int DO_CLEAN_UP=2;
	
	IRobotState item;
	RobotBehavior behavior;
//	static final Lock lock= new ReentrantLock(); 
	 
	StatePresenter(IRobotState item,RobotBehavior behavior){
		setName("Robot Action Hanlder");
		this.item=item;
		this.behavior=behavior;	
	}
	
	public void run(){
		
//		lock.lock();  //make it class scope mutex.
		
		try{
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
		finally{
	//		lock.unlock();
		}
        
	}
	
}
