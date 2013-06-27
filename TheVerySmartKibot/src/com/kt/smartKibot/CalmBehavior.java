package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

public class CalmBehavior extends RobotBehavior {

	private static final String TAG = "CalmBehavior";
	private volatile boolean isEnd = false;
	private volatile int targetHourlyBrief=-1;
	private volatile boolean isAlreadyDailyBrief=true;
	
	public CalmBehavior(ArrayList<RobotLog> logHistory) {
		super(logHistory);
	}
	
	@Override
	public void onStart(Context ctx) {
		super.onStart(ctx);
		
		isEnd=false;
		
		RobotActivity.setModeIndicatorColor(Color.GREEN,"얌전함");
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
		
	
		
		case RobotEvent.EVT_TIMER_HOURLY:
		{
			targetHourlyBrief=evt.getParam1();
			
			if(targetHourlyBrief==9)
				isAlreadyDailyBrief=false;
			
		}
		break;
			

		case RobotEvent.EVT_TIMER:
			
				if (evt.getParam1() == 2 ) {
					if(targetHourlyBrief!=-1)
					{
						if(targetHourlyBrief==18)
						{
							targetHourlyBrief=-1;
							changeState(new StateBye() );
							return;
							
						}
						
						if(targetHourlyBrief==9 && !isAlreadyDailyBrief)/*9시에 한번만*/
						{
							changeState(new StateScheduleBriefing(StateScheduleBriefing.DAILY));
							targetHourlyBrief=-1;
							isAlreadyDailyBrief=true;
							return;
						}
						else{ // 매시간 일정 briefing 않는다.
							//no briefing every hour on the calm mode
						//	changeState(new StateScheduleBriefing(StateScheduleBriefing.HOURLY,targetHourlyBrief+1));
						}
						
					}
				}
				
				
			if (StateGreeting.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 2 ) {
						changeState(new StateLookAround(evt,history_log));
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

			if (StateLookAround.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 4) {
					if(StateGreeting.class.isInstance(getLastState())) {
						changeState(new StateWandering(evt, history_log));
					}
					else {
						changeState(new StateSleeping());
					}
					return;
				}
			}

			if (StateTouchResponse.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 4) {
					changeState(new StateSleeping());
					return;
				}
			}

			if (StateSleeping.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == 60*60/5/*60min*/) {
					//clam mode는 wandering으로 가지 않는다.
						changeState(new StateLookAround(evt, history_log));
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
						
						//change StateLookAround when big noise
						changeState(new StateLookAround(evt, history_log));
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
