package com.kt.smartKibot;

import java.util.Iterator;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

public class TestBehavior extends RobotBehavior {

	private static final String TAG = "TestBehavior";
	private volatile boolean isEnd = false;
	static int targetHour=15;//오후3시 일정부터 차례 대로 
	
	@Override
	public void onStart(Context ctx) {
		super.onStart(ctx);
		
		isEnd=false;
		
		RobotActivity.setModeIndicatorColor(Color.MAGENTA,"테스트");
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
						changeState(new StateAttraction());
					return;
				}
			}
			
			if(StateAttraction.class.isInstance(getCurrentState())){
				if (evt.getParam1() == 2 ) {
					//int random=(int)(Math.random()*11)+9;
					
					if(targetHour==19){
						changeState(new StateScheduleBriefing(StateScheduleBriefing.DAILY));
						targetHour=0;
					}else{
						changeState(new StateScheduleBriefing(StateScheduleBriefing.HOURLY,targetHour++));
					}
					return;
				}
			}
			
			if(StateScheduleBriefing.class.isInstance(getCurrentState())){
				
				if (evt.getParam1() >= 4 && RobotSpeech.getInstance(ctx).isSpeaking()==false) {
					
				if(targetHour!=0)
				{
					if(targetHour==19){
						changeState(new StateScheduleBriefing(StateScheduleBriefing.DAILY));
						targetHour=0;
					}else{
						changeState(new StateScheduleBriefing(StateScheduleBriefing.HOURLY,targetHour++));
					}
					return;
				}
				else{
					
					if (evt.getParam1() >= 4 && RobotSpeech.getInstance(ctx).isSpeaking()==false) {
						targetHour=15;
						changeState(new StateBye());
						return;
					}
				}
				
				}
			}
			
			if(StateBye.class.isInstance(getCurrentState())){
				if (evt.getParam1() >= 4 && RobotSpeech.getInstance(ctx).isSpeaking()==false) {
					int random=(int)(Math.random()*2);
					if(random==0){
						changeState(new StateWandering(evt,history_log));
					}else
					{
						changeState(new StateWandering(StateWandering.MODE_ACTIVE,evt,history_log));
					}
					return;
				}
			}

			if (StateWandering.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2) {
					int random=(int)(Math.random()*3l);
					
					if(random==0){
						changeState(new StateEvasion(StateEvasion.CAUSE_BIG_NOISE));
					}
					else if(random==1){
						changeState(new StateEvasion(StateEvasion.CAUSE_TOUCH_TOO_MUCH));
					}
					else{
						changeState(new StateEvasion(StateEvasion.CAUSE_SLEEPY));
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
				if (evt.getParam1() == 2) 
				{
					int rand=(int) (Math.random()*(long)StateGreeting.CAUSE_GOOD_AFTERNOON)+StateGreeting.CAUSE_HELLO; 
					changeState(new StateGreeting(rand));
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
