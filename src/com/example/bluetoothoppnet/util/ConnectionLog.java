package com.example.bluetoothoppnet.util;

public class ConnectionLog {
	
	public long timestamp;
	public String name;
	public String MAC;
	public short rssi;
	public long delay;
	
	public ConnectionLog(long timestamp, String name, String MAC, short rssi, long delay){
		this.timestamp = timestamp;
		this.name = name;
		this.MAC = MAC;
		this.rssi = rssi;
		this.delay = delay;
	}
}
