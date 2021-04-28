package com.catandmouse;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PlayerOverlayItem extends OverlayItem {
	PlayerBean bean;

	public PlayerOverlayItem(PlayerBean bean) {
		super(new GeoPoint((int)(bean.getLatitude() * 1E6), (int)(bean.getLongitude() * 1E6)), null, bean.getPlayerName());
		this.bean = bean;
	}

	public PlayerBean getBean() {
		return bean;
	}
	
	

}
