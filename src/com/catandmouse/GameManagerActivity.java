package com.catandmouse;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.openfeint.api.resource.Leaderboard;
import com.openfeint.api.resource.Score;
import com.openfeint.api.resource.User;
import com.openfeint.api.ui.Dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameManagerActivity extends CMActivity {
	List<Game> gameList;
	Game currentGame = null;
	Spinner spinGameNames;
	ArrayAdapter<String> spinAdapter;
	private BluetoothAdapter mBluetoothAdapter = null;
	Button buttonGetMice;
    private ArrayAdapter<String> mActivityArrayAdapter;
    private ArrayList<Pair<String,String>> mActivityArrayIds;
    ImageView imgState;
    TextView tvScore;
    TextView tvGameType;
    TextView tvRatio;
    TextView tvLocation; 
    Button buttonPlayerMap;
    ListView lvActivity;
    private static final String SPACES="    ";
    private boolean bLoaded = false;
    private long feintScore = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.info("GameManager onCreate()");
		
		// Handle orientation
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int orientation = display.getOrientation();
		if (orientation==Configuration.ORIENTATION_PORTRAIT)
			setContentView(R.layout.game_manager_land);
		else
			setContentView(R.layout.game_manager); // Seems backwards but it works
		
		spinGameNames = (Spinner) findViewById(R.id.spinner_GameName);
		spinGameNames.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View itemSelected,
					int selectedItemPosition, long selectedId) {
				LogUtil.info("spinGames onItemSelected() called, calling onLoad");
				Game game = gameList.get(selectedItemPosition);
				onLoad(game);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
		bLoaded = true; // setting up the spinner causes onLoad to be called
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter==null) {
			// Catastrophic!
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			};
			getGenericAlertDialog(getResources().getString(R.string.Bluetooth_Error_Title), 
					getResources().getString(R.string.Device_No_Bluetooth), listener);
		}
		
		setupSpinner();
		
		tvScore = (TextView) findViewById(R.id.textView_Score);
		imgState = (ImageView) findViewById(R.id.imageView_GameManagerState);
		buttonGetMice = (Button) findViewById(R.id.button_GetMice);
		tvGameType = (TextView)findViewById(R.id.textView_GameManagerType);
		tvRatio = (TextView)findViewById(R.id.textView_Ratio);
		tvLocation = (TextView)findViewById(R.id.textView_GameManagerLocation);
		buttonPlayerMap = (Button) findViewById(R.id.button_PlayerMap);
		// Activity window
		//TextView tvListViewText = 
		mActivityArrayIds = new ArrayList<Pair<String,String>>();
		mActivityArrayAdapter = new ArrayAdapter(this, R.layout.textview_for_listview);
		lvActivity = (ListView)findViewById(R.id.listView_GameManagerActivity);
		lvActivity.setAdapter(mActivityArrayAdapter);
		lvActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				LogUtil.info("Activity window clicked, position="+position);
				String msg = mActivityArrayAdapter.getItem(position);
				final Pair<String,String> pIds = mActivityArrayIds.get(position);
				LogUtil.info("Processing activity msg="+msg);
				if (msg.contains(getResources().getString(R.string.became_cat)) ||
						msg.contains(getResources().getString(R.string.became_mouse))) {
					// only one user, easy...
					if (msg.indexOf(getResources().getString(R.string.became_cat))!=-1) {
						if (!((CMApplication)getApplicationContext()).user.userID().equals(pIds.first))
							sendFriendRequest(pIds.first);
					}
					else if (msg.indexOf(getResources().getString(R.string.became_mouse))!=-1) {
						if (!((CMApplication)getApplicationContext()).user.userID().equals(pIds.second))
							sendFriendRequest(pIds.second);
					}
				}
				else if (msg.contains(" "+getResources().getString(R.string.caught)+" ")) {
					// OK, 2 users here, make them pick
					String user1 = msg.substring(msg.indexOf(SPACES)+SPACES.length(), msg.indexOf(getResources().getString(R.string.caught))-1);
					String user2 = msg.substring(msg.indexOf(" "+getResources().getString(R.string.caught)+" ")+getResources().getString(R.string.caught).length()+2);
					LogUtil.info("user1="+user1+" user2="+user2);
					
					// are one of these myself?
					if (user1.equals(((CMApplication)getApplicationContext()).user.name)) {
						sendFriendRequest(pIds.second);
					}
					else if (user2.equals(((CMApplication)getApplicationContext()).user.name)) {
						sendFriendRequest(pIds.first);
					}
					else {
						// Neither are me - make the user choose
						LayoutInflater inflater =
				    	    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				    	final View layout =
				    	    inflater.inflate(R.layout.user_select,null);
				    	final RadioButton rbUser1 = (RadioButton)layout.findViewById(R.id.radio1_UserSelect);
				    	rbUser1.setText(user1);
				    	final RadioButton rbUser2 = (RadioButton)layout.findViewById(R.id.radio2_UserSelect);
				    	rbUser2.setText(user2);
						DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// which radio button is selected?
								if (rbUser1.isChecked())
									sendFriendRequest(pIds.first);
								else if (rbUser2.isChecked())
									sendFriendRequest(pIds.second);
							}
							
						};
						
						DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
							
						};
						
						AlertDialog dlg = getGenericOKCancelDialog(getResources().getString(R.string.Select_Player), layout,
				    			okListener, cancelListener);
				    	dlg.show();
					}
				}
				
			}
		});
		
		displayAd(R.id.frameLayout_GameManager);
		
		// Load up the panel with the selected game
