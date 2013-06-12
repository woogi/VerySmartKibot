package com.kt.smartKibot;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kt.face.ScreenSaverOpenGLSurface;


public class RobotActivity extends Activity implements OnUtteranceCompletedListener,IRobotEvtDelegator{

	private static final String TAG="RobotActivity";
	private RobotBrain brain;
	private ScreenSaverOpenGLSurface mSurface = null;
	private boolean DEBUG=true;
	private int currentFaceMode=RobotFace.MODE_UNKNOWN;
	IRobotEvtHandler touchEvtHandler=null;
	
	

	@Override
	public void onHeadLongPressed() {
		// TODO Auto-generated method stub
		//super.onHeadLongPressed();
		if(DEBUG)
		{
			Toast.makeText(getApplicationContext(),"head long pressed",Toast.LENGTH_SHORT).show();
		}
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_HEAD_LONG_PRESSED);
		finish();
		
	}

	@Override
	public void onHeadPressed() {
		// TODO Auto-generated method stub
		//super.onHeadPressed();
		if(DEBUG)
		{
			Toast.makeText(getApplicationContext(),"head pressed",Toast.LENGTH_SHORT).show();
		}
		
		
	//	TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_HEAD_PRESSED);
		finish();
	}

	@Override
	public void onLeftEarPatted() {
		// TODO Auto-generated method stub
		//super.onLeftEarPatted();
		if(DEBUG)
		{
			Toast.makeText(getApplicationContext(),"left ear patted",Toast.LENGTH_SHORT).show();
		}
		
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_LEFT_EAR_PATTED);
	}
	
	@Override
	public void onRightEarPatted() {
		// TODO Auto-generated method stub
		//super.onRightEarPatted();
		if(DEBUG)
		{
			Toast.makeText(getApplicationContext(),"right ear patted",Toast.LENGTH_SHORT).show();
		}
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_RIGHT_EAR_PATTED);
	}


	@Override
	public void onLeftFootPressed() {
		// TODO Auto-generated method stub
		//super.onLeftFootPressed();
		if(DEBUG)
		{
			Toast.makeText(getApplicationContext(),"left foot pressed",Toast.LENGTH_SHORT).show();
		}
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_LEFT_FOOT_PRESSED);
	}
	
	@Override
	public void onRightFootPressed() {
		// TODO Auto-generated method stub
		//super.onRightFootPressed();
		if(DEBUG)
		{
			Toast.makeText(getApplicationContext(),"right foot pressed",Toast.LENGTH_SHORT).show();
		}
	
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_RIGHT_FOOT_PRESSED);
	}


	
	@Override
	public void installHandler(IRobotEvtHandler handler) {
		// TODO Auto-generated method stub
		touchEvtHandler=handler;
	}

	@Override
	public void uninstallHandler() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	private static final String baseFacePath = "/system/media/robot/face/";
	private static final String[] facePaths = { "/face15", "/face16", "/face14", "/face03",
			"/face13", "/face15", "/face08", "/face02", "/face01", "/face04",
			"/face09", "/face11", "/face12", "/face10", "/face05", "/face06",
			"/face07" };
	
	public static final String ACTION_CHANGE_FACE="com.kt.kibot.ChangeFace";
	public static final String ACTION_FINISH_FACE="com.kt.kibot.FinishFace";
	
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		
		int _id=Integer.parseInt(utteranceId);
		if(_id>=RobotFace.UTT_ID_FACE_ACT_FACE_OFF_IMM && _id<=RobotFace.UTT_ID_FACE_ACT_FACE_OFF_60_SEC)
		{
			int interval=(_id-RobotFace.UTT_ID_FACE_ACT_FACE_OFF_IMM)*10*1000;
			
			if(interval!=0)
			{
				try{
				Thread.sleep(interval);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			finish();
			
		}
		
	}
	
	private static FaceDetector fd;
	public static FaceDetector getFaceDetector(){
	    return fd;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		fd = (FaceDetector)findViewById(R.id.camera_surface);
		fd.initialize(getFilesDir(), getAssets());

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Intent it=getIntent();
		
		
		//launcher 화면 에서  최초실행 
		if(true==it.getAction().equals("android.intent.action.MAIN"))
		{
		
			Log.d(TAG,"start with action.Main");
			
			//write asset data on file system.
			new UtilAssets(getApplicationContext(),"rmm").toFileSystem();
			
			brain=new RobotBrain(getApplicationContext());
	
		}

		
					
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private boolean startFaceAnimation(Intent intent) {
	
				int oldMode=currentFaceMode;
		int newMode=intent.getIntExtra("_id", 0);
		String message=intent.getStringExtra("_toast_message");
		
		if(DEBUG)
			Log.d(TAG,"try to change face from old id:"+oldMode+" to new id:"+newMode);
		
		
		
		// OpenGL에필요한파일들이있는경로
		String sModelPath = new StringBuilder(baseFacePath).append("1").toString();
		/* todo getString 제대로 동작안함 다시 확인이 필요함
		// .append(2) // 검수버전
			.append(Settings.Etc.getString(mContentResolver, "kibotcolor"))// 정식
			.toString();
		*/

		// 흰색 Default이미지
		String sDefaultPageFileName = new StringBuilder(sModelPath).append(
				"/1.tga").toString();

		// 파일 경로 체크
		if (!new File(sModelPath).exists()
				|| !new File(sDefaultPageFileName).exists()) {
			Log.d("faceimage", "face image load fail");
			return false;
		}

		// animation interval time
		int nInterval = 200;

		// 제일마지막 animation interval time
		int nLastInterver = 100;

		String[] sAniFolder = new String[3];

		currentFaceMode=newMode;
		sAniFolder[0] = new StringBuilder(sModelPath).append(
				facePaths[currentFaceMode]).toString();

		if (DEBUG)
			Log.i(TAG, sAniFolder[0]);

		if (mSurface == null) {

			if (DEBUG)
				Log.i(TAG, "mSurface init");

			mSurface = new ScreenSaverOpenGLSurface(this, sModelPath,
					sDefaultPageFileName, sAniFolder, nInterval, nLastInterver);

			LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
			layout.addView(mSurface);

			mSurface.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						Log.d(TAG, "touch face to finish activity");
						finish();
					}
					return true;
				}
			});

			mSurface.Start();
			
			if (DEBUG)
				Log.i(TAG, "mSurface start");

		}
		else 
		{

			// 이미객체가생성되어있는경우폴더만지정한다.
			mSurface.Start(sDefaultPageFileName, sAniFolder);

			if (DEBUG)
				Log.i(TAG, "mSurface start :: " + sAniFolder[0]);
		}
		
		if(message!=null)
			Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
		
		return true;
	}

	private void clearAnimation() {
		try {
			if(mSurface != null){
			mSurface.Stop();
			mSurface.Destory();
			mSurface=null;
			}
		} catch (Exception e) {
			if (DEBUG)
				Log.e(TAG, e.getMessage(), e);
		}
	}
	
	
	private void closeFaceAniView() {
		LinearLayout layout= (LinearLayout) findViewById(R.id.layout);
		layout.setVisibility(View.INVISIBLE);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 1);
		layout.setLayoutParams(params);
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onDestroy");
		clearAnimation();
		
		brain.finalize();
		
		RobotSpeech.finish();
		RobotMotion.finish();
		RobotFace.finish();
		
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onNewIntent");
		
		setIntent(intent);
		super.onNewIntent(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.d(TAG,"onResume");
		// TODO Auto-generated method stub
		super.onResume();
		
		Intent it=getIntent();
		
		// set main activity context
		RobotSpeech.getInstance(this);
		RobotMotion.getInstance(this);
		RobotFace.getInstance(this);
		
		if(true==it.getAction().equals(ACTION_CHANGE_FACE)){
			Log.d(TAG,"action -- kibot.ChangeFace");
						if (!startFaceAnimation(getIntent())) {
				closeFaceAniView();
			}
		}
	
		
		if(true==it.getAction().equals(ACTION_FINISH_FACE)){
			Log.d(TAG,"action -- kibot.finishFace");
			
			finish();
		}
	}
	

}
