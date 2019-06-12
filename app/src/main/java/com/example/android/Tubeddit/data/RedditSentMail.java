package com.example.android.Tubeddit.data;

public class RedditSentMail {

    public String mDestination;
    public String mTimeSincePost;
    public String mSubject;
    public String mBody;

    public RedditSentMail(String destination, String timeSincePost, String subject, String body) {
        mDestination = destination;
        mTimeSincePost = timeSincePost;
        mSubject = subject;
        mBody = body;
    }

}
