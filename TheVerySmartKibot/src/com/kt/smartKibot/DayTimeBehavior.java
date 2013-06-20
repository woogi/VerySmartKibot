package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

public class DayTimeBehavior extends RobotBehavior {

	private static final String TAG = "DayTimeBehavior";
	private volatile boolean isEnd = false;
	private volatile int targetHourlyBrief=-1;
	private volatile boolean isAlreadyDailyBrief=false;

	public DayTimeBehavior(ArrayList<RobotLog> logHistory) {
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
		RobotActivity.setModeIndicatorColor(Color.YELLOW);
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
		
		case RobotEvent.EVT_TOUCH_SCREEN: 
		{
			changeState(new StateSleeping());
		}
		break;
		
		case RobotEvent.EVT_TIMER_HOURLY:
		{
			targetHourlyBrief=evt.getParam1();
			
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
							isAlreadyDailyBrief=true;
							return;
							
						}
						
						if((targetHourlyBrief==8) || targetHourlyBrief==9 && isAlreadyDailyBrief==false)/* 8시 혹은 9시에 한번만*/
						{
							changeState(new StateScheduleBriefing(StateScheduleBriefing.DAILY));
							targetHourlyBrief=-1;
							isAlreadyDailyBrief=true;
							return;
						}
						else{
							changeState(new StateScheduleBriefing(StateScheduleBriefing.HOURLY,targetHourlyBrief+1));
							targetHourlyBrief=-1;
							isAlreadyDailyBrief=false; //여기선 무조건 dailyBrief상태를 false로 바꿀수있다.(8시나 ,9시이나 이미 dailyBrief한경우나 8,9시 이외의 시간)
							return;
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
				if (evt.getParam1() == 30*60/5/*30min*/) {
					
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
			
			if (StateLookAround.class.isInstance(getCurrentState())) {
				if (evt.getParam1() == NoiseDetector.PARAM_BIG_NOISE) {
					changeState(new StateWandering(evt, history_log));
				}
				return;
			}
			
			break;


		case RobotEvent.EVT_TOUCH_BODY: {

			int _cntTouch = 1;

			RobotLog lastLog =null;
			if(history_log.size()>1){
				lastLog=history_log.get(history_log.size()-2);
				Log.d(TAG,"last Log timeStamp:"+lastLog.getEvent().getTimeStamp().toMillis(false)+" current:"+currentTime);
				if(lastLog.getEvent().getTimeStamp().toMillis(false) +1000*2 >currentTime) return; /* 2초 안에 같은 event */
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
