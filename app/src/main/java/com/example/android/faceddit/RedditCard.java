package com.example.android.faceddit;

public class RedditCard {

    public String mTitle;
    public String mSubreddit;
    public String mThumbnailUrl;
    public String mScore;
    public String mAuthor;

    public RedditCard(String title, String subreddit, String thumbnailUrl, String score, String author) {
        mTitle = title;
        mSubreddit = subreddit;
        mThumbnailUrl = thumbnailUrl;
        mScore = score;
        mAuthor = author;
    }

}
