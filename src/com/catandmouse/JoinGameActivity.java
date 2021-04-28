package com.catandmouse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.myjson.Gson;
import com.google.myjson.reflect.TypeToken;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class JoinGameActivity extends CMActivity {
	private List<GameBean> gameList = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_game);
		
		final TableLayout table = (TableLayout) findViewById(R.id.tableLayout_JoinGame);
		
		// Query the system for all available games. 
		
		final HttpGet request = new HttpGet(ServerUtil.GET_GAMES_URL);
		final HttpClient client = ServerUtil.getHttpClient();
		waitCursor = ProgressDialog.show(this, null, getResources().getString(R.string.Retrieving_Games), true, true);
		Thread getGamesThread = new Thread() {
			public void run() {
				try {
					HttpResponse resp = client.execute(request);
					int rc = resp.getStatusLine().getStatusCode();
					switch (rc) {
					case 0:
					case HttpStatus.SC_OK:
						// Success - should have a List<GameBean> in our json response
						Reader reader = new InputStreamReader(resp.getEntity().getContent());
						Gson gson = new Gson();
						Type listType = new TypeToken<List<GameBean>>(){}.getType();
						gameList = gson.fromJson(reader, listType);
						handler.post(new Runnable(){

							public void run() {
								if (gameList!=null && gameList.size()>0) {
									for (int i=0; i < gameList.size(); i++) {
										// Add our table rows with some of this data
										addTableRow(table, gameList.get(i));
									}
								}
								else {
									// Add some message to the table that there's no games
									TableRow row = new TableRow(JoinGameActivity.this);
									row.setHorizontalGravity(Gravity.CENTER);
									row.setBackgroundColor(android.R.color.white);
									TextView tv = new TextView(JoinGameActivity.this);
									tv.setGravity(Gravity.CENTER_HORIZONTAL);
									tv.setTextAppearance(JoinGameActivity.this, android.R.style.TextAppearance_Small_Inverse);
									tv.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
									tv.setText(getResources().getString(R.string.No_Games_Available));
									row.addView(tv);
									table.addView(row);
								}
								
								waitCursor.dismiss();
							}
							
						});
						break;
					default:
						LogUtil.error("Bad return code from GetGames: "+rc);
					}
				} catch (Exception e) {
					LogUtil.error(e.toString());
				}
				finally {
					waitCursor.dismiss();
				}
			}
			
		};
		getGamesThread.start();
	}
	
	private void addTableRow(TableLayout table, GameBean gb) {
		//TableRow newRow = new TableRow(this);
		//newRow.setBackgroundColor(getResources().getColor(android.R.color.white));
		LogUtil.info("JoinGame gamebean name="+gb.getGameName());
    	LayoutInflater inflater =
    	    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	final View layout =
    	    inflater.inflate(R.layout.join_game_view,null);
    	layout.setOnClickListener(new GameNameClickListener(gb.getGameName()));
		
		//int iTextSize = getResources().get
//		newRow.setClickable(true);
//		newRow.setLongClickable(true);
//		newRow.setOnClickListener(new GameNameClickListener());
//		newRow.setOnLongClickListener(new GameNameLongClickListener());
		
		// Game name - clickable to JoinGameInfo view, long clickable to join
		TextView tvGameName = (TextView) layout.findViewById(R.id.textView_JoinGameView_GameName);    	

//		tvGameName.setTextColor(iTextColor);
//		tvGameName.setTextSize(20);
//		tvGameName.setClickable(true);
//		tvGameName.setLongClickable(true);
//		
//		tvGameName.setOnClickListener(new GameNameClickListener());
//		tvGameName.setOnLongClickListener(new GameNameLongClickListener());
		
		tvGameName.setText(gb.getGameName());
		
		// Game Type
		TextView tvGameType = (TextView) layout.findViewById(R.id.textView_JoinGameView_Type);    	

		//tvGameType.setTextColor(iTextColor);
		String type = (gb.getGameType() == CMConstants.GAME_TYPE_NORMAL ? getResources().getString(R.string.Normal) : getResources().getString(R.string.Reverse));
		tvGameType.setText(type);
		
		// Private
		TextView tvPublic = (TextView) layout.findViewById(R.id.textView_JoinGameView_Private);
		int iTextColor = 0;
		if (gb.isPrivate())
			iTextColor = getResources().getColor(R.color.red);
		else
			iTextColor = getResources().getColor(R.color.green);

		tvPublic.setTextColor(iTextColor);
		tvPublic.setText((gb.isPrivate() ? getResources().getString(R.string.Private) : getResources().getString(R.string.Public)));
		
		// Location
		TextView tvLocation = (TextView) layout.findViewById(R.id.textView_JoinGameView_Location);
		if(gb.getLocationName()==null || gb.getLocationName().length()==0) {
			// no location settings?
			if (gb.getLatitude()==0 && gb.getLongitude()==0)
				tvLocation.setText(getResources().getString(R.string.Global));
		}
		else
			tvLocation.setText(gb.getLocationName());

		// Ratio
		TextView tvRatio = (TextView) layout.findViewById(R.id.textView_JoinGameView_Ratio);
		tvRatio.setText(Integer.toString(gb.getRatio())+" : 1");
		
		// Player Count
		TextView tvPlayers = (TextView) layout.findViewById(R.id.textView_JoinGameView_Players);
		tvPlayers.setText(Long.toString(gb.getPlayerCount())+" "+getResources().getString(R.string.Players));
		
		// Time
//		DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(this);
//		Date startTime = new Date(gb.getStartTime());
//		Date endTime = new Date(gb.getEndTime());
		
		TextView tvTime = (TextView) layout.findViewById(R.id.textView_JoinGame_view_Time);
		int iDateFormat = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR;
		tvTime.setText(DateUtils.formatDateTime(this, gb.getStartTime(), iDateFormat)+" - "+DateUtils.formatDateTime(this, gb.getEndTime(), iDateFormat));
		
		//newRow.addView(layout);
		
		table.addView(layout);
	}
	
	private class GameNameClickListener implements OnClickListener {
		private String gameName;
		public GameNameClickListener(String gameName) {
			this.gameName = gameName;
		}
		
		public void onClick(View view) {
			// Get a Game object, call joinGame
			// find game
			Game game = null;

			for (int i=0; i < gameList.size(); i++) {
				GameBean gb = gameList.get(i);
				if (gb.getGameName().equals(gameName)) {
					game = new Game(gb.getGameNumber(), gb.getGameName(), gb.getGameType(), 
							gb.isPrivate(), gb.getLocationName(), gb.getLatitude(), 
							gb.getLongitude(), gb.getRange(), gb.getRatio(),
							gb.getStartTime(), gb.getEndTime(), gb.getGameType()==CMConstants.GAME_TYPE_NORMAL ? false : true, 0,0);
					break;
				}
			}
			
			if (game!=null) {
				// Are we already in this game?
				Game existingGame = ((CMApplication)getApplicationContext()).dh.selectGame(game.getGameNumber());
				if (existingGame!=null) {
					// already in it
					toastMessage(getResources().getString(R.string.AlreadyInGame), Toast.LENGTH_SHORT);
					return;
				}
				// If this is a private game, prompt for the password now
				if (game.isPrivate()) {
					Dialog passwordDialog = getPasswordDialog(game);
					passwordDialog.show();
				}
				else
					joinGame(game, null);
			}
			else {
				// Didn't find our game by name?
				LogUtil.error("Couldn't find game by name: "+gameName);
			}
			
			// Launch JoinGameInfoActivity for this game
			// find game
//			Game game = null;
//
//			for (int i=0; i < gameList.size(); i++) {
//				GameBean gb = gameList.get(i);
//				if (gb.getGameName().equals(gameName)) {
//					game = new Game(gb.getGameNumber(), gb.getGameName(), gb.getGameType(), 
//							gb.isPrivate(), gb.getLocationName(), gb.getLatitude(), 
//							gb.getLongitude(), gb.getRange(), gb.getRatio(),
//							gb.getStartTime(), gb.getEndTime(), gb.getGameType()==CMConstants.GAME_TYPE_NORMAL ? false : true, 0,0); 
//					break;
//				}
//			}
//			
//			if (game!=null) {
//				Intent intent = new Intent(myself, JoinGameInfoActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putParcelable(CMConstants.GAME_PACKAGE, game);
//				intent.putExtras(bundle);
//				startActivity(intent);
//			}
//			else {
//				// Didn't find our game by name?
//				LogUtil.error("Couldn't find game by name: "+gameName);
//			}
		}
	}
	
	private class GameNameLongClickListener implements OnLongClickListener {

		public boolean onLongClick(View view) {
			// Get a Game object, call joinGame
			String gameName = (String) ((TextView)view).getText();
			// find game
			Game game = null;

			for (int i=0; i < gameList.size(); i++) {
				GameBean gb = gameList.get(i);
				if (gb.getGameName().equals(gameName)) {
					game = new Game(gb.getGameNumber(), gb.getGameName(), gb.getGameType(), 
							gb.isPrivate(), gb.getLocationName(), gb.getLatitude(), 
							gb.getLongitude(), gb.getRange(), gb.getRatio(),
							gb.getStartTime(), gb.getEndTime(), gb.getGameType()==CMConstants.GAME_TYPE_NORMAL ? false : true, 0,0);
					break;
				}
			}
			
			if (game!=null) {
				// If this is a private game, prompt for the password now
				if (game.isPrivate()) {
					Dialog passwordDialog = getPasswordDialog(game);
					passwordDialog.show();
				}
				else
					joinGame(game, null);
			}
			else {
				// Didn't find our game by name?
				LogUtil.error("Couldn't find game by name: "+gameName);
			}
			
			return true;
		}
		
	}
	
}
