package com.kt.smartKibot;

import java.util.Calendar;

import android.content.Context;

public class StateAttraction implements IRobotState {

	private boolean _DEBUG=true;
	private static final String TAG="StateAttraction";
	private volatile boolean isEnd=false;
	private WeatherInfo wInfo=null;
	private User detectedUser;
	
	public StateAttraction(User user){
		detectedUser=user;
	}
	
	public StateAttraction(){
		detectedUser=null;
	}
	
	@Override
	public void onStart(Context ctx) {
		isEnd=false;
		if(_DEBUG) {
			if(detectedUser!=null){
				RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN,TAG+ " ID:"+detectedUser.id);
			}
			else{
				RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN,TAG);
			}
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
					
					if(cnt==20){
						RobotSpeech.getInstance(ctx).speak("찾았다!", 1.1f, 1.0f);
					
					}
					
					if(cnt==30){
					
						if(detectedUser!=null){
						RobotSpeech.getInstance(ctx).speak(""+detectedUser.name+"님 거기 있었네", 1.1f, 1.0f);
						}else
						{
						RobotSpeech.getInstance(ctx).speak("거기서 뭐하세요?", 1.1f, 1.0f);
						}
					}
					
					if(cnt==50){
						RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
						int rand=(int)(Math.random()*6l);
						
						switch(rand){
							case 0: RobotSpeech.getInstance(ctx).speak("심심해요 놀아주세요", 1.1f, 1.0f);
							break;
							case 1:
							{
								int _hour=(hour==12)? 12:hour%12;
								RobotSpeech.getInstance(ctx).speak("지금 시간은"+_hour+"시 "+minute+"분 이에요" , 1.1f, 1.0f);
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
							
							case 3:
							{
								RobotSpeech.getInstance(ctx).speak("키봇은 머리를 쓰다듬으면 좋아요" , 1.1f, 1.0f);
							}
							break;
							
							case 4:
							{
								RobotSpeech.getInstance(ctx).speak("키봇은 다리를 만지면 좋아요" , 1.1f, 1.0f);
							}
							break;
							
							case 5:
							{
								RobotSpeech.getInstance(ctx).speak("키봇 머리에 파란불이 들어오면 대화 할수 있어요" , 1.1f, 1.0f);
							}
							break;
							
							case 6:
							{
								RobotSpeech.getInstance(ctx).speak("키봇 머리에 초록불이 들어오면 키봇이 당신을 찾고 있는중이에요 얼굴을 보여주세요" , 1.1f, 1.0f);
							}
							break;
									
						}//end of switch random
					}
					
					if(cnt==60) {
						RobotMotion.getInstance(ctx).setLogoLEDDimming(0);
					}
						
					if(cnt==70){ 
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
