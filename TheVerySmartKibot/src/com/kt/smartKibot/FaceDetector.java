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
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.kt.facerecognition.framework.FaceDetection;

public class FaceDetector extends SurfaceView implements
	SurfaceHolder.Callback, Runnable, PreviewCallback {
    private static final String TAG = "CameraPreview";
    private static final String DETECTION_DATA_DIR = "detectiondata/";
    private static final String FACE_DETECTION_DATA_FILE = DETECTION_DATA_DIR + "lbpcascade_frontalface.xml";
    private static final String EYE_DETECTION_DATA_FILE = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses.xml";
    private static final String EYE_DETECTION_DATA_FILE_1 = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses1.xml";
    private static final String EYE_DETECTION_DATA_FILE_2 = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses2.xml";
    private static String DATA_PATH;
    
    private final int myReductCoef = 2;
    
    private Context myContext;
    private Camera myCamera;
    private int myWidth;
    private int myHeight;
    private byte[] myBuffer;
    private byte[] myData;
    private Bitmap myBitmap;
    private int[] myRGBA;
    private FaceDetection myFaceDetection;
    private OnFaceDetectListener myListener;
    private boolean stop;

    public FaceDetector(Context context, AttributeSet attrs) {
	super(context, attrs);
	
	myContext = context;

	getHolder().addCallback(this);
	getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	stop = true;
    }
    
    public interface OnFaceDetectListener {
   	public void onFaceDetect(Context context, FaceDetector faceDetector);
    }

    public void setOnFaceDetectListener(OnFaceDetectListener listener) {
   	myListener = listener;
    }
    
    public void initialize(File filesDir, AssetManager assets){
	DATA_PATH = filesDir.getPath() + "/";
	makeDataDirectory(DETECTION_DATA_DIR);
	copyAssetsToData(assets, FACE_DETECTION_DATA_FILE);
	copyAssetsToData(assets, EYE_DETECTION_DATA_FILE, EYE_DETECTION_DATA_FILE_1, EYE_DETECTION_DATA_FILE_2);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
	Log.i(TAG, "surfaceCreated");

	myCamera = Camera.open();
	
	if (myCamera != null) {
	    myCamera.setPreviewCallbackWithBuffer(this);

	    Parameters params = myCamera.getParameters();

	    myWidth = 640;
	    myHeight = 480;
	    params.setPreviewSize(myWidth, myHeight);

	    List<String> focusModes = params.getSupportedFocusModes();
	    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
	    }

	    myCamera.setParameters(params);

	    allocateBuffer();
	    setPreviewDisplay();

	    startPreview(params.getPreviewSize().width, params.getPreviewSize().height);
	}
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {  
       this.setMeasuredDimension(2, 2);  
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
	Log.i(TAG, "surfaceDestroyed");

	if (myCamera != null) {
	    synchronized (this) {
		myCamera.stopPreview();
		myCamera.setPreviewCallback(null);
		myCamera.release();
		myCamera = null;
	    }
	}

	stopPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
	if (!stop) {
	    synchronized (this) {
		myData = reducedData(data);
		this.notify();
	    }
	    new Thread(this).start();
	}

	camera.addCallbackBuffer(myBuffer);
    }

    @Override
    public void run() {
	synchronized (this) {
	    try {
		this.wait();
		processFrame(myData);
	    } catch (InterruptedException e) {
		Log.e(TAG, "Interrupted: " + e.getMessage());
	    }
	}
    }

    private byte[] reducedData(byte[] data) {
	int size = myWidth / myReductCoef * myHeight / myReductCoef
		+ (data.length - myWidth * myHeight);

	byte[] reduced = new byte[size];

	for (int i = 0, idx = 0; i < myHeight; i++) {
	    if (i % 2 == 0) {
		for (int j = 0; j < myWidth; j++) {
		    if (j % 2 == 1) {
			reduced[idx] = data[i * myWidth + j];
			idx++;
		    }
		}
	    }
	}

	return reduced;
    }

    private void processFrame(byte[] data) {
	if (!stop){
	    Log.i(TAG, "processFrame called");
	    
	    if (myRGBA != null && myBitmap != null) {

		myFaceDetection.getFaceRect(data);

		if (myFaceDetection.detectedFaceNumber > 0) {
		    Log.i(TAG, "face detected");
		    if (myListener != null) {
			myListener.onFaceDetect(myContext, this);
		    }
		}
	    }
	}
    }
    
    public void setStop(boolean stop) {
	this.stop = stop;
    }

    private void startPreview(int previewWidth, int previewHeight) {
	int width = previewWidth / myReductCoef;
	int height = previewHeight / myReductCoef;

	myRGBA = new int[width * height];

	try {
	    myBitmap = Bitmap.createBitmap(width, height,
		    Bitmap.Config.ARGB_8888);
	} catch (OutOfMemoryError e) {
	    Log.e(TAG, "Bitmap Out of Memory: " + e.getMessage());
	    myBitmap.recycle();
	    myBitmap = null;
	}

	initFaceDetection(width, height);

	try {
	    Log.i(TAG, "start preview");
	    myCamera.startPreview();
	    Log.i(TAG, "preview started");
	} catch (RuntimeException e) {
	    Log.e(TAG, "Starting camera preview failed");
	}
    }

    private void stopPreview() {
	if (myBitmap != null) {
	    myBitmap.recycle();
	    myBitmap = null;
	}

	myRGBA = null;

	if (myFaceDetection != null) {
	    myFaceDetection.close();
	}
    }

    private void initFaceDetection(int previewWidth, int previewHeight) {
	String facedetectiondata = DATA_PATH + FACE_DETECTION_DATA_FILE;
	String eyedetectiondata = DATA_PATH + EYE_DETECTION_DATA_FILE;

	/* new Face Detection instance */
	myFaceDetection = new FaceDetection();
	myFaceDetection.setTrainingFilePath(facedetectiondata, eyedetectiondata);
	myFaceDetection.setCamPreviewSize(previewWidth, previewHeight);
	myFaceDetection.setMinimumDetectionSize(1);

	/* Region Of Interest (ROI) */
	Rect roi = new Rect(0, 0, previewWidth, previewHeight);
	myFaceDetection.setROI(roi.left, roi.top, roi.right, roi.bottom);
    }

    private void allocateBuffer() {
	int bitsPerPixel = ImageFormat.getBitsPerPixel(myCamera.getParameters().getPreviewFormat());
	int width = myWidth;
	int height = myHeight;
	int size = width * height * bitsPerPixel / 8;

	myBuffer = new byte[size];
	myCamera.addCallbackBuffer(myBuffer);
    }

    private void setPreviewDisplay() {
	try {
	    SurfaceView fakeview = (SurfaceView) ((View) getParent()).findViewById(R.id.camera_surface);
	    fakeview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    myCamera.setPreviewDisplay(fakeview.getHolder());
	} catch (IOException e) {
	    Log.e(TAG, "Setting camera preview failed: " + e.getMessage());
	}
    }
    
    private void makeDataDirectory(String dirName) {
	File dir = new File(DATA_PATH + dirName);
	if (!dir.exists()){
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
}