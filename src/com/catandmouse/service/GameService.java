package com.catandmouse.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.catandmouse.CMApplication;
import com.catandmouse.CMConstants.AchievementTypeEnums;
import com.catandmouse.CMIntentActions;
import com.catandmouse.DataHelper;
import com.catandmouse.Game;
import com.catandmouse.LogUtil;
import com.catandmouse.CMNotification;
import com.catandmouse.CMConstants;

import com.openfeint.api.resource.Achievement;
import com.openfeint.api.resource.User;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @author Brian Emond
 * Handle all scoring, achievements and db management
 *
 */
public class GameService extends Service {

	Map<Integer, Timer> mapMouseTimers = new HashMap<Integer, Timer>();
	Map<Integer, Integer> mapTomCheck = new HashMap<Integer, Integer>();
	Timer syncTimer = null;
	
	public void onCreate() {
		// register for actions
        // Cat and Mouse Actions
		for (int i=0; i < CMIntentActions.CM_ACTIONS.length; i++) {
			IntentFilter filter = new IntentFilter(CMIntentActions.CM_ACTIONS[i]);
			this.registerReceiver(mReceiver, filter);
		}    	
		
	}

	public void onDestroy() {
		// Remove all games from db
		((CMApplication)getApplicationContext()).dh.deleteAll();
		
		// delete all timers
		for (int i=0; i < mapMouseTimers.size(); i++) {
			Timer timer  = mapMouseTimers.get(i);
			if (timer!=null)
				timer.cancel();
		}
		mapMouseTimers.clear();
		if (syncTimer!=null)
			syncTimer.cancel();
		
		this.unregisterReceiver(mReceiver);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.info("GameService started");
		// Intent contains game number. If I just started a normal game, start my mouse timer
		if (intent!=null) {
			int gameNumber = intent.getIntExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, 0);
			Game game = ((CMApplication)getApplicationContext()).dh.selectGame(gameNumber);
			if (game!=null) {
				if (game.getGameType()==CMConstants.GAME_TYPE_NORMAL) {
					// create mouse timer if it doesn't exist
					if (!mapMouseTimers.containsKey(gameNumber)) {
						Timer timer = new Timer();
						timer.scheduleAtFixedRate(new MouseTimerTask(gameNumber,CMConstants.GAME_TYPE_NORMAL), CMConstants.TIME_MILLISECONDS_1_MIN, CMConstants.TIME_MILLISECONDS_1_MIN);
						mapMouseTimers.put(gameNumber, timer);						
					}
				}
			}
		}
		
		// start sync timer
		if (syncTimer==null) {
			syncTimer = new Timer();
			syncTimer.scheduleAtFixedRate(new TimerTask(){

				@Override
				public void run() {
					LogUtil.info("Processing Game Sync Check");
					((CMApplication)getApplicationContext()).gameLoginCheck();
				}
				
			}, CMConstants.TIME_MILLISECONDS_1_MIN*2, CMConstants.TIME_MILLISECONDS_1_MIN*2);
		}
		
