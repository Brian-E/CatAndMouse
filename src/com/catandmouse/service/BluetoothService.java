package com.catandmouse.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.catandmouse.CMApplication;
import com.catandmouse.CMIntentActions;
import com.catandmouse.Game;
import com.catandmouse.GameManagerActivity;
import com.catandmouse.LogUtil;
import com.catandmouse.CMNotification;
import com.catandmouse.CMConstants;
import com.catandmouse.R;
import com.catandmouse.ServerUtil;
import com.openfeint.api.resource.User;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

public class BluetoothService extends Service {

	private static final String CM_STRING="-c&m";
	private static final String CM_GAME_SEPARATOR=",";
	
	private Map<Integer,Vector<String>> mapCatsWhoCaughtMe;
	private BluetoothAdapter mBluetoothAdapter = null;
	private String origBTName = null;
	protected SharedPreferences settings;
	private Timer syncTimer;
	private boolean bDiscoveryScheduled = false;
	
	public void onCreate() {
		// Initialization
        settings = getSharedPreferences(CMConstants.SETTINGS, MODE_PRIVATE);
		
		mapCatsWhoCaughtMe = new HashMap<Integer,Vector<String>>();
		
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter==null) {
        	// catastrophic!  We shouldn't be here
        	LogUtil.error("Bluetooth adapter is null!");
        	stopSelf();
        }
        
        if (!mBluetoothAdapter.isEnabled()) {
        	// also catastrophic!  we shouldn't be here
        	LogUtil.error("Bluetooth adapter is not enabled!");
        	stopSelf();
        }
        	
        origBTName = mBluetoothAdapter.getName();
        if (origBTName==null)
        	origBTName = "";
        
		// Register for all the intents I care about
		// Blutooth actions
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when the scan mode has changed
        filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        
        // Register for broadcasts when the bluetooth state has changed
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        
        // Cat and Mouse Actions
		for (int i=0; i < CMIntentActions.CM_ACTIONS.length; i++) {
			filter = new IntentFilter(CMIntentActions.CM_ACTIONS[i]);
			this.registerReceiver(mReceiver, filter);
		}   
		
		// setup the sync timer
		syncTimer = new Timer();
		syncTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				SyncTask st = new SyncTask();
				st.start();
			}
			
		}, CMConstants.TIME_MILLISECONDS_1_MIN*2, CMConstants.TIME_MILLISECONDS_1_MIN*2);
        
    }

	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.info("Bluetooth Service started");
		// Intent should contain game number
		// If I just joined a reverse game, probably want to go into discoverable mode
		if (intent!=null) {
			Game game = ((CMApplication)getApplicationContext()).dh.selectGame(intent.getIntExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, 0));
			if (game!=null) {
				if (game.getGameType()==CMConstants.GAME_TYPE_REVERSE) {
					sendCatNotification(game.getGameNumber());
				}
				else {
					scheduleDiscovery();
				}
			}
		}
		
		return START_STICKY;
	}

	public void onDestroy() {
		LogUtil.info("Bluetooth Service destroyed");
		this.unregisterReceiver(mReceiver);
		
		syncTimer.cancel();
		
		// set the name back to the original
		if (mBluetoothAdapter!=null && origBTName!=null && mBluetoothAdapter.getName()!=null && !mBluetoothAdapter.getName().equals(origBTName))
			mBluetoothAdapter.setName(origBTName);
	}

	public IBinder onBind(Intent arg0) {
		// Nobody binds to me!
		return null;
	}
	
    private void doDiscovery() {
        LogUtil.info("doDiscovery()");

        // If we're not already discovering, start it
        if (!mBluetoothAdapter.isDiscovering()) {
            // Request discover from BluetoothAdapter
        	LogUtil.info("BluetoothService starting discovery");
            mBluetoothAdapter.startDiscovery();
        }
    }   	
    
