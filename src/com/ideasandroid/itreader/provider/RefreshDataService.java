package com.ideasandroid.itreader.provider;

import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.ideasandroid.rsslib4android.RSSFactory;
import com.ideasandroid.rsslib4android.RSSItem;

public class RefreshDataService extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Thread thr = new Thread(null, mTask, "AlarmService_Service");
		thr.start();
	}

	Runnable mTask = new Runnable() {
		public void run() {
			// 后台自动从网络上获取数据
			synchronized (mBinder) {
				if (NetworkStateCheckUtil.isNetworkAvailable(getApplicationContext())) {
					try {
						boolean isInited = getSharedPreferences("ideasrss",
								Context.MODE_PRIVATE).getBoolean("isInited",
								false);
						ContentResolver cr = getContentResolver();
						RSSFactory rf = new RSSFactory();
						if (isInited) {
							rf.init(getApplicationContext());
							Cursor c = rf.getRssCursor(cr, RSSItem.ISREADED
									+ "='0'");
							if (c.getCount() > 0) {
								setNotifyAlarm();
							}
						} else {
							rf.initSystemRssConfig(cr);
							rf.init(getApplicationContext());
							Cursor c = rf.getRssCursor(cr, RSSItem.ISREADED
									+ "='0'");
							if (c.getCount() > 0) {
								setNotifyAlarm();
							}
						}
					} catch (Exception e) {
						Log.d(RefreshDataService.class.toString(),
								"some problem do not need to crect it");
					}
				}
				RefreshDataService.this.stopSelf();
			}
		}

	};

	public void setNotifyAlarm() {
		if (isRunning("com.ideasandroid.itreader.service.NewRSSNotifyService")) {
			stopService(new Intent(
					"com.ideasandroid.itreader.service.NewRSSNotifyService"));
			startService(new Intent(
					"com.ideasandroid.itreader.service.NewRSSNotifyService"));
		} else {
			startService(new Intent(
					"com.ideasandroid.itreader.service.NewRSSNotifyService"));
		}
		/**
		 * AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		 * Intent intent = new Intent(this,NewRSSNotifyService.class);
		 * PendingIntent pendingIntent = PendingIntent.getService(this, 0,
		 * intent, 0); am.set(AlarmManager.RTC_WAKEUP,
		 * System.currentTimeMillis() + (5 * 1000), pendingIntent);
		 */
	}

	public boolean isRunning(String serviceName) {
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

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onDestroy() {
		Log.d(RefreshDataService.class.toString(), "Data Refresh sucess!");
		super.onDestroy();
	}

	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

}
