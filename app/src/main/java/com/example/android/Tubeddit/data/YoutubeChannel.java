package com.example.android.Tubeddit.data;

public class YoutubeChannel {

    public String mId;
    public String mChannelName;
    public String mDescription;
    public String mCreationDate;
    public String mChannelIconUrl;

    public YoutubeChannel(String id, String channelName, String description, String creationDate, String channelIconUrl) {
        mId = id;
        mChannelName = channelName;
        mDescription = description;
        mCreationDate = creationDate;
        mChannelIconUrl = channelIconUrl;
    }
}
