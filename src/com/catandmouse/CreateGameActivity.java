package com.catandmouse;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

public class CreateGameActivity extends CMActivity implements OnClickListener {
	EditText editGameName, editPassword, editLocation, editLatitude, editLongitude, editRange;
	Spinner spinType, spinRatio; 
	ArrayAdapter<?> adapterTypes, adapterRatio;
	TextView tvMiceCats;
	CheckBox cbPrivate, cbLocation;
	Button buttonLocation, buttonCreateGame, buttonCancel;
	LinearLayout layoutLocationValues;
	DatePicker dpStartDate, dpEndDate;
	TimePicker tpStartTime, tpEndTime;
	//CreateGameActivity myself;
	CheckoutButton launchSimplePayment;
	DialogInterface.OnClickListener listener;
	//private static final int server = PayPal.ENV_SANDBOX;
	// The ID of your application that you received from PayPal
	//private static final String appID = "APP-80W284485P519543T";
	protected static final int INITIALIZE_SUCCESS = 0;
	protected static final int INITIALIZE_FAILURE = 1;
	private static final int PAYPAL_TRANACTION_REQUEST_CODE=150;
	private static final int CREATE_MAP_REQUEST_CODE=100;
	private boolean bRetryNoPayment = false;
	
	Handler hRefresh = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
		    	case INITIALIZE_SUCCESS:
					LogUtil.info("PayPal initialized");
		    		//setupButton();
		            break;
		    	case INITIALIZE_FAILURE:
					LogUtil.error("PayPal failed to initialize");
					AlertDialog diag = getGenericAlertDialog("PayPal initialization failed", "PayPal initialization failed, please try again", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							try {
								CreateGameActivity.this.finish();
							} catch (Throwable e) {
								e.printStackTrace();
							}
							
						}
						
					});
					diag.show();
		    		break;
			}
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.create_game);
		
		// is paypal initialized?
		if (!((CMApplication)getApplicationContext()).bPayPalInitialized) {
			LogUtil.error("PayPal failed to initialize");
			AlertDialog diag = getGenericAlertDialog(getResources().getString(R.string.Paypal_Init_Error_Title), getResources().getString(R.string.Paypal_Init_Error), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					try {
						CreateGameActivity.this.finish();
					} catch (Throwable e) {
						e.printStackTrace();
					}
					
				}
				
			});
			diag.show();
			return;
		}
		
		// init paypal
//		waitCursor = ProgressDialog.show(this, "Initializing PayPal", "Initializing PayPal, please wait...", true, true);
//		Thread libraryInitializationThread = new Thread() {
//			public void run() {
//				waitCursor.show();
//				initLibrary();
//				waitCursor.dismiss();
//				
//				// The library is initialized so let's create our CheckoutButton and update the UI.
//				if (PayPal.getInstance().isLibraryInitialized()) {
//					hRefresh.sendEmptyMessage(INITIALIZE_SUCCESS);
//				}
//				else {
//					hRefresh.sendEmptyMessage(INITIALIZE_FAILURE);
//				}
				
