package com.kt.smartKibot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class UtilAssets {
	
	Context ctx;
	String targetPath;
	String assetPath;

	
	public UtilAssets(Context ctx,String assetPath){
		this.ctx=ctx;
		//this.targetPath="/data/data/" + ctx.getPackageName();
		this.targetPath="/sdcard";
		this.assetPath=assetPath;
	}
	
	public void toFileSystem(){
		copyFileOrDir(assetPath);
	}
	
	public String getFilePathOnFilSystem(String fileName){
		String fullPath=targetPath+"/"+assetPath+"/"+fileName;
		//String fullPath = "/system/media/robot/rmm/" + path;
		
		return fullPath;
	}
	
	private void copyFileOrDir(String path) {
	    AssetManager assetManager = ctx.getAssets();
	    String assets[] = null;
	    try {
	        assets = assetManager.list(path);
	        if (assets.length == 0) {
	            copyFile(path);
	        } else {
	            String fullPath = targetPath+"/"+assetPath;
	            //"/data/data/" + ctx.getPackageName() + "/" + path;
	            //String fullPath = "/system/media/robot/"+ path;
	            File dir = new File(fullPath);
	            if (!dir.exists())
	                dir.mkdir();
	            for (int i = 0; i < assets.length; ++i) {
	                copyFileOrDir(path + "/" + assets[i]);
	            }
	        }
	    } catch (IOException ex) {
	        Log.e("tag", "I/O Exception", ex);
	    }
	}

	private void copyFile(String filename) {
	    AssetManager assetManager = ctx.getAssets();

	    InputStream in = null;
	    OutputStream out = null;
	    try {
	        in = assetManager.open(filename);
	        //String newFileName = "/data/data/" +ctx.getPackageName() + "/" + filename;
	        String newFileName = targetPath+"/" + filename;
	        out = new FileOutputStream(newFileName);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        Log.e("tag", e.getMessage());
	    }

	}
}
