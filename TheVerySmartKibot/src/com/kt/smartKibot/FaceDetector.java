package com.kt.smartKibot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Message;
import android.util.Log;

import com.kt.facerecognition.framework.FaceDetection;
import com.kt.facerecognition.framework.FaceRecognition;

public class FaceDetector implements IRobotEvtDelegator,
	CamSurface.OnFaceDetectListener {
    private static final String TAG = "FaceDetector";
    private static CamSurface cameraSurface;
    private Context ctx=null;
    private static FaceDetector instance;
    private FaceDetection faceDetection;
    private IRobotEvtHandler handler;
    private Vector<Integer> targets;
    // private int cptDetection;
    private boolean detected;

    // private long[] detection;

    
    @Override
    public void onFaceDetected(Bitmap bitmap, int detectedFaceNumber,
	    Rect[] detectedFacePostion) {
	Message msg = new Message();
	msg.what = CamConf.DRAW_RECT;
	msg.obj = detectedFacePostion;
	RobotActivity.UIHandler.sendMessage(msg);
	Log.i(TAG, "onFaceDetected");
	// cptDetection++;
	detected = true;
	// if (detection[0] == -1) {
	// detection[0] = System.currentTimeMillis();
	// detection[1] = detection[0];
	// } else {
	// detection[1] = System.currentTimeMillis();
	// }
	// if (cptDetection >= 2 && (detection[1] - detection[0]) > 1000) {
	if (cameraSurface != null) {
	    cameraSurface.stopSearch();
	}
	RobotEvent evt = new RobotEvent(RobotEvent.EVT_FACE_DETECTION);
	int param1 = -1;
	Vector<Integer> idValues = getRecognizedFacesIds(bitmap,
		detectedFaceNumber, detectedFacePostion);
	if (!idValues.isEmpty()) {
	    param1 = idValues.firstElement();
	}
	evt.setParam1(param1);
	handler.handle(null, evt);
	// }
    }

    @Override
    public void onFaceLost() {
	Message msg = new Message();
	msg.what = CamConf.DRAW_RECT;
	msg.obj = null;
	RobotActivity.UIHandler.sendMessage(msg);
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

    public void setTarget(int id, Bitmap picture) {
	if (targets == null) {
	    targets = new Vector<Integer>();
	}
	int width = picture.getWidth();
	int height = picture.getHeight();
	int[] rgb = new int[width * height];
	byte[] data = new byte[width * height * 3 / 2];
	FaceDetection fd = CamUtils.initFaceDetection(width, height);
	picture.getPixels(rgb, 0, width, 0, 0, width, height);
	CamUtils.encodeYUV420SP(data, rgb, width, height);
	fd.getFaceRect(data);
	rgb = null;
	data = null;
	Log.i(TAG, " /target:" + fd.detectedFaceNumber);
	if (fd.detectedFaceNumber > 0) {
	    targets.add(id);
	    Rect rect = fd.detectedFacePostion[0];
	    Bitmap face = CamUtils.cropFace(picture, rect);
	    String tagetFaceFile = CamConf.SAVE_TARGETS_PATH + "target" + id
		    + ".jpg";
	    CamUtils.saveBitmapToFile(face, tagetFaceFile);
	}
    }

    @Override
    public void start() {
	Log.i(TAG, "start");
	if (targets != null) {
	    makeCameraTrainingCSV(CamConf.SAVE_TARGETS_TRAINING_LIST_PATH);
	    FaceRecognition.startTrainingFaces(
		    CamConf.SAVE_TARGETS_TRAINING_LIST_PATH,
		    CamConf.SAVE_TARGETS_TRAINING_RESULT_PATH);
	}
	cameraSurface = CamSurface.getInstance(ctx);
	if (cameraSurface != null) {
	    cameraSurface.setOnFaceDetectListener(this);
	    cameraSurface.start();
	}
	// cptDetection = 0;
	detected = false;
	// detection = new long[2];
	// detection[0] = -1;
	// detection[1] = -1;
	
	RobotMotion.getInstance(ctx).led(0,1,2);
    }

    @Override
    public void stop() {
	Log.i(TAG, "stop");
	if (cameraSurface != null) {
	    cameraSurface.stopSample();
	}
	targets = null;
	CamUtils.recursiveDeletion(new File(CamConf.SAVE_TARGETS_PATH));
	RobotMotion.getInstance(ctx).led(0,0,0);
    }

    public static FaceDetector getInstance(Context ctx) {
	Log.i(TAG, "instance is " + instance);
	if (instance == null) {
	    instance = new FaceDetector();
	    instance.ctx=ctx;
	}
	return instance;
    }

    private Vector<Integer> getRecognizedFacesIds(Bitmap bitmap,
	    int detectedFaceNumber, Rect[] detectedFacePostion) {
	Vector<Integer> idValues = new Vector<Integer>();
	for (int cpt = 0; cpt < (targets == null ? 0 : targets.size()); cpt++) {
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int[] rgb = new int[width * height];
	    byte[] data = new byte[width * height * 3 / 2];
	    faceDetection = CamUtils.initFaceDetection(width, height);
	    bitmap.getPixels(rgb, 0, width, 0, 0, width, height);
	    CamUtils.encodeYUV420SP(data, rgb, width, height);
	    faceDetection.getFaceRect(data);
	    Log.i(TAG, "/detected:" + detectedFaceNumber);
	    for (int i = 0; i < detectedFaceNumber; i++) {
		Rect rect = flip(detectedFacePostion[i], width);
		Bitmap detectedFace = CamUtils.cropFace(bitmap, rect);
		FaceRecognition facerecognition = new FaceRecognition(
			CamConf.SAVE_TARGETS_TRAINING_RESULT_PATH);
		int predictedClass = facerecognition
			.getPredictClass(detectedFace);
		facerecognition.close();
		Log.i(TAG, " /predictedClass:" + predictedClass);
		if (predictedClass > 0) {
		    idValues.add(predictedClass);
		}
	    }
	}
	return idValues;
    }

    private void makeCameraTrainingCSV(String cvsFile) {
	File file = CamUtils.createFile(cvsFile);
	if (file != null) {
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(cvsFile));
		for (int id : targets) {
		    String strOut = CamConf.SAVE_TARGETS_PATH + "target" + id
			    + ".jpg;" + id;
		    out.write(strOut);
		    out.newLine();
		}
		out.close();
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
	    }
	}
    }

    private Rect flip(Rect rect, int width) {
	int rectWidth = rect.width();
	rect.left = width - rect.right;
	rect.right = rect.left + rectWidth;
	return rect;
    }

    public boolean isDetected() {
	return detected;
    }
}