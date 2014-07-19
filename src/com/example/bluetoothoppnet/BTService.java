package com.example.bluetoothoppnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.bluetoothoppnet.util.Constants;

/**
 * Service API for managing Bluetooth connections and data exchange
 * @author fshi
 *
 */
public class BTService extends Service {

	private final IBinder mBinder = new LocalBinder();

	private static final UUID MY_UUID = UUID.fromString("8113ac40-438f-11e1-b86c-0800200c9a66");

	private BluetoothAdapter mBluetoothAdapter;
	private ServerThread btServer;

	// server/client status
	private boolean serverRunning = false;

	Messenger mMessenger = null;

	public BTService(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	/**
	 * BT Server thread
	 * @author fshi
	 *
	 */
	private class ServerThread extends Thread {
		private final BluetoothServerSocket btServerSocket;
		ArrayList<ConnectedThread> connections = new ArrayList<ConnectedThread>();

		public ServerThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.STR_APPLICATION_NAME, MY_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			btServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			serverRunning = true;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				Log.d(Constants.TAG_APPLICATION, "BT server waiting for incoming connections");
				try {
					socket = btServerSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				// If a connection was accepted
				if (socket != null && socket.isConnected()) {
					if(mBluetoothAdapter.isDiscovering()){
						mBluetoothAdapter.cancelDiscovery();
					}
					// Do work to manage the connection (in a separate thread)
					Log.d(Constants.TAG_APPLICATION, "Connected");//manageConnectedSocket(socket);
					// start a new thread to handling data exchange
					ConnectedThread newConn = new ConnectedThread(socket);
					newConn.start();
					connections.add(newConn);
				}
			}
			try {
				btServerSocket.close();
				for(int i = 0; i < connections.size(); i++){
					connections.get(i).cancel();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverRunning = false;
			return;
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				btServerSocket.close();
			} catch (IOException e) { }
		}
	}


	// thread to handle incoming client request and messages, only input stream is available
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = mSocket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Object buffer;

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					ObjectInputStream in = new ObjectInputStream(mmInStream);
					buffer = in.readObject();
					// Send the obtained bytes to the UI activity
					try {
						JSONObject m;
						m = new JSONObject(buffer.toString());
						// Send the obtained bytes to the UI Activity
						Log.d(Constants.TAG_APPLICATION, m.get(Constants.DATA_TIMESTAMP).toString());
						Message msg=Message.obtain();

						Bundle b = new Bundle();
						b.putLong(Constants.DATA_TIMESTAMP, m.getLong(Constants.DATA_TIMESTAMP));
						msg.what = Constants.WHAT_DATA_TIMESTAMP;
						msg.setData(b);
						mMessenger.send(msg);

						// send ACK back to the client to terminate the client
						JSONObject ackToSend = new JSONObject();
						try {
							ackToSend.put(Constants.DATA_TIMESTAMP, System.currentTimeMillis());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// Perform the write unsynchronized
						new ObjectOutputStream(mmOutStream).writeObject(ackToSend.toString());
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
					// stop the connected thread
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					break;
				}
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class ClientThread extends Thread {
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mBTDevice;

		public ClientThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mBTDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = mBTDevice.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) { }
			mSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			if(mBluetoothAdapter.isDiscovering()){
				mBluetoothAdapter.cancelDiscovery();
			}
			long timestampConnect = System.currentTimeMillis();
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				// stop the connection after
				
				mSocket.connect();
				Message msg=Message.obtain();
				Bundle b = new Bundle();
				b.putString(Constants.DATA_DEVICE_MAC, mBTDevice.getAddress());
				msg.setData(b);
				msg.what = Constants.WHAT_CLIENT_CONNECTED;
				try {
					mMessenger.send(msg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mSocket.close();
				} catch (IOException closeException) { }
				return;
			}
			// Do work to manage the connection (in a separate thread)
			Log.d(Constants.TAG_APPLICATION, "client connected to the server");
			OutputStream mOutStream = null;
			InputStream mInStream = null;
			Object buffer;
			try {
				mOutStream = mSocket.getOutputStream();
				mInStream = mSocket.getInputStream();

				//prepare data
				JSONObject dataToSend = new JSONObject();
				try {
					dataToSend.put(Constants.DATA_TIMESTAMP, System.currentTimeMillis());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Perform the write unsynchronized
				// timestamp_write
				long timestampWrite = System.currentTimeMillis();
				new ObjectOutputStream(mOutStream).writeObject(dataToSend.toString());
				long timestampWriteFinished = System.currentTimeMillis();
				// expect some acknowledge from server
				try {
					// Read from the InputStream
					ObjectInputStream in = new ObjectInputStream(mInStream);
					buffer = in.readObject();
					long timestampAckReceived = System.currentTimeMillis();
					// Send the obtained bytes to the UI activity
					try {
						JSONObject m;
						m = new JSONObject(buffer.toString());
						// Send the obtained bytes to the UI Activity
						Log.d(Constants.TAG_APPLICATION, m.get(Constants.DATA_TIMESTAMP).toString());
						Message msg=Message.obtain();

						Bundle b = new Bundle();
						b.putLong(Constants.DATA_TIMESTAMP, m.getLong(Constants.DATA_TIMESTAMP));
						b.putString(Constants.DATA_DEVICE_MAC, mBTDevice.getAddress());
						b.putLong(Constants.LOG_TIMESTAMP_CONNECT, timestampConnect);
						b.putLong(Constants.LOG_TIMESTAMP_WRITE, timestampWrite);
						b.putLong(Constants.LOG_TIMESTAMP_WRITE_FINISHED, timestampWriteFinished);
						b.putLong(Constants.LOG_TIMESTAMP_ACK_RECEIVED, timestampAckReceived);
						msg.what = Constants.WHAT_DATA_CLIENT_TIMING_LOG;
						msg.setData(b);
						mMessenger.send(msg);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
					// stop the connected thread
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				mSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d(Constants.TAG_APPLICATION, "client finished");
			return;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(Constants.TAG_APPLICATION, "onStartCommand");
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(Constants.TAG_APPLICATION, "onBind");
		// TODO Auto-generated method stub
		Bundle extras=intent.getExtras();

		if (extras!=null) {
			mMessenger=(Messenger)extras.get(Constants.EXTRA_MESSENGER);	
		}
		return mBinder;
	}

	public class LocalBinder extends Binder {
		BTService getService() {
			return BTService.this;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that BluetoothGatt.close() is called
		// such that resources are cleaned up properly.  In this particular example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}

	public void close() {
		// clean up
		stopServer();
		return;
	}

	public int startServer(){
		if (mBluetoothAdapter == null) {
			Log.e(Constants.TAG_APPLICATION, "Unable to obtain a BluetoothAdapter.");
		}
		btServer = new ServerThread();
		btServer.start();
		return 1;
	}

	public int stopServer(){
		if(serverRunning){
			btServer.cancel();
		}
		return 1;
	}

	/**
	 * connect to a BT device
	 * @return
	 */
	public int connect(BluetoothDevice btDevice){
		if (mBluetoothAdapter == null) {
			Log.e(Constants.TAG_APPLICATION, "Unable to obtain a BluetoothAdapter.");
			return 0;
		}
		ClientThread btClient = new ClientThread(btDevice);
		btClient.start();
		return 1;
	}

	/**
	 * disconnect from a BT device
	 */
	public int disconnect() {
		if (mBluetoothAdapter == null) {
			Log.d(Constants.TAG_APPLICATION, "BluetoothAdapter not initialized");
			return 0;
		}
		return 1;
	}


}
