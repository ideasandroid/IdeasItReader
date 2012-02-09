package com.ideasandroid.itreader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.commonsware.cwac.cache.WebImageCache;
import com.ideasandroid.itreader.provider.NetworkStateCheckUtil;
import com.ideasandroid.itreader.provider.RefreshDataService;
import com.ideasandroid.rsslib4android.RSSFactory;
import com.ideasandroid.rsslib4android.RSSItem;

public class IdeasItReaderNew extends ListActivity {

	ProgressDialog progressdialog = null;
	SpeechListAdapter adapt = null;
	private boolean isRefresh=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainnew);
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
				progressdialog.setMessage(getString(R.string.net_exception));
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
		
		getActionBar().setHomeButtonEnabled(true);
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
		// get news from server 1times/hour
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
		((SpeechListAdapter) getListAdapter()).toggle(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.main_list_menu, menu);
		    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			Intent ab = new Intent(this, AboutActivity.class);
			ab.putExtra("tabIndex", 0);
			startActivity(ab);
			return true;
		case R.id.menu_setting:
			Intent st = new Intent(this, SettingsActivity.class);
			startActivity(st);
			return true;
		case R.id.menu_refresh:
		    isRefresh=true;
		    progressdialog=new ProgressDialog(this);
		    progressdialog.setMessage(getText(R.string.loading_data));
                    new SystemInitTask(progressdialog, this).execute();
                    return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		 MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.menu.main_list_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		RSSItem rssItem = (RSSItem) getListAdapter().getItem(info.position);
		Log.d("selectedItemTitel:", rssItem.getTitle());

		switch (item.getItemId()) {
		case R.id.cmenu_view_all_content:
			Intent i = new Intent(this, WebActivity.class);
			i.putExtra("targetUrl", rssItem.getLink());
			i.putExtra("shareTitle", rssItem.getTitle());
			startActivity(i);
			return true;
		case R.id.cmenu_delete:
			getContentResolver().delete(
					Uri.withAppendedPath(RSSItem.CONTENT_URL,
							String.valueOf(rssItem.getId())), null, null);
			adapt.initRss();
			return true;
		case R.id.cmenu_mark_all_readed:
			ContentValues v = new ContentValues();
			v.put(RSSItem.ISREADED, 1);
			getContentResolver().update(RSSItem.CONTENT_URL, v, null, null);
			adapt.initRss();
			return true;
		case R.id.cmenu_share:
			Intent it = new Intent(Intent.ACTION_SEND);   
			it.putExtra(Intent.EXTRA_TEXT,rssItem.getTitle()+"->"+rssItem.getLink()+getString(R.string.share_itnewsreader_info));   
			it.setType("text/plain");   
			startActivity(Intent.createChooser(it,getString(R.string.share_type)));
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
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void onNothingSelected(AdapterView<?> arg0) {

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
			URLImageParser p = new URLImageParser(vh.getRssContent(), IdeasItReaderNew.this);
			vh.getRssContent().setText(Html.fromHtml(item.getDescription(),p,null));
			vh.getRssContent().setVisibility(
					mExpanded[position] ? View.VISIBLE : View.GONE);
			if (item.getIsReaded() == 1) {
				vh.getStatusIcon().setImageResource(R.drawable.rss_readed);
			} else {
				vh.getStatusIcon().setImageResource(R.drawable.rss_noreaded);
			}
			return sv;
		}
		
		ImageGetter imgGetter = new Html.ImageGetter() {
			public Drawable getDrawable(String source) {
				Drawable drawable = null;
				Log.d("Image Path", source);
				URL url;
				try {
					url = new URL(source);
					drawable = Drawable.createFromStream(url.openStream(), "");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return drawable;
			}
		};
		
		public class URLDrawable extends BitmapDrawable {
		    // the drawable that you need to set, you could set the initial drawing
		    // with the loading image if you need to
		    protected Drawable drawable = null;

		    @Override
		    public void draw(Canvas canvas) {
		        // override the draw to facilitate refresh function later
		        if(drawable != null) {
		            drawable.draw(canvas);
		        }
		    }
		}
		
		public class URLImageParser implements ImageGetter {
		    Context c;
		    View container;
		    public URLImageParser(View t, Context c) {
		        this.c = c;
		        this.container = t;
		    }

		    public Drawable getDrawable(String source) {
		        URLDrawable urlDrawable = new URLDrawable();
		        urlDrawable.drawable = getResources().getDrawable(R.drawable.icon);
		        ImageGetterAsyncTask asyncTask = 
		            new ImageGetterAsyncTask( urlDrawable);
		        asyncTask.execute(source);
		        return urlDrawable;
		    }

		    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
		        URLDrawable urlDrawable;

		        public ImageGetterAsyncTask(URLDrawable d) {
		            this.urlDrawable = d;
		        }

		        @Override
		        protected Drawable doInBackground(String... params) {
		            String source = params[0];
		            return fetchDrawable(source);
		        }

		        @Override
		        protected void onPostExecute(Drawable result) {
		            // set the correct bound according to the result from HTTP call
		            urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0 
		                    + result.getIntrinsicHeight()); 

		            // change the reference of the current drawable to the result
		            // from the HTTP call
		            urlDrawable.drawable = result;

		            // redraw the image by invalidating the container
		            URLImageParser.this.container.invalidate();
		        }

		        /***
		         * Get the Drawable from URL
		         * @param urlString
		         * @return
		         */
		        public Drawable fetchDrawable(String urlString) {
		            try {
		                InputStream is = fetch(urlString);
		                Drawable drawable = Drawable.createFromStream(is, "src");
		                drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0 
		                      + drawable.getIntrinsicHeight()); 
		                return drawable;
		            } catch (Exception e) {
		                return null;
		            } 
		        }

		        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
		            DefaultHttpClient httpClient = new DefaultHttpClient();
		            HttpGet request = new HttpGet(urlString);
		            HttpResponse response = httpClient.execute(request);
		            return response.getEntity().getContent();
		        }
		    }
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