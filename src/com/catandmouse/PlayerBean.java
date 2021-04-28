package com.catandmouse;


public class PlayerBean {
	private String playerId;
	private String playerName;
	private boolean isCat;
	private float latitude;
	private float longitude;
	
	public PlayerBean() {}
	
	public PlayerBean(String playerId, String playerName, boolean isCat, float latitude, float longitude) {
		this.playerId = playerId;
		this.playerName = playerName;
		this.isCat = isCat;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void setCat(boolean isCat) {
		this.isCat = isCat;
	}

	public boolean isCat() {
		return isCat;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}


}
