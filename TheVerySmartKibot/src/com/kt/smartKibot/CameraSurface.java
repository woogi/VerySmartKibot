package com.kt.smartKibot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kt.facerecognition.framework.FaceDetection;

public class CameraSurface extends SurfaceView implements
	SurfaceHolder.Callback, PreviewCallback, Runnable {

    private static final String TAG = "FaceCameraSurface";
    private static final int FRAME_WIDTH = 640, FRAME_HEIGHT = 480;

    private static String DATA_PATH;
    private static final String DETECTION_DATA_DIR = "detectiondata/";
    private static final String EYE_DETECTION_DATA_FILE = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses.xml";
    private static final String EYE_DETECTION_DATA_FILE_1 = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses1.xml";
    private static final String EYE_DETECTION_DATA_FILE_2 = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses2.xml";
    private static final String FACE_DETECTION_DATA_FILE = DETECTION_DATA_DIR + "lbpcascade_frontalface.xml";

    private static Context ctx;
    private static CameraSurface instance;
    private static boolean reusing;

    private Bitmap bitmap;
    private byte[] buffer;
    private Camera camera;
    private byte[] data;
    private FaceDetection faceDetection;
    private OnFaceDetectListener faceListener;
    private int[] rgba;
    private boolean stopSample, stopSearch;

    /* Constructor */
    public CameraSurface(Context context) {
	super(context);
	ctx = context;
	initializeAssets();
	getHolder().addCallback(this);
	getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	stopSample = true;
	stopSearch = true;
	/* UI */
	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(360, 270);
	params.topMargin = 10;
	params.rightMargin = 10;
	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
	setLayoutParams(params);
	Message msg = new Message();
	msg.what = 1;
	msg.obj = this;
	RobotActivity.UIhandler.sendMessage(msg);
    }

    /* Interface listener */
    public interface OnFaceDetectListener {
	public void onFaceDetected(Bitmap bitmap, int detectedFaceNumber, Rect[] detectedFacePostion);
    }

    /* Set listener */
    public void setOnFaceDetectListener(OnFaceDetectListener listener) {
	Log.i("nicolas", "listener is " + listener);
	faceListener = listener;
    }

    /* public methods */
    public static CameraSurface getInstance(Context context){
	synchronized (context) {
	    if (instance == null) {
		instance = new CameraSurface(context);
		reusing = false;
	    } else {
		reusing = true;
	    }
	    return instance;
	}
    }

    public void start() {
	Log.i(TAG, "start");
	stopSample = false;
	stopSearch = false;
    }

    public void stopSample() {
	Log.i(TAG, "stop sample");
	if (!reusing) {
	    stopSample = true;
	    stopSearch = true;
	    /* Remove UI */
	    Message msg = new Message();
	    msg.what = -1;
	    msg.obj = this;
	    RobotActivity.UIhandler.sendMessage(msg);
	    instance = null;
	}
	reusing = false;
    }

    public void stopSearch() {
	Log.i(TAG, "stop search");
	stopSearch = true;
    }

    /* SurfaceHolder.Callback methods implementations */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
	Log.i(TAG, "surfaceCreated");
	try {
	    // This case can happen if the camera is open and closed too frequently.
	    camera = Camera.open();
	} catch (Exception e) {
	    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
	    Log.e(TAG, e.getMessage());
	    ((Activity) ctx).finish();
	    return;
	}

	if (camera != null) {
	    camera.setPreviewCallbackWithBuffer(this);
	    Parameters params = camera.getParameters();
	    params.setPreviewSize(FRAME_WIDTH, FRAME_HEIGHT);
	    if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
	    }
	    camera.setParameters(params);
	    allocateBuffer();
	    setPreviewDisplay();
	    startPreview(FRAME_WIDTH / 2, FRAME_HEIGHT / 2);
	}
	Message msg = new Message();
	msg.what = 2;
	msg.obj = getLayoutParams();
	RobotActivity.UIhandler.sendMessage(msg);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
	Log.i(TAG, "surfaceDestroyed");
	if (camera != null) {
	    synchronized (this) {
		camera.stopPreview();
		camera.setPreviewCallback(null);
		camera.release();
		camera = null;
	    }
	}
	stopPreview();
    }

    /* PreviewCallback method implementation */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
	if (!stopSample) {
	    synchronized (this) {
		this.data = reducedData(data);
		this.notify();
	    }
	    new Thread(this).start();
	}
	camera.addCallbackBuffer(buffer);
    }

    /* Runnable method implementation */
    @Override
    public void run() {
	Bitmap bitmap = null;
	synchronized (this) {
	    try {
		this.wait();
		bitmap = processFrame(data);
	    } catch (InterruptedException e) {
		Log.e(TAG, "Interrupted: " + e.getMessage());
	    }
	}
	if (bitmap != null) {
	    Message msg = new Message();
	    msg.what = 0;
	    msg.obj = bitmap;
	    RobotActivity.UIhandler.sendMessage(msg);
	}
    }

    /* Private methods */
    public void initializeAssets() {
	File filesDir = ctx.getFilesDir();
	AssetManager assets = ctx.getAssets();
	DATA_PATH = filesDir.getPath() + "/";
	makeDataDirectory(DETECTION_DATA_DIR);
	copyAssetsToData(assets, FACE_DETECTION_DATA_FILE);
	copyAssetsToData(assets, EYE_DETECTION_DATA_FILE,
		EYE_DETECTION_DATA_FILE_1, EYE_DETECTION_DATA_FILE_2);
    }

    private void makeDataDirectory(String dirName) {
	File dir = new File(DATA_PATH + dirName);
	if (!dir.exists()) {
	    dir.mkdir();
	}
    }

    private void copyAssetsToData(AssetManager assets, String assetFileName) {
	try {
	    InputStream in = assets.open(assetFileName);
	    OutputStream out = new FileOutputStream(DATA_PATH + assetFileName);
	    byte[] buf = new byte[1024];
	    int read;

	    while ((read = in.read(buf)) != -1) {
		out.write(buf, 0, read);
	    }
	    in.close();
	    out.flush();
	    out.close();
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
    }

    private void copyAssetsToData(AssetManager assets, String destFileName,
	    String assetFileName1, String assetFileName2) {
	try {
	    InputStream inPart1 = assets.open(assetFileName1);
	    InputStream inPart2 = assets.open(assetFileName2);
	    OutputStream out = new FileOutputStream(DATA_PATH + destFileName);
	    byte[] buf = new byte[1024];
	    int read;
	    while ((read = inPart1.read(buf)) != -1) {
		out.write(buf, 0, read);
	    }
	    while ((read = inPart2.read(buf)) != -1) {
		out.write(buf, 0, read);
	    }
	    inPart1.close();
	    inPart2.close();
	    out.flush();
	    out.close();
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
    }

    private void allocateBuffer() {
	int bitsPerPixel = ImageFormat.getBitsPerPixel(camera.getParameters()
		.getPreviewFormat());
	int w = FRAME_WIDTH;
	int h = FRAME_HEIGHT;
	int size = w * h * bitsPerPixel / 8;
	buffer = new byte[size];
	camera.addCallbackBuffer(buffer);
    }

    private void setPreviewDisplay() {

	try {
	    SurfaceView fakeview = this;
	    fakeview.getHolder().setType(
		    SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    camera.setPreviewDisplay(fakeview.getHolder());
	} catch (IOException e) {
	    Log.e(TAG, "Setting camera preview failed: " + e.getMessage());
	}
    }

    private void startPreview(int width, int height) {
	rgba = new int[width * height];
	try {
	    bitmap = Bitmap
		    .createBitmap(width, height, Bitmap.Config.ARGB_8888);
	} catch (OutOfMemoryError e) {
	    Log.e(TAG, "Bitmap Out of Memory: " + e.getMessage());
	    bitmap.recycle();
	    bitmap = null;
	}
	initFaceDetection(width, height);
	try {
	    Log.i(TAG, "start preview");
	    camera.startPreview();
	} catch (RuntimeException e) {
	    Log.e(TAG, "Starting camera preview failed");
	}
    }

    private void stopPreview() {
	if (bitmap != null) {
	    bitmap.recycle();
	    bitmap = null;
	}
	rgba = null;
	if (faceDetection != null) {
	    faceDetection.close();
	}
    }

    private byte[] reducedData(byte[] data) {
	int size = FRAME_WIDTH / 2 * FRAME_HEIGHT / 2
		+ (data.length - FRAME_WIDTH * FRAME_HEIGHT);
	byte[] reduced = new byte[size];
	for (int i = 0, idx = 0; i < FRAME_HEIGHT; i++) {
	    if (i % 2 == 0) {
		for (int j = 0; j < FRAME_WIDTH; j++) {
		    if (j % 2 == 1) {
			reduced[idx] = data[i * FRAME_WIDTH + j];
			idx++;
		    }
		}
	    }
	}
	return reduced;
    }

    private Bitmap processFrame(byte[] data) {
	Log.i(TAG, "processFrame called");
	if (stopSample || rgba == null || bitmap == null) {
	    Log.i(TAG, "processFrame stopped before end");
	    return null;
	} else {
	    Bitmap fullBitmap = getBitmapFromData(data);
	    if (!stopSearch) {
		faceDetection.getFaceRect(data);
		if (faceDetection.detectedFaceNumber > 0) {
		    Log.i(TAG, "face detected");
		    if (faceListener != null) {
			faceListener.onFaceDetected(fullBitmap,
				faceDetection.detectedFaceNumber,
				faceDetection.detectedFacePostion);
		    }
		}
	    }
	    return fullBitmap;
	}
    }

    private void initFaceDetection(int previewWidth, int previewHeight) {
	String facedetectiondata = DATA_PATH + FACE_DETECTION_DATA_FILE;
	String eyedetectiondata = DATA_PATH + EYE_DETECTION_DATA_FILE;
	/* new Face Detection instance */
	faceDetection = new FaceDetection();
	faceDetection.setTrainingFilePath(facedetectiondata, eyedetectiondata);
	faceDetection.setCamPreviewSize(previewWidth, previewHeight);
	faceDetection.setMinimumDetectionSize(1);
	/* Region Of Interest (ROI) */
	Rect roi = new Rect(0, 0, previewWidth, previewHeight);
	faceDetection.setROI(roi.left, roi.top, roi.right, roi.bottom);
    }

    private Bitmap getBitmapFromData(byte[] data) {
	Bitmap bitmap = this.bitmap;
	int[] rgba = this.rgba;
	int w = FRAME_WIDTH / 2;
	int h = FRAME_HEIGHT / 2;
	applyGrayScale(rgba, data, w, h);
	bitmap.setPixels(rgba, 0, w, 0, 0, w, h);
	return flip(bitmap);
    }

    private void applyGrayScale(int[] pixels, byte[] data, int width, int height) {
	int p;
	int size = width * height;
	for (int i = 0; i < size; i++) {
	    p = data[i] & 0xFF;
	    pixels[i] = 0xff000000 | p << 16 | p << 8 | p;
	}
    }

    private Bitmap flip(Bitmap bitmap) {
	Matrix matrix = new Matrix();
	matrix.preScale(-1f, 1f);
	return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
		bitmap.getHeight(), matrix, true);
    }
}
