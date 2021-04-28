package com.catandmouse;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;

public class GeneralSettingsActivity extends Activity {
	SharedPreferences settings;
	CheckBox cbUseGPS, cbCatCatchNotification, cbCatCatchSound, cbCatCatchVibrate, cbCatchMouseNotification, cbCatchMouseVibrate, cbNoSounds;
	TimePicker tpStartTime, tpEndTime;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.general_options);
		
		settings = getSharedPreferences(CMConstants.SETTINGS, MODE_PRIVATE);
		
		//cbUseGPS = (CheckBox) findViewById(R.id.checkBox_Options_UseGPS);
		cbCatCatchNotification = (CheckBox) findViewById(R.id.checkBox_Options_CatCatchNotification);
		cbCatCatchSound = (CheckBox) findViewById(R.id.checkBox_Options_CatCatchSound);
		cbCatCatchVibrate = (CheckBox) findViewById(R.id.checkBox_Options_CatCatchVibrate);
		cbCatchMouseNotification = (CheckBox) findViewById(R.id.checkBox_Options_CatchMouseNotification);
		cbCatchMouseVibrate = (CheckBox) findViewById(R.id.checkBox_Options_CatchMouseVibrate);
		cbNoSounds = (CheckBox) findViewById(R.id.checkBox_Options_TurnOffSounds);
		tpStartTime = (TimePicker) findViewById(R.id.timePicker_Options_SoundsOffStartTime);
		tpEndTime = (TimePicker) findViewById(R.id.timePicker_Options_SoundsOffEndTime);
		
//		cbUseGPS.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_USE_GPS, true));
//		cbUseGPS.setOnClickListener(new View.OnClickListener(){
//			public void onClick(View arg0) {
//				Editor edit = settings.edit();
//				edit.putBoolean(CMConstants.SETTING_GENERAL_USE_GPS, cbUseGPS.isChecked());
//				edit.commit();
//			}
//		});
		
		cbCatCatchNotification.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_CAUGHT_NOTIFICATION, true));
		cbCatCatchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cbCatCatchSound.setEnabled(isChecked);
				cbCatCatchVibrate.setEnabled(isChecked);
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_GENERAL_CAUGHT_NOTIFICATION, isChecked);
				edit.commit();
			}
		});
		
		cbCatCatchSound.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_CAUGHT_SOUND, true));
		cbCatCatchSound.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_GENERAL_CAUGHT_SOUND, cbCatCatchSound.isChecked());
				edit.commit();
			}
		});
		
		cbCatCatchVibrate.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_CAUGHT_VIBRATE, true));
		cbCatCatchVibrate.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_GENERAL_CAUGHT_VIBRATE, cbCatCatchVibrate.isChecked());
				edit.commit();
			}
		});
		
		cbCatchMouseNotification.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_CATCH_MOUSE_NOTIFICATION, true));
		cbCatchMouseNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cbCatchMouseVibrate.setEnabled(isChecked);
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_GENERAL_CATCH_MOUSE_VIRBRATE, cbCatchMouseVibrate.isChecked());
				edit.commit();
			}
		});
		
		cbCatchMouseVibrate.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_CATCH_MOUSE_VIRBRATE, true));
		cbCatchMouseVibrate.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_GENERAL_CATCH_MOUSE_VIRBRATE, cbCatchMouseVibrate.isChecked());
				edit.commit();
			}
		});
		
		cbNoSounds.setChecked(settings.getBoolean(CMConstants.SETTING_GENERAL_SOUNDS_OFF, true));
		cbNoSounds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor edit = settings.edit();
				edit.putBoolean(CMConstants.SETTING_GENERAL_SOUNDS_OFF, isChecked);
				edit.commit();
				
				tpStartTime.setEnabled(isChecked);
				tpEndTime.setEnabled(isChecked);
			}
		});
		
		int iStartTimeHour = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_START_TIME_HOUR, 20);
		int iStartTimeMin = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_START_TIME_MIN, 0);
		tpStartTime.setCurrentHour(iStartTimeHour);
		tpStartTime.setCurrentMinute(iStartTimeMin);
		tpStartTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				Editor edit = settings.edit();
				edit.putInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_START_TIME_HOUR, hourOfDay);
				edit.putInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_START_TIME_MIN, minute);
				edit.commit();
			}
		});
		
		int iEndTimeHour = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_END_TIME_HOUR, 8);
		int iEndTimeMin = settings.getInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_END_TIME_MIN, 0);
		tpEndTime.setCurrentHour(iEndTimeHour);
		tpEndTime.setCurrentMinute(iEndTimeMin);
		tpEndTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				Editor edit = settings.edit();
				edit.putInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_END_TIME_HOUR, hourOfDay);
				edit.putInt(CMConstants.SETTING_GENERAL_SOUNDS_OFF_END_TIME_MIN, minute);
				edit.commit();
			}
		});
	}

}
