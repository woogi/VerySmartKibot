package com.kt.smartKibot;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class NoiseCheckThread extends Thread {
	AudioRecord audioRecord=null;
	OnNoiseListener noiseListener=null;
	boolean isRecording = false;
	int threshold=500;
	int frequency = 11025;
	int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	public NoiseCheckThread(Context context) {
		
	}

	public void run() {
		int bufferSize;
		short[] buffer;
		int psum=0,pavg=0,tsum=0,tavg=0,count=0,tcount=0,dB=0;
		
		bufferSize=AudioRecord.getMinBufferSize(frequency,channelConfiguration,audioEncoding);
		audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,frequency,
				channelConfiguration,audioEncoding,bufferSize);
		
		if(audioRecord.getState()==AudioRecord.STATE_UNINITIALIZED) {
			Log.d("EmotionTest","AudioRecord uninitialized...");
			audioRecord.release();
			audioRecord=null;
			return;
		}

		buffer = new short[bufferSize];
		audioRecord.startRecording();
		isRecording=true;

		while (isRecording) {
			int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
			if(bufferReadResult==0) continue;
			
			psum=0;
			if(count<300) count++;
			if(++tcount==1000) tcount=1;
			
			if (bufferReadResult != 0){
        			psum=0;
        			if(count<300) count++;
        			if(++tcount==1000) tcount=1;
        			
        			for (int i = 0; i < bufferReadResult; i++) {
        				psum+=Math.abs(buffer[i]);
        			}
        			
        			dB=cal_dB(buffer,bufferReadResult);
        			pavg=(int)(psum/bufferReadResult);
        			tsum=(int)(tavg*(count-1))+pavg;
        			tavg=(int)(tsum/count);
        			if(pavg>threshold) {
        				Log.d("NoiseCheckThread","value("+tcount+"): "+pavg+", "+dB+","+bufferReadResult);
        				if(noiseListener!=null) noiseListener.onNoiseEvent(pavg,dB);
        			}
			}
		}
		
		audioRecord.stop();
		audioRecord.release();
		audioRecord=null;
		Log.d("NoiseCheckThread","### NoiseCheckThread stopping...");
	}
	
	public void stopRunning() {
		isRecording=false;
	}
	
	int cal_dB(short[] buf,int size) {
		double sum=0,sqsum=0;
		for(int i=0;i<size;i++) {
			final long v=buf[i];
			sum+=v;
			sqsum+=v*v;
		}
		double power=(sqsum-sum*sum/320)/320;
		float max_short=32768;
		power/=max_short*max_short;
		double result=Math.log10(power)*10f+0.6f;
		
		return (int)result;
	}
	
	public interface OnNoiseListener {
		public void onNoiseEvent(int vol, int dB);
	}
	
	public void setOnNoiseListener(OnNoiseListener listener) {
		noiseListener=listener;
	}
	
}
