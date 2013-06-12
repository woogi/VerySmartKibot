package com.kt.smartKibot;



import android.app.IRobotAsyncWorkListener;
import android.app.IRobotIntResultListener;
import android.app.RobotManager;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

public class RobotMotion {
	
	private static final String TAG = "RobotMotion";
	public RobotManager _robotManager;
	private Context ctx;
	private static RobotMotion _this=null;
	private static final boolean D = false;
	private int instanceId = -1;
	volatile boolean isFreeMove=false;
	
	private static final String DEFAULT_RMM_ALIAS="curRmm007";
	private UtilAssets utilAssets=null;
	
	private int curWheelState = STOPPED;
	// 현재 바퀴 상태 정의
	private static final int MOVE_FOWARD = 11;
	private static final int MOVE_BACK = 12;
	private static final int TURN_LEFT = 13;
	private static final int TURN_RIGHT = 14;
	private static final int STOPPED = 15;
	
	private boolean emrState=false;
	
	// led 위치에 적용되는 색
	private int[] _ccsidx = { 0, 0, 0, 0, 0 };

	// led 색상
	private static int[] s_ccs = { 0xFF000000, 0xFF0000FF, 0xFF00FF00,
			0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF };

	// led 점등 속도
	private double ledIntervals[] = { 0.5, 0.25, 0.16, 0.12, 0.1 }; // 1hz, 2hz, 3hz,
															// 4hz, 5hz,
	private LedThread[] ledDimThreads = { null, null, null, null, null, null };
	
	// 속도
	private static int[] s_spds3 = { 40, 50, 60, 70, 80 };
	private static float[] s_spds = { 0.01f,0.06f, 0.09f, 0.12f, 0.15f, 0.18f };
	private static float[] s_rots = { 40.0f, 50.0f, 60.0f, 70.0f, 80.0f };
	//public final int lengthToTime = 250000;
	//public final int lengthToTime = 100000;
	public final int lengthToTime = 20000;
	public final int degToTime = 1200;
	public final int friction = 5;

	// 얼굴 보는 방향 정의
	public static final int HEAD_FRONT = 0;
	public static final int HEAD_LEFT = 1;
	public static final int HEAD_RIGHT = 2;

	private RobotMotion(Context ctx){
		
		this.ctx=ctx;
		_robotManager = (RobotManager)this.ctx
				.getSystemService(Context.ROBOT_SERVICE);
		instanceId = _robotManager.init();
		
		IRobotAsyncWorkListener.Stub mAsyncListener = new IRobotAsyncWorkListener.Stub() {
			public void onUpcallEventFromRobot(int work, String printable) {
			//	KibotVar.setWork(work);
			

			}
		};
		
		_robotManager.registerListener(instanceId, mAsyncListener);
		_robotManager.setEmrEnable(emrState);
		
		utilAssets=new UtilAssets(ctx,"rmm");
		
	}
	
	
	
