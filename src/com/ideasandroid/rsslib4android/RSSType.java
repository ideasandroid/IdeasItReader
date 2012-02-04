package com.ideasandroid.rsslib4android;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public class RSSType implements Parcelable, BaseColumns{
	
	public static final Uri CONTENT_URL=Uri.parse("content://com.ideasandroid.itreader.provider.rssprovider/rsstype");

	public static final String TABLE_NAME="RSSType";
	
	public static final String DEFAULT_SORT_ORDER=_ID + " ASC";
	
	public static final String NAME="name";
	public static final String REMARK="remark";
	public static final String ISSYSINIT="isSysInit";

	private int id;
	private String name;
	private String remark;
	private int isSysInit;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public int getIsSysInit() {
		return isSysInit;
	}

	public void setIsSysInit(int isSysInit) {
		this.isSysInit = isSysInit;
	}

}
