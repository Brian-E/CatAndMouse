package com.catandmouse;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashActivity extends CMActivity {
	AnimationDrawable catAnimation, mouseAnimation;
	TextView tvPro;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		tvPro = (TextView) findViewById(R.id.textView_Splash_Pro);
		if (((CMApplication)getApplicationContext()).isPro())
			tvPro.setText(getResources().getString(R.string.Pro));
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){

			public void run() {
				startActivity(new Intent(SplashActivity.this, MainMenuActivity.class));
				SplashActivity.this.finish();
			}
			
		}, 5000);
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout_Splash);
		//Drawable drawCat = getResources().getDrawable(R.drawable.cat_splash);
		//Drawable drawMouse = getResources().getDrawable(R.drawable.mouse_splash);
		//Display display = getWindowManager().getDefaultDisplay(); 
		
		// background
		ImageView background = (ImageView) findViewById(R.id.imageView_Splash_Background);
		Animation animScroll = AnimationUtils.loadAnimation(this, R.anim.splash_background_anim);
		background.startAnimation(animScroll);
		
		ImageView background2 = (ImageView) findViewById(R.id.ImageView_Splash_Background2);
		Animation animScroll2 = AnimationUtils.loadAnimation(this, R.anim.splash_background_anim2);
		background2.startAnimation(animScroll2);
		
		// Cat
		 // Get the background, which has been compiled to an AnimationDrawable object.
		ImageView catView = (ImageView) findViewById(R.id.imageView_Splash_Cat);
		catView.setBackgroundResource(R.drawable.cat_splash_anim);
		catAnimation = (AnimationDrawable) catView.getBackground();
		
		// Mouse
		ImageView mouseView = (ImageView) findViewById(R.id.imageView_Splash_Mouse);
		mouseView.setBackgroundResource(R.drawable.mouse_splash_anim);
		mouseAnimation = (AnimationDrawable) mouseView.getBackground();

		
//		GIFView catView = new GIFView(this, R.drawable.cat_splash);
//		RelativeLayout.LayoutParams catParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//		catParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		catParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		catParams.addRule(RelativeLayout.BELOW, R.id.textView_Splash_Cat);
//		catView.setLayoutParams(catParams);
//		catView.setLocation(0, display.getHeight()-drawCat.getMinimumHeight()-20);
		//rl.addView(catView);//, catParams);
//		
//		// mouse
		//GIFView mouseView = new GIFView(this, R.drawable.mouse_splash);
//		RelativeLayout.LayoutParams mouseParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//		mouseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		mouseParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		mouseParams.addRule(RelativeLayout.BELOW, R.id.textView_Splash_Mouse);
//		mouseParams.addRule(RelativeLayout.RIGHT_OF, catView.getId());
//		mouseView.setLayoutParams(mouseParams);
		//mouseView.setLocation(display.getWidth()-drawMouse.getMinimumWidth()-10, display.getHeight()-drawMouse.getMinimumHeight()-20);
		//rl.addView(mouseView);//, mouseParams);
		
		// Header
		LinearLayout header = (LinearLayout) findViewById(R.id.linearLayout_Splash_Header);
		rl.bringChildToFront(header);
		
//		
		
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams();
//		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		this.addContentView(gv, params);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			 // Start the animation (looped playback by default).
			 catAnimation.start();
			 mouseAnimation.start();
		}
			
	}
	
	@Override
	protected void registerReceiver() {
		super.registerReceiver();

		IntentFilter filter = new IntentFilter(CMIntentActions.ACTION_PRO_CHECK);
		this.registerReceiver(myReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(myReceiver);
	}	
	
	
	protected final BroadcastReceiver myReceiver  = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(CMIntentActions.ACTION_PRO_CHECK)) {
				if (((CMApplication)getApplicationContext()).isPro()) {
					tvPro.setText(getResources().getString(R.string.Pro));
				}
			}
		}
	};

}
