package com.catandmouse;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.MapView;


public class PlayerLocationMapView extends MapView {
	 int oldZoomLevel=-1;
	 
	public PlayerLocationMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	public PlayerLocationMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PlayerLocationMapView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent ev) {
//		if (ev.equals(MotionEvent.ACTION_UP)) {
//		}
//		return super.onTouchEvent(ev);
//	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (getZoomLevel() != oldZoomLevel) {
			oldZoomLevel = getZoomLevel();
		}		
	}
	
	

}
