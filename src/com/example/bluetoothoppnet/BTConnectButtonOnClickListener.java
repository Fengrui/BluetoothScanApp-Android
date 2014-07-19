package com.example.bluetoothoppnet;

import android.view.View;
import android.view.View.OnClickListener;

public class BTConnectButtonOnClickListener implements OnClickListener {

	BTDevice btDevice;
	BTService btService;
	
	public BTConnectButtonOnClickListener(BTDevice btDevice, BTService btService){
		this.btDevice = btDevice;
		this.btService = btService;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		btService.connect(btDevice.getRawDevice());
	}
}