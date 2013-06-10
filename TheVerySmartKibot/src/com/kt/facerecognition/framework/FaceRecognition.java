
/// @file     FaceRecognition.java
/// @brief   FaceRecognition class, it is implemented for only face recognition.





package com.kt.facerecognition.framework;

import android.graphics.Bitmap;
import android.util.Log;

import com.kt.facerecognition.wrap.facerecognitionJNI;

public class FaceRecognition {
    private static final String TAG = "FaceRecognition";

/// @brief constructor
/// @param 1. String: trained data file.
/// @return N/A
    public FaceRecognition(String trainingfilepath)
    {
        //load training model data..
        facerecognitionJNI.initializeFR(trainingfilepath);
    }



/// @brief make training data file from face list(.csv) file
/// @param 1. String: face and classID list file
/// @param 2. String: output file path to save training data
/// @return void
    public static void startTrainingFaces(String csvFilePath, String outputFilePath)
    {
        facerecognitionJNI.startTraining(csvFilePath, outputFilePath);
    }



/// @brief get predicted result(=class ID)
/// @param 1. String: face image te be predicted, size(resolution) MUST be same with training data.
/// @return int: class ID will be returned, search in DB to get more information.
    public int getPredictClass(String inputImageFile)
    {
        int predictedclass = -1;
        predictedclass = facerecognitionJNI.getPredict(inputImageFile);
        Log.d(TAG, "getPredictClass = " + predictedclass);
        return predictedclass;
    }

/// @brief same with "public int getPredictClass(String inputImageFile)" except for input parameter is Bitmap object.
/// @param 1. Bitmap: bitmap object. It MUST be RGBA_8888 format.
/// @return int: class ID will be returned, search in DB to get more information.
    public int getPredictClass(Bitmap bmp)
    {
        int predictedclass = -1;
        predictedclass = facerecognitionJNI.getPredictBitmap(bmp);
        Log.d(TAG, "getPredictClass(bitmap) = " + predictedclass);
        return predictedclass;
    }

    public void close()
    {
        //facerecognitionJNI.finalizeFR();
    }

}
