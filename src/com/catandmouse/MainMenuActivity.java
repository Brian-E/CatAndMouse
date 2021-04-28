package com.catandmouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.android.vending.licensing.LicenseCheckerCallback;
import com.openfeint.api.resource.User;
import com.openfeint.api.ui.Dashboard;
import com.openfeint.gamefeed.GameFeedSettings;
import com.openfeint.gamefeed.GameFeedView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuActivity extends CMActivity {
	//DialogInterface.OnClickListener cancelListener;
	TextView tvPro;
	TextView tvCurrentGames;
	Button buttonPro;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
//		cancelListener = new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.cancel();
//			}
//		};
		
		// Override buttons
		ImageButton buttonFeint = (ImageButton) findViewById(R.id.imageButton_Main_Feint);
		buttonFeint.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Dashboard.open();
			}
		});
		
		// Join game
		Button buttonJoinGame = (Button) findViewById(R.id.button_Main_JoinGame);
		buttonJoinGame.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenuActivity.this, JoinGameActivity.class);
				startActivity(intent);
			}
		});
		
		// Quit Game
		Button buttonQuitGame = (Button) findViewById(R.id.button_Main_QuitGame);
		buttonQuitGame.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// First, are they in any games?
				final List<Game>gameList = ((CMApplication)getApplicationContext()).dh.selectAll();
				if (gameList.size()==0) {
					toastMessage(getResources().getString(R.string.No_Games_To_Quit), Toast.LENGTH_SHORT);
					return;
				}
				
		    	LayoutInflater inflater =
		    	    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    	final View layout =
		    	    inflater.inflate(R.layout.quit_game,null);
		    	final Spinner spinGames = (Spinner) layout.findViewById(R.id.spinner_SelectQuitGame);
		    	
				List<String> listGameNames = new ArrayList<String>(gameList.size()+1);
				listGameNames.add(getResources().getString(R.string.All_Games));
				for (int i=0; i < gameList.size(); i++) {
					listGameNames.add(gameList.get(i).getGameName());
				}

				ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(MainMenuActivity.this, android.R.layout.simple_spinner_item, listGameNames);
				spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinGames.setAdapter(spinAdapter);
				spinGames.setSelection(0);
				
				DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// get the feint user first
						final User user = ((CMApplication)getApplicationContext()).user;
						if (user!=null && user.userID()!=null) {
							// What game did they pick?
							final int iGame = spinGames.getSelectedItemPosition();
							waitCursor = ProgressDialog.show(MainMenuActivity.this, null, getResources().getString(R.string.Quitting_Game), true, true);
							Thread quitThread = new Thread(){

								public void run() {
									if (iGame==0) {
										// All games. OK, spin through the list and issue a quit game for each
										boolean success = true;
										for (int i=0; i < gameList.size(); i++) {
											int rc = ServerUtil.quitGame(user.userID(), gameList.get(i).getGameNumber());
											if (rc!=HttpStatus.SC_OK) {
												LogUtil.error("Error quitting game, rc: "+rc);
												success = false;
											}
											// Whether it worked or not, proceed like it did
											// delete game from here in case GameService isn't running
											((CMApplication)getApplicationContext()).dh.delete(gameList.get(i).getGameNumber());
											Intent deleteIntent = new Intent(CMIntentActions.ACTION_QUIT_GAME);
											deleteIntent.putExtra(CMConstants.GAME_PACKAGE+CMConstants.PARM_GAME_NUMBER, gameList.get(i).getGameNumber());
											sendBroadcast(deleteIntent);
										}
										// Logout the user
										ServerUtil.doLogout(user);
										waitCursor.dismiss();
										if (success) {
											handler.post(new Runnable(){

												public void run() {
													updateGameList();
													toastMessage(getResources().getString(R.string.Successfully_Quit_Game), Toast.LENGTH_SHORT);
												}
												
											});
										}
										else {
											handler.post(new Runnable(){

												public void run() {
													DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
															dialog.cancel();
														}
													};
													Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Error), 
															getResources().getString(R.string.Failed_Quit_Games), cancelListener);
													dlg.show();
												}
												
											});
										}
									}
									else {
										Game game = gameList.get(iGame-1);
										int rc = ServerUtil.quitGame(user.userID(), game.getGameNumber());
										waitCursor.dismiss();
										if (rc==HttpStatus.SC_OK) {
											// good message
											handler.post(new Runnable(){

												public void run() {
													updateGameList();
													toastMessage(getResources().getString(R.string.Successfully_Quit_Game), Toast.LENGTH_SHORT);
												}
												
											});
											((CMApplication)getApplicationContext()).dh.delete(game.getGameNumber());
										}
										else {
											LogUtil.error("Error quitting game, rc: "+rc);
											handler.post(new Runnable(){

												public void run() {
													DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
															dialog.cancel();
														}
													};
													Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Error), 
															getResources().getString(R.string.Failed_Quit_Game), cancelListener);
													dlg.show();
												}
											});
										}
										// Whether it worked or not, proceed like it did
										// delete game from here in case GameService isn't running
										((CMApplication)getApplicationContext()).dh.delete(game.getGameNumber());
										Intent deleteIntent = new Intent(CMIntentActions.ACTION_QUIT_GAME);
										deleteIntent.putExtra(CMConstants.GAME_PACKAGE+CMConstants.PARM_GAME_NUMBER, game.getGameNumber());
										sendBroadcast(deleteIntent);
										
										// if we're not in any games, log us out
										if (((CMApplication)getApplicationContext()).dh.selectAll().size()==0)
											ServerUtil.doLogout(user);
									}
								}
								
							};
							quitThread.start();
						}
						else {
							// Problem with the feint user
							LogUtil.error("Feint user is null");
							// error message
							DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							};
							Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Error), 
									getResources().getString(R.string.Feint_Not_Logged_In), cancelListener);
							dlg.show();
							((CMApplication)getApplicationContext()).loadUser();
						}
					}
				};
				
				DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				};

		    	AlertDialog dlg = getGenericOKCancelDialog(getResources().getString(R.string.Quit_Game), layout,
		    			okListener, cancelListener);
		    	dlg.show();
