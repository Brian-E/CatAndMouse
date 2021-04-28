package com.catandmouse.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.catandmouse.CMApplication;
import com.catandmouse.CMConstants;
import com.catandmouse.Game;
import com.catandmouse.LogUtil;

import com.openfeint.api.resource.Leaderboard;
import com.openfeint.api.resource.Score;
import com.openfeint.api.resource.User;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Brian Emond
 *
 */
public class ScoreService extends Service {
	private Timer scoreTimer;

	public void onCreate() {
		scoreTimer = new Timer();
		scoreTimer.scheduleAtFixedRate(new ScoreTimerTask(), CMConstants.TIME_MILLISECONDS_1_MIN*2, CMConstants.TIME_MILLISECONDS_1_MIN*2); // Every 2 minutes
	}

	public void onDestroy() {
		scoreTimer.cancel();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	private class ScoreTimerTask extends TimerTask {

		public void run() {
			ScoreAsyncTask task = new ScoreAsyncTask();
			task.start();
		}
	}
	
	private class ScoreAsyncTask extends Thread {

		public void run() {
			updateFeintScores();
		} 
		
	}	
	
	/**
	 * Go through all games in the database and update the scores on Feint
	 *  If successful, reset the local score to 0 
	 */
	private void updateFeintScores() {
		LogUtil.info("ScoreService updating scores");
		// Get all games in the db
		List<Game> gameList = ((CMApplication)getApplicationContext()).dh.selectAll();
		if (gameList.size() > 0) {
			// First, let's see if we're logged into Feint
			final User user = ((CMApplication)getApplicationContext()).user;
			if (user!=null) {
				for (int i=0; i < gameList.size(); i++) {
					final Game game = gameList.get(i);
					if (game.getCachedScore() > 0) {
						// Update the Feint score and reset this one
						final long myScore = game.getCachedScore();

						// Reset the score. If there are any failures with Feint we'll update it again
						((CMApplication)getApplicationContext()).resetScore(game.getGameNumber());

						// Get list of leaderboards and search by name
						Leaderboard.list(new Leaderboard.ListCB() {
							public void onSuccess(List<Leaderboard> leaderboards) {
								//final boolean bUpdatedScore = false;
								// Find our leaderboard
								boolean bFoundLeaderboard = false;
								for (int i=0; i < leaderboards.size(); i++) {
									final Leaderboard l = leaderboards.get(i);
									if (l.name.equals(game.getGameName())) {
										bFoundLeaderboard = true;
										// This is our guy, update
										l.getUserScore(user, new Leaderboard.GetUserScoreCB(){
											public void onSuccess(Score score) {
												long updatedScore = game.getCachedScore();
												if (score!=null)
													updatedScore += score.score;
												final long finalScore = updatedScore;
												// Got our current score; calculate new one
												Score newScore = new Score(updatedScore); 
												newScore.submitTo(l, new Score.SubmitToCB() {

													public void onSuccess(boolean newHighScore) {
														LogUtil.info("Score successfully posted to Feint");
														((CMApplication)getApplicationContext()).updateScoreFromFeint(game.getGameNumber(), finalScore);																						
													}

													public void onFailure(String exceptionMessage) {
														LogUtil.error("Failed to update score on Feint for leaderboard "+game.getGameName());
														((CMApplication)getApplicationContext()).updateScore(game.getGameNumber(), myScore);
													}								
												});
											}

											public void onFailure(String exceptionMessage) {
												LogUtil.error("Failed to retrieve users score for user "+user.name+" and leaderboard "+game.getGameName()+" Error: "+exceptionMessage.toString());
												((CMApplication)getApplicationContext()).updateScore(game.getGameNumber(), myScore);
											}
										});
										break;
									}
								}

								if (!bFoundLeaderboard) {
									LogUtil.error("Failed to find leaderboard for game "+game.getGameName());
									((CMApplication)getApplicationContext()).updateScore(game.getGameNumber(), myScore);								
								}
							}

							public void onFailure(String exceptionMessage) {
								LogUtil.error("Failed to get leaderboard list");
								((CMApplication)getApplicationContext()).updateScore(game.getGameNumber(), myScore);								
							}
						});
					}

				}
			}
			else {
				LogUtil.error("Feint user is null");
				((CMApplication)getApplicationContext()).loadUser();
			}
		}
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}

}
