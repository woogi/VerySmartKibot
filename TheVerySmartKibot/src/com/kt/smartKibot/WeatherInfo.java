package com.kt.smartKibot;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

class WeatherStatus{
	
	public static final int TODAY=0;
	public static final int TOMORROW=1;
	public static final int AFTER_TOMORROW=2;
	
	int hour;/* 3시간 단위 max (18이면 15~18시)*/
	int day; /*오늘,내일,모레 (0이면 오늘)*/
	float temp; /*현재온도*/
	float tmx; /* 최대 온도 없으면 -999.0*/
	float tmn;
	int sky; /*하늘: 1:맑음,2:구름조금,3:구름많음,4:흐림*/
	int pty; /*강수상태 0:없음,1:비,2:비/눈,3:눈/비,4:눈*/
	String wf;
	int pop; /*강수 확률*/
	int reh; /*습도*/
	String brief;
}

public class WeatherInfo {
	
	
	private static final String targetUri="http://www.kma.go.kr/wid/queryDFS.jsp?gridx=61&gridy=124";
	private DocumentBuilderFactory dbf;
	
	ArrayList <WeatherStatus> status;
	
	public WeatherInfo() {
	
		dbf=DocumentBuilderFactory.newInstance();
		status=new ArrayList <WeatherStatus>();
		
	}

	private String getNodeValue(Element elem,String tagName)
	{
		NodeList elemList=(NodeList)elem.getElementsByTagName(tagName);
		Node _node=elemList.item(0);
		String _value=_node.getFirstChild().getNodeValue();
		
		return _value;
	}
	
	
    public WeatherStatus getInfoHourly(int targetHour,int day ){
    	
    	for(int i=0; i<status.size();i++){
    		
    		if(day==status.get(i).day)
    		{
    			if(status.get(i).hour > targetHour && status.get(i).hour <=targetHour+3)
    				return status.get(i);
    		}
    	}
    	
    	return null;
    	
    }
    
    
	public void refresh(){
		
		status.clear();
		
		try{
			DocumentBuilder db=dbf.newDocumentBuilder();
			String rawInfo=getInfo();
			
			Document doc=db.parse(new InputSource(new StringReader(rawInfo)));
			doc.getDocumentElement().normalize();
			NodeList nodeList=doc.getElementsByTagName("data");
			
			for(int i=0;i<nodeList.getLength();i++){
				
				WeatherStatus wi=new WeatherStatus();
				
				Node node=nodeList.item(i);
				Element elem=(Element)node;
				
				/* test*/
				/*
				NodeList elemList=(NodeList)elem.getElementsByTagName("hour");
				Node _node=elemList.item(0);
				String _value=_node.getFirstChild().getNodeValue();
				*/
				
				wi.hour=
					Integer.parseInt(getNodeValue(elem,"hour")
				);
				
				wi.day=
				Integer.parseInt(
						getNodeValue(elem,"day")
				);
				
				wi.temp=
				Float.parseFloat(
						getNodeValue(elem,"temp")
				);
				wi.tmx=
				Float.parseFloat(
						getNodeValue(elem,"tmx")
				);
				wi.tmn=
				Float.parseFloat(
						getNodeValue(elem,"tmn")
				);
				wi.sky=
				Integer.parseInt(
						getNodeValue(elem,"sky")
				);
				wi.pty=
				Integer.parseInt(
						getNodeValue(elem,"pty")
				);
				wi.wf=
						getNodeValue(elem,"wfKor");
				wi.pop=
				Integer.parseInt(
						getNodeValue(elem,"pop")
				);
				
				wi.reh=
				Integer.parseInt(
						getNodeValue(elem,"reh")
				);
				
				
				status.add(wi);
				
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String getInfo(){
		
		String info=null;
		
		HttpClient client =new DefaultHttpClient();
		HttpGet get =new HttpGet(targetUri);
		try{
			
		HttpResponse response =client.execute(get);
		HttpEntity resEntity= response.getEntity();
		
		
		if(resEntity!=null){
			info=EntityUtils.toString(resEntity);
		}
		
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return info;
		
	}

}
