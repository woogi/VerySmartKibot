package com.kt.smartKibot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.kt.facerecognition.framework.FaceDetection;

public class FaceCameraSurface extends SurfaceView implements
	SurfaceHolder.Callback, PreviewCallback, Runnable {

    private static final String TAG = "FaceCameraSurface";
    private static final String DETECTION_DATA_DIR = "detectiondata/";
    private static final String EYE_DETECTION_DATA_FILE = DETECTION_DATA_DIR
	    + "haarcascade_eye_tree_eyeglasses.xml";
    private static final String EYE_DETECTION_DATA_FILE_1 = DETECTION_DATA_DIR
	    + "haarcascade_eye_tree_eyeglasses1.xml";
    private static final String EYE_DETECTION_DATA_FILE_2 = DETECTION_DATA_DIR
	    + "haarcascade_eye_tree_eyeglasses2.xml";
    private static final String FACE_DETECTION_DATA_FILE = DETECTION_DATA_DIR
	    + "lbpcascade_frontalface.xml";
    private static String DATA_PATH;
    private final int frameWidth = 640, frameHeight = 480;
    private final int reductCoef = 2;
    private Bitmap bitmap;
    private byte[] buffer;
    private Camera camera;
    private byte[] data;
    private FaceDetection faceDetection;
    private OnFaceDetectListener faceListener;
    private int[] rgba;
    private boolean stop;

    /* Handling the sample preview */
    private static ImageView sampleView;
    private static Handler sampleHandler = new Handler() {
	public void handleMessage(Message msg) {
	    switch (msg.what) {
	    case 0:
		Bitmap bitmap = (Bitmap) msg.obj;
		sampleView.setImageBitmap(bitmap);
		break;
	    case 1:
		sampleView.setVisibility(VISIBLE);
		break;
	    case 2:
		sampleView.setVisibility(GONE);
	    }
	};
    };

    /* Constructor from layout */
    public FaceCameraSurface(Context context, AttributeSet attrs) {
	super(context, attrs);
	getHolder().addCallback(this);
	getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	stop = true;
    }

    /* Interface listener */
    public interface OnFaceDetectListener {
	public void onFaceDetect();
    }

    /* Set listener */
    public void setOnFaceDetectListener(OnFaceDetectListener listener) {
	faceListener = listener;
    }

    /* public methods */
    public void initializeAssets(File filesDir, AssetManager assets) {
	DATA_PATH = filesDir.getPath() + "/";
	makeDataDirectory(DETECTION_DATA_DIR);
	copyAssetsToData(assets, FACE_DETECTION_DATA_FILE);
	copyAssetsToData(assets, EYE_DETECTION_DATA_FILE,
		EYE_DETECTION_DATA_FILE_1, EYE_DETECTION_DATA_FILE_2);
    }

    public void start() {
	stop = false;
	sampleHandler.sendEmptyMessage(1);
    }

    public void stopSample() {
	stop = true;
	sampleHandler.sendEmptyMessage(2);
    }

    public void stopSearch() {
	stop = true;
    }

    /* SurfaceHolder.Callback methods implementations */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
	Log.i(TAG, "surfaceCreated");
	camera = Camera.open();
	if (camera != null) {
	    camera.setPreviewCallbackWithBuffer(this);
	    Parameters params = camera.getParameters();
	    params.setPreviewSize(frameWidth, frameHeight);
	    List<String> focusModes = params.getSupportedFocusModes();
	    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
	    }
	    camera.setParameters(params);
	    allocateBuffer();
	    setPreviewDisplay();
	    startPreview(frameWidth / reductCoef, frameHeight / reductCoef);
	}
	sampleView = ((ImageView) ((View) getParent())
		.findViewById(R.id.camera_sample));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

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
	if (!stop) {
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
	    if (bitmap != null) {
		sampleHandler.sendMessage(sampleHandler
			.obtainMessage(0, bitmap));
	    }
	}
    }

    /* Private methods */
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
	int w = frameWidth;
	int h = frameHeight;
	int size = w * h * bitsPerPixel / 8;
	buffer = new byte[size];
	camera.addCallbackBuffer(buffer);
    }

    private void setPreviewDisplay() {
    	
	try {
	
	    SurfaceView fakeview = (SurfaceView) ((View) getParent())
		    .findViewById(R.id.camera_surface);
		    
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
	int size = frameWidth / reductCoef * frameHeight / reductCoef
		+ (data.length - frameWidth * frameHeight);
	byte[] reduced = new byte[size];
	for (int i = 0, idx = 0; i < frameHeight; i++) {
	    if (i % 2 == 0) {
		for (int j = 0; j < frameWidth; j++) {
		    if (j % 2 == 1) {
			reduced[idx] = data[i * frameWidth + j];
			idx++;
		    }
		}
	    }
	}
	return reduced;
    }

    private Bitmap processFrame(byte[] data) {
	Log.i(TAG, "processFrame called");
	if (stop) {
	    Log.i(TAG, "processFrame stopped before end");
	    return null;
	} else {
	    Bitmap fullBitmap = getBitmapFromData(data);
	    if (rgba != null && bitmap != null) {
		faceDetection.getFaceRect(data);
		if (faceDetection.detectedFaceNumber > 0) {
		    Log.i(TAG, "face detected");
		    if (faceListener != null) {
			faceListener.onFaceDetect();
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
	int w = frameWidth / reductCoef;
	int h = frameHeight / reductCoef;
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