	static synchronized RobotMotion getInstance(Context ctx){
		if(_this==null) _this=new RobotMotion(ctx);
		
		return _this;
	}
		 static synchronized void finish(){
		 if(_this==null) return;
		 
			if (_this.instanceId != -1) {

				try {
					_this._robotManager.setEmrEnable(false);
					//stopPatten();
					_this.stopRMM();
					_this.stopFreeMove();
					_this.stopWheel();
					for (int i = 0; i < 6; i++) {
						_this._robotManager.setLedColor(i, 0xFF000000);
					}
					_this._robotManager.unregisterListener(_this.instanceId);
					_this._robotManager.close(_this.instanceId);
					_this._robotManager = null;
					_this.instanceId = -1;
				} catch ( NullPointerException e ) {
				}
			}
			
		 _this=null;
		 
	 }
	/**
	 * 바퀴 움직이는 함수 양쪽 바퀴를 다르게 제어
	 * 
	 * @param left
	 * @param right
	 * @param speed
	 */
	public void wheel(int left, int right) {
		if (D)
			Log.i(TAG, "wheel action start");
		boolean isLeftBack = false;
		boolean isRightBack = false;

		if (_robotManager == null)
			return;
		
		if (left < 0) {
			isLeftBack = true;
			left = -left;
		}
		if (right < 0) {
			isRightBack = true;
			right = -right;
		}

		if (isLeftBack && isRightBack) {
			if (curWheelState != MOVE_BACK) {
				curWheelState = MOVE_BACK;
			}
			
			_robotManager.moveWheelSeparate(s_spds3[right - 1],
					-s_spds3[left - 1]);
			
			head(-s_spds3[right - 1], -s_spds3[left - 1]);
			
		} else if (!isLeftBack && isRightBack) {

			if (curWheelState != TURN_RIGHT) {
				curWheelState = TURN_RIGHT;
			}

			if (left == 0) {
				_robotManager.moveWheelSeparate(s_spds3[right - 1], 0);
				head(-s_spds3[right - 1], 0);
			} else {
				_robotManager.moveWheelSeparate(s_spds3[right - 1],
						s_spds3[left - 1]);
				head(-s_spds3[right - 1], s_spds3[left - 1]);
			}
		} else if (isLeftBack && !isRightBack) {

			if (curWheelState != TURN_LEFT) {
				curWheelState = TURN_LEFT;
			}
			if (right == 0) {
				_robotManager.moveWheelSeparate(0, -s_spds3[left - 1]);
				head(0, -s_spds3[left - 1]);
			} else {
				_robotManager.moveWheelSeparate(-s_spds3[right - 1],
						-s_spds3[left - 1]);
				head(s_spds3[right - 1], -s_spds3[left - 1]);
			}
		} else {
			if (D)
				Log.i(TAG, "wheel 4");
			if (right == 0 && left == 0) {
				head(HEAD_FRONT);
				stopWheel();
				_robotManager.setEmrEnable(false);
			} else if (right == 0) {
				if (curWheelState != TURN_RIGHT) {
					curWheelState = TURN_RIGHT;
				}
				_robotManager.moveWheelSeparate(0, s_spds3[left - 1]);
				head(0, s_spds3[left - 1]);
			} else if (left == 0) {
				if (curWheelState != TURN_LEFT) {
					curWheelState = TURN_LEFT;
				}
				_robotManager.moveWheelSeparate(-s_spds3[right - 1], 0);
				head(s_spds3[right - 1], 0);
			} else {
				if (curWheelState != MOVE_FOWARD) {
					curWheelState = MOVE_FOWARD;
				}
				_robotManager.moveWheelSeparate(-s_spds3[right - 1],
						s_spds3[left - 1]);
				head(s_spds3[right - 1], s_spds3[left - 1]);
			}
		}
	}

	/**
	 * 앞으로 이동하는 함수
	 * 
	 * @param speed
	 * @param speakOn
	 * @param faceOn
	 */
	private void goForward(int speed, boolean speakOn, boolean faceOn) {
		if (curWheelState != MOVE_FOWARD) {
			if (speakOn) {
				// speakMsg("앞으로!");
//				playBGM(R.raw.bgm12);
			}
			if (faceOn) {
				//faceOn(KibotVar.EMO_MODE_FUN);
			}
			curWheelState = MOVE_FOWARD;
		}

		if (_robotManager == null)
			return;

		if (D)
			Log.i(TAG, "goFront");
		
		_robotManager.stopWheel();
		head(HEAD_FRONT);
		_robotManager.moveWheel(s_spds[speed - 1], 0.0f);
	}

	/**
	 * 앞으로 직진하는 함수
	 * 
	 * @param speed
	 */
	public void goForward(int speed) {
		goForward(speed, true, true);
	}


	/**
	 * 앞으로 이동하는 함수
	 * 
	 * @param speed
	 * @param len length * 10cm 만큼 전진
	//public final int lengthToTime = 250000;
	 */
	public void goForward(int speed, int length) {
		goForward(speed);
		long pastMillTime = System.currentTimeMillis();
		int time = length * lengthToTime / (speed * 30 + 30);
		if (D)
			Log.i(TAG, time + "");
		while ((System.currentTimeMillis() - pastMillTime) < time) {

		}
		stopWheel();
	}


	public void playRMM(String fileName){
		
		//String fullFilePath="/system/media/robot/rmm/A12.rmm";
		String fullFilePath=utilAssets.getFilePathOnFilSystem(fileName);
		
		Log.d(TAG,"path:"+fullFilePath);
		
		_robotManager.loadRmm(fullFilePath, DEFAULT_RMM_ALIAS, 
				new IRobotIntResultListener.Stub(){
					public void onResult(int result, int opCode) throws RemoteException
					{
							if(result<0)
							{
								Log.d(TAG,"Loading rmm file is failed. :"+result);
								return;
							}
							else
							{
								Log.d(TAG,"play rmm with "+DEFAULT_RMM_ALIAS);
								_robotManager.playRmm(DEFAULT_RMM_ALIAS, null);
							}
					}});
	}
	
