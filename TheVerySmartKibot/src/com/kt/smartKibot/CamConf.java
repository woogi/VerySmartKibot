package com.kt.smartKibot;

import android.os.Environment;

public class CamConf {

	public static final int FRAME_WIDTH = 640;
	public static final int FRAME_HEIGHT = 480;

	public static final int FACE_IMAGE_SIZE_W = 92;
	public static final int FACE_IMAGE_SIZE_H = 112;

	public static final int MAX_NUM_CLASS = 50;

	public static final int DUMMY_CLASS_ID = -100;

	public static final int STOP = 0;
	public static final int BACK = 1;
	public static final int FWD = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	public static final int LOST = 5;
	
	/* only for camera preview */
	private static final String SDCARD = Environment.getExternalStorageDirectory().getPath() + "/";
	public static final String SAVE_FACES_PATH = SDCARD + "facerecognition/smartKibot/camera/.project/.face/";
	public static final String SAVE_FACES_TRAINING_LIST_PATH = SDCARD + "facerecognition/smartKibot/camera/.project/.training.csv";
	public static final String SAVE_FACES_TRAINING_RESULT_PATH = SDCARD + "facerecognition/smartKibot/camera/.project/.modeldata.dat";

	public static String DATA_PATH;
	public static final String DETECTION_DATA_DIR = "detectiondata/";
	public static final String EYE_DETECTION_DATA_FILE = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses.xml";
	public static final String EYE_DETECTION_DATA_FILE_1 = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses1.xml";
	public static final String EYE_DETECTION_DATA_FILE_2 = DETECTION_DATA_DIR + "haarcascade_eye_tree_eyeglasses2.xml";
	public static final String FACE_DETECTION_DATA_FILE = DETECTION_DATA_DIR + "lbpcascade_frontalface.xml";
	
	public static final int RM_VIEWS = 0;
	public static final int ADD_CAM = 1;
	public static final int ADD_SAMPLE = 2;
	public static final int ADD_RECT = 3;
	public static final int DRAW_SAMPLE = 4;
	public static final int DRAW_RECT = 5;
}
