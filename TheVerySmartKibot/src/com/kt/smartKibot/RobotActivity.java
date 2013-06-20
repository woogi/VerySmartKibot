package com.kt.smartKibot;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kt.face.ScreenSaverOpenGLSurface;


public class RobotActivity extends Activity implements OnUtteranceCompletedListener,IRobotEvtDelegator{

	private static final String TAG="RobotActivity";
	private RobotBrain brain;
	private ScreenSaverOpenGLSurface mSurface = null;
	private boolean DEBUG=true;
	private int currentFaceMode=RobotFace.MODE_UNKNOWN;
	IRobotEvtHandler touchEvtHandler=null;
	private static RelativeLayout mainLayout;
	private static Context ctx;
	private static FaceCameraSurface cameraSurface;
	private static ImageView sampleView;
	private static TextView logView;
	private static View modeIndicator=null;
	private static volatile boolean isEnd=false;
	
	
	
	private static final String baseFacePath = "/system/media/robot/face/";
	private static final String[] facePaths = { "/face15", "/face16", "/face14", "/face03",
			"/face13", "/face15", "/face08", "/face02", "/face01", "/face04",
			"/face09", "/face11", "/face12", "/face10", "/face05", "/face06",
			"/face07","/face20" };
	
	public static final String ACTION_CHANGE_FACE="com.kt.kibot.ChangeFace";
	public static final String ACTION_FINISH_FACE="com.kt.kibot.FinishFace";
	

	@Override
	public void finish() {
		isEnd=true;
		if (brain != null){ 
			brain.finalize();
		    brain=null;
		}
		
		RobotSpeech.finish();
		RobotMotion.finish();
		RobotFace.finish();
		super.finish();
	}



	@Override
	public void onHeadLongPressed() {
		super.onHeadLongPressed();
		if(DEBUG)
		{
			writeLog("head long pressed");
		}
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_HEAD_LONG_PRESSED);
		finish();
		
	}

	@Override
	public void onHeadPressed() {
		super.onHeadPressed();
		if(DEBUG)
		{
			writeLog("head pressed");
		}
		
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_HEAD_PRESSED);
		finish();
	}

	@Override
	public void onLeftEarPatted() {
		super.onLeftEarPatted();
		if(DEBUG)
		{
			writeLog("left ear patted");
		}
		
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_LEFT_EAR_PATTED);
	}
	
	@Override
	public void onRightEarPatted() {
		super.onRightEarPatted();
		if(DEBUG)
		{
			writeLog("right ear patted");
		}
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_RIGHT_EAR_PATTED);
	}


	@Override
	public void onLeftFootPressed() {
		super.onLeftFootPressed();
		if(DEBUG)
		{
			writeLog("left foot pressed");
		}
		
		TouchDetector.getInstance().sendEvent(this, TouchDetector.PARAM_LEFT_FOOT_PRESSED);
	}
	
	@Override
	public void onRightFootPressed() {
		super.onRightFootPressed();
		if(DEBUG)
		{
			writeLog("right foot pressed");
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
	
	static void setModeIndicatorColor(int color){
		
		if(isEnd) return;
		
    	modeIndicator.setBackgroundColor(color);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Log.d(TAG,"onCreate");
		
		isEnd=false;
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.main);
		
		ctx = this;
		mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
                // faceSurface = (FaceCameraSurface) findViewById(R.id.camera_surface);
                // faceSurface.initializeAssets(getFilesDir(), getAssets());
        	
        	logView = (TextView) findViewById(R.id.log_view);
        	((ScrollView) logView.getParent()).setVerticalScrollBarEnabled(false);
        	
    	modeIndicator=(View)findViewById(R.id.robot_mode);
    	
    	modeIndicator.setBackgroundColor(Color.YELLOW);
		
		Intent it=getIntent();
		
		//launcher 화면 에서  최초실행 
		if(true==it.getAction().equals("android.intent.action.MAIN"))
		{
		
			Log.d(TAG,"start with action.Main");
			// set main activity context
			RobotSpeech.getInstance(this);
			RobotMotion.getInstance(this);
			RobotFace.getInstance(this);
			
			//write asset data on file system.
		//	new UtilAssets(getApplicationContext(),"rmm").toFileSystem();
			
			brain=new RobotBrain(getApplicationContext());
			
			//screen touch event need refactoring.
			installHandler(brain);	
	
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
					if (event.getAction() == MotionEvent.ACTION_DOWN) 
					{
						Log.d(TAG, "touch face");
						
						touchEvtHandler.handle(getApplicationContext(), 
						new RobotEvent(RobotEvent.EVT_TOUCH_SCREEN));
												writeLog("event touch screen");
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
		
		if(message!=null) {
			writeLog(message);
		}
		
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
		Log.d(TAG,"onDestroy");
		
		clearAnimation();
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
	
	private static Handler layoutHandler = new Handler() {
		public void handleMessage(Message msg) {
		    switch (msg.what) {
		    case 0:
			if (cameraSurface != null){
        		    mainLayout.addView(cameraSurface);
			}
			break;
		    case 1:
			if (cameraSurface != null){
        		    cameraSurface.stopSample();
        		    mainLayout.removeView(cameraSurface);
			}
			break;
		    case 2:
			if (sampleView != null) {
        		    mainLayout.addView(sampleView);
			}
			break;
		    case 3:
			if (sampleView != null){
        		    mainLayout.removeView(sampleView);
        		    sampleView = null;
			}
			break;
		    case 4:
			if (sampleView != null) {
			    sampleView.setImageBitmap((Bitmap) msg.obj);
			}
			break;
		    }
		};
	    };
	
	public static FaceCameraSurface addCameraSurface() {
	    cameraSurface = new FaceCameraSurface(ctx);
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(180, 135);
	    params.topMargin = 10;
	    params.rightMargin = 10;
	    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
	    cameraSurface.setLayoutParams(params);
	    layoutHandler.sendEmptyMessage(0);
	    return cameraSurface;
	}

	public static void removeCameraSurface(){
	    layoutHandler.sendEmptyMessage(1);
	}

	public static ImageView addSampleView(){
	    sampleView = new ImageView(ctx);
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(180, 135);
	    params.topMargin = 10;
	    params.rightMargin = 10;
	    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
	    sampleView.setLayoutParams(params);
	    sampleView.setBackgroundColor(Color.BLACK);
	    layoutHandler.sendEmptyMessage(2);
	    return sampleView;
	}
	
	public static void removeSampleView(){
	    layoutHandler.sendEmptyMessage(3);
	}
    
        public static void displaySample(Bitmap bitmap) {
            layoutHandler.sendMessage(layoutHandler.obtainMessage(4, bitmap));
        }
	
	 public static void writeLog(String text){
		 
		if(isEnd) return;
		
	    logView.append(text + "\n");
	    ((ScrollView) logView.getParent()).fullScroll(View.FOCUS_DOWN);
	}

}
