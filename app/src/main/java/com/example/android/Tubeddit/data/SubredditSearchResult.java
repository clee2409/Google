package com.example.android.Tubeddit.data;

public class SubredditSearchResult {

    String mSubredditName;
    String mUserName;
    String mSubscriberCount;
    String mTimeSinceCreation;
    String mPublicDescription;

    public SubredditSearchResult(String subredditName, String subscriberCount, String timeSinceCreation, String publicDescription) {
        mSubredditName = subredditName;
        mSubscriberCount = subscriberCount;
        mTimeSinceCreation = timeSinceCreation;
        mPublicDescription = publicDescription;
    }
}
