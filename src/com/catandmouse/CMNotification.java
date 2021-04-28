package com.catandmouse;

import android.os.Parcel;
import android.os.Parcelable;

public class CMNotification implements Parcelable {

	private int gameNumber;
	private int gameType;
	private String intendedPlayerId;
	private String catPlayerId;
	private String catPlayerName;
	private String mousePlayerId;
	private String mousePlayerName;
	private int notificationType;
	private long activityTime;
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		public CMNotification createFromParcel(Parcel in) {

			return new CMNotification(in); 

		}

		public CMNotification[] newArray(int arg0) {
			return new CMNotification[arg0];
		}
	};
	
	public CMNotification(int gameNumber, int gameType, String intendedPlayerId, String catPlayerId, String catPlayerName, 
			String mousePlayerId, String mousePlayerName,
			int notificationType, long activityTime) {
		this.gameNumber = gameNumber;
		this.gameType = gameType;
		this.intendedPlayerId = intendedPlayerId;
		this.catPlayerId = catPlayerId;
		this.catPlayerName = catPlayerName;
		this.mousePlayerId = mousePlayerId;
		this.mousePlayerName = mousePlayerName;
		this.notificationType = notificationType;
		this.activityTime = activityTime;
	}
	
	public CMNotification(Parcel in) {
		this.gameNumber = in.readInt();
		this.gameType = in.readInt();
		this.intendedPlayerId = in.readString();
		this.catPlayerId = in.readString();
		this.catPlayerName = in.readString();
		this.mousePlayerId = in.readString();
		this.mousePlayerName = in.readString();
		this.notificationType = in.readInt();
		this.activityTime = in.readLong();
	}
	
	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getGameType() {
		return gameType;
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

	public void setActivityTime(long activityTime) {
		this.activityTime = activityTime;
	}

	public long getActivityTime() {
		return activityTime;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(gameNumber);
		parcel.writeInt(gameType);
		parcel.writeString(intendedPlayerId);
		parcel.writeString(catPlayerId);
		parcel.writeString(catPlayerName);
		parcel.writeString(mousePlayerId);
		parcel.writeString(mousePlayerName);
		parcel.writeInt(notificationType);
		parcel.writeLong(activityTime);
	}

	@Override
	public boolean equals(Object o) {
		LogUtil.info("CMNotification equals() called");
		if (o instanceof CMNotification) {
			// game number
			if (((CMNotification)o).getGameNumber()!=gameNumber)
				return false;

			// game type
			if (((CMNotification)o).getGameType()!=gameType)
				return false;

			// intended player id
			if ((((CMNotification)o).getIntendedPlayerId()==null && intendedPlayerId!=null) ||
					(((CMNotification)o).getIntendedPlayerId()!=null && intendedPlayerId==null))
				return false;
			else if ((((CMNotification)o).getIntendedPlayerId()!=null && intendedPlayerId!=null) &&
					(!((CMNotification)o).getIntendedPlayerId().equals(intendedPlayerId)))
				return false;

			// cat player id
			if ((((CMNotification)o).getCatPlayerId()==null && catPlayerId!=null) ||
					(((CMNotification)o).getCatPlayerId()!=null && catPlayerId==null))
				return false;
			else if ((((CMNotification)o).getCatPlayerId()!=null && catPlayerId!=null) &&
					(!((CMNotification)o).getCatPlayerId().equals(catPlayerId)))
				return false;

			// cat player name
			if ((((CMNotification)o).getCatPlayerName()==null && catPlayerName!=null) ||
					(((CMNotification)o).getCatPlayerName()!=null && catPlayerName==null))
				return false;
			else if ((((CMNotification)o).getCatPlayerName()!=null && catPlayerName!=null) &&
					(!((CMNotification)o).getCatPlayerName().equals(catPlayerName)))
				return false;

			// mouse player id
			if ((((CMNotification)o).getMousePlayerId()==null && mousePlayerId!=null) ||
					(((CMNotification)o).getMousePlayerId()!=null && mousePlayerId==null))
				return false;
			else if ((((CMNotification)o).getMousePlayerId()!=null && mousePlayerId!=null) &&
					(!((CMNotification)o).getMousePlayerId().equals(mousePlayerId)))
				return false;

			// mouse player name
			if ((((CMNotification)o).getMousePlayerName()==null && mousePlayerName!=null) ||
					(((CMNotification)o).getMousePlayerName()!=null && mousePlayerName==null))
				return false;
			else if ((((CMNotification)o).getMousePlayerName()!=null && mousePlayerName!=null) &&
					(!((CMNotification)o).getMousePlayerName().equals(mousePlayerName)))
				return false;

			// notification type
			if (((CMNotification)o).getNotificationType()!=notificationType)
				return false;

			// activity time
			if (((CMNotification)o).getActivityTime()!=activityTime)
				return false;

			LogUtil.info("CMNotification equals() returning true");
			return true;
		}

		LogUtil.info("CMNotification equals() returning false");
		return false;
	}
	
	

}
