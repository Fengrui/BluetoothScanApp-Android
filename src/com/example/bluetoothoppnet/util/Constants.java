package com.example.bluetoothoppnet.util;


public abstract class Constants {
	public static final String STR_APPLICATION_NAME = "BluetoothOppNet";
	public static final String TAG_APPLICATION = "BluetoothOppNet";
	public static final String TAG_ACT_BTDEVICELIST = "BTDeviceList";
	public static final String TAG_ACT_TEST = "ActivityTest";
	public static final String EXTRA_MESSENGER = "extra_messenger";
	
	// messenge bundle key
	public static final String DATA_TIMESTAMP = "data_timestamp";
	public static final String DATA_DEVICE_MAC = "data_device_mac";
	public static final String DATA_DEVICE_NAME = "data_device_name";
	
	// messenger what field indicator, from 100
	public static final int WHAT_DATA_TIMESTAMP = 100;
	public static final int WHAT_DATA_CLIENT_TIMING_LOG = 101;
	public static final int WHAT_CLIENT_CONNECTED = 110;
	public static final int WHAT_CLIENT_CONNECTED_FAILED = 111;
	
	// BT client and server state in listview, start from 200
	public static final int STATE_CLIENT_CONNECTED = 201;
	public static final int STATE_CLIENT_FAILED = 202;
	public static final int STATE_CLIENT_UNCONNECTED = 203;
	public static final int STATE_CLIENT_OUTDATED = 204;
	
	// String, upper case
	public static final String STATE_CONNECTED = "SUCCESS";
	public static final String STATE_CONNECTION_FAILED = "FAILED";
	public static final String STATE_NOT_CONNECTED = "CONNECT";
	
	public static final String LOG_TIMESTAMP_CONNECT = "log_timestamp_connect";
	public static final String LOG_TIMESTAMP_WRITE = "log_timestamp_write";
	public static final String LOG_TIMESTAMP_WRITE_FINISHED = "log_timestamp_write_finished";
	public static final String LOG_TIMESTAMP_ACK_RECEIVED = "log_timestamp_ack_received";
	
	// BT client and server status, start from 10
	public static final int SERVER_DISCONNECTED = 10;
	public static final int SERVER_CONNECTED = 11;
	public static final int CLIENT_DISCONNECTED = 12;
	public static final int CLIENT_CONNECTED = 13;
	
	// Shared preference keys
	public static final String SP_RADIO_SCAN_DURATION = "sp_radio_scan_duration";
	
}
