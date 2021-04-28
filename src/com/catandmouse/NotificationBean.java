package com.catandmouse;

public class NotificationBean {
    private int gameNumber;
    private int gameType;
    private String intendedPlayerId;
    private String catPlayerId;
    private String catPlayerName;
    private String mousePlayerId;
    private String mousePlayerName;
    private int notificationType = 0;
    private long activityDate;
    
    public NotificationBean () {
    }
    
    public NotificationBean(int gameNumber, int gameType, String intendedPlayerId,String catPlayerId,
    		String catPlayerName, String mousePlayerId, String mousePlayerName, int notificationType, long activityDate) {
    	this.gameNumber = gameNumber;
    	this.gameType = gameType;
    	this.intendedPlayerId = intendedPlayerId;
    	this.catPlayerId = catPlayerId;
    	this.catPlayerName = catPlayerName;
    	this.mousePlayerId = mousePlayerId;
    	this.mousePlayerName = mousePlayerName;
    	this.notificationType = notificationType;
    	this.activityDate = activityDate;
    }
    
	public int getGameNumber() {
		return gameNumber;
	}
	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}
	public int getGameType() {
		return gameType;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}
	public String getIntendedPlayerId() {
		return intendedPlayerId;
	}
	public void setIntendedPlayerId(String intendedPlayerId) {
		this.intendedPlayerId = intendedPlayerId;
	}
	public void setCatPlayerId(String catPlayerId) {
		this.catPlayerId = catPlayerId;
	}

	public String getCatPlayerId() {
		return catPlayerId;
	}

	public String getCatPlayerName() {
		return catPlayerName;
	}
	public void setCatPlayerName(String catPlayerName) {
		this.catPlayerName = catPlayerName;
	}
	public void setMousePlayerId(String mousePlayerId) {
		this.mousePlayerId = mousePlayerId;
	}

	public String getMousePlayerId() {
		return mousePlayerId;
	}

	public String getMousePlayerName() {
		return mousePlayerName;
	}
	public void setMousePlayerName(String mousePlayerName) {
		this.mousePlayerName = mousePlayerName;
	}
	public int getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(int notificationType) {
		this.notificationType = notificationType;
	}
	public long getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(long activityDate) {
		this.activityDate = activityDate;
	}
    

}
