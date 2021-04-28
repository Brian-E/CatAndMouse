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

public class NotificationDataHelper {

	private static final String DATABASE_NAME = "catandmouse.notdb";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "notifications";
	public static final String COLUMN_GAME_NUMBER="gameNumber";
	public static final String COLUMN_GAME_TYPE="gameType";
	public static final String COLUMN_INTENDED_PLAYERID="intendedPlayerId";
	public static final String COLUMN_CAT_PLAYER_ID="catPlayerId";
	public static final String COLUMN_CAT_PLAYER_NAME="catPlayerName";
	public static final String COLUMN_MOUSE_PLAYER_ID="mousePlayerId";
	public static final String COLUMN_MOUSE_PLAYER_NAME="mousePlayerName";
	private static final String COLUMN_NOTIFICATION_TYPE = "notificationType";
	public static final String COLUMN_ACTIVITY_TIME="activityTime";
	public static final String [] COLUMNS = {COLUMN_GAME_NUMBER,COLUMN_GAME_TYPE,COLUMN_INTENDED_PLAYERID,
		COLUMN_CAT_PLAYER_ID,COLUMN_CAT_PLAYER_NAME, COLUMN_MOUSE_PLAYER_ID,COLUMN_MOUSE_PLAYER_NAME,COLUMN_NOTIFICATION_TYPE,COLUMN_ACTIVITY_TIME};

	private Context context;
	private SQLiteDatabase db;

	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into "
		+ TABLE_NAME + "("+
		COLUMN_GAME_NUMBER+","+
		COLUMN_GAME_TYPE+","+
		COLUMN_INTENDED_PLAYERID+","+
		COLUMN_CAT_PLAYER_ID+","+
		COLUMN_CAT_PLAYER_NAME+","+
		COLUMN_MOUSE_PLAYER_ID+","+
		COLUMN_MOUSE_PLAYER_NAME+","+
		COLUMN_NOTIFICATION_TYPE+","+
		COLUMN_ACTIVITY_TIME+
		") values (?,?,?,?,?,?,?,?,?)";

	public NotificationDataHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement(INSERT);
	}

	public long insert(CMNotification n) {
		try {
		this.insertStmt.bindLong(1, n.getGameNumber());
		this.insertStmt.bindLong(2, n.getGameType());
		this.insertStmt.bindString(3, n.getIntendedPlayerId());
		this.insertStmt.bindString(4, n.getCatPlayerId());
		this.insertStmt.bindString(5, n.getCatPlayerName());
		this.insertStmt.bindString(6, n.getMousePlayerId());
		this.insertStmt.bindString(7, n.getMousePlayerName());
		this.insertStmt.bindLong(8, n.getNotificationType());
		this.insertStmt.bindLong(9, n.getActivityTime());
		return this.insertStmt.executeInsert();
		}
		catch (SQLException e) {
			LogUtil.error("Failed to insert notification!");
			return -1;
		}
	}
	
//	public boolean update(int gameNumber, ContentValues args) {
//		boolean success = db.update(TABLE_NAME, args, COLUMN_GAME_NUMBER + "=='" + gameNumber +"'", null) > 0;
//		if (!success)
//			LogUtil.error("Failed to update db, args="+args.toString());
//		return success;
//	}

	public void delete(int gameNumber) {
		int rowsDeleted = db.delete(TABLE_NAME, COLUMN_GAME_NUMBER+"=="+gameNumber, null);
		if (rowsDeleted==0)
			LogUtil.warn("Notifications not deleted for game number:"+gameNumber);
	}
	
	public void deleteBefore(long activityTime) {
		int rowsDeleted = db.delete(TABLE_NAME, COLUMN_ACTIVITY_TIME+"<"+activityTime, null);
		if (rowsDeleted==0)
			LogUtil.warn("Notifications not deleted after activity time:"+activityTime);
	}
	
	public void deleteAll() {
		this.db.delete(TABLE_NAME, null, null);
	}
	
