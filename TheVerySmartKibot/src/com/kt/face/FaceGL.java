package com.kt.face;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FaceGL {

	public static final int MSG_CLOSE = 1; 			//효과가 끝났다.
	public static final int MSG_GONEIMAGEVIEW = 3;	//이미지뷰를 지운다.
	public static final int MSG_STARTCURL = 4;		//효과를 시작한다.
	
	private static boolean D = true;
	
	/**
	 * JNI 함수들
	 * ------------------------------------------------------------------
	 * -------------
	 */
	static {
		System.loadLibrary("songgl");
	}

	protected static native long sglInitialize();

	protected static native long sglInitializeSmile();

	protected static native void sglRelease(long lGLContext);

	protected static native void sglRender(long lGLContext);

	protected static native int sglResize(long lGLContext, int nWidth,
			int nHeight);

	protected static native int sglResource(long lGLContext, String sPath);

	protected static native boolean sglEvent(long lGLContext, int nEventID,
			int param1, int param2);

	public static native int sglSetTexture(long lGLContext,Bitmap bitmap,String sKey);
	public static native void sglSetTextureCoordRate(long lGLContext,float fURate,float fVRat);

	public static native int sglGetCloseMsg();

	public static native int sglSetPageModel(long lGLContext,int nPage,byte[] btData,int nDataSize,byte[] btAniData,int nAniSize);

	public static native int sglSetTextureDir(long lGLContext,int nGroupIndex,String dir);
	public static native int sglSetIntervalTime(long lGLContext,int nMiddleTime,int nLastTime);
	public static native int sglSetDefaultTexture(long lGLContext,String sFilePath);

	protected static native void sglRequestRelease();
	protected static native int sglGetRequestReleaseStatus();
	protected static native void sglSetRequestReleaseStatus(int v);	

	/**
	 * Curl의 프레임의 속도를 조절한다. 1.0은 기본값    
	 * @param fRate fRate > 1.0은 빠르게 , fRate < 1.0 느리게 진행한다.
	 */
	public static native void sglSetCurlFrameRate(float fRate);
	// ------------------------------------------------------------------------------------------

	public long mlGLContext;

	public FaceGL() {
		mlGLContext = 0;
	}

	public boolean Initialize() {
		boolean bResult = true;
		mlGLContext = sglInitialize();
		if (mlGLContext == 0)
			bResult = false;
		return bResult;
	}

	//스마일 할때는 이것으로 초기화를 한다.
	public boolean InitializeSmile() {
		boolean bResult = true;
		mlGLContext = sglInitializeSmile();
		if (mlGLContext == 0)
			bResult = false;
		return bResult;
	}

	public void Release() {
		if (mlGLContext != 0) 
		{
			long TempContext = mlGLContext;

			if(D) Log.i("theK", String.format("Request = %d" , sglGetRequestReleaseStatus()));

			sglRequestRelease();
			try
			{
				mlGLContext = 0; //일단 자바상에서 모든 작업을 못하게 막는다.
				int nCnt = 0;
				while(true)
				{
					if(D) Log.i("theK",String.format("Loop Count = %d" , nCnt));
					Thread.sleep(200); //0.2초기다려서 실제적으로 메모리가 종료될때까지 기다린다.
					//메모리가 해제될때까지 기다린다.
					if(sglGetRequestReleaseStatus() == 2)
					{
						if(D) Log.i("theK",String.format("Closed = %d" , sglGetRequestReleaseStatus()));
						break;
					}
					if(nCnt >= 50)
					{
						if(D) Log.i("theK",String.format("NO = %d" , sglGetRequestReleaseStatus()));
						sglRelease(TempContext);
						mlGLContext = 0;
						break;
					}
					nCnt ++;
				}
//				sglRelease(TempContext);
			}
			catch(Exception ex)
			{

			}
			finally
			{
				sglSetRequestReleaseStatus(0);//메모리가 해제되었다고 마크해야한다.
				if(D) Log.i("theK", String.format("End = %d" , sglGetRequestReleaseStatus()));
			}
			//sglRelease(mlGLContext);
			//mlGLContext = 0;

		}
	}



	public void ReleaseDirectly() {
		try{
			if (mlGLContext != 0) 
			{	
				sglRelease(mlGLContext);
				mlGLContext = 0;	
			}
		}catch(Exception e){
			if(D) Log.e("theK", e.getMessage(), e);
		}
	}

	public void Render() {
		if (mlGLContext != 0) {
			sglRender(mlGLContext);
		}
	}

	public int Resize(int nWidth, int nHeight) {
		int nResult = 0;
		if (mlGLContext != 0) {
			nResult = sglResize(mlGLContext, nWidth, nHeight);
		}
		return nResult;

	}

	public int Resource(String sPath) {
		int nResult = 0;
		if (mlGLContext != 0) {
			nResult = sglResource(mlGLContext, sPath);
		}
		return nResult;
	}


	public int sglSetTexture(Bitmap btmap,String sKey)
	{
		int nResult = 0;
		if (mlGLContext != 0) {
			nResult = sglSetTexture(mlGLContext, btmap, sKey);
		}
		return nResult;
	}

	/**
	 * GSL_UP 0x000001 GSL_DOWN 0x000002 GSL_RIGHT 0x000004 GSL_LEFT 0x000008
	 * GSL_TRUNRIGHT 0x000010 GSL_TRUNLEFT 0x000020
	 * 
	 * @param lGLContext
	 * @param nEventID
	 * @param param1
	 * @param param2
	 * @return
	 */
	public boolean Event(int nEventID, int param1, int param2) {
		boolean bResult = true;
		if (mlGLContext != 0) {
			bResult = sglEvent(mlGLContext, nEventID, param1, param2);
		}
		return bResult;
	}

	public static Handler gHandlerforlcurlview = null;

	//C => Java를 호추할하는 이벤트 함수.
	public static void OnReceiveEvent(int nID)
	{
		if(gHandlerforlcurlview != null)
		{
			Message msg = new Message();
			msg.what = nID;
			msg.arg1 = 1;
			msg.arg1 = 2;
			gHandlerforlcurlview.sendMessage(msg);
		}
	}

}
