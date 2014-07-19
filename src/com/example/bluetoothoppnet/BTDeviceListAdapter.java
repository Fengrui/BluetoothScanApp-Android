package com.example.bluetoothoppnet;

import java.util.ArrayList;

import com.example.bluetoothoppnet.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BTDeviceListAdapter extends ArrayAdapter<BTDevice> {
	Context context; 
	int layoutResourceId;
	ArrayList<BTDevice> deviceList = null;

	public BTDeviceListAdapter(Context context, int layoutResourceId, ArrayList<BTDevice> deviceList) {

		super(context, layoutResourceId, deviceList);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.deviceList = deviceList;
	}

	public void addDevice(BTDevice btDevice) {
		deviceList.add(btDevice);
	}

	public void setDeviceAction(String MAC, int action) {
		for (int i=0; i<deviceList.size(); i++){
			if(deviceList.get(i).getMAC().equalsIgnoreCase(MAC)){
				switch(action){
				case Constants.WHAT_CLIENT_CONNECTED:
					deviceList.get(i).setConnState(Constants.STATE_CLIENT_CONNECTED);
					break;
				case Constants.WHAT_CLIENT_CONNECTED_FAILED:
					deviceList.get(i).setConnState(Constants.STATE_CLIENT_CONNECTED);
					break;
				case Constants.WHAT_DATA_TIMESTAMP:
					deviceList.get(i).setConnState(Constants.STATE_CLIENT_UNCONNECTED);
					break;
				default:
					break;
				}

			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DeviceHolder holder = null;

		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DeviceHolder();

			holder.deviceMac = (TextView)row.findViewById(R.id.device_mac);
			holder.deviceName = (TextView)row.findViewById(R.id.device_name);
			holder.deviceRssi = (TextView)row.findViewById(R.id.device_rssi);
			holder.deviceSignal = (ImageView) row.findViewById(R.id.signal_strength);
			holder.deviceConnect = (Button) row.findViewById(R.id.device_connect);

			row.setTag(holder);
		}
		else
		{
			holder = (DeviceHolder)row.getTag();
		}

		BTDevice device = deviceList.get(position);

		holder.deviceMac.setText(device.getMAC());
		if(device.getName() != null){
			holder.deviceName.setText(device.getName());
		}
		short rssi = device.getRssi();
		holder.deviceRssi.setText(String.valueOf(rssi) + "dBm");
		/*
		 * -30dBm = Awesome
		 * -60dBm = Good
		 * -80dBm = OK
		 * -90dBm = Bad
		 */
		if (rssi > -30){
			holder.deviceSignal.setImageResource(R.drawable.signal_strong);
		}else if (rssi > -50){
			holder.deviceSignal.setImageResource(R.drawable.signal_high);
		}else if (rssi > -60){
			holder.deviceSignal.setImageResource(R.drawable.signal_mid);
		}else if (rssi > -80){
			holder.deviceSignal.setImageResource(R.drawable.signal_low);
		}else{
			holder.deviceSignal.setImageResource(R.drawable.signal_weak);
		}
		switch(device.getConnState()){
		case Constants.STATE_CLIENT_CONNECTED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connected_button);
			holder.deviceConnect.setText(Constants.STATE_CONNECTED);
			break;
		case Constants.STATE_CLIENT_FAILED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connect_failed_button);
			holder.deviceConnect.setText(Constants.STATE_CONNECTION_FAILED);
			break;
		case Constants.STATE_CLIENT_UNCONNECTED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connect_button);
			holder.deviceConnect.setText(Constants.STATE_NOT_CONNECTED);
			break;
		case Constants.STATE_CLIENT_OUTDATED:
			holder.deviceConnect.setBackgroundResource(R.drawable.connect_outdated_button);
			holder.deviceConnect.setText(Constants.STATE_NOT_CONNECTED);
			holder.deviceConnect.setTextColor(Color.BLACK);
			holder.deviceConnect.setEnabled(false);
			break;
		default:
			break;

		}
		holder.deviceConnect.setOnClickListener(device.getOnClickListener());

		return row;
	}

	static class DeviceHolder
	{
		TextView deviceName;
		TextView deviceMac;
		TextView deviceRssi;
		ImageView deviceSignal;
		Button deviceConnect;
	}
}
