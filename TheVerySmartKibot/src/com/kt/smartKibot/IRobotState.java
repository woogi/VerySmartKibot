package com.kt.smartKibot;

import android.content.Context;

public interface IRobotState {
	public void onStart(Context ctx);
	public void doAction(Context ctx);
	public void cleanUp(Context ctx);
	public void onChanged(Context ctx);

}
