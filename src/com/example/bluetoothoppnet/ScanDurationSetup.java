package com.example.bluetoothoppnet;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.bluetoothoppnet.util.Constants;

public class ScanDurationSetup extends Dialog {

	Button okButton;
	Button cancelButton;
	
	private int selectedDurationId;

	Context parentActivity;
	public class onRadioButtonClicked implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			// Is the button now checked?
			boolean checked = ((RadioButton) view).isChecked();

			// Check which radio button was clicked
			switch(view.getId()) {
			case R.id.radio_scan_duration_1:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_1;
				}
				break;
			case R.id.radio_scan_duration_2:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_2;
				}
				break;
			case R.id.radio_scan_duration_5:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_5;
				}
				break;
			case R.id.radio_scan_duration_10:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_10;
				}
				break;
			default:
				break;
			}
		}
	}

	public ScanDurationSetup(Context context) {
		super(context);
		parentActivity = context;
		// TODO Auto-generated constructor stub
		setContentView(R.layout.dialog_set_scanduration);
		okButton = (Button) findViewById(R.id.ok_button);
		okButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				savePreferences(Constants.SP_RADIO_SCAN_DURATION, selectedDurationId);
				ScanDurationSetup.this.dismiss(); 
			}
		});
		cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ScanDurationSetup.this.dismiss(); 
			}
		});

		// scan duration id refering to radio button id
		selectedDurationId = loadSavedPreferences();
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.scan_duration_radiogroup);

		int count = radioGroup.getChildCount();
		for (int i=0;i<count;i++) {
			View o = radioGroup.getChildAt(i);
			if (o instanceof RadioButton) {
				o.setOnClickListener(new onRadioButtonClicked());
				if( o.getId() == selectedDurationId){
					((RadioButton) o).setChecked(true);
				}
			}
		}
	}

	private int loadSavedPreferences() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(parentActivity);
		int scanDuration = sharedPreferences.getInt(Constants.SP_RADIO_SCAN_DURATION, R.id.radio_scan_duration_5);
		return scanDuration;
	}

	private void savePreferences(String key, int value) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(parentActivity);
		Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
}