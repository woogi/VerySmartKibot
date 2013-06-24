package com.kt.smartKibot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.util.Log;

import com.kt.facerecognition.framework.FaceRecognition;

public class FaceRecognizer implements IRobotEvtDelegator, CameraSurface.OnFaceDetectListener {

	private static final String TAG = "FaceRecognizer";

	private static CameraSurface cameraSurface;
	private static FaceRecognizer instance;

	// private IRobotEvtHandler handler;
	private String loggedName;
	private Rect reference;
	private int tolerance;

	@Override
	public void onFaceDetected(Bitmap bitmap, int detectedFaceNumber, Rect[] detectedFacePostion) {
		Log.i(TAG, "onFaceDetected");
		// if (cameraSurface != null) {
		// cameraSurface.stopSearch();
		// }
		// RobotEvent evt = new RobotEvent(RobotEvent.EVT_FACE_RECOGNITION);
		// handler.handle(null, evt);
		handleDetectedFaces(bitmap, detectedFaceNumber, detectedFacePostion);
	}

	@Override
	public void installHandler(IRobotEvtHandler handler) {
		Log.i(TAG, "installHandler");
		// this.handler = handler;
	}

	@Override
	public void uninstallHandler() {
		Log.i(TAG, "uninstallHandler");
		stop();
		// handler = null;
	}

	@Override
	public void start() {
		Log.i(TAG, "start");
		cameraSurface = CameraSurface.getInstance(RobotActivity.getContext());
		if (cameraSurface != null) {
			cameraSurface.setOnFaceDetectListener(this);
			cameraSurface.start();
		}
		/* init logged name */
		loggedName = null;
		/* init tolerance */
		int length = (Config.FRAME_HEIGHT / 2) / 2;
		tolerance = length / 3;
		/* init ground zone */
		int l = (Config.FRAME_WIDTH / 2) / 2 - length / 2;
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

	public static FaceRecognizer getInstance() {
		Log.i(TAG, "instance is " + instance);
		if (instance == null) {
			instance = new FaceRecognizer();
		}
		return instance;
	}

	private void handleDetectedFaces(Bitmap bitmap, int detectedFaceNumber, Rect[] detectedFacePostion) {
		Log.i(TAG, "handle detected faces (" + detectedFaceNumber + ")");
		Vector<DetectedFaceProperties> faces = new Vector<DetectedFaceProperties>();
		DetectedFaceProperties bestFace = new DetectedFaceProperties();
		for (int i = 0; i < detectedFaceNumber; i++) {
			DetectedFaceProperties face = new DetectedFaceProperties();
			face.rect = detectedFacePostion[i];
			/* for front camera do horizontal flip frame rectangle */
			face.rect = flip(face.rect);
			face.imageUri = saveCroppedFace(face.rect, bitmap);
			if (face.imageUri != null) {
				/* Returns cursor to browse db if known, null if not */
				Cursor cursorClass = isKnown(face.imageUri);
				if (cursorClass != null) {
					/*
					 * if face is known, cursor is not null. then browse in
					 * database to get tag name associated
					 */
					face.tagName = cursorClass.getString(cursorClass.getColumnIndex(CameraTagDatabase.KEY_TAG_NAME));
					face.friendLevel = Integer.parseInt(cursorClass.getString(cursorClass.getColumnIndex(CameraTagDatabase.KEY_FRIEND_LEVEL)));
					cursorClass.close();
				}
			}
			faces.add(face);
			bestFace = chooseBestFace(face, bestFace);
		}
		if (loggedName == null) {
			/* if no one is logged in, register new log name in database */
			if (bestFace.tagName == null) {
				// TODO add new face with new tag in database
			} else {
				loggedName = bestFace.tagName;
			}
		}
		if (bestFace != null) {
			letsMove(bestFace.rect);
		}
	}

	private Rect flip(Rect rect) {
		int rectWidth = rect.width();
		rect.left = Config.FRAME_WIDTH / 2 - rect.right;
		rect.right = rect.left + rectWidth;
		return rect;
	}

	private Uri saveCroppedFace(Rect faceFrame, Bitmap bitmap) {
		int x = faceFrame.left;
		int y = faceFrame.top;
		int w = faceFrame.width();
		int h = faceFrame.height();
		Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, w, h);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, Config.FACE_IMAGE_SIZE_W, Config.FACE_IMAGE_SIZE_H, true);
		String croppedFaceFilename = Config.DATA_PATH + "face.jpg";
		Uri croppedFaceUri = saveBitmapToFileCache(resizedBitmap, croppedFaceFilename);
		return croppedFaceUri;
	}

	private Cursor isKnown(Uri croppedFaceUri) {
		String capturedFaceFilePath = croppedFaceUri.getPath();
		FaceRecognition facerecognition = new FaceRecognition(Config.SAVE_FACES_TRAINING_RESULT_PATH);
		int predictedClass = facerecognition.getPredictClass(capturedFaceFilePath);
		facerecognition.close();
		Cursor cursorClass = null;
		/* if predectedClass > 0 face has been recognized */
		if (predictedClass > 0) {
			cursorClass = new CameraTagDatabase(RobotActivity.getContext()).getClassIDMatches(predictedClass);
		}
		/* return cursor to browse db and null if not known */
		return cursorClass;
	}

	private DetectedFaceProperties chooseBestFace(DetectedFaceProperties face, DetectedFaceProperties best) {
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
			Log.i(TAG, "LEFT"); // TODO LEFT
		} else if (Math.abs(deltaX) > 2 * Math.abs(deltaY) && deltaX < -2 * tolerance) {
			Log.i(TAG, "RIGHT"); // TODO RIGHT
		} else if (2 * Math.abs(deltaY) > Math.abs(deltaX) && deltaY > tolerance) {
			Log.i(TAG, "BACK"); // TODO BACK
		} else if (2 * Math.abs(deltaY) > Math.abs(deltaX) && deltaY < -tolerance) {
			Log.i(TAG, "FWD"); // TODO FWD
		} else {
			Log.i(TAG, "STOP"); // TODO STOP
		}
	}

	private Uri saveBitmapToFileCache(Bitmap bitmap, String strFilePath) {
		File fileCacheItem = new File(strFilePath);
		Uri savedUri = Uri.fromFile(fileCacheItem);
		OutputStream out = null;
		try {
			fileCacheItem.createNewFile();
			out = new FileOutputStream(fileCacheItem);
			bitmap.compress(CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return savedUri;
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
