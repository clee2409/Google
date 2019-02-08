package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

@Parcel
public class YoutubeVideo {

    public YoutubeVideo() {}

    public String mTitle;
    public String mChannelTitle;
    public String mPublishedDate;
    public String mThumbnailUrl;
    public String mId;

    public YoutubeVideo(String title, String channelTitle, String publishedDate, String thumbnailUrl, String id) {
        mTitle = title;
        mChannelTitle = channelTitle;
        mPublishedDate = publishedDate;
        mThumbnailUrl = thumbnailUrl;
        mId = id;
    };

}
