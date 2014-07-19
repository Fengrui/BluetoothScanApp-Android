package com.example.bluetoothoppnet;

import android.bluetooth.BluetoothDevice;
import android.view.View.OnClickListener;

public class BTDevice{
	private short rssi;
	private int connState;
	private BluetoothDevice btDevice;
	private OnClickListener btConnect;
	private long avgContactsDelay;
	private int contactTimes;
	
	public BTDevice(BluetoothDevice device){
		this.btDevice = device;
		this.contactTimes = 0;
		this.avgContactsDelay = 0;
	}
	
	public long getDelay(){
		return this.avgContactsDelay;
	}
	
	public void updateConnectionDelay(long delay){
		avgContactsDelay = ((contactTimes*avgContactsDelay) + delay) / (contactTimes + 1);
		contactTimes ++;
	}
	
	public void setConnState(int state){
		this.connState = state;
	}
	
	public int getConnState(){
		return connState;
	}
	
	public void setOnClickListener(OnClickListener listener){
		this.btConnect = listener;
	}
	
	public OnClickListener getOnClickListener(){
		return btConnect;
	}

	public BluetoothDevice getRawDevice(){
		return btDevice;
	}
	
	public void setRssi(short rssi){
		this.rssi = rssi;
	}
	
	public String getName(){
		return btDevice.getName();
	}
	
	public String getMAC(){
		return btDevice.getAddress();
	}
	
	public short getRssi(){
		return rssi;
	}
	
}
