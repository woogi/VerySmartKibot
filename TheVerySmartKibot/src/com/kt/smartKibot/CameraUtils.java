package com.kt.smartKibot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.kt.facerecognition.framework.FaceRecognition;

public class CameraUtils {
	
	private static final String TAG = "CameraUtils";
	private static final String N = "nicolas";
	
	public static void initializeAssets(Context ctx) {
		File filesDir = ctx.getFilesDir();
		AssetManager assets = ctx.getAssets();
		Config.DATA_PATH = filesDir.getPath() + "/";
		makeDataDirectory(Config.DETECTION_DATA_DIR);
		copyAssetsToData(assets, Config.FACE_DETECTION_DATA_FILE);
		copyAssetsToData(assets, Config.EYE_DETECTION_DATA_FILE, Config.EYE_DETECTION_DATA_FILE_1, Config.EYE_DETECTION_DATA_FILE_2);
	}

	public static boolean registerName(CameraTagDatabase database, String inputName) {
		/* get input tag name */
		if (inputName == null || inputName.length() == 0) {
			Log.i(N, "Name is null or empty");
			return false;
		}

		/* check if name already used */
		Cursor cursorTag = database.getTagMatches(inputName);
		if (cursorTag != null) {
			String tag = cursorTag.getString(cursorTag.getColumnIndex(CameraTagDatabase.KEY_TAG_NAME));
			Log.i(N, inputName + " Looks Like " + tag);
			return false;
		}

		return true;
	}

	public static boolean registerImage(Uri imageUri, String destinationPath) {
		/* copy image file in training directory.. */
		boolean copied = false;
		if (imageUri != null) {
			String originalPath = imageUri.getPath();
			Log.i(N, "copy image from [" + originalPath + "] to [" + destinationPath + "]");
			copied = FileCopy(originalPath, destinationPath);
			if (!copied || originalPath == null || destinationPath == null) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static String getNextFileNum(String tartDirectory) {
		int count = 0;
		String strNextCount;
		checkProjectRootDirectory(tartDirectory);
		for (count = 0;; count++) {
			strNextCount = String.format(Locale.getDefault(), "%03d.jpg", count);
			File file = new File(tartDirectory + strNextCount);
			if (!file.exists()) {
				return strNextCount;
			}
		}
	}
	
	public static void addItemToDatabase(CameraTagDatabase database, String tagName, String imagePath, int friendLevel) {
		/* get empty class ID or find class ID by tagName in database */
		int[] idAndCount = findClassId(database, tagName);
		int classId = idAndCount[0];
		int tagCount = idAndCount[1];
		/*
		 * tag add in database (int classID, String tagName, int countInClass,
		 * String dirPath, String imageFilepath)
		 */
		database.addItem(classId, tagName, tagCount, Config.SAVE_FACES_PATH, imagePath, friendLevel);
		/* make training list file and start training data */
		if (makeCameraTrainingCSV(database) == true) {
			FaceRecognition.startTrainingFaces(Config.SAVE_FACES_TRAINING_LIST_PATH, Config.SAVE_FACES_TRAINING_RESULT_PATH);
		}
	}
	
	private static void makeDataDirectory(String dirName) {
		File dir = new File(Config.DATA_PATH + dirName);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	private static void copyAssetsToData(AssetManager assets, String assetFileName) {
		try {
			InputStream in = assets.open(assetFileName);
			OutputStream out = new FileOutputStream(Config.DATA_PATH + assetFileName);
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

	private static void copyAssetsToData(AssetManager assets, String destFileName, String assetFileName1, String assetFileName2) {
		try {
			InputStream inPart1 = assets.open(assetFileName1);
			InputStream inPart2 = assets.open(assetFileName2);
			OutputStream out = new FileOutputStream(Config.DATA_PATH + destFileName);
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
	
	private static boolean FileCopy(String originalfilepath, String destinationfilepath) {
		File sourceFile = new File(originalfilepath);
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(sourceFile);
			outputStream = new FileOutputStream(destinationfilepath);
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage());
			}
			try {
				inputStream.close();
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage());
			}
		}
		return true;
	}
	
	private static void checkProjectRootDirectory(String tartDirectory) {
		File checkDirFile = new File(tartDirectory);
		if (!checkDirFile.exists()) {
			checkDirFile.mkdirs();
		}
	}
	
	private static int[] findClassId(CameraTagDatabase database, String tagName) {
		int classId = -1;
		int tagCountInClass = -1;
		Cursor cursorTag = database.getTagMatches(tagName);
		if (cursorTag == null) {
			/* find empty class ID [start] */
			for (int i = 1;; i++) {
				Cursor cursorClass = database.getClassIDMatches(i);
				if (cursorClass == null) {
					classId = i;
					tagCountInClass = 1;
					break;
				} else {
					cursorClass.close();
				}
			}
			/* find empty class ID [end] */
		} else {
			/* find saved class ID same with input tagName... */
			classId = cursorTag.getInt(cursorTag.getColumnIndex(CameraTagDatabase.KEY_CLASS_ID));
			tagCountInClass = cursorTag.getCount();
			cursorTag.close();
		}
		Log.i(TAG, "class id found is " + classId);
		Log.i(TAG, "tag count in class is " + tagCountInClass);
		int[] idAndCount = { classId, tagCountInClass };
		return idAndCount;
	}
	
	private static boolean makeCameraTrainingCSV(CameraTagDatabase database) {
		Cursor cursor = database.getPathNClassID();
		boolean status = false;
		if (cursor != null) {
			String dumpFilePath = Config.SAVE_FACES_TRAINING_LIST_PATH;
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(dumpFilePath));
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					String strOut = cursor.getString(cursor.getColumnIndex(CameraTagDatabase.KEY_TRAINING_IMAGE_PATH)) + ";" + cursor.getInt(cursor.getColumnIndex(CameraTagDatabase.KEY_CLASS_ID));
					out.write(strOut);
					out.newLine();
					cursor.moveToNext();
				}
				out.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			cursor.close();
			status = true;
		} else {
			Log.e(TAG, "makeCameraTrainingCSV cursor is null, check database");
		}
		return status;
	}
}
