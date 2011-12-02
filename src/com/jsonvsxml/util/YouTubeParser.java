package com.jsonvsxml.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jsonvsxml.C;
import com.jsonvsxml.youtube.VideoItem;

import android.util.Log;

public class YouTubeParser {
	/*
	 *  Get XML page from URL
	 */
	static public String executeHttpGet(String url) throws Exception{
		BufferedReader in = null;
		
		try{
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			
			while((line = in.readLine()) != null){
				sb.append(line);
			}
			return sb.toString();
		} finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
	
	static public ArrayList<VideoItem> xmlParseredData(String response) {
		ArrayList<VideoItem> videoItems = new ArrayList<VideoItem>();
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			ByteArrayInputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
			
			Document doc = db.parse(is);
			
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("item");
	      
			int res_count = nodeList.getLength();
			
			for (int i = 0 ; i< res_count; i++) {
				VideoItem videoInfo = new VideoItem();

				Element channel = (Element)nodeList.item(i);
				
				Element media = (Element)channel.getElementsByTagName("media:group").item(0);
				Element title = (Element)channel.getElementsByTagName("title").item(0);
				Element date = (Element)channel.getElementsByTagName("pubDate").item(0);
				Element rating = (Element)channel.getElementsByTagName("yt:rating").item(0);
				Element duration = (Element)media.getElementsByTagName("yt:duration").item(0);
				// Element videoUrl = (Element)media.getElementsByTagName("media:content").item(0);
				Element videoId  = (Element)media.getElementsByTagName("yt:videoid").item(0);
				Element thumbUrl = (Element)media.getElementsByTagName("media:thumbnail").item(0);
				
				videoInfo.title = title.getChildNodes().item(0).getNodeValue().trim();
				videoInfo.date = date.getChildNodes().item(0).getNodeValue().trim().substring(0, 16);
				// videoInfo.videoUrl = videoUrl.getAttributeNode("url").getNodeValue();
				videoInfo.videoId  = videoId.getChildNodes().item(0).getNodeValue();
				videoInfo.thumbUrl = thumbUrl.getAttributeNode("url").getNodeValue();
				if (rating != null) {
					videoInfo.numLikes = rating.getAttributeNode("numLikes").getNodeValue();
					int numLikes = Integer.parseInt(videoInfo.numLikes);
					if (numLikes > 10000) {
						numLikes = numLikes/1000;
						videoInfo.numLikes = String.valueOf(numLikes) + 'K';
					}
					videoInfo.numDislikes = rating.getAttributeNode("numDislikes").getNodeValue();
					int numDislikes = Integer.parseInt(videoInfo.numDislikes);
					if (numDislikes > 10000) {
						numDislikes = numDislikes/1000;
						videoInfo.numDislikes = String.valueOf(numDislikes) + 'K';
					}
				}
				videoInfo.duration = duration.getAttributeNode("seconds").getNodeValue();
				
				videoItems.add(videoInfo);
			}
		} catch(Exception e) {
			Log.e(C.TAG, "xmlParseredData  ex = " + e.getMessage());
		}
		return videoItems;
	}
	
	static public ArrayList<VideoItem> jsonParseredData(String response) {
		ArrayList<VideoItem> videoItems = new ArrayList<VideoItem>();
		
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject feed = jsonObject.getJSONObject("feed");
			JSONArray  entries = feed.getJSONArray("entry");
			
			for (int i=0, len=entries.length(); i<len; i++) {
				JSONObject entry     = entries.getJSONObject(i);
				JSONObject mediaInfo = entry.getJSONObject("media$group");
				JSONObject rating    = entry.getJSONObject("yt$rating");
				VideoItem  videoInfo = new VideoItem();
				
				videoInfo.title 		= entry.getString("title");
				videoInfo.date 			= entry.getString("published");
				videoInfo.duration 		= mediaInfo.getJSONObject("yt$duration").getString("seconds");
				videoInfo.numDislikes 	= rating.getString("numDislikes");
				videoInfo.numLikes 		= rating.getString("numLikes");
				videoInfo.videoId 		= mediaInfo.getString("yt$videoid");
				videoInfo.thumbUrl 		= mediaInfo.getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
				
				Log.e(C.TAG, "TITLE :  "      	+ videoInfo.title);
				Log.e(C.TAG, "DATE :  "        	+ videoInfo.date);
				Log.e(C.TAG, "DURATION :  " 	+ videoInfo.duration);
				Log.e(C.TAG, "NUM LIKES :  " 	+ videoInfo.numDislikes);
				Log.e(C.TAG, "NUM DISLIKES :  " + videoInfo.numLikes);
				Log.e(C.TAG, "VIDEO ID :  " 	+ videoInfo.videoId);
				Log.e(C.TAG, "THUMB URL :  " 	+ videoInfo.thumbUrl);
				
				videoItems.add(videoInfo);
			}
		} catch(Exception e) {
			Log.e(C.TAG, "jsonParseredData  ex = " + e.getMessage());
		}
		return videoItems;
	}
}