////				if (PayPal.getInstance().isLibraryInitialized()) {
////					LogUtil.info("PayPal initialized");
////					setupButton();
////
////					//bPayPalInitialzed = true;
////				}
////				else {
////					LogUtil.error("PayPal failed to initialize");
////					handler.post(new Runnable(){
////
////						@Override
////						public void run() {
////							AlertDialog diag = getGenericAlertDialog("PayPal initialization failed", "PayPal initialization failed, please try again", new DialogInterface.OnClickListener(){
////
////								@Override
////								public void onClick(DialogInterface dialog,
////										int which) {
////									try {
////										CreateGameActivity.this.finish();
////									} catch (Throwable e) {
////										e.printStackTrace();
////									}
////									
////								}
////								
////							});
////							diag.show();
////						}
////						
////					});
////					//PayPal.getInstance().setLibraryInitialized(true);
////					//bPayPalInitialzed = true;
////					//initPayPal(true);
////				}
//			}
//		};
//		libraryInitializationThread.start();		
		
		listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		};
		//myself = this;
		TextView tvInstructions = (TextView) findViewById(R.id.textView_CreateGame_Instructions);
		String instructions = getResources().getString(R.string.Create_Game_Info);
		instructions = String.format(instructions, ((CMApplication)getApplicationContext()).isPro() ? CMConstants.GAME_COST_PRO : CMConstants.GAME_COST_FREE);
		tvInstructions.setText(instructions);
		
		editGameName = (EditText) findViewById(R.id.editText_CreateGame_Name);
		spinType = (Spinner) findViewById(R.id.spinner_CreateGame_Type);
		adapterTypes = ArrayAdapter.createFromResource(this,
		        R.array.game_types, android.R.layout.simple_spinner_item);
		adapterTypes.setDropDownViewResource(
		    android.R.layout.simple_spinner_dropdown_item);		
		spinType.setAdapter(adapterTypes);
		spinType.setSelection(0);
		spinType.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String value = (String) adapterTypes.getItem(position);
				if (value.equals(getResources().getString(R.string.Normal))) {
					tvMiceCats.setText(getResources().getString(R.string.mice_cats));
				}
				else if (value.equals(getResources().getString(R.string.Reverse))) {
					tvMiceCats.setText(getResources().getString(R.string.cats_mice));
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		spinRatio = (Spinner) findViewById(R.id.spinner_CreateGame_Ratio);
		adapterRatio = ArrayAdapter.createFromResource(this,
		        R.array.ratios, android.R.layout.simple_spinner_item);
		adapterRatio.setDropDownViewResource(
		    android.R.layout.simple_spinner_dropdown_item);		
		spinRatio.setAdapter(adapterRatio);
		spinRatio.setSelection(8); // Hopefully this is 10...
		
		tvMiceCats = (TextView) findViewById(R.id.textView_CreateGame_MiceCatsLabel);
		
		cbPrivate = (CheckBox) findViewById(R.id.checkBox_CreateGame_Private);
		cbPrivate.setChecked(false);
		cbPrivate.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				editPassword.setEnabled(isChecked);
			}
		});
		
		editPassword = (EditText) findViewById(R.id.editText_CreateGame_Password);
		editPassword.setEnabled(false);
		
		editLocation = (EditText) findViewById(R.id.editText_CreateGame_Location);
		
		layoutLocationValues = (LinearLayout) findViewById(R.id.linearLayout_CreateGame_Location);
		layoutLocationValues.setVisibility(View.GONE);
		
		editLatitude = (EditText) findViewById(R.id.editText_CreateGame_Latitude);
		editLongitude = (EditText) findViewById(R.id.editText_CreateGame_Longitude);
		editRange = (EditText) findViewById(R.id.editText_CreateGame_Range);

		cbLocation = (CheckBox) findViewById(R.id.checkBox_CreateGame_Location);
		cbLocation.setChecked(false);
		cbLocation.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				buttonLocation.setEnabled(isChecked);
				layoutLocationValues.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				if (isChecked) {
					// set all values to 0
					editLatitude.setText("0");
					editLongitude.setText("0");
					editRange.setText("0");
				}
			}
		});
		
		buttonLocation = (Button) findViewById(R.id.button_CreateGame_SetLocation);
		buttonLocation.setEnabled(false);
		buttonLocation.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				Intent mapIntent = new Intent(CreateGameActivity.this, CircleActivity.class);
				startActivityForResult(mapIntent, CREATE_MAP_REQUEST_CODE);
			}
		});
		
		// Dates
		GregorianCalendar gc = new GregorianCalendar();
		gc.setLenient(true);
		dpStartDate = (DatePicker) findViewById(R.id.datePicker_CreateGame_StartTime);
		dpStartDate.init(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc.get(Calendar.DAY_OF_MONTH), null);
		dpEndDate = (DatePicker) findViewById(R.id.DatePicker_CreateGame_EndTime);
		gc.add(Calendar.DAY_OF_MONTH, 1);
		dpEndDate.init(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc.get(Calendar.DAY_OF_MONTH), null);
		
		gc.add(Calendar.DAY_OF_MONTH, -1);
		// Times
		tpStartTime = (TimePicker) findViewById(R.id.timePicker_CreateGame_StartTime);
		tpStartTime.setIs24HourView(false); // show AM/PM
		tpStartTime.setCurrentHour(gc.get(Calendar.HOUR_OF_DAY));
		tpStartTime.setCurrentMinute(gc.get(Calendar.MINUTE));
		
		tpEndTime = (TimePicker) findViewById(R.id.TimePicker_CreateGame_EndTime);
		tpEndTime.setIs24HourView(false); // show AM/PM
		tpEndTime.setCurrentHour(gc.get(Calendar.HOUR_OF_DAY));
		tpEndTime.setCurrentMinute(gc.get(Calendar.MINUTE));
		
		
		
