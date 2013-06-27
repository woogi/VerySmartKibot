package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

public class ActiveBehavior extends RobotBehavior {

	private static final String TAG = "ActiveBehavior";
	private volatile boolean isEnd = false;
	private volatile int targetHourlyBrief=-1;
	private volatile boolean isAlreadyDailyBrief=true;
	private volatile boolean isAlreadyGoodMorning=true;
	private volatile boolean isAlreadyGoodLunch=true;
	private volatile boolean isAlreadyGoodAfternoon=true;

	public ActiveBehavior(ArrayList<RobotLog> logHistory) {
		super(logHistory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kt.smartKibot.RobotBehavior#onStart(android.content.Context)
	 */
	@Override
	public void onStart(Context ctx) {
		super.onStart(ctx);
		isEnd=false;
		targetHourlyBrief=-1;
		isAlreadyDailyBrief=false;
		RobotActivity.setModeIndicatorColor(Color.RED,"활발함");
		RobotTimer.getInstance().start();
		changeState(new StateGreeting());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kt.smartKibot.RobotBehavior#handle(android.content.Context,
	 * com.kt.smartKibot.RobotEvent)
	 */
	@Override
	public void handle(Context ctx, RobotEvent evt) {
		

		if (isEnd)
			return;

		Time _t = new Time();
		_t.setToNow();
		long currentTime = _t.toMillis(false);

		switch (evt.getType()) 
		{
		
		case RobotEvent.EVT_TIMER_HOURLY:
		{
			targetHourlyBrief=evt.getParam1();
			
			//reset
			if(targetHourlyBrief==8)
				isAlreadyGoodMorning=false;
			
			if(targetHourlyBrief==9)
				isAlreadyDailyBrief=false;
			
			if(targetHourlyBrief==11)
				isAlreadyGoodLunch=false;
			
			if(targetHourlyBrief==13)
				isAlreadyGoodAfternoon=false;
			
		}
		break;
	
		
			

		case RobotEvent.EVT_TIMER:
			
			if (evt.getParam1() == 2 ) {
				if(targetHourlyBrief!=-1)
				{
					if(targetHourlyBrief==18)
					{
						changeState(new StateBye() );
						targetHourlyBrief=-1;
						return;
						
					}
					
					if(targetHourlyBrief==9 && !isAlreadyDailyBrief)/* 9시에 한번만*/
					{
						changeState(new StateScheduleBriefing(StateScheduleBriefing.DAILY));
						targetHourlyBrief=-1;
						isAlreadyDailyBrief=true;
						return;
					}
					else{
						changeState(new StateScheduleBriefing(StateScheduleBriefing.HOURLY,targetHourlyBrief+1));
						targetHourlyBrief=-1;
						return;
					}
				}
			}
			
			//todo	
			if (StateGreeting.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2 ) {
					StateGreeting state=(StateGreeting)getCurrentState();
				
					if(state.getCause()==StateGreeting.CAUSE_HELLO){
						changeState(new StateLookAround(evt,history_log));
					}else{
						changeState(new StateSleeping());
					}
					return;
				}
			}
		
			
			//need to change it (change to the sleeping state after just finishing this state) 
			if(StateScheduleBriefing.class.isInstance(getCurrentState())){
				if (evt.getParam1() >= 4 && RobotSpeech.getInstance(ctx).isSpeaking()==false) {
					changeState(new StateSleeping());
					return;
				}
			}
			
			if(StateBye.class.isInstance(getCurrentState())){
				if (evt.getParam1() >= 4 && RobotSpeech.getInstance(ctx).isSpeaking()==false) {
					changeState(new StateSleeping());
					return;
				}
			}

			if (StateWandering.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 4) {
					changeState(new StateSleeping());
					return;
				}
			}
			
			if (StateAttraction.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 3) {
					changeState(new StateSleeping());
					return;
				}
			}

			if (StateEvasion.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2) {
					changeState(new StateSleeping());
					return;
				}
			}

