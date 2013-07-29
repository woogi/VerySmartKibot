package com.kt.smartKibot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Color;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.text.format.Time;
import android.util.Log;

public class DayTimeBehavior extends RobotBehavior {

	private static final String TAG = "DayTimeBehavior";
	private volatile boolean isEnd = false;
	private volatile int targetHourlyBrief=-1;
	private volatile boolean isAlreadyDailyBrief=true;
	private volatile boolean isAlreadyGoodMorning=true;
	private volatile boolean isAlreadyGoodLunch=true;
	private volatile boolean isAlreadyGoodAfternoon=true;
	private volatile boolean chattingHasAnswer=false;

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
		RobotActivity.setModeIndicatorColor(Color.YELLOW,"주간");
		RobotTimer.getInstance().start();
		
		chattingHasAnswer=false;
		
		changeState(new StateSleeping());
		//changeState(new StateLookAround());
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
		
//		
//		//////****** :1 test code for look around state
//		
//		case RobotEvent.EVT_TIMER:
//		{
//			if (StateSleeping.class.isInstance(getCurrentState()))
//			{
//				if(evt.getParam1()==2){
//					changeState(new StateLookAround(evt,history_log));
//				}
//				
//			}
//			
//			if (StateAttraction.class.isInstance(getCurrentState()))
//			{
//				if(evt.getParam1()==2){
//					changeState(new StateLookAround(evt,history_log));
//				}
//				
//			}
//			
//			//else
//			if(evt.getParam1()==8){
//				changeState(new StateSleeping());
//			}
//			
//		
//			
//		}
//		break;
////		
////		case RobotEvent.EVT_NOISE_DETECTION:
////		{
////
////			if (StateSleeping.class.isInstance(getCurrentState())) 
////			{
////				changeState(new StateLookAround(evt, history_log));
////			}
////		}
////		break;
//		
//		
//		case RobotEvent.EVT_FACE_DETECTION:
//		{
//			Log.d(TAG,"param1:"+evt.getParam1()+" param2:"+evt.getParam2());
//		
//			User user=new User(null,null);
//			user.id=evt.getParam1();
//			changeState(new StateAttraction(user));
//		}
//		break;
//		
//		//////****** 1: ~test code for look around state
		
		//////****** 2: test code for Listening state
		
			
//		case RobotEvent.EVT_TIMER:
//			{
//				
//				
//				if (StateChattingResponse.class.isInstance(getCurrentState()) && !RobotSpeech.getInstance(ctx).isSpeaking()) {
//					if (evt.getParam1() >2 ) {
//						changeState(new StateListening());
//						return;
//					}
//				}
//				
//				if(evt.getParam1()==6){
//				if (!StateSleeping.class.isInstance(getCurrentState())) {
//					
//					changeState(new StateSleeping());
//					return;
//				}
//				
//				}
//				
//			
//			}
//		break;
//			
//		case RobotEvent.EVT_NOISE_DETECTION:
//		{
//			if (StateSleeping.class.isInstance(getCurrentState())) 
//			{
//				if(evt.getParam2()==NoiseDetector.PARAM_KIND_HUMAN_VOICE)
//				{
//					changeState(new StateListening());
//				}
//				else
//				{
//				//	changeState(new StateLookAround(evt, history_log));
//				}
//			}
//		}
//		break;
//		
//		case RobotEvent.EVT_ROBOT_CHATTING_ASK:
//		{
//			if(StateListening.class.isInstance(getCurrentState()))
//			{
//				
//				if(evt.getParam1()!=0 && evt.getExtParam()!=null){
//					
//					changeState(new StateChattingResponse(evt.getExtParam()));
//				}
//				else{
//					changeState(new StateChattingResponse(null));
//				}
//			}
//		}
//		break;
		
