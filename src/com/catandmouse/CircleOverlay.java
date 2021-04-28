package com.catandmouse;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.openfeint.api.resource.User;

@SuppressWarnings("rawtypes")
public class CircleOverlay extends ItemizedOverlay {
	private ArrayList<CircleOverlayItem> mOverlays = new ArrayList<CircleOverlayItem>();
	Context mContext;
	Activity parent;
	boolean drawCircle;
	Drawable defaultMarker;
    private CircleOverlayItem inDrag=null;
    private ImageView dragCenterImage=null;
    private ImageView dragEdgeImage=null;
    private int xDragImageOffset=0;
    private int yDragImageOffset=0;
    private int xDragCenterTouchOffset=0;
    private int yDragCenterTouchOffset=0;
    private int xDragEdgeTouchOffset=0;
    private int yDragEdgeTouchOffset=0;
    private float pixels = 0;
    
	public CircleOverlay(Activity parent, Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
		this.parent = parent;
		populate();
		drawCircle = false;
		this.defaultMarker = defaultMarker;
	      dragCenterImage=(ImageView)parent.findViewById(R.id.drag);
	      dragCenterImage.setImageDrawable(defaultMarker);
	      xDragImageOffset=dragCenterImage.getDrawable().getIntrinsicWidth()/2;
	      yDragImageOffset=dragCenterImage.getDrawable().getIntrinsicHeight();		
	      dragCenterImage=(ImageView)parent.findViewById(R.id.drag);
	      dragCenterImage.setImageDrawable(defaultMarker);	      
	      dragEdgeImage=(ImageView)parent.findViewById(R.id.drag2);
	      dragEdgeImage.setImageDrawable(defaultMarker);
	      
	}
	
//	public CircleOverlay(Drawable defaultMarker, Context context) {
//		super(defaultMarker);
//		mContext = context;
//		drawCircle = false;
//		this.defaultMarker = defaultMarker;
//	      dragImage=(ImageView)findViewById(R.id.drag);
//	      xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
//	      yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();		
//	}	

	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	public int size() {
		return mOverlays.size();
	}
	
	public void addOverlay(CircleOverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
    	final int action=event.getAction();
    	final int x=(int)event.getX();
    	final int y=(int)event.getY();
    	boolean result=false;

    	if (action==MotionEvent.ACTION_DOWN) {
    		for (CircleOverlayItem item : mOverlays) {
    			Point p=new Point(0,0);

    			mapView.getProjection().toPixels(item.getPoint(), p);

    			if (hitTest(item, defaultMarker, x-p.x, y-p.y)) {
					// First, I need the current distance between my 2 points
					float distance = getMarkerDistance();
					pixels = mapView.getProjection().metersToEquatorPixels(distance);

					result=true;
    				inDrag=item;
    				if (item.isCenter())
    					mOverlays.removeAll(mOverlays);
    				else
    					mOverlays.remove(item);
    				populate();

    				if (item.isCenter()) {
    					xDragCenterTouchOffset=0;
    					yDragCenterTouchOffset=0;
    					xDragEdgeTouchOffset=0;
    					yDragEdgeTouchOffset=0;

    					setDragCenterImagePosition(p.x, p.y);
    					dragCenterImage.setVisibility(View.VISIBLE);

    					setDragEdgeImagePosition(p.x+(int)pixels, p.y);
    					dragEdgeImage.setVisibility(View.VISIBLE);

    					xDragCenterTouchOffset=x-p.x;
    					yDragCenterTouchOffset=y-p.y;
    					xDragEdgeTouchOffset=x-p.x+(int)pixels;
    					yDragEdgeTouchOffset=y-p.y;
    				}
    				else {
    					xDragEdgeTouchOffset=0;
    					yDragEdgeTouchOffset=0;

    					setDragEdgeImagePosition(p.x, p.y);
    					dragEdgeImage.setVisibility(View.VISIBLE);

    					xDragEdgeTouchOffset=x-p.x;
    					yDragEdgeTouchOffset=y-p.y;
    				}

    				break;
    			}
    		}
    	}
    	else if (action==MotionEvent.ACTION_MOVE && inDrag!=null) {
    		if (inDrag.isCenter()) {
    			setDragCenterImagePosition(x, y);
    			setDragEdgeImagePosition(x+(int)pixels, y);
    		}
    		else {
    			Point p=new Point(0,0);

    			mapView.getProjection().toPixels(inDrag.getPoint(), p);
    			setDragEdgeImagePosition(x, p.y);
    		}
    		result=true;
    	}
    	else if (action==MotionEvent.ACTION_UP && inDrag!=null) {
    		if (inDrag.isCenter()) {
    			dragCenterImage.setVisibility(View.GONE);
    			dragEdgeImage.setVisibility(View.GONE);

    			GeoPoint ptCenter=mapView.getProjection().fromPixels(x-xDragCenterTouchOffset,
    					y-yDragCenterTouchOffset);
    			GeoPoint ptEdge=mapView.getProjection().fromPixels(x-xDragEdgeTouchOffset,
    					y-yDragEdgeTouchOffset);
    			CircleOverlayItem center = new CircleOverlayItem(ptCenter, true);
    			CircleOverlayItem edge = new CircleOverlayItem(ptEdge, false);

    			mOverlays.add(center);
    			mOverlays.add(edge);
    		}
    		else {
    			dragEdgeImage.setVisibility(View.GONE);

    			GeoPoint ptEdge=mapView.getProjection().fromPixels(x-xDragEdgeTouchOffset,
    					y-yDragEdgeTouchOffset);
    			CircleOverlayItem edge = new CircleOverlayItem(ptEdge, false);

    			mOverlays.add(edge);
    		}

    		populate();

    		inDrag=null;
    		result=true;
    	}

    	return(result || super.onTouchEvent(event, mapView));
    }

