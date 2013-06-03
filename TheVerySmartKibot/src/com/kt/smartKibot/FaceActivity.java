package com.kt.smartKibot;

import java.io.File;

import com.kt.face.ScreenSaverOpenGLSurface;

import android.app.Activity;
import android.content.ContentResolver;
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


public class FaceActivity extends Activity implements OnUtteranceCompletedListener{

	static final String TAG="Face Activity";
	AlarmAgent agent;
	RobotBrain brain;
	ScreenSaverOpenGLSurface mSurface = null;
	LinearLayout mlayout;
	private int curEmoId=-1;
	private ContentResolver mContentResolver;
	boolean DEBUG=true;
	boolean isOpenGLOn=false;
	
	private String[] mNames = { "EMO_MODE_FUN", "EMO_MODE_ATTENTION",
			"EMO_MODE_EATTING", "EMO_MODE_LOVE", "EMO_MODE_SAD",
			"EMO_MODE_EXCITED", "EMO_MODE_BRUSH", "EMO_MODE_YAWN",
			"EMO_MODE_FUN", "EMO_MODE_PLEASED", "EMO_MODE_WASHING",
			"EMO_MODE_SLEEP", "EMO_MODE_SERIOUS", "EMO_MODE_READING",
			"EMO_MODE_ANGRY", "EMO_MODE_WINK_LEFT", "EMO_MODE_WINK_RIGHT" };

	private String[] mFacePaths2 = { "/face15", "/face16", "/face14", "/face03",
			"/face13", "/face15", "/face08", "/face02", "/face01", "/face04",
			"/face09", "/face11", "/face12", "/face10", "/face05", "/face06",
			"/face07" };
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Log.d(TAG,"Face Activity onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Intent it=getIntent();
		
		
		//launcher 화면 에서  최초실행 
		if(true==it.getAction().equals("android.intent.action.MAIN"))
		{
			
		
			Log.d(TAG,"action --- action.Main");
			
			
			/* alarm 등록
			agent=new AlarmAgent();
			agent.init(getApplicationContext());
		
			*/
			
			brain=new RobotBrain(getApplicationContext());
	
			
			//alarm 등록
			//agent.regSchedule(brain);
			
			mContentResolver = getContentResolver();
			
		// set main activity context
			/*
			RobotSpeach.getSingleton(this);
			RobotMotion.getSingleton(this);
			RobotFace.getSingleton(this);
			*/
			
			/*
			initFaceAniView();
			
		
			if (!startFaceAnimation(getIntent())) {
				closeFaceAniView();
			}
			*/
			
			
			//finish();
			
		}

		
					
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void initFaceAniView() {
		mlayout = (LinearLayout) findViewById(R.id.layout);
		curEmoId=-1;
	}
	
	private boolean startFaceAnimation(Intent intent) {
		int tmp = intent.getIntExtra("_id", 0);
		
		Log.d(TAG, "  curEmoId:"+curEmoId+"  target:"+tmp);
		/*
		if (curEmoId == tmp) {
			return false;
		}
		*/
		
		curEmoId = tmp;

		// Base Path
		String sBasePath = "/system/media/robot/face/";

		
		// OpenGL에필요한파일들이있는경로
		String sModelPath = new StringBuilder(sBasePath).append("1").toString();
		/* todo getString 제대로 동작안함 다시 확인이 필요함
		// .append(2) // 검수버전
				.append(Settings.Etc.getString(mContentResolver, "kibotcolor"))// 정식
																				// 버전
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

		sAniFolder[0] = new StringBuilder(sModelPath).append(
				mFacePaths2[curEmoId]).toString();

		if (DEBUG)
			Log.i(TAG, sAniFolder[0]);

		if (mSurface == null) {

			if (DEBUG)
				Log.i(TAG, "mSurface init");

			mSurface = new ScreenSaverOpenGLSurface(this, sModelPath,
					sDefaultPageFileName, sAniFolder, nInterval, nLastInterver);
//todo change view
			mlayout.addView(mSurface);
			//setContentView(mSurface);

			mSurface.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
			//			startRecognition();
					}
					return true;
				}
			});

			isOpenGLOn = true;
			mSurface.Start();
			if (DEBUG)
				Log.i(TAG, "mSurface start");

		} else {

			isOpenGLOn = true;
			// 이미객체가생성되어있는경우폴더만지정한다.
			mSurface.Start(sDefaultPageFileName, sAniFolder);

			if (DEBUG)
				Log.i(TAG, "mSurface start :: " + sAniFolder[0]);
		}
		
		return true;
	}

	private void clearAnimation() {
		try {
			if(mSurface != null){
			mSurface.Stop();
			mSurface.Destory();
			curEmoId=-1;
			mSurface=null;
			}
		} catch (Exception e) {
			if (DEBUG)
				Log.e(TAG, e.getMessage(), e);
		}
	}
	private void closeFaceAniView() {
		mlayout = (LinearLayout) findViewById(R.id.layout);
		mlayout.setVisibility(View.INVISIBLE);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 1);
		mlayout.setLayoutParams(params);
		curEmoId=-1;
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG,"Face Activity onDestroy");
		clearAnimation();
		
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
		Log.d(TAG,"Face Activity onNewIntent!");
		
		setIntent(intent);
		super.onNewIntent(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.d(TAG,"Face Activity onResume!");
		// TODO Auto-generated method stub
		super.onResume();
		
		Intent it=getIntent();
		
		// set main activity context
		RobotSpeech.getInstance(this);
		RobotMotion.getInstance(this);
		RobotFace.getInstance(this);
		
		if(true==it.getAction().equals("com.kt.kibot.ChangeFace")){
			Log.d(TAG,"action -- kibot.ChangeFace");
						initFaceAniView();
			if (!startFaceAnimation(getIntent())) {
				closeFaceAniView();
			}
		}
	
		
		if(true==it.getAction().equals("com.kt.kibot.FinishFace")){
			Log.d(TAG,"action -- kibot.finishFace");
			
			finish();
		}
	}
	

}
