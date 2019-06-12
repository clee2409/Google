package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

@Parcel
public class RedditCard {

    public String mTitle;
    public String mSubreddit;
    public String mThumbnailUrl;
    public String mScore;
    public String mAuthor;
    public String mSelftext;
    public String mPermalink;
    public String mTimeSinceCreated;
    public String mCommentCount;
    public String mGoldGildCount;
    public String mSilverGildCount;
    public String mPlatinumGildCount;
    public String mPreviewUrl;
    public boolean mIsVideoOrGif;
    public String mLikes;
    public String mFullname;
    public Gildings mGildings;

    public RedditCard() {}
    public RedditCard(String title, String subreddit, String timeSinceCreation,
                      String thumbnailUrl, String score, String author, String selftext, String commentCount,
                      String permalink, String previewUrl, boolean isVideoOrGif, String likes, String fullname, Gildings gildings) {
        mTitle = title;
        mSubreddit = subreddit;
        mTimeSinceCreated = timeSinceCreation;
        mThumbnailUrl = thumbnailUrl;
        mScore = score;
        mAuthor = author;
        mSelftext = selftext;
        mCommentCount = commentCount;
        mPermalink = permalink;
        mPreviewUrl = previewUrl;
        mIsVideoOrGif = isVideoOrGif;
        mLikes = likes;
        mFullname = fullname;
        mGildings = gildings;
    }
}
