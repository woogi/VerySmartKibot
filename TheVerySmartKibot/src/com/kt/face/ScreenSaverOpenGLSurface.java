package com.kt.face;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * @author  jwh0728
 */
public class ScreenSaverOpenGLSurface extends GLSurfaceView
{

	public static ByteArrayOutputStream gPage2Model = null;
	/**
	 * @uml.property  name="mRender"
	 * @uml.associationEnd  
	 */
	protected ScreenSaverRender mRender;
	private static final boolean D = true;

	public ScreenSaverOpenGLSurface(Context context,String sModelPath,String sDefaultPageFileName,String[] sAniFolder,int nInterval,int nLastInterver) {
		super(context);

		//모델을 메모리에 로딩한다.
		//        InitP2();

		mRender = new ScreenSaverRender(sModelPath,sDefaultPageFileName,sAniFolder,nInterval,nLastInterver);
		setRenderer(mRender);

		//2.3에서는 이렇게
		//mSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT); 
		//2.2에서는 이렇게
		this.getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
	}


	@Override
	public boolean onTouchEvent(final MotionEvent event) 
	{
		//렌더링에 영향을 주지 않기 위해서 큐에 이벤트가 쌓이게 하여 순차적으로 실행하게 끔한다.
		queueEvent(new Runnable()
		{
			public void run() 
			{
				if(mRender != null)
					mRender.onTouchEvent(event);
			}
		});
		return false;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {

		return super.onTrackballEvent(event);
	}


	/**
	 * 현재 설정된 상태로 실행하지 않고 재 초기화하여 실행한다.
	 * @param sDefaultPageFileName
	 * @param sAniFolder
	 * @return
	 */
	public boolean SubStart(String sDefaultPageFileName,String[] sAniFolder)
	{
		return mRender.Start(sDefaultPageFileName,sAniFolder,200,3000);
	}

	/**
	 * OpenGL Thread와 다르다.. queEvent에 넣어서 OpenGL Thread와 맞추어서 명령어를 넣어준다.
	 * @param sDefaultPageFileName
	 * @param sAniFolder
	 * @param nInterval
	 * @param nLastInterver
	 * @return
	 */
	public boolean Start(final String sDefaultPageFileName,final String[] sAniFolder)
	{
		queueEvent(new Runnable(){
			public void run() 
			{
				SubStart(sDefaultPageFileName,sAniFolder, 200,3000);
			}
		});
		return true;
	}


	/**
	 * 현재 설정된 상태로 실행하지 않고 재 초기화하여 실행한다.
	 * @param sDefaultPageFileName
	 * @param sAniFolder
	 * @param nInterval
	 * @param nLastInterver
	 * @return
	 */
	public boolean SubStart(String sDefaultPageFileName,String[] sAniFolder,int nInterval,int nLastInterver)
	{
		return mRender.Start(sDefaultPageFileName, sAniFolder, nInterval, nLastInterver);
	}

	/**
	 * OpenGL Thread와 다르다.. queEvent에 넣어서 OpenGL Thread와 맞추어서 명령어를 넣어준다.
	 * @param sDefaultPageFileName
	 * @param sAniFolder
	 * @param nInterval
	 * @param nLastInterver
	 * @return
	 */
	public boolean Start(final String sDefaultPageFileName,final String[] sAniFolder,final int nInterval,final int nLastInterver)
	{
		queueEvent(new Runnable(){
			public void run() 
			{
				SubStart(sDefaultPageFileName,sAniFolder, nInterval,nLastInterver);
			}
		});
		return true;
	}

	/**
	 * 현재 설정된 상태 그대로 실행한다.
	 * @return
	 */
	public boolean SubStart()
	{
		return mRender.Start();
	}

	/**
	 * OpenGL Thread와 다르다.. queEvent에 넣어서 OpenGL Thread와 맞추어서 명령어를 넣어준다.
	 * @return
	 */
	public boolean Start()
	{
		queueEvent(new Runnable(){
			public void run() 
			{
				SubStart();
			}
		});
		return true;
	}

	/**
	 * 레더링을 중지한다.
	 */
	public void Stop()
	{
		mRender.Stop();
	}


	public void Destory()
	{
		if(gPage2Model != null)
		{
			try 
			{
				gPage2Model.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			gPage2Model = null;
		}

		if(mRender != null)
		{
			try 
			{
				mRender.Destory();
				mRender = null;
			}catch(Exception e){
				if(D) Log.e("theK", e.getMessage(), e);
			}
		}
	}

	void readInputStreamAsByteArray(ByteArrayOutputStream outStream,InputStream is) throws IOException 
	{
		byte []buffer = new byte[1024];

		int len;
		while ((len = is.read(buffer)) > 0)
		{
			outStream.write(buffer, 0, len);
		}	
	}

	//	void InitP2()
	//    {
		//    	InputStream input = null;
	//    	ByteArrayOutputStream Page2Model = null;
	//        try 
	//        {
	//            input = getResources().openRawResource(R.raw.p2model);
	//            Page2Model = new ByteArrayOutputStream(1024); 
	//            readInputStreamAsByteArray(Page2Model,input);
	//            ScreenSaverRender.gbtModel = Page2Model.toByteArray();
	//        } 
	//        catch (IOException e) 
	//        {
	//            Log.e("Error", "P2model");
	//        } 
	//        finally 
	//        {
	//            try 
	//            {
	//                input.close();
	//                Page2Model.close();
	//            } 
	//            catch (IOException e) 
	//            {
	//                
	//            }
	//        }
	//    }


	@Override
	public void onPause() {
		Stop();
		super.onPause();
	}

	@Override
	public void onResume() {
		Start();
		super.onResume();
	}
}