			//todo
			if (StateLookAround.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 4) {
					//최초 시작시 한번
					if(StateGreeting.class.isInstance(getLastState())) {
						
						StateGreeting state=(StateGreeting)getCurrentState();
						
						if(state.getCause()==StateGreeting.CAUSE_HELLO){
							changeState(new StateWandering(evt, history_log));
						return;
						}
					}
						
					Calendar c = Calendar.getInstance();
					int hour=c.get(Calendar.HOUR_OF_DAY);
					int minute=c.get(Calendar.MINUTE);
	
					if((hour==8 || hour==9) && !isAlreadyGoodMorning)
					{
						changeState(new StateGreeting(StateGreeting.CAUSE_GOOD_MORNING));
						isAlreadyGoodMorning=true;
						return;
						
					}
					
					if(hour==11 && minute>20  && !isAlreadyGoodLunch)
					{
						changeState(new StateGreeting(StateGreeting.CAUSE_GOOD_LUNCH));
						isAlreadyGoodLunch=true;
						return;
					}
					
					if(hour==13 && minute>20  && !isAlreadyGoodAfternoon)
					{
						changeState(new StateGreeting(StateGreeting.CAUSE_GOOD_AFTERNOON));
						isAlreadyGoodAfternoon=true;
						return;
						
					}
					
					changeState(new StateSleeping());
					return;
							
				}
				
			}//:end of is state lookAround


			if (StateTouchResponse.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 4) {
					changeState(new StateSleeping());
					return;
				}
			}

			if (StateSleeping.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 3*60/5/*3min*/) {
					
					int random=(int)(Math.random()*2l);
					
					if(random==0){
						changeState(new StateLookAround(evt, history_log));
					}
					
					if(random==1){
						changeState(new StateWandering(evt, history_log));
					}
					
				}
				return;
			}

		break;

		case RobotEvent.EVT_NOISE_DETECTION:

			if (StateSleeping.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == NoiseDetector.PARAM_SMALL_NOISE) {
					changeState(new StateLookAround(evt, history_log));
					return;
				}

				if (evt.getParam1() == NoiseDetector.PARAM_BIG_NOISE) {
					
					// check latest 3 StateSleeping state has received big noise
					int cntBigNoise = 0;
					int cntSleepingState = 1; // 현재 상황이 sleepingState이니 기본 cnt는 1
					RobotLog prevLog = null;
					
					ListIterator<RobotLog> it = history_log
							.listIterator(history_log.size());
					
					while (it.hasPrevious()) {
						RobotLog log = it.previous();
						
						if (log.getEvent().getTimeStamp().toMillis(false) + 1000 * 60 * 10 < currentTime) 
							break; //10분이내 내역만  
						
						if (StateSleeping.class.isInstance(log.getState())) {
							if (prevLog != null
									&& !StateSleeping.class.isInstance(prevLog.getState())) {
								// 이전상태가 sleeping이 아닐때만... log는 event 단위로
								// 저장하기때문에 상태변했을때만 count
								if (++cntSleepingState > 3)
									break;
								
								Log.d(TAG, "count of sleeping state:"
										+ cntSleepingState);
							}
							
							if (log.getEvent().getType() == RobotEvent.EVT_NOISE_DETECTION
									&& log.getEvent().getParam1() == NoiseDetector.PARAM_BIG_NOISE) {
								++cntBigNoise;
								Log.d(TAG, "count of big noise" + cntBigNoise);
							}
							
						}
						
						prevLog = log;
					}
					
					if (cntBigNoise >= 3) {
						changeState(new StateEvasion(
								StateEvasion.CAUSE_BIG_NOISE));
					} else {
						changeState(new StateWandering(evt, history_log));
					}
					
					return;
				}
			}
			break;


		case RobotEvent.EVT_TOUCH_BODY: {

			int _cntTouch = 1;

			{ //todo refactoring it..later
				RobotEvent lastTouchEvt =null;
					
				ListIterator<RobotLog> it = history_log.listIterator(history_log.size()-1);//not from this event sent just before
				while (it.hasPrevious()) {
					RobotLog temp=it.previous();
					if(temp.getEvent().getType()==RobotEvent.EVT_TOUCH_BODY)
					{
						lastTouchEvt=temp.getEvent();
						break;
					}
				}
				
				if(lastTouchEvt!=null)
				{
					Log.d(TAG,"last Log timeStamp:"+lastTouchEvt.getTimeStamp().toMillis(false)+" current:"+currentTime);
					if(lastTouchEvt.getTimeStamp().toMillis(false) +1000*1 >currentTime) return; /* 1초 안에 같은 event */
				}
			}
			
			ListIterator<RobotLog> it = history_log.listIterator(history_log.size());
			while (it.hasPrevious()) {
				RobotLog log = it.previous();
				if (log.getEvent().getTimeStamp().toMillis(false) + 1000 * 60 * 1 < currentTime) // 1분 이내 내역만
					break;
				if (log.getEvent().getType() == RobotEvent.EVT_TOUCH_BODY)
					++_cntTouch;
			}

			Log.d(TAG, "total count of body touch:" + _cntTouch + "in 1 min.");

			if (_cntTouch < 5) {
				changeState(new StateTouchResponse(evt.getParam1(), history_log));
				
			} else {
				changeState(new StateEvasion(StateEvasion.CAUSE_TOUCH_TOO_MUCH));
			}
		}
			break;

		case RobotEvent.EVT_FACE_DETECTION: {
			Log.d(TAG, "Face Detection Event");

			changeState(new StateAttraction());

		}
		break;

		}// end of switch
	}// end of handle

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kt.smartKibot.RobotBehavior#onStop(android.content.Context)
	 */
	@Override
	public void onStop(Context ctx) {
		// TODO Auto-generated method stub

		isEnd = true;

		RobotTimer.getInstance().stop();

		Iterator<IRobotState> it = history_state.iterator();

		while (it.hasNext()) {
			it.next().onChanged(ctx);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kt.smartKibot.RobotBehavior#onEndState(com.kt.smartKibot.IRobotState)
	 */
	@Override
	protected void onStateActionEnd(IRobotState state) {
		// TODO Auto-generated method stub

		Log.d(TAG, "state end:" + state);

		if (isEnd)
			return;

		if (StateTouchResponse.class.isInstance(state)
				|| StateEvasion.class.isInstance(state)) {
			/*
			 * IRobotState lastState=null;
			 * 
			 * ListIterator<RobotLog>
			 * it=history_log.listIterator(history_log.size());
			 * while(it.hasPrevious()) { RobotLog log=it.previous();
			 * if(!StateTouchResponse.class.isInstance(log.getState()) &&
			 * !StateEvasion.class.isInstance(log.getState())) {
			 * lastState=log.getState(); break; }
			 * 
			 * }
			 * 
			 * if(lastState!=null){ changeState(lastState); }
			 */

			//changeState(new StateSleeping());
			return;
		}

	}


}
