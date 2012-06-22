package com.ideasandroid.itreader;

import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.commonsware.cwac.cache.WebImageCache;
import com.ideasandroid.itreader.provider.NetworkStateCheckUtil;
import com.ideasandroid.itreader.provider.RefreshDataService;
import com.ideasandroid.rsslib4android.RSSFactory;
import com.ideasandroid.rsslib4android.RSSItem;

public class IdeasItReaderNew extends ListActivity {

	ProgressDialog progressdialog = null;
	private final int ABOUT_MENU = 1;
	private final int EXIT_MENU = 2;
	private final int SETTINGS_MENU = 3;
	private final int REFRESH_MENU = 4;
	SpeechListAdapter adapt = null;
	private final static int DELETE_ID = 2;
	private final static int VIEW_ID = 1;
	private final static int MARKED_AS_ALLREDEAD = 3;
	private final static int SHARE = 100;
	private WebView googleAdsWebView = null;
	
	private boolean isRefresh=false;

	// ListView rssList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainnew);
		googleAdsWebView = (WebView) findViewById(R.id.googleAdsWebView);
		// getWindow().setLayout(100, 200);
		registerForContextMenu(getListView());
		progressdialog = new ProgressDialog(this);
		if (isRunning("com.ideasandroid.itreader.service.NewRSSNotifyService")) {
			stopService(new Intent(
					"com.ideasandroid.itreader.service.NewRSSNotifyService"));
		}
		boolean isInited = getSharedPreferences("ideasrss",
				Context.MODE_PRIVATE).getBoolean("isInited", false);
		if (!isInited) {
			if (NetworkStateCheckUtil.isNetworkAvailable(this)) {
				progressdialog.setMessage(getText(R.string.loading_init));
				new SystemInitTask(progressdialog, this).execute();
			} else {
				progressdialog.setMessage("暂无网络连接！请在有网络连接时使用本软件！");
				progressdialog.setCancelable(true);
				progressdialog.setButton(getText(R.string.process_cancl),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								android.os.Process
										.killProcess(android.os.Process.myPid());
							}
						});
				progressdialog.show();
			}
		} else {
			progressdialog.setMessage(getText(R.string.loading_data));
			new SystemInitTask(progressdialog, this).execute();
		}
		boolean adsshow = getSharedPreferences("ideasrss",
				Context.MODE_PRIVATE).getBoolean("adsshow", false);
		Editor edit = getSharedPreferences("ideasrss",
				Context.MODE_PRIVATE).edit();
		edit.putBoolean("adsshow", !adsshow);
		edit.commit();
		googleAdsWebView.getSettings().setJavaScriptEnabled(true);
		googleAdsWebView
					.loadUrl("http://ideasapi.sinaapp.com/ad/googleads.html?timestamp="+new Date().getTime());

	}
	
	private WebImageCache getCache() {
        return(((Application)getApplication()).getCache());     
    }

	private boolean isRunning(String serviceName) {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> mServiceList = mActivityManager
				.getRunningServices(30);
		for (int i = 0; i < mServiceList.size(); i++) {
			if (serviceName.equals(mServiceList.get(i).service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void startBGService() {
		// 1小时从服务器上获取一次最新的RSS
		long firstime = SystemClock.elapsedRealtime() + 60 * 1000 * 60;
		Intent refreshDataIntent = new Intent(IdeasItReaderNew.this,
				RefreshDataService.class);
		PendingIntent mAlarmSender = PendingIntent.getService(
				IdeasItReaderNew.this, 0, refreshDataIntent, 0);
		// Schedule the alarm!
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0,
				refreshDataIntent, PendingIntent.FLAG_NO_CREATE);
		if (sender != null) {
			am.cancel(mAlarmSender);
		}
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,
				1 * 60 * 60 * 1000, mAlarmSender);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		Log.d("ItemClick:", "ItemClicked");
		((SpeechListAdapter) getListAdapter()).toggle(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_MENU, 0, R.string.appMenu_about).setIcon(
				R.drawable.menu_icon_about);
		menu.add(0, SETTINGS_MENU, 0, R.string.appMenu_settings).setIcon(
				R.drawable.menu_icon_settings);
		menu.add(0, REFRESH_MENU, 0, R.string.appMenu_refresh).setIcon(
                R.drawable.menu_icon_refresh);
		menu.add(0, EXIT_MENU, 0, R.string.appMenu_exit).setIcon(
				R.drawable.menu_icon_exit);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ABOUT_MENU:
			Intent ab = new Intent(this, AboutActivity.class);
			ab.putExtra("tabIndex", 0);
			startActivity(ab);
			return true;
		case SETTINGS_MENU:
			Intent st = new Intent(this, SettingsActivity.class);
			startActivity(st);
			return true;
		case REFRESH_MENU:
		    isRefresh=true;
		    progressdialog=new ProgressDialog(this);
		    progressdialog.setMessage(getText(R.string.loading_data));
            new SystemInitTask(progressdialog, this).execute();
            return true;
		case EXIT_MENU:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle("操作");
		menu.add(0, VIEW_ID, 0, "查看全文");
		menu.add(0, MARKED_AS_ALLREDEAD, 0, "全部标记为已读");
		menu.add(0, DELETE_ID, 0, "删除");
		menu.add(0, SHARE, 0, "分享新闻");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		RSSItem rssItem = (RSSItem) getListAdapter().getItem(info.position);
		Log.d("selectedItemTitel:", rssItem.getTitle());

		switch (item.getItemId()) {
		case VIEW_ID:
			Intent i = new Intent(this, WebActivity.class);
			i.putExtra("targetUrl", rssItem.getLink());
			startActivity(i);
			return true;
		case DELETE_ID:
			getContentResolver().delete(
					Uri.withAppendedPath(RSSItem.CONTENT_URL,
							String.valueOf(rssItem.getId())), null, null);
			adapt.initRss();
			return true;
		case MARKED_AS_ALLREDEAD:
			ContentValues v = new ContentValues();
			v.put(RSSItem.ISREADED, 1);
			getContentResolver().update(RSSItem.CONTENT_URL, v, null, null);
			adapt.initRss();
			return true;
		case SHARE:
			Intent it = new Intent(Intent.ACTION_SEND);   
			it.putExtra(Intent.EXTRA_TEXT,"您好，这个新闻不错，看一看吧！"+rssItem.getLink());   
			it.setType("text/plain");   
			startActivity(Intent.createChooser(it, "分享方式"));
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	private class SpeechListAdapter extends BaseAdapter {
		private Cursor cursor;
		private boolean[] mExpanded;

		public SpeechListAdapter(Context context) {
			initRss();
		}

		public void initRss() {
			RSSFactory f = new RSSFactory();
			Cursor mcursor = f.getRssCursor(getContentResolver(),
					RSSItem.TYPEID + " = '" + 1 + "'");
			startManagingCursor(mcursor);
			this.cursor = mcursor;
			int count = cursor.getCount();
			mExpanded = new boolean[count];
			initMExpanded(count + 1);
			notifyDataSetChanged();
		}

		public void refreshRss() {
			RSSFactory f = new RSSFactory();
			Cursor mcursor = f.getRssCursor(getContentResolver(),
					RSSItem.TYPEID + " = '" + 1 + "'");
			startManagingCursor(mcursor);
			this.cursor = mcursor;
			notifyDataSetChanged();
		}


		public int getCount() {
			return cursor.getCount();
		}


		public Object getItem(int position) {
			cursor.moveToPosition(position);
			int titleIndex = cursor.getColumnIndex(RSSItem.TITLE);
			int descriptionIndex = cursor.getColumnIndex(RSSItem.DESCRIPTION);
			int readedIndex = cursor.getColumnIndex(RSSItem.ISREADED);
			int idIndex = cursor.getColumnIndex(RSSItem._ID);
			int typeIdIndex = cursor.getColumnIndex(RSSItem.TYPEID);
			int linkIndex = cursor.getColumnIndex(RSSItem.LINK);
			int channelIdIndex = cursor.getColumnIndex(RSSItem.CHANNELID);
			int pubDateIndex = cursor.getColumnIndex(RSSItem.PUBDATE);

			RSSItem item = new RSSItem();
			item.setTitle(cursor.getString(titleIndex));
			item.setDescription(cursor.getString(descriptionIndex));
			item.setLink(cursor.getString(linkIndex));
			item.setChannelId(cursor.getInt(channelIdIndex));
			item.setTypeId(typeIdIndex);
			item.setPubDate(cursor.getString(pubDateIndex));
			item.setId(cursor.getInt(idIndex));
			item.setIsReaded(cursor.getInt(readedIndex));
			return item;
		}


		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			RSSItem item = (RSSItem) getItem(position);
			LayoutInflater mInflater = LayoutInflater
					.from(IdeasItReaderNew.this);
			View sv;
			ViewHolder vh;
			if (convertView == null) {
				sv = mInflater.inflate(R.layout.speechview, null);
				vh = new ViewHolder(sv);
				sv.setTag(vh);
			} else {
				sv = (View) convertView;
				vh = (ViewHolder) sv.getTag();
			}
			vh.getRssTitle().setText(Html.fromHtml(item.getTitle()));
			vh.getRssContent().setText(Html.fromHtml(item.getDescription()));
			vh.getRssContent().setVisibility(
					mExpanded[position] ? View.VISIBLE : View.GONE);
			if (item.getIsReaded() == 1) {
				vh.getStatusIcon().setImageResource(R.drawable.rss_readed);
			} else {
				vh.getStatusIcon().setImageResource(R.drawable.rss_noreaded);
			}
			return sv;
		}
		

		private void initMExpanded(int position) {
			for (int i = 0; i < mExpanded.length; i++) {
				if (i != position) {
					mExpanded[i] = false;
				}
			}
		}

		public void toggle(int position) {
			initMExpanded(position);
			mExpanded[position] = !mExpanded[position];
			RSSItem item = (RSSItem) getItem(position);
			ContentValues v = new ContentValues();
			v.put(RSSItem.ISREADED, 1);
			getContentResolver().update(
					Uri.withAppendedPath(RSSItem.CONTENT_URL,
							String.valueOf(item.getId())), v, null, null);
			refreshRss();
		}

		/**
		 * Remember our context so we can use it when constructing views.
		 */
		class ViewHolder {
			private View v = null;

			public ViewHolder(View v) {
				this.v = v;
			}

			TextView rssTitle = null;
			TextView rssContent = null;
			ImageView statusIcon = null;

			public TextView getRssTitle() {
				if (rssTitle == null) {
					rssTitle = (TextView) v.findViewById(R.id.rssTitle);
				}
				return rssTitle;
			}

			public TextView getRssContent() {
				if (rssContent == null) {
					rssContent = (TextView) v.findViewById(R.id.rssContent);
				}
				return rssContent;
			}

			public ImageView getStatusIcon() {
				if (statusIcon == null) {
					statusIcon = (ImageView) v.findViewById(R.id.statusicon);
				}
				return statusIcon;
			}

		}

	}

	public class SystemInitTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressdialog = null;
		private Context context = null;

		public SystemInitTask(ProgressDialog progressdialog, Context context) {
			super();
			this.progressdialog = progressdialog;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			if (progressdialog != null) {
				progressdialog.setCancelable(true);
				progressdialog.setButton(
						context.getText(R.string.process_cancl),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								SystemInitTask.this.cancel(true);
								dialog.cancel();
							}
						});
				progressdialog.show();
			}
			System.gc();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			RSSFactory f = new RSSFactory();
			boolean isInited = context.getSharedPreferences("ideasrss",
					Context.MODE_PRIVATE).getBoolean("isInited", false);
			if (!isInited) {
				ContentResolver cr = context.getContentResolver();
				f.initSystemRssConfig(cr);
				f.init(context);
				Editor edit = context.getSharedPreferences("ideasrss",
						Context.MODE_PRIVATE).edit();
				edit.putBoolean("isInited", true);
				edit.commit();
				startBGService();
			}
			if(isRefresh){
			    f.init(context);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (progressdialog.isShowing()) {
				progressdialog.cancel();
				progressdialog=null;
			}
			isRefresh=false;
			adapt = new SpeechListAdapter(context);
			setListAdapter(adapt);
		}

	}
}