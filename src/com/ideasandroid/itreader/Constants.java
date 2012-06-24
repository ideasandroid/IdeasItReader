package com.ideasandroid.itreader;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class Constants {
	public static final SimpleDateFormat dateformatter = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	
	public static final int DATABASE_VERSION=1;
	
	public static String getAppVersionName(Context context) {    
	    String versionName = "";    
	    try {    
	        // ---get the package info---    
	        PackageManager pm = context.getPackageManager();    
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);    
	        versionName = pi.versionName;    
	        if (versionName == null || versionName.length() <= 0) {    
	            return "";    
	        }    
	    } catch (Exception e) {    
	        Log.e("VersionInfo", "Exception", e);    
	    }    
	    return versionName;    
	}
}
