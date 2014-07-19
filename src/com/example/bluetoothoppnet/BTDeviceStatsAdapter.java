package com.example.bluetoothoppnet;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BTDeviceStatsAdapter extends ArrayAdapter<BTDevice> {
	Context context;
	int layoutResourceId;
	ArrayList<BTDevice> deviceList = null;

	public BTDeviceStatsAdapter(Context context, int layoutResourceId, ArrayList<BTDevice> deviceList) {

		super(context, layoutResourceId, deviceList);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.deviceList = deviceList;
	}

	public void addDevice(BTDevice btDevice) {
		deviceList.add(btDevice);
	}

	public void setDeviceDelay(String MAC, long delay){
		for (int i=0; i<deviceList.size(); i++){
			if(deviceList.get(i).getMAC().equalsIgnoreCase(MAC)){
				deviceList.get(i).updateConnectionDelay(delay);
			}
		}
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

			holder.deviceMac = (TextView)row.findViewById(R.id.device_mac);
			holder.deviceName = (TextView)row.findViewById(R.id.device_name);
			holder.deviceAvgDelay = (TextView)row.findViewById(R.id.device_avg_connection_delay);

			row.setTag(holder);
		}
		else
		{
			holder = (DeviceStatsHolder)row.getTag();
		}

		BTDevice device = deviceList.get(position);

		holder.deviceMac.setText(device.getMAC());
		if(device.getName() != null){
			holder.deviceName.setText(device.getName());
		}
		long avgDelay = device.getDelay();
		if (avgDelay > 1000){
			holder.deviceAvgDelay.setText(String.valueOf((float)avgDelay / 1000) + " (s)");
		}
		else{
			holder.deviceAvgDelay.setText(String.valueOf(avgDelay) + " (ms)");
		}
		
		return row;
	}

	static class DeviceStatsHolder
	{
		TextView deviceName;
		TextView deviceMac;
		TextView deviceAvgDelay;
	}
}
