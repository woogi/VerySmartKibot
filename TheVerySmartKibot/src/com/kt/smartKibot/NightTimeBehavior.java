package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

public class NightTimeBehavior extends RobotBehavior {

	private static final String TAG = "NightTimeBehavior";
	private volatile boolean isEnd = false;
	
	public NightTimeBehavior(ArrayList<RobotLog> logHistory) {
		super(logHistory);
	}
	
	@Override
	public void onStart(Context ctx) {
		super.onStart(ctx);
		
		isEnd=false;
		
		RobotActivity.setModeIndicatorColor(Color.GRAY);
		changeState(new StateSleeping());
		RobotTimer.getInstance().start();
	
	}

	@Override
	public void handle(Context ctx, RobotEvent evt) {

		Time _t = new Time();
		_t.setToNow();
		long currentTime = _t.toMillis(false);
		
		if (isEnd)
			return;
		
		switch (evt.getType()) {
		
		case RobotEvent.EVT_TOUCH_BODY: {
			
		
			RobotLog lastLog =null;
			
			if(history_log.size()>1){
				lastLog=history_log.get(history_log.size()-2);
				Log.d(TAG,"last Log timeStamp:"+lastLog.getEvent().getTimeStamp().toMillis(false)+" current:"+currentTime);
				if(lastLog.getEvent().getTimeStamp().toMillis(false) +1000*2 >currentTime) return; /* 2초 안에 같은 event */
			}
			 
			changeState(new StateEvasion(StateEvasion.CAUSE_SLEEPY));
			
			}
		break;
		
		case RobotEvent.EVT_TIMER:
		{
			if (StateEvasion.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 1) {
					changeState(new StateSleeping());
					return;
				}
			}
			
		}
		break;
			

	
			
		}// end of switch
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kt.smartKibot.RobotBehavior#changeState(com.kt.smartKibot.IRobotState
	 * )
	 */
	@Override
	protected void changeState(IRobotState state) {
		RobotTimer.getInstance().reset();
		super.changeState(state);

		Log.d(TAG, "changed:" + state);

	}

	@Override
	public void onStop(Context ctx) {

		isEnd = true;

		RobotTimer.getInstance().stop();

		Iterator<IRobotState> it = history_state.iterator();

		while (it.hasNext()) {
			it.next().onChanged(ctx);
		}
	}


	@Override
	protected void onStateActionEnd(IRobotState state) {
		// TODO Auto-generated method stub

	}

}
