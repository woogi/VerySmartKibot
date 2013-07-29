package com.kt.smartKibot;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.format.Time;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
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
	private IRobotEvtHandler evtHandler=null;
	private static FaceRectangle faceRectangle=null;
	private static RelativeLayout mainLayout;
	private static Context ctx;
	private static ImageView sampleView;
	
	private static TextView logView;
	private static TextView modeIndicator=null;
	private ImageView earImage=null;
	private ImageView logInImage=null;
	private static volatile boolean isEnd=false;
	GestureDetector gestureDetector=null;
	SpeechRecognizer speechRecognizer=null;
	public static final int ACTION_CHECK_NOISE=0;
	public static final int ACTION_START_LISTEN=1;
	public static final int END_ACTION_CHECK_NOISE=2;
	public static final int END_ACTION_LISTEN=3;
	public static final int UPDATE_LOGIN_UI=4;
	volatile int curAction=-1;
	
	
	private static final String baseFacePath = "/system/media/robot/face/";
	private static final String[] facePaths = { "/face15", "/face16", "/face14", "/face03",
			"/face13", "/face15", "/face08", "/face02", "/face01", "/face04",
			"/face09", "/face11", "/face12", "/face10", "/face05", "/face06",
			"/face07","/face20" };
	
	public static final String ACTION_CHANGE_FACE="com.kt.kibot.ChangeFace";
	public static final String ACTION_FINISH_FACE="com.kt.kibot.FinishFace";
	
	BlinkingEarThread blinkingEarThread=null;
	
	
	class BlinkingEarThread extends Thread{
		
		boolean isStop=false;
		
		public  void run() {
			// TODO Auto-generated method stub
			
			isStop=false;
			while(!isStop)
			{
				
				 runOnUiThread(new Runnable(){
			            @Override
			             public void run() {
			            	showEar(true);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							showEar(false);
			             }
			        });
				
				
			}

		}
		
		public void stopIt(){
			isStop=true;
		}
		
	};
	
	private void startBlinkingEar(){
		
		blinkingEarThread=new BlinkingEarThread();
		
		blinkingEarThread.start();
	
	
	}
	
	private void stopBlinkingEar(){
	
		if(blinkingEarThread!=null)
		{
			blinkingEarThread.stopIt();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			blinkingEarThread=null;
		}
		
	}
	
	private void setLogInIndicator(boolean logIn){
		
		if(logIn)
		{
			logInImage.setImageResource(R.drawable.in);
		}else{
			logInImage.setImageResource(R.drawable.out);
		}
		
	}

	private void showEar(boolean show){
		if(earImage!=null){
			if(show==true){
			earImage.setVisibility(View.VISIBLE);
			}else{
				earImage.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private Handler mainHandler=new Handler() {
		int arg1=-1;
		int arg2=-1;
		
	
		public void handleMessage(Message m) {
			if(m.what==ACTION_CHECK_NOISE) {
				
				
				//start speech recognizer
				Log.d(TAG,"handleMessge ,ACTION_CHECK_NOISE");
				
				if(speechRecognizer!=null){
					speechRecognizer.stopListening();
					speechRecognizer.cancel();
					speechRecognizer.destroy();
					speechRecognizer=null;
				}
				
				speechRecognizer=SpeechRecognizer.createSpeechRecognizer(RobotActivity.this);
				speechRecognizer.setRecognitionListener(recognitionListener);
				
				Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
				intent.putExtra(RecognizerIntent.DETAILS_META_DATA,"CHECK_NOISE");
		        if(speechRecognizer!=null) speechRecognizer.startListening(intent);
		        
				curAction=ACTION_CHECK_NOISE;
		        
		        arg1=m.arg1;
		        
			}
			else if(m.what==END_ACTION_CHECK_NOISE) {
				Log.d(TAG,"handleMessge ,END_ACTION_CHECK_NOISE");
				
				if(speechRecognizer!=null) {
					Log.d(TAG,"try to destory speechRecognizer");
					speechRecognizer.stopListening();
					speechRecognizer.cancel();
					speechRecognizer.destroy();
					speechRecognizer=null;
					Log.d(TAG,"end of speech recognition");
				}
				
				arg2=m.arg1;
				
				curAction=END_ACTION_CHECK_NOISE;
				
				RobotChatting.getInstance(RobotActivity.this).onResultCheckNoise(arg1, arg2);
				
			}
			else if(m.what==ACTION_START_LISTEN){
				Log.d(TAG,"handleMessge ,ACTION_START_LISTEN");
				
				if(speechRecognizer!=null){
					speechRecognizer.stopListening();
					speechRecognizer.cancel();
					speechRecognizer.destroy();
					speechRecognizer=null;
				}
				
				speechRecognizer=SpeechRecognizer.createSpeechRecognizer(RobotActivity.this);
				speechRecognizer.setRecognitionListener(recognitionListener);
				
				Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
				intent.putExtra(RecognizerIntent.DETAILS_META_DATA,"CHECK_NOISE");
		        if(speechRecognizer!=null) speechRecognizer.startListening(intent);
		        
				curAction=ACTION_START_LISTEN;
				
				
			}else if(m.what==END_ACTION_LISTEN){
				Log.d(TAG,"handleMessge ,END_ACTION_LISTEN");
				
				if(speechRecognizer!=null) {
					Log.d(TAG,"try to destory speechRecognizer");
					speechRecognizer.stopListening();
					speechRecognizer.cancel();
					speechRecognizer.destroy();
					speechRecognizer=null;
					Log.d(TAG,"end of speech recognition");
				}
				
				int isOK=m.arg1;
				
				curAction=END_ACTION_LISTEN;
				RobotChatting.getInstance(RobotActivity.this).onResultListen(isOK,(ArrayList<String>)m.obj);
				
			}else if(m.what==UPDATE_LOGIN_UI){
				
				if(null==LogIn.getInstance().whosLogIn()){
					setLogInIndicator(false);
				}else{
					setLogInIndicator(true);
				}
				
			}
		}
		
		
	};
	
	
	RecognitionListener recognitionListener=new RecognitionListener() {
	
		
		public void onReadyForSpeech(Bundle arg0) {
			Log.d(TAG,"onReadyForSpeech()...");
			RobotMotion.getInstance(ctx).led(0, 1, 3);
			
			if(curAction==ACTION_START_LISTEN){
			MediaPlayer mp = MediaPlayer.create(ctx, R.raw.ding);  
			  mp.start();
			}
		}
		
		public void onBeginningOfSpeech() {
			Log.d(TAG,"onBeginningOfSpeech()...");
			
			showEar(true);
			//startBlinkingEar();
		}

		public void onEndOfSpeech() {
			Log.d(TAG,"onEndOfSpeech()...");
			
			showEar(false);
			//stopBlinkingEar();
			RobotMotion.getInstance(ctx).offAllLed();

		}

		public void onResults(Bundle bundle) {
			Log.d(TAG,"onResults()...");
			
			
			ArrayList<String> results=bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			for(int i=0;i<results.size();i++)
				Log.d(TAG,"result("+i+"): "+results.get(i));
			
			if(curAction==ACTION_CHECK_NOISE)
			{
				mainHandler.sendMessage(mainHandler.obtainMessage(END_ACTION_CHECK_NOISE,NoiseDetector.PARAM_KIND_HUMAN_VOICE, 0));
			}
			else if(curAction==ACTION_START_LISTEN){
				mainHandler.sendMessage(mainHandler.obtainMessage(END_ACTION_LISTEN, 1,0, (Object)results));
			}
			
			
			
			showEar(false);
			
		}
		
		public void onError(int arg0) {
			Log.d(TAG,"onError()...");
			
			if(curAction==ACTION_CHECK_NOISE)
				mainHandler.sendMessage(mainHandler.obtainMessage(END_ACTION_CHECK_NOISE,NoiseDetector.PARAM_KIND_NOT_FROM_HUMAN,0));
			else if(curAction==ACTION_START_LISTEN){
				mainHandler.sendMessage(mainHandler.obtainMessage(END_ACTION_LISTEN, 0,0,null));
			}
			
			
			
			showEar(false);
		}
		
		public void onBufferReceived(byte[] arg0) { }
		public void onEvent(int arg0, Bundle arg1) {
			Log.d("KibotTest","onEvent()...");
		}
		public void onPartialResults(Bundle arg0) {
			Log.d("KibotTest","onPartialResults()...");
		}
		public void onRmsChanged(float arg0) { 
	//		Log.d("KibotTest","rms:"+arg0);
		}
		
    };
	
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
	//	RobotChatting.finish();
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
		evtHandler=handler;
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
	
	static boolean isLogScreenShowing(){
		if(isEnd) return false;
		
    	if(logView.getVisibility()==View.VISIBLE){
    		return true;
    	}
    	else{
    		return false;
    	}
		
	}
	
	static void showDbgLogScreen(boolean show){
		
		if(isEnd) return;
		
		
	    if(show) {
	    	logView.setVisibility(View.VISIBLE);
	    }else{
	    	logView.setVisibility(View.INVISIBLE);
	    }
	    
	}
	static void setModeIndicatorColor(int color,String msg){
		
		if(isEnd) return;
		
    	modeIndicator.setBackgroundColor(color);
    	modeIndicator.setText(msg);
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
     
    	
    	
    	//debug screen is invisible.
		RobotActivity.showDbgLogScreen(true);
		    	modeIndicator=(TextView)findViewById(R.id.robot_mode);
    	
    	modeIndicator.setBackgroundColor(Color.YELLOW);
		
    	
    	earImage=(ImageView) findViewById(R.id.listen_image);
    	logInImage=(ImageView) findViewById(R.id.logIn);
    	
    	showEar(false);
    	
		Intent it=getIntent();
		
		//launcher 화면 에서  최초실행 
		if(true==it.getAction().equals("android.intent.action.MAIN"))
		{
		
			Log.d(TAG,"start with action.Main");
			// set main activity context
			RobotSpeech.getInstance(this);
			RobotMotion.getInstance(this);
			RobotFace.getInstance(this);
			RobotChatting.getInstance(this);
			
			RobotChatting.getInstance(this).setMainActivityHandler(mainHandler);
			
			//write asset data on file system.
		//	new UtilAssets(getApplicationContext(),"rmm").toFileSystem();
			
			brain=new RobotBrain(getApplicationContext(),mainHandler);
			
			//screen touch event need refactoring.
			installHandler(brain);	
			
			
			//todo refactoring 
			//NoiseDetector.getInstance(this).setMainActivityHandler(mainHandler);
			
			
	
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
			
			class MyOnGestureListener extends SimpleOnGestureListener implements
	        OnGestureListener {
				
				 private static final int SWIPE_MIN_DISTANCE = 100;
				 private static final int SWIPE_MAX_OFF_PATH = 350;
				 private static final int SWIPE_THRESHOLD_VELOCITY = 200;
				    
				@Override
				public boolean onDown(MotionEvent e) {
					
					return true;
				}
				
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						float velocityY) {
					
					
					try {
			            
			            // right to left swipe
			            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
			                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			            	
			            	if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			            		return false;
			            	
								evtHandler.handle(getApplicationContext(), 
								new RobotEvent(RobotEvent.EVT_SWIPE_SCREEN,0,0,null));
								writeLog("event swipe screen right to left");
			                
			            }
			            // left to right swipe
			            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
			                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			            	
			            	if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			            		return false;
			            
								evtHandler.handle(getApplicationContext(), 
								new RobotEvent(RobotEvent.EVT_SWIPE_SCREEN,1,0,null));
								writeLog("event swipe screen left to right");
			            
			            }
			            // down to up swipe
			            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
			                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			            	
			            	if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
			            		return false;
			            	
								evtHandler.handle(getApplicationContext(), 
								new RobotEvent(RobotEvent.EVT_SWIPE_SCREEN,2,0,null));
								writeLog("event swipe screen down to up");
			            
			            }
			            // up to down swipe
			            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
			                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			            	
			            	if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
			            		return false;
			            	
								evtHandler.handle(getApplicationContext(), 
								new RobotEvent(RobotEvent.EVT_SWIPE_SCREEN,3,0,null));
								writeLog("event swipe screen up to down");
			             
			            }
			        } 
					catch (Exception e){
			            
			        }
					
					return true;
				}
				
				@Override
				public void onLongPress(MotionEvent e) {
					evtHandler.handle(getApplicationContext(), 
					new RobotEvent(RobotEvent.EVT_LONG_PRESS_SCREEN));
					writeLog("event long press screen");
					
				}
				
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					
					return true;
				}
				
				@Override
				public void onShowPress(MotionEvent e) {
			
					evtHandler.handle(getApplicationContext(), 
					new RobotEvent(RobotEvent.EVT_TOUCH_SCREEN));
					writeLog("event touch screen");
					
			
				}
				
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}
			}

			gestureDetector = new GestureDetector(this, new MyOnGestureListener());	
			gestureDetector.setIsLongpressEnabled(true);
			
	
			mSurface.setOnTouchListener(new OnTouchListener() {
				Time _t = new Time();
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					
					
					gestureDetector.onTouchEvent(event);						
					
					Log.d(TAG, "touch face");
						
					if (event.getAction() == MotionEvent.ACTION_DOWN) 
					{
					}
					
					if(event.getAction()==MotionEvent.ACTION_UP)
					{
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
	
	public static Handler UIHandler = new Handler() {
	    public void handleMessage(Message msg) {
		switch (msg.what) {
		case CamConf.RM_VIEWS:
		    if (msg.obj != null) {
			mainLayout.removeView((CamSurface) msg.obj);
			if (faceRectangle != null) {
			    mainLayout.removeView(faceRectangle);
			    faceRectangle = null;
			}
			if (sampleView != null) {
			    mainLayout.removeView(sampleView);
			    sampleView = null;
			}
		    }
		    break;
		case CamConf.ADD_CAM:
		    if (msg.obj != null) {
			mainLayout.addView((CamSurface) msg.obj);
		    }
		    break;
		case CamConf.ADD_SAMPLE:
		    if (sampleView == null) {
			sampleView = new ImageView(ctx);
			if (msg.obj != null) {
			    sampleView.setLayoutParams((RelativeLayout.LayoutParams) msg.obj);
			}
			sampleView.setBackgroundColor(Color.BLACK);
			mainLayout.addView(sampleView);
		    }
		    break;
		    
		case CamConf.ADD_RECT:
			  if (faceRectangle == null) {
			    faceRectangle = new FaceRectangle(ctx);
			    if (msg.obj != null) {
			      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msg.obj;
			      faceRectangle.setLayoutParams(params);
			      faceRectangle.setSize(params.width, params.height);
			    }
			    mainLayout.addView(faceRectangle);
			  }
			break;
			
			
		
		case CamConf.DRAW_SAMPLE:
		    if (sampleView != null) {
			sampleView.setImageBitmap((Bitmap) msg.obj);
		    }
		    break;
		case CamConf.DRAW_RECT:
		    if (faceRectangle != null) {
			faceRectangle.draw((Rect[]) msg.obj);
		    }
		    break;
		}
	    };
	};
	
	
	 public static void writeLog(String text){
		 
		if(isEnd) return;
		
	    logView.append(text + "\n");
	    ((ScrollView) logView.getParent()).fullScroll(View.FOCUS_DOWN);
	}

}
