package com.catandmouse;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.openfeint.api.resource.User;

@SuppressWarnings("rawtypes")
public class PlayerLocationOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	Activity parent;
	String gameName;
	
	public PlayerLocationOverlay(Activity parent, Drawable defaultMarker, String gameName) {
		super(boundCenter(defaultMarker));
		this.parent = parent;
		this.gameName = gameName;
		populate();
	}
	
	public PlayerLocationOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker);
		mContext = context;
	}	

	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	public int size() {
		return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
    @Override
    public void draw(Canvas canvas, MapView mapView,
                      boolean shadow) {
      super.draw(canvas, mapView, shadow);
      
    }	
    
    @Override
    protected boolean onTap(int index) {
    	// First, get the userid/name of this user
    	PlayerOverlayItem player = (PlayerOverlayItem) this.getItem(index);
    	User.findByID(player.getBean().getPlayerId(), new User.FindCB() {
			
			@Override
			public void onSuccess(User foundUser) {
		    	AlertDialog dlg = CMActivity.getFeintUserDialog(parent, foundUser, gameName);
		    	dlg.show();
			}
		});
        return super.onTap(index);
    }
    
    
    

}
