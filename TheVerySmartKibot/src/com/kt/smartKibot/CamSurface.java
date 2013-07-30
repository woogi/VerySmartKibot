package com.kt.smartKibot;

import java.io.IOException;
import java.util.Vector;

import android.content.Context;
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

public class CamSurface extends SurfaceView implements SurfaceHolder.Callback,
	PreviewCallback, Runnable {

    private static final String TAG = "CameraSurface";

    private static Context ctx;
    private static CamSurface instance;
    private static boolean reusing;

    private Bitmap bitmap;
    private byte[] buffer;
    private Camera camera;
    private byte[] data;
    private FaceDetection faceDetection;
    private OnFaceDetectListener faceListener;
    private boolean lost;
    private int[] rgba;
    private boolean stopSample, stopSearch;

    /* Constructor */
    public CamSurface(Context context) {
	super(context);
	ctx = context;
	getHolder().addCallback(this);
	getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	stopSample = true;
	stopSearch = true;
	/* UI */
	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 150);
	params.topMargin = 10;
	params.rightMargin = 10;
	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
	setLayoutParams(params);
	Message msg = new Message();
	msg.what = CamConf.ADD_CAM;
	msg.obj = this;
	RobotActivity.UIHandler.sendMessage(msg);
	setBackgroundColor(0xff000000);
	lost = true;
    }

    /* Interface listener */
    public interface OnFaceDetectListener {
	public void onFaceDetected(Bitmap bitmap, Vector<Rect> detectedPostions);

	public void onFaceLost();
    }

    /* Set listener */
    public void setOnFaceDetectListener(OnFaceDetectListener listener) {
	faceListener = listener;
    }

    /* public methods */
    public static CamSurface getInstance(Context context) {
	synchronized (context) {
	    if (instance == null) {
		instance = new CamSurface(context);
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
	if (reusing) {
	    Log.i(TAG, "reusing sample");
	    reusing = false;
	} else {
	    Log.i(TAG, "not reusing sample");
	    stopSample = true;
	    stopSearch = true;
	    /* Remove UI */
	    Message msg = new Message();
	    msg.what = CamConf.RM_VIEWS;
	    msg.obj = this;
	    RobotActivity.UIHandler.sendMessage(msg);
	    instance = null;
	}
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
	    /*
	     * This case can happen if the camera is open and closed too
	     * frequently.
	     */
	    camera = Camera.open();
	} catch (Exception e) {
	    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
	    Log.e(TAG, "surfaceCreated/ " + e.getMessage());
	    return;
	}

	if (camera != null) {
	    camera.setPreviewCallbackWithBuffer(this);
	    Parameters params = camera.getParameters();
	    params.setPreviewSize(CamConf.FRAME_WIDTH, CamConf.FRAME_HEIGHT);
	    params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
	    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
	    camera.setParameters(params);
	    allocateBuffer();
	    setPreviewDisplay();
	    startPreview(CamConf.FRAME_WIDTH / 2, CamConf.FRAME_HEIGHT / 2);
	}
	Message msg;
	/* Add Sample View */
	msg = new Message();
	msg.what = CamConf.ADD_SAMPLE;
	msg.obj = getLayoutParams();
	RobotActivity.UIHandler.sendMessage(msg);
	/* Add Face Rectangle View */
	msg = new Message();
	msg.what = CamConf.ADD_RECT;
	msg.obj = getLayoutParams();
	RobotActivity.UIHandler.sendMessage(msg);
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
	if (!stopSample) {
	    synchronized (this) {
		this.data = reducedData(data);
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
	    bitmap = processFrame(data);
	}
	if (bitmap != null) {
	    /* Draw Sample */
	    Message msg = new Message();
	    msg.what = CamConf.DRAW_SAMPLE;
	    msg.obj = bitmap;
	    RobotActivity.UIHandler.sendMessage(msg);
	}
    }

    /* Private methods */
    private void allocateBuffer() {
	int bitsPerPixel = ImageFormat.getBitsPerPixel(camera.getParameters()
		.getPreviewFormat());
	int w = CamConf.FRAME_WIDTH;
	int h = CamConf.FRAME_HEIGHT;
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
	try {
	    Log.i(TAG, "start preview");
	    camera.startPreview();
	} catch (RuntimeException e) {
	    Log.e(TAG, "Starting camera preview failed");
	}
	faceDetection = CamUtils.initFaceDetection(width, height);
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
	int size = CamConf.FRAME_WIDTH / 2 * CamConf.FRAME_HEIGHT / 2
		+ (data.length - CamConf.FRAME_WIDTH * CamConf.FRAME_HEIGHT);
	byte[] reduced = new byte[size];
	for (int i = 0, idx = 0; i < CamConf.FRAME_HEIGHT; i++) {
	    if (i % 2 == 0) {
		for (int j = 0; j < CamConf.FRAME_WIDTH; j++) {
		    if (j % 2 == 1) {
			reduced[idx] = data[i * CamConf.FRAME_WIDTH + j];
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
		Log.i(TAG, "processFrame --> search");
		faceDetection.getFaceRect(data);
		Vector<Rect> positions = new Vector<Rect>();
		if (faceDetection.detectedFaceNumber > 0) {
		    for (int i = 0; i < faceDetection.detectedFaceNumber; i++) {
			positions.add(faceDetection.detectedFacePostion[i]);
		    }
		    lost = false;
		    Log.i(TAG, "processFrame --> face detected");
		    if (faceListener != null) {
			faceListener.onFaceDetected(fullBitmap, positions);
		    }
		} else if (!lost) {
		    lost = true;
		    faceListener.onFaceLost();
		}
		Message msg = new Message();
		msg.what = CamConf.DRAW_RECT;
		msg.obj = positions;
		RobotActivity.UIHandler.sendMessage(msg);
	    }
	    return fullBitmap;
	}
    }

    private Bitmap getBitmapFromData(byte[] data) {
	Bitmap bitmap = this.bitmap;
	int[] rgba = this.rgba;
	int w = CamConf.FRAME_WIDTH / 2;
	int h = CamConf.FRAME_HEIGHT / 2;
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
