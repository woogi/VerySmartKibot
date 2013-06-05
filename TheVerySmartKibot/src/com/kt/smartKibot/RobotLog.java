package com.kt.smartKibot;

public class RobotLog {
	RobotBehavior behavior;
	RobotEvent event;
	IRobotState state;
	
	public RobotLog(RobotBehavior behavior, RobotEvent event, IRobotState state) {
		super();
		this.behavior = behavior;
		this.event = event;
		this.state = state;
	}

	public RobotBehavior getBehavior() {
		return behavior;
	}

	public void setBehavior(RobotBehavior behavior) {
		this.behavior = behavior;
	}

	public RobotLog() {
		super();
		this.behavior = null;
		this.event = null;
		this.state = null;
	}

	public RobotEvent getEvent() {
		return event;
	}

	public void setEvent(RobotEvent event) {
		this.event = event;
	}

	public IRobotState getState() {
		return state;
	}

	public void setState(IRobotState state) {
		this.state = state;
	}

	
	

}