//		Intent intent = getIntent();
//		if (gameList!=null && gameList.size()>0)
//			onLoad(gameList.get(spinGameNames.getSelectedItemPosition())); 
		
	}
	
	private void sendFriendRequest(final String userId) {
		LogUtil.info("Showing feint dialog for user "+userId);
		User.findByID(userId, new User.FindCB() {

			@Override
			public void onSuccess(User foundUser) {
				AlertDialog dlg = CMActivity.getFeintUserDialog(GameManagerActivity.this, foundUser, currentGame.getGameName());
				dlg.show();
			}

			@Override
			public void onFailure(String exceptionMessage) {
				LogUtil.error("Error getting Feint user="+userId+": "+exceptionMessage);
				toastMessage(getResources().getString(R.string.Feint_User_Lookup_Error, ""), Toast.LENGTH_SHORT);
			}
		});
	}

	protected void onPause() {
		super.onPause();
		LogUtil.info("GameManager onPause()");
		this.unregisterReceiver(myReceiver);
		bLoaded = false;
	}

	protected void onResume() {
		LogUtil.info("GameManager onResume()");
		// May have joined another game while gone; get it loaded
		//setupSpinner();

		// Load up the panel with the selected game - use game from intent if its there
		Intent intent = getIntent();
		if (intent!=null) {
			Bundle bundle = intent.getExtras();
			if (bundle!=null) {
				Game inGame = bundle.getParcelable(CMConstants.GAME_PACKAGE);
				if (inGame!=null && !bLoaded) {
					LogUtil.info("onLoad from onResume game in intent");
					Game game = ((CMApplication)getApplicationContext()).dh.selectGame(inGame.getGameNumber());
					if (game!=null)
						onLoad(game);
				}
				else {
					LogUtil.info("GameManager onResume() inGame is null");					
				}
			}
			else if (!bLoaded) {
				LogUtil.info("GameManager onResume() bundle is null");
				setupSpinner(); // hopefully this causes onLoad() to be called
//				if (gameList!=null && gameList.size()>0)
//					onLoad(gameList.get(spinGameNames.getSelectedItemPosition()));
			}
		}
		else if (!bLoaded) {
			setupSpinner(); // hopefully this causes onLoad() to be called
//			if (gameList!=null && gameList.size()>0)
//				onLoad(gameList.get(spinGameNames.getSelectedItemPosition()));
		}
			
		bLoaded = true;
		super.onResume();
		
//		if (currentGame!=null)
//			onLoad(currentGame);
//		if (currentGame==null && gameList!=null && gameList.size()>0) {
//			LogUtil.info("onLoad from onResume");
//			onLoad(gameList.get(spinGameNames.getSelectedItemPosition()));
//		}
		
	}
	
	private void setupSpinner() {
		// Load up the game name combo box with all game names
		gameList = ((CMApplication)getApplicationContext()).dh.selectAll();
		List<String> listGameNames = new ArrayList<String>(gameList.size());
		for (int i=0; i < gameList.size(); i++) {
			listGameNames.add(gameList.get(i).getGameName());
		}

		spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listGameNames);
		spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinGameNames.setAdapter(spinAdapter);
		// In case we get here from on resume, try and re-load our current game
