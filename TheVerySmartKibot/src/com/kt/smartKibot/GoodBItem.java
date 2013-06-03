package com.kt.smartKibot;

import android.content.Context;
import android.content.Intent;

public class GoodBItem implements IRobotState {

	//Context ctx;
	WeatherInfo wInfo;
	String tts;
	
	public GoodBItem(){
		//this.ctx=ctx;
		
	}
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		wInfo=new WeatherInfo();
		tts=new String();
	

		RobotFace.getInstance(ctx).on();
	}

	@Override
	public void doAction(Context ctx) {
		// TODO Auto-generated method stub
		wInfo.refresh();
		WeatherStatus status=wInfo.status.get(0);
		WeatherStatus st=wInfo.getInfoHourly(18,WeatherStatus.TODAY);
		
		tts="오늘도 수고  하셨습니다. ";
		
		if(st!=null){
		
			tts+="귀가길은 ";
			switch(st.sky)
			{
				case 1:
					tts+="하늘이 맑고 ";
					break;
				case 2:
					tts+="구름이 조금있고 ";
					break;
				case 3:
					tts+="구름이 많고 ";
					break;
				case 4:
					//tts+="흐리고";
					break;
			}
			
			switch(st.pty)
			{
				case 0: 
					if(st.sky==1){
					tts+=" 화창한 날씨가 예상됩니다. ";
					}else{
						if(st.sky==4)
						{
							tts+="흐린 날씨가 예상됩니다.";
						}
						else{
						tts+=" 맑은 날씨가 예상 됩니다.";
						}
					}
					
					break;
				case 1: 
					tts+=" 비가 올걸로 예상됩니다. ";
					break;
				case 2: 
					tts+=" 비 또는 눈이 올것으로 예상됩니다. ";
					break;
				case 3: 
					tts+=" 눈 또는 비가  올것으로 예상됩니다. ";
					break;
				case 4: 
					tts+=" 눈이 올것으로 예상됩니다. ";
					break;
			
			}
			
			tts+= "예상 기온은 "+st.temp +"도 입니다.";
			
			if( st.sky>=3)
			tts+= "운전 조심하세요.";
			
			if(st.pty>0)
			{
				tts+="아 그리고 잊지 말고 우산을 챙기세요.";
			}
			else{
				
				if(st.pop>50)
				{
					tts+="아 그리고 비가 올지도 모르니  우산을 챙기세요.";
				}
			}
			
		
		}
		
		try{
			
		
		RobotMotion.getInstance(ctx).led(0,100,3);
		Thread.sleep(2000);
			
		
		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_LEFT);
		Thread.sleep(200);
		
		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_RIGHT);
		Thread.sleep(200);
		
		RobotMotion.getInstance(ctx).head(RobotMotion.HEAD_FRONT);
		Thread.sleep(200);
		
		RobotMotion.getInstance(ctx).goBack(1,1);
		

		RobotSpeech.getInstance(ctx).speakWithCmpNotification(tts,RobotFace.UTT_ID_FACE_ACT_FACE_OFF_20_SEC,null);
		
		}catch(Exception e){
			e.printStackTrace();
		};
	}

	@Override
	public void cleanUp(Context ctx) {
		// TODO Auto-generated method stub

		RobotMotion.getInstance(ctx).offAllLed();
	}

	@Override
	public void onChanged(Context ctx) {
		// TODO Auto-generated method stub
		
	}

}
