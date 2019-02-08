package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

@Parcel
public class RedditCard {

    public String mTitle;
    public String mSubreddit;
    public String mThumbnailUrl;
    public String mScore;
    public String mAuthor;
    public String mPermalink;
    public String mUtcSinceCreated;
    public String mGoldGildCount;
    public String mSilverGildCount;
    public String mPlatinumGildCount;
    public Gildings mGildings;

    public RedditCard() {}
    public RedditCard(String title, String subreddit, String thumbnailUrl, String score, String author, String permalink,
                      Gildings gildings) {
        mTitle = title;
        mSubreddit = subreddit;
        mThumbnailUrl = thumbnailUrl;
        mScore = score;
        mAuthor = author;
        mPermalink = permalink;
        mGildings = gildings;
    }

}
