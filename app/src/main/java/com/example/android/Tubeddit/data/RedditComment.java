package com.example.android.Tubeddit.data;

import java.util.ArrayList;

public class RedditComment {

    public String mAuthor;
    public String mTimeSincePost;
    public String mScore;
    public String mBody;
    public ArrayList<RedditComment> mReplies;
    public Gildings mGildings;

    public RedditComment() {}
    public RedditComment(String author, String timeSincePost, String score, String body, ArrayList<RedditComment> replies, Gildings gildings) {
        mAuthor = author;
        mTimeSincePost = timeSincePost;
        mScore = score;
        mBody = body;
        mReplies = replies;
        mGildings = gildings;
    }

}
