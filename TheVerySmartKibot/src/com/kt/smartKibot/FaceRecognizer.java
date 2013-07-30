package com.kt.smartKibot;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import com.kt.facerecognition.framework.FaceRecognition;

public class FaceRecognizer implements IRobotEvtDelegator,
	CamSurface.OnFaceDetectListener {

    private static final String TAG = "FaceRecognizer";
    private static final int MAX_FAILURE = 200;

    private Context ctx;

    private static CamSurface cameraSurface;
    private static FaceRecognizer instance;

    private int cpt;
    private CamDatabase database;
    private int direction;
    private IRobotEvtHandler handler;
    private String loggedName;
    private Rect reference;
    private int tolerance;

    @Override
    public void onFaceDetected(Bitmap bitmap, Vector<Rect> detectedPostions) {
	handleDetectedFaces(bitmap, detectedPostions);
	RobotEvent evt = new RobotEvent(RobotEvent.EVT_FACE_RECOGNITION);
	handler.handle(null, evt);
	cpt = 0;
    }

    @Override
    public void onFaceLost() {
	direction = CamConf.LOST;
	cpt = ++cpt % MAX_FAILURE;
	Log.i(TAG, "FAIL #" + cpt);
	if (cpt == 0) {
	    RobotEvent evt = new RobotEvent(RobotEvent.EVT_FACE_RECOGNITION);
	    handler.handle(null, evt);
	    RobotEvent event = new RobotEvent(RobotEvent.EVT_FACE_LOST);
	    handler.handle(null, event);
	}
    }

    @Override
    public void installHandler(IRobotEvtHandler handler) {
	Log.i(TAG, "installHandler");
	this.handler = handler;
    }

    @Override
    public void uninstallHandler() {
	Log.i(TAG, "uninstallHandler");
	stop();
	handler = null;
    }

    @Override
    public void start() {
	Log.i(TAG, "start");
	cameraSurface = CamSurface.getInstance(ctx);
	if (cameraSurface != null) {
	    cameraSurface.setOnFaceDetectListener(this);
	    cameraSurface.start();
	}
	cpt = 0;
	/* init direction */
	direction = CamConf.LOST;
	/* init database */
	database = new CamDatabase(ctx);
	/* init logged name */
	loggedName = null;
	/* init tolerance */
	int length = (CamConf.FRAME_HEIGHT / 2) / 2;
	tolerance = length / 10;
	/* init ground zone */
	int l = (CamConf.FRAME_WIDTH / 2) / 2 - length / 2;
	int t = length / 2;
	int r = l + length;
	int b = t + length;
	reference = new Rect(l, t, r, b);
    }

    @Override
    public void stop() {
	Log.i(TAG, "stop");
	if (cameraSurface != null) {
	    cameraSurface.stopSample();
	}
    }

    public static FaceRecognizer getInstance(Context context) {
	Log.i(TAG, "instance is " + instance);
	if (instance == null) {
	    instance = new FaceRecognizer();
	    instance.ctx = context;
	}
	return instance;
    }

    public int getDirection() {
	return direction;
    }

    /**
     * 
     * @param bitmap
     *            Whole bitmap from the Camera SurfaceHolder.Callback
     * @param detectedPositions
     *            Vector of Rectangles from the FaceDetection framework that
     *            represents the detected faces positions in the bitmap
     */
    private void handleDetectedFaces(Bitmap bitmap,
	    Vector<Rect> detectedPositions) {
	Vector<DetectedFaceProperties> faces = new Vector<DetectedFaceProperties>();
	DetectedFaceProperties bestFace = new DetectedFaceProperties();
	for (Rect rect : detectedPositions) {
	    DetectedFaceProperties face = new DetectedFaceProperties();
	    face.rect = rect;
	    /* for front camera do horizontal flip frame rectangle */
	    face.rect = flip(face.rect);
	    face.imageUri = CamUtils.saveCroppedFace(face.rect, bitmap);
	    if (face.imageUri != null) {
		/* Returns cursor to browse DB if known, null if not */
		Cursor cursorClass = isKnown(face.imageUri);
		if (cursorClass != null) {
		    /*
		     * if face is known, cursor is not null. then browse in
		     * database to get tag name associated
		     */
		    face.tagName = cursorClass.getString(cursorClass
			    .getColumnIndex(CamDatabase.KEY_TAG_NAME));
		    face.friendLevel = Integer
			    .parseInt(cursorClass.getString(cursorClass
				    .getColumnIndex(CamDatabase.KEY_FRIEND_LEVEL)));
		    cursorClass.close();
		}
	    }
	    faces.add(face);
	    bestFace = chooseBestFace(face, bestFace);
	}
	/* if no one is logged in, register new log name in database */
	if (loggedName == null) {
	    if (bestFace.tagName == null) {
		String num = CamUtils.getNextFileNum(CamConf.SAVE_FACES_PATH);
		loggedName = "name_" + num;
		String imagePath = CamConf.SAVE_FACES_PATH + num + ".jpg";
		if (CamUtils.registerName(database, loggedName)
			&& CamUtils.registerImage(bestFace.imageUri, imagePath)) {
		    CamUtils.addItemToDatabase(database, loggedName, imagePath,
			    0);
		    Log.i(TAG, "<follow> add name " + loggedName + " in DB "
			    + " (friend #" + bestFace.friendLevel + ")");
		}
	    } else {
		loggedName = bestFace.tagName;
		CamUtils.logInAs(database, loggedName);
		Log.i(TAG, "<follow> " + loggedName + " already in DB "
			+ " (friend #" + bestFace.friendLevel + ")");
	    }
	}
	if (bestFace != null) {
	    letsMove(bestFace.rect);
	} else {
	    direction = CamConf.LOST;
	}
    }

    private Rect flip(Rect rect) {
	int rectWidth = rect.width();
	rect.left = CamConf.FRAME_WIDTH / 2 - rect.right;
	rect.right = rect.left + rectWidth;
	return rect;
    }

    private Cursor isKnown(Uri croppedFaceUri) {
	String capturedFaceFilePath = croppedFaceUri.getPath();
	FaceRecognition facerecognition = new FaceRecognition(
		CamConf.SAVE_FACES_TRAINING_RESULT_PATH);
	int predictedClass = facerecognition
		.getPredictClass(capturedFaceFilePath);
	facerecognition.close();
	Cursor cursorClass = null;
	/* if predectedClass > 0 face has been recognized */
	if (predictedClass > 0) {
	    cursorClass = database.getClassIDMatches(predictedClass);
	}
	/* return cursor to browse db and null if not known */
	return cursorClass;
    }

    private DetectedFaceProperties chooseBestFace(DetectedFaceProperties face,
	    DetectedFaceProperties best) {
	/* if someone is logged in, only follow this one */
	if (loggedName != null && loggedName != "") {
	    if (loggedName.equals(face.tagName)) {
		return face;
	    } else {
		return null;
	    }
	} else { /* if none is logged in */
	    /* better priority if the face is in the db */
	    if (best.tagName == null) {
		return face;
	    } else { /* if both are in the db */
		/* better priority according to friendship level */
		if (face.friendLevel > best.friendLevel) {
		    return face;
		} else {
		    return best;
		}
	    }
	}
    }

    private void letsMove(Rect current) {
	/*
	 * Movement algorithm according to the face tracking. It calculates the
	 * leading difference between the horizontal one (LEFT-RIGHT) and the
	 * vertical one (FWD-BACK) If the deduced movement is already the
	 * current movement, no need to tell again to move
	 */
	int deltaX = (current.left - reference.left);
	int deltaY = (current.width() - reference.width());
	if (Math.abs(deltaX) > 2 * Math.abs(deltaY) && deltaX > 2 * tolerance) {
	    direction = CamConf.LEFT;
	} else if (Math.abs(deltaX) > 2 * Math.abs(deltaY)
		&& deltaX < -2 * tolerance) {
	    direction = CamConf.RIGHT;
	} else if (2 * Math.abs(deltaY) > Math.abs(deltaX)
		&& deltaY > tolerance) {
	    direction = CamConf.BACK;
	} else if (2 * Math.abs(deltaY) > Math.abs(deltaX)
		&& deltaY < -tolerance) {
	    direction = CamConf.FWD;
	} else {
	    direction = CamConf.STOP;
	}
    }

    private class DetectedFaceProperties {
	public Rect rect;
	public String tagName;
	public int friendLevel;
	public Uri imageUri;

	public DetectedFaceProperties() {
	    rect = null;
	    tagName = null;
	    friendLevel = 0;
	    imageUri = null;
	}
    }
}
