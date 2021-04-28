package com.catandmouse;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class GameSettingsActivity extends Activity {
	SharedPreferences settings;
	CheckBox cbNormalNotification, cbNormalPlaySound, cbNormalVibrate, cbReverseNotification, cbReversePlaySound,
		cbReverseVibrate;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_options);

		settings = getSharedPreferences(CMConstants.SETTINGS, MODE_PRIVATE);
		//cbAutoDiscover = (CheckBox) findViewById(R.id.checkBox_Options_AutoDiscover);
		cbNormalNotification = (CheckBox) findViewById(R.id.checkBox_Options_SendNotificationCat);
		cbNormalPlaySound = (CheckBox) findViewById(R.id.checkBox_Options_PlaySoundBecomeCat);
		cbNormalVibrate = (CheckBox) findViewById(R.id.checkBox_Options_VibrateBecomeCat);
		cbReverseNotification = (CheckBox) findViewById(R.id.CheckBox_Options_SendNotificationMouse);
		cbReversePlaySound = (CheckBox) findViewById(R.id.CheckBox_Options_PlaySoundBecomeMouse);
		cbReverseVibrate = (CheckBox) findViewById(R.id.CheckBox_Options_VibrateBecomeMouse);

//		cbAutoDiscover.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_AUTO_DISCOVER, true));
//		cbAutoDiscover.setOnClickListener(new View.OnClickListener(){
//			public void onClick(View arg0) {
//				Editor edit = settings.edit();
//				edit.putBoolean(CMConstants.SETTING_GENERAL_AUTO_DISCOVER, cbAutoDiscover.isChecked());
//				edit.commit();
//			}
//		});
		
		cbNormalNotification.setChecked(settings.getBoolean(CMConstants.SETTING_NORMAL_CAT_NOTIFICATION, true));
		cbNormalNotification.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				boolean bChecked = cbNormalNotification.isChecked();
				cbNormalPlaySound.setEnabled(bChecked);
				cbNormalVibrate.setEnabled(bChecked);
				
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_NORMAL_CAT_NOTIFICATION, bChecked);
				edit.commit();
			}
		});
		
		cbNormalPlaySound.setChecked(settings.getBoolean(CMConstants.SETTING_NORMAL_CAT_SOUND, true));
		cbNormalPlaySound.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				boolean bChecked = cbNormalPlaySound.isChecked();
				
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_NORMAL_CAT_SOUND, bChecked);
				edit.commit();
			}			
		});
		
		cbNormalVibrate.setChecked(settings.getBoolean(CMConstants.SETTING_NORMAL_CAT_VIBRATE, true));
		cbNormalVibrate.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_NORMAL_CAT_VIBRATE, cbNormalVibrate.isChecked());
				edit.commit();
			}
		});	

		cbReverseNotification.setChecked(settings.getBoolean(CMConstants.SETTING_REVERSE_MOUSE_NOTIFICATION, true));
		cbReverseNotification.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				boolean bChecked = cbReverseNotification.isChecked();
				cbReversePlaySound.setEnabled(bChecked);
				cbReverseVibrate.setEnabled(bChecked);
				
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_REVERSE_MOUSE_NOTIFICATION, bChecked);
				edit.commit();
			}
		});
		
		cbReversePlaySound.setChecked(settings.getBoolean(CMConstants.SETTING_REVERSE_MOUSE_SOUND, true));
		cbReversePlaySound.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				boolean bChecked = cbReversePlaySound.isChecked();
				
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_REVERSE_MOUSE_SOUND, bChecked);
				edit.commit();
			}			
		});
		
		cbReverseVibrate.setChecked(settings.getBoolean(CMConstants.SETTING_REVERSE_MOUSE_VIBRATE, true));
		cbReverseVibrate.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_REVERSE_MOUSE_VIBRATE, cbReverseVibrate.isChecked());
				edit.commit();
			}
		});	
		
	}

}
