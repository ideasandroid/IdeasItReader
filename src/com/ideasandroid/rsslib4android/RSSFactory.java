package com.ideasandroid.rsslib4android;

import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.ideasandroid.itreader.Constants;
import com.ideasandroid.itreader.provider.RssDbUtil;
import com.ideasapi.rest.HttpRestClient;

public class RSSFactory {

	public RSSFactory() {

	}

	public Cursor getRssCursor(ContentResolver contentResolver,
			String whereCaulse) {
		String[] projection = new String[] { RSSItem._ID, RSSItem.TITLE,
				RSSItem.LINK, RSSItem.ISREADED, RSSItem.CHANNELID,
				RSSItem.TYPEID, RSSItem.PUBDATE, RSSItem.DESCRIPTION };
		Cursor cursor = contentResolver.query(RSSItem.CONTENT_URL, projection,
				whereCaulse, null, RSSItem.PUBDATE + " DESC");
		return cursor;
	}

	public void init(Context context) {
		RssDbUtil util=new RssDbUtil(context);
		Log.d("RSS init", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
		// 先册除两天前的已读新闻
		SharedPreferences settings = context.getSharedPreferences("ideasrss",
				Context.MODE_PRIVATE);
		// 删除已读超时新闻
		if (!settings.getString("ideasrss.timeout.readed", "2").equals("0")) {
			String where = "julianday('now') - julianday(" + RSSItem.PUBDATE
					+ ")>" + settings.getString("ideasrss.timeout.readed", "2")
					+ " and " + RSSItem.ISREADED + " = 1";
			util.deleteRssItems(where);
		}
		// 删除未读超时新闻
		if (!settings.getString("ideasrss.timeout.noreaded", "4").equals("0")) {
			String where1 = "julianday('now') - julianday(" + RSSItem.PUBDATE
					+ ")>"
					+ settings.getString("ideasrss.timeout.noreaded", "4")
					+ " and " + RSSItem.ISREADED + " = 0";
			util.deleteRssItems(where1);
		}
		// 开新更新数据
		Log.d("RSS deleted", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
		List<RSSChannel> list =util.queryRSSChannels(null);
		for(RSSChannel channel:list){
			Date lastUpdateTime = new Date(channel.getPubDate());
			String url = channel.getUrl();
			Log.d("begin request item:", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
			int channelId = channel.getId();
			int typeId = channel.getTypeId();
			try {
				parseRSSByRemoteJson(url, channelId, typeId,
						lastUpdateTime,context);
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void parseRSSByRemoteJson(String _url, int channelId, int typeId,Date lastUpdateTime,Context context) throws JSONException{
		Log.d("begin parseRSSByRemoteJson:", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
		HttpClient client = new DefaultHttpClient();
		String url = "http://ideasapi.sinaapp.com/rss/rss2json";
		Map<String, String> parasMap = new HashMap<String, String>();
		parasMap.put("url", _url);
		parasMap.put("lastUpdateTime", lastUpdateTime.toGMTString());
		String jsonString = null;
		try {
			jsonString = HttpRestClient.post(url).use(client).charset("UTF-8")
					.data(parasMap).asString();
		} catch (Exception e) {
			Log.d("", e.toString());
		}
		if(jsonString!=null){
			RssDbUtil util=new RssDbUtil(context);
			List<ContentValues> needInsertItems=new ArrayList<ContentValues>();
			JSONArray items=new JSONArray(jsonString);
			for(int i=0;i<items.length();i++){
				JSONObject item=items.getJSONObject(i);
				String title = item.getString("title");
				String link = item.getString("link");
				String description = item.getString("description");
				String pubDate = item.getString("pubDate");
				//String guid = item.getString("guid");
				description=HtmlRegexpUtil.fiterHtmlTag(description,"img");
				description = description.replaceAll("</img>", "");
				description=HtmlRegexpUtil.fiterHtmlTag(description, "IMG");
				description = description.replaceAll("</IMG>", "");
				
				Date pd = null;
				try {
					if (pubDate != null
							&& (pubDate.contains("GMT") || pubDate
									.contains(","))) {
						pd = new Date(pubDate);
					}else{
						pd = Constants.dateformatter.parse(pubDate);
					}
				} catch (ParseException e) {
					pd = new Date();
				}
				if (pd.after(lastUpdateTime)) {
					/*RSSItem rssitem=new RSSItem();
					rssitem.setTitle(title);
					rssitem.setLink(link);
					rssitem.setGuid(link);
					rssitem.setDescription(description);
					rssitem.setChannelId(channelId);
					rssitem.setTypeId(typeId);
					rssitem.setPubDate(Constants.dateformatter
							.format(pd));
					rssitem.setIsReaded(0);*/
					
					ContentValues values = new ContentValues();
					values.put(RSSItem.TITLE, title);
					values.put(RSSItem.LINK, link);
					values.put(RSSItem.GUID, link);
					values.put(RSSItem.DESCRIPTION, description);
					values.put(RSSItem.CHANNELID, channelId);
					values.put(RSSItem.TYPEID, typeId);
					values.put(RSSItem.PUBDATE, Constants.dateformatter
							.format(pd));
					values.put(RSSItem.ISREADED, 0);
					
					if(!util.existItem(RSSItem.LINK+"='"+link+"'")){
						needInsertItems.add(values);
					}else{
						break;
					}
				} else {
					break;
				}
			}
			util.insertRssItem(needInsertItems);
			util.updateChannelUpdateTime(channelId);
		}
		
		Log.d("end parseRSSByRemoteJson:", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
	}

	public void initSystemRssConfig(ContentResolver contentResolver) {
		Log.d("sys init ql begin:", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
		Cursor cursor = contentResolver.query(RSSType.CONTENT_URL,
				new String[] { RSSType._ID }, RSSType.ISSYSINIT + "=1", null,
				null);
		int idIndex = cursor.getColumnIndex(RSSType._ID);
		StringBuffer ids = new StringBuffer();
		if (cursor.moveToFirst()) {
			do {
				ids.append(cursor.getString(idIndex) + ",");
			} while (cursor.moveToNext());
			String idStr = ids.toString();
			idStr = idStr.substring(0, idStr.lastIndexOf(","));
			cursor.close();
			Log
					.d("delete channels:", RSSChannel.TYPEID + " in (" + idStr
							+ ")");
			contentResolver.delete(RSSChannel.CONTENT_URL, RSSChannel.TYPEID
					+ " in (" + idStr + ")", null);
			contentResolver.delete(RSSItem.CONTENT_URL, RSSItem.TYPEID
					+ " in (" + idStr + ")", null);
		}else{
			cursor.close();
		}
		contentResolver.delete(RSSType.CONTENT_URL, RSSType.ISSYSINIT + "=1",
				null);
		Log.d("sys init ql end:", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
		Log.d("sys init read type channel begin:", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
		List<RSSType> typeList = null;
		List<RSSChannel> channelList = null;
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			URL url = new URL(
					"http://ideasapi.sinaapp.com/api/ideasrssconfig.xml");
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xr = saxParser.getXMLReader();
			RssServiceHandler handler = new RssServiceHandler();
			xr.setContentHandler(handler);
			InputStreamReader isr = new InputStreamReader(url.openStream(),
					"UTF-8");
			xr.parse(new InputSource(isr));
			typeList = handler.getTypeList();
			channelList = handler.getChannelList();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < typeList.size(); i++) {
			RSSType type = (RSSType) typeList.get(i);
			ContentValues values = new ContentValues();
			values.put(RSSType._ID, type.getId());
			values.put(RSSType.NAME, type.getName());
			values.put(RSSType.REMARK, type.getRemark());
			values.put(RSSType.ISSYSINIT, type.getIsSysInit());
			contentResolver.insert(RSSType.CONTENT_URL, values);
		}

		for (int i = 0; i < channelList.size(); i++) {
			RSSChannel channel = (RSSChannel) channelList.get(i);
			ContentValues values = new ContentValues();
			values.put(RSSChannel.TITLE, channel.getTitle());
			values.put(RSSChannel.URL, channel.getUrl());
			values.put(RSSChannel.LASTUPDATEDATE, channel.getLastUpdateDate());
			values.put(RSSChannel.TYPEID, channel.getTypeId());
			contentResolver.insert(RSSChannel.CONTENT_URL, values);
		}
		Log.d("sys init read type channel end:", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS").format(new Date())));
	}
}
