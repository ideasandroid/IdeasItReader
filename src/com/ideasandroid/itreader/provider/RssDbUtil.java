/*
 * Copyright (C) 2010 ideasandroid
 *
 */

package com.ideasandroid.itreader.provider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.ideasandroid.itreader.Constants;
import com.ideasandroid.rsslib4android.RSSChannel;
import com.ideasandroid.rsslib4android.RSSItem;
import com.ideasandroid.rsslib4android.RSSType;


public class RssDbUtil{
    private static final String LOG_TAG = "RSSChannelProvider";
    private static final String DATABASE_NAME = "ideasreader.db";
    private static final int DATABASE_VERSION = Constants.DATABASE_VERSION;


    private SQLiteOpenHelper mOpenHelper;
    
    public RssDbUtil(Context context){
    	if(mOpenHelper==null){
    		mOpenHelper = new DatabaseHelper(context);
    	}
    }
    
    public void insertRssItem(List<ContentValues> needInsertItems){
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase(); 
    	try { 
    		db.beginTransaction();
    		for(ContentValues item:needInsertItems){
	    	    db.insert(RSSItem.TABLE_NAME, RSSItem.TITLE, item);
    		}
    	    db.setTransactionSuccessful();
    	    db.endTransaction();
    	    } catch (SQLException ex) { 
    	        //txtMsg.setText("插入数据失败\n" + ex.toString() + "\n"); 
    	    } 
    	    db.close();
    }
    
    public boolean existItem(String where){
    	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    	Cursor c= db.query(RSSItem.TABLE_NAME, null, where, null, null, null, null);
    	if(c.getCount()!=0){
    		c.close();
    		db.close();
    		return true;
    	}else{
    		c.close();
    		db.close();
    		return false;
    	}
    }
    
    public void updateChannelUpdateTime(int channelId){
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	ContentValues values = new ContentValues();
		values.put(RSSChannel.LASTUPDATEDATE, new Date().toGMTString());
    	db.update(RSSChannel.TABLE_NAME, values, RSSChannel._ID + "=" +channelId, null);
    	db.close();
    }
    
    public void deleteRssItems(String where){
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	db.delete(RSSItem.TABLE_NAME, where, null);
    	db.close();
    }
    
    public List<RSSChannel> queryRSSChannels(String where){
    	List<RSSChannel> list=new ArrayList<RSSChannel>();
    	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    	Cursor cursor=db.query(RSSChannel.TABLE_NAME, null, where, null, null, null, null);
    	cursor.moveToFirst();
		int idIndex = cursor.getColumnIndex(RSSChannel._ID);
		int urlIndex = cursor.getColumnIndex(RSSChannel.URL);
		int typeIdIndex = cursor.getColumnIndex(RSSChannel.TYPEID);
		int pubDateIndex = cursor.getColumnIndex(RSSChannel.LASTUPDATEDATE);
		do {
			RSSChannel channel=new RSSChannel();
			channel.setId(cursor.getInt(idIndex));
			channel.setUrl(cursor.getString(urlIndex));
			channel.setTypeId(cursor.getInt(typeIdIndex));
			channel.setPubDate(cursor.getString(pubDateIndex));
			list.add(channel);
		} while (cursor.moveToNext());
    	db.close();
    	return list;
    }



    public static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
    
        	 
        	db.execSQL("CREATE TABLE "+RSSType.TABLE_NAME+" ("
                    + RSSType._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RSSType.NAME + " TEXT, "
                    + RSSType.ISSYSINIT + " INTEGER, "
                    + RSSType.REMARK + " TEXT);");
        	//db.execSQL("insert into "+RSSType.TABLE_NAME + " ("+RSSType._ID+",name,remark) values(1,'IT资讯','新新最快的IT资讯')");
        	
            db.execSQL("CREATE TABLE "+RSSChannel.TABLE_NAME+" ("
                    + RSSChannel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RSSChannel.TITLE + " TEXT, "
                    + RSSChannel.LINK + " TEXT, "
                    + RSSChannel.DESCRIPTION + " TEXT, "
                    + RSSChannel.URL + " TEXT, "
                    + RSSChannel.LANGUAGE + " TEXT, "
                    + RSSChannel.COPYRIGHT + " INTEGER, "
                    + RSSChannel.TYPEID + " INTEGER, "
                    + RSSChannel.LASTUPDATEDATE + " DATE, "
                    + RSSChannel.PUBDATE + " DATE);");
             
            //db.execSQL("insert into "+RSSChannel.TABLE_NAME + " (title,url,lastUpdateDate,typeId) values('cnBeta','http://www.cnbeta.com/backend.php','Fri, 12 Aug 2010 14:18:28 GMT',1)");
            //db.execSQL("insert into "+RSSChannel.TABLE_NAME + " (title,url,lastUpdateDate,typeId) values('eng','http://cn.engadget.com/rss.xml','Fri, 12 Aug 2009 14:18:28 GMT',1)");
            //db.execSQL("CREATE INDEX index1 ON "+RSSChannel.TABLE_NAME+"(" + RSSChannel._ID + ");");
            
            db.execSQL("CREATE TABLE "+RSSItem.TABLE_NAME+" ("
                    + RSSItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RSSItem.TITLE + " TEXT, "
                    + RSSItem.LINK + " TEXT, "
                    + RSSItem.DESCRIPTION + " TEXT, "
                    + RSSItem.GUID + " TEXT, "
                    + RSSItem.CHANNELID + " INTEGER, "
                    + RSSItem.TYPEID + " INTEGER, "
                    + RSSItem.ISREADED + " INTEGER, "
                    + RSSItem.PUBDATE + " DATE);");
            
            //db.execSQL("CREATE INDEX index2 ON "+RSSItem.TABLE_NAME+"(" + RSSItem.PUBDATE + ");");
         
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
                    newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS "+RSSItem.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "+RSSChannel.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "+RSSType.TABLE_NAME);
            
            onCreate(db);
        }
    }
}
