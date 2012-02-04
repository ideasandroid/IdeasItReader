package com.ideasandroid.itreader.service;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.ideasandroid.itreader.provider.RefreshDataService;

public class BootReceiver extends BroadcastReceiver {
	private PendingIntent mAlarmSender;
	@Override
	public void onReceive(Context context, Intent intent) {
		// 在这里干你想干的事，本例是启动一个定时调试程序，每30分钟启动一个Service去更新数据
		SharedPreferences settings = context.getSharedPreferences("ideasrss",
				Context.MODE_PRIVATE);
		Intent refreshDataIntent= new Intent(context,
				RefreshDataService.class);
		mAlarmSender = PendingIntent.getService(context, 0,refreshDataIntent, 0);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, refreshDataIntent, PendingIntent.FLAG_NO_CREATE);  
        if(sender!=null){
        	am.cancel(mAlarmSender);
        }
		long firstime=SystemClock.elapsedRealtime()+5*60*1000;
		long refreshRate=(long)(Double.valueOf(settings.getString("ideasrss.refresh.rate", "1")).doubleValue()*60);
		Log.d("*********************refreshRate:", String.valueOf(refreshRate));
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,
				refreshRate * 60 * 1000, mAlarmSender);
	}
}
