package com.catandmouse;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.android.vending.licensing.LicenseCheckerCallback;
import com.catandmouse.service.BluetoothService;
import com.catandmouse.service.GameService;
import com.catandmouse.service.LocationService;
import com.catandmouse.service.NotificationService;
import com.catandmouse.service.ScoreService;
import com.google.myjson.Gson;
import com.google.myjson.reflect.TypeToken;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.OpenFeintDelegate;
import com.openfeint.api.OpenFeintSettings;
import com.openfeint.api.resource.CurrentUser;
import com.openfeint.api.resource.User;
import com.paypal.android.MEP.PayPal;

import android.app.Application;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class CMApplication extends Application {
	public DataHelper dh;
	public NotificationDataHelper ndh;
	public User user = null;
	protected static final int INITIALIZE_SUCCESS = 0;
	protected static final int INITIALIZE_FAILURE = 1;
	public boolean bPayPalInitialized = false;
	public boolean bFeintInitialized = false;
	public boolean bAppInitialized = false;
//	private Handler handler;
	private boolean isPro = false;
	public boolean checkedGamesList = false;

//	private BluetoothService btService;
//	private GameService gameService;
//	private LocationService locationService;
//	private NotificationService notificationService;
//	private ScoreService scoreService;
		
	Handler hRefresh = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
		    	case INITIALIZE_SUCCESS:
		    		bPayPalInitialized=true;
		            break;
		    	case INITIALIZE_FAILURE:
		    		// try again in 10 seconds
		    		Timer timer = new Timer();
		    		timer.schedule(new TimerTask(){

						@Override
						public void run() {
				    		initPayPal();
						}
		    			
		    		}, 10000);
		    		break;
			}
		}
	};
	
	public void onCreate() {
		super.onCreate();
		
		// Delay Feint and paypal setup for 4 seconds for splash screen
//        handler = new Handler();
//		Timer setupTimer = new Timer();
//		setupTimer.schedule(new TimerTask(){
//
//			@Override
//			public void run() {
//				handler.post(new Runnable(){
//
//					@Override
//					public void run() {
//						initFeint();
//				        // PayPal
//				        initPayPal();						
//					}
//				});
//			}
//			
//		}, 4000);
		
		// init
		initFeint();
        // PayPal
        initPayPal();						
		
		// check if we're pro
		checkPro();
		
		// Database Setup
		this.dh = new DataHelper(this);
		//this.dh.deleteAll(); // In case of bad shutdown, we're exited out of all games
		
		this.ndh = new NotificationDataHelper(this);
		//this.ndh.deleteAll(); // In case of bad shutdown, we're exited out of all games		
	}
	
	private void init() {
		if (!bAppInitialized) {
			bAppInitialized = true;
			// Start the score service - it should always be running
			Intent scoreIntent = new Intent(this, ScoreService.class);
			this.startService(scoreIntent);

			// If we *think* we're in any games, we should start the services now
			List<Game> gamesList = dh.selectAll();
			if (gamesList.size()>0) {
				for (Game game : gamesList) {
					startServices(game.getGameNumber());
				}
			}		
		}
	}
	
	public void initFeint() {
		// OpenFeint setup
        Map<String, Object> options = new HashMap<String, Object>();
        options.put(OpenFeintSettings.SettingCloudStorageCompressionStrategy, OpenFeintSettings.CloudStorageCompressionStrategyDefault);
        // use the below line to set orientation
        // options.put(OpenFeintSettings.RequestedOrientation, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        OpenFeintSettings settings = new OpenFeintSettings("Cat and Mouse", "HOmA1tBK461EQdJs0eNtw", "umYou84aXbsLqqptzp6wrnAShSzDBk0LsbjO9ZveNoc", "278082", options);
        
        OpenFeint.initialize(this, settings, new OpenFeintDelegate() {

        	public void userLoggedIn(CurrentUser user) {
        		bFeintInitialized = true;
        		init();
        		loadUser();
        	}

        	public void userLoggedOut(User user) {
        		// Send logout intent to all receivers
        		Intent intent = new Intent(CMIntentActions.ACTION_LOGOUT);
        		sendBroadcast(intent);
        	} 
        });
        		
	}
	
	public void initPayPal() {
		// Initialize the library. We'll do it in a separate thread because it requires communication with the server
		// which may take some time depending on the connection strength/speed.
		Thread libraryInitializationThread = new Thread() {
			public void run() {
				initLibrary();
				
				// The library is initialized so let's create our CheckoutButton and update the UI.
				if (PayPal.getInstance().isLibraryInitialized()) {
					LogUtil.info("PayPal initialized");
					hRefresh.sendEmptyMessage(INITIALIZE_SUCCESS);
				}
				else {
					LogUtil.error("PayPal failed to initialize");
					hRefresh.sendEmptyMessage(INITIALIZE_FAILURE);
				}
			}
		};
		libraryInitializationThread.start();
	}
	
	private void initLibrary() {
		// Paypal setup
		boolean bInitialize = true;
		PayPal pp = PayPal.getInstance();
		if (pp!=null){
			if (!pp.isLibraryInitialized())
				pp.deinitialize();
			else
				bInitialize = false;
		}
			
		// If the library is already initialized, then we don't need to initialize it again.
		if(bInitialize) {
			try {
				pp = PayPal.initWithAppID(this, CMConstants.appID, CMConstants.PAYPAL_ENV);
				pp.setLanguage("en_US"); // Sets the language for the library.
			}
			catch (Exception e) {
				LogUtil.error("PayPal failed to initialize; error="+e.toString());
			}
		}
	}	
	
	public void loadUser() {
		if (user==null) {
			if (!bFeintInitialized)
				initFeint();
			else {
				user = OpenFeint.getCurrentUser();
		        if (user != null) {
		        	user.load(new User.LoadCB() {
						
						@Override
						public void onSuccess() {
							if (!checkedGamesList) {
								// check our list of games we think we're in
								gameLoginCheck();
							}
						}
					});
		        }				
			}
		}
	}
	
	public boolean isPro() {
		return isPro;
	}
	
	public void setPro(boolean isPro) {
		this.isPro = isPro;
	}
	
	public boolean checkProPackage() {
		// Check for the existence of the pro unlock product, and compare it's key signature against
		// this one. 
		try {
			PackageManager manager = getPackageManager(); 
			PackageInfo appProInfo = manager.getPackageInfo(
					CMConstants.PRO_PACKAGE, PackageManager.GET_SIGNATURES);
			PackageInfo appInfo = manager.getPackageInfo(
					CMConstants.PACKAGE_NO_PERIOD, PackageManager.GET_SIGNATURES);
			
			// Now test if the first signature equals this one			
			if (appProInfo.signatures[0].toCharsString().equals(appInfo.signatures[0].toCharsString())) {
				LogUtil.info("Certificate check successful for checkPro()");
				return true;
			}			
		} catch (NameNotFoundException e) {
			// Expected exception that occurs if the package is not present.
			return false;
		}	
		
		return false;
	}
	
	public void checkPro() {
		// Do the package match and the license check. Activate pro if they both pass
		if (checkProPackage()) {
			LogUtil.info("Certificate check successful for checkPro()");
			// We're almost pro - time to check the license
			CMLicenseCheck.doLicenseCheck(this, new LicenseCheckerCallback(){

				@Override
				public void allow() {
					// we're good!
					isPro = true;
					sendProNotice();
				}

				@Override
				public void dontAllow() {
					// failed
					sendProNotice();
				}

				@Override
				public void applicationError(
						ApplicationErrorCode errorCode) {
					// VERY BAD!
					LogUtil.error("Failed license call: "+errorCode.toString());
					sendProNotice();
				}

			});
		}
		else {
			LogUtil.info("Certificate check failed for checkPro()");
			sendProNotice();
		}
			
	}
	
	private void sendProNotice() {
		// Send a notification so activities can update themselves
		Intent intent = new Intent(CMIntentActions.ACTION_PRO_CHECK);
		intent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_IS_PRO, isPro);
		sendBroadcast(intent);				
	}
	
	public void startServices(int gameNumber) {
		// Should only be called when joining a game
		Intent btIntent = new Intent(this, BluetoothService.class);
		btIntent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, gameNumber);
		this.startService(btIntent);
		
		Intent gameIntent = new Intent(this, GameService.class);
		gameIntent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, gameNumber);
		this.startService(gameIntent);
		
		Intent locIntent = new Intent(this, LocationService.class);
		locIntent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, gameNumber);
		this.startService(locIntent);
		
		Intent notIntent = new Intent(this, NotificationService.class);
		notIntent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, gameNumber);
		this.startService(notIntent);
	}

    public void updateScoreFromFeint(int gameNumber, long score) {
		Intent intent = new Intent(CMIntentActions.ACTION_SCORE_UPDATE);
		intent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, gameNumber);
		intent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_SCORE, score);
		intent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_FEINT_SCORE, true);
		sendBroadcast(intent);
    }	
    
    public void updateScore(int gameNumber, long score) {
    	// First, get this game
    	Game game = dh.selectGame(gameNumber);
    	if (game!=null) {
    		// Found it - get our new current score
    		long currentScore = game.getCachedScore()+score;
			ContentValues args = new ContentValues();
			args.put(DataHelper.COLUMN_CACHED_SCORE, currentScore);
			dh.update(gameNumber, args);
			
			// Send an intent for the game manager screen
			Intent intent = new Intent(CMIntentActions.ACTION_SCORE_UPDATE);
			intent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, gameNumber);
			intent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_SCORE, currentScore);
			sendBroadcast(intent);
    	}
    	else {
    		LogUtil.error("Error retreiving game for game number: "+gameNumber);
    	}
    }

    public void resetScore(int gameNumber) {
    	// First, get this game
    	Game game = dh.selectGame(gameNumber);
    	if (game!=null) {
    		// Found it - reset the score
    		long currentScore = 0;
			ContentValues args = new ContentValues();
			args.put(DataHelper.COLUMN_CACHED_SCORE, currentScore);
			dh.update(gameNumber, args);
    	}
    	else {
    		LogUtil.error("Error retreiving game for game number: "+gameNumber);
    	}
    }
    
    public void gameLoginCheck() {
    	// Check to see if we are actually logged into the games we think we are
    	// Update our database if we're not in-sync
    	final List<Game> currentGameList = dh.selectAll();
    	if (currentGameList.size() > 0) {
        	Thread gameCheckThread = new Thread() {

    			public void run() {
    				// we should have a valid user id at this point
    				String playerId = user.userID();
    				if (playerId!=null && playerId.length()>0) {
    					Vector<NameValuePair> vars = new Vector<NameValuePair>();
    					vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, playerId));
    					String url = ServerUtil.CHECK_JOINED_GAMES_URL+"?"+URLEncodedUtils.format(vars, null);
    					
    					HttpGet request = new HttpGet(url);
    					HttpClient client = ServerUtil.getHttpClient();
    					try {
    						List<GamePlayerBean> gameList = new ArrayList<GamePlayerBean>();
    						HttpResponse resp = client.execute(request);
    						int rc = resp.getStatusLine().getStatusCode();
    						switch (rc) {
    						case 0:
    						case HttpStatus.SC_OK:
    							// Success - should have a List<GameBean> in our json response
    							Reader reader = new InputStreamReader(resp.getEntity().getContent());
    							Gson gson = new Gson();
    							Type listType = new TypeToken<List<GamePlayerBean>>(){}.getType();
    							gameList = gson.fromJson(reader, listType);
    							break;
    						default:
    							LogUtil.error("Bad rc from check joined games: "+rc);
    							break;
    						}
    						
    						// Check our current games against what we're in and 
    						// remove any discrepancies
    						for (Game game : currentGameList) {
    							boolean bFound = false;
    							for (GamePlayerBean bean : gameList) {
    								if (bean.getGameNumber()==game.getGameNumber()) {
    									bFound = true;
    									// make sure we're in sync
    									if (bean.isCat()!=game.isCat()) {
    										// out of sync
    										LogUtil.warn("Out of sync, correcting");
    										ContentValues cv = new ContentValues();
    										cv.put(DataHelper.COLUMN_IS_CAT, bean.isCat());
    										dh.update(game.getGameNumber(), cv);			
    										// send out a notification
    										CMNotification n = new CMNotification(game.getGameNumber(), game.getGameType(), 
    												bean.getPlayerId(), bean.isCat() ? user.userID() : "", bean.isCat() ? user.name : "", 
    												!bean.isCat() ? user.userID() : "", !bean.isCat() ? user.name : "",  
    												bean.isCat() ? CMConstants.NOTIFICATION_STATE_CAT : CMConstants.NOTIFICATION_STATE_MOUSE, System.currentTimeMillis());
    										Bundle bundle = new Bundle();
    										bundle.putParcelable(CMConstants.NOTIFICATION_PACKAGE, n);
    										String action = CMIntentActions.mapActionsToNotifications.get(n.getNotificationType());
    										Intent intent = new Intent(action);
    										intent.putExtras(bundle);
    										sendBroadcast(intent);
    									}
    									break;
    								}
    							}
    							
    							if (!bFound) {
    								// delete it :(
    								LogUtil.warn("Not in a game we think we are: deleting");
    								dh.delete(game.getGameNumber());
    								// send out a quit game notification to alert services/activites
    								Intent quitIntent = new Intent(CMIntentActions.ACTION_QUIT_GAME);
    								quitIntent.putExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, game.getGameNumber());
    								sendBroadcast(quitIntent);
    							}
    						}
    					}
    					catch (Exception e) {
    						LogUtil.error("Error checking joined games: "+e.toString());
    					}
    				}
    				// however we made it here, we're flagging that we're done
					checkedGamesList = true;
					// send notification
					String action = CMIntentActions.ACTION_CHECKED_GAMES;
					Intent intent = new Intent(action);
					sendBroadcast(intent);
    			}
        	};
        	gameCheckThread.start();
    	}
    	else {
			checkedGamesList = true;
    		// send notification as well
			String action = CMIntentActions.ACTION_CHECKED_GAMES;
			Intent intent = new Intent(action);
			sendBroadcast(intent);
    	}
    }
}
