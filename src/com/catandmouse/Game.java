package com.catandmouse;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Brian Emond
 *	Generic POJO to store game info
 */
public class Game implements Parcelable {
	private int gameNumber;
	private String gameName;
	private int gameType;
	private boolean isPrivate;
	private String locationName;
	private double latitude;
	private double longitude;
	private long range;
	private int ratio;
	private long startTime;
	private long endTime;
	private boolean isCat;
	private long lastCaughtTime;
	private long cachedScore;

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		public Game createFromParcel(Parcel in) {

			return new Game(in); 

		}

		public Game[] newArray(int arg0) {
			return new Game[arg0];
		}
	};
	
	public Game(int gameNumber, String gameName, int gameType, boolean isPrivate, String locationName, double latitude, 
			double longitude, long range, int ratio, long startTime,
			long endTime, boolean isCat, long lastCaughtTime, long cachedScore) {
		this.gameNumber = gameNumber;
		this.gameName = gameName;
		this.gameType = gameType;
		this.isPrivate = isPrivate;
		this.locationName = locationName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.range = range;
		this.ratio = ratio;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isCat = isCat;
		this.lastCaughtTime = lastCaughtTime;
		this.cachedScore = cachedScore;
	}
	
	public Game (Parcel in) {
		this.gameNumber = in.readInt();
		this.gameName = in.readString();
		this.gameType = in.readInt();
		this.isPrivate = in.readInt() == 1 ? true : false;
		this.locationName = in.readString();
		this.latitude = in.readFloat();
		this.longitude = in.readFloat();
		this.range = in.readLong();
		this.ratio = in.readInt();
		this.startTime = in.readLong();
		this.endTime = in.readLong();
		this.isCat = in.readInt() == 1 ? true : false;		
		this.lastCaughtTime = in.readLong();
		this.cachedScore = in.readLong();
	}
	
	public int getGameNumber() {
		return gameNumber;
	}
	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}
	public String getGameName() {
		return gameName;
	}
	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	public int getGameType() {
		return gameType;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationName() {
		return locationName;
	}

	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public long getRange() {
		return range;
	}
	public void setRange(long range) {
		this.range = range;
	}
	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public int getRatio() {
		return ratio;
	}

	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public boolean isCat() {
		return isCat;
	}
	public void setCat(boolean isCat) {
		this.isCat = isCat;
	}

	public long getLastCaughtTime() {
		return lastCaughtTime;
	}

	public void setLastCaughtTime(long lastCaughtTime) {
		this.lastCaughtTime = lastCaughtTime;
	}

	public void setCachedScore(long cachedScore) {
		this.cachedScore = cachedScore;
	}

	public long getCachedScore() {
		return cachedScore;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(gameNumber);
		parcel.writeString(gameName);
		parcel.writeInt(gameType);
		parcel.writeInt(isPrivate ? 1 : 0);
		parcel.writeString(locationName);
		parcel.writeDouble(latitude);
		parcel.writeDouble(longitude);
		parcel.writeLong(range);
		parcel.writeInt(ratio);
		parcel.writeLong(startTime);
		parcel.writeLong(endTime);
		parcel.writeInt(isCat ? 1 : 0);
		parcel.writeLong(lastCaughtTime);
		parcel.writeLong(cachedScore);
	}
}