//				dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){
//					public void onClick(View arg0) {
//						// get the feint user first
//						User user = ((CMApplication)getApplicationContext()).user;
//						if (user!=null && user.userID()!=null) {
//							// What game did they pick?
//							int iGame = spinGames.getSelectedItemPosition();
//							if (iGame==0) {
//								// All games. OK, spin through the list and issue a quit game for each
//								boolean success = true;
//								for (int i=0; i < gameList.size(); i++) {
//									int rc = ServerUtil.quitGame(user.userID(), gameList.get(i).getGameNumber());
//									if (rc!=HttpStatus.SC_OK) {
//										LogUtil.error("Error quitting game, rc: "+rc);
//										success = false;
//									}
//									else {
//										((CMApplication)getApplicationContext()).dh.delete(gameList.get(i).getGameNumber());
//									}
//								}
//								if (success) {
//									toastMessage(getResources().getString(R.string.Successfully_Quit_Game), Toast.LENGTH_SHORT);
//								}
//								else {
//									Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Error), getResources().getString(R.string.Failed_Quit_Games));
//									dlg.show();
//								}
//							}
//							else {
//								Game game = gameList.get(iGame);
//								int rc = ServerUtil.quitGame(user.userID(), game.getGameNumber());
//								if (rc==HttpStatus.SC_OK) {
//									// good message
//									toastMessage(getResources().getString(R.string.Successfully_Quit_Game), Toast.LENGTH_SHORT);
//									((CMApplication)getApplicationContext()).dh.delete(game.getGameNumber());
//								}
//								else {
//									LogUtil.error("Error quitting game, rc: "+rc);
//									Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Error), getResources().getString(R.string.Failed_Quit_Game));
//									dlg.show();
//								}
//							}
//						}
//						else {
//							// Problem with the feint user
//							LogUtil.error("Feint user is null");
//							// error message
//							Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Error), getResources().getString(R.string.Feint_Not_Logged_In));
//							dlg.show();
//							((CMApplication)getApplicationContext()).loadUser();
//						}
//					}
//				});
//
			}
		});
		
		// Create game
		Button buttonCreateGame = (Button) findViewById(R.id.button_Main_CreateGame);
		buttonCreateGame.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenuActivity.this, CreateGameActivity.class);
				startActivity(intent);
			}
		});
		
		// Game Manager
		Button buttonGameManager = (Button) findViewById(R.id.button_Main_GameManager);
		buttonGameManager.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenuActivity.this, GameManagerActivity.class);
				startActivity(intent);
			}
		});
		
		// Help
		Button buttonHelp = (Button) findViewById(R.id.button_Main_Help);
		buttonHelp.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenuActivity.this, HelpActivity.class);
				startActivity(intent);
			}
		});
		
		// Support
		Button buttonSupport = (Button) findViewById(R.id.button_Main_Support);
		buttonSupport.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenuActivity.this, SupportActivity.class);
				startActivity(intent);
			}
		});
		
		// Pro Unlock
		buttonPro = (Button) findViewById(R.id.button_Main_ProUnlock);
		buttonPro.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				waitCursor = ProgressDialog.show(MainMenuActivity.this, null, getResources().getString(R.string.Checking_Pro), true, true);
				Thread proThread = new Thread() {

					@Override
					public void run() {
						// First, check the signature
						if (!((CMApplication)getApplicationContext()).checkProPackage()) {
							waitCursor.dismiss();
							handler.post(new Runnable(){

								public void run() {
									DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											Intent intent = new Intent(Intent.ACTION_VIEW);
											intent.setData(Uri.parse("market://details?id="+CMConstants.PRO_PACKAGE));
											startActivity(intent);					
										}
									};

									DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
										
									};
									Dialog d = getGenericYesNoDialog(getResources().getString(R.string.No_Pro_Title), 
											getResources().getString(R.string.No_Pro_Msg), okListener, cancelListener);
									d.show();								
								}
							});
						}
						else {
							// Passed the first check, now check the license
							CMLicenseCheck.doLicenseCheck(MainMenuActivity.this, new LicenseCheckerCallback(){

								@Override
								public void allow() {
									waitCursor.dismiss();
									handler.post(new Runnable(){

										@Override
										public void run() {
											((CMApplication)getApplicationContext()).setPro(true);
											doProUnlock();
										}					
									});
								}

								@Override
								public void dontAllow() {
									waitCursor.dismiss();
									handler.post(new Runnable(){

										public void run() {
											String title = getResources().getString(R.string.License_Check_Failed_Title);
											String msg = getResources().getString(R.string.License_Check_Failed_Msg);
											DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													dialog.cancel();
												}
											};
											Dialog dialog = getGenericAlertDialog(title, msg, listener);
											dialog.show();
										}
										
									});
								}

								@Override
								public void applicationError(ApplicationErrorCode errorCode) {
									// Shit, this is bad!
									waitCursor.dismiss();
									LogUtil.error("Error checking the license: "+errorCode.toString());
									handler.post(new Runnable(){

										public void run() {
											String title = getResources().getString(R.string.License_Check_Failed_Title);
											String msg = getResources().getString(R.string.License_Check_Error_Msg, CMConstants.EMAIL_SUPPORT);
											DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													dialog.cancel();
												}
											};
											Dialog dialog = getGenericAlertDialog(title, msg, listener);
											dialog.show();
										}
										
									});
								}
								
							});
						}
					}
				};
				proThread.start();
			}
		});
		
		tvPro = (TextView) findViewById(R.id.textView_Header_Pro);
		if (((CMApplication)getApplicationContext()).isPro()) {
			tvPro.setText(getResources().getString(R.string.Pro));
			buttonPro.setText(getResources().getString(R.string.Pro_Unlocked));
			buttonPro.setEnabled(false);
		}
		
		tvCurrentGames = (TextView) findViewById(R.id.textView_Main_CurrentGames);		
		
		// Check for a bluetooth adapter and give adequate warning if one is not found
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
		
		// GameFeed
        Map<String, Object> gameFeedSettings = new HashMap<String, Object>();
        gameFeedSettings.put(GameFeedSettings.Alignment, GameFeedSettings.AlignmentType.TOP);
        gameFeedSettings.put(GameFeedSettings.AnimateIn, true);
        GameFeedView gameFeedView = new GameFeedView(this, gameFeedSettings);
        gameFeedView.addToLayout(findViewById(R.id.linearLayout_Main_Main));
        
        // display Ad
        displayAd(R.id.frameLayout_Main);
	}
	
	private void updateGameList() {
		// update tvCurrentGames with our current games
		List<Game> gamesList = ((CMApplication)getApplicationContext()).dh.selectAll();
		if (gamesList.size()>0) {
			String strGames = getResources().getString(R.string.Current_Games)+" ";
			for (int i=0; i < gamesList.size(); i++) {
				if (i > 0)
					strGames += ", ";
				strGames += gamesList.get(i).getGameName();
			}
			tvCurrentGames.setText(strGames);
		}
		else {
			tvCurrentGames.setText(getResources().getString(R.string.No_Current_Games));
		}
	}

	@Override
	protected void registerReceiver() {
		super.registerReceiver();

		IntentFilter filter = new IntentFilter(CMIntentActions.ACTION_CHECKED_GAMES);
		this.registerReceiver(myReceiver, filter);
		
		filter = new IntentFilter(CMIntentActions.ACTION_PRO_CHECK);
		this.registerReceiver(myReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(myReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateGameList();
	}
	
	protected final BroadcastReceiver myReceiver  = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(CMIntentActions.ACTION_CHECKED_GAMES)) {
				// Update tvCurrentGames with our current game list
				updateGameList();
			}
			if (action.equals(CMIntentActions.ACTION_PRO_CHECK)) {
				if (((CMApplication)getApplicationContext()).isPro()) {
					doProUnlock();
				}
			}
		}
	};
	
	private void doProUnlock() {
		tvPro.setText(getResources().getString(R.string.Pro));
		buttonPro.setText(getResources().getString(R.string.Pro_Unlocked));
		buttonPro.setEnabled(false);
		toastMessage(getResources().getString(R.string.Pro_Unlocked), Toast.LENGTH_SHORT);		
	}

}