	public void stopRMM(){
		_robotManager.stopRmm();
	}
	
	
		
	private void goBack(int speed, boolean speakOn, boolean faceOn) {
		if (curWheelState != MOVE_BACK) {
			if (speakOn) {
				// speakMsg("뒤로!");
//				playBGM(R.raw.bgm14);
			}
			if (faceOn) {
//				faceOn(KibotVar.EMO_MODE_FUN);
			}
			curWheelState = MOVE_BACK;
		}

		if (D)
			Log.i(TAG, "goBack");
		if (_robotManager == null)
			return;
		_robotManager.stopWheel();
		head(HEAD_FRONT);
		_robotManager.moveWheel(-s_spds[speed - 1], 0.0f);
	}

	public void stopAll(){
		stopFreeMove();
		stopRMM();
		stopWheel();
		offAllLed();
		/*head(HEAD_FRONT);*/
	}
	
	public void startFreeMove(){
	
		_robotManager.setEmrEnable(true);
		_robotManager.enterFreeMove(0, new IRobotIntResultListener.Stub()
		{
			public void onResult(int arg0, int arg1) throws RemoteException
			{

			}
		});
		
		//isFreeMove = true;
	}
	
	public void setEmrState(boolean state){
		emrState=state;
		_robotManager.setEmrEnable(state);
		
	}
	
	
	public void stopFreeMove(){
		//restore original state
		_robotManager.setEmrEnable(emrState);
		_robotManager.leaveFreeMove();
		
		//isFreeMove = false;
	}
	
	
	/**
	 * 뒤로 이동하는 함수
	 * 
	 * @param speed
	 */
	public void goBack(int speed) {
		goBack(speed, true, true);
	}
	
	public void goBack(int speed, int length) {
		goBack(speed);
		long pastMillTime = System.currentTimeMillis();
		int time = length * lengthToTime / (speed * 30 + 30);
		if (D)
			Log.i(TAG, time + "");
		while ((System.currentTimeMillis() - pastMillTime) < time) {
	
		}
		stopWheel();
	}
	


	/**
	 * 왼쪽으로 회전하는 함수
	 * 
	 * @param speed
	 */
	public void turnLeft(int speed) {
		if (curWheelState != TURN_LEFT) {
			curWheelState = TURN_LEFT;
		}

		if (D)
			Log.i(TAG, "turnLeft");
		if (_robotManager == null)
			return;
	//	_robotManager.stopWheel();
	//	head(HEAD_LEFT);
		_robotManager.moveWheel(0.0f, -s_rots[speed - 1]);
	}

	/**
	 * 회전하는 함수
	 * 
	 * @param speed
	 * @param deg
	 */
	public void turnLeft(int speed, int deg) {
		turnLeft(speed);
		long pastMillTime = System.currentTimeMillis();
		int time = (int) ((deg * degToTime / (s_rots[speed - 1])) - friction
				* s_rots[speed - 1]);
		if (D)
			Log.i(TAG, time + "");
		while ((System.currentTimeMillis() - pastMillTime) < time) {
		}
		if (D)
			Log.i(TAG, "deg = " + deg + ", speed = " + speed + ", time = "
					+ (System.currentTimeMillis() - pastMillTime));

		stopWheel();
	}



	/**
	 * 오른쪽으로 회전하는 함수
	 * 
	 * @param speed
	 * @param speakOn
	 * @param faceOn
	 */
	public void turnRight(int speed, boolean speakOn, boolean faceOn) {
		if (curWheelState != TURN_RIGHT) {
			if (speakOn) {
				// speakMsg("오른쪽으로!");
//				playBGM(R.raw.bgm16);
			}
			if (faceOn) {
//				faceOn(KibotVar.EMO_MODE_FUN);
			}
			curWheelState = TURN_RIGHT;
		}
		if (D)
			Log.i(TAG, "turnRight");
		if (_robotManager == null)
			return;
		_robotManager.stopWheel();
		head(HEAD_RIGHT);
		_robotManager.moveWheel(0.0f, s_rots[speed - 1]);
		_robotManager.moveHome();
	}