	@Override
    public void draw(Canvas canvas, MapView mapView,
                      boolean shadow) {
		//if (drawCircle) {
			// Get the positions of the overlays and draw the circle
			Projection projection = mapView.getProjection();
			CircleOverlayItem center = null;
			CircleOverlayItem edge = null;
			if (mOverlays.size()>0) {
				for (CircleOverlayItem item : mOverlays) {
					if (item.isCenter())
						center = item;
					else
						edge = item;
				}
			}

			if (center!=null && edge!=null) {
				GeoPoint gpCenterPoint = center.getPoint();
				GeoPoint gpEdgePoint = edge.getPoint();
				Point pCenter = new Point();
				Point pEdge = new Point();
				projection.toPixels(gpCenterPoint, pCenter);
				projection.toPixels(gpEdgePoint, pEdge);
				float radius = (float) Math.sqrt(Math.pow(pCenter.x - pEdge.x, 2) + Math.pow(pCenter.y - pEdge.y, 2));
				drawCircle(canvas, pCenter, radius);
			}
			drawCircle = false;
		//}
		
	    super.draw(canvas, mapView, shadow);
    }	
    
    protected void drawCircle(Canvas canvas, Point curScreenCoords, float radius) {
        //curScreenCoords = toScreenPoint(curScreenCoords);
        // Draw inner info window
        canvas.drawCircle((float) curScreenCoords.x, (float) curScreenCoords.y, radius, getInnerPaint());
        // if needed, draw a border for info window
        canvas.drawCircle(curScreenCoords.x, curScreenCoords.y, radius, getBorderPaint());
    }

    public boolean isDrawCircle() {
		return drawCircle;
	}

	public void setDrawCircle(boolean drawCircle) {
		this.drawCircle = drawCircle;
	}

	private Paint innerPaint, borderPaint;

    public Paint getInnerPaint() {
        if (innerPaint == null) {
            innerPaint = new Paint();
            innerPaint.setARGB(225, 68, 89, 82); // gray
            innerPaint.setAntiAlias(true);
            innerPaint.setAlpha(100);
        }
        return innerPaint;
    }

    public Paint getBorderPaint() {
        if (borderPaint == null) {
            borderPaint = new Paint();
            borderPaint.setARGB(255, 68, 89, 82);
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Style.STROKE);
            borderPaint.setStrokeWidth(2);
        }
        return borderPaint;
    }    
    
    private void setDragCenterImagePosition(int x, int y) {
        RelativeLayout.LayoutParams lp=
          (RelativeLayout.LayoutParams)dragCenterImage.getLayoutParams();
              
        lp.setMargins(x-xDragImageOffset-xDragCenterTouchOffset,
                        y-yDragImageOffset-yDragCenterTouchOffset, 0, 0);
        dragCenterImage.setLayoutParams(lp);
      }    

    private void setDragEdgeImagePosition(int x, int y) {
        RelativeLayout.LayoutParams lp=
          (RelativeLayout.LayoutParams)dragEdgeImage.getLayoutParams();
              
        lp.setMargins(x-xDragImageOffset-xDragEdgeTouchOffset,
                        y-yDragImageOffset-yDragEdgeTouchOffset, 0, 0);
        dragEdgeImage.setLayoutParams(lp);
      } 
    
    private float getMarkerDistance() {
    	// Get the current map distance from my markers
    	GeoPoint centerPoint = null;
    	GeoPoint edgePoint = null;
    	for (CircleOverlayItem item : mOverlays) {
    		if (item.isCenter())
    			centerPoint = item.getPoint();
    		else
    			edgePoint = item.getPoint();
    	}
    	
    	if (centerPoint!=null && edgePoint!=null) {
    		Location locationA = new Location("point A");  

    		locationA.setLatitude(centerPoint.getLatitudeE6() / 1E6);  
    		locationA.setLongitude(centerPoint.getLongitudeE6() / 1E6);  

    		Location locationB = new Location("point B");  

    		locationB.setLatitude(edgePoint.getLatitudeE6() / 1E6);  
    		locationB.setLongitude(edgePoint.getLongitudeE6() / 1E6);  

    		return locationA.distanceTo(locationB);

    	}
    	
    	return 0;
    }
}
