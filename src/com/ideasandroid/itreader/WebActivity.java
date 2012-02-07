package com.ideasandroid.itreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;

public class WebActivity extends Activity {

    WebView webview;
    
    private String targetUrl="";
    private String shareTitle="";

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
	targetUrl = intent.getStringExtra("targetUrl");
	shareTitle=intent.getStringExtra("shareTitle");
	webview.loadUrl(targetUrl);
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.webwiew_menu, menu);
    	// Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        actionProvider.setShareIntent(createShareIntent());

        // Set file with share history to the provider and set the share intent.
        MenuItem overflowItem = menu.findItem(R.id.menu_item_share_action_provider_overflow);
        ShareActionProvider overflowProvider =
            (ShareActionProvider) overflowItem.getActionProvider();
        overflowProvider.setShareHistoryFileName(
            ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        overflowProvider.setShareIntent(createShareIntent());

        return true;
    }
    
    private Intent createShareIntent() {
    	Intent it = new Intent(Intent.ACTION_SEND);   
		it.putExtra(Intent.EXTRA_TEXT,shareTitle+targetUrl+"[IT新闻阅读器推荐资讯]");   
		it.setType("text/plain");
        return it;
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