	/**
	 * 오른쪽으로 회전하는 함수
	 * 
	 * @param speed
	 */
	public void turnRight(int speed) {
		turnRight(speed, true, true);
	}

	/**
	 * 오른쪽으로 회전하는 함수
	 * 
	 * @param speed
	 * @param deg
	 */
	public void turnRight(int speed, int deg) {
		turnRight(speed);
		long pastMillTime = System.currentTimeMillis();
		int time = (int) ((deg * degToTime / (s_rots[speed - 1])) - friction
				* s_rots[speed - 1]);
		if (D)
			Log.i(TAG, time + "");
		while ((System.currentTimeMillis() - pastMillTime) < time) {
		}
		if (D)
			Log.i(TAG, "deg = " + deg + ", speed = " + speed + ", time = "
					+ (System.currentTimeMillis() - pastMillTime));
		stopWheel();
	}


	/**
	 * 정지하는 함수
	 * 
	 * @param speakOn
	 * @param faceOn
	 */
	public void stopWheel(boolean speakOn, boolean faceOn) {
			if (curWheelState != STOPPED) {
				if (speakOn) {
					// speakMsg("스탑!");
//					playBGM(R.raw.bgm13);
				}
				/*
				if (faceOn) {
					faceOn();
				}
				*/
				curWheelState = STOPPED;
			}
		if (D)
			Log.i(TAG, "stop");
		if (_robotManager == null)
			return;

		head(HEAD_FRONT);
		_robotManager.moveWheel(0.0f, 0.0f);
	}

	public void stopWheel() {
		stopWheel(true, true);
	}


	/*
	 * 0:0ff
	 * 1~63: LED brightness
	 */
	public void setLogoLEDDimming(int brightness){
		if(brightness==0)
		{
			_robotManager.setDimLedEnable(false);
		}
		else{
			
			_robotManager.setDimLedEnable(true);
			_robotManager.setDimLedLevel(brightness);
		}
		
		try{
			Thread.sleep(200);
			
		}catch(Exception e){ e.printStackTrace();}
		
		Log.d(TAG,"logoDimming:"+brightness);
	}