//	public Game selectGame(int inGameNumber) {
//		Game game = null;
//		Cursor cursor = this.db.query(TABLE_NAME, COLUMNS,
//				COLUMN_GAME_NUMBER+"=='"+inGameNumber+"'", null, null, null, null);
//		if (cursor.moveToFirst()) {
//			int gameNumber = cursor.getInt(0);
//			String gameName = cursor.getString(1);
//			int gameType = cursor.getInt(2);
//			String locationName = cursor.getString(3);
//			int ratio = cursor.getInt(4);
//			long endTime = cursor.getLong(5);
//			boolean isCat = cursor.getInt(6) == 1 ? true : false;
//			long lastCaughtTime = cursor.getLong(7);
//			long cachedScore = cursor.getLong(8);
//			game = new Game(gameNumber, gameName, gameType, false, locationName, 0, 0, 0, ratio, 0, endTime, isCat, lastCaughtTime,cachedScore);
//		}
//		if (cursor != null && !cursor.isClosed()) {
//			cursor.close();
//		}
//		
//		return game;
//	}

	public List<CMNotification> selectNotifications(long afterActivityTime, int inGameNumber) {
		List<CMNotification> list = new ArrayList<CMNotification>();
		Cursor cursor = this.db.query(TABLE_NAME, COLUMNS,COLUMN_GAME_NUMBER+" == "+inGameNumber+" AND "+
				COLUMN_ACTIVITY_TIME+" > "+afterActivityTime, null, null, null, COLUMN_ACTIVITY_TIME+" asc");
		if (cursor.moveToFirst()) {
			do {
				int gameNumber = cursor.getInt(0);
				int gameType = cursor.getInt(1);
				String intendedPlayerId = cursor.getString(2);
				String catPlayerId = cursor.getString(3);
				String catPlayerName = cursor.getString(4);
				String mousePlayerId = cursor.getString(5);
				String mousePlayerName = cursor.getString(6);
				int notificationType = cursor.getInt(7);
				long activityTime = cursor.getLong(8);
				CMNotification n = new CMNotification(gameNumber, gameType, intendedPlayerId,
						catPlayerId, catPlayerName, mousePlayerId, mousePlayerName, notificationType, activityTime);
				list.add(n);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

//	public List<Integer> selectGameNumbers() {
//		List<Integer> list = new ArrayList<Integer>();
//		Cursor cursor;
//		String[] columns = new String[]{COLUMN_GAME_NUMBER};
//		cursor = this.db.query(TABLE_NAME, columns, null, null, null, null, null);
//		
//		if (cursor.moveToFirst()) {
//			do {
//				int gameNumber = cursor.getInt(0);
//				list.add(gameNumber);
//			} while (cursor.moveToNext());
//		}
//		if (cursor != null && !cursor.isClosed()) {
//			cursor.close();
//		}
//		
//		return list;
//	}
	
//	public List<Integer> selectGameNumbers(boolean isCat) {
//		List<Integer> list = new ArrayList<Integer>();
//		Cursor cursor;
//		if (isCat) {
//			String[] columns = new String[]{COLUMN_GAME_NUMBER};
//			cursor = this.db.query(TABLE_NAME, columns, COLUMN_IS_CAT+"==1", null, null, null, null);
//		}
//		else {
//			String[] columns = new String[]{COLUMN_GAME_NUMBER};
//			cursor = this.db.query(TABLE_NAME, columns, COLUMN_IS_CAT+"==0", null, null, null, null);
//		}
//		
//		if (cursor.moveToFirst()) {
//			do {
//				int gameNumber = cursor.getInt(0);
//				list.add(gameNumber);
//			} while (cursor.moveToNext());
//		}
//		if (cursor != null && !cursor.isClosed()) {
//			cursor.close();
//		}
//		
//		return list;
//	}

	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + "("+
					"id INTEGER PRIMARY KEY AUTOINCREMENT,"+
					COLUMN_GAME_NUMBER + " INTEGER,"+
					COLUMN_GAME_TYPE+" INTEGER,"+
					COLUMN_INTENDED_PLAYERID+" STRING,"+
					COLUMN_CAT_PLAYER_ID+" STRING,"+
					COLUMN_CAT_PLAYER_NAME+" STRING,"+
					COLUMN_MOUSE_PLAYER_ID+" STRING,"+
					COLUMN_MOUSE_PLAYER_NAME+" STRING,"+
					COLUMN_NOTIFICATION_TYPE+" INTEGER,"+
					COLUMN_ACTIVITY_TIME+" INTEGER)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Example", "Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}

