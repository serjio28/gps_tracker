// Simple GPS tracker by Sergey Filippov/serjio28 2012
// Initially was created to check the current altitude
// during mountain trips
//
// version 0.9

package com.serjio28.gps_tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Gps_trackerActivity extends Activity {
	protected static final String TAG = "GPS";
	private TextView mLat, mLng, mAcc, mAlt, mTime, mMarks, mElevation;
	private TextView mAddress;
	private LocationManager mLocationManager;
	private Handler mHandler;
	private static int nmarks = 0;
	boolean gpsEnabled = true;

	// operation definitions
	private static final int UPDATE_ADDRESS = 1;
	private static final int UPDATE_LAT = 2;
	private static final int UPDATE_TIME = 3;
	private static final int UPDATE_ACC = 4;
	private static final int UPDATE_ALT = 5;
	private static final int UPDATE_MARK = 6;
	private static final int UPDATE_LNG = 7;

	private static final int TEN_SECONDS = 10000;
	private static final int TEN_METERS = 10;

	private static String file_name = null;
	private static double elevation_start = 0;
	private static double current_altitude = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// find references to the main fields
		mLat = (TextView) findViewById(R.id.lat);
		mLng = (TextView) findViewById(R.id.lng);
		mAcc = (TextView) findViewById(R.id.acc);
		mAlt = (TextView) findViewById(R.id.alt);
		mTime = (TextView) findViewById(R.id.time);
		mMarks = (TextView) findViewById(R.id.marks);
		mElevation = (TextView) findViewById(R.id.elevation);

		// create file name to keep the values of the data
		file_name = CurrentDateFormatted() + ".txt";

		// define receiver of the main commands from GPS block
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_ADDRESS:
					mAddress.setText((String) msg.obj);
					break;
				case UPDATE_LAT:
					mLat.setText((String) msg.obj);
					break;
				case UPDATE_LNG:
					mLng.setText((String) msg.obj);
					break;
				case UPDATE_TIME:
					mTime.setText((String) msg.obj);
					break;
				case UPDATE_ACC:
					mAcc.setText((String) msg.obj);
					break;
				case UPDATE_ALT:
					mAlt.setText((String) msg.obj);

					double elevation = current_altitude - elevation_start;
					String Selevation = "start:"
							+ String.format("%.2f", elevation_start)
							+ " delta:" + String.format("%.2f", elevation);
					Log.d(TAG, "Start elevation:" + elevation_start
							+ " Current elevation:" + current_altitude);
					mElevation.setText((String) Selevation);

					break;
				case UPDATE_MARK:
					nmarks++;
					String Smarks = nmarks + "";
					if (WriteToFile((String) msg.obj))
						mMarks.setText((String) Smarks);

					break;
				}
			}
		}; // end of mHandler

		// define reactions to the main buttons
		// Set initial elevation and start tracking
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Set elevation");

				elevation_start = current_altitude;

				String Selevation = "start:"
						+ String.format("%.2f", elevation_start) + " delta:0";
				Log.d(TAG, "Start elevation:" + elevation_start
						+ " Current elevation:" + current_altitude);
				mElevation.setText((String) Selevation);

			}
		});

		// define exit button
		final Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Good bye");
				finish();
			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (gpsEnabled) {
			setup();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// take care of GPS 
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		gpsEnabled = mLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!gpsEnabled) {
			// GPS is missing or turned off - inform the user
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("GPS location is turned off!")
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			setup();
		}
		;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(gpsEnabled){
			mLocationManager.removeUpdates(listener);
		};
	}

	private void setup() {
		Location gpsLocation = null;
		mLocationManager.removeUpdates(listener);

		// register GPS event receiver
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TEN_SECONDS,
					TEN_METERS, listener);
			gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		};
		
		// make a first update
		if (gpsLocation != null)
			updateLocation(gpsLocation);
	}

	private void updateLocation(Location location) {
		double alt, latitude, longitude;
		float bear, speed, acc;

		long getTime = location.getTime();

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		String FgetTime = dateFormat.format(getTime);

		// read GPS data and send them to handlers 
		if (location.hasAccuracy()) {
			acc = location.getAccuracy();
		} else {
			acc = 0;
		}
		;

		if (location.hasAltitude()) {
			alt = location.getAltitude();
		} else {
			alt = 0;
		}
		;

		if (location.hasBearing()) {
			bear = location.getBearing();
		} else {
			bear = 0;
		}
		;

		if (location.hasSpeed()) {
			speed = location.getSpeed();
		} else {
			speed = 0;
		}
		;
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		current_altitude = alt;

		String toMark = getTime + ";" + latitude + ";" + longitude + ";" + alt
				+ ";" + acc + ";" + bear + ";" + speed + ";";

		Message.obtain(mHandler, UPDATE_LAT, String.format("%f", latitude))
				.sendToTarget();

		Message.obtain(mHandler, UPDATE_LNG, String.format("%f", longitude))
				.sendToTarget();

		Message.obtain(mHandler, UPDATE_TIME, FgetTime).sendToTarget();

		Message.obtain(mHandler, UPDATE_ACC, acc + " meters").sendToTarget();

		Message.obtain(mHandler, UPDATE_ALT,
				String.format("%.2f", alt) + " meters").sendToTarget();

		Message.obtain(mHandler, UPDATE_MARK, toMark).sendToTarget();

	}

	public static String CurrentDateFormatted() {
		String ret;

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"HH_mm_ss_yyyy_MM_dd");
		Date date = new Date();
		ret = dateFormat.format(date);

		return ret;
	};

	private boolean WriteToFile(String data) {
		try {
			// Write data to a file to keep tracking
			File extStore = Environment.getExternalStorageDirectory();
			String SD_PATH = extStore.getAbsolutePath();

			File gps_data = new File(SD_PATH + "/" + file_name);

			FileWriter writer = new FileWriter(gps_data, true);
			writer.append(data + "\n");
			writer.flush();
			writer.close();
			Log.d(TAG, "Data saved OK");

		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
			return false;
		}
		return true;
	}

	// the listener to receive GPS events
	private final LocationListener listener = new LocationListener() {

		public void onLocationChanged(Location location) {
			updateLocation(location);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
}
