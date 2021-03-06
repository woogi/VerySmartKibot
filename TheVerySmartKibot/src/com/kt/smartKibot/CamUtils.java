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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.util.Log;

import com.kt.facerecognition.framework.FaceDetection;
import com.kt.facerecognition.framework.FaceRecognition;

public class CamUtils {

    private static final String TAG = "CameraUtils";

    public static FaceDetection initFaceDetection(int previewWidth,
	    int previewHeight) {
	FaceDetection faceDetection = new FaceDetection();
	faceDetection.setTrainingFilePath(CamConf.DATA_PATH
		+ CamConf.FACE_DETECTION_DATA_FILE, CamConf.DATA_PATH
		+ CamConf.EYE_DETECTION_DATA_FILE);
	faceDetection.setCamPreviewSize(previewWidth, previewHeight);
	faceDetection.setMinimumDetectionSize(1);
	faceDetection.setROI(0, 0, previewWidth, previewHeight);
	return faceDetection;
    }

    public static void resetTargetsFolder() {
	CamUtils.recursiveDeletion(new File(CamConf.SAVE_TARGETS_PATH));
	new File(CamConf.SAVE_TARGETS_PATH).mkdirs();
    }

    public static Bitmap adjustedBrightness(Bitmap src, float value) {
	Bitmap alteredBitmap = Bitmap.createBitmap(src.getWidth(),
		src.getHeight(), src.getConfig());
	Canvas canvas = new Canvas(alteredBitmap);
	Paint paint = new Paint();
	ColorMatrix cm = new ColorMatrix();
	float b = value;
	cm.set(new float[] { 1, 0, 0, 0, b,
	/*		   */0, 1, 0, 0, b,
	/*		   */0, 0, 1, 0, b,
	/*                 */0, 0, 0, 1, 0 });

	paint.setColorFilter(new ColorMatrixColorFilter(cm));
	Matrix matrix = new Matrix();
	canvas.drawBitmap(src, matrix, paint);

	return alteredBitmap;
    }

