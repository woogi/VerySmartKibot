package com.kt.smartKibot;

import android.content.Context;

public interface IRobotEvtDelegator {
	
	void installHandler(IRobotEvtHandler handler);
	void uninstallHandler();
	void start();
	void stop();
	
}

interface IRobotEvtHandler{
	void handle(Context ctx,RobotEvent evt);
}
