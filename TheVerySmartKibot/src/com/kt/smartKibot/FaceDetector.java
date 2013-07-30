package com.kt.smartKibot;

import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.kt.facerecognition.framework.FaceRecognition;

public class FaceDetector implements IRobotEvtDelegator,
	CamSurface.OnFaceDetectListener {

    private Context ctx;
    private static final String TAG = "FaceDetector";
    private static CamSurface cameraSurface;
    private static FaceDetector instance;
    private IRobotEvtHandler handler;
    private boolean detected;

    @Override
    public void onFaceDetected(Bitmap bitmap, Vector<Rect> detectedPostions) {
	if (cameraSurface != null) {
	    cameraSurface.stopSearch();
	}
	Log.i(TAG, "onFaceDetected");
	if (!detected) {
	    detected = true;
	    RobotEvent evt = new RobotEvent(RobotEvent.EVT_FACE_DETECTION);
	    int param1 = -1;
	    Vector<Integer> idValues = getRecognizedFacesIds(bitmap,
		    detectedPostions);
	    if (!idValues.isEmpty()) {
		param1 = idValues.firstElement();
	    }
	    evt.setParam1(param1);
	    handler.handle(null, evt);
	}
    }

    @Override
    public void onFaceLost() {
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
	detected = false;
    }

    @Override
    public void stop() {
	Log.i(TAG, "stop");
	if (cameraSurface != null) {
	    cameraSurface.stopSample();
	}
    }

    public static FaceDetector getInstance(Context context) {
	Log.i(TAG, "instance is " + instance);
	if (instance == null) {
	    instance = new FaceDetector();
	    instance.ctx = context;
	}
	return instance;
    }

    /**
     * 
     * @param bitmap
     *            Whole bitmap from the Camera SurfaceHolder.Callback
     * @param detectedPositions
     *            Vector of Rectangles from the FaceDetection framework that
     *            represents the detected faces positions in the bitmap
     * @return Vector of Integers containing the ids of the targets detected in
     *         the bitmap. If none of the targets is in the bitmap, then the
     *         only element is -1.
     */
    private Vector<Integer> getRecognizedFacesIds(Bitmap bitmap,
	    Vector<Rect> detectedPositions) {
	Vector<Integer> idValues = new Vector<Integer>();
	Log.i(TAG, "/detected:" + detectedPositions.size());
	FaceRecognition facerecognition = new FaceRecognition(
		CamConf.SAVE_TARGETS_TRAINING_RESULT_PATH);
	for (Rect rect : detectedPositions) {
	    rect = flip(rect, bitmap.getWidth());
	    Bitmap detectedFace = CamUtils.cropFace(bitmap, rect);
	    int predictedClass = facerecognition.getPredictClass(detectedFace);
	    Log.i("nicolas", " /predictedClass:" + predictedClass);
	    if (predictedClass > 0) {
		idValues.add(predictedClass);
	    }
	}
	facerecognition.close();
	return idValues;
    }

    /**
     * Horizontal flip of a rectangle (used when Front Camera)
     * 
     * @param rect
     *            Rectangle to be flipped
     * @param width
     *            Maximum width of the preview
     * @return
     */
    private Rect flip(Rect rect, int width) {
	int rectWidth = rect.width();
	rect.left = width - rect.right;
	rect.right = rect.left + rectWidth;
	return rect;
    }

    /**
     * 
     * @return Boolean true if a face has been detected and false otherwise
     */
    public boolean isDetected() {
	return detected;
    }
}