//		if (currentGame!=null) {
//			// What position is our game in?
//			int i=0;
//			for (i=0; i < gameList.size(); i++) {
//				if (currentGame.getGameName().equals(gameList.get(i).getGameName()))
//					break;
//			}
//			if (i < gameList.size())
//				spinGameNames.setSelection(i);
//			else
//				spinGameNames.setSelection(0);
//		}
//		else if (gameList.size()>0)
//			spinGameNames.setSelection(0);
//		else
//		{
//			// display some message that you're not in a game
//		}
	}
	
	private void onLoad(Game game) {
		bLoaded = true;
		LogUtil.info("GameManager onLoad()");
		currentGame = game;
		
		updateScore();
		
		// State
		imgState.setImageDrawable(currentGame.isCat() ? getResources().getDrawable(R.drawable.ic_stat_notify_cat) : getResources().getDrawable(R.drawable.ic_stat_notify_mouse));
		
		// Leaderboard button
		Button buttonLeaderboard = (Button) findViewById(R.id.button_GameManagerLeaderboard);
		buttonLeaderboard.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				// Launch the Feint leaderboard for this game
				Leaderboard.list(new Leaderboard.ListCB() {
					public void onSuccess(List<Leaderboard> leaderboards) {
						boolean bFound = false;
						for (int i=0; i < leaderboards.size(); i++) {
							Leaderboard l = leaderboards.get(i);
							if (l.name.equals(currentGame.getGameName())) {
								bFound = true;
								Dashboard.openLeaderboard(l.resourceID());
								break;
							}
						}
						if (!bFound)
							toastMessage(getResources().getString(R.string.No_Leaderboard), Toast.LENGTH_SHORT);
					}
				});
			}
		});
		
		// Get Mice button
		buttonGetMice.setText(getResources().getString(R.string.Get_Mice));
		buttonGetMice.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				// Prompt to go into discovery mode if we're not already in it
				if (mBluetoothAdapter!=null) {
					int iScanMode = mBluetoothAdapter.getScanMode();
					if (iScanMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
						Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
						discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
						startActivityForResult(discoverIntent, REQUEST_DISCOVERABLE);
					}
				}
			}
		});
		
		// button should only be enabled if I'm a cat and I'm not already in discoverable mode
		// default to disabled
		buttonGetMice.setEnabled(false);
		if (mBluetoothAdapter!=null) {
			int iScanMode = mBluetoothAdapter.getScanMode();
			if (currentGame.isCat() && iScanMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
				buttonGetMice.setEnabled(true);
			else if (currentGame.isCat() && iScanMode==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
				buttonGetMice.setText(getResources().getString(R.string.Scanning));
		}
		
		// Game type
		tvGameType.setText(" "+(currentGame.getGameType()==CMConstants.GAME_TYPE_NORMAL ? getResources().getString(R.string.Normal) : 
			getResources().getString(R.string.Reverse)));
		
		// Ratio
		tvRatio.setText(" "+Integer.toString(currentGame.getRatio())+":1");
		
		// Location
		tvLocation.setText(" "+game.getLocationName());
		
		// Player Map button
		buttonPlayerMap.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				// Player map
				Intent mapIntent = new Intent(GameManagerActivity.this, PlayerLocationActivity.class);
				mapIntent.putExtra(CMConstants.PARM_GAME_NUMBER, currentGame.getGameNumber());
				mapIntent.putExtra(CMConstants.PARM_GAME_NAME, currentGame.getGameName());
				startActivity(mapIntent);
			}
		});		
		
		// Activity window - clear it out and fill it up
		mActivityArrayAdapter.clear();
		mActivityArrayIds.clear();
		updateActivity();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_DISCOVERABLE && resultCode!=Activity.RESULT_CANCELED) {
			// change the state of the mouse button
    		buttonGetMice.setText(getResources().getString(R.string.Scanning));
    		buttonGetMice.setEnabled(false);
    		buttonGetMice.refreshDrawableState();
		}
	}

	protected void registerReceiver() {
		super.registerReceiver();
		
		IntentFilter filter = new IntentFilter(CMIntentActions.ACTION_PLAYER_CAUGHT);
		this.registerReceiver(myReceiver, filter);
		
		filter = new IntentFilter(CMIntentActions.ACTION_STATE_CAT);
		this.registerReceiver(myReceiver, filter);
		
		filter = new IntentFilter(CMIntentActions.ACTION_STATE_MOUSE);
		this.registerReceiver(myReceiver, filter);
		
		filter = new IntentFilter(CMIntentActions.ACTION_SCORE_UPDATE);
		this.registerReceiver(myReceiver, filter);
		
		filter = new IntentFilter(CMIntentActions.ACTION_LOGOUT);
		this.registerReceiver(myReceiver, filter);
		
		filter = new IntentFilter(CMIntentActions.ACTION_GAME_OVER);
		this.registerReceiver(myReceiver, filter);
		
		filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		this.registerReceiver(myReceiver, filter);
		
	}	
	
	protected final BroadcastReceiver myReceiver  = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CMIntentActions.ACTION_PLAYER_CAUGHT.equals(action)) {
				// Post this if it's for this game
				Bundle bundle = intent.getExtras();
				CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
				if (n!=null && n.getGameNumber()==currentGame.getGameNumber()) {
					// OK, I'll post it
					//String msg = n.getCatPlayerName()+" "+getResources().getString(R.string.caught)+" "+n.getMousePlayerName();
					updateActivity();//msg, n.getActivityTime());
				}
			}
			else if (CMIntentActions.ACTION_STATE_CAT.equals(action)) {
				// Post this if it's for this game
				Bundle bundle = intent.getExtras();
				CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
				if (n!=null && n.getGameNumber()==currentGame.getGameNumber()) {
					// OK, I'll post it
					//String msg = n.getCatPlayerName()+" "+getResources().getString(R.string.became_cat);
					updateActivity();//msg, n.getActivityTime());
					
					// Now, if this is me, I need to update currentGame and my status
					User user = ((CMApplication)getApplicationContext()).user;
					if (user!=null && user.name!=null && user.name.length()>0) {
						if (n!=null && n.getCatPlayerName().equals(user.name)) {
							// Great Scott it is me!  Make some changes...
							currentGame.setCat(true);
							imgState.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_notify_cat));
							
							// Make sure the Get Mice button is setup correctly...
							int iScanMode = mBluetoothAdapter.getScanMode();
							if (iScanMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
								// Make sure it's enabled and says Get Mice
								buttonGetMice.setText(getResources().getString(R.string.Get_Mice));
								buttonGetMice.setEnabled(true);
							}
							else {
								// We're discoverable. Make sure it says scanning and is disabled
								buttonGetMice.setText(getResources().getString(R.string.Scanning));
								buttonGetMice.setEnabled(false);
							}
						}
					}				
					else {
						LogUtil.error("Bad user object");
						((CMApplication)getApplicationContext()).loadUser();
					}
				}				
			}
			else if (CMIntentActions.ACTION_STATE_MOUSE.equals(action)) {
				// Post this if it's for this game
				Bundle bundle = intent.getExtras();
				CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
				if (n!=null && n.getGameNumber()==currentGame.getGameNumber()) {
					// OK, I'll post it
					//String msg = n.getMousePlayerName()+" "+getResources().getString(R.string.became_mouse);
					updateActivity();//msg, n.getActivityTime());
				}				
				
				// Now, if this is me, I need to update currentGame and my status
				User user = ((CMApplication)getApplicationContext()).user;
				if (user!=null && user.name!=null && user.name.length()>0) {
					if (n!=null && n.getMousePlayerName().equals(user.name)) {
						// Cool
						currentGame.setCat(false);
						imgState.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_notify_mouse));
						
						// Make sure the Get Mice button says Get Mice and is disabled
						buttonGetMice.setText(getResources().getString(R.string.Get_Mice));
						buttonGetMice.setEnabled(false);
					}
				}
				else {
					LogUtil.error("Bad user object");
					((CMApplication)getApplicationContext()).loadUser();
				}
			}
			else if (CMIntentActions.ACTION_GAME_OVER.equals(action)) {
				// let em know
				Bundle bundle = intent.getExtras();
				CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
				if (currentGame!=null && n!=null && currentGame.getGameNumber()==n.getGameNumber()) {
					// its this one, call updateActivity
					updateActivity();
				}
			}
			else if (CMIntentActions.ACTION_LOGOUT.equals(action)) {
				// let em know
				Bundle bundle = intent.getExtras();
				CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
				if (currentGame!=null && n!=null && currentGame.getGameNumber()==n.getGameNumber()) {
					// its this one, call updateActivity
					updateActivity();
				}
			}			
			else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
				LogUtil.info("Action_Scan_Mode_Changed in GameManager. Action: "+action.toString()+" intent:"+intent.toString());
				// Enable or disable the Get Mice button based off this action and our current state
            	int iNewMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
            	int iOldMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, 0);
            	
            	if (iNewMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            		// We just became discoverable. Disable the button and change the text
            		LogUtil.info("GameManagerActivity: detected SCAN_MODE_CONNECTABLE_DISCOVERABLE");
            		buttonGetMice.setText(getResources().getString(R.string.Scanning));
            		buttonGetMice.setEnabled(false);
            		buttonGetMice.refreshDrawableState();
            	}
            	else {
            		// Are we still a cat in this game?
            		if (currentGame.isCat()) {
            			// We are, re-enable the button
						buttonGetMice.setText(getResources().getString(R.string.Get_Mice));
						buttonGetMice.setEnabled(true);
						buttonGetMice.refreshDrawableState();
            		}
            		else {
            			// Nope, disable the button
						buttonGetMice.setText(getResources().getString(R.string.Get_Mice));
						buttonGetMice.setEnabled(false);
						buttonGetMice.refreshDrawableState();
            		}
            	}
			}
			else if (CMIntentActions.ACTION_SCORE_UPDATE.equals(action)) {
				// Is it for my game?
				int gameNumber = intent.getIntExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, -1);
				long score = intent.getLongExtra(CMConstants.PACKAGE+CMConstants.PARM_SCORE, -1);
				boolean bFeintScore = intent.getBooleanExtra(CMConstants.PACKAGE+CMConstants.PARM_FEINT_SCORE, false);
				if (currentGame!=null && currentGame.getGameNumber()==gameNumber) {
					// Why it sure is!  Update my score
					if (bFeintScore)
						updateFeintScore(score);
					else
						updateLocalScore(score);
				}
			}
		}
	};
	
	private void updateFeintScore(long value) {
		feintScore = value;
		tvScore.setText(Long.toString(value));
		currentGame.setCachedScore(0);
	}
	
	private void updateLocalScore(long value) {
		tvScore.setText(Long.toString(feintScore+value));
		currentGame.setCachedScore(value);
	}
	
	private void updateScore() {
		// Score - 2 parts; get db part and Feint part
		final User user = ((CMApplication)getApplicationContext()).user;
		if (user!=null) {
			Leaderboard.list(new Leaderboard.ListCB() {
				public void onSuccess(List<Leaderboard> leaderboards) {
					boolean bFoundLeaderboard = false;
					for (int i=0; i < leaderboards.size(); i++) {
						Leaderboard l = leaderboards.get(i);
						if (l.name.equals(currentGame.getGameName())) {
							bFoundLeaderboard = true;
							l.getUserScore(user, new Leaderboard.GetUserScoreCB(){

								public void onSuccess(Score score) {
									Long myScore = (score!=null ? score.score : 0);
									feintScore = myScore;
									// Sweet - get my cached score and add it to this
									Game game = ((CMApplication)getApplicationContext()).dh.selectGame(currentGame.getGameNumber());
									if (game!=null) {
										myScore += game.getCachedScore();
										currentGame.setCachedScore(game.getCachedScore());
									}
									tvScore.setText(myScore.toString());
								}

								public void onFailure(String exceptionMessage) {
									// Don't care why it failed, other than to log it. Use score from db
									LogUtil.error("Error retrieving score from Feint: "+exceptionMessage);
									Game game = ((CMApplication)getApplicationContext()).dh.selectGame(currentGame.getGameNumber());
									if (game!=null) {
										tvScore.setText(Long.toString(game.getCachedScore()));
										currentGame.setCachedScore(game.getCachedScore());
									}
									else
										tvScore.setText(Long.toString(currentGame.getCachedScore()));
								}

							});
							break;
						}
					}
					if (!bFoundLeaderboard) {
						Game game = ((CMApplication)getApplicationContext()).dh.selectGame(currentGame.getGameNumber());
						if (game!=null) {
							tvScore.setText(Long.toString(game.getCachedScore()));
							currentGame.setCachedScore(game.getCachedScore());
						}
						else
							tvScore.setText(Long.toString(currentGame.getCachedScore()));
					}
				}

				public void onFailure(String exceptionMessage) {
					// whatever, just use cached score
					LogUtil.error("Error retrieving score from Feint: "+exceptionMessage);
					Game game = ((CMApplication)getApplicationContext()).dh.selectGame(currentGame.getGameNumber());
					if (game!=null) {
						tvScore.setText(Long.toString(game.getCachedScore()));
						currentGame.setCachedScore(game.getCachedScore());
					}
					else
						tvScore.setText(Long.toString(currentGame.getCachedScore()));
				}
			});
		}
		else {
			// WTF?
			LogUtil.error("User is null!");
			((CMApplication)getApplicationContext()).loadUser();
			// Set score from game
			Game game = ((CMApplication)getApplicationContext()).dh.selectGame(currentGame.getGameNumber());
			if (game!=null) {
				tvScore.setText(Long.toString(game.getCachedScore()));
				currentGame.setCachedScore(game.getCachedScore());
			}
			else
				tvScore.setText(Long.toString(currentGame.getCachedScore()));
		}

	}
	
	private void updateActivity() {
		// Get all the activities for this game from the db
		List<CMNotification> listNotifications = ((CMApplication)getApplicationContext()).ndh.selectNotifications(0, currentGame.getGameNumber());
		
		mActivityArrayAdapter.clear();
		mActivityArrayIds.clear();
		for (int i=0; i < listNotifications.size(); i++) {
			CMNotification n = listNotifications.get(i);
			
			// Add the message with proper datestamp to the top of the Activity window
			//DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(this);
			Date timeStamp = new Date(n.getActivityTime());
			String msg = android.text.format.DateFormat.format("hh:mm:ss AA",timeStamp)+SPACES;
			
			if (n.getNotificationType()==CMConstants.NOTIFICATION_PLAYER_CAUGHT) {
				msg += n.getCatPlayerName()+" "+getResources().getString(R.string.caught)+" "+n.getMousePlayerName();
			} else if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_CAT) {
				msg += n.getCatPlayerName()+" "+getResources().getString(R.string.became_cat);
			} else if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_MOUSE) {
				msg += n.getMousePlayerName()+ " "+getResources().getString(R.string.became_mouse);
			} else if (n.getNotificationType()==CMConstants.NOTIFICATION_GAME_OVER) {
				msg += getResources().getString(R.string.Game_Ended);
			} else if (n.getNotificationType()==CMConstants.NOTIFICATION_INACTIVTY_LOGOUT) {
				msg += getResources().getString(R.string.Inactivity_Logout);
			}
			
			mActivityArrayAdapter.insert(msg, 0);
			mActivityArrayIds.add(0, new Pair<String,String>(n.getCatPlayerId(), n.getMousePlayerId()));
		}
	}
	
