package com.kt.facerecognition.wrap;

import android.graphics.Bitmap;


public class facerecognitionJNI {

    static{
        try{
            System.loadLibrary("facerecognition");      
        } catch(Exception exception){
            throw new RuntimeException("Can not load facerecognition library",   exception);
        }
    }

    public static native void initializeFR(String trainingfilepath);
    public static native void startTraining(String csvFilePath, String outputFilePath);
    public static native int getPredict(String inputImageFile);
    public static native int getPredictBitmap(Bitmap bmp);
    public static native void finalizeFR(); 
}
