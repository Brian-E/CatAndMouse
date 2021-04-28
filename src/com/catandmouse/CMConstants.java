package com.catandmouse;

import com.paypal.android.MEP.PayPal;

//import com.paypal.android.MEP.PayPal;


public class CMConstants {
	// TODO: These are critical values to change at release time - DO NOT FORGET!!!
	
	// FIRST THINGS FIRST - GO CHANGE THE VALUE OF MAPS_API_KEY IN STRINGS.XML
    public static final boolean isTestMode=true; // should be false
    public static final boolean isGodMode=true; // should be false

    public static final String PAYPAL_SELLER_EMAIL = isTestMode ? "briane_1314913505_biz@gmail.com" : "payment@catandmouseapp.com";
	//public static final String PAYPAL_MERCHANT_NAME="Cat and Mouse Game Purchase";
	//public static final String PAYPAL_CUSTOM_ID="12345"; 
	public static final int PAYPAL_ENV = isTestMode ? PayPal.ENV_SANDBOX : PayPal.ENV_LIVE;
	public static final String appID = isTestMode ? "APP-80W284485P519543T" : "APP-45X30225P7535534S";
    // Google Maps API Key
    public static final String GOOGLE_MAPS_API_KEY=isTestMode ? "0gQUVYX8-2mywRBu1oWVkyYv2-lRSVfqeQCT3RA" : "0gQUVYX8-2mywEMR6RrdoCv8CDoFCFShIp6gLeg";
    // AdMob Publisher Id
    public static final String ADMOB_PUBLISHER_ID="a14de2708050dca";
    
    // Facebook
    public static final String FACEBOOK_ID="225123684207235";
    public static final String FACEBOOK_SECRET="6c940dcfa3b9cdfa1497342d7bf5067e";
	////////////////////////////////////////////////////////////////////////////////////////
    // Contact info
    public static final String EMAIL_SUPPORT="support@catandmouseapp.com";
	
	// Misc
	public static final String PACKAGE="com.catandmouse.";
	public static final String PRO_PACKAGE="com.catandmousepro";
	public static final String PACKAGE_NO_PERIOD="com.catandmouse";
	public static final String NOTIFICATION_PACKAGE="com.catandmouse.notification";
	public static final String GAME_PACKAGE="com.catandmouse.game";
	
	public static final int MOUSE_POINT_NORMAL_GAME=1;
	public static final int MOUSE_POINT_REVERSE_GAME=5;
	public static final int CAT_POINT_NORMAL_GAME=5;
	public static final int CAT_POINT_REVERSE_GAME=5;
		
	public static final String ALL="ALL";
	
	public static final double GAME_COST_FREE=2.99;
	public static final double GAME_COST_PRO=0.99;

    public static final int REQUEST_ENABLE_BT = 2;
    
	// Errors
	
	// General -1 to -10
	public static final int ERR_MISSING_PARMS = 601;
	public static final int ERR_BAD_PARMS = 602;
	public static final int ERR_NO_LOCATION_DATA = 603;
	public static final int ERR_INVALID_GAME_NUMBER = 604;
	public static final int ERR_INVALID_PLAYER_ID = 605;
	public static final int ERR_BAD_MAC_ADDR = 606;
	public static final int ERR_MULTIPLE_MAC_ADDR = 607;
	public static final int ERR_PLAYER_NOT_FOUND = 608;
	
	// Class specific -11 to ....
	public static final int ERR_LOGIN_EXISTING_DIFF_MAC_ADDR = 611;
	public static final int ERR_JOIN_GAME_PLAYER_NOT_LOGGED_IN = 612;
	public static final int ERR_JOIN_GAME_NO_PASSWORD = 614;
	public static final int ERR_JOIN_GAME_BAD_PASSWORD = 615;
	public static final int ERR_JOIN_GAME_TOO_EARLY = 616;
	public static final int ERR_JOIN_GAME_TOO_LATE = 617;
	public static final int ERR_JOIN_GAME_OUTSIDE_PERIMETER = 618;
	public static final int ERR_CREATE_GAME_NAME_EXISTS = 619;
	