    public static void appendCameraTrainingCSV(String cvsFile, String line) {
	File file = CamUtils.createFile(cvsFile);
	if (file != null) {
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(cvsFile,
			true));
		out.write(line);
		out.newLine();
		out.close();
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
	    }
	}
    }

    public static void encodeYUV420SP(byte[] data, int[] argb, int width,
	    int height) {
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

    public static void saveBitmapToFile(Bitmap bitmap, String filePath) {
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

    public static Uri saveCroppedFace(Rect faceFrame, Bitmap bitmap) {
	int x = faceFrame.left;
	int y = faceFrame.top;
	int w = faceFrame.width();
	int h = faceFrame.height();
	Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, w, h);
	Bitmap resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap,
		CamConf.FACE_IMAGE_SIZE_W, CamConf.FACE_IMAGE_SIZE_H, true);
	String croppedFaceFilename = CamConf.DATA_PATH + "face.jpg";
	Uri croppedFaceUri = saveBitmapToFileCache(resizedBitmap,
		croppedFaceFilename);
	return croppedFaceUri;
    }

    private static Uri saveBitmapToFileCache(Bitmap bitmap, String strFilePath) {
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

    public static Bitmap cropFace(Bitmap picture, Rect rect) {
	Bitmap croppedFace = Bitmap.createBitmap(picture, rect.left, rect.top,
		rect.width(), rect.height());
	Bitmap scaledFace = Bitmap.createScaledBitmap(croppedFace,
		CamConf.FACE_IMAGE_SIZE_W, CamConf.FACE_IMAGE_SIZE_H, true);
	return scaledFace;
    }

    public static File createFile(String filePath) {
	try {
	    File file = new File(filePath);
	    File dir = file.getParentFile();
	    if (!dir.exists()) {
		dir.mkdirs();
	    }
	    if (!file.exists()) {
		file.createNewFile();
	    }
	    return file;
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
	return null;
    }

    public static void recursiveDeletion(File file) {
	if (file.isDirectory()) {
	    for (File child : file.listFiles()) {
		recursiveDeletion(child);
	    }
	}
	file.delete();
    }

    public static void initializeAssets(Context ctx) {
	File filesDir = ctx.getFilesDir();
	AssetManager assets = ctx.getAssets();
	CamConf.DATA_PATH = filesDir.getPath() + "/";
	makeDataDirectory(CamConf.DETECTION_DATA_DIR);
	copyAssetsToData(assets, CamConf.FACE_DETECTION_DATA_FILE);
	copyAssetsToData(assets, CamConf.EYE_DETECTION_DATA_FILE,
		CamConf.EYE_DETECTION_DATA_FILE_1,
		CamConf.EYE_DETECTION_DATA_FILE_2);
    }

    public static boolean registerName(CamDatabase database, String inputName) {
	/* get input tag name */
	if (inputName == null || inputName.length() == 0) {
	    Log.i(TAG, "Name is null or empty");
	    return false;
	}

	/* check if name already used */
	Cursor cursorTag = database.getTagMatches(inputName);
	if (cursorTag != null) {
	    String tag = cursorTag.getString(cursorTag
		    .getColumnIndex(CamDatabase.KEY_TAG_NAME));
	    Log.i(TAG, inputName + " Looks Like " + tag);
	    return false;
	}

	return true;
    }

    public static boolean registerImage(Uri imageUri, String destinationPath) {
	/* copy image file in training directory.. */
	boolean copied = false;
	if (imageUri != null) {
	    String originalPath = imageUri.getPath();
	    Log.i(TAG, "copy image from [" + originalPath + "] to ["
		    + destinationPath + "]");
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
	    strNextCount = String.format(Locale.getDefault(), "%03d", count);
	    File file = new File(tartDirectory + strNextCount);
	    if (!file.exists()) {
		return strNextCount;
	    }
	}
    }

    public static void addItemToDatabase(CamDatabase database, String tagName,
	    String imagePath, int friendLevel) {
	/* get empty class ID or find class ID by tagName in database */
	int[] idAndCount = findClassId(database, tagName);
	int classId = idAndCount[0];
	int tagCount = idAndCount[1];
	/*
	 * tag add in database (int classID, String tagName, int countInClass,
	 * String dirPath, String imageFilepath)
	 */
	database.addItem(classId, tagName, tagCount, CamConf.SAVE_FACES_PATH,
		imagePath, friendLevel);
	/* make training list file and start training data */
	if (makeCameraTrainingCSV(database) == true) {
	    FaceRecognition.startTrainingFaces(
		    CamConf.SAVE_FACES_TRAINING_LIST_PATH,
		    CamConf.SAVE_FACES_TRAINING_RESULT_PATH);
	}
	logInAs(database, tagName);
    }

    public static void logInAs(CamDatabase database, String tagName) {
	Cursor cursorTag = database.getTagMatches(tagName);
	String imagePath = null;
	int friendlevel = -1;

	if (cursorTag != null) {
	    friendlevel = Integer.parseInt(cursorTag.getString(cursorTag
		    .getColumnIndex(CamDatabase.KEY_FRIEND_LEVEL)));
	    imagePath = cursorTag.getString(cursorTag
		    .getColumnIndex(CamDatabase.KEY_TRAINING_IMAGE_PATH));
	    cursorTag.close();
	    friendlevel++;

	    int[] idAndCount = findClassId(database, tagName);
	    int classId = idAndCount[0];
	    int countInClass = idAndCount[1];

	    /* remove from db */
	    database.removeItem(String.valueOf(classId));

	    /*
	     * tag add in database (int classID, String tagName, int
	     * countInClass, String dirPath, String imageFilepath)
	     */
	    database.addItem(classId, tagName, countInClass,
		    CamConf.SAVE_FACES_PATH, imagePath, friendlevel);
	}
    }

    private static void makeDataDirectory(String dirName) {
	File dir = new File(CamConf.DATA_PATH + dirName);
	if (!dir.exists()) {
	    dir.mkdir();
	}
    }

    private static void copyAssetsToData(AssetManager assets,
	    String assetFileName) {
	try {
	    InputStream in = assets.open(assetFileName);
	    OutputStream out = new FileOutputStream(CamConf.DATA_PATH
		    + assetFileName);
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

    private static void copyAssetsToData(AssetManager assets,
	    String destFileName, String assetFileName1, String assetFileName2) {
	try {
	    InputStream inPart1 = assets.open(assetFileName1);
	    InputStream inPart2 = assets.open(assetFileName2);
	    OutputStream out = new FileOutputStream(CamConf.DATA_PATH
		    + destFileName);
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

    private static boolean FileCopy(String originalfilepath,
	    String destinationfilepath) {
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

    private static int[] findClassId(CamDatabase database, String tagName) {
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
	    classId = cursorTag.getInt(cursorTag
		    .getColumnIndex(CamDatabase.KEY_CLASS_ID));
	    tagCountInClass = cursorTag.getCount();
	    cursorTag.close();
	}
	Log.i(TAG, "class id found is " + classId);
	Log.i(TAG, "tag count in class is " + tagCountInClass);
	int[] idAndCount = { classId, tagCountInClass };
	return idAndCount;
    }

    private static boolean makeCameraTrainingCSV(CamDatabase database) {
	Cursor cursor = database.getPathNClassID();
	boolean status = false;
	if (cursor != null) {
	    String dumpFilePath = CamConf.SAVE_FACES_TRAINING_LIST_PATH;
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(
			dumpFilePath));
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
		    String strOut = cursor
			    .getString(cursor
				    .getColumnIndex(CamDatabase.KEY_TRAINING_IMAGE_PATH))
			    + ";"
			    + cursor.getInt(cursor
				    .getColumnIndex(CamDatabase.KEY_CLASS_ID));
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
