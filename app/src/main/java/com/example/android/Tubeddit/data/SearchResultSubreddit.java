package com.example.android.Tubeddit.data;

public class SearchResultSubreddit {

    public String mSubredditName;
    public String mSubscriberCount;
    public String mTimeSinceCreation;
    public String mPublicDescription;

    public SearchResultSubreddit(String subredditName, String subscriberCount, String timeSinceCreation, String publicDescription) {
        mSubredditName = subredditName;
        mSubscriberCount = subscriberCount;
        mTimeSinceCreation = timeSinceCreation;
        mPublicDescription = publicDescription;
    }

}
