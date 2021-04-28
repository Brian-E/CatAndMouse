package com.catandmouse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {

	private static final String DATABASE_NAME = "catandmouse.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "games";
	public static final String COLUMN_GAME_NUMBER="gameNumber";
	public static final String COLUMN_GAME_NAME="gameName";
	public static final String COLUMN_GAME_TYPE="gameType";
	public static final String COLUMN_LOCATION_NAME="locationName";
	private static final String COLUMN_RATIO = "ratio";
	public static final String COLUMN_GAME_END_TIME="endTime";
	public static final String COLUMN_IS_CAT="isCat";
	public static final String COLUMN_LAST_CAUGHT_TIME="lastCaughtTime";
	public static final String COLUMN_CACHED_SCORE="cachedScore";
	public static final String [] COLUMNS = {COLUMN_GAME_NUMBER,COLUMN_GAME_NAME,COLUMN_GAME_TYPE,COLUMN_LOCATION_NAME,
											  COLUMN_RATIO, COLUMN_GAME_END_TIME,COLUMN_IS_CAT,COLUMN_LAST_CAUGHT_TIME,
											  COLUMN_CACHED_SCORE};

	private Context context;
	private SQLiteDatabase db;

	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into "
		+ TABLE_NAME + "("+
		COLUMN_GAME_NUMBER+","+
		COLUMN_GAME_NAME+","+
		COLUMN_GAME_TYPE+","+
		COLUMN_LOCATION_NAME+","+
		COLUMN_RATIO+","+
		COLUMN_GAME_END_TIME+","+
		COLUMN_IS_CAT+","+
		COLUMN_LAST_CAUGHT_TIME+","+
		COLUMN_CACHED_SCORE+
		") values (?,?,?,?,?,?,?,?,?)";

	public DataHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement(INSERT);
	}

	public long insert(Game game) {
		try {
		this.insertStmt.bindLong(1, game.getGameNumber());
		this.insertStmt.bindString(2, game.getGameName());
		this.insertStmt.bindLong(3, game.getGameType());
		this.insertStmt.bindString(4, game.getLocationName());
		this.insertStmt.bindLong(5, game.getRatio());
		this.insertStmt.bindLong(6, game.getEndTime());
		this.insertStmt.bindLong(7, game.isCat() ? 1 : 0);
		this.insertStmt.bindLong(8, game.getLastCaughtTime());
		this.insertStmt.bindLong(9, 0);
		return this.insertStmt.executeInsert();
		}
		catch (SQLException e) {
			LogUtil.error("Failed to insert game!");
			return -1;
		}
	}
	
	public boolean update(int gameNumber, ContentValues args) {
		boolean success = db.update(TABLE_NAME, args, COLUMN_GAME_NUMBER + "=='" + gameNumber +"'", null) > 0;
		if (!success)
			LogUtil.error("Failed to update db, args="+args.toString());
		return success;
	}

	public void delete(int gameNumber) {
		int rowsDeleted = db.delete(TABLE_NAME, COLUMN_GAME_NUMBER+"=='"+gameNumber+"'", null);
		if (rowsDeleted==0)
			LogUtil.warn("Delete: Game number not found:"+gameNumber);
		else if (rowsDeleted>0)
			LogUtil.info("Game number "+gameNumber+" deleted from db successfully");
	}
	
	public void deleteAll() {
		this.db.delete(TABLE_NAME, null, null);
	}
	
	public Game selectGame(int inGameNumber) {
		Game game = null;
		Cursor cursor = this.db.query(TABLE_NAME, COLUMNS,
				COLUMN_GAME_NUMBER+"=='"+inGameNumber+"'", null, null, null, null);
		if (cursor.moveToFirst()) {
			int gameNumber = cursor.getInt(0);
			String gameName = cursor.getString(1);
			int gameType = cursor.getInt(2);
			String locationName = cursor.getString(3);
			int ratio = cursor.getInt(4);
			long endTime = cursor.getLong(5);
			boolean isCat = cursor.getInt(6) == 1 ? true : false;
			long lastCaughtTime = cursor.getLong(7);
			long cachedScore = cursor.getLong(8);
			game = new Game(gameNumber, gameName, gameType, false, locationName, 0, 0, 0, ratio, 0, endTime, isCat, lastCaughtTime,cachedScore);
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		return game;
	}

	public List<Game> selectAll() {
		List<Game> list = new ArrayList<Game>();
		Cursor cursor = this.db.query(TABLE_NAME, COLUMNS,
				null, null, null, null, COLUMN_GAME_NUMBER+" asc");
		if (cursor.moveToFirst()) {
			do {
				int gameNumber = cursor.getInt(0);
				String gameName = cursor.getString(1);
				int gameType = cursor.getInt(2);
				String locationName = cursor.getString(3);
				int ratio = cursor.getInt(4);
				long endTime = cursor.getLong(5);
				boolean isCat = cursor.getInt(6) == 1 ? true : false;
				long lastCaughtTime = cursor.getLong(7);
				long cachedScore = cursor.getLong(8);
				Game game = new Game(gameNumber, gameName, gameType, false, locationName, 0, 0, 0, ratio, 0, endTime, 
						isCat, lastCaughtTime, cachedScore);
				list.add(game);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	public List<Game> selectAll(boolean bIsCat) {
		List<Game> list = new ArrayList<Game>();
		Cursor cursor = this.db.query(TABLE_NAME, COLUMNS,
				bIsCat ? COLUMN_IS_CAT+"==1" : COLUMN_IS_CAT+"==0", null, null, null, COLUMN_GAME_NUMBER+" asc");
		if (cursor.moveToFirst()) {
			do {
				int gameNumber = cursor.getInt(0);
				String gameName = cursor.getString(1);
				int gameType = cursor.getInt(2);
				String locationName = cursor.getString(3);
				int ratio = cursor.getInt(4);
				long endTime = cursor.getLong(5);
				boolean isCat = cursor.getInt(6) == 1 ? true : false;
				long lastCaughtTime = cursor.getLong(7);
				long cachedScore = cursor.getLong(8);
				Game game = new Game(gameNumber, gameName, gameType, false, locationName, 0, 0, 0, ratio, 0, endTime, 
						isCat, lastCaughtTime, cachedScore);
				list.add(game);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	public List<Integer> selectGameNumbers() {
		List<Integer> list = new ArrayList<Integer>();
		Cursor cursor;
		String[] columns = new String[]{COLUMN_GAME_NUMBER};
		cursor = this.db.query(TABLE_NAME, columns, null, null, null, null, null);
		
		if (cursor.moveToFirst()) {
			do {
				int gameNumber = cursor.getInt(0);
				list.add(gameNumber);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		return list;
	}
	
	public List<Integer> selectGameNumbers(boolean isCat) {
		List<Integer> list = new ArrayList<Integer>();
		Cursor cursor;
		if (isCat) {
			String[] columns = new String[]{COLUMN_GAME_NUMBER};
			cursor = this.db.query(TABLE_NAME, columns, COLUMN_IS_CAT+"==1", null, null, null, null);
		}
		else {
			String[] columns = new String[]{COLUMN_GAME_NUMBER};
			cursor = this.db.query(TABLE_NAME, columns, COLUMN_IS_CAT+"==0", null, null, null, null);
		}
		
		if (cursor.moveToFirst()) {
			do {
				int gameNumber = cursor.getInt(0);
				list.add(gameNumber);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		return list;
	}

	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + "("+
					COLUMN_GAME_NUMBER +" INTEGER PRIMARY KEY,"+
					COLUMN_GAME_NAME + " STRING,"+
					COLUMN_GAME_TYPE+" INTEGER,"+
					COLUMN_LOCATION_NAME+" STRING,"+
					COLUMN_RATIO+" INTEGER,"+
					COLUMN_GAME_END_TIME+" INTEGER,"+
					COLUMN_IS_CAT+" INTEGER,"+
					COLUMN_LAST_CAUGHT_TIME+" INTEGER,"+
					COLUMN_CACHED_SCORE+" INTEGER)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Example", "Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}

