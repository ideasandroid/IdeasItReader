package com.ideasandroid.rsslib4android;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public class RSSChannel implements Parcelable, BaseColumns{
	
	public static final Uri CONTENT_URL=Uri.parse("content://com.ideasandroid.itreader.provider.rssprovider/rsschannel");
	
	public static final String TABLE_NAME="RSSChannel";
	
	public static final String DEFAULT_SORT_ORDER="title ASC";
	
	public static final String TITLE="title";
	public static final String LINK="link";
	public static final String URL="url";
	public static final String DESCRIPTION="description";
	public static final String PUBDATE="pubDate";
	public static final String LANGUAGE="language";
	public static final String COPYRIGHT="copyright";
	public static final String LASTUPDATEDATE="lastUpdateDate";
	public static final String TYPEID="typeId";
	
	
	private int id;
	private String title;
	private String link;
	private String url;
	private String description;
	private String pubDate;  
	private String language;
	private String copyright;
	private String lastUpdateDate;
	private int typeId;
	
	public RSSChannel(){
		
	}
	public RSSChannel(Parcel in) {
		title = in.readString();
		link = in.readString();
		url = in.readString();
		description = in.readString();
		pubDate = in.readString();
		language = in.readString();
		copyright = in.readString();
		lastUpdateDate=in.readString();
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	public String getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	

	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(title);
		dest.writeString(link);
		dest.writeString(url);
		dest.writeString(description);
		dest.writeString(pubDate);
		dest.writeString(language);
		dest.writeString(copyright);
		dest.writeString(lastUpdateDate);
	}
	
	
}
