package com.example.android.Tubeddit.utils;

import android.app.Dialog;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.Tubeddit.data.Gildings;
import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.RedditCardDetail;
import com.example.android.Tubeddit.data.RedditComment;
import com.example.android.Tubeddit.data.YoutubeComment;
import com.example.android.Tubeddit.data.YoutubeVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JsonUtils {

    public static ArrayList<RedditCard> parseRedditCards(String rawJson) throws JSONException {

        ArrayList<RedditCard> resultArrayList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONObject jsonObject1 = jsonObject.getJSONObject("data");

        //array of Reddit newsfeed cards
        JSONArray jsonArray = jsonObject1.getJSONArray("children");

        for (int i = 0; i < jsonArray.length(); i ++) {
            JSONObject newsfeedData = jsonArray.getJSONObject(i);
            JSONObject newsfeedData1 = newsfeedData.getJSONObject("data");
            String title = newsfeedData1.getString("title");
            String subreddit = newsfeedData1.getString("subreddit");
            String score = newsfeedData1.getString("score");
            String author = newsfeedData1.getString("author");
            String permalink = newsfeedData1.getString("permalink");
            JSONObject gildings = newsfeedData1.getJSONObject("gildings");
            String redditSilverCount = gildings.getString("gid_1");
            String redditGoldCount = gildings.getString("gid_2");
            String redditPlatinumCount = gildings.getString("gid_3");


            String thumbnail = newsfeedData1.getString("thumbnail");

            /*
            URLConnection connection = null;
            try {
                connection = new URL(thumbnail).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }


            String contentType = connection.getHeaderField("Content-Type");
            boolean isImage = contentType.startsWith("image/");
            */

            Gildings resultGildings = new Gildings(redditSilverCount, redditGoldCount, redditPlatinumCount);

            RedditCard redditCard = new RedditCard(title, subreddit, thumbnail, score, author, permalink,
                    resultGildings);
            resultArrayList.add(redditCard);
        }
        return resultArrayList;
    }

    public static RedditCardDetail parseRedditCardDetail(String rawJson) throws JSONException {
        RedditCardDetail result = null;
        ArrayList<RedditComment> resultCommentsList = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(rawJson);
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
        JSONArray jsonArray1 = jsonObject1.getJSONArray("children");
        JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
        JSONObject jsonObject3 = jsonObject2.getJSONObject("data");
        String upvoteRatio = jsonObject3.getString("upvote_ratio");

        //ALL THAT JUST TO GET UPVOTE RATIO FOR THE THREAD

        //RETRIEVE THREAD COMMENTS AND DATA

        JSONObject indexTwoJsonObject = jsonArray.getJSONObject(1);
        JSONObject jsonObject4 = indexTwoJsonObject.getJSONObject("data");
        JSONArray redditCommentsArray = jsonObject4.getJSONArray("children");

        for (int i = 0; i < redditCommentsArray.length()-1; i++) {
            //Last index of redditCommentsArray is a non-comment data type, thus the length()-1.
            JSONObject jsonObject5 = redditCommentsArray.getJSONObject(i);
            JSONObject redditComment = jsonObject5.getJSONObject("data");

            String testString = redditComment.getString("author");

            String author = redditComment.getString("author");
            String timeSinceCreated = redditComment.getString("created_utc");
            String score = redditComment.getString("score");
            String body = redditComment.getString("body");

            JSONObject gildings = redditComment.getJSONObject("gildings");
            String silverCount = gildings.getString("gid_1");
            String goldCount = gildings.getString("gid_2");
            String platinumCount = gildings.getString("gid_3");

            Gildings resultGildings = new Gildings(silverCount, goldCount, platinumCount);

            RedditComment resultRedditComment = new RedditComment(author, timeSinceCreated, score, body, null, resultGildings);
            resultCommentsList.add(resultRedditComment);

            //REPLY CHAINS
            if (redditComment.optString("replies").equals("")) {
                break;
            }
            else {
                JSONObject jsonObject6 = redditComment.getJSONObject("replies");
                JSONObject jsonObject7 = jsonObject6.getJSONObject("data");
                JSONArray replies = jsonObject7.getJSONArray("children");

                //each individual reply
                for (int x = 0; x < replies.length()-1; x++) {
                    //last index of replies is garbage, hence the .length()-1
                    JSONObject boilerplateData = replies.getJSONObject(x);
                    JSONObject reply = boilerplateData.getJSONObject("data");

                    String replyAuthor = reply.getString("author");
                    String replyTimeSinceCreated = reply.getString("created_utc");
                    String replyScore = reply.getString("score");
                    //
                }
            }

        }

        result = new RedditCardDetail(upvoteRatio, resultCommentsList);
        return result;
    }

    public static ArrayList<YoutubeComment> parseYoutubeCommentReplies(String rawJson) throws JSONException {
        ArrayList<YoutubeComment> resultRepliesList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONArray jsonArray = jsonObject.getJSONArray("items");

        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
        if (jsonObject1.has("replies")) {
            JSONObject jsonObject2 = jsonObject1.getJSONObject("replies");
            JSONArray jsonArray1 = jsonObject2.getJSONArray("comments");

            for (int i = 0; i < jsonArray1.length(); i++) {
                JSONObject jsonObject3 = jsonArray1.getJSONObject(i);
                JSONObject jsonObject4 = jsonObject3.getJSONObject("snippet");

                String authorName = jsonObject4.getString("authorDisplayName");
                String profileImageUrl = jsonObject4.getString("authorProfileImageUrl");

                String authorChannelId = jsonObject4.getJSONObject("authorChannelId").getString("value");
                String commentText = jsonObject4.getString("textDisplay");
                String likeCount = jsonObject4.getString("likeCount");
                String publishedDate = jsonObject4.getString("publishedAt");
                String updatedDate = jsonObject4.getString("updatedAt");

                YoutubeComment youtubeComment = new YoutubeComment(null, authorName, profileImageUrl, authorChannelId,
                        commentText, publishedDate, updatedDate,
                        likeCount, null);

                resultRepliesList.add(youtubeComment);
            }
        }

        return resultRepliesList;
    }

    public static ArrayList<YoutubeVideo> parseYoutubeVideos(String rawJson) throws JSONException {

        ArrayList<YoutubeVideo> resultList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONArray jsonArray = jsonObject.getJSONArray("items");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            JSONObject videoSnippet = jsonObject1.getJSONObject("snippet");

            String title = videoSnippet.getString("title");
            String channelTitle = videoSnippet.getString("channelTitle");
            String publishedDate = videoSnippet.getString("publishedAt");
            String videoId = jsonObject1.getString("id");

            String thumbnailUrl;
            if (videoSnippet.getJSONObject("thumbnails").has("maxres")) {
                thumbnailUrl = videoSnippet.getJSONObject("thumbnails")
                        .getJSONObject("maxres")
                        .getString("url");
            }
            else {
                thumbnailUrl = videoSnippet.getJSONObject("thumbnails")
                        .getJSONObject("high")
                        .getString("url");
            }

            YoutubeVideo resultVideo = new YoutubeVideo(title, channelTitle, publishedDate, thumbnailUrl, videoId);
            resultList.add(resultVideo);
        }

        return resultList;
    }

    public static ArrayList<YoutubeComment> parseYoutubeComments(String rawJson) throws JSONException {
        ArrayList<YoutubeComment> resultCommentList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONArray jsonObject1 = jsonObject.getJSONArray("items");

        for(int i=0; i < jsonObject1.length(); i++) {
            JSONObject jsonObject2 = jsonObject1.getJSONObject(i);
            JSONObject jsonObject3 = jsonObject2.getJSONObject("snippet");

            String replyCount = jsonObject3.getString("totalReplyCount");

            JSONObject jsonObject4 = jsonObject3.getJSONObject("topLevelComment");

            String commentId = jsonObject4.getString("id");

            JSONObject jsonObject5 = jsonObject4.getJSONObject("snippet");


            String authorName = jsonObject5.getString("authorDisplayName");
            String profileImageUrl = jsonObject5.getString("authorProfileImageUrl");
            String authorChannelId = jsonObject5.getString("authorChannelId");
            String commentText = jsonObject5.getString("textDisplay");
            String likeCount = jsonObject5.getString("likeCount");
            String publishedDate = jsonObject5.getString("publishedAt");
            String updatedDate = jsonObject5.getString("updatedAt");

            YoutubeComment youtubeComment = new YoutubeComment(commentId, authorName, profileImageUrl,
                    authorChannelId, commentText, publishedDate,
                    updatedDate, likeCount, replyCount);

            resultCommentList.add(youtubeComment);
        }

        return resultCommentList;
    }

    public static void redditLogInAndAuth() {

        //*check

    }

}
