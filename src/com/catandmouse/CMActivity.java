package com.catandmouse;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.ads.AdRequest.ErrorCode;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.resource.Leaderboard;
import com.openfeint.api.resource.Score;
import com.openfeint.api.resource.User;
import com.openfeint.api.resource.CurrentUser;
import com.openfeint.api.ui.Dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public abstract class CMActivity extends Activity {
	protected SharedPreferences settings;
	private Game joinGame;
	private String joinGamePassword;
	protected ProgressDialog waitCursor;
    protected static final int REQUEST_DISCOVERABLE = 1;
	Handler handler;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //LogUtil.info("CMActivity OnCreate()");
        settings = getSharedPreferences(CMConstants.SETTINGS, MODE_PRIVATE);
        handler = new Handler();
        
        registerReceiver();		
        
        //waitCursor = new MyProgressDialog(this);
        //waitCursor.setCancelable(true);
	}
	
	public void toastMessage(String message, int duration) {
		Toast.makeText(this, message, duration).show();
	}
	
	protected void registerReceiver() {
        // Register for all CMActions
		for (int i=0; i < CMIntentActions.CM_ACTIONS.length; i++) {
			IntentFilter filter = new IntentFilter(CMIntentActions.CM_ACTIONS[i]);
			this.registerReceiver(mReceiver, filter);
		}    		
		
		// Probably a few bluetooth actions I want to listen for as well
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		this.registerReceiver(mReceiver, filter);
	}

	protected final BroadcastReceiver mReceiver  = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (CMIntentActions.ACTION_PLAYER_CAUGHT.equals(action)) {
				// Was it me?
				User user = ((CMApplication)getApplicationContext()).user;
				if (user!=null && user.name!=null && user.name.length()>0) {
					Bundle bundle = intent.getExtras();
					CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
					if (n!=null && user.name.equals(n.getCatPlayerName())) {
						// I caught someone!
						// hurray msg
						String msg = getResources().getString(R.string.you_caught)+" "+n.getMousePlayerName()+"!";
						toastMessage(msg, Toast.LENGTH_LONG);
					}
					else if (n!=null && user.name.equals(n.getMousePlayerName())) {
						// I got caught.
						// I haz a sad msg
						String msg = getResources().getString(R.string.got_caught)+" "+n.getCatPlayerName()+"!";
						toastMessage(msg, Toast.LENGTH_LONG);
					}
				}
				else {
					LogUtil.error("Bad user object");
					((CMApplication)getApplicationContext()).loadUser();
				}
			}
			else if (CMIntentActions.ACTION_STATE_CAT.equals(action)) {
				// Is it me?
				User user = ((CMApplication)getApplicationContext()).user;
				if (user!=null && user.name!=null && user.name.length()>0) {
					Bundle bundle = intent.getExtras();
					CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
					if (n!=null && n.getCatPlayerName().equals(user.name)) {
						// Prompt 'em if we're not already discoverable
						BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
						if (mBluetoothAdapter!=null) {
							int iScanMode = mBluetoothAdapter.getScanMode();
							if (iScanMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
								// Need game object for game name
								Game game = ((CMApplication)getApplicationContext()).dh.selectGame(n.getGameNumber());
								if (game!=null) {
									AlertDialog alert = CMActivity.this.getCatDiscoverableDialog(game.getGameName());
									alert.show();
								}
							}
						}
					}
				}
				else {
					LogUtil.error("Bad user object");
					((CMApplication)getApplicationContext()).loadUser();
				}
			}
			else if (CMIntentActions.ACTION_STATE_MOUSE.equals(action)) {
				// Is it me?
				User user = ((CMApplication)getApplicationContext()).user;
				if (user!=null && user.name!=null && user.name.length()>0) {
					Bundle bundle = intent.getExtras();
					CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
					if (n!=null && n.getMousePlayerName().equals(user.name)) {
						// If normal game, just give em some toast
						if (n.getGameType()==CMConstants.GAME_TYPE_NORMAL) {
							// Need game object for game name
							Game game = ((CMApplication)getApplicationContext()).dh.selectGame(n.getGameNumber());
							if (game!=null) {
								String msg = getResources().getString(R.string.switched_back_mouse)+" "+game.getGameName();
								toastMessage(msg, Toast.LENGTH_LONG);
							}
						}
						else {
							// Reverse game. More serious message!
							// Need game object for game name
							Game game = ((CMApplication)getApplicationContext()).dh.selectGame(n.getGameNumber());
							if (game!=null) {
								String msg = getResources().getString(R.string.became_mouse_message)+" "+game.getGameName()+". "+
								getResources().getString(R.string.run_hide);
								toastMessage(msg, Toast.LENGTH_LONG);
							}
						}
					}
				}
			}
			else if (CMIntentActions.ACTION_GAME_OVER.equals(action)) {
				// let em know
				Bundle bundle = intent.getExtras();
				CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
				Game game = ((CMApplication)getApplicationContext()).dh.selectGame(n.getGameNumber());
				if (game!=null) {
					String msg = String.format(getResources().getString(R.string.game_over), game.getGameName());
					toastMessage(msg, Toast.LENGTH_LONG);
				}
			}
			else if (CMIntentActions.ACTION_LOGOUT.equals(action)) {
				// let em know
				Bundle bundle = intent.getExtras();
				CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
				Game game = ((CMApplication)getApplicationContext()).dh.selectGame(n.getGameNumber());
				if (game!=null) {
					String msg = String.format(getResources().getString(R.string.logged_out), game.getGameName());
					toastMessage(msg, Toast.LENGTH_LONG);
				}				
			}
			else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
				// Are we changing from discoverable to undiscoverable?
            	int iNewMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
				if (iNewMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					// If we are part of a reverse-type game and the setting is not on to auto-prompt,
					// lets prompt them since they're in the UI
					//if (settings.getBoolean(CMConstants.SETTING_GENERAL_AUTO_DISCOVER, true)) {
						List<Game> gameList = ((CMApplication)getApplicationContext()).dh.selectAll();
						for (int i=0; i < gameList.size(); i++) {
							Game game = gameList.get(i);
							if (game.isCat()) {
								// OK, now we prompt them
								Intent newIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
								newIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
								startActivity(newIntent);
								break;
							}
						}
					//}
				}
			}
			else if (CMIntentActions.ACTION_JOIN_GAME.equals(action)) {
				// Should have the game we're joining as a bundle
				Bundle bundle = intent.getExtras();
				if (bundle!=null) {
					Game game = bundle.getParcelable(CMConstants.GAME_PACKAGE);
					if (game!=null) {
						if (game.isPrivate()) {
							Dialog passwordDialog = getPasswordDialog(game);
							passwordDialog.show();
						}
						else
							joinGame(game, null);
					}
				}
			}
		}
	};
	
	protected AlertDialog getCatDiscoverableDialog(String gameName) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String msg = getResources().getString(R.string.became_cat_message)+" "+gameName+".  "+getResources().getString(R.string.become_discoverable);
		builder.setMessage(msg)
			   .setTitle(getResources().getString(R.string.cat_alert))	
		       .setCancelable(false)
		       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		        	   discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);		        	   
						startActivityForResult(discoverIntent, REQUEST_DISCOVERABLE);
		           }
		       })
		       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		
		return alert;
	}

	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(mReceiver);
	}

	protected void onResume() {
		super.onResume();
		registerReceiver();
		//checkForDiscoverPrompt();
	}
	
