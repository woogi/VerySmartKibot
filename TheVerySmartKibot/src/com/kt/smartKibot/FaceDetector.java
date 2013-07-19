package com.kt.smartKibot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.util.Log;

import com.kt.facerecognition.framework.FaceDetection;
import com.kt.facerecognition.framework.FaceRecognition;

public class FaceDetector implements IRobotEvtDelegator,
	CamSurface.OnFaceDetectListener {
    private static final String TAG = "FaceDetector";
    private static CamSurface cameraSurface;
    private static FaceDetector instance;
    private FaceDetection faceDetection;
    private IRobotEvtHandler handler;
    private int targets = 0;

    @Override
    public void onFaceDetected(Bitmap bitmap, int detectedFaceNumber,
	    Rect[] detectedFacePostion) {
	Log.i(TAG, "onFaceDetected");
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

    public void setTarget(int id, Bitmap picture) {
	int width = picture.getWidth();
	int height = picture.getHeight();
	int[] rgb = new int[width * height];
	byte[] data = new byte[width * height * 3 / 2];
	initFaceDetection(width, height);
	picture.getPixels(rgb, 0, width, 0, 0, width, height);
	encodeYUV420SP(data, rgb, width, height);
	faceDetection.getFaceRect(data);
	Log.i(TAG, " /target:" + faceDetection.detectedFaceNumber);
	if (faceDetection.detectedFaceNumber > 0) {
	    targets++;
	    Rect rect = faceDetection.detectedFacePostion[0];
	    Bitmap face = cropFace(picture, rect);
	    String tagetFaceFile = CamConf.SAVE_TARGETS_PATH + "target" + id
		    + ".jpg";
	    saveBitmapToFile(face, tagetFaceFile);
	    makeCameraTrainingCSV(id, CamConf.SAVE_TARGETS_TRAINING_LIST_PATH);
	    FaceRecognition.startTrainingFaces(
		    CamConf.SAVE_TARGETS_TRAINING_LIST_PATH,
		    CamConf.SAVE_TARGETS_TRAINING_RESULT_PATH);
	}
    }

    @Override
    public void start() {
	Log.i(TAG, "start");
	cameraSurface = CamSurface.getInstance(RobotActivity.getContext());
	if (cameraSurface != null) {
	    cameraSurface.setOnFaceDetectListener(this);
	    cameraSurface.start();
	}
    }

    @Override
    public void stop() {
	Log.i(TAG, "stop");
	if (cameraSurface != null) {
	    cameraSurface.stopSample();
	}
	targets = 0;
	File file = new File(CamConf.SAVE_TARGETS_PATH);
	recursiveDeletion(file);
    }

    public static FaceDetector getInstance() {
	Log.i(TAG, "instance is " + instance);
	if (instance == null) {
	    instance = new FaceDetector();
	}
	return instance;
    }

    private Vector<Integer> getRecognizedFacesIds(Bitmap bitmap,
	    int detectedFaceNumber, Rect[] detectedFacePostion) {
	Vector<Integer> idValues = new Vector<Integer>();
	for (int cpt = 0; cpt < targets; cpt++) {
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int[] rgb = new int[width * height];
	    byte[] data = new byte[width * height * 3 / 2];
	    initFaceDetection(width, height);
	    bitmap.getPixels(rgb, 0, width, 0, 0, width, height);
	    encodeYUV420SP(data, rgb, width, height);
	    faceDetection.getFaceRect(data);
	    Log.i(TAG, "/detected:" + detectedFaceNumber);
	    for (int i = 0; i < detectedFaceNumber; i++) {
		Rect rect = flip(detectedFacePostion[i], width);
		Bitmap detectedFace = cropFace(bitmap, rect);
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

    private void initFaceDetection(int previewWidth, int previewHeight) {
	String facedetectiondata = CamConf.DATA_PATH
		+ CamConf.FACE_DETECTION_DATA_FILE;
	String eyedetectiondata = CamConf.DATA_PATH
		+ CamConf.EYE_DETECTION_DATA_FILE;
	/* new Face Detection instance */
	faceDetection = new FaceDetection();
	faceDetection.setTrainingFilePath(facedetectiondata, eyedetectiondata);
	faceDetection.setCamPreviewSize(previewWidth, previewHeight);
	faceDetection.setMinimumDetectionSize(1);
	/* Set ROI (Region Of Interest) and draw scaled frame */
	Rect roi = new Rect(0, 0, previewWidth, previewHeight);
	faceDetection.setROI(roi.left, roi.top, roi.right, roi.bottom);
    }

    void encodeYUV420SP(byte[] data, int[] argb, int width, int height) {
	final int frameSize = width * height;
	int idxY = 0, idxUV = frameSize, idx = 0;
	int R, G, B, Y, U, V;
	for (int j = 0; j < height; j++) {
	    for (int i = 0; i < width; i++) {
		R = (argb[idx] & 0xff0000) >> 16;
		G = (argb[idx] & 0xff00) >> 8;
		B = (argb[idx] & 0xff) >> 0;
		Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
		U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
		V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
		data[idxY++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
		if (j % 2 == 0 && idx % 2 == 0) {
		    data[idxUV++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
		    data[idxUV++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
		}
		idx++;
	    }
	}
    }

    private Bitmap cropFace(Bitmap picture, Rect rect) {
	Bitmap croppedFace = Bitmap.createBitmap(picture, rect.left, rect.top,
		rect.width(), rect.height());
	Bitmap scaledFace = Bitmap.createScaledBitmap(croppedFace,
		CamConf.FACE_IMAGE_SIZE_W, CamConf.FACE_IMAGE_SIZE_H, true);
	return scaledFace;
    }

    private void saveBitmapToFile(Bitmap bitmap, String filePath) {
	Log.i(TAG, "save bitmap to " + filePath);
	File file = createFile(filePath);
	if (file != null) {
	    OutputStream out = null;
	    try {
		out = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, out);
		out.close();
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
	    }
	}
    }

    private void makeCameraTrainingCSV(int id, String cvsFile) {
	File file = createFile(cvsFile);
	if (file != null) {
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(cvsFile));
		String strOut = CamConf.SAVE_TARGETS_PATH + "target" + id
			+ ".jpg;" + id;
		out.write(strOut);
		out.newLine();
		out.close();
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
	    }
	}
    }

    private File createFile(String filePath) {
	try {
	    File file = new File(filePath);
	    File dir = file.getParentFile();
	    if (!dir.exists()) {
		dir.mkdirs();
	    }
	    file.createNewFile();
	    return file;
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
	return null;
    }

    private void recursiveDeletion(File file) {
	if (file.isDirectory()) {
	    for (File child : file.listFiles()) {
		recursiveDeletion(child);
	    }
	}
	file.delete();
    }

    private Rect flip(Rect rect, int width) {
	int rectWidth = rect.width();
	rect.left = width - rect.right;
	rect.right = rect.left + rectWidth;
	return rect;
    }
}