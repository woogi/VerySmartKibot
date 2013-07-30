package com.kt.smartKibot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.Log;

import com.kt.facerecognition.framework.FaceDetection;
import com.kt.facerecognition.framework.FaceRecognition;

public class DayTimeBehavior extends RobotBehavior {

    private static final String TAG = "DayTimeBehavior";
    long lastTouch = 0;
    private volatile boolean isEnd = false;
    private Context ctx;
    private FaceDetection fd;
    private int cptR = 0, cptD = 0, cptF = 0, cptT = 0;

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
	// TODO Auto-generated method stub
	super.onStart(ctx);
	RobotTimer.getInstance().start();
	CamUtils.resetTargetsFolder();
	this.ctx = ctx;
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		/* new Face Detection instance */
		fd = new FaceDetection();
		fd.setTrainingFilePath(CamConf.DATA_PATH
			+ CamConf.FACE_DETECTION_DATA_FILE, CamConf.DATA_PATH
			+ CamConf.EYE_DETECTION_DATA_FILE);
		fd.setMinimumDetectionSize(1);
		/* Set Targets from Assets folder */
		setTarget(1, "target1.jpg");
		setTarget(2, "target2.jpg");
		setTarget(3, "target3.jpg");
		setTarget(4, "target4.jpg");
		setTarget(5, "target5.jpg");
		/* Initiate Face Recognition */
		FaceRecognition.startTrainingFaces(
			CamConf.SAVE_TARGETS_TRAINING_LIST_PATH,
			CamConf.SAVE_TARGETS_TRAINING_RESULT_PATH);
	    }
	}).start();
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
	// TODO Auto-generated method stub

	if (isEnd)
	    return;

	Time _t = new Time();
	_t.setToNow();
	long currentTime = _t.toMillis(false);

	switch (evt.getType()) {
	case RobotEvent.EVT_TOUCH_SCREEN: {
	    changeState(new StateSleeping());
	}
	    break;

	case RobotEvent.EVT_TIMER:
	    if (StateGreeting.class.isInstance(getCurrentState())) {
		if (evt.getParam1() == 2 /* test */) {
		    changeState(new StateLookAround(evt, history_log));
		    return;
		}
	    }

	    if (StateWandering.class.isInstance(getCurrentState())) {
		if (evt.getParam1() == 4) {
		    changeState(new StateSleeping());
		    return;
		}
	    }

	    if (StateEvasion.class.isInstance(getCurrentState())) {
		if (evt.getParam1() == 4) {
		    changeState(new StateSleeping());
		    return;
		}
	    }

	    if (StateLookAround.class.isInstance(getCurrentState())) {
		if (evt.getParam1() == 6) {
		    cptF++;
		    cptT++;
		    Log.i("nicolas", " | " + cptR + " | " + cptD + " | " + cptF
			    + " | " + cptT + " | ");
		    if (StateGreeting.class.isInstance(getLastState())) {
			changeState(new StateWandering(evt, history_log));
		    } else {
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
		if (evt.getParam1() == 2) {
		    changeState(new StateLookAround(evt, history_log));
		    int rand = (int) (Math.random() * 2);
		    if (rand == 0) {
			changeState(new StateWandering(evt, history_log));
		    } else {
			changeState(new StateLookAround(evt, history_log));
		    }
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
		    // event

		    int cntBigNoise = 0;
		    int cntSleepingState = 1; // 현재 상황이 sleepingState이니 기본 cnt는
					      // 1
		    RobotLog prevLog = null;

		    ListIterator<RobotLog> it = history_log
			    .listIterator(history_log.size());

		    while (it.hasPrevious()) {
			RobotLog log = it.previous();

			if (log.getEvent().getTimeStamp().toMillis(false) + 1000 * 60 * 10 < currentTime)
			    /* 10 분 이내 내역만 */
			    break;

			if (StateSleeping.class.isInstance(log.getState())) {
			    if (prevLog != null
				    && !StateSleeping.class.isInstance(prevLog
					    .getState())) {
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

	case RobotEvent.EVT_BATTERY_STATE:
	    switch (evt.getParam1()) {
	    case BatteryChecker.PARAM_POWER_CONNECTED:
		changeState(new StateCharging());
		break;

	    case BatteryChecker.PARAM_POWER_DISCONNECTED:
		if (StateCharging.class.isInstance(getCurrentState())) {

		    changeState(new StateWandering(evt, history_log));
		}
		break;

	    }
	    break;

	case RobotEvent.EVT_TOUCH_BODY: {

	    int _cntTouch = 1;

	    if (lastTouch != 0
		    && System.currentTimeMillis() < lastTouch + 2 * 1000)
		break;
	    else
		lastTouch = System.currentTimeMillis();
	    /*
	     * if(evt.getTimeStamp().toMillis(false) +1000*2 <currentTime)
	     * //2초이내 이벤트 는 무시 return;
	     */

	    ListIterator<RobotLog> it = history_log.listIterator(history_log
		    .size());
	    while (it.hasPrevious()) {
		RobotLog log = it.previous();
		if (log.getEvent().getTimeStamp().toMillis(false) + 1000 * 60 * 2 < currentTime)
		    // 2분 이내 내역만
		    break;
		if (log.getEvent().getType() == RobotEvent.EVT_TOUCH_BODY)
		    ++_cntTouch;
	    }

	    Log.d(TAG, "total count of body touch:" + _cntTouch + "in 2 min.");

	    if (_cntTouch < /* 6 */100000) {
		changeState(new StateTouchResponse(evt.getParam1(), history_log));
		// changeState(new StateTouchResponse(evt.getParam1(),
		// history_log));
	    } else {
		changeState(new StateEvasion(StateEvasion.CAUSE_TOUCH_TOO_MUCH));
	    }
	}
	    break;

	case RobotEvent.EVT_FACE_DETECTION: {
	    Log.d(TAG, "Face Detection Event");
	    if (evt.getParam1() > 0) {
		cptR++;
		cptT++;
		Log.i("nicolas", " | " + cptR + " | " + cptD + " | " + cptF
			+ " | " + cptT + " | " + "/" + evt.getParam1() + "/");
	    } else {
		cptD++;
		cptT++;
		Log.i("nicolas", " | " + cptR + " | " + cptD + " | " + cptF
			+ " | " + cptT + " | ");
	    }
	    if (StateLookAround.class.isInstance(getCurrentState())) {
		changeState(new StateGreeting());
	    } else if (StateWandering.class.isInstance(getCurrentState())) {
		changeState(new StateFollowing());
	    }

	}
	    break;
	case RobotEvent.EVT_FACE_RECOGNITION: {
	    Log.d(TAG, "Face Recognition Event");
	}
	    break;
	case RobotEvent.EVT_FACE_LOST: {
	    Log.d(TAG, "Face Lost Event");
	    changeState(new StateSleeping());
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

	    // changeState(new StateSleeping());
	    return;
	}

    }

    /**
     * With a whole picture, crops the face in it and save this face with the id
     * parameter for following Face Recognitions
     * 
     * @param id
     *            The id associated to the target
     * @param asset
     *            The name of the picture in the assets/targets folder (with
     *            extension)
     */
    private void setTarget(int id, String asset) {
	try {
	    InputStream is = ctx.getAssets().open("targets/" + asset);
	    Bitmap picture = BitmapFactory.decodeStream(is);
	    int width = picture.getWidth();
	    int height = picture.getHeight();
	    String savedPicture = CamConf.SAVE_TARGETS_PATH + "target" + id
		    + "pic.jpg";
	    CamUtils.saveBitmapToFile(picture, savedPicture);
	    fd.setCamPreviewSize(width, height);
	    fd.setROI(0, 0, width, height);
	    ;
	    fd.getFaceRect(savedPicture);
	    Log.i(TAG, " /target#" + id + ":" + fd.detectedFaceNumber);
	    if (fd.detectedFaceNumber > 0) {
		Rect rect = fd.detectedFacePostion[0];
		Bitmap face = CamUtils.cropFace(picture, rect);
		for (int value = -40; value <= 40; value += 10) {
		    String tagetFaceFile = CamConf.SAVE_TARGETS_PATH + "target"
			    + id + "_" + (40 + value) + ".jpg";
		    CamUtils.saveBitmapToFile(
			    CamUtils.adjustedBrightness(face, value),
			    tagetFaceFile);
		    CamUtils.appendCameraTrainingCSV(
			    CamConf.SAVE_TARGETS_TRAINING_LIST_PATH,
			    tagetFaceFile + ";" + id);
		}
	    }
	} catch (IOException e) {
	    Log.e(TAG, "bitmap target null " + e.getMessage());
	}
    }

}