//		buttonCreateGame = (Button) findViewById(R.id.button_CreateGame_CreateGame);
//		buttonCreateGame.setOnClickListener(this);//new OnClickListener(){
//			public void onClick(View arg0) {
//				// field validation, paypal, call create game, shitload!
//				if (!validData())
//					return;
//				
//				// Time to charge it to paypal
//				// First, how long is their game?
//				GregorianCalendar startDate = new GregorianCalendar();
//				startDate.set(dpStartDate.getYear(), dpStartDate.getMonth(), dpStartDate.getDayOfMonth(), 
//						tpStartTime.getCurrentHour(), tpStartTime.getCurrentMinute());
//				GregorianCalendar endDate = new GregorianCalendar();
//				endDate.set(dpEndDate.getYear(), dpEndDate.getMonth(), dpEndDate.getDayOfMonth(), 
//						tpEndTime.getCurrentHour(), tpEndTime.getCurrentMinute());
//				long gameLength = endDate.getTimeInMillis() - startDate.getTimeInMillis();
//				long gameDays = gameLength / CMConstants.TIME_MILLISECONDS_DAY + (gameLength % CMConstants.TIME_MILLISECONDS_DAY > 0 ? 1 : 0);
//				double gameCost = gameDays * (((CMApplication)getApplicationContext()).isPro() ? CMConstants.GAME_COST_PRO : CMConstants.GAME_COST_FREE);
//				
//				
//			}
//		});
		
		buttonCancel = (Button) findViewById(R.id.button_CreateGame_Cancel);
		buttonCancel.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				finish();
			}
		});
		
		// Paypal button
		setupButton();		
		
