package com.kt.smartKibot;

import java.util.Iterator;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

public class CalmBehavior extends RobotBehavior {

	private static final String TAG = "CalmBehavior";
	private volatile boolean isEnd = false;
	
	@Override
	public void onStart(Context ctx) {
		super.onStart(ctx);
		
		isEnd=false;
		
		RobotActivity.setModeIndicatorColor(Color.GREEN);
		RobotTimer.getInstance().start();
		changeState(new StateSleeping());
	
	}

	@Override
	public void handle(Context ctx, RobotEvent evt) {

		if (isEnd)
			return;

		Time _t = new Time();
		_t.setToNow();
		long currentTime = _t.toMillis(false);

		switch (evt.getType()) 
		{
		
			

		case RobotEvent.EVT_TIMER:
			if (StateGreeting.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2 ) {
					int random=(int)(Math.random()*11)+9;
					
					if(random==19){
						changeState(new StateScheduleBriefing(StateScheduleBriefing.DAILY));
					}else{
						changeState(new StateScheduleBriefing(StateScheduleBriefing.HOURLY,random));
					}
					
					return;
				}
			}
			
			if(StateScheduleBriefing.class.isInstance(getCurrentState())){
				if (evt.getParam1() >= 4 && RobotSpeech.getInstance(ctx).isSpeaking()==false) {
					changeState(new StateBye());
					return;
				}
			}
			
			if(StateBye.class.isInstance(getCurrentState())){
				if (evt.getParam1() >= 4 && RobotSpeech.getInstance(ctx).isSpeaking()==false) {
					changeState(new StateWandering(evt,history_log));
					return;
				}
			}

			if (StateWandering.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2) {
					int random=(int)(Math.random()*2l);
					
					if(random==0){
						changeState(new StateEvasion(StateEvasion.CAUSE_BIG_NOISE));
					}
					else{
						changeState(new StateEvasion(StateEvasion.CAUSE_TOUCH_TOO_MUCH));
					}
					
					return;
				}
			}

			if (StateEvasion.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2) {
					changeState(new StateSleeping());
					return;
				}
			}
			
			if (StateSleeping.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2) {
					
					int random=(int)(Math.random()*2l);
					
					if(random==0){
						RobotEvent event=new RobotEvent(RobotEvent.EVT_NOISE_DETECTION);
						changeState(new StateLookAround(event, history_log));
					}else
					{
						changeState(new StateLookAround(evt, history_log));
					}
					return;
				}
			}
			
			if (StateLookAround.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2) {
					
					int random=(int)(Math.random()*6l);
					//PARAM_HEAD_LONG_PRESSED~ 0~5
					changeState(new StateTouchResponse(random,history_log));
					return;
				}
			}
			
			if (StateTouchResponse.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2) {
					changeState(new StateGreeting());
					return;
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
		// TODO Auto-generated method stub
		super.changeState(state);

		Log.d(TAG, "changed:" + state);

		RobotTimer.getInstance().reset();
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
