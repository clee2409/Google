package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class RedditComment {

    public String mAuthor;
    public String mTimeSincePost;
    public String mScore;
    public String mBody;
    public String mFullname;
    public String mLikes;
    public String mLinkPermalink;
    public int mDepth;
    public ArrayList<RedditComment> mReplies;
    public Gildings mGildings;

    public RedditComment() {}
    public RedditComment(String author, String timeSincePost, String score,
                         String body, String fullname, String likes,
                         String linkPermalink, int depth, ArrayList<RedditComment> replies, Gildings gildings) {
        mAuthor = author;
        mTimeSincePost = timeSincePost;
        mScore = score;
        mBody = body;
        mFullname = fullname;
        mLikes = likes;
        mLinkPermalink = linkPermalink;

        mDepth = depth;
        mReplies = replies;
        mGildings = gildings;
    }
}
