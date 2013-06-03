package com.kt.face;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author  jwh0728
 */
public class ScreenSaverRender implements GLSurfaceView.Renderer 
{
	public static    byte[] gbtModel;
	/**
	 * @uml.property  name="mSongGL"
	 * @uml.associationEnd  
	 */
	protected FaceGL mSongGL;
	protected boolean bLoaded;
	protected boolean bStarted;
	protected String msDefaultPageFileName;
	protected String[] msAniFolder;
	protected int mnInterval;
	protected int mnLastInterval;
	protected String msModelPath;
	
	private static final boolean D = false;
	
	
	public ScreenSaverRender(String sModelPath,String sDefaultPageFileName,String[] sAniFolder,int nInterval,int nLastInterver)
	{
		msDefaultPageFileName = sDefaultPageFileName;
		msAniFolder = sAniFolder;
		mnInterval = nInterval;
		mnLastInterval= nLastInterver;
		msModelPath = sModelPath;
		bLoaded = false;
		bStarted = false;
	}
	
	/**
	 * 현재 설정된 상태로 실행하지 않고 재 초기화하여 실행한다.
	 * @param sDefaultPageFileName
	 * @param sAniFolder
	 * @return
	 */
	public boolean Start(String sDefaultPageFileName,String[] sAniFolder)
	{
		return Start(sDefaultPageFileName,sAniFolder,200,3000);
	}
	
	/**
	 * 현재 설정된 상태로 실행하지 않고 재 초기화하여 실행한다.
	 * @param sDefaultPageFileName
	 * @param sAniFolder
	 * @param nInterval
	 * @param nLastInterver
	 * @return
	 */
	public boolean Start(String sDefaultPageFileName,String[] sAniFolder,int nInterval,int nLastInterver)
	{
		msDefaultPageFileName = sDefaultPageFileName;
		msAniFolder = sAniFolder;
		mnInterval = nInterval;
		mnLastInterval= nLastInterver;
		return Start();
	}
	
	/**
	 * 현재 설정된 상태 그대로 실행한다.
	 * @return
	 */
	public boolean Start()
	{
		if(bLoaded || bStarted) 
		{
			if(bLoaded && bStarted)
			{
				FaceGL.sglSetTextureCoordRate(0, 1.0f, 1.0f);
				
				//3>Interval Time을 셋한다.
				FaceGL.sglSetIntervalTime(0, mnInterval, mnLastInterval);
				
				//4>
				FaceGL.sglSetDefaultTexture(0, msDefaultPageFileName);
				
				for(int i = 0; i < msAniFolder.length; i++)
				{
					if(msAniFolder[i] != null)
						FaceGL.sglSetTextureDir(mSongGL.mlGLContext, i, msAniFolder[i]);
				}
				
				FaceGL.sglSetTextureDir(mSongGL.mlGLContext, -1, "end");
			}
			
			return true;
		}
		bStarted = true;
		
		//---------------------  리소스를 초기화 해준다.
		//1>모델을 읽어온다.
		//byte[] btP2Model = gbtModel;//CurlActivity.gPage2Model.toByteArray();
		//SongGL.sglSetPageModel(0, 1, btP2Model, btP2Model.length,btP2Model,0);
		
		//2>Animation 폴더를 셋한다.
		for(int i = 0; i < msAniFolder.length; i++)
		{
			if(msAniFolder[i] != null)
				FaceGL.sglSetTextureDir(0, i, msAniFolder[i]);
		}
		
		FaceGL.sglSetTextureCoordRate(0, 1.0f, 1.0f);
		

		//3>Interval Time을 셋한다.
		FaceGL.sglSetIntervalTime(0, mnInterval, mnLastInterval);
		
		//4>
		FaceGL.sglSetDefaultTexture(0, msDefaultPageFileName);
		//---------------------  리소스를 초기화 해준다.
		
		mSongGL = new FaceGL();
		//초기화를 한다.
		mSongGL.InitializeSmile();
		return true;
	}
	
	
	/**
	 * 레더링을 중지한다.
	 */
	public void Stop()
	{
		if(mSongGL != null)
		{
			mSongGL.Release();
			mSongGL = null;
			bLoaded = false;
		}
		bStarted = false;
	}
	

	@Override
	public void onDrawFrame(GL10 gl) {
		
		if(bLoaded == false && mSongGL != null)
		{	
			//Default Page를 읽어서 셋한다.
			mSongGL.Resource(msModelPath);
			bLoaded = true;
		}
		
		//렌더링을 한다.
		if(mSongGL != null) 
			mSongGL.Render();
		
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

		//뷰포트가 변환될때 발행한다.
		if(mSongGL != null) 
			mSongGL.Resize(width, height);
		
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		//소스세이프가 만들어질때 실행한다.
		Start();
	}
	
	
	public boolean onTouchEvent(MotionEvent event) 
	{
//		PointF ptNow = new PointF(event.getX(),event.getY());
//		if(event.getAction() == MotionEvent.ACTION_DOWN)
//		{	
//		}
//		else if(event.getAction() == MotionEvent.ACTION_MOVE)
//		{	
//		}
//		else if(event.getAction() == MotionEvent.ACTION_UP)
//		{
//		}
		return false;
	}
	
	
	public void Destory()
	{
		try 
		{
		if(mSongGL != null)
		{
			mSongGL.ReleaseDirectly();
			mSongGL = null;
			bLoaded = false;
		}
		}catch(Exception e){
			if(D) Log.e("theK", e.getMessage(), e);
		}
	}
}
