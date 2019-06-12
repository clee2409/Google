package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class RedditCardDetail {
    public String mUpvoteRatio;
    public ArrayList<RedditComment> mCommentsList;

    public RedditCardDetail() {}
    public RedditCardDetail(String upvoteRatio, ArrayList<RedditComment> commentsList) {
        this.mUpvoteRatio = upvoteRatio;
        this.mCommentsList = commentsList;
    }

}