// Perhaps in the future we use this function that checks to see if we should be doing a discovery or not    
//    private void doDiscovery() {
//        LogUtil.info("doDiscovery()");
//
//        // If we're not already discovering, start it
//        if (!mBluetoothAdapter.isDiscovering()) {
//        	// Let's make sure we should be doing a discovery
//        	boolean bDoDiscovery = false;
//        	List<Integer> mouseGameNumbers = ((CMApplication)getApplicationContext()).dh.selectGameNumbers(false);
//        	if (mouseGameNumbers!=null && mouseGameNumbers.size()>0) {
//        		// check each game for the last time we were caught
//        		for (Integer i : mouseGameNumbers) {
//        			Game game = ((CMApplication)getApplicationContext()).dh.selectGame(i);
//        			if (game!=null && (System.currentTimeMillis() - game.getLastCaughtTime() > CMConstants.TIME_MILLISECONDS_5_MIN)) {
//        				bDoDiscovery = true;
//        				break;
//        			}
//        		}
//        		
//        		if (bDoDiscovery) {
//        			// Request discover from BluetoothAdapter
//        			LogUtil.info("BluetoothService starting discovery");
//        			mBluetoothAdapter.startDiscovery();
//        		}
//        	}
//        }
//    }       
    
    private void scheduleDiscovery() {
    	bDiscoveryScheduled = true;
    	// After 5 seconds, kick off a discovery
    	Timer timer = new Timer();
    	timer.schedule(new TimerTask(){
    		public void run() {
    			doDiscovery();
    			bDiscoveryScheduled = false;
    		}

    	}, 5000);
    }

//		protected Void doInBackground(Void... params) {
//			// After 5 seconds, kick off a discovery
//			Timer timer = new Timer();
//			timer.schedule(new TimerTask(){
//				public void run() {
//					doDiscovery();
//				}
//				
//			}, 5000);
//			return null;
//		}
    	
