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
import com.jsonvsxml.twitter.TwitItem;

import android.util.Log;

public class TwitterParser {
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
	
	static public ArrayList<TwitItem> xmlParseredData(String response) {
		ArrayList<TwitItem> twitItems = new ArrayList<TwitItem>();
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			ByteArrayInputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
			
			Document doc = db.parse(is);
			
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("status");
	      
			int res_count = nodeList.getLength();
			
			for (int i = 0 ; i< res_count; i++) {
				TwitItem twitInfo = new TwitItem();

				Element status   = (Element)nodeList.item(i);
				Element user     = (Element)status.getElementsByTagName("user").item(0);
				Element body     = (Element)status.getElementsByTagName("text").item(0);
				Element date     = (Element)status.getElementsByTagName("created_at").item(0);
				Element twitId   = (Element)status.getElementsByTagName("id").item(0);
				Element userName = (Element)user.getElementsByTagName("name").item(0);
				Element imageUrl = (Element)user.getElementsByTagName("profile_image_url").item(0);
				
				twitInfo.body = body.getChildNodes().item(0).getNodeValue();
				twitInfo.date = date.getChildNodes().item(0).getNodeValue();
				twitInfo.twitId = twitId.getChildNodes().item(0).getNodeValue();
				twitInfo.userName = userName.getChildNodes().item(0).getNodeValue();
				twitInfo.profileImage = imageUrl.getChildNodes().item(0).getNodeValue();
				twitInfo.profileImage = twitInfo.profileImage.replace("normal", "reasonably_small");
				
				twitItems.add(twitInfo);
			}
		} catch(Exception e) {
			Log.e(C.TAG, "xmlParseredData  ex = " + e.getMessage());
		}
		return twitItems;
	}
	
	static public ArrayList<TwitItem> jsonParseredData(String response) {
		ArrayList<TwitItem> twitItems = new ArrayList<TwitItem>();

		try {
			JSONArray json = new JSONArray(response);
			
			for (int i=0, len=json.length(); i<len; i++) {
				JSONObject status = json.getJSONObject(i);
				JSONObject userInfo = status.getJSONObject("user");
				TwitItem twitInfo = new TwitItem();
				
				twitInfo.body     = status.getString("text");
				twitInfo.date     = status.getString("created_at");
				twitInfo.twitId   = status.getString("id");
				twitInfo.userName = userInfo.getString("name");
				twitInfo.profileImage = userInfo.getString("profile_image_url");
				
				twitItems.add(twitInfo);
			}
		} catch(Exception e) {
			Log.e(C.TAG, "jsonParseredData  ex = " + e.getMessage());
		}
		return twitItems;
	}
}
