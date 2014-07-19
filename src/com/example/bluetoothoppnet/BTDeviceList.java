package com.example.bluetoothoppnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bluetoothoppnet.util.ConnectionLog;
import com.example.bluetoothoppnet.util.Constants;


public class BTDeviceList extends ActionBarActivity implements ActionBar.TabListener {
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	//Bluetooth adapter declaration
	private BluetoothAdapter mBluetoothAdapter = null;

	/**
	 * List of request code
	 */
	private final int REQUEST_BT_ENABLE = 1;
	private final int REQUEST_BT_DISCOVERABLE = 11;

	private int RESULT_BT_DISCOVERABLE_DURATION = 300;

	private final int SCAN_INTERVAL = 5000;

	/**
	 * boolean to indicate scanning process
	 */
	private boolean mScanning;
	// current activity
	static Context context = null;

	// device list array and adapter
	private ArrayList<BTDevice> deviceList = new ArrayList<BTDevice>();
	BTDeviceListAdapter deviceListAdapter;

	// device stats adapter
	BTDeviceStatsAdapter deviceStatsAdapter;

	// connection log array and adapter
	private ArrayList<ConnectionLog> logList = new ArrayList<ConnectionLog>();
	BTDeviceLogsAdapter deviceLogsAdapter;
	
	// BT service API
	private BTService mBTService;