	public static final String PARM_PLAYER_ID="playerId";
	public static final String PARM_PLAYER_NAME="playerName";
	public static final String PARM_MAC_ADDR="macAddress";
	public static final String PARM_GAME_NUMBER="gameNumber";
	public static final String PARM_GAME_NAME="gameName";	
	public static final String PARM_PASSWORD="password";
	public static final String PARM_LATITUDE="latitude";
	public static final String PARM_LONGITUDE="longitude";
	public static final String PARM_RANGE="range";
	public static final String PARM_GAME_PLAYER_ID="gamePlayerId";
	public static final String PARM_LAST_UPDATE_TIME="lastUpdateTime";
	public static final String PARM_GAME_TYPE="gameType";
	public static final String PARM_CAT_PLAYER_NAME="catPlayerName";
	public static final String PARM_MOUSE_PLAYER_NAME="mousePlayerName";
	public static final String PARM_INTENDED_PLAYER_ID="intendedPlayerId";
	public static final String PARM_GAME_BEAN="gameBean";
	public static final String PARM_NOTIFICATION_BEAN="notificationBean";
	public static final String PARM_CENTER_LATITUDE="centerLatitude";
	public static final String PARM_CENTER_LONGITUDE="centerLongitude";
	public static final String PARM_CORNER_LATITUDE="cornerLatitude";
	public static final String PARM_CORNER_LONGITUDE="cornerLongitude";
	public static final String PARM_FEINT_SCORE="feintScore";
	
	public static final String PARM_SCORE="score";
	public static final String PARM_IS_PRO="isPro";
	
	public static final long TIME_MILLISECONDS_5_MIN=300000;
	public static final long TIME_MILLISECONDS_30_SEC=30000;
	public static final long TIME_MILLISECONDS_1_MIN=60000;
	public static final long TIME_MILLISECONDS_HOUR=TIME_MILLISECONDS_1_MIN*60;
	public static final long TIME_MILLISECONDS_DAY=TIME_MILLISECONDS_HOUR*24;
	public static final long TIME_PLAYER_INACTIVITY_MILLISECONDS=TIME_MILLISECONDS_5_MIN;
	
	// Notifications
	public static int NOTIFICATION_PLAYER_CAUGHT = 0;
	public static int NOTIFICATION_STATE_CAT = 1;
	public static int NOTIFICATION_STATE_MOUSE = 2;
	public static int NOTIFICATION_GAME_OVER = 3;
	public static int NOTIFICATION_INACTIVTY_LOGOUT = 4;
	public static int NOTIFICATION_JOIN_GAME = 5;
	public static int NOTIFICATION_QUIT_GAME = 6;
	public static int NOTIFICATION_CAUGHT_MOUSE = 7;
	
	// Game types
	public static final int GAME_TYPE_NORMAL = 0;
	public static final int GAME_TYPE_REVERSE = 1;
	
