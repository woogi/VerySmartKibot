package com.kt.smartKibot;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.widget.Toast;

public class RobotSpeech implements OnInitListener{

	static final String TAG="RobotSpeech";

	private TextToSpeech tts=null;
	private boolean isInit=false;
	
	static RobotSpeech _this=null;
	
	private float pitch=1.0f;
	private float speed=1.0f;
	
	private Context ctx;
	boolean showToast=false;
	
	
	private RobotSpeech(Context ctx){
		tts=new TextToSpeech(ctx, RobotSpeech.this);
		this.ctx=ctx;
		
	};
	
	static synchronized RobotSpeech getInstance(Context ctx){
		if(_this==null)_this=new RobotSpeech(ctx);
		
		return _this;
	}	
	
	static synchronized void finish(){
		
		if(_this==null) return;
		
		_this.tts.shutdown();
		_this=null;
	}	
	
	protected void speakWithCmpNotification(String text, int flag,OnUtteranceCompletedListener listener ) {
		
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
				String.valueOf(flag));
		
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
		if(listener==null)
		{
			tts.setOnUtteranceCompletedListener((OnUtteranceCompletedListener) ctx);
		}
		
		
		if(showToast==true){
			
			Toast.makeText(ctx,text,Toast.LENGTH_SHORT).show();
		}
		
		Log.d(TAG,"speakWithCmpNotification: "+text+"flag: "+flag);
	}
	
	public void speak(String msg,float speed,float pitch){
		
		tts.setSpeechRate(speed);
		tts.setPitch(pitch);
		speak(msg);
		
		tts.setSpeechRate(this.speed);
		tts.setPitch(this.pitch);
		
	}
	
	public void speak(String msg){
		
		assert tts!=null :"not initilized yet";
		
	    tts.speak(msg,TextToSpeech.QUEUE_FLUSH,null);
	    if(showToast==true) 
	    {
	    	Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show();
	    	
	    }
		
	    Log.d(TAG,"speak: "+msg);
	}
	
	public void setVolume(float volume/*0~1*/){
			
			AudioManager mAudioManager = 
		            (AudioManager)ctx.getSystemService(ctx.AUDIO_SERVICE);
			
			mAudioManager.setStreamVolume(AudioManager.STREAM_TTS, 
					(int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_TTS) * volume),
					AudioManager.STREAM_TTS);
					
	}
	
	public boolean isSpeaking(){
		
		return tts.isSpeaking();
		
	}
	
	 public void onInit(int arg0) {
		// TODO Auto-generated method stub
		
		isInit = arg0 == TextToSpeech.SUCCESS;   //성공여부 저장
		
		if(isInit==true)
		{
			tts.setLanguage(Locale.KOREA);
			speed=0.9f;
			pitch=1.0f;
			
			tts.setSpeechRate(speed);
			tts.setPitch(pitch);
		}
	}


}