//		if (((CMApplication)getApplicationContext()).bPayPalInitialzed)
//			setupButton();
//		else {
//			AlertDialog dlg = this.getGenericAlertDialog(getResources().getString(R.string.Paypal_Init_Error_Title), getResources().getString(R.string.Paypal_Init_Error), new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//					CreateGameActivity.this.finish();
//				}
//			});
//			dlg.show();
//			//((CMApplication)getApplicationContext()).initPayPal(true);
//			
//		}
	}
	
	private void setupButton() {
		LinearLayout layoutButtons = (LinearLayout) findViewById(R.id.linearLayout_CreateGame_BottomButtons);
		
		// Add Paypal button
		
		LinearLayout layoutSimplePayment = new LinearLayout(this);
		layoutSimplePayment.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 
		    LayoutParams.WRAP_CONTENT));

		layoutSimplePayment.setOrientation(LinearLayout.VERTICAL);
		launchSimplePayment = PayPal.getInstance().getCheckoutButton(this, PayPal.BUTTON_194x37, CheckoutButton.TEXT_PAY);
		launchSimplePayment.setOnClickListener(this);//new View.OnClickListener(){
		layoutSimplePayment.addView(launchSimplePayment);
		layoutButtons.addView(layoutSimplePayment,0);		
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.info("CreateGameActivity.onActivityResult called, requestCode="+requestCode+
				" resultCode="+resultCode+" intent="+data.toString());
		switch (requestCode) {
		case CREATE_MAP_REQUEST_CODE:
			switch(resultCode) {
			case Activity.RESULT_OK:
			default:
				// Hopefully data is filled with our coordinate info
				double latitude = data.getDoubleExtra(CMConstants.PARM_LATITUDE, 0);
				double longitude = data.getDoubleExtra(CMConstants.PARM_LONGITUDE, 0);
				long range = data.getLongExtra(CMConstants.PARM_RANGE, 0);
				
				editLatitude.setText(Double.toString(latitude));
				editLongitude.setText(Double.toString(longitude));
				editRange.setText(Long.toString(range));
				break;
			}
			break;
		
		case PAYPAL_TRANACTION_REQUEST_CODE:
			switch(resultCode) {
			case Activity.RESULT_OK:
				// Create our game
				String password = null;
				if (cbPrivate.isChecked())
					password = editPassword.getText().toString();

				String locationName = getResources().getString(R.string.Global);
				double latitude = 0;
				double longitude = 0;
				long range = 0;
				if (cbLocation.isChecked()) {
					locationName = editLocation.getText().toString();
					latitude = Double.parseDouble(editLatitude.getText().toString());
					longitude = Double.parseDouble(editLongitude.getText().toString());
					range = Long.parseLong(editRange.getText().toString());
				}

				int ratio = Integer.parseInt((String) spinRatio.getSelectedItem());
				GregorianCalendar startDate = new GregorianCalendar();
				startDate.set(dpStartDate.getYear(), dpStartDate.getMonth(), dpStartDate.getDayOfMonth(), 
						tpStartTime.getCurrentHour(), tpStartTime.getCurrentMinute());
				GregorianCalendar endDate = new GregorianCalendar();
				endDate.set(dpEndDate.getYear(), dpEndDate.getMonth(), dpEndDate.getDayOfMonth(), 
						tpEndTime.getCurrentHour(), tpEndTime.getCurrentMinute());

				final GameBean gb = new GameBean(0, editGameName.getText().toString(), spinType.getSelectedItemPosition(), cbPrivate.isChecked(),
						password, locationName, latitude, longitude, range, ratio, startDate.getTimeInMillis(), endDate.getTimeInMillis());

				//				waitCursor.setMessage(getResources().getString(R.string.Creating_Game));
				//				waitCursor.setIndeterminate(true);
				//				waitCursor.show();
				waitCursor = ProgressDialog.show(this, null, getResources().getString(R.string.Creating_Game), true);//(this);
				Thread createThread = new Thread() {
					public void run() {
						int rc = ServerUtil.createGame(gb);
						waitCursor.dismiss();

						switch(rc) {
						case HttpStatus.SC_OK:
						case 0:
							bRetryNoPayment = false;
							// Success! Let em know - ask em if they want to share this game info
							final DialogInterface.OnClickListener noListener = new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
									CreateGameActivity.this.finish();
								}
							};
							final DialogInterface.OnClickListener yesListener = new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									Intent sendIntent = new Intent(Intent.ACTION_SEND);         
									sendIntent.setType("text/plain");
									String imBody = getResources().getString(R.string.Share_Create_Game, gb.getGameName()+(gb.isPrivate() ? ", "+getResources().getString(R.string.Password_)+gb.getPassword()+"." : "."));
//									imBody += "  "+getResources().getString(R.string.Name_)+gb.getGameName();
//									imBody += "  "+getResources().getString(R.string.Type_)+(gb.getGameType()==1 ? getResources().getString(R.string.Reverse) : getResources().getString(R.string.Normal))+"  ";
//									if (gb.isPrivate())
//										imBody += getResources().getString(R.string.Password_)+gb.getPassword();

									sendIntent.putExtra(Intent.EXTRA_TEXT, imBody); 
									startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.Create_Game)));
									dialog.cancel();
									CreateGameActivity.this.finish();
								}
							};

							handler.post(new Runnable(){
								public void run() {
									AlertDialog dlg = (AlertDialog)getGenericYesNoDialog(getResources().getString(R.string.Create_Game_Success_Title),
											getResources().getString(R.string.Create_Game_Success_Msg), yesListener, noListener);

									dlg.show();
								}
							});
							break;
						case CMConstants.ERR_BAD_PARMS:
							// Very bad!  We need to set a flag and prompt them to try again without charging!
							LogUtil.error("Create game failed with bad parms!");
							bRetryNoPayment = true;
							final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							};
							handler.post(new Runnable(){
								public void run() {
									AlertDialog dlg = getGenericAlertDialog(getResources().getString(R.string.Error), getResources().getString(R.string.Create_Unknown_Error),listener);
									dlg.show();
								}
							});							
							break;
						case CMConstants.ERR_CREATE_GAME_NAME_EXISTS:
							// This is also very bad, we should've checked for this already. Re-prompt just for game name
							LogUtil.error("Create game failed with game name exists!");
							bRetryNoPayment = true;
							final DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							};
							handler.post(new Runnable(){
								public void run() {
									AlertDialog dlg = getGenericAlertDialog(getResources().getString(R.string.Game_Name_Exists), getResources().getString(R.string.Create_Game_Name_Exists_Error),listener1);
									dlg.show();
								}
							});							
							break;
						}

					}

				};
				createThread.start();
				break;

			case PayPalActivity.RESULT_FAILURE:
				String resultInfo = data.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
				String resultExtra = "Error ID: " + data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
				LogUtil.error("Payment failed!  "+resultInfo+" "+resultExtra);
				Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Paypal_Error_Title), 
						getResources().getString(R.string.Paypal_Error), listener);
				dlg.show();
				break;

			default:
			case Activity.RESULT_CANCELED:
				LogUtil.error("Payment canceled");
				Dialog dlg1 = getGenericAlertDialog(getResources().getString(R.string.PayPal_Canceled_Title), 
						getResources().getString(R.string.PayPal_Canceled), listener);
				dlg1.show();
				break;
			}
			break;
			
		default:
			break;
		}
		
		launchSimplePayment.updateButton();
	}

	private boolean validData() {
		if (CMConstants.isGodMode)
			return true;
		// Make sure all our fields are filled in, proper values, etc
		
		// Game name has already been checked
		
		// type and ratio should be ok. Spin controls; can't fuck up your input
		
		// Are we private with no password?
		String strPassword = editPassword.getText().toString();
		if (cbPrivate.isChecked() && (strPassword==null || strPassword.length()==0)) {
			Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Password_Required_Title), 
					getResources().getString(R.string.Password_Required_Message), listener);
			dlg.show();
			editPassword.requestFocus();
			return false;
		}
		
		// Location - make sure there are values?
		if (cbLocation.isChecked()) {
			String locationName = editLocation.getText().toString();
			String strLatitude = editLatitude.getText().toString();
			String strLongitude = editLongitude.getText().toString();
			String strRange = editRange.getText().toString();
			if (locationName==null || locationName.length()==0) {
				Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Location_Name_Title), 
						getResources().getString(R.string.No_Location_Name), listener);
				dlg.show();
				editLocation.requestFocus();
				return false;								
			}
			try {
				Float.parseFloat(strLatitude);
			}
			catch (NumberFormatException e) {
				LogUtil.error("Bad latitude in CreateGame, value: "+strLatitude);
				Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Invalid_Latitude_Title),
						getResources().getString(R.string.Invalid_Latitude), listener);
				dlg.show();
				editLatitude.requestFocus();
				return false;				
			}
			try {
				Float.parseFloat(strLongitude);
			}
			catch (NumberFormatException e) {
				LogUtil.error("Bad longitude in CreateGame, value: "+strLatitude);
				Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Invalid_Longitude_Title),
						getResources().getString(R.string.Invalid_Longitude), listener);
				dlg.show();
				editLongitude.requestFocus();
				return false;				
			}
			try {
				Integer.parseInt(strRange);
			}
			catch (NumberFormatException e) {
				LogUtil.error("Bad range in CreateGame, value: "+strLatitude);
				Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Invalid_Range_Title), 
						getResources().getString(R.string.Invalid_Range), listener);
				dlg.show();
				editRange.requestFocus();
				return false;				
			}
						
		}
		
		// start and end date and time
		long currentTime = System.currentTimeMillis();
		GregorianCalendar startDate = new GregorianCalendar();
		startDate.set(dpStartDate.getYear(), dpStartDate.getMonth(), dpStartDate.getDayOfMonth(), 
				tpStartTime.getCurrentHour(), tpStartTime.getCurrentMinute());
		GregorianCalendar endDate = new GregorianCalendar();
		endDate.set(dpEndDate.getYear(), dpEndDate.getMonth(), dpEndDate.getDayOfMonth(), 
				tpEndTime.getCurrentHour(), tpEndTime.getCurrentMinute());
		
		// First, check for stupid things like endDate before startDate, or they're equal
		if (endDate.getTimeInMillis() - startDate.getTimeInMillis() <= 0) {
			// assholes
			Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Bad_Kitty), 
					getResources().getString(R.string.End_Time_Before_Start_Time), listener);
			dlg.show();
			dpEndDate.requestFocus();
			return false;
		}
		
		// How long did they set the game for?  Limit is 1 YEAR!
		long oneYear = CMConstants.TIME_MILLISECONDS_DAY*365; 
		if (endDate.getTimeInMillis() - startDate.getTimeInMillis() > oneYear) {
			Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.One_Year_Max_Title), 
					getResources().getString(R.string.One_Year_Max), listener);
			dlg.show();
			dpEndDate.requestFocus();
			return false;			
		}
		
		// Don't let them create a game more than one month out
		long oneMonth = CMConstants.TIME_MILLISECONDS_DAY*30;
		if (startDate.getTimeInMillis() - currentTime > oneMonth) {
			Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Start_Date_Too_Far_Title), 
					getResources().getString(R.string.Start_Date_Too_Far), listener);
			dlg.show();
			dpStartDate.requestFocus();
			return false;			
			
		}
		
		return true;

	}

	public void onClick(View v) {
		// Check the game name
		final String strGameName = editGameName.getText().toString();
		if (strGameName==null || strGameName.length()==0) {
			Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Invalid_Game_Name_Title), 
					getResources().getString(R.string.Game_Name_Not_Null), listener);
			dlg.show();
			launchSimplePayment.updateButton();			
			editGameName.requestFocus();
			return;
		}
		
		// Check the game name against the server
		waitCursor = ProgressDialog.show(this, null, getResources().getString(R.string.Checking_Game_Name), true);//(this);
		Thread createThread = new Thread() {
			public void run() {
				int rc = ServerUtil.checkGameName(strGameName);
				waitCursor.dismiss();
				LogUtil.info("Check game returned "+rc);
				switch (rc) {
				case HttpStatus.SC_OK:
				case 0:
					// All good, proceed
					handler.post(new Runnable(){

						@Override
						public void run() {
							// field validation, paypal, call create game, shitload!
							if (!validData()) {
								launchSimplePayment.updateButton();
								return;
							}
							else {
								// Check in case of error we don't launch paypal
								if (bRetryNoPayment || CMConstants.isGodMode) {
									bRetryNoPayment = false;
									onActivityResult(PAYPAL_TRANACTION_REQUEST_CODE, Activity.RESULT_OK, new Intent());
									return;
								}
								// Time to charge it to paypal
								// First, how long is their game?
								GregorianCalendar startDate = new GregorianCalendar();
								startDate.set(dpStartDate.getYear(), dpStartDate.getMonth(), dpStartDate.getDayOfMonth(), 
										tpStartTime.getCurrentHour(), tpStartTime.getCurrentMinute());
								GregorianCalendar endDate = new GregorianCalendar();
								endDate.set(dpEndDate.getYear(), dpEndDate.getMonth(), dpEndDate.getDayOfMonth(), 
										tpEndTime.getCurrentHour(), tpEndTime.getCurrentMinute());
								long gameLength = endDate.getTimeInMillis() - startDate.getTimeInMillis();
								long gameDays = gameLength / CMConstants.TIME_MILLISECONDS_DAY + (gameLength % CMConstants.TIME_MILLISECONDS_DAY > CMConstants.TIME_MILLISECONDS_HOUR ? 1 : 0);
								double gameCost = gameDays * (((CMApplication)getApplicationContext()).isPro() ? CMConstants.GAME_COST_PRO : CMConstants.GAME_COST_FREE);
								
								PayPalPayment payment = new PayPalPayment();
								payment.setSubtotal(new BigDecimal(gameCost));
								payment.setCurrencyType("USD");
								payment.setRecipient(CMConstants.PAYPAL_SELLER_EMAIL);
								payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);
								payment.setMerchantName(getResources().getString(R.string.PayPal_Merchant_Name));
								//payment.setDescription("Payment for game named "+editGameName.getText().toString());
								//payment.setCustomID(CMConstants.PAYPAL_CUSTOM_ID);
								payment.setMemo(getResources().getString(R.string.PayPal_Memo)+" "+editGameName.getText().toString());
								Intent checkoutIntent = PayPal.getInstance().checkout(payment, CreateGameActivity.this);
								startActivityForResult(checkoutIntent, PAYPAL_TRANACTION_REQUEST_CODE); 				
								
								// PayPal is being a fuckin cocksucker - bypass for now
								//onActivityResult(1, Activity.RESULT_OK, new Intent());																
							}
						}
						
					});
					break;
				case CMConstants.ERR_CREATE_GAME_NAME_EXISTS:
				default:
					// Bummer dude, give an error message
					handler.post(new Runnable(){

						@Override
						public void run() {
							Dialog dlg = getGenericAlertDialog(getResources().getString(R.string.Game_Name_Exists), 
									getResources().getString(R.string.Game_Name_Exists_Error), listener);
							dlg.show();
							launchSimplePayment.updateButton();							
							editGameName.requestFocus();
						}
						
					});
					break;
				
				}
			}
		};
		createThread.start();
	}
	
