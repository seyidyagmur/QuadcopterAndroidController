package net.milgar.quad;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

	ImageButton btn_ileri;
	ImageButton btn_sag;
	ImageButton btn_sol;
	ImageButton btn_geri;
	TextView tv;
	ImageButton btn_baglan;
	ImageButton btn_basla;
	ImageButton btn_kes;
	ImageButton btn_alcal;
	int ax, ay, az;
	OutputStream outstream;
	SensorManager sensorManager;
	Sensor sensor;
	ArrayAdapter<String> adaptorlist;
	ListView liste;
	BluetoothAdapter blt;
	Set<BluetoothDevice> arraydevice;
	ArrayList<String> eslesen;
	ArrayList<BluetoothDevice> aygitlar;
	public static final UUID mmuuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final int baglanti = 0;
	protected static final int mesajoku = 1;
	IntentFilter filtre;
	BroadcastReceiver receiver;
	boolean isConnected = false;
	boolean sabit = true;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				case baglanti:
					isConnected = true;
					ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
					Toast.makeText(getApplicationContext(), "Baglandý", Toast.LENGTH_SHORT).show();
					String s = "successfully connected";
					tv.setText("Baglandi!");
					runnable.run();

					break;
				case mesajoku:
					byte[] readBuf = (byte[]) msg.obj;
					String string = new String(readBuf);
					Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ax = 0;
		ay = 0;
		az = 0;
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if ((sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)) != null)
			System.out.println("Basrili");
		btn_ileri = (ImageButton) findViewById(R.id.ileri);
		btn_sag = (ImageButton) findViewById(R.id.sag);
		btn_sol = (ImageButton) findViewById(R.id.sol);
		btn_geri = (ImageButton) findViewById(R.id.geri);
		tv = (TextView) findViewById(R.id.durum);
		btn_baglan = (ImageButton) findViewById(R.id.btn_baglan);
		btn_basla = (ImageButton) findViewById(R.id.btn_basla);
		btn_kes = (ImageButton) findViewById(R.id.btn_kes);
		btn_alcal = (ImageButton) findViewById(R.id.btn_alcal);
		liste = (ListView) findViewById(R.id.listem);
		liste.setOnItemClickListener(this);
		adaptorlist = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
		liste.setAdapter(adaptorlist);
		blt = BluetoothAdapter.getDefaultAdapter();
		eslesen = new ArrayList<String>();
		filtre = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		aygitlar = new ArrayList<BluetoothDevice>();
		tv.setText("Baglanti yok!");

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) {

					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					aygitlar.add(device);
					String s = "";

					for (int a = 0; a < eslesen.size(); a++) {
						if (device.getName().equals(eslesen.get(a))) {

							s = "(Eslesti)";
							break;
						}
					}

					adaptorlist.add(device.getName() + "" + s + "" + "\n" + device.getAddress());
				} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					if (blt.getState() == blt.STATE_OFF) {

						bltac();
					}
				}

			}


		};

		registerReceiver(receiver, filtre);
		filtre = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filtre);


		if (blt == null) {
			Toast.makeText(getApplicationContext(), "Bluetooth aygýtlarý bulunamadý", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			if (!blt.isEnabled()) {
				bltac();
			}

			getPairedDevices();
			startDiscovery();


			btn_ileri.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sabit = false;
				}
			});
			btn_geri.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sabit = true;
				}
			});

			btn_kes.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						if(isConnected)
						dur();
					} catch (IOException e) {
						// TODO Auto-generated catch block

					}

				}
			});

			btn_basla.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						if(isConnected)
						basla();
					} catch (IOException e) {
						// TODO Auto-generated catch block

					}

				}
			});
			btn_baglan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {

						if(isConnected)
						baglan();
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}

				}
			});
			btn_alcal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {

						if(isConnected)
						alcal();
					} catch (IOException e) {
						// TODO Auto-generated catch block

					}

				}
			});

		}


	}

	public void onResume() {
		super.onResume();
		sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onStop() {
		super.onStop();
		sensorManager.unregisterListener(gyroListener);
		//unregisterReceiver(receiver);
	}

	public SensorEventListener gyroListener = new SensorEventListener() {
		public void onAccuracyChanged(Sensor sensor, int acc) {
		}

		public void onSensorChanged(SensorEvent event) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			ax = (int) x;
			ay = (int) y;
			az = (int) z;
			System.out.println("sonuc: "+ay+" roll: "+az);

		}
	};

	private void startDiscovery() {
		// TODO Auto-generated method stub
		blt.cancelDiscovery();
		blt.startDiscovery();

	}

	private void bltac() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		tv.setText("Baglanti kapandi!");

		startActivityForResult(intent, 1);
	}

	private void getPairedDevices() {
		// TODO Auto-generated method stub
		arraydevice = blt.getBondedDevices();
		if (arraydevice.size() > 0) {
			for (BluetoothDevice device : arraydevice) {
				eslesen.add(device.getName());

			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Kontrol islemi için bluetoothun acik olmasi gerekir", Toast.LENGTH_SHORT).show();
			tv.setText("Bluetooth kapali!");
			finish();
		}
	}


	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
		// TODO Auto-generated method stub

		if (blt.isDiscovering()) {
			blt.cancelDiscovery();
		}
		if (adaptorlist.getItem(arg2).contains("Eslesti")) {

			BluetoothDevice selectedDevice = aygitlar.get(arg2);
			ConnectThread connect = new ConnectThread(selectedDevice);
			connect.start();

		} else {
			Toast.makeText(getApplicationContext(), "Cihazlar eslesmedi", Toast.LENGTH_SHORT).show();
			tv.setText("Baglanti yok!");
		}
	}

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		public void run() {
			//
			// Do the stuff
			//
			try {
				Acilar();
			} catch (IOException e) {
				e.printStackTrace();
			}

			handler.postDelayed(this, 500);
		}
	};


	private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {

			BluetoothSocket tmp = null;
			mmDevice = device;

			try {

				tmp = device.createRfcommSocketToServiceRecord(mmuuid);
			} catch (IOException e) {

			}
			mmSocket = tmp;
		}

		public void run() {

			blt.cancelDiscovery();

			try {

				mmSocket.connect();

			} catch (IOException connectException) {

				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			mHandler.obtainMessage(baglanti, mmSocket).sendToTarget();
		}


		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			outstream = tmpOut;
		}

		public void run() {
			byte[] buffer;
			int bytes;


			while (true) {
				try {

					buffer = new byte[1024];
					bytes = mmInStream.read(buffer);

					mHandler.obtainMessage(mesajoku, bytes, -1, buffer)
							.sendToTarget();

				} catch (IOException e) {
					break;
				}
			}
		}


		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	public void Acilar() throws IOException {
		String yon = "";

		int pitch = az;
		int roll = ay;

		if(sabit){
			pitch = 0;
			roll = 0;
		} else {
			roll = roll > 30 ? 30 : roll;
			roll = roll < -30 ? -30 : roll;
			pitch = pitch > 30 ? 30 : pitch;
			pitch = pitch < -30 ? -30 : pitch;
		}

		outstream.write(("p"+pitch+"\nr"+roll+"\n").getBytes());

	}

	public void dur() throws IOException {
		outstream.write("i5\n".getBytes());
	}

	public void basla() throws IOException {
 		outstream.write("i6\n".getBytes());
	}

	public void baglan() throws IOException {
		outstream.write("i7\n".getBytes());
	}

	public void alcal() throws IOException {
		outstream.write("i8\n".getBytes());
	}
}