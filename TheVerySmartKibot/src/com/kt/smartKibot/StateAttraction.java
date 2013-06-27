package com.kt.smartKibot;

import java.util.Calendar;

import android.content.Context;

public class StateAttraction implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateAttraction";
	private volatile boolean isEnd=false;
	private WeatherInfo wInfo=null;
	
	@Override
	public void onStart(Context ctx) {
		isEnd=false;
		if(_DEBUG) {
				RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN,TAG);
		}
		else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
		}

		wInfo=new WeatherInfo();
	}

	@Override
	public void doAction(Context ctx) {

		Calendar c = Calendar.getInstance();
		int hour=c.get(Calendar.HOUR_OF_DAY);
		int minute=c.get(Calendar.MINUTE);
		
		RobotMotion.getInstance(ctx).stopAll();
		try {
			int cnt=0;

				while(!isEnd) {
					if(cnt==0) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
						RobotMotion.getInstance(ctx).goForward(1, 1);
					}
					
					if(cnt==10) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
					}
					
					if(cnt==20) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
						int rand=(int)(Math.random()*3l);
						
						switch(rand){
							case 0: RobotSpeech.getInstance(ctx).speak("하이", 1.1f, 1.0f);
							break;
							case 1:
							{
								int _hour=(hour==12)? 12:hour%12;
								RobotSpeech.getInstance(ctx).speak("현재 시간은 "+_hour+"시 "+minute+"분 이에요" , 1.1f, 1.0f);
							}
							break;
							
							case 2: 
							{
								int _hour=(hour==12)? 12:(hour)%12;
								String msg=" ";
								wInfo.refresh();
								WeatherStatus st=wInfo.getInfoHourly(hour,WeatherStatus.TODAY);
									
									switch(st.pty)
									{
										case 0: 
											if(st.sky==1){
											msg+=" 날씨가 짱 좋아요 ";
											}else{
												if(st.sky==4)
												{
													msg+="날씨가 흐려요 ";
												}
												else{
												msg+=" 날씨가 맑아요";
												}
											}
											
											break;
										case 1: 
											msg+="비가 오나요? ";
											break;
										case 2: 
											msg+="비가 오나요? 눈이 오나요? ";
											break;
										case 3: 
											msg+="눈이 오나요? 비가 오나요?";
											break;
										case 4: 
											msg+="눈이 오나요? ";
											break;
									}
								 RobotSpeech.getInstance(ctx).speak(msg, 1.1f, 1.0f);
							}// end of case #2
							break;
									
						}//end of switch random
					}
					
					if(cnt==30) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
					}
						
					if(cnt==40){ 
						RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
						RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
					}
					
					++cnt;	
					Thread.sleep(100);
					}
		}catch(Exception e){e.printStackTrace();}

	}

	@Override
	public void cleanUp(Context ctx) {
		RobotMotion.getInstance(ctx).stopAll();
		RobotMotion.getInstance(ctx).setLogoLEDDimming(0);

	}

	@Override
	public void onChanged(Context ctx) {
		isEnd=true;

	}

}