//    }
    
    private class SyncTask extends Thread {

		@Override
		public void run() {
			boolean bMouseCheck = false;
			boolean bCatCheck = false;
			// make sure we're running discoveries and/or our cat name is right
			List<Game> gamesList = ((CMApplication)getApplicationContext()).dh.selectAll();
			for (Game game : gamesList) {
				if (game.isCat() && !bCatCheck) {
					String catName = getCatName();
					if (!mBluetoothAdapter.getName().equals(catName))
						mBluetoothAdapter.setName(catName);
					bCatCheck = true;
				}
				else if (!game.isCat() && !bMouseCheck) {
					bMouseCheck = true;
					if (!bDiscoveryScheduled)
						doDiscovery(); // if discovery isn't scheduled because its already running, this checks for that
				}
			}
		}
    	
    }

    private class CatCleanupAsyncTask extends Thread {
    	private int gameNumber;
    	private String catName;

    	public CatCleanupAsyncTask(int GameNumber, String strCatName) {
    		gameNumber = GameNumber;
    		catName = strCatName;
    	}
    	
    	public void run() {
			// After 5 minutes, delete the cat from the specified game
			LogUtil.info("BluetoothService performing cat cleanup");
			Timer timer = new Timer();
			timer.schedule(new TimerTask(){
				public void run() {
					Vector<String> vecCats = mapCatsWhoCaughtMe.get(gameNumber);
					if (vecCats!=null) {
						vecCats.remove(catName);
						if (vecCats.size()==0)
							mapCatsWhoCaughtMe.remove(gameNumber);
					}
				}
				
			}, CMConstants.TIME_MILLISECONDS_5_MIN);
    	}
    	
//		protected Void doInBackground(Object... params) {
//			// After 5 minutes, delete the cat from the specified game
//			LogUtil.info("BluetoothService performing cat cleanup");
//			final Integer gameNumber = (Integer)params[0];
//			final String catName = (String) params[1];
//			Timer timer = new Timer();
//			timer.schedule(new TimerTask(){
//				public void run() {
//					Vector<String> vecCats = mapCatsWhoCaughtMe.get(gameNumber);
//					if (vecCats!=null) {
//						vecCats.remove(catName);
//						if (vecCats.size()==0)
//							mapCatsWhoCaughtMe.remove(gameNumber);
//					}
//				}
//				
//			}, 300000);
//			return null;
//		}
    }
    
    private String getCatName() {
    	// Traverse through all games I'm a cat in and create the appropriate string
    	String catName = null;
		List<Integer> listCatGames = ((CMApplication)getApplicationContext()).dh.selectGameNumbers(true);
		if (listCatGames.size() > 0) {
			// I'm a cat in at least one game. Let's setup my device name
			User user = ((CMApplication)getApplicationContext()).user;
			if (user!=null && user.name!=null && user.name.length() > 0) {
				catName = user.name+CM_STRING;
				for (int i=0; i < listCatGames.size(); i++) {
					if (i > 0)
						catName += CM_GAME_SEPARATOR;
					catName += listCatGames.get(i).toString();
				}
			}
		}    	
		LogUtil.info("BluetoothService created cat name: "+catName);
    	
    	return catName;
    }
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action!=null) {
            	LogUtil.info("BluetoothService processing broadcast: "+action);

            	// Bluetooth actions
            	// When discovery finds a device
            	if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            		// Get the BluetoothDevice object from the Intent
            		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            		String deviceName = device.getName();
            		String macAddress = device.getAddress();

            		// do we care?
            		if (deviceName!=null && deviceName.contains(CM_STRING)) {
            			// OK, we might care. Next step, what game(s) is this player a cat in?
            			String catName = deviceName.substring(0, deviceName.indexOf(CM_STRING));
            			String gameNumbers = null;
            			try {
            				gameNumbers = deviceName.substring(deviceName.lastIndexOf(CM_STRING)+CM_STRING.length());
            			}
            			catch (IndexOutOfBoundsException e) {
            				LogUtil.error("No game numbers on some cat string: "+catName);
            				return;
            			}
            			LogUtil.debug("Game Numbers for this cat:" +gameNumbers);
            			if (gameNumbers!=null && gameNumbers.length()>0) {
            				StringTokenizer st = new StringTokenizer(gameNumbers, CM_GAME_SEPARATOR);
            				int [] iCatGames = new int[st.countTokens()+1];
            				if (st.countTokens()==1) { // the usual I would suspect
            					try {
            						iCatGames[0] = Integer.parseInt(gameNumbers);
            					}
            					catch (NumberFormatException e) {
            						LogUtil.error("Bad cat number: "+gameNumbers);
            						return;
            					}
            				}
            				else {
            					// Cat in more than 1 game...interesting.  OK, parse it
            					for (int i=0; i < st.countTokens() && st.hasMoreTokens(); i++) {
            						String token = st.nextToken();
            						try {
            							iCatGames[i] = Integer.parseInt(token);
            						}
            						catch (NumberFormatException e) {
            							LogUtil.error("Bad cat number: "+gameNumbers);
            							return;
            						}
            					}
            				}

            				// Get all games that I'm a mouse in
            				List<Game> listMouseGames = ((CMApplication)getApplicationContext()).dh.selectAll(false);
            				if (listMouseGames.size() > 0) {
            					// Do any of my games match this cat's games?
            					for (int i=0; i < listMouseGames.size(); i++) {
            						int mouseGame = listMouseGames.get(i).getGameNumber();
            						for (int x=0; x < iCatGames.length; x++) {
            							if (mouseGame == iCatGames[x]) {
            								// OK, you caught me. But have you already caught me?
            								if (mapCatsWhoCaughtMe.containsKey(mouseGame)) {
            									// Somebody already caught me. If this is a reverse game
            									// only one person can catch me
            									if (listMouseGames.get(i).getGameType()==CMConstants.GAME_TYPE_NORMAL) {
            										// Normal game - Was it you?
            										Vector<String> vecCats = mapCatsWhoCaughtMe.get(mouseGame);
            										if (vecCats==null || !vecCats.contains(catName)) {
            											// Now I'm finally caught.  Report this
            											LogUtil.info("Got caught by cat "+catName);
            											if (vecCats==null)
            												vecCats = new Vector<String>();
            											vecCats.add(catName);
            											mapCatsWhoCaughtMe.put(mouseGame, vecCats);

            											// Kick off the task to remove this cat in 5 minutes
            											Thread catCleanupTask = new CatCleanupAsyncTask(new Integer(mouseGame), catName);
            											catCleanupTask.start();

            											// Need my player id
            											User user = ((CMApplication)getApplicationContext()).user;
            											if (user!=null && user.name!=null && user.name.length()>0) {
            												doPlayerCaught(user.userID(), catName, mouseGame, macAddress);
            											}
            											else {
            												LogUtil.error("User or username was null");
            												// remove the cat from the list so it tries again
                											Vector<String> vCats = mapCatsWhoCaughtMe.get(mouseGame);
                											if (vCats!=null) {
                												vCats.remove(catName);
                											}
            											}
            										}
            										else if (vecCats!=null && vecCats.contains(catName)) {
            											LogUtil.info("This cat already caught me");
            										}
            									}
            								}
            								else {
            									// I'm caught. Report it and add this guy
            									LogUtil.info("Got caught by cat "+catName);
            									Vector<String> vec = new Vector<String>();
            									vec.add(catName);
            									mapCatsWhoCaughtMe.put(mouseGame, vec);

            									// Kick off the task to remove this cat in 5 minutes
            									Thread catCleanupTask = new CatCleanupAsyncTask(new Integer(mouseGame), catName);
            									catCleanupTask.start();

            									// Need my player id
            									User user = ((CMApplication)getApplicationContext()).user;
            									if (user!=null && user.name!=null && user.name.length()>0) {
            										doPlayerCaught(user.userID(), catName, mouseGame, macAddress);
            									}
            									else {
            										LogUtil.error("User or username was null");
    												// remove the cat from the list so it tries again
        											Vector<String> vCats = mapCatsWhoCaughtMe.get(mouseGame);
        											if (vCats!=null) {
        												vCats.remove(catName);
        											}
            									}
            								}
            							}
            						}
            					}
            				}
            			}
            		}
            	}
            	else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            		LogUtil.info("Discovery started");
            	}
            	else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            		// If we're still a mouse in a game, run discovery again
            		List<Integer> listMouseGames = ((CMApplication)getApplicationContext()).dh.selectGameNumbers(false);
            		if (listMouseGames.size() > 0) {
            			scheduleDiscovery();            		}
            	}
            	else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
            		// Are we becoming discoverable, or stopping being discoverable?
            		int iNewMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
            		//int iOldMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, 0);

            		if (iNewMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            			// We just became discoverable. Setup the proper name for this device
            			String catName = getCatName();
            			if (catName!=null) {
            				mBluetoothAdapter.setName(catName);
            			}
            		}
            		else {
            			// Make sure the device name is set back to the original
            			if (!mBluetoothAdapter.getName().equals(origBTName))
            				mBluetoothAdapter.setName(origBTName);

            			// Kick off a new discoverable mode if I'm still a 
            			// cat in any games - nope; reverse-only games
            			// Am I still a cat?
            			List<Game> listGames = ((CMApplication)getApplicationContext()).dh.selectAll();
            			if (listGames.size()>0) {
            				// is it a reverse game?
            				for (int i=0; i < listGames.size(); i++) {
            					Game game = listGames.get(i);
            					if (game.getGameType()==CMConstants.GAME_TYPE_REVERSE) {
            						// Send cat notification
            						sendCatNotification(game.getGameNumber());
            					}
            				}
            			}
            		}
            	}
            	else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            		int iActionState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,0);
            		if (iActionState==BluetoothAdapter.STATE_TURNING_OFF || iActionState==BluetoothAdapter.STATE_OFF) {
            			// That's it - we're bailing out!
            			Intent logoutIntent = new Intent(CMIntentActions.ACTION_LOGOUT);
            			getApplicationContext().sendBroadcast(logoutIntent);
            			stopSelf();
            		}
            	}            	

            	// Cat and Mouse actions
            	//                else if (CMIntentActions.ACTION_JOIN_GAME.equals(action)) {
            	//                	// Find out the type of game we joined and take the appropriate action
            	//                	int gameNumber = intent.getIntExtra(CMConstants.PARM_GAME_NUMBER, 0);
            	//                	int gameType = intent.getIntExtra(CMConstants.PARM_GAME_TYPE, -1);
            	//                	if (gameNumber > 0 && gameType==CMConstants.GAME_TYPE_NORMAL) {
            	//                		// Just joined a normal game - start scanning
            	//                		doDiscovery();
            	//                	}
            	//                	else if (gameNumber > 0 && gameType==CMConstants.GAME_TYPE_REVERSE) {
            	//                		// I'm a cat. In this case, I believe I can prompt them to become discoverable
            	//                		Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            	//                		discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            	//                		startActivity(discoverIntent);
            	//                	}
            	//                }
            	else if (CMIntentActions.ACTION_QUIT_GAME.equals(action) ||
            			CMIntentActions.ACTION_GAME_OVER.equals(action)) {
            		// Am I still in any games?
            		List<Game> gamesList = ((CMApplication)getApplicationContext()).dh.selectAll();
            		if (gamesList.size() > 0) {
            			// Am I a cat in any games?
            			int iScanMode = mBluetoothAdapter.getScanMode();
            			int i;
            			for (i=0; i < gamesList.size(); i++) {
            				Game game = gamesList.get(i);
            				if (game.isCat()) {
            					// Reset my cat name
            					// I only care about this if I'm currently in discoverable mode
            					if (iScanMode==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            						// OK, I'm discoverable. Setup the cat name if appropriate
            						String catName = getCatName();
            						if (catName!=null) {
            							mBluetoothAdapter.setName(catName);
            							break;
            						}
            					}
            				}
            			}
            			if (i==gamesList.size()) {
            				// make sure its set back to orig
            				if (!mBluetoothAdapter.getName().equals(origBTName))
            					mBluetoothAdapter.setName(origBTName);
            			}
            		}
            		else {
            			// make sure its set back to orig
            			if (!mBluetoothAdapter.getName().equals(origBTName))
            				mBluetoothAdapter.setName(origBTName);
            		}
            	}
            	else if (CMIntentActions.ACTION_LOGOUT.equals(action)) {
            		stopSelf();
            	}
            	else if (CMIntentActions.ACTION_STATE_CAT.equals(action)) {
            		// Is it me?
            		Bundle bundle = intent.getExtras();
            		CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
            		if (n!=null) {
            			User user = ((CMApplication)getApplicationContext()).user;
            			if (user!=null && user.name!=null && user.name.length()>0) {
            				if (user.name.equals(n.getCatPlayerName())) {
            					// it is me.
            					// clear out the vector of cats who caught me
            					mapCatsWhoCaughtMe.remove(n.getGameNumber());

            					//Do I ask to start discoverable if its not on?
            					int iScanMode = mBluetoothAdapter.getScanMode();
            					if (iScanMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            						// Might as well send the notification from here
            						if (n.getGameType()==CMConstants.GAME_TYPE_NORMAL) {
            							// Are we configured to send a notification?
            							if (settings.getBoolean(CMConstants.SETTING_NORMAL_CAT_NOTIFICATION, true)) {
            								// I guess we are - so send it
            								sendCatNotification(n.getGameNumber());
            							}
            						}
            					}
            					else {
            						// we are discoverable. Make sure we got the right cat name
            						String catName = getCatName();
            						if (catName!=null)
            							mBluetoothAdapter.setName(catName);
            					}
            				}
            			}
            			else {
            				LogUtil.error("User or username is null!");
            			}
            		}
            		else {
            			LogUtil.error("no notification package in intent!");
            		}
            	}
            	else if (CMIntentActions.ACTION_STATE_MOUSE.equals(action)) {
            		// Is it me?
            		Bundle bundle = intent.getExtras();
            		CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
            		if (n!=null) {
            			User user = ((CMApplication)getApplicationContext()).user;
            			if (user!=null && user.name!=null && user.name.length()>0) {
            				if (user.name.equals(n.getMousePlayerName())) {
            					// it is me. If pro player, might need to adjust my cat name
            					if (((CMApplication)getApplicationContext()).isPro()) {
            						String catName = getCatName();
            						if (catName!=null)
            							mBluetoothAdapter.setName(catName);
            						else
            							mBluetoothAdapter.setName(origBTName);
            					}
            					else {
            						// Change my name back
            						mBluetoothAdapter.setName(origBTName);
            					}

            					// Do discovery
            					doDiscovery();

            					// Send notification if I just became a mouse in a reverse game
            					if (n.getGameType()==CMConstants.GAME_TYPE_REVERSE) {
            						// Should I send a notification?
            						if (settings.getBoolean(CMConstants.SETTING_REVERSE_MOUSE_NOTIFICATION, true)) {
            							// Yup
            							sendMouseNotification(n.getGameNumber());
            						}
            					}
            				} 
            				//            			else {
            				//            				// its not me, but it might be a cat who caught me; do clean up
            				//            				int gameNumber = n.getGameNumber();
            				//            				Vector<String> vecCats = mapCatsWhoCaughtMe.get(gameNumber);
            				//            				if (vecCats!=null) {
            				//            					if (vecCats.remove(n.getMousePlayerName())) {
            				//            						if (vecCats.size() > 0)
            				//            							mapCatsWhoCaughtMe.put(gameNumber, vecCats);
            				//            						else
            				//            							mapCatsWhoCaughtMe.remove(gameNumber);
            				//            					}
            				//            				}
            				//            			}// Shouldn't be doing this - let the task clean this up
            			}
            			else {
            				LogUtil.error("User or username is null!");
            			}
            		}
            		else {
            			LogUtil.error("no notification package in intent!");
            		}
            	}
            	else if (action.equals(CMIntentActions.ACTION_PLAYER_CAUGHT)) {
            		// Play a sound if configured
            		Bundle bundle = intent.getExtras();
            		CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
            		if (n!=null) {
            			User user = ((CMApplication)getApplicationContext()).user;
            			if (user!=null && user.name!=null && user.name.length()>0) {
            				// is it me?
            				if (user.name.equals(n.getMousePlayerName())) {
            					// Yup, I got caught. Send notification
            					if (settings.getBoolean(CMConstants.SETTING_GENERAL_CAUGHT_NOTIFICATION, true))
            						sendCaughtNotification(n);
            				}
            				else if (user.name.equals(n.getCatPlayerName())) {
            					// Yeah, I caught someone. Send notification
            					if (settings.getBoolean(CMConstants.SETTING_GENERAL_CATCH_MOUSE_NOTIFICATION, true))
            						sendCaughtMouseNotification(n);
            				}
            			}
            			else {
            				LogUtil.error("User or username is null!");
            			}
            		}            	
            		else {
            			LogUtil.error("no notification package in intent!");
            		}
            	}
            }
		}
    };	
    
    private void sendCatNotification(int gameNumber) {
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);    	
    	
    	int icon = R.drawable.ic_stat_notify_cat;
    	Context context = getApplicationContext();      // application Context
    	
    	Notification notification = new Notification(icon, null, System.currentTimeMillis());
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	Intent notificationIntent = new Intent(this, GameManagerActivity.class);
    	Game game = ((CMApplication)getApplicationContext()).dh.selectGame(gameNumber);
		Bundle bundle = new Bundle();
		bundle.putParcelable(CMConstants.GAME_PACKAGE, game);
    	
    	notificationIntent.putExtras(bundle);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	CharSequence contentTitle = getResources().getString(R.string.Cat_Alert);  // expanded message title
    	CharSequence contentText = getResources().getString(R.string.Became_Cat_Notification, game!=null ? game.getGameName() : "");      // expanded message text
    	
    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);    	
    	// Sound?
    	if (iCanHasSoundsVibrate() && settings.getBoolean(CMConstants.SETTING_NORMAL_CAT_SOUND, true))
    		notification.sound = Uri.parse("android.resource://"+CMConstants.PACKAGE_NO_PERIOD+"/raw/meow");
    	// vibrate?
    	if (iCanHasSoundsVibrate() && settings.getBoolean(CMConstants.SETTING_NORMAL_CAT_VIBRATE, true))
    		notification.defaults |= Notification.DEFAULT_VIBRATE;
    	
    	mNotificationManager.notify(CMConstants.NOTIFICATION_STATE_CAT, notification);
    }
    
    private void sendCaughtNotification(CMNotification cmNotification) {
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);    	
    	
    	int icon = R.drawable.ic_stat_notify_mouse_dead;
    	Context context = getApplicationContext();      // application Context
    	
    	Notification notification = new Notification(icon, null, System.currentTimeMillis());
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	Intent notificationIntent = new Intent(this, GameManagerActivity.class);
    	Game game = ((CMApplication)getApplicationContext()).dh.selectGame(cmNotification.getGameNumber());
		Bundle bundle = new Bundle();
		bundle.putParcelable(CMConstants.GAME_PACKAGE, game);
    	
    	notificationIntent.putExtras(bundle);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	CharSequence contentTitle = getResources().getString(R.string.You_Got_Caught);  // expanded message title
    	CharSequence contentText = getResources().getString(R.string.Got_Caught_Notification, cmNotification.getCatPlayerName(), game!=null ? game.getGameName() : "");      // expanded message text
    	
    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);    	
    	// Sound?
    	if (iCanHasSoundsVibrate() && settings.getBoolean(CMConstants.SETTING_GENERAL_CAUGHT_SOUND, true))
    		notification.sound = Uri.parse("android.resource://"+CMConstants.PACKAGE_NO_PERIOD+"/raw/catscreech");
    	// vibrate?
    	if (iCanHasSoundsVibrate() && settings.getBoolean(CMConstants.SETTING_GENERAL_CAUGHT_VIBRATE, true))
    		notification.defaults |= Notification.DEFAULT_VIBRATE;
    	
    	mNotificationManager.notify(CMConstants.NOTIFICATION_PLAYER_CAUGHT, notification);
    }
    
    private void sendMouseNotification(int gameNumber) {
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);    	
    	
    	int icon = R.drawable.ic_stat_notify_mouse;
    	Context context = getApplicationContext();      // application Context
    	
    	Notification notification = new Notification(icon, null, System.currentTimeMillis());
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	Intent notificationIntent = new Intent(this, GameManagerActivity.class);
    	Game game = ((CMApplication)getApplicationContext()).dh.selectGame(gameNumber);
		Bundle bundle = new Bundle();
		bundle.putParcelable(CMConstants.GAME_PACKAGE, game);
    	
    	notificationIntent.putExtras(bundle);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	CharSequence contentTitle = getResources().getString(R.string.Mouse_Alert);  // expanded message title
    	CharSequence contentText = getResources().getString(R.string.Became_Mouse_Notification, game!=null ? game.getGameName() : "");      // expanded message text
    	
    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);    	
    	// Sound?
    	if (iCanHasSoundsVibrate() && settings.getBoolean(CMConstants.SETTING_REVERSE_MOUSE_SOUND, true))
    		notification.sound = Uri.parse("android.resource://"+CMConstants.PACKAGE_NO_PERIOD+"/raw/mousesqueak");
    	// vibrate?
    	if (iCanHasSoundsVibrate() && settings.getBoolean(CMConstants.SETTING_REVERSE_MOUSE_VIBRATE, true))
    		notification.defaults |= Notification.DEFAULT_VIBRATE;
    	
    	mNotificationManager.notify(CMConstants.NOTIFICATION_STATE_MOUSE, notification);
    }
    
    private void sendCaughtMouseNotification(CMNotification cmNotification) {
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);    	
    	
    	int icon = R.drawable.ic_stat_notify_mouse_dead;
    	Context context = getApplicationContext();      // application Context
    	
    	Notification notification = new Notification(icon, null, System.currentTimeMillis());
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	Intent notificationIntent = new Intent(this, GameManagerActivity.class);
    	Game game = ((CMApplication)getApplicationContext()).dh.selectGame(cmNotification.getGameNumber());
		Bundle bundle = new Bundle();
		bundle.putParcelable(CMConstants.GAME_PACKAGE, game);
    	
    	notificationIntent.putExtras(bundle);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	CharSequence contentTitle = getResources().getString(R.string.Caught_Mouse);  // expanded message title
    	CharSequence contentText = getResources().getString(R.string.Caught_Mouse_Notification, cmNotification.getMousePlayerName(), game!=null ? game.getGameName() : "");      // expanded message text
    	
    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);    	
    	// vibrate?
    	if (iCanHasSoundsVibrate() && settings.getBoolean(CMConstants.SETTING_GENERAL_CATCH_MOUSE_VIRBRATE, true))
    		notification.defaults |= Notification.DEFAULT_VIBRATE;
    	
    	mNotificationManager.notify(CMConstants.NOTIFICATION_CAUGHT_MOUSE, notification);
    }
    
    private boolean iCanHasSoundsVibrate() {
    	if (settings.getBoolean(CMConstants.SETTING_GENERAL_SOUNDS_OFF, true)) {
        	// Check to see if we're within the times we don't want want sounds/vibration
        	GregorianCalendar gcCurrentTime = new GregorianCalendar();
        	int iCurrentHour = gcCurrentTime.get(Calendar.HOUR_OF_DAY);
        	int iCurrentMin = gcCurrentTime.get(Calendar.MINUTE);
        	
        	int iStartTimeHour = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_START_TIME_HOUR, 20);
        	int iStartTimeMin = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_START_TIME_MIN, 0);
        	int iEndTimeHour = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_END_TIME_HOUR, 8);
        	int iEndTimeMin = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_END_TIME_MIN, 0);
        	
        	if (iStartTimeHour > iEndTimeHour) {
        		// The expected result. We set gcStartTime to today, and gcEndTime to tomorrow
        		if ((iCurrentHour > iStartTimeHour || iCurrentHour < iEndTimeHour) || // greater than start time hour or less than end time hour
        				(iCurrentHour==iStartTimeHour && iCurrentMin >= iStartTimeMin) || // Within start time hour
        				(iCurrentHour==iEndTimeHour && iCurrentMin < iEndTimeMin)) // within end time hour
        			return false;
        	}
        	else if (iStartTimeHour < iEndTimeHour) {
        		if ((iCurrentHour > iStartTimeHour && iCurrentHour < iEndTimeHour) || // greater than start time hour AND less than end time hour
        				(iCurrentHour==iStartTimeHour && iCurrentMin >= iStartTimeMin) || // Within start time hour
        				(iCurrentHour==iEndTimeHour && iCurrentMin < iEndTimeMin)) // within end time hour
        			return false;
        		
        	}
        	else if (iStartTimeHour==iEndTimeHour){
        		// OK, the asshole has like an hour timespan or something - whatever
        		if (iCurrentHour==iStartTimeHour && 
        				(iCurrentMin > iStartTimeMin || iCurrentMin < iEndTimeMin))
        			return false;
        	}
    	}
    	
    	
    	
    	return true;
    }
    
	
	public void doPlayerCaught(String playerId, String catname, int gameNumber, String catMacAddress) {
		PlayerCaughtThread someThread = new PlayerCaughtThread(playerId, catname, gameNumber, catMacAddress);
		someThread.start();
	}
	
	private class PlayerCaughtThread extends Thread {
		String playerId;
		String catName;
		int gameNumber;
		String catMacAddress;
		
		public PlayerCaughtThread(String playerId, String catName, int gameNumber, String catMacAddress) {
			this.playerId = playerId;
			this.gameNumber = gameNumber;
			this.catMacAddress = catMacAddress;
			this.catName = catName;
		}
		
		public void run() {
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, playerId));
			vars.add(new BasicNameValuePair(CMConstants.PARM_GAME_NUMBER, Integer.toString(gameNumber)));
			vars.add(new BasicNameValuePair(CMConstants.PARM_MAC_ADDR, catMacAddress));
			String url = ServerUtil.CAUGHT_PLAYER_URL;//+"?"+URLEncodedUtils.format(vars, null);
			HttpPost request = new HttpPost(url);
			HttpClient client = ServerUtil.getHttpClient();
			try {
				request.setEntity(new UrlEncodedFormEntity(vars));
				HttpResponse resp = client.execute(request);
				int rc = resp.getStatusLine().getStatusCode();
				if (rc!=HttpStatus.SC_OK) {
					// whatever, log it
					LogUtil.error("Error returned from player caught. rc= "+rc);
				}
			} catch (Exception e) {
				LogUtil.error("Exception from PlayerCaughtThread: "+e.toString());
				// Need to remove this cat from our list so we process this again
				Vector<String> vecCats = mapCatsWhoCaughtMe.get(gameNumber);
				if (vecCats!=null) {
					vecCats.remove(catName);
					if (vecCats.size()==0)
						mapCatsWhoCaughtMe.remove(gameNumber);
				}
			}			
		}
	}    

}
