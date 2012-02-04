/*
 * Copyright (C) 2010 ideasandroid
 *
 */

package com.ideasandroid.itreader.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
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


public class RSSProvider extends ContentProvider {
    private static final String LOG_TAG = "RSSChannelProvider";
    
    
    public static final Uri CONTENT_URL=Uri.parse("content://com.ideasandroid.itreader.provider.rssprovider");

    private static final String DATABASE_NAME = "ideasreader.db";
    private static final int DATABASE_VERSION = Constants.DATABASE_VERSION;
    
    private static final HashMap<String, String> RSSCHANNEL_PROJECTION_MAP;

    private static final int RSSCHANNEL = 1;
    private static final int RSSCHANNEL_ID = 2;
    
    private static final int RSSTYPE = 3;
    private static final int RSSTYPE_ID = 4;
    private static final int RSS = 5;
    private static final int RSS_ID = 6;

    private static final String AUTHORITY = "com.ideasandroid.itreader.provider.rssprovider";

    private static final UriMatcher URI_MATCHER;
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, "rsschannel", RSSCHANNEL);
        URI_MATCHER.addURI(AUTHORITY, "rsschannel/#", RSSCHANNEL_ID);
        URI_MATCHER.addURI(AUTHORITY, "rsstype", RSSTYPE);
        URI_MATCHER.addURI(AUTHORITY, "rsstype/#", RSSTYPE_ID);
        URI_MATCHER.addURI(AUTHORITY, "rss", RSS);
        URI_MATCHER.addURI(AUTHORITY, "rss/#", RSS_ID);
        
        RSSCHANNEL_PROJECTION_MAP = new HashMap<String,String>();
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel._ID, RSSChannel._ID);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.LINK, RSSChannel.LINK);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.TITLE, RSSChannel.TITLE);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.URL, RSSChannel.URL);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.DESCRIPTION, RSSChannel.DESCRIPTION);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.PUBDATE, RSSChannel.PUBDATE);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.LANGUAGE, RSSChannel.LANGUAGE);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.COPYRIGHT, RSSChannel.COPYRIGHT);
        RSSCHANNEL_PROJECTION_MAP.put(RSSChannel.LASTUPDATEDATE, RSSChannel.LASTUPDATEDATE);
    }


    private SQLiteOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        switch (URI_MATCHER.match(uri)) {
            case RSSCHANNEL:
                qb.setTables(RSSChannel.TABLE_NAME);
                break;
            case RSSCHANNEL_ID:
                qb.setTables(RSSChannel.TABLE_NAME);
                qb.appendWhere("_id=" + uri.getPathSegments().get(1));
                break;
            case RSSTYPE:
                qb.setTables(RSSType.TABLE_NAME);
                break;
            case RSSTYPE_ID:
                qb.setTables(RSSType.TABLE_NAME);
                qb.appendWhere("_id=" + uri.getPathSegments().get(1));
                break;
            case RSS:
                qb.setTables(RSSItem.TABLE_NAME);
                break;
            case RSS_ID:
                qb.setTables(RSSItem.TABLE_NAME);
                qb.appendWhere("_id=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // If no sort order is specified use the default
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case RSSCHANNEL:
                return "vnd.android.cursor.dir/vnd.com.ideasandroid.provider.rsschannel";
            case RSSCHANNEL_ID:
                return "vnd.android.cursor.item/vnd.com.ideasandroid.provider.rsschannel";
            case RSSTYPE:
                return "vnd.android.cursor.dir/vnd.com.ideasandroid.provider.rsstype";
            case RSSTYPE_ID:
                return "vnd.android.cursor.item/vnd.com.ideasandroid.provider.rsstype";
            case RSS:
                return "vnd.android.cursor.dir/vnd.com.ideasandroid.provider.rss";
            case RSS_ID:
                return "vnd.android.cursor.item/vnd.com.ideasandroid.provider.rss";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values = new ContentValues(initialValues);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
        case RSSCHANNEL:
        	final long rowId = db.insert(RSSChannel.TABLE_NAME, RSSChannel.TITLE, values);
        	db.close();
            if (rowId > 0) {
                Uri insertUri = ContentUris.withAppendedId(RSSChannel.CONTENT_URL, rowId);
                getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;
            }
        case RSSTYPE:
        	final long rowId1 = db.insert(RSSType.TABLE_NAME, RSSType.NAME, values);
        	
            if (rowId1 > 0) {
                Uri insertUri = ContentUris.withAppendedId(RSSType.CONTENT_URL, rowId1);
                getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;
            }
        case RSS:
        	final long rowId2 = db.insert(RSSItem.TABLE_NAME, RSSItem.TITLE, values);
        	
            if (rowId2 > 0) {
                Uri insertUri = ContentUris.withAppendedId(RSSItem.CONTENT_URL, rowId2);
                getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;
            }
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
    }


    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
            case RSSCHANNEL:
                count = db.delete(RSSChannel.TABLE_NAME, selection, selectionArgs);
                break;
            case RSSCHANNEL_ID:
                count = db.delete(RSSChannel.TABLE_NAME, RSSChannel._ID + "=" + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case RSSTYPE:
                count = db.delete(RSSType.TABLE_NAME, selection, selectionArgs);
                break;
            case RSSTYPE_ID:
                count = db.delete(RSSType.TABLE_NAME, RSSType._ID + "=" + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case RSS:
                count = db.delete(RSSItem.TABLE_NAME, selection, selectionArgs);
                break;
            case RSS_ID:
                count = db.delete(RSSItem.TABLE_NAME, RSSItem._ID + "=" + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	int count;
    	switch (URI_MATCHER.match(uri)) {
        case RSSCHANNEL:
        	count=db.update(RSSChannel.TABLE_NAME,values,selection, selectionArgs);
            break;
        case RSSCHANNEL_ID:
            count = db.update(RSSChannel.TABLE_NAME, values, RSSChannel._ID + "=" +uri.getPathSegments().get(1) +
            		(!TextUtils.isEmpty(selection)?"AND ("+selection+")":""), selectionArgs);
            break;
        case RSSTYPE:
        	count=db.update(RSSType.TABLE_NAME,values,selection, selectionArgs);
            break;
        case RSSTYPE_ID:
            count = db.update(RSSType.TABLE_NAME, values, RSSType._ID + "=" +uri.getPathSegments().get(1) +
            		(!TextUtils.isEmpty(selection)?"AND ("+selection+")":""), selectionArgs);
            break;
        case RSS:
        	count=db.update(RSSItem.TABLE_NAME,values,selection, selectionArgs);
            break;
        case RSS_ID:
            count = db.update(RSSItem.TABLE_NAME, values, RSSItem._ID + "=" +uri.getPathSegments().get(1) +
            		(!TextUtils.isEmpty(selection)?"AND ("+selection+")":""), selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
    	}
    	getContext().getContentResolver().notifyChange(uri, null);
        return count;
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