//	private void initLibrary(boolean bForceInit) {
//		// Paypal setup
//		PayPal pp = PayPal.getInstance();
//		if (pp==null || bForceInit) {
//			try {
//				pp = PayPal.initWithAppID(this, "APP-80W284485P519543T", CMConstants.PAYPAL_ENV);
//				pp.setLanguage("en_US"); // Sets the language for the library.
//			}
//			catch (Exception e) {
//				LogUtil.error("PayPal failed to initialize; error="+e.toString());
//			}
//		}
//	}		
//	
//	private void initLibrary() {
//		boolean bInitialize = true;
//		PayPal pp = PayPal.getInstance();
//		if (pp!=null){
//			if (!pp.isLibraryInitialized())
//				pp.deinitialize();
//			else
//				bInitialize = false;
//		}
//			
//		// If the library is already initialized, then we don't need to initialize it again.
//		if(bInitialize) {
//			// This is the main initialization call that takes in your Context, the Application ID, and the server you would like to connect to.
//			pp = PayPal.initWithAppID(this, appID, server);
//			
//			// -- These are required settings.
//        	pp.setLanguage("en_US"); // Sets the language for the library.
//        	// --
//        	
//        	// -- These are a few of the optional settings.
//        	// Sets the fees payer. If there are fees for the transaction, this person will pay for them. Possible values are FEEPAYER_SENDER,
//        	// FEEPAYER_PRIMARYRECEIVER, FEEPAYER_EACHRECEIVER, and FEEPAYER_SECONDARYONLY.
//        	pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER); 
//        	// Set to true if the transaction will require shipping.
//        	pp.setShippingEnabled(true);
//        	// Dynamic Amount Calculation allows you to set tax and shipping amounts based on the user's shipping address. Shipping must be
//        	// enabled for Dynamic Amount Calculation. This also requires you to create a class that implements PaymentAdjuster and Serializable.
//        	pp.setDynamicAmountCalculationEnabled(false);
//        	// --
//        	if (pp.isLibraryInitialized())
//        		LogUtil.info("PayPal initialized in initLibrary()");
//        	else
//        		LogUtil.error("PayPal not initialized in initLibrary()");
//		}
//	}	

}
