package com.example.android.Tubeddit.data;

//Holds Data for Youtube Comments

//Also holds data Youtube Comment Replies which is just like Youtube Comments except
//Youtube Comment Replies do not have a replycount so values for that will return null

public class YoutubeComment {

    public String mCommentId;
    public String mAuthor;
    public String mProfileImageUrl;
    public String mChannelId;
    public String mText;
    public String mPublishedTime;
    public String mUpdatedTime;
    public String mLikeCount;
    public String mReplyCount;

    public YoutubeComment(String commentId, String author, String profileImageUrl,
                          String channelId, String text, String publishedTime,
                          String updatedTime, String likeCount, String replyCount) {

        mCommentId = commentId;
        mAuthor = author;
        mProfileImageUrl = profileImageUrl;
        mChannelId = channelId;
        mText = text;
        mPublishedTime = publishedTime;
        mUpdatedTime = updatedTime;
        mLikeCount = likeCount;
        mReplyCount = replyCount;
    }
}
