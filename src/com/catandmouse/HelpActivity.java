package com.catandmouse;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		WebView wv = (WebView) findViewById(R.id.webView1);
		wv.loadUrl(ServerUtil.HELP_URL);
	}

}