//	protected void checkForDiscoverPrompt() {
//		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//		if (adapter!=null) {
//			int iScanMode = adapter.getScanMode();
//			if (iScanMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//				// If we are a cat and the setting is on to auto-prompt,
//				// lets prompt them since they're in the UI
//				if (settings.getBoolean(CMConstants.SETTING_GENERAL_AUTO_DISCOVER, true)) {
//					List<Game> gameList = ((CMApplication)getApplicationContext()).dh.selectAll();
//					for (int i=0; i < gameList.size(); i++) {
//						Game game = gameList.get(i);
//						if (game.isCat()) {
//							// OK, now we prompt them
//							Intent newIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//							newIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//							startActivityForResult(newIntent,REQUEST_DISCOVERABLE);
//							break;
//						}
//					}
//				}
//			}
//		}
//	}
	
	protected void joinGame(final Game game, final String password) {
		// First first, are we not a pro player and trying to join a second game?
		if (!((CMApplication)getApplicationContext()).isPro()) {
			List<Game> gameList = ((CMApplication)getApplicationContext()).dh.selectAll();
			if (gameList.size() > 0) {
				// Error
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				};
				DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id="+CMConstants.PRO_PACKAGE));
						startActivity(intent);					
					}
				};
				
				Dialog d = getGenericYesNoDialog(getResources().getString(R.string.One_Game_Allowed_Title), 
						getResources().getString(R.string.One_Game_Allowed_Msg), okListener, listener);
				d.show(); 
				return;
			}
		}
		
		// Second, do we have bluetooth and is it enabled
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter==null) {
			// No bluetooth, bummer. Let em know
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			};
			Dialog d = getGenericAlertDialog(getResources().getString(R.string.Bluetooth_Error_Title), 
					getResources().getString(R.string.Device_No_Bluetooth), listener);
			d.show(); 
			return;
		}
		else if (!mBluetoothAdapter.isEnabled()) {
        	// Warn them to enable before joining game
        	LogUtil.warn("Bluetooth adapter is not enabled, prompting user");
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		String msg = getResources().getString(R.string.Bluetooth_Not_On_Msg);
    		builder.setMessage(msg)
    			   .setTitle(getResources().getString(R.string.Bluetooth_Not_On_Title))	
    		       .setCancelable(false)
    		       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    		        	   joinGame = game; // Save these off
    		        	   joinGamePassword = password;
    		        	   startActivityForResult(discoverIntent, CMConstants.REQUEST_ENABLE_BT);
    		           }
    		       })
    		       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                dialog.cancel();
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    		return;
        }
        
		// Third, are we logged into Feint?
		if (OpenFeint.isUserLoggedIn()) {
			final User user = ((CMApplication)getApplicationContext()).user;
			if (user!=null && user.name!=null && user.name.length()>0) {
				// Need a mac address and a location
		        final String macAddress = mBluetoothAdapter.getAddress();
		        
		        // Need our last location as well
		        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		        LocationProvider locationProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER); // default

				// Is GPS enabled?
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);


					// Prompt to enable GPS
					//			    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
					//			    		String msg = getResources().getString(R.string.Enable_GPS);
					//			    		builder.setMessage(msg)
					//			    			   .setTitle(getResources().getString(R.string.Enable_GPS_Title))	
					//			    		       .setCancelable(false)
					//			    		       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					//			    		           public void onClick(DialogInterface dialog, int id) {
					//			    		        	   Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
					//			    		        	   startActivity(intent);
					//			    		           }
					//			    		       })
					//			    		       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					//			    		           public void onClick(DialogInterface dialog, int id) {
					//			    		                dialog.cancel();
					//			    		           }
					//			    		       });
					//			    		AlertDialog alert = builder.create();
					//			    		alert.show();
				}
				//					else // it's enabled, set it as our provider
				//						locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
				
				final Location location = locationManager.getLastKnownLocation(locationProvider.getName());
				final double latitude = location!=null ? location.getLatitude() : 0;
				final double longitude = location!=null ? location.getLongitude() : 0;
				
				// Show indeterminate wait from here
				waitCursor = ProgressDialog.show(this, null, getResources().getString(R.string.Joining_Game, game.getGameName()), true, true);
				// Have all our data, let's try logging in
				Thread loginThread = new Thread() {

					public void run() {
						String msg;
						int rc = ServerUtil.doLogin(user, macAddress, latitude, longitude);
						switch (rc) {
						case 0:
						case HttpStatus.SC_OK:
							// We're logged in successfully
							LogUtil.info("Successful login!");

							// Now join the game
							rc = ServerUtil.joinGame(user.userID(), game.getGameNumber(), password);
							switch(rc) {
							case 0:
							case HttpStatus.SC_OK:
								// All's good.  
								// Much to do...insert record into db, send intent, etc.  Success message?
								((CMApplication)getApplicationContext()).dh.insert(game);
								((CMApplication)getApplicationContext()).startServices(game.getGameNumber());

								waitCursor.dismiss();
								handler.post(new Runnable(){

									public void run() {
										//String msg = getResources().getString(R.string.Successfully_Joined_Game)+" "+game.getGameName();
										//toastMessage(msg, Toast.LENGTH_LONG);
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												Intent share = new Intent(Intent.ACTION_SEND);
												share.setType("text/plain");
												share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.Share_Join_Game, game.getGameName()));
												startActivity(Intent.createChooser(share, getResources().getString(R.string.join_game)));
											}
										};
										
										Dialog d = getGenericYesNoDialog(getResources().getString(R.string.Successfully_Joined_Game), 
												getResources().getString(R.string.Successfully_Joined_Game)+" "+game.getGameName()+". "+getResources().getString(R.string.Share_Prompt), okListener, listener);
										d.show(); 
									}
									
								});
								break;
								// Handle all join game errors
							case CMConstants.ERR_BAD_PARMS:
							case CMConstants.ERR_MISSING_PARMS:
								waitCursor.dismiss();
								LogUtil.error("Missing or bad parms for join game");
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Login_Error), 
												getResources().getString(R.string.Login_Error), listener);
										d.show(); 
									}
									
								});
								break;
							case CMConstants.ERR_JOIN_GAME_PLAYER_NOT_LOGGED_IN:
								// WTF?
								waitCursor.dismiss();
								LogUtil.error("Joing game error, player not logged in");
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Login_Error), 
												getResources().getString(R.string.Login_Error), listener);
										d.show(); 
									}
									
								});
								break;
							case CMConstants.ERR_JOIN_GAME_BAD_PASSWORD:
							case CMConstants.ERR_JOIN_GAME_NO_PASSWORD:
								waitCursor.dismiss();
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Error_Joining_Game), 
												getResources().getString(R.string.Invalid_Password), listener);
										d.show(); 
									}
									
								});
								break;
							case CMConstants.ERR_JOIN_GAME_TOO_EARLY:
								waitCursor.dismiss();
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Error_Joining_Game), 
												getResources().getString(R.string.Join_Game_Too_Early), listener);
										d.show(); 
									}
									
								});
								break;
							case CMConstants.ERR_JOIN_GAME_TOO_LATE:
								waitCursor.dismiss();
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Error_Joining_Game), 
												getResources().getString(R.string.Join_Game_Too_Late), listener);
										d.show(); 
									}
									
								});
								break;
							case CMConstants.ERR_NO_LOCATION_DATA:
								LogUtil.warn("Tried joining game outside perimeter");
								waitCursor.dismiss();
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Error_Joining_Game), 
												getResources().getString(R.string.Invalid_Location_Data), listener);
										d.show(); 
									}
									
								});
								break;
							case CMConstants.ERR_JOIN_GAME_OUTSIDE_PERIMETER:
								LogUtil.warn("Tried joining game outside perimeter");
								waitCursor.dismiss();
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Error_Joining_Game), 
												getResources().getString(R.string.Location_Outside_Perimeter), listener);
										d.show(); 
									}
									
								});
								break;
							case CMConstants.ERR_INVALID_GAME_NUMBER:
							default:
								waitCursor.dismiss();
								handler.post(new Runnable(){

									public void run() {
										DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										};
										Dialog d = getGenericAlertDialog(getResources().getString(R.string.Unknown_Error), 
												getResources().getString(R.string.Unknown_Error), listener);
										d.show(); 
									}
									
								});
								break;
							}
							break;
							// Handle all login errors
						case CMConstants.ERR_BAD_PARMS:
						case CMConstants.ERR_MISSING_PARMS:
							waitCursor.dismiss();
							LogUtil.error("Missing or invalid parms for login");
							handler.post(new Runnable(){

								public void run() {
									DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									};
									Dialog d = getGenericAlertDialog(getResources().getString(R.string.Unknown_Error), 
											getResources().getString(R.string.Unknown_Error), listener);
									d.show(); 
								}
								
							});
							break;
						case CMConstants.ERR_LOGIN_EXISTING_DIFF_MAC_ADDR:
							// dude's already logged in on another device
							waitCursor.dismiss();
							handler.post(new Runnable(){

								public void run() {
									String title = getResources().getString(R.string.Already_Logged_In_Title);
									String msg = getResources().getString(R.string.Login_Already_Diff_Device);
									DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									};
									Dialog dialog = getGenericAlertDialog(title, msg, listener);
									dialog.show();
								}
								
							});
							break;
						default:
							// WTF?
							waitCursor.dismiss();
							LogUtil.error("Error during login: "+rc);
							handler.post(new Runnable(){

								public void run() {
									DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									};
									Dialog d = getGenericAlertDialog(getResources().getString(R.string.Unknown_Error), 
											getResources().getString(R.string.Unknown_Error), listener);
									d.show(); 
								}
								
							});
							break;
						}
						waitCursor.dismiss();
					}
					
				};
				loginThread.start();

			}
			else {
				// User or username is null
				LogUtil.error("Bad user object");
				((CMApplication)getApplicationContext()).loadUser();
				toastMessage(getResources().getString(R.string.Feint_Not_Logged_In),Toast.LENGTH_SHORT);
			}
		}
		else {
			// Warn them that Feint login is required and prompt to launch Feint
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(getResources().getString(R.string.Feint_Not_Logged_In));
        	builder.setMessage(R.string.Feint_Login_Warning);
        	// OK Button
        	builder.setPositiveButton(android.R.string.yes,
        		    new DialogInterface.OnClickListener() {
        		        public void onClick(DialogInterface dialog, int which) {
        		        	Dashboard.open();
        		        }
        		    });       
        	// Cancel button
        	builder.setNegativeButton(android.R.string.no,
        		    new DialogInterface.OnClickListener() {
        		        public void onClick(DialogInterface dialog, int whichButton) {
        		        	dialog.cancel();
        		        }
        		    });
        	AlertDialog dialog = builder.create();
        	dialog.show();
		}
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case CMConstants.REQUEST_ENABLE_BT:
			// Tried to join a game without bluetooth enabled, and now we're back. Try joining game again
			if (resultCode==Activity.RESULT_OK) {
				joinGame(joinGame, joinGamePassword);
			}
		}
	}
	
	protected Dialog getPasswordDialog(final Game game) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	LayoutInflater inflater =
    	    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	final View layout =
    	    inflater.inflate(R.layout.password,null);
    	builder.setView(layout);
    	builder.setTitle(R.string.Password_Required);
    	// OK Button
    	builder.setPositiveButton(android.R.string.ok,
    		    new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) {
    		            TextView tvPassword =
    		                (TextView) findViewById(R.id.editText_Password);
    		            String strPassword = tvPassword.getText().toString();
    		            joinGame(game, strPassword);
    		        }
    		    });       
    	// Cancel button
    	builder.setNegativeButton(android.R.string.cancel,
    		    new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int whichButton) {
    		        	dialog.cancel();
    		        }
    		    });
    	AlertDialog passwordDialog = builder.create();
    	return passwordDialog;
	}
	
	protected AlertDialog getGenericAlertDialog(String title, String msg, DialogInterface.OnClickListener okListener) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(title);
    	builder.setMessage(msg);
    	// OK Button
    	builder.setPositiveButton(android.R.string.ok,okListener);
    	AlertDialog dialog = builder.create();
    	return dialog;    	
	}

	protected Dialog getGenericYesNoDialog(String title, String msg, DialogInterface.OnClickListener yesListener,
			DialogInterface.OnClickListener noListener) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(title);
    	builder.setMessage(msg);
    	// OK Button
    	builder.setPositiveButton(android.R.string.yes, yesListener);
    	builder.setNegativeButton(android.R.string.no, noListener);
    	AlertDialog dialog = builder.create();
    	return dialog;    	
	}
	
	protected Dialog getGenericYesNoDialog(String title, View view, DialogInterface.OnClickListener yesListener,
			DialogInterface.OnClickListener noListener) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(title);
    	builder.setView(view);
    	// OK Button
    	builder.setPositiveButton(android.R.string.yes, yesListener);
    	builder.setNegativeButton(android.R.string.no, noListener);
    	AlertDialog dialog = builder.create();
    	return dialog;    	
	}
	
	protected AlertDialog getGenericOKCancelDialog(String title, View view, DialogInterface.OnClickListener okListener,
			DialogInterface.OnClickListener cancelListener) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(title);
    	builder.setView(view);
    	// OK Button
    	builder.setPositiveButton(android.R.string.ok, okListener);
    	builder.setNegativeButton(android.R.string.cancel, cancelListener);
    	AlertDialog dialog = builder.create();
    	return dialog;    	
	}
	
	public static AlertDialog getFeintUserDialog(final Activity activity, final User someUser, final String gameName) {
		// Create the feint user dialog
    	LayoutInflater inflater =
    	    (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	final View layout =
    	    inflater.inflate(R.layout.feint_user,null);
    	
    	// build the dialog
    	final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	builder.setView(layout);
    	
    	// name
    	TextView tvName = (TextView) layout.findViewById(R.id.textView_FeintDialog_Player);
    	tvName.setText(someUser.name);
    	
    	final ImageView image = (ImageView) layout.findViewById(R.id.imageView_FeintDialog_User);
    	// set this image to the user once we get it
    	someUser.downloadProfilePicture(new User.DownloadProfilePictureCB() {
			
			@Override
			public void onSuccess(Bitmap iconBitmap) {
				image.setImageBitmap(iconBitmap);
			}
		});
    	
    	final TextView tvScore = (TextView) layout.findViewById(R.id.textView_FeintDialog_Score);
    	// Get the leaderboard for the game we're concerned about, and then we can get 
    	// someUser's score
    	Leaderboard.list(new Leaderboard.ListCB() {
			
			@Override
			public void onSuccess(List<Leaderboard> leaderboards) {
				for (Leaderboard leaderboard : leaderboards) {
					if (leaderboard.name.equals(gameName)) {
						// found it
						leaderboard.getUserScore(someUser, new Leaderboard.GetUserScoreCB(){

							@Override
							public void onSuccess(Score score) {
								// got it
								tvScore.setText(score!=null ? Long.toString(score.score) : "0");
							}
						});
					}
				}
			}
		});
    	
    	builder.setPositiveButton(activity.getResources().getString(R.string.SendFriendRequest),
    		    new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) {
    		        	// Send a friend request to the user
    		        	((CurrentUser)((CMApplication)activity.getApplicationContext()).user).befriend(someUser, null);
    		        }
    		    });       
    	
    	// Cancel button
    	builder.setNegativeButton(android.R.string.cancel,
    		    new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int whichButton) {
    		        	dialog.cancel();
    		        }
    		    });
    	
    	AlertDialog dialog = builder.create();
    	return dialog;    	
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.gameoptions, menu);
        
        menu.findItem(R.id.SettingsScreen).setIntent(
            new Intent(this, SettingsActivity.class));
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(ServerUtil.HELP_URL));
        menu.findItem(R.id.HelpScreen).setIntent(i);
        return true;
    }    

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        startActivity(item.getIntent());
        return true;
    }        
    
    protected void displayAd(int controlId) {
		if (!((CMApplication)getApplicationContext()).isPro()) {
			// put an ad on lvActivity
		    final AdView adView = new AdView(this, AdSize.BANNER, CMConstants.ADMOB_PUBLISHER_ID);
		    
		    // where we put ad depends on orientation
		    final FrameLayout adLayout;
	    	adLayout = (FrameLayout) findViewById(controlId);
//
		    //FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(adLayout.getLayoutParams());
		    //adView.setVisibility(View.VISIBLE);
		    //adLayout.addView(adView);//, adsParams);
		    //adLayout.bringChildToFront(adView);
		    adView.setAdListener(new AdListener(){

				@Override
				public void onDismissScreen(Ad arg0) {
					LogUtil.info("AdListener.onDismissScreen");
				}

				@Override
				public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
					LogUtil.info("AdListener.onFailedToReceiveAd");
				}

				@Override
				public void onLeaveApplication(Ad arg0) {
					LogUtil.info("AdListener.onLeaveApplication");
				}

				@Override
				public void onPresentScreen(Ad arg0) {
					LogUtil.info("AdListener.onPresentScreen");
				}

				@Override
				public void onReceiveAd(Ad arg0) {
					LogUtil.info("AdListener.onReceiveAd");
				    FrameLayout.LayoutParams adsParams =new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, android.view.Gravity.BOTTOM|android.view.Gravity.RIGHT);		    
					adLayout.addView(adView,adsParams);
					// in 15 seconds, hide the ad
					Timer timer = new Timer();
					timer.schedule(new TimerTask(){

						@Override
						public void run() {
							handler.post(new Runnable(){

								@Override
								public void run() {
									adLayout.removeView(adView);
								}
								
							});
						}
						
					}, 15000);
				}
		    	
		    });
		    
		    AdRequest request = new AdRequest();
		    
		    if (CMConstants.isTestMode) {
			    // get device id
			    TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE); 
			    request.addTestDevice(tm.getDeviceId()); 
			    request.setTesting(true); 		    	
		    }
		    
		    // Try and get our location
		    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		    Location location = null;
			List<String> providers = locationManager.getProviders(true);
			try {
				for (int i=0; i < providers.size(); i++) {
					location = locationManager.getLastKnownLocation(providers.get(i));
				}				
			}
			catch (Exception e) {
				LogUtil.error("Error getting location in GameManagerActivity: "+e.toString());
			}		    
			
			if (location!=null)
				request.setLocation(location);
			else
				LogUtil.warn("Couldn't get location in GameManagerActivity");

		    adView.loadAd(request);            
		}
    }
}
