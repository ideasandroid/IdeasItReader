package com.ideasandroid.itreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {

    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_PROGRESS);
	setContentView(R.layout.webbrowse);
	Intent intent = getIntent();
	webview = (WebView) findViewById(R.id.mbtWebWindow);
	webview.setWebViewClient(new HotelOrderWebViewClient());
	webview.setWebChromeClient(new WebChromeClient() {

	    @Override
	    public void onProgressChanged(WebView view, int newProgress) {
		setProgress(newProgress * 100);
	    }

	});
	webview.getSettings().setJavaScriptEnabled(true);
	WebSettings webSettings = webview.getSettings();
	webSettings.setSupportZoom(true);
	webSettings.setBuiltInZoomControls(true);
	Log.d(WebActivity.class.getName(), intent.getStringExtra("targetUrl"));
	webview.loadUrl(intent.getStringExtra("targetUrl"));
    }

    private class HotelOrderWebViewClient extends WebViewClient {
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    view.loadUrl(url);
	    return true;
	}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	    webview.goBack();
	    return true;
	} else if ((keyCode == KeyEvent.KEYCODE_BACK) && !webview.canGoBack()) {
	    // When the user center presses, let them pick a contact.
	    this.finish();
	}
	return super.onKeyDown(keyCode, event);
    }
}
