package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

@Parcel
public class YoutubeVideo {

    public YoutubeVideo() {}

    public String mTitle;
    public String mChannelTitle;
    public String mPublishedDate;
    public String mVideoDescription;

    public String mViewCount;
    public String mLikeCount;
    public String mDislikeCount;
    public String mCommentCount;

    public String mThumbnailUrl;
    public String mChannelId;
    public String mId;

    public YoutubeVideo(String title, String channelTitle, String publishedDate,
                        String videoDescription, String viewCount, String likeCount,
                        String dislikeCount, String commentCount, String thumbnailUrl,
                        String channelId, String id) {
        mTitle = title;
        mChannelTitle = channelTitle;
        mPublishedDate = publishedDate;
        mVideoDescription = videoDescription;
        mViewCount = viewCount;
        mLikeCount = likeCount;
        mDislikeCount = dislikeCount;
        mCommentCount = commentCount;
        mThumbnailUrl = thumbnailUrl;
        mChannelId = channelId;
        mId = id;
    }

}