		//////****** 2: ~test code for Listening state
		
		
		case RobotEvent.EVT_ROBOT_CHATTING_ASK:
		{
			Log.d(TAG,"EVT_ROBOT_CHATTING_ASK param1:"+evt.getParam1()+" ext:"+evt.getExtParam());
		
			
			
			String whatSay=evt.getExtParam();
			
			
			
			if(StateListening.class.isInstance(getCurrentState()))
			{
				if(evt.getParam1()==1 && whatSay!=null)
					{
					
					if(whatSay.equals("없어") ||whatSay.equals("그만하자") || whatSay.equals("없소")
							||whatSay.equals("옥소") 
							||whatSay.equals("업소") 
					||whatSay.equals("쉿"))
					{
						
					changeState(new StateSleeping(StateSleeping.CAUSE_ORDER) );
					}else{
						if(evt.getExtParam()==null){
							chattingHasAnswer=false;
						}
						else{
							chattingHasAnswer=true;
						}
							
						changeState(new StateChattingResponse(evt.getExtParam()) );
					}
					
				}
				else
				{
					changeState(new StateChattingResponse(null) );
				}
			}
			
		}
		break;
		
		case RobotEvent.EVT_BT_MOTION_DETECTION:
		{
			if(evt.getParam1()==BTMotionDetector.PARAM_GOAWAY)
			{
				changeState(new StateGreeting(StateGreeting.CAUSE_USER_GOAWAY));
			}
			
			if(evt.getParam1()==BTMotionDetector.PARAM_COMMING)
			{
				changeState(new StateGreeting(StateGreeting.CAUSE_USER_COMMING));
			}
			
		}break;
		
	
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
			

