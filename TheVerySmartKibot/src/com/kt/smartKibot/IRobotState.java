package com.kt.smartKibot;

import android.content.Context;

public interface IRobotState {
	
	//prepare state
	public void onStart(Context ctx);
	
	//do something during state
	public void doAction(Context ctx);
	
	//end of state
	public void cleanUp(Context ctx);
	
	//when called being changed to the other State
	public void onChanged(Context ctx);

}
