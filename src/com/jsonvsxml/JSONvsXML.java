package com.jsonvsxml;

import com.jsonvsxml.util.TwitterParser;
import com.jsonvsxml.util.YouTubeParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class JSONvsXML extends Activity {
	private static String youtubeXML  = "http://gdata.youtube.com/feeds/api/videos?alt=rss&v=2&orderby=published&author=BillboardMagazine";
	private static String youtubeJSON = "http://gdata.youtube.com/feeds/api/videos?alt=json&v=2&orderby=published&author=BillboardMagazine";
	
	private static String twitterXML  = "https://api.twitter.com/statuses/user_timeline.xml?screen_name=jucklee";
	private static String twitterJSON = "https://api.twitter.com/statuses/user_timeline.json?screen_name=jucklee";
	
	public Handler timeHandler = new Handler() {
		public void handleMessage(Message msg) {
			String reqUrl = (String)msg.obj;
			int elapsed = msg.arg1;
			int size = msg.arg2;
			
			switch (msg.what) {
				case C.MSG_DOWNLOAD_DONE:
					showDownloadResult(reqUrl, size, elapsed);
					break;
				case C.MSG_PARSE_DONE:
					showParseResult(elapsed);
					break;
			}
		}
	};
	
	private Context mContext;
	
	private TextView mUrl;
	private TextView mFileSize;
	private TextView mTimeDownload;
	private TextView mTimeParse;
	private TextView mTotalTime;
	
	private EditText mItemNum;
	
	private Button mYoutubeXML;
	private Button mYoutubeJSON;
	private Button mTwitterXML;
	private Button mTwitterJSON;
	
	private int crtType = C.TYPE_YOUTUBE_XML;
	
	private int download_ms = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		mContext = this;
		
		mUrl 			= (TextView)findViewById(R.id.reqUrl);
		mFileSize 		= (TextView)findViewById(R.id.fileSize);
		mTimeDownload 	= (TextView)findViewById(R.id.timeToDownlaod);
		mTimeParse 		= (TextView)findViewById(R.id.timeToParse);
		mTotalTime		= (TextView)findViewById(R.id.totalTime);
		
		mItemNum		= (EditText)findViewById(R.id.numItem);
		
		mYoutubeXML  = (Button)findViewById(R.id.youtubeXML);
		mYoutubeJSON = (Button)findViewById(R.id.youtubeJSON);
		mTwitterXML  = (Button)findViewById(R.id.twitterXML);
		mTwitterJSON = (Button)findViewById(R.id.twitterJSON);
		
		mYoutubeXML.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				crtType = C.TYPE_YOUTUBE_XML;
				DataDownloadTask task = new DataDownloadTask(youtubeXML + "&max-results=" + mItemNum.getText().toString());
				task.execute();
			}
		});
		mYoutubeJSON.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				crtType = C.TYPE_YOUTUBE_JSON;
				DataDownloadTask task = new DataDownloadTask(youtubeJSON + "&max-results=" + mItemNum.getText().toString());
				task.execute();
			}
		});
		mTwitterXML.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				crtType = C.TYPE_TWITTER_XML;
				DataDownloadTask task = new DataDownloadTask(twitterXML + "&count=" + mItemNum.getText().toString());
				task.execute();
			}
		});
		mTwitterJSON.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				crtType = C.TYPE_TWITTER_JSON;
				DataDownloadTask task = new DataDownloadTask(twitterJSON + "&count=" + mItemNum.getText().toString());
				task.execute();
			}
		});
	}
	
	public void showDownloadResult(String url, int size, int time) {
		int sizeKB = 0;
		if (size > C.KB) {
			sizeKB = size/C.KB;
		}
		mUrl.setText(C.HEAD_REQ_URL + url);
		mFileSize.setText(C.HEAD_FILE_SIZE + String.valueOf(sizeKB) + "KB" + "(" + String.valueOf(size) + "byte" + ")");
		mTimeDownload.setText(C.HEAD_DOWNLOAD_TIME + String.valueOf(time) + "ms");
		
		download_ms = time;
	}
	
	public void showParseResult(int time) {
		mTimeParse.setText(C.HEAD_PARSE_TIME + String.valueOf(time) + "ms");
		mTotalTime.setText(C.HEAD_TOTAL_TIME + String.valueOf(download_ms + time) + "ms");
	}
	
	/*
	 *  Get   XML DATA + 
	 *  Parse XML DATA
	 */
	class DataDownloadTask extends AsyncTask<String, String, String> {
		private ProgressDialog progressDialog = null;
		private String url;
		private int elapsed;
		private int size;
		
		public DataDownloadTask(String url) {
			this.url = url;
		}
		
		@Override
		protected void onPreExecute() {
	        super.onPreExecute();
	        
	        progressDialog = new ProgressDialog(mContext);
	        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        progressDialog.setMessage("DOWNLOADING");
	        progressDialog.setCancelable(true);
	        progressDialog.show();
	    }
		
		@Override
	    protected void onPostExecute(String unused) {
	    	if (progressDialog != null) {
				progressDialog.dismiss();
			}
	    }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				long start = System.currentTimeMillis();
				String response = YouTubeParser.executeHttpGet(url);
				elapsed = (int)(System.currentTimeMillis() - start);
				size = (int)(response.length());
				sendDownloadResult();
				
				start = System.currentTimeMillis();
				
				switch (crtType) {
					case C.TYPE_YOUTUBE_XML:
						YouTubeParser.xmlParseredData(response);
						break;
						
					case C.TYPE_YOUTUBE_JSON:
						YouTubeParser.jsonParseredData(response);
						break;
						
					case C.TYPE_TWITTER_XML:
						TwitterParser.xmlParseredData(response);
						break;
						
					case C.TYPE_TWITTER_JSON:
						TwitterParser.jsonParseredData(response);
						break;
				}
				
				elapsed = (int)(System.currentTimeMillis() - start);
				sendParseResult();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public void sendDownloadResult() {
			Message msg = new Message();
			msg.what = C.MSG_DOWNLOAD_DONE;
			msg.setTarget(timeHandler);
			msg.arg1 = elapsed;
			msg.arg2 = size;
			msg.obj = url;
			msg.sendToTarget();
		}
		
		public void sendParseResult() {
			Message msg = new Message();
			msg.what = C.MSG_PARSE_DONE;
			msg.setTarget(timeHandler);
			msg.arg1 = elapsed;
			msg.sendToTarget();
		}
	}
}