		return START_STICKY;
	}
	
	private class MouseTimerTask extends TimerTask {
		private int gameNumber;
		private int gameType;
		private int mouseRounds = 0;
		
		@Override
		public boolean cancel() {
			LogUtil.info("GameService MouseTimerTask cancel()");
			return super.cancel();
		}
		
		public MouseTimerTask(int gameNumber, int gameType) {
			LogUtil.info("Game Service creating mouse timer task()");
			this.gameNumber = gameNumber;
			this.gameType = gameType;
		}
		public void run() {
			// Check the database for the lastCaughtTime. If its greater than 5 minutes
			// I get a point
			LogUtil.info("GameService processing MouseTimerTask for the "+mouseRounds+" time");
			if (gameType==CMConstants.GAME_TYPE_NORMAL || (gameType==CMConstants.GAME_TYPE_REVERSE && mouseRounds < 5)) {
				Game game = ((CMApplication)getApplicationContext()).dh.selectGame(gameNumber);
				if (game!=null) {
					// am I still a mouse?
					if (!game.isCat()) {
						if (System.currentTimeMillis()-game.getLastCaughtTime() > CMConstants.TIME_MILLISECONDS_5_MIN) {
							// Yeah, I get a point(s) based on game type. Update score.
							int score = game.getGameType()==CMConstants.GAME_TYPE_NORMAL ? 
									CMConstants.MOUSE_POINT_NORMAL_GAME : CMConstants.MOUSE_POINT_REVERSE_GAME * game.getRatio();
							((CMApplication)getApplicationContext()).updateScore(gameNumber, score);

							// Achievement - Mighty Mouse and Jerry
							updateAchievementCount(AchievementTypeEnums.JERRY, score);
							updateAchievementCount(AchievementTypeEnums.MIGHTY_MOUSE, score);
						}
						else {
							LogUtil.info("GameService mouse timer no point since you were caught in last 5 minutes");
						}
					}
					else {
						// I'm a cat, cancel this timer
						LogUtil.debug("Removing a mouse timer because I'm a cat in game "+game.getGameName());
						this.cancel();
						mapMouseTimers.remove(gameNumber);
					}
				}
				else {
					// If I'm here, I shouldn't be processing!
					this.cancel();
					mapMouseTimers.remove(gameNumber);
				}
				mouseRounds++;
			}
			else {
				// Should be a reverse game that we've processed more than 5 rounds
				LogUtil.warn("Processed more than 5 rounds in a reverse game, no more points for you!");
			}
		}
	}
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();
			if (action!=null) {
				LogUtil.info("GameService processing broadcast: "+action);
				if (CMIntentActions.ACTION_PLAYER_CAUGHT.equals(action)) {
					// Did I do the catching?
					User user = ((CMApplication)getApplicationContext()).user;
					if (user!=null && user.name!=null && user.name.length()>0) {
						Bundle bundle = intent.getExtras();
						CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
						if (n!=null && user.name.equals(n.getCatPlayerName())) {
							// hot damn it is me!  Update my score
							// Get the game
							Game game = ((CMApplication)getApplicationContext()).dh.selectGame(n.getGameNumber());
							if (game!=null) {
								((CMApplication)getApplicationContext()).updateScore(n.getGameNumber(), n.getGameType()==CMConstants.GAME_TYPE_NORMAL ? 
										CMConstants.CAT_POINT_NORMAL_GAME * game.getRatio() : CMConstants.CAT_POINT_REVERSE_GAME);
								// Achievements - First kill and King of jungle
								updateAchievementCount(AchievementTypeEnums.FIRST_KILL, 1);
								updateAchievementCount(AchievementTypeEnums.KING_OF_JUNGLE, 1);
								// update the tom checker
								if (game.getGameType()==CMConstants.GAME_TYPE_NORMAL)
									mapTomCheck.put(n.getGameNumber(), 1);
							}
							else {
								// shit, not in this game anymore?
								LogUtil.error("Received action caught for game I'm not in, no score update");
							}
						}
						else if (n!=null && user.name.equals(n.getMousePlayerName())) {
							// Well shit, I got caught!  Gotta update my lastCaughtTime
							ContentValues args = new ContentValues();
							args.put(DataHelper.COLUMN_LAST_CAUGHT_TIME, n.getActivityTime());
							((CMApplication)getApplicationContext()).dh.update(n.getGameNumber(), args);
							// Achievement - First death
							updateAchievementCount(AchievementTypeEnums.FIRST_DEATH, 1);
						}
					}
					else {
						LogUtil.error("Bad user data");
						((CMApplication)getApplicationContext()).loadUser();

					}
				}
				else if (CMIntentActions.ACTION_STATE_CAT.equals(action)) {
					// If its me, gotta destroy my mouse timer
					User user = ((CMApplication)getApplicationContext()).user;
					if (user!=null && user.name!=null && user.name.length()>0) {
						Bundle bundle = intent.getExtras();
						CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
						if (n!=null) {
							if (user.name.equals(n.getCatPlayerName())) {
								// Great Odins Raven it is me!  Kill the mouse timer for this game
								Timer timer = mapMouseTimers.get(n.getGameNumber());
								if (timer!=null) {
									timer.cancel();
								}
								mapMouseTimers.remove(n.getGameNumber());
								// Achievement prep - put an entry into tom check
								if (n.getGameType()==CMConstants.GAME_TYPE_NORMAL)
									mapTomCheck.put(n.getGameNumber(), 0);
							}
							else {
								LogUtil.info("GameSerice cat is not me. User="+user.name+", cat="+n.getCatPlayerName());
							}
						}
					}	
					else {
						LogUtil.error("User is null!");
						((CMApplication)getApplicationContext()).loadUser();
					}

				}
				else if (CMIntentActions.ACTION_STATE_MOUSE.equals(action)) {
					// If its me, gotta create my mouse timer
					User user = ((CMApplication)getApplicationContext()).user;
					if (user!=null && user.name!=null && user.name.length()>0) {
						Bundle bundle = intent.getExtras();
						CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
						if (n!=null) {
							if (user.name.equals(n.getMousePlayerName())) {
								// By Zeus' Beard it is me!  Create the mouse timer for this game
								if (!mapMouseTimers.containsKey(n.getGameNumber())) {
									Timer timer = new Timer();
									timer.scheduleAtFixedRate(new MouseTimerTask(n.getGameNumber(),n.getGameType()), CMConstants.TIME_MILLISECONDS_1_MIN, CMConstants.TIME_MILLISECONDS_1_MIN);
									mapMouseTimers.put(n.getGameNumber(), timer);
									// Achievement - was I a cat in a normal game and didn't get a mouse?
									Integer i = mapTomCheck.get(n.getGameNumber());
									if (i!=null && i==0) {
										// Achievement - Tom
										updateAchievementCount(AchievementTypeEnums.TOM, 1);
										mapTomCheck.remove(n.getGameNumber());
									}
								}
								else {
									LogUtil.info("GameService not creating mouse timer cause it already exists");
								}
							}
							else {
								LogUtil.info("GameService mouse is not me, user="+user.name+", mouse="+n.getMousePlayerName());
							}
						}
						else {
							LogUtil.info("GameService notification is null");
						}
					}	
					else {
						LogUtil.error("User is null!");
						((CMApplication)getApplicationContext()).loadUser();
					}

				}
				else if (CMIntentActions.ACTION_GAME_OVER.equals(action)) {
					// Game ended. Delete it from the db
					Bundle bundle = intent.getExtras();
					CMNotification n = bundle.getParcelable(CMConstants.NOTIFICATION_PACKAGE);
					if (n!=null)
						((CMApplication)getApplicationContext()).dh.delete(n.getGameNumber());

					// Mouse Timer?
					Timer timer = mapMouseTimers.get(n.getGameNumber());
					if (timer!=null) {
						timer.cancel();
					}
					mapMouseTimers.remove(n.getGameNumber());
				}
				else if (CMIntentActions.ACTION_LOGOUT.equals(action)) {
					// delete all the games
					((CMApplication)getApplicationContext()).dh.deleteAll();

					// delete any mouse timers
					for (int i=0; i < mapMouseTimers.size(); i++) {
						Timer timer = mapMouseTimers.get(i);
						if (timer!=null)
							timer.cancel();
					}
					mapMouseTimers.clear();
					if (syncTimer!=null) {
						syncTimer.cancel();
						syncTimer = null;
					}
					stopSelf();
				}
				else if (CMIntentActions.ACTION_QUIT_GAME.equals(action)) {
					// delete this game from the db
					int gameNumber = intent.getIntExtra(CMConstants.GAME_PACKAGE+CMConstants.PARM_GAME_NUMBER, 0);
					((CMApplication)getApplicationContext()).dh.delete(gameNumber);

					// If there's a mouse timer task for this game, get rid of it too
					Timer timer = mapMouseTimers.get(gameNumber);
					if (timer!=null) {
						timer.cancel();
					}
					mapMouseTimers.remove(gameNumber);

					// if we're not in any other games, cancel the sync timer
					if (((CMApplication)getApplicationContext()).dh.selectAll().size()==0){
						if (syncTimer!=null) {
							syncTimer.cancel();
							syncTimer = null;
						}
						stopSelf();
					}
				}
			}
		}
    };	

	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void updateAchievementCount(AchievementTypeEnums enumAchievement, int count) {
		SharedPreferences settings = this.getSharedPreferences(CMConstants.ACHIEVEMENTS, MODE_PRIVATE);
		// Only do something with this if it's not completed
		if (!settings.getBoolean(enumAchievement.getPrefValue()+CMConstants.COMPLETED, false)) {
			int iCount = settings.getInt(enumAchievement.getPrefValue(), 0);
			if (count==0)
				iCount = 0;
			else
				iCount += count;

			Editor edit = settings.edit();
			edit.putInt(enumAchievement.getPrefValue(), iCount);
			
			// Update it on feint, unless it's already complete
			switch(enumAchievement) {
			case FIRST_KILL:
			case FIRST_DEATH:
				// one-timer, update it and mark complete
				edit.putBoolean(enumAchievement.getPrefValue()+CMConstants.COMPLETED, true);
				updateAchievement(enumAchievement, 100);
				break;
			case KING_OF_JUNGLE:
			case MIGHTY_MOUSE:
			case TOM:
			case JERRY:
				// get our current value
				int currentCount = settings.getInt(enumAchievement.getPrefValue(), 0);
				currentCount += count;
				
				// get our achievement specific total count
				int iTotalCount = 0;
				switch (enumAchievement) {
				case KING_OF_JUNGLE:
					iTotalCount = CMConstants.ACHIEVEMENT_KING_JUNGLE_COUNT;
					break;
				case MIGHTY_MOUSE:
					iTotalCount = CMConstants.ACHIEVEMENT_MIGHTY_MOUSE_COUNT;
					break;
				case TOM:
					iTotalCount = CMConstants.ACHIEVEMENT_TOM_COUNT;
					break;
				case JERRY:
					iTotalCount = CMConstants.ACHIEVEMENT_JERRY_COUNT;
					break;
				}
				
				if (iTotalCount > 0) {// safeguard
					// are we over the amount?
					if (currentCount >= iTotalCount) {
						// mark it completed
						edit.putBoolean(enumAchievement.getPrefValue()+CMConstants.COMPLETED, true);
					}
					//update feint
					updateAchievement(enumAchievement, ((float)((float)currentCount / (float)iTotalCount)) * 100);
				}
				break;
			}
			
			edit.commit();
		}
	}
	
	public void updateAchievement(final AchievementTypeEnums enumAchievement, final float pctComplete) {
		Thread myThread = new Thread(){

			@Override
			public void run() {
				// Update the achievement
				Achievement.list(new Achievement.ListCB() {
					
					@Override
					public void onSuccess(List<Achievement> achievements) {
						for (final Achievement a : achievements) {
							if (a.title.equals(enumAchievement.getFeintValue())) {
								// found it. Unlock if it's 100%
								if (pctComplete>=100) {
									// unlock it
									a.unlock(new Achievement.UnlockCB() {
										
										@Override
										public void onSuccess(boolean newUnlock) {
											LogUtil.info("Successfully unlocked achievement: "+enumAchievement.getFeintValue());
										}
									});
								}
								else {
									a.updateProgression(pctComplete, null);
								}
							}
						}
					}
				});
			}
			
		};
		
		myThread.start();
	}

}
