package com.catandmouse;

public class GamePlayerBean {
	private String playerId;
	private int gameNumber;
	private boolean isCat;
	
	public GamePlayerBean(){}
	
	public GamePlayerBean(String playerId, int gameNumber, boolean isCat) {
		this.playerId = playerId;
		this.gameNumber = gameNumber;
		this.isCat = isCat;
	}
	
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public int getGameNumber() {
		return gameNumber;
	}
	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}
	public boolean isCat() {
		return isCat;
	}
	public void setCat(boolean isCat) {
		this.isCat = isCat;
	}
	
	
}
