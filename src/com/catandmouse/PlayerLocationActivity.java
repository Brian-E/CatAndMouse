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

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.google.myjson.Gson;
import com.google.myjson.reflect.TypeToken;

public class PlayerLocationActivity extends MapActivity {
	PlayerLocationOverlay catOverlay;
	PlayerLocationOverlay mouseOverlay;
	MyLocationOverlay myLocationOverlay;
	MapView mapView;
	int gameNumber;
	String gameName;
	Timer timer;
	Drawable catDrawable;
	Drawable mouseDrawable;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		LogUtil.info("PlayerMap onCreate()");
		setContentView(R.layout.player_location);
		mapView = (MapView) findViewById(R.id.mapview_PlayerLocationView);
		mapView.setBuiltInZoomControls(true);

		Intent intent = this.getIntent();
		if (intent!=null) {
			gameNumber = intent.getIntExtra(CMConstants.PARM_GAME_NUMBER, 0);
			gameName = intent.getStringExtra(CMConstants.PARM_GAME_NAME);
		}
		// Get our players
		//mapView.get

		List<Overlay> mapOverlays = mapView.getOverlays();

		myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
		mapOverlays.add(myLocationOverlay);

		//	    mapView.setOnTouchListener(new OnTouchListener(){
		//
		//			@Override
		//			public boolean onTouch(View arg0, MotionEvent me) {
		//				LogUtil.info("Playermap onTouch MotionEvent="+me.toString());
		//				if (me.equals(MotionEvent.ACTION_UP)) {
		//					LogUtil.info("Player map action up");
		//					getPlayerLocations();
		//				}
		//				return false;
		//			}
		//	    });

		catDrawable = this.getResources().getDrawable(R.drawable.ic_stat_notify_cat);
		mouseDrawable = this.getResources().getDrawable(R.drawable.ic_stat_notify_mouse);
//		catOverlay = new PlayerLocationOverlay(this,catDrawable,gameName);	    
//		mouseOverlay = new PlayerLocationOverlay(this,mouseDrawable,gameName);	
		//setupTimer();
		// call convenience method that zooms map on our location
		//zoomToMyLocation();	    
		//mapView.postInvalidate();	    
	}
	
	private void setupTimer() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				getPlayerLocations();
			}

		}, 10000, 30000);		
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

	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableCompass();
		if (timer!=null)
			timer.cancel();
	}

	protected void onResume() {
		super.onResume();
		LogUtil.info("PlayerMap onResume()");
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		zoomToMyLocation();	    
		setupTimer();

		//		myLocationOverlay.runOnFirstFix(new Runnable(){
		//
		//			public void run() {
		//				if (myLocationOverlay.getMyLocation()!=null) {
		//					mapController.animateTo(myLocationOverlay.getMyLocation());
		//					//mapController.setZoom(15);
		//				}
		//				else
		//					LogUtil.warn("myLocationOverlay.getMyLocation() is null");
		//			}
		//			
		//		});
	}

	private void zoomToMyLocation() {
		myLocationOverlay.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				LogUtil.info("PlayerMap zoomToMyLocation() run()");
				GeoPoint myLocationGeoPoint = myLocationOverlay.getMyLocation();
				if(myLocationGeoPoint != null) {
					mapView.getController().animateTo(myLocationGeoPoint);
					mapView.getController().setZoom(12);
					mapView.postInvalidate();
				}
				else {
					Toast.makeText(PlayerLocationActivity.this, getResources().getString(R.string.No_Location), Toast.LENGTH_SHORT).show();
				}				
				getPlayerLocations();
			}

		});
	}	

	protected boolean isRouteDisplayed() {
		return false;
	}

	private void getPlayerLocations() {
		Vector<NameValuePair> vars = new Vector<NameValuePair>();
		vars.add(new BasicNameValuePair(CMConstants.PARM_GAME_NUMBER, Integer.toString(gameNumber)));
		vars.add(new BasicNameValuePair(CMConstants.PARM_CENTER_LATITUDE, Double.toString(((double)mapView.getMapCenter().getLatitudeE6())/1000000)));
		vars.add(new BasicNameValuePair(CMConstants.PARM_CENTER_LONGITUDE, Double.toString(((double)mapView.getMapCenter().getLongitudeE6())/1000000)));
		Projection projection = mapView.getProjection();
		GeoPoint gpTopLeft = projection.fromPixels(0, 0);
		vars.add(new BasicNameValuePair(CMConstants.PARM_CORNER_LATITUDE, Double.toString(((double)gpTopLeft.getLatitudeE6())/1000000)));
		vars.add(new BasicNameValuePair(CMConstants.PARM_CORNER_LONGITUDE, Double.toString(((double)gpTopLeft.getLongitudeE6())/1000000)));

		String url = ServerUtil.PLAYER_LOCATIONS_URL+"?"+URLEncodedUtils.format(vars, null);
		HttpGet request = new HttpGet(url);
		HttpClient client = ServerUtil.getHttpClient();
		try {
			HttpResponse resp = client.execute(request);
			int rc = resp.getStatusLine().getStatusCode();
			if (rc==0) {
				LogUtil.info("no notifications");
			}
			else if (rc!=HttpStatus.SC_OK) {
				// whatever, log it
				LogUtil.error("Error returned from get notifications. rc= "+rc);						
			}
			else {
				// Process the response
				Reader reader = new InputStreamReader(resp.getEntity().getContent());
				Gson gson = new Gson();
				Type listType = new TypeToken<List<PlayerBean>>(){}.getType();
				List<PlayerBean> beanList = gson.fromJson(reader, listType);
				LogUtil.info("PlayerMap getPlayerLocations() playerlist size="+beanList.size());
				// Clear off the current overlays
				List<Overlay> mapOverlays = mapView.getOverlays();
				mapOverlays.remove(catOverlay);
				mapOverlays.remove(mouseOverlay);
				mapOverlays.clear();
				catOverlay = new PlayerLocationOverlay(PlayerLocationActivity.this,catDrawable,gameName);	    
				mouseOverlay = new PlayerLocationOverlay(PlayerLocationActivity.this,mouseDrawable,gameName);	
				mapOverlays.add(myLocationOverlay);
				for (int i=0; i < beanList.size(); i++) {
					PlayerBean bean = beanList.get(i);
					// make sure its not me
					if (!((CMApplication)getApplicationContext()).user.userID().equals(bean.getPlayerId())) {
						// put this jamoke on the map
						OverlayItem oi = new PlayerOverlayItem(bean);
						if (bean.isCat())
							catOverlay.addOverlay(oi);
						else
							mouseOverlay.addOverlay(oi);
					}
				}
				LogUtil.info("PlayerMap getPlayerLocations() adding overlays");
				// refresh the map?
				mapOverlays.add(catOverlay);
				mapOverlays.add(mouseOverlay);
				mapView.postInvalidate();
			} 
		}		
		catch (ClientProtocolException e) {
			LogUtil.error(e.toString());
		}
		catch (IOException e) {
			LogUtil.error(e.toString());
		}		

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.playermapoptions, menu);

		return true;
	}    

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==R.id.toggleSatellite)
			mapView.setSatellite(!mapView.isSatellite());
		else if (item.getItemId()==R.id.Refresh)
			getPlayerLocations();
		else if (item.getItemId()==R.id.zoomLocation)
			zoomToMyLocation();
		return true;
	}    	

}
