package com.catandmouse;

public class GameBean {
	private int gameNumber;
	private String gameName;
	private int gameType;
	private boolean isPrivate;
	private String password;
	private String locationName;
	double latitude;
	double longitude;
	private long range;
	private int ratio;
	private long playerCount;
	private long startTime;
	private long endTime;
	
	public GameBean () {
	}
	
	public GameBean(int gameNumber, String gameName, int gameType, boolean isPrivate, String password,String locationName, 
			double latitude, double longitude, long range, int ratio, long startTime, long endTime) {
		this.gameNumber = gameNumber;
		this.gameName = gameName;
		this.gameType = gameType;
		this.isPrivate = isPrivate;
		this.password = password;
		this.locationName = locationName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.range = range;
		this.ratio = ratio;
		this.startTime = startTime;
		this.endTime = endTime;
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

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
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

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	/**
	 * @param playerCount the playerCount to set
	 */
	public void setPlayerCount(long playerCount) {
		this.playerCount = playerCount;
	}

	/**
	 * @return the playerCount
	 */
	public long getPlayerCount() {
		return playerCount;
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

}