	// Code to manage Service lifecycle - client connection
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			Log.d(Constants.TAG_APPLICATION, "receive service obj");
			mBTService = ((BTService.LocalBinder) service).getService();
			if (mBTService.startServer() > 0){
				Log.d(Constants.TAG_APPLICATION, "server started");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBTService = null;
		}
	};


	//comparator to sort btdevice arraylist
	public class BTDeviceConnStateComparator implements Comparator<BTDevice>
	{
		@Override
		public int compare(BTDevice lhs, BTDevice rhs) {
			// TODO Auto-generated method stub
			return lhs.getConnState() - rhs.getConnState();
		}
	}

	/**
	 * The Handler that gets information back from the BTService
	 */
	class btServiceHandler extends Handler {

		@Override
		public void handleMessage(Message msg)
		{
			Bundle b = msg.getData();
			switch(msg.what){
			case Constants.WHAT_DATA_TIMESTAMP:
				Toast.makeText(context, String.valueOf(msg.getData().getLong(Constants.DATA_TIMESTAMP)),Toast.LENGTH_LONG).show();
				break;
			case Constants.WHAT_DATA_CLIENT_TIMING_LOG:
				// update devicestat list
				deviceStatsAdapter.setDeviceDelay(b.getString(Constants.DATA_DEVICE_MAC), b.getLong(Constants.LOG_TIMESTAMP_ACK_RECEIVED) - b.getLong(Constants.LOG_TIMESTAMP_CONNECT));
				deviceStatsAdapter.notifyDataSetChanged();
				// update deviceList adapter
				deviceListAdapter.setDeviceAction(b.getString(Constants.DATA_DEVICE_MAC), Constants.WHAT_DATA_TIMESTAMP);
				deviceListAdapter.notifyDataSetChanged();
				// update log adapter
				long timestamp = b.getLong(Constants.LOG_TIMESTAMP_ACK_RECEIVED);
				String MAC = b.getString(Constants.DATA_DEVICE_MAC);
				String name = null;
				short rssi = 0;
				for(BTDevice device : deviceList){
					if (device.getMAC().equals(MAC)){
						name = device.getName();
						rssi = device.getRssi();
						break;
					}
				}
				if(name != null){
					ConnectionLog connLog = new ConnectionLog(timestamp, name, MAC, rssi, b.getLong(Constants.LOG_TIMESTAMP_ACK_RECEIVED) - b.getLong(Constants.LOG_TIMESTAMP_CONNECT));
					ListView lvLogs = (ListView) mSectionsPagerAdapter.getItem(2).getView().findViewById(R.id.device_list);
					deviceLogsAdapter.addLog(connLog);
					lvLogs.setAdapter(deviceLogsAdapter);
				}
				break;
			case Constants.WHAT_CLIENT_CONNECTED:
				// update main UI (current listview)
				deviceListAdapter.setDeviceAction(b.getString(Constants.DATA_DEVICE_MAC), Constants.WHAT_CLIENT_CONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				Toast.makeText(context, "Client connected",Toast.LENGTH_SHORT).show();
				break;
			case Constants.WHAT_CLIENT_CONNECTED_FAILED:
				deviceListAdapter.setDeviceAction(b.getString(Constants.DATA_DEVICE_MAC), Constants.WHAT_CLIENT_CONNECTED_FAILED);
				deviceListAdapter.notifyDataSetChanged();
				Toast.makeText(context, "Connection failed",Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}

		}
	}

	private btServiceHandler mHandler = new btServiceHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
		}

		setContentView(R.layout.activity_btdevice_list);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// ensure the device not release invisible fragment
		mViewPager.setOffscreenPageLimit(2);
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(
					actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}


		// Create a BroadcastReceiver for ACTION_FOUND
		final BroadcastReceiver BTFoundReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					Log.d(Constants.TAG_APPLICATION, "get a device : " + String.valueOf(device.getAddress()));
					/*
					 * -30dBm = Awesome
					 * -60dBm = Good
					 * -80dBm = OK
					 * -90dBm = Bad
					 */
					short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
					int deviceIndex = -1;
					for(int i=0; i < deviceList.size(); i++){
						if (deviceList.get(i).getMAC().equals(device.getAddress())){
							deviceIndex = i;
						}
					}
					if (deviceIndex < 0){ // device not exist
						BTDevice btDevice = new BTDevice(device);
						btDevice.setRssi(rssi);
						btDevice.setConnState(Constants.STATE_CLIENT_UNCONNECTED);
						BTConnectButtonOnClickListener onClickListener = new BTConnectButtonOnClickListener(btDevice, mBTService);
						btDevice.setOnClickListener(onClickListener);
						deviceIndex = deviceList.size();
						deviceList.add(btDevice);
					}
					else{ // device already found
						BTDevice btDevice = deviceList.get(deviceIndex);
						btDevice.setRssi(rssi);
						btDevice.setConnState(Constants.STATE_CLIENT_UNCONNECTED);
					}
					// reorder the list
					Collections.sort(deviceList, new BTDeviceConnStateComparator());
					ListView lv = (ListView) mSectionsPagerAdapter.getItem(0).getView().findViewById(R.id.device_list);
					deviceListAdapter = new BTDeviceListAdapter(context, R.layout.fragment_btdevice_list_device, deviceList);
					lv.setAdapter(deviceListAdapter);
				}
				else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					mScanning = true;
					invalidateOptionsMenu();
					Log.d(Constants.TAG_APPLICATION, "Discovery process has been started: " + String.valueOf(System.currentTimeMillis()));
				}
				else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					mScanning = false;
					invalidateOptionsMenu();
					ListView lv = (ListView) mSectionsPagerAdapter.getItem(1).getView().findViewById(R.id.device_list);
					deviceStatsAdapter = new BTDeviceStatsAdapter(context, R.layout.fragment_btdevice_list_stat, deviceList);
					lv.setAdapter(deviceStatsAdapter);
					Log.d(Constants.TAG_APPLICATION, "Discovery process has been stopped: " + String.valueOf(System.currentTimeMillis()));
				}
			}
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		registerReceiver(BTFoundReceiver, filter);
		Intent btServiceIntent = new Intent(this, BTService.class);
		btServiceIntent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(mHandler));
		bindService(btServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		//startService(btServiceIntent);
		
		// adapters
		deviceLogsAdapter = new BTDeviceLogsAdapter(context, R.layout.fragment_btdevice_list_log, logList);
	}

	@Override
	protected void onDestroy() {
		Log.d(Constants.TAG_APPLICATION, "onDestroy()");
		super.onDestroy();
		unbindService(mServiceConnection);
		mBTService = null;
	}

	@Override
	protected void onResume() {
		Log.d(Constants.TAG_APPLICATION, "onResume()");
		super.onResume();
	}

	private void startScanService(boolean scanStart) {
		if (scanStart){ // if command is to start scanning
			if (mBluetoothAdapter.isDiscovering()){ // if scan is already started, stop the current scanning
				mBluetoothAdapter.cancelDiscovery();
			}
			// set device state to outdated
			for (BTDevice device : deviceList){
				device.setConnState(Constants.STATE_CLIENT_OUTDATED);
			}
			mBluetoothAdapter.startDiscovery();
			// Cancel the discovery process after SCAN_INTERVAL
			final Handler discoveryHandler = new Handler();
			discoveryHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mBluetoothAdapter.isDiscovering()){
						mBluetoothAdapter.cancelDiscovery();
					}
				}
			}, SCAN_INTERVAL);
		}
		else{ // if command is to stop scanning
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode){
		case REQUEST_BT_ENABLE:
			if (resultCode == RESULT_OK) {
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is enabled by the user.");
				Intent discoverableIntent = new
						Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, RESULT_BT_DISCOVERABLE_DURATION);
				startActivityForResult(discoverableIntent, REQUEST_BT_DISCOVERABLE);
			}
			else{
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is not enabled by the user.");
			}
			break;
		case REQUEST_BT_DISCOVERABLE:
			if (resultCode == RESULT_CANCELED){
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is not discoverable.");
			}
			else{
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is discoverable by 300 seconds.");
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.btdevice_list, menu);
		if (!mScanning) {
			menu.findItem(R.id.action_stop).setVisible(false);
			menu.findItem(R.id.action_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.action_stop).setVisible(true);
			menu.findItem(R.id.action_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id){
		case R.id.action_settings:
			break;
		case R.id.action_scan:
			Log.d(Constants.TAG_APPLICATION, "Click scan button");
			startScanService(true);
			break;
		case R.id.action_stop:
			Log.d(Constants.TAG_APPLICATION, "Click stop button");
			startScanService(false);
			break;
		case R.id.action_set_scaninterval:
			Log.d(Constants.TAG_APPLICATION, "Set scan interval");
			
			break;
		case R.id.action_set_scantime:
			Log.d(Constants.TAG_APPLICATION, "Set scan time");
			ScanDurationSetup scanDurationDialog = new ScanDurationSetup(this);
			scanDurationDialog.setTitle("SET SCAN INTERVAL");
			scanDurationDialog.show();
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		ArrayList<Fragment> fragments = new ArrayList<Fragment>();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class below).
			Fragment fragment = null;
			if (fragments.size() <= position){
				Log.d(Constants.TAG_APPLICATION, "create item at " + String.valueOf(position));
				fragment = PlaceholderFragment.newInstance(position + 1);
				fragments.add(fragment);
			}
			else{
				Log.d(Constants.TAG_APPLICATION, "retrieve item at " + String.valueOf(position));
				fragment = fragments.get(position);
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_btdevice_list, container, false);
			switch(getArguments().getInt(ARG_SECTION_NUMBER)){
			case 1:
				rootView.findViewById(R.id.header_device_rssi).setVisibility(View.VISIBLE);
				rootView.findViewById(R.id.header_device_stat).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_logs_timestamp).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_logs_rssi).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_logs_delay).setVisibility(View.GONE);
				break;
			case 2:
				rootView.findViewById(R.id.header_device_rssi).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_stat).setVisibility(View.VISIBLE);
				rootView.findViewById(R.id.header_device_logs_timestamp).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_logs_rssi).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_logs_delay).setVisibility(View.GONE);				
				break;
			default:
				rootView.findViewById(R.id.header_device_rssi).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_stat).setVisibility(View.GONE);
				rootView.findViewById(R.id.header_device_logs_timestamp).setVisibility(View.VISIBLE);
				rootView.findViewById(R.id.header_device_logs_rssi).setVisibility(View.VISIBLE);
				rootView.findViewById(R.id.header_device_logs_delay).setVisibility(View.VISIBLE);	
			}
			return rootView;
		}
	}

}
