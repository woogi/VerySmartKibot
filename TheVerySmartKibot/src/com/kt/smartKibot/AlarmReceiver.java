package com.kt.smartKibot;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AlarmReceiver extends BroadcastReceiver implements IRobotEvtDelegator{

	private static final String TAG="AlarmReceiver";
	
	static IRobotEvtHandler evtHandler;

	private void doAction(Context ctx,Intent it){
		
		Iterator<String> _it=it.getCategories().iterator();
		String category="";
		
		
		assert _it.hasNext():"category should not be null";
		
		if(_it.hasNext())
		{
			category=_it.next();
		}
		
		RobotEvent evt=null;
		
		if(category.compareTo("SCHEDULE")==0)
		{
			evt=new RobotEvent(RobotEvent.EVT_ALARM_SCHEDULE_BREIF);
		}
		
		if(category.compareTo("GOODBYE")==0)
		{
			evt=new RobotEvent(RobotEvent.EVT_ALARM_GOOD_BYE);
		}
		
		if(category.compareTo("GOODMORNING")==0)
		{
			evt=new RobotEvent(RobotEvent.EVT_ALARM_GOOD_MORNING);
		}
		
		
		if(evt!=null){
			evtHandler.handle(ctx,evt);
		}
		else{
			throw new RuntimeException("unexpected alarm Item!");
		}
		
		
	};
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

		
		doAction(arg0,arg1);
		
		
	//	Log.d(TAG, "intent type:"+type+" category:" +category);
		
	}
	
	

	@Override
	public void installHandler(IRobotEvtHandler handler) {
		// TODO Auto-generated method stub
		evtHandler=handler;
		
	}

	/* (non-Javadoc)
	 * @see com.kt.smartKibot.IRobotEvtDelegator#uninstallHandler()
	 */
	@Override
	public void uninstallHandler() {
		// TODO Auto-generated method stub
		
	}
	
	

}
