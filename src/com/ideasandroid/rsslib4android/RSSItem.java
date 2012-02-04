package com.ideasandroid.rsslib4android;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public class RSSItem implements Parcelable, BaseColumns{
	
	public static final Uri CONTENT_URL=Uri.parse("content://com.ideasandroid.itreader.provider.rssprovider/rss");

	public static final String TABLE_NAME="RSSItem";
	public static final String DEFAULT_SORT_ORDER="pubDate ASC";
	
	public static final String ID = "id";
	public static final String TITLE="title";
	public static final String LINK="link";
	public static final String DESCRIPTION="description";
	public static final String PUBDATE="pubDate";
	public static final String GUID="guid";
	public static final String CHANNELID="channelId";
	public static final String TYPEID="typeId";
	public static final String ISREADED="isReaded";
	
	private int id;
	private String title;
	private String link;
	private String description;
	private String pubDate;
	private String guid;
	private int channelId;
	private int typeId;
	private int isReaded;
	
	public RSSItem(){
		
	}
	
	public RSSItem(String title,String link,String description,int channelId,int typeId,String guid,String pubDate){
		this.title=title;
		this.link=link;
		this.description=description;
		this.pubDate=pubDate;
		this.guid=guid;
		this.channelId=channelId;
		this.typeId=typeId;
		this.isReaded=0;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getIsReaded() {
		return isReaded;
	}
	public void setIsReaded(int isReaded) {
		this.isReaded = isReaded;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(link);
		dest.writeString(description);
		dest.writeString(guid);
		dest.writeString(pubDate);
		dest.writeInt(isReaded);
		dest.writeInt(id);
	}
	
	public static Object createFromParcel(Parcel parcel) {
      return null;
  }
	
}
