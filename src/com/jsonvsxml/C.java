package com.jsonvsxml;

public class C {
	public static final String TAG = "JSONvsXML";
	
	public static final int MSG_DOWNLOAD_DONE = 10001;
	public static final int MSG_PARSE_DONE = 10002;
	
	public static final int KB = 1024;
	
	public static final int DEFAULT_ITEM_COUNT = 25;
	
	public static final String HEAD_REQ_URL = "URL : ";
	public static final String HEAD_FILE_SIZE = "FILE SIZE : ";
	public static final String HEAD_DOWNLOAD_TIME = "DOWNLOAD : ";
	public static final String HEAD_PARSE_TIME = "PARSE : ";
	public static final String HEAD_TOTAL_TIME = "TOTAL : ";
	
	public static final int TYPE_YOUTUBE_XML  = 2001;
	public static final int TYPE_YOUTUBE_JSON = 2002;
	public static final int TYPE_TWITTER_XML  = 2003;
	public static final int TYPE_TWITTER_JSON = 2004;
}
