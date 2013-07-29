package com.kt.smartKibot;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.Log;
import android.widget.Toast;

public class BTManager {
	final static String TAG="BTManager";
	
	private Context ctx;
	private BluetoothAdapter btAdapter=null;
	private BluetoothDevice targetDevice=null;
	private String targetDevName=null;
	
	private BroadcastReceiver bcReceiver=null;
	
	private ArrayList<ScanResult> scanResult=null;
	
	private ScanResultHandler scanResultHandler=null;
	
	public interface ScanResultHandler{
		void onReceive(ArrayList<ScanResult> result);
	}
	
	public class ScanResult{
		
		private BluetoothDevice device;
		private int cur_rssi;
		
		public ScanResult(BluetoothDevice device, int cur_rssi) {
			super();
			this.device = device;
			this.cur_rssi = cur_rssi;
			
		
		}
		
		public BluetoothDevice getDevice() {
			return device;
		}
		public void setDevice(BluetoothDevice device) {
			this.device = device;
		}
		public int getCur_rssi() {
			return cur_rssi;
		}
		public void setCur_rssi(int cur_rssi) {
			this.cur_rssi = cur_rssi;
		}
		
		
	}
	
	synchronized public void scanWithName(String devName){
		
		if(btAdapter!=null && btAdapter.isEnabled()==true)
		{
			
			if(btAdapter.isDiscovering()==false)
			{
				ctx.registerReceiver(bcReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
				ctx.registerReceiver(bcReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
				targetDevName=devName;
				scanResult.clear();
				
				btAdapter.startDiscovery();
				
			}
			else{
				
				Log.d(TAG,"discovering is proceeding");
				
			}
		}
		else{
			
		}
		
	}
	
//	public void scan(){
//	
//		targetDevName=null;
//		scanResult.clear();
//		
//		if(btAdapter!=null && btAdapter.isEnabled()==true)
//		{
//			
//			if(btAdapter.isDiscovering()==false)
//			{
//				ctx.registerReceiver(bcReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
//				ctx.registerReceiver(bcReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
//				
//				btAdapter.startDiscovery();
//				
//			}
//			else{
//				
//				Toast.makeText(ctx, "discovering is proceeding", Toast.LENGTH_SHORT).show();
//				
//			}
//		}
//		else{
//			
//		}
//		
//		
//	}
	
	
	
	public void init(Context ctx,ScanResultHandler handler){
		
		this.ctx=ctx;
		this.scanResultHandler=handler;
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (btAdapter == null)
		{
			// TODO : failed
		}
		else
		{
			if (btAdapter.isEnabled() == false)
			{
				// Bluetooth 사용 요청
			//	String enableBluetooth = BluetoothAdapter.ACTION_REQUEST_ENABLE;
			//	startActivityForResult(new Intent(enableBluetooth),RETURN_REQUEST_ENABLE);
			}
		
			
		}

		
		scanResult=new ArrayList<BTManager.ScanResult>();
		
		
		bcReceiver=new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				try{
					
					
			    	String action = intent.getAction();
			    	
			    	if (action.equals(BluetoothDevice.ACTION_FOUND))
			    	{
			    		
			    			//target is specified
				    		Log.d(TAG,"Device is found (target is specified)");
			    			
			    			boolean bCheck = false;
			    			BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			    			int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,(short) Integer.MIN_VALUE);
			    			
				    		Log.d(TAG,"targetDevName:"+targetDevName+" remoteDevName:"+remoteDevice.getName());

			    			if( targetDevName.equals(remoteDevice.getName()))
			    			{
				    				scanResult.add(new ScanResult(remoteDevice,rssi));
				    				btAdapter.cancelDiscovery();
			    			}
			    	
			    	}
			    	else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
			    	{
			    		Log.d(TAG,"discovery finished");
			    		if(scanResultHandler!=null)
			    		{
			    			scanResultHandler.onReceive(scanResult);
			    		}
			    	
			    	}
		    	}
		    	catch(Exception e)
		    	{
		    		// exception
		    	}
		    }
				
		};
		

		
	}
	
	public void finish(){
		
		if(btAdapter!=null){
			btAdapter.cancelDiscovery();
		
			try{
			ctx.unregisterReceiver(bcReceiver);
			}catch(IllegalArgumentException e){
				Log.d(TAG,"receiver is not registered yet");
			}
		}
		
		
		
	}

}
