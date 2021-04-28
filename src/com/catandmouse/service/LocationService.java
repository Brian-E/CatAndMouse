package com.catandmouse.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.catandmouse.CMConstants;
import com.catandmouse.CMIntentActions;
import com.catandmouse.Game;
import com.catandmouse.LogUtil;
import com.catandmouse.CMApplication;
import com.catandmouse.ServerUtil;
import com.openfeint.api.resource.User;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service {
	LocationManager locationManager;
	LocationListener locationListener;
	Timer locationTimer;
	long lastUpdateTime;
	static final long locationUpdateThreshold = CMConstants.TIME_MILLISECONDS_1_MIN;
	Location lastLocation;
	
	public void onCreate() {
		lastUpdateTime = 0;
		// Register some listeners
        IntentFilter filter = new IntentFilter(CMIntentActions.ACTION_QUIT_GAME);
        this.registerReceiver(mReceiver, filter);
		
        filter = new IntentFilter(CMIntentActions.ACTION_LOGOUT);
        this.registerReceiver(mReceiver, filter);
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		    	User user = ((CMApplication)getApplicationContext()).user;
		    	List<Game> gamesList = ((CMApplication)getApplicationContext()).dh.selectAll();
		    	// Are we in any games?
		    	if (gamesList.size()>0) {
		    		// Are we due for an update?
		    		if (System.currentTimeMillis() - locationUpdateThreshold > lastUpdateTime) {
		    			if (user!=null) {
		    				LogUtil.info("LocationService updating location");
		    				ServerUtil.doLocationUpdate(user.userID(), location.getLatitude(), location.getLongitude());
		    				lastUpdateTime = System.currentTimeMillis();
		    				lastLocation = location;
		    			}
		    			else {
		    				LogUtil.error("no user retrieved for location update!");
			    			// User is null, force an update
			    			((CMApplication)getApplicationContext()).loadUser();
		    			}
		    		}
		    	}
		    	else {
		    		// No games, kill myself
		    		stopSelf();
		    	}
		    }

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}

			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			}
		  };
		  
		  // setup my location timer to keep me alive in the game
		  locationTimer = new Timer();

	}
	
	private class LocationTimerTask extends TimerTask {

		public void run() {
			// update my location
			// Are we due for an update?
			if (System.currentTimeMillis() - locationUpdateThreshold > lastUpdateTime) {
				Location location = null;
				List<String> providers = locationManager.getProviders(true);
				try {
					for (int i=0; i < providers.size(); i++) {
						location = locationManager.getLastKnownLocation(providers.get(i));
					}				
				}
				catch (Exception e) {
					LogUtil.error("Error getting location: "+e.toString());
				}
				
				// which location do we use?
				if (location!=null)
					lastLocation = location;
				
				if (lastLocation!=null) {
			    	User user = ((CMApplication)getApplicationContext()).user;
			    	List<Game> gamesList = ((CMApplication)getApplicationContext()).dh.selectAll();
			    	// Are we in any games?
			    	if (gamesList.size()>0) {
			    		if (user!=null) {

			    			LogUtil.info("LocationService updating location");
			    			ServerUtil.doLocationUpdate(user.userID(), lastLocation.getLatitude(), lastLocation.getLongitude());
			    			lastUpdateTime = System.currentTimeMillis();
			    		}
			    		else {
			    			// User is null, force an update
			    			((CMApplication)getApplicationContext()).loadUser();
			    		}
			    	}
			    	else {
			    		// No games, kill myself
			    		stopSelf();
			    	}
				}
				else {
					LogUtil.warn("No location acquired for update");
				}
			}
		}
	}
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            LogUtil.info("LocationService processing broadcast: "+action);
            
            if (action.equals(CMIntentActions.ACTION_QUIT_GAME)) {
            	// If we're not in any games, unregister
            	List<Game> gamesList = ((CMApplication)getApplicationContext()).dh.selectAll();
            	if (gamesList.size()==0) {
            		// kill myself
            		stopSelf();
            	}
            }
            else if (action.equals(CMIntentActions.ACTION_LOGOUT)) {
            	stopSelf();
            }
		}
    };

	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
		locationTimer.cancel();
		this.unregisterReceiver(mReceiver);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.info("Location Service started");
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, locationListener);
		// Perhaps a user option whether they want to use GPS or not?
		//SharedPreferences settings = getSharedPreferences(CMConstants.SETTINGS, MODE_PRIVATE);
		//if (settings.getBoolean(CMConstants.SETTING_GENERAL_USE_GPS, true))
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, locationListener);
		
		// start timer
	    locationTimer.scheduleAtFixedRate(new LocationTimerTask(), CMConstants.TIME_MILLISECONDS_1_MIN*4, CMConstants.TIME_MILLISECONDS_1_MIN*4);
		
		return START_STICKY;
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}
}