	/**
	 * led 점등하는 함수
	 * 
	 * @param loc
	 * @param speed
	 * @param color
	 */
	public void led(int loc, int speed, int color) {
		final int at = loc;
		double tmp = 0;
		
		if (speed != 0) {
			tmp = 1.0 / (speed * 2);
		} else {
			// 속도가 0일때 계산 차 때문에 선언시 thread 선언시 순서가 달라짐.
			// 달라지는 순서를 맞추기 위해서 아래와 같이 계산을 두번 함.
			tmp = 1.0 / (speed * 2);
			tmp = 0;
		}
		
		final double interval = tmp * 1000;
		_ccsidx[at] = color;
		if (_ccsidx[at] >= s_ccs.length)
			_ccsidx[at] = 0;

		 if(ledDimThreads[at] != null){
			 ledDimThreads[at].kill();
		 }
		 
		ledDimThreads[at] = new LedThread() {
			boolean isOn = false;

			public void run() {
				thisAt = at;
				numOfInit++;
				thisNum = numOfInit;
				// Log.i(TAG, "led : "+ interval + " : " + thisAt + " : " +
				// s_ccs[_ccsidx[thisAt]]);
				while (alive) {
					try {
						if (_robotManager == null)
							return;
						// if(D) Log.i(TAG, "LED" + 4);
						int cc = s_ccs[_ccsidx[at]];
						long pastMillTime = System.currentTimeMillis();
						
						if (interval == 0) {
							// 속도가 0일 때 먼저 죽는 경우가 있기 때문에 기다렸다가 속도 설정하고 죽음
//							while ((System.currentTimeMillis() - pastMillTime) < 1000) {
//
//							}
							try {
								sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (at == 0) {
								_robotManager.setHornLedColors(cc, cc);
							} else if (at == 2) {
								_robotManager.setArmLedColors(cc, cc);
							} else if (at == 4) {
								_robotManager.setLedColor(at, cc);
							}
							isOn = true;
							return ;
						} else {
							if (isOn) {//try to off when it is on
								if (at == 0) {
									_robotManager.setHornLedColors(s_ccs[0],
											s_ccs[0]);
								} else if (at == 2) {
									_robotManager.setArmLedColors(s_ccs[0],
											s_ccs[0]);
								} else if (at == 4) {
									_robotManager.setLedColor(at, s_ccs[0]);
								}
								isOn = false;
							} else { //try to on when it is off
								if (at == 0) {
									_robotManager.setHornLedColors(cc, cc);
								} else if (at == 2) {
									_robotManager.setArmLedColors(cc, cc);
								} else if (at == 4) {
									_robotManager.setLedColor(at, cc);
								}
								isOn = true;
							}

							pastMillTime = System.currentTimeMillis();
							int time = (int) interval;
							// if(D) Log.i(TAG, time+"");
//							while ((System.currentTimeMillis() - pastMillTime) < time) {
//
//							}
							try {
								sleep(time);
							} 
							catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// 다른게 들어오면 종료 처리 
							if (ledDimThreads[thisAt].thisNum != this.thisNum) {
								alive = false;
							}
						}
					} 
					catch (NullPointerException e) {
					}
				}
				
				//not alive
				if (_robotManager != null) {
					try {
						long pastMillTime = System.currentTimeMillis();
						while ((System.currentTimeMillis() - pastMillTime) < 500) {

						}
						if (at == 0) {
							_robotManager.setHornLedColors(s_ccs[0], s_ccs[0]);
						} else if (at == 2) {
							_robotManager.setArmLedColors(s_ccs[0], s_ccs[0]);
						} else if (at == 4) {
							_robotManager.setLedColor(at, s_ccs[0]);
						}
					} 
					catch (NullPointerException e) {
						
					}
				}
			}//end of run()
		};
		
		//test
		//ledDimThreads[at].start();
	}


	/**
	 * 얼굴 보는 방향 설정하는 함수
	 * 
	 * @param dir
	 */
	public void headWithSpeed(int dir,float speed) {
		float HeadRollSpeed = 0.7f;/* 0.1~1.0*/
		HeadRollSpeed=speed;

		switch (dir) {
		case HEAD_FRONT:
			if (_robotManager != null) {
				_robotManager.moveHome();
			}
			break;

		case HEAD_LEFT:
			if (_robotManager != null) {
				_robotManager.moveHeadRoll(-30, HeadRollSpeed);
			}
			break;

		case HEAD_RIGHT:
			if (_robotManager != null) {
				_robotManager.moveHeadRoll(30, HeadRollSpeed);
			}
			break;

		}
	}
	
	public void headRoll(float deg,float speed){
		
			if (_robotManager != null) {
				_robotManager.moveHeadRoll(deg, speed);
			}
	}

	/**
	 * 얼굴 보는 방향 설정하는 함수
	 * 
	 * @param dir
	 */
	public void head(int dir) {
		final float speed = 0.7f;
		headWithSpeed(dir,speed);

	}

	/**
	 * 얼굴 보는 방향 설정하는 함수
	 * 
	 * @param right
	 * @param left
	 */
	
	private void head(int right, int left) {
		final float HeadRollSpeed = 0.7f;
		if (left <= 0 && right <= 0) {
			int angle = (right - left) / 3;
			if (angle >= 30) {
				_robotManager.moveHeadRoll(-30, HeadRollSpeed);
			} else if (angle <= -30) {
				_robotManager.moveHeadRoll(30, HeadRollSpeed);
			} else {
				_robotManager.moveHeadRoll(angle, HeadRollSpeed);
			}
		} else {
			int angle = left - right;
			if (angle >= 30) {
				_robotManager.moveHeadRoll(30, HeadRollSpeed);
			} else if (angle <= -30) {
				_robotManager.moveHeadRoll(-30, HeadRollSpeed);
			} else {
				_robotManager.moveHeadRoll(angle, HeadRollSpeed);
			}
		}
	}
	
	
	public void offAllLed() {
		
	
		for (LedThread t : ledDimThreads) {
		if (t != null) {
				t.kill();
			}
		}
		
		 for(int i = 0 ; i < 5 ; i ++){
		 _robotManager.setLedColor(i, 0xff000000);
		 }
	}


}

class LedThread extends Thread {
	
	public static long numOfInit = 0;
	public int thisAt;
	public long thisNum;
	public boolean alive = true;
	public void kill(){
		alive = false;
		while(this.isAlive()){
			
		}
	}
	
	
}