//	private class CustomAdapter extends ArrayAdapter<CMNotification> implements OnClickListener{
//		List<CMNotification> objects;
//		
//		public CustomAdapter(Context context, int resource,
//				int textViewResourceId, List<CMNotification> objects) {
//			super(context, resource, textViewResourceId, objects);
//			this.objects = objects;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			ViewHolder holder = null;
//			TextView tvDate = null;
//			TextView tvCat = null;
//			TextView tvVerb = null;
//			TextView tvMouse = null;
//			if (convertView==null) {		    	
//				convertView = mInflater.inflate(R.layout.activity_item,null);
//		    	holder = new ViewHolder(convertView);
//	            convertView.setTag(holder);		    	
//			}
//			
//			holder = (ViewHolder) convertView.getTag();
//			
//			CMNotification n = objects.get(position);
//			
//			Date timeStamp = new Date(n.getActivityTime());
//			tvDate = holder.getTvDate();
//			tvDate.setText(android.text.format.DateFormat.format("hh:mm:ss AA",timeStamp)+"    ");
//			
//			if (n.getNotificationType()==CMConstants.NOTIFICATION_PLAYER_CAUGHT) {
//				tvCat = holder.getTvActivity1();
//				tvCat.setText(n.getCatPlayerName()+" ");
//				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, (float)0.4);
//				tvCat.setLayoutParams(params);
//				
//				if (!((CMApplication)getApplicationContext()).user.name.equals(n.getCatPlayerName())) {
//					tvCat.setTag(n.getCatPlayerName());
//					tvCat.setTextColor(R.color.blue);
//					tvCat.setOnClickListener(this);//new View.OnClickListener() {
//
////						@Override
////						public void onClick(View v) {
////							User.findByName((String) ((TextView)v).getText(), new User.FindCB() {
////
////								@Override
////								public void onSuccess(User foundUser) {
////									AlertDialog dlg = CMActivity.getFeintUserDialog(GameManagerActivity.this, foundUser, currentGame.getGameName());
////									dlg.show();
////								}
////							});
////						}
////					});
//				}
//				tvVerb = holder.getTvActivity2();
//				tvVerb.setText(getResources().getString(R.string.caught)+" ");
//				tvMouse = holder.getTvActivity3();
//				tvMouse.setText(n.getMousePlayerName());
//				tvCat.setLayoutParams(params);
//				
//				if (!((CMApplication)getApplicationContext()).user.name.equals(n.getMousePlayerName())) {
//					tvMouse.setTextColor(R.color.blue);
//					tvMouse.setTag(n.getMousePlayerName());
//					tvMouse.setOnClickListener(this);//new View.OnClickListener() {
//
////						@Override
////						public void onClick(View v) {
////							User.findByName((String) ((TextView)v).getText(), new User.FindCB() {
////
////								@Override
////								public void onSuccess(User foundUser) {
////									AlertDialog dlg = CMActivity.getFeintUserDialog(GameManagerActivity.this, foundUser, currentGame.getGameName());
////									dlg.show();
////								}
////							});
////						}
////					});
//				}
//			} else if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_CAT) {
//				tvCat = holder.getTvActivity1();
//				tvCat.setText(n.getCatPlayerName()+" ");
//				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, (float)0.5);
//				tvCat.setLayoutParams(params);
//				
//				if (!((CMApplication)getApplicationContext()).user.name.equals(n.getCatPlayerName())) {
//					tvCat.setTextColor(R.color.blue);
//					tvCat.setTag(n.getCatPlayerName());
//					tvCat.setOnClickListener(this);//new View.OnClickListener() {
//
////						@Override
////						public void onClick(View v) {
////							User.findByName((String) ((TextView)v).getText(), new User.FindCB() {
////
////								@Override
////								public void onSuccess(User foundUser) {
////									AlertDialog dlg = CMActivity.getFeintUserDialog(GameManagerActivity.this, foundUser, currentGame.getGameName());
////									dlg.show();
////								}
////							});
////						}
////					});
//				}				
//				tvVerb = holder.getTvActivity2();
//				tvVerb.setText(getResources().getString(R.string.became_cat));
//			} else if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_MOUSE) {
//				tvMouse = holder.getTvActivity1();
//				tvMouse.setText(n.getMousePlayerName()+" ");
//				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, (float)0.5);
//				tvMouse.setLayoutParams(params);
//				
//				if (!((CMApplication)getApplicationContext()).user.name.equals(n.getMousePlayerName())) {
//					tvMouse.setTextColor(R.color.blue);
//					tvMouse.setTag(n.getMousePlayerName());
//					tvMouse.setOnClickListener(this);//new View.OnClickListener() {
//
////						@Override
////						public void onClick(View v) {
////							User.findByName((String) ((TextView)v).getText(), new User.FindCB() {
////
////								@Override
////								public void onSuccess(User foundUser) {
////									AlertDialog dlg = CMActivity.getFeintUserDialog(GameManagerActivity.this, foundUser, currentGame.getGameName());
////									dlg.show();
////								}
////							});
////						}
////					});
//				}				
//				tvVerb = holder.getTvActivity2();
//				tvVerb.setText(getResources().getString(R.string.became_mouse));
//			}
//			
//			return convertView;
//		}
//		
//		private class ViewHolder {
//			private View mRow;
//			private TextView tvDate=null;
//			private TextView tvActivity1=null;
//			private TextView tvActivity2=null;
//			private TextView tvActivity3=null;
//			
//			public ViewHolder(View row) {
//				mRow = row;
//			}
//
//			public TextView getTvDate() {
//				if (tvDate==null)
//					tvDate = (TextView) mRow.findViewById(R.id.textView_Activity_Date);
//				return tvDate;
//			}
//
//			public TextView getTvActivity1() {
//				if (tvActivity1==null)
//					tvActivity1 = (TextView) mRow.findViewById(R.id.textView_Activity_1);
//				return tvActivity1;
//			}
//
//			public TextView getTvActivity2() {
//				if (tvActivity2==null)
//					tvActivity2 = (TextView) mRow.findViewById(R.id.textView_Activity_2);
//				return tvActivity2;
//			}
//
//			public TextView getTvActivity3() {
//				if (tvActivity3==null)
//					tvActivity3 = (TextView) mRow.findViewById(R.id.textView_Activity_3);
//				return tvActivity3;
//			}
//			
//			
//		}
//
//		@Override
//		public void onClick(View v) {
//			LogUtil.info("Activity item clicked: "+v.toString());
//			String tag = (String) v.getTag();
//			if (tag!=null && tag.length()>0) {
//				LogUtil.info("Tag = "+tag);
//				User.findByName(tag, new User.FindCB() {
//
//					@Override
//					public void onSuccess(User foundUser) {
//						AlertDialog dlg = CMActivity.getFeintUserDialog(GameManagerActivity.this, foundUser, currentGame.getGameName());
//						dlg.show();
//					}
//
//					@Override
//					public void onFailure(String exceptionMessage) {
//						LogUtil.error("Error getting Feint user: "+exceptionMessage);
//					}
//					
//					
//				});
//
//			}
//			else
//				LogUtil.warn("Tag is null");
//		}
//	}

}
