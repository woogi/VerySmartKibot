package com.kt.smartKibot;

import java.io.IOException;

import android.app.Activity;
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

public class CamSurface extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback, Runnable {

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
	private int[] rgba;
	private boolean stopSample, stopSearch;

	/* Constructor */
	public CamSurface(Context context) {
		super(context);
		ctx = context;
		CamUtils.initializeAssets(ctx);
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
			msg.what = -1;
			msg.obj = this;
			RobotActivity.UIhandler.sendMessage(msg);
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
			params.setPreviewSize(CamConf.FRAME_WIDTH, CamConf.FRAME_HEIGHT);
			if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
			}
			camera.setParameters(params);
			allocateBuffer();
			setPreviewDisplay();
			startPreview(CamConf.FRAME_WIDTH / 2, CamConf.FRAME_HEIGHT / 2);
		}
		Message msg = new Message();
		msg.what = 2;
		msg.obj = getLayoutParams();
		RobotActivity.UIhandler.sendMessage(msg);
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
			Message msg = new Message();
			msg.what = 0;
			msg.obj = bitmap;
			RobotActivity.UIhandler.sendMessage(msg);
		}
	}

	/* Private methods */
	private void allocateBuffer() {
		int bitsPerPixel = ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat());
		int w = CamConf.FRAME_WIDTH;
		int h = CamConf.FRAME_HEIGHT;
		int size = w * h * bitsPerPixel / 8;
		buffer = new byte[size];
		camera.addCallbackBuffer(buffer);
	}

	private void setPreviewDisplay() {
		try {
			SurfaceView fakeview = this;
			fakeview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			camera.setPreviewDisplay(fakeview.getHolder());
		} catch (IOException e) {
			Log.e(TAG, "Setting camera preview failed: " + e.getMessage());
		}
	}

	private void startPreview(int width, int height) {
		rgba = new int[width * height];
		try {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
		int size = CamConf.FRAME_WIDTH / 2 * CamConf.FRAME_HEIGHT / 2 + (data.length - CamConf.FRAME_WIDTH * CamConf.FRAME_HEIGHT);
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
				if (faceDetection.detectedFaceNumber > 0) {
					Log.i(TAG, "processFrame --> face detected");
					if (faceListener != null) {
						faceListener.onFaceDetected(fullBitmap, faceDetection.detectedFaceNumber, faceDetection.detectedFacePostion);
					}
				} else {
					faceListener.onFaceLost();
				}
			}
			return fullBitmap;
		}
	}

	private void initFaceDetection(int previewWidth, int previewHeight) {
		String facedetectiondata = CamConf.DATA_PATH + CamConf.FACE_DETECTION_DATA_FILE;
		String eyedetectiondata = CamConf.DATA_PATH + CamConf.EYE_DETECTION_DATA_FILE;
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
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}
}