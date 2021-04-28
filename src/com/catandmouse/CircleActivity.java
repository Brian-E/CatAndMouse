package com.catandmouse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class CircleActivity extends MapActivity {
	CircleOverlay circleOverlay;
	MyLocationOverlay myLocationOverlay;
	boolean bMyLocationShowing=false;
	MapView mapView;
	
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.create_game_map);
		mapView = (MapView) findViewById(R.id.mapview_CreateGameView);
	    mapView.setBuiltInZoomControls(true);
	    
	    myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
	    mapView.getOverlays().add(myLocationOverlay);
	    
	    showMarkers();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (bMyLocationShowing)
			myLocationOverlay.disableMyLocation();
	    //myLocationOverlay.disableCompass();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (bMyLocationShowing)
			myLocationOverlay.enableMyLocation();
	    //myLocationOverlay.enableCompass();
	}

	public void finish() {
	    Intent data = new Intent();
	    GeoPoint center = null;
	    GeoPoint edge = null;
	    if (circleOverlay!=null && circleOverlay.size()>0) {
	    	for (int i=0; i < circleOverlay.size(); i++) {
	    		CircleOverlayItem item = (CircleOverlayItem) circleOverlay.getItem(i);
	    		if (item.isCenter())
	    			center = item.getPoint();
	    		else
	    			edge = item.getPoint();
	    	}
	    	if (center!=null && edge!=null) {
	    		// Need the distance between them
	    		Location locationA = new Location("point A");  

	    		locationA.setLatitude(center.getLatitudeE6() / 1E6);  
	    		locationA.setLongitude(center.getLongitudeE6() / 1E6);  

	    		Location locationB = new Location("point B");  

	    		locationB.setLatitude(edge.getLatitudeE6() / 1E6);  
	    		locationB.setLongitude(edge.getLongitudeE6() / 1E6);  

	    		long distance = (long)locationA.distanceTo(locationB);

	    		data.putExtra(CMConstants.PARM_LATITUDE, center.getLatitudeE6() / 1E6);
	    		data.putExtra(CMConstants.PARM_LONGITUDE, center.getLongitudeE6() / 1E6);
	    		data.putExtra(CMConstants.PARM_RANGE, distance);
	    	}
	    }
	    setResult(RESULT_OK, data); 

	    super.finish();
	}

	public void showMarkers() {
		if (circleOverlay==null) {
			// Put the overlay markers on the screen and set their initial coordinates
			CircleOverlayItem center = new CircleOverlayItem(mapView.getMapCenter(), true);
			// Get the right edge coordiantes
			GeoPoint gpCenterRight = new GeoPoint(mapView.getMapCenter().getLatitudeE6(), mapView.getMapCenter().getLongitudeE6()+mapView.getLongitudeSpan()/4);
			CircleOverlayItem edge = new CircleOverlayItem(gpCenterRight, false);
			circleOverlay = new CircleOverlay(this, getResources().getDrawable(android.R.drawable.presence_online));
			circleOverlay.addOverlay(center);
			circleOverlay.addOverlay(edge);
			circleOverlay.setDrawCircle(true);
		}
		
		mapView.getOverlays().add(circleOverlay);
		mapView.postInvalidate();
	}
	
	  @Override
	  public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_S) {
	    	mapView.setSatellite(!mapView.isSatellite());
	      return(true);
	    }
	    else if (keyCode == KeyEvent.KEYCODE_Z) {
	    	mapView.displayZoomControls(true);
	      return(true);
	    }
	    
	    return(super.onKeyDown(keyCode, event));
	  }

	  private GeoPoint getPoint(double lat, double lon) {
	    return(new GeoPoint((int)(lat*1000000.0),
	                          (int)(lon*1000000.0)));
	  }	

	private void zoomToMyLocation() {
		if (!bMyLocationShowing)
			myLocationOverlay.enableMyLocation();
		
		myLocationOverlay.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				GeoPoint myLocationGeoPoint = myLocationOverlay.getMyLocation();
				if(myLocationGeoPoint != null) {
					mapView.getController().animateTo(myLocationGeoPoint);
					mapView.getController().setZoom(12);
				}
				else {
					Toast.makeText(CircleActivity.this, getResources().getString(R.string.No_Location), Toast.LENGTH_SHORT).show();
				}				
			}
			
		});
	}	

	protected boolean isRouteDisplayed() {
		return false;
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.creategamemapoptions, menu);
        
        return true;
    }    

    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId()==R.id.toggleSatellite)
    		mapView.setSatellite(!mapView.isSatellite());
    	if (item.getItemId()==R.id.toggleMarkers) {
    		if (mapView.getOverlays().contains(circleOverlay)) {
    			mapView.getOverlays().remove(circleOverlay);
    			circleOverlay = null;
    			mapView.postInvalidate();
    		}
    		else
    			showMarkers();
    	}
    	if (item.getItemId()==R.id.zoomLocation) {
    		zoomToMyLocation();
    		bMyLocationShowing = !bMyLocationShowing;
    	}
    	
        return true;
    }    	

}
