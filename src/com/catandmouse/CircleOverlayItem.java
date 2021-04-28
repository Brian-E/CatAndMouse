package com.catandmouse;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class CircleOverlayItem extends OverlayItem {
	boolean isCenter;
	boolean isBeingMoved;

	public CircleOverlayItem(GeoPoint point, boolean isCenter) {
		super(point, null, null);
		this.isCenter = isCenter;
		isBeingMoved = false;
	}

	public boolean isCenter() {
		return isCenter;
	}

	public boolean isBeingMoved() {
		return isBeingMoved;
	}

	public void setBeingMoved(boolean isBeingMoved) {
		this.isBeingMoved = isBeingMoved;
	}


}
