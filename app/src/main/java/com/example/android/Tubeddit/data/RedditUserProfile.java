package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

@Parcel
public class RedditUserProfile {

    //SAME CLASS AS REDDIT USER

    public RedditUserProfile() {
    }

    public String mUserName;
    public String mPublicDescription;
    public String mIconUrl;
    public String mTimeSinceCreation;
    public String mKarmaCount;

    public RedditUserProfile(String userName, String publicDescription, String iconUrl, String timeSinceCreation, String karmaCount) {
        mUserName = userName;
        mPublicDescription = publicDescription;
        mIconUrl= iconUrl;
        mTimeSinceCreation = timeSinceCreation;
        mKarmaCount = karmaCount;
    }

}
