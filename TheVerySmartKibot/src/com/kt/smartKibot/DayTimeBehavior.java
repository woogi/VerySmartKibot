package com.kt.smartKibot;

import android.content.Context;
import android.util.Log;

public class DayTimeBehavior extends RobotBehavior{

	static final String TAG="DayTimeBehavior";
	NoiseDetector noiseDetector=null;
	protected RobotTimer robotTimer=null;
	
	/* (non-Javadoc)
	 * @see com.kt.smartKibot.RobotBehavior#onStart(android.content.Context)
	 */
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		super.onStart(ctx);
		RobotTimer.getInstance().start();
		changeState(new StateGreeting());
	}

	/* (non-Javadoc)
	 * @see com.kt.smartKibot.RobotBehavior#handle(android.content.Context, com.kt.smartKibot.RobotEvent)
	 */
	@Override
	public void handle(Context ctx, RobotEvent evt) {
		// TODO Auto-generated method stub
		
		switch(evt.getType())
		{
			case RobotEvent.EVT_TIMER:
				if(StateGreeting.class.isInstance(getCurrentState()))
				{
					if(evt.getParam1()==1) //elapsed 30 sec..
					{
						changeState(new StateSleeping()); //test
						return;
					}
				}
				
				if(StateWandering.class.isInstance(getCurrentState()))
				{
					if(evt.getParam1()==6/*test*/) //elapsed 60 sec..
					{
						changeState(new StateSleeping());
						return;
					}
				}
				
				if(StateLookAround.class.isInstance(getCurrentState()))
				{
					if(evt.getParam1()==6/*test*/) //elapsed 60 sec..
					{
						changeState(new StateSleeping());
						return;
					}
				}
				
				if(StateSleeping.class.isInstance(getCurrentState()))
				{
					if(evt.getParam1()==3/*test*/) //elapsed 60 sec..
					{
						int rand=(int)(Math.random()*2);
						if(rand==0){
							changeState(new StateWandering());
						}
						else{
							changeState(new StateLookAround());
						}
					}
						return;
				}
				
			break;
			
			case RobotEvent.EVT_NOISE_DETECTION:
				
				if(StateSleeping.class.isInstance(getCurrentState()))
				{
					if(evt.getParam1()==NoiseDetector.PARAM_SMALL_NOISE) 
					{
						changeState(new StateLookAround());
						return;
					}
				
				
					if(evt.getParam1()==NoiseDetector.PARAM_BIG_NOISE) 
					{
						changeState(new StateWandering());
						return;
					}
				}
		    break;
			
			case RobotEvent.EVT_BATTERY_STATE:
				switch(evt.getParam1())
				{
					case BatteryChecker.PARAM_POWER_CONNECTED:
						changeState(new StateCharging());
						break;
						
					case BatteryChecker.PARAM_POWER_DISCONNECTED:
						if(StateCharging.class.isInstance(getCurrentState()))
						{
							changeState(new StateWandering());
						}
						break;
				
				}
			break;
		}//end of switch
	}//end of handle
		

	/* (non-Javadoc)
	 * @see com.kt.smartKibot.RobotBehavior#changeState(com.kt.smartKibot.IRobotState)
	 */
	@Override
	protected void changeState(IRobotState state) {
		// TODO Auto-generated method stub
		super.changeState(state);
		
		Log.d(TAG,"changed:"+state);
		
		RobotTimer.getInstance().reset();
	}

	/* (non-Javadoc)
	 * @see com.kt.smartKibot.RobotBehavior#onStop(android.content.Context)
	 */
	@Override
	public void onStop(Context ctx) {
		// TODO Auto-generated method stub
		RobotTimer.getInstance().stop();
	}

	/* (non-Javadoc)
	 * @see com.kt.smartKibot.RobotBehavior#onEndState(com.kt.smartKibot.IRobotState)
	 */
	@Override
	protected void onStateActionEnd(IRobotState state) {
		// TODO Auto-generated method stub
		
	}

}