		//todo 
		case RobotEvent.EVT_TIMER:
		{
			
			
				if (evt.getParam1() == 3 ) {
					if(targetHourlyBrief!=-1)
					{
						if(targetHourlyBrief==18)
						{
							if(LogIn.getInstance().whosLogIn()!=null)
							{
								changeState(new StateBye() );
								targetHourlyBrief=-1;
							return;
							}
						}
						
						if(targetHourlyBrief==9 && !isAlreadyDailyBrief)/* 9시에 한번만*/
						{
							if(LogIn.getInstance().whosLogIn()!=null)
							{
								changeState(new StateScheduleBriefing(StateScheduleBriefing.DAILY));
								targetHourlyBrief=-1;
								isAlreadyDailyBrief=true;
							return;
							}
						}
						else
						{
							if(LogIn.getInstance().whosLogIn()!=null)
							{
								changeState(new StateScheduleBriefing(StateScheduleBriefing.HOURLY,targetHourlyBrief+1));
								targetHourlyBrief=-1;
							return;
							}
						}
					}
				}
				
				if (StateListening.class.isInstance(getCurrentState())) {
					if (evt.getParam1()>3 ) {
						
						ListIterator<RobotLog> it = history_log.listIterator(history_log.size()-1);
						RobotEvent event=it.next().getEvent();
						int cnt=0;
					
						while (it.hasPrevious()) {
							RobotLog temp=it.previous();
							if(temp.getEvent().getType()!=RobotEvent.EVT_TIMER)
							{								event=temp.getEvent();
								cnt++;
								break;
							}
						}
						
						Log.d(TAG,"last event type:"+event.getType() +"cnt:"+cnt);
						
						if(event.getType()==RobotEvent.EVT_NOISE_DETECTION)
						{
							if(NoiseDetector.PARAM_BIG_NOISE==evt.getParam1())
							{
								changeState(new StateWandering(evt,history_log));
								return;
							}else{
								changeState(new StateLookAround(evt,history_log));
								return;
							}
						}else
						{
							changeState(new StateSleeping()); //there is no speaking at all for 15sec then make kibot sleep
						return;
						}
					}
				}
				
				if (StateChattingResponse.class.isInstance(getCurrentState()) && !RobotSpeech.getInstance(ctx).isSpeaking()) {
					if (evt.getParam1() >2) {
						
						if(chattingHasAnswer==true){
							changeState(new StateListening()); //end of talking then listen again.
							return;
						}else{
							changeState(new StateSleeping()); 
							return;
						}
					}
				}
				
				
				if (StateGreeting.class.isInstance(getCurrentState())) {
					if (evt.getParam1() >=2 ) {
						//StateGreeting state=(StateGreeting)getCurrentState();
					
//						if(state.getCause()==StateGreeting.CAUSE_HELLO){
//							changeState(new StateLookAround(evt,history_log));
//						}else{
//							changeState(new StateSleeping());
//						}
							changeState(new StateSleeping());
						return;
					}
				}
			
				
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
						changeState(new StateSleeping(StateSleeping.CAUSE_NOBODY_FOUND));
						return;
					}
				}
				
				if (StateAttraction.class.isInstance(getCurrentState()) && !RobotSpeech.getInstance(ctx).isSpeaking()) {
					if (evt.getParam1()  >=4) {
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
					if (evt.getParam1()>5) {
//						//최초 시작시 한번
//						if(StateGreeting.class.isInstance(getLastState())) {
//							
//							StateGreeting state=(StateGreeting)getLastState();
//							
//							if(state.getCause()==StateGreeting.CAUSE_HELLO){
//								changeState(new StateWandering(evt, history_log));
//							return;
//							}
//						}
							
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
						
						changeState(new StateSleeping(StateSleeping.CAUSE_NOBODY_FOUND));
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
					if (evt.getParam1() == 30*60/5/*30min*/) {
						
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
			
		}//:end of case TIMER event
 		break;

		case RobotEvent.EVT_NOISE_DETECTION:

			if (StateSleeping.class.isInstance(getCurrentState())) {
				
//				if(evt.getParam2()==NoiseDetector.PARAM_KIND_NOT_FROM_HUMAN)
//				{
//						
//					if (evt.getParam1() == NoiseDetector.PARAM_SMALL_NOISE) {
//						changeState(new StateLookAround(evt, history_log));
//						return;
//					}
//	
//					if (evt.getParam1() == NoiseDetector.PARAM_BIG_NOISE) {
//						
//						// check latest 3 StateSleeping state has received big noise
//						int cntBigNoise = 0;
//						int cntSleepingState = 1; // 현재가 sleepingState이니 기본 cnt는 1
//						RobotLog prevLog = null;
//						
//						ListIterator<RobotLog> it = history_log
//								.listIterator(history_log.size());
//						
//						while (it.hasPrevious()) {
//							RobotLog log = it.previous();
//							
//							if (log.getEvent().getTimeStamp().toMillis(false) + 1000 * 60 * 10 < currentTime) 
//								break; //10분이내 내역만  
//							
//							if (StateSleeping.class.isInstance(log.getState())) {
//								if (prevLog != null
//										&& !StateSleeping.class.isInstance(prevLog.getState())) {
//									// 이전상태가 sleeping이 아닐때만... log는 event 단위로
//									// 저장하기때문에 상태변했을때만 count
//									if (++cntSleepingState > 3)
//										break;
//									
//									Log.d(TAG, "count of sleeping state:"
//											+ cntSleepingState);
//								}
//								
//								if (log.getEvent().getType() == RobotEvent.EVT_NOISE_DETECTION
//										&& log.getEvent().getParam1() == NoiseDetector.PARAM_BIG_NOISE) {
//									++cntBigNoise;
//									Log.d(TAG, "count of big noise" + cntBigNoise);
//								}
//								
//							}
//							
//							prevLog = log;
//						}
//						
//						if (cntBigNoise >= 3) {
//							changeState(new StateEvasion(
//									StateEvasion.CAUSE_BIG_NOISE));
//						} else {
//							changeState(new StateWandering(evt, history_log));
//						}
//						
//						return;
//					}
//				}//: end of not from human
//				else{
					changeState(new StateListening());
				//}
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
					{						lastTouchEvt=temp.getEvent();
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
			
			//if(StateLookAround.class.isInstance(getCurrentState()) && StateWandering.class.isInstance(getCurrentState()))
			if(evt.getParam1()>0){//recoginize somebody
				User user=LogIn.getInstance().getUserById(evt.getParam1());	
				changeState(new StateAttraction(user));
				
			}else{// just detect somebody
				changeState(new StateAttraction());
			}


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
