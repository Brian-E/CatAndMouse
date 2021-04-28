package com.catandmouse;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.TextView;

public class MyProgressDialog extends ProgressDialog {

	public MyProgressDialog(Context context) {
		super(context);
		setContentView(R.layout.progress_dialog);
	}

	public void setMessage(CharSequence message) {
		super.setMessage(message);
		
		TextView tvMsg = (TextView) findViewById(R.id.textView_ProgressDialog_Text);
		tvMsg.setText(message);
	}

	public void show(CharSequence message) {
		setMessage(message);
		super.show();
	}
	
	

}
