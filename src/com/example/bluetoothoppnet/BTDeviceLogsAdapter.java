package com.example.bluetoothoppnet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.bluetoothoppnet.util.ConnectionLog;

public class BTDeviceLogsAdapter extends ArrayAdapter<ConnectionLog> {
	Context context;
	int layoutResourceId;
	ArrayList<ConnectionLog> logList = null;

	public BTDeviceLogsAdapter(Context context, int layoutResourceId, ArrayList<ConnectionLog> logList) {

		super(context, layoutResourceId, logList);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.logList = logList;
	}

	public void addLog(ConnectionLog log) {
		logList.add(log);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DeviceStatsHolder holder = null;

		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DeviceStatsHolder();

			holder.timestamp = (TextView)row.findViewById(R.id.device_timestamp);
			holder.deviceMac = (TextView)row.findViewById(R.id.device_mac);
			holder.deviceName = (TextView)row.findViewById(R.id.device_name);
			holder.deviceRssi = (TextView)row.findViewById(R.id.device_rssi);
			holder.deviceDelay = (TextView)row.findViewById(R.id.device_delay);

			row.setTag(holder);
		}
		else
		{
			holder = (DeviceStatsHolder)row.getTag();
		}

		ConnectionLog log = logList.get(position);
		Date date = new Date(log.timestamp);
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.UK);
		String dateFormatted = formatter.format(date);
		holder.timestamp.setText(dateFormatted);
		holder.deviceMac.setText(log.MAC);
		if(log.name != null){
			holder.deviceName.setText(log.name);
		}
		holder.deviceRssi.setText(String.valueOf(log.rssi));
		
		long avgDelay = log.delay;
		if (avgDelay > 1000){
			holder.deviceDelay.setText(String.valueOf((float)avgDelay / 1000) + " (s)");
		}
		else{
			holder.deviceDelay.setText(String.valueOf(avgDelay) + " (ms)");
		}
		
		return row;
	}

	static class DeviceStatsHolder
	{
		TextView timestamp;
		TextView deviceName;
		TextView deviceMac;
		TextView deviceRssi;
		TextView deviceDelay;
	}
}
