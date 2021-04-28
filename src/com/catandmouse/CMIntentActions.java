package com.catandmouse;

import java.util.HashMap;
import java.util.Map;

public class CMIntentActions {
	public static final String ACTION_PLAYER_CAUGHT="com.catandmouse.action.PLAYER_CAUGHT";
	public static final String ACTION_STATE_CAT="com.catandmouse.action.STATE_CAT";
	public static final String ACTION_STATE_MOUSE="com.catandmouse.action.STATE_MOUSE";
	public static final String ACTION_GAME_OVER="com.catandmouse.action.GAME_OVER";
	public static final String ACTION_LOGOUT="com.catandmouse.action.LOGOUT";
	public static final String ACTION_JOIN_GAME="com.catandmouse.action.JOIN_GAME";
	public static final String ACTION_QUIT_GAME="com.catandmouse.action.QUIT_GAME";
	public static final String ACTION_CHECKED_GAMES="com.catandmouse.action.CHECKED_GAMES";
	public static final String ACTION_PRO_CHECK="com.catandmouse.action.PRO_CHECK";
	
	public static final String ACTION_SCORE_UPDATE="com.catandmouse.action.SCORE_UPDATE"; // Not adding this to the list purposely
	
	public static final String [] CM_ACTIONS = {ACTION_PLAYER_CAUGHT,ACTION_STATE_CAT,ACTION_STATE_MOUSE,
		ACTION_GAME_OVER,ACTION_LOGOUT,ACTION_JOIN_GAME,ACTION_QUIT_GAME};
	
	public static final Map<Integer, String> mapActionsToNotifications = new HashMap<Integer, String>();
    static {
    	mapActionsToNotifications.put(CMConstants.NOTIFICATION_PLAYER_CAUGHT, CMIntentActions.ACTION_PLAYER_CAUGHT);
    	mapActionsToNotifications.put(CMConstants.NOTIFICATION_STATE_CAT, CMIntentActions.ACTION_STATE_CAT);
    	mapActionsToNotifications.put(CMConstants.NOTIFICATION_STATE_MOUSE, CMIntentActions.ACTION_STATE_MOUSE);
    	mapActionsToNotifications.put(CMConstants.NOTIFICATION_GAME_OVER, CMIntentActions.ACTION_GAME_OVER);
    	mapActionsToNotifications.put(CMConstants.NOTIFICATION_INACTIVTY_LOGOUT, CMIntentActions.ACTION_LOGOUT);
    	mapActionsToNotifications.put(CMConstants.NOTIFICATION_JOIN_GAME, CMIntentActions.ACTION_JOIN_GAME);
    	mapActionsToNotifications.put(CMConstants.NOTIFICATION_QUIT_GAME, CMIntentActions.ACTION_QUIT_GAME);
    }


}
