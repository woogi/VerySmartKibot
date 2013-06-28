package com.kt.smartKibot;

import android.content.Context;
import android.content.Intent;

public class StateBye implements IRobotState {

	WeatherInfo wInfo=null;
	static final boolean _DEBUG=true;
	private volatile boolean isEnd=false;
	
	static final String TAG="StateBye";
	
	public StateBye(){
		
	}
	
	@Override
	public void onStart(Context ctx) {
		// TODO Auto-generated method stub
		wInfo=new WeatherInfo();

		if (_DEBUG) {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN, TAG);
		} else {
			RobotFace.getInstance(ctx).change(RobotFace.MODE_FUN);
		}
		
		isEnd=false;
	}

	@Override
	public void doAction(Context ctx) {
		
		String msg=new String();
		wInfo.refresh();
		//WeatherStatus status=wInfo.status.get(0);
		WeatherStatus st=wInfo.getInfoHourly(18,WeatherStatus.TODAY);
		
		msg="소장님. 오늘도 수고  하셨습니다. ";
		
		if(st!=null){
		
			msg+="귀가길은 ";
			switch(st.sky)
			{
				case 1:
					msg+="하늘이 맑고 ";
					break;
				case 2:
					msg+="구름이 조금있고 ";
					break;
				case 3:
					msg+="구름이 많고 ";
					break;
				case 4:
					//msg+="흐리고";
					break;
			}
			
			switch(st.pty)
			{
				case 0: 
					if(st.sky==1){
					msg+=" 화창한 날씨가 예상됩니다. ";
					}else{
						if(st.sky==4)
						{
							msg+="흐린 날씨가 예상됩니다.";
						}
						else{
						msg+=" 맑은 날씨가 예상 됩니다.";
						}
					}
					
					break;
				case 1: 
					msg+=" 비가 올걸로 예상됩니다. ";
					break;
				case 2: 
					msg+=" 비 또는 눈이 올것으로 예상됩니다. ";
					break;
				case 3: 
					msg+=" 눈 또는 비가  올것으로 예상됩니다. ";
					break;
				case 4: 
					msg+=" 눈이 올것으로 예상됩니다. ";
					break;
			
			}
			
			msg+= "예상 기온은 "+st.temp +"도 입니다.";
			
			if( st.sky>=3)
			msg+= "운전 조심하세요.";
			
			if(st.pty>0)
			{
				msg+="아 그리고 잊지 말고 우산을 챙기세요.";
			}
			else{
				
				if(st.pop>50)
				{
					msg+="아 그리고 비가 올지도 모르니  우산을 챙기세요.";
				}
			}
			
		
		}


		try{
			
			int cnt=0;
			boolean moveBack=false;
			
			RobotMotion.getInstance(ctx).setLogoLEDDimming(2);
			
			while(!isEnd)
			{
			
				if(cnt==0) RobotMotion.getInstance(ctx).goForward(1,1);
					
				if(cnt==10) RobotSpeech.getInstance(ctx).speak(msg);
				
				if(cnt>40 && RobotSpeech.getInstance(ctx).isSpeaking()==false && moveBack==false) 
				{
					RobotMotion.getInstance(ctx).goBack(1,1);
					moveBack=true;
				}
				
				Thread.sleep(100);
				
				++cnt;
			}
		}
		catch(Exception e){e.printStackTrace();}
		
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
