package com.jsonvsxml.youtube;

public class VideoItem {
	public String title;		// title
	public String date;			// pubDate
	public String duration;		// yt:duration or media:content <attr : duration>
	public String videoId;		// yt:videoid
	public String videoUrl;		// media:content
	public String thumbUrl;		// media:thumbnail
	// public String youtubeUrl;
	public String numLikes;
	public String numDislikes;
	
	public VideoItem() {
	}
	
	/*
	public VideoItem(String title, String description, String pubDate, int duration, String videoUrl, String thumbUrl) {
		this.title = title;
		this.description = description;
		this.date = pubDate;
		this.duration = duration;
		this.videoUrl = videoUrl;
		this.thumbUrl = thumbUrl;
	}
	*/
}
