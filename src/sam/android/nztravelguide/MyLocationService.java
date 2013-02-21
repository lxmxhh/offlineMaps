package sam.android.nztravelguide;

import sam.android.nztravelguide.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class MyLocationService extends Service implements LocationListener, SensorEventListener {
	
	private NotificationManager mNotificationManager;
	private SensorManager mSensorManager;
	private WakeLock wakeLock;
	
	private long minTime; // ms
	private float minDistance; // m
	private Handler locationHandler;

	private double variation; // Magnetic variation.
	
	public MyLocationService() {
		minTime = NZTravelGuide.minTime;
		minDistance = NZTravelGuide.minDistance;
		locationHandler = NZTravelGuide.locationHandler;
	}
		
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("MyLocationService", "Service: onCreate()");
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (locationHandler == null) {
			// stop service to avoid error when recording a track and being killed manually
			Intent intent = new Intent(this, MyLocationService.class);
			this.stopService(intent);
		} else {
			if (!NZTravelGuide.isGPSTracking) {
				// stop service to avoid error when recording a track and being killed manually
				Intent intent = new Intent(this, MyLocationService.class);
				this.stopService(intent);
				clearNotification();
			} else {
				// when normally start service
				showNotification();
				acquireWakeLock();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("MyLocationService", "Service: onDestroy()");
		clearNotification();
		releaseWakeLock();
	}
	
	public void registerSensor(Context context) {
		if (NZTravelGuide.currentLocation != null)
			setVariation(NZTravelGuide.currentLocation);
		
		mSensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
		if (mSensorManager != null) {
			Sensor compass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			if (compass != null) {
				mSensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_UI);
//				Log.d("MyLocationService", "SensorManager.registerListener()");
			}
		}
	}

	protected void unregisterSensor() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
			mSensorManager = null;
//			Log.d("MyLocationService", "SensorManager.unregisterListener()");
		}
	}
	
	@Override
	public IBinder onBind(Intent i) {
		return null;
	}
	
	private void showNotification() {
		int iconId = 0;
		String contentTitle = null;
		String contentText = null;
		
		iconId = R.drawable.globe;
		contentTitle = getString(R.string.app_name);
		contentText = getString(R.string.notify_recording);
		
		Intent notifyIntent = new Intent(this, NZTravelGuideTracks.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				this, 0, notifyIntent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		Notification notification = new Notification();
		notification.flags = Notification.FLAG_NO_CLEAR;
		notification.icon = iconId;
		notification.defaults = Notification.DEFAULT_SOUND;
		
		notification.setLatestEventInfo(this, contentTitle, contentText, pendingIntent);
		mNotificationManager.notify(1, notification);
	}
	
	private void clearNotification() {
		mNotificationManager.cancel(1);
	}
	
	private void acquireWakeLock() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (wakeLock == null) {
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
			if (!wakeLock.isHeld()) {
				wakeLock.acquire();
			}
		}
	}
	
	private void releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}
	
	private void moveMarker(Location location) {
		//addMarker(location, PhysicMap.getZoomLevel());
		Message m = locationHandler.obtainMessage(NZTravelGuide.MethodAddMarker, 0, 0, location);
		locationHandler.sendMessage(m);
		//mapControl.invalidate(); // not works if leaving activity and entering again
		//mapControl.updateScreen(); // works
		m = locationHandler.obtainMessage(NZTravelGuide.MethodUpdateScreen, 0, 0, null);
		locationHandler.sendMessage(m);
	}
	
	@Override
	public void onLocationChanged(Location location) {
//		String longitude = String.valueOf(location.getLongitude());
//		String latitude = String.valueOf(location.getLatitude());
//		Log.i("Location", location.getProvider()+" onLocationChanged(): latitude="+latitude+", longitude="+longitude);
		// Recalculate the variation if there was a jump in location > 1km:
		if (NZTravelGuide.currentLocation == null
				|| location.distanceTo(NZTravelGuide.currentLocation) > 1000) {
			setVariation(location);
		}
		NZTravelGuide.currentLocation = location;
		NZTravelGuide.inHome = true;
//		BigPlanet.isMapInCenter = false;
		if (!NZTravelGuide.isGPSTracking) {
			if (NZTravelGuide.isFollowMode) {
				//goToMyLocation(location, PhysicMap.getZoomLevel());
				Message m = locationHandler.obtainMessage(NZTravelGuide.MethodGoToMyLocation, 0, 0, location);
				locationHandler.sendMessage(m);
			} else {
				moveMarker(location);
			}
		} else { // isGPSTracking = true
			if ((location.hasAccuracy() && location.getAccuracy()<50) || !location.hasAccuracy()) {
				if (NZTravelGuide.isFollowMode) {
					//trackMyLocation(location, PhysicMap.getZoomLevel());
					Message m = locationHandler.obtainMessage(NZTravelGuide.MethodTrackMyLocation, 0, 0, location);
					locationHandler.sendMessage(m);
				} else {
					moveMarker(location);
				}
			}
		}
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		Log.i("Location", provider + " is disabled.");
		if (provider.equals("gps")) {
			String networkProvider = LocationManager.NETWORK_PROVIDER;
			try {
				NZTravelGuide.locationManager.requestLocationUpdates(networkProvider, minTime, minDistance, NZTravelGuide.networkLocationListener);
				Log.i("Location", networkProvider +" requestLocationUpdates() "+ minTime +" "+ minDistance);
			} catch (RuntimeException e) {
			}
		}
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Log.i("Location", provider + " is enabled.");
		if (provider.equals("gps")) {
			try {
				NZTravelGuide.locationManager.requestLocationUpdates(provider, minTime, minDistance, NZTravelGuide.gpsLocationListener);
			} catch (RuntimeException e) {
			}
		} else {
			try {
				NZTravelGuide.locationManager.requestLocationUpdates(provider, minTime, minDistance, NZTravelGuide.networkLocationListener);
			} catch (RuntimeException e) {
			}
		}
		Log.i("Location", provider +" requestLocationUpdates() "+ minTime +" "+ minDistance);
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		int numSatellites = extras.getInt("satellites", 0);
		NZTravelGuide.locationProvider = provider+" "+status+" "+numSatellites;
		if (status == 0) {
			Log.i("Location", provider + " is OUT OF SERVICE");
		} else if (status == 1) {
			Log.i("Location", provider + " is TEMPORARILY UNAVAILABLE");
			// invoke network's requestLocationUpdates() if not tracking
			if (provider.equals("gps") && !NZTravelGuide.isGPSTracking) {
				String networkProvider = LocationManager.NETWORK_PROVIDER;
				try {
					NZTravelGuide.locationManager.requestLocationUpdates(networkProvider, minTime, minDistance, NZTravelGuide.networkLocationListener);
					Log.i("Location", networkProvider +" requestLocationUpdates() "+ minTime +" "+ minDistance);
				} catch (RuntimeException e) {
				}
			}
		} else {
			Log.i("Location", provider + " is AVAILABLE");
			// gpsLocationListener has higher priority than networkLocationListener
			if (provider.equals("gps")) {
				NZTravelGuide.locationManager.removeUpdates(NZTravelGuide.networkLocationListener);
			}
		}
		//BigPlanet.setActivityTitle(BigPlanet.this);
		provider = provider+" "+status+" "+numSatellites;
		String title = NZTravelGuide.getTitle(provider);
		if (NZTravelGuide.titleHandler != null) {
			Message m = NZTravelGuide.titleHandler.obtainMessage(NZTravelGuideTracks.SetTitle, 0, 0, title);
			NZTravelGuide.titleHandler.sendMessage(m);
		}
	}
	
	private void setVariation(Location location) {
		long timestamp = location.getTime();
		if (timestamp == 0) {
			// Hack for Samsung phones which don't populate the time field
			timestamp = System.currentTimeMillis();
		}

		GeomagneticField field = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), timestamp);
		variation = field.getDeclination();
		
//		Log.d("MyLocationService", "Variation reset to " + variation + " degrees.");
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			float magneticHeading = event.values[0];
			double heading = magneticHeading + variation;
			if (NZTravelGuide.setHeading((float) heading)) {
				//mapControl.invalidate(); // not works if leaving activity and entering again
				//mapControl.updateScreen(); // works
				Message m = locationHandler.obtainMessage(NZTravelGuide.MethodUpdateScreen, 0, 0, null);
				locationHandler.sendMessage(m);
			}
		}
	}
	
}