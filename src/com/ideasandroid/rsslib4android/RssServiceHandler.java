package com.ideasandroid.rsslib4android;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RssServiceHandler extends DefaultHandler {
	List<RSSType> typeList=null;
	List<RSSChannel> channelList=null;
	public RssServiceHandler(){
		//先删除历史初始化数据
		typeList=new ArrayList<RSSType>();
		channelList=new ArrayList<RSSChannel>();
	}
	public void endDocument() throws SAXException {
	}

	public void startDocument() throws SAXException {
	}

	public void startElement(String namespaceURI, String localName, 
            String qName, Attributes atts)
			throws SAXException {
		if (localName.equals("type")) {
			RSSType type=new RSSType();
			type.setId(Integer.valueOf(atts.getValue("id").trim()));
			type.setName(atts.getValue("name").trim());
			type.setRemark(atts.getValue("remark").trim());
			type.setIsSysInit(1);
			typeList.add(type);
			
		}else if(localName.equals("channel")){
			RSSChannel channel=new RSSChannel();
			channel.setTitle(atts.getValue("title").trim());
			channel.setUrl(atts.getValue("url").trim());
			channel.setLastUpdateDate(atts.getValue("lastUpdateDate").trim());
			channel.setTypeId(Integer.valueOf(atts.getValue("typeId").trim()));
			channelList.add(channel);
		}
	}

	public List<RSSType> getTypeList() {
		return typeList;
	}
	public List<RSSChannel> getChannelList() {
		return channelList;
	}
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

	}

	public void characters(char[] p0, int p1, int p2) throws SAXException {
	}
}