	// Settings
	public static final String SETTINGS="Settings";
	//public static final String SETTING_GENERAL_USE_GPS="UseGPS";
	public static final String SETTING_GENERAL_CAUGHT_NOTIFICATION="NotificationWhenCaught";
	public static final String SETTING_GENERAL_CAUGHT_SOUND="PlaySoundWhenCaught";
	public static final String SETTING_GENERAL_CAUGHT_SOUND_FILE="SoundWhenCaughtFile";
	public static final String SETTING_GENERAL_CAUGHT_VIBRATE="VibrateWhenCaught";
	// Removed the below option yet again - this can't be automated, it is an activity that requires a users action to launch it; otherwise we always lose focus from 
	// our current activity and if they chose not to be discoverable we'll launch the same activity again
//	public static final String SETTING_GENERAL_AUTO_DISCOVER="AutoPromptDiscoverable"; // handled in BluetoothService - no its not, not using. Yes, I am using again!
	public static final String SETTING_GENERAL_CATCH_MOUSE_NOTIFICATION="CaughtMouseNotification";
	public static final String SETTING_GENERAL_CATCH_MOUSE_VIRBRATE="CaughtMouseVibrate";
	public static final String SETTING_GENERAL_SOUNDS_OFF="TurnOffSounds";
	public static final String SETTING_GENERAL_SOUNDS_OFF_START_TIME_HOUR="TurnOffSoundsStartTimeHour";
	public static final String SETTING_GENERAL_SOUNDS_OFF_START_TIME_MIN="TurnOffSoundsStartTimeMin";
	public static final String SETTING_GENERAL_SOUNDS_OFF_END_TIME_HOUR="TurnOffSoundsEndTimeHour";
	public static final String SETTING_GENERAL_SOUNDS_OFF_END_TIME_MIN="TurnOffSoundsEndTimeMin";
	
	public static final String SETTING_NORMAL_CAT_NOTIFICATION="NotificationWhenCatNormal";
	public static final String SETTING_NORMAL_CAT_SOUND="PlaySoundWhenCatNormal";
	public static final String SETTING_NORMAL_CAT_SOUND_FILE="SoundWhenCatNormalFile";
	public static final String SETTING_NORMAL_CAT_VIBRATE="VibrateWhenCatNormal";
	
	public static final String SETTING_REVERSE_MOUSE_NOTIFICATION="NotificationWhenMouseReverse";
	public static final String SETTING_REVERSE_MOUSE_SOUND="PlaySoundWhenMouseReverse";
	public static final String SETTING_REVERSE_MOUSE_SOUND_FILE="SoundWhenMouseReverseFile";
	public static final String SETTING_REVERSE_MOUSE_VIBRATE="VibrateWhenMouseReverse";
	
	// PUBNUB
	public static final String PUBNUB_PUBLISH_KEY="pub-5901b2b5-5dd6-48bb-986d-ee604931f07a";
	public static final String PUBNUB_SUBSCRIBE_KEY="sub-903f254c-920c-11e0-8fea-29df83201866";
	public static final String PUBNUB_SECRET_KEY="sec-efd833df-5f86-4af1-9c4d-2170f8273996";
	
	// Achievements
	public static final String COMPLETED="_completed";
	public static final String ACHIEVEMENTS="Achievements";	
	public static final String SETTING_ACHIEVEMENT_KILL_COUNT="AchievementKillCount";
	public static final String SETTING_ACHIEVEMENT_MOUSE_COUNT="AchievementMouseCount";
	public static final int ACHIEVEMENT_KING_JUNGLE_COUNT=20;
	public static final int ACHIEVEMENT_MIGHTY_MOUSE_COUNT=2000;
	public static final int ACHIEVEMENT_TOM_COUNT=10;
	public static final int ACHIEVEMENT_JERRY_COUNT=200;
	
	public enum AchievementTypeEnums {
		FIRST_KILL(0, "First_Kill", "Look What The Cat Dragged In"),
		FIRST_DEATH(1, "First_Death", "Dead Mouse"),
		KING_OF_JUNGLE(2, "King_Of_Jungle", "King Of The Jungle"),
		MIGHTY_MOUSE(3, "Mighty_Mouse", "Mighty Mouse"),
		TOM(4, "Tom", "Tom"),
		JERRY(5, "Jerry", "Jerry"),
		;
		
		private int enumValue;		
		private String prefValue;
		private String feintValue;
		
		AchievementTypeEnums(int enumValue, String prefValue, String feintValue) {
			this.enumValue = enumValue;
			this.prefValue = prefValue;
			this.feintValue = feintValue;
		}

		public int getEnumValue() {
			return enumValue;
		}

		public String getPrefValue() {
			return prefValue;
		}

		public String getFeintValue() {
			return feintValue;
		}
		
	}
}
