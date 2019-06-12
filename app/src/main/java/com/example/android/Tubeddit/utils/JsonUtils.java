package com.example.android.Tubeddit.utils;

import com.example.android.Tubeddit.data.Gildings;
import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.RedditCardDetail;
import com.example.android.Tubeddit.data.RedditComment;
import com.example.android.Tubeddit.data.RedditSentMail;
import com.example.android.Tubeddit.data.RedditUserProfile;
import com.example.android.Tubeddit.data.SearchResultSubreddit;
import com.example.android.Tubeddit.data.YoutubeChannel;
import com.example.android.Tubeddit.data.YoutubeComment;
import com.example.android.Tubeddit.data.YoutubeVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtils {

    public static ArrayList<RedditCard> parseRedditCards(String rawJson) throws JSONException {

        ArrayList<RedditCard> resultArrayList = new ArrayList<>();

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(rawJson);
        }   catch (JSONException e) {
            jsonObject = new JSONArray(rawJson).getJSONObject(0);
        }

        //JSONObject jsonObject = new JSONObject(rawJson);

        JSONObject jsonObject1 = jsonObject.getJSONObject("data");

        //array of Reddit newsfeed cards
        JSONArray jsonArray = jsonObject1.getJSONArray("children");

        for (int i = 0; i < jsonArray.length(); i ++) {
            JSONObject newsfeedData = jsonArray.getJSONObject(i);
            if (!newsfeedData.getString("kind").equals("t3") && !newsfeedData.getString("kind").equals("t1")) continue;
            JSONObject newsfeedData1 = newsfeedData.getJSONObject("data");
            String title = null;
            if (newsfeedData1.has("title")) {
                title = newsfeedData1.getString("title");
            }
            if (newsfeedData1.has("link_title")) {
                title = newsfeedData1.getString("link_title");
            }
            String subreddit = newsfeedData1.getString("subreddit");
            String score = newsfeedData1.getString("score");
            String author = newsfeedData1.getString("author");
            String selftext = null;
            if (newsfeedData1.has("selftext")) {
                selftext = newsfeedData1.getString("selftext");
            }
            String permalink = newsfeedData1.getString("permalink");
            String commentCount = newsfeedData1.getString("num_comments");
            long timeSinceCreation = newsfeedData1.getLong("created");
            String likes = String.valueOf(newsfeedData1.getString("likes"));

            String fullname = newsfeedData1.getString("name");

            String redditSilverCount = null;
            String redditGoldCount = null;
            String redditPlatinumCount = null;
            Gildings resultGildings = null;

            if (newsfeedData1.has("gildings")) {
                JSONObject gildings = newsfeedData1.getJSONObject("gildings");
                if (gildings.has("gid_1"))redditSilverCount = gildings.getString("gid_1");
                if (gildings.has("gid_2")) redditGoldCount = gildings.getString("gid_2");
                if (gildings.has("gid_3")) redditPlatinumCount = gildings.getString("gid_3");
                resultGildings = new Gildings(redditSilverCount, redditGoldCount, redditPlatinumCount);
            }

            String thumbnail = "";
            if (newsfeedData1.has("thumbnail")) {
                thumbnail = newsfeedData1.getString("thumbnail");
            }


            /*
            URLConnection connection = null;
            try {
                connection = new URL(thumbnail).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String contentType = connection.getHeaderField("Content-Type");
            String isImage = contentType.startsWith("image/");
            */
            boolean isVideoOrGif = false;
            String sourceUrl = null;
            if (newsfeedData1.has("preview")) {
                JSONObject previews = newsfeedData1.getJSONObject("preview");

                if (previews.has("reddit_video_preview")) {
                    //previewUrl
                    isVideoOrGif = true;
                    JSONObject redditVideoPreview = previews.getJSONObject("reddit_video_preview");
                    sourceUrl = redditVideoPreview.getString("scrubber_media_url");
                }
                else if (previews.has("reddit_video")){
                    isVideoOrGif = true;
                    JSONObject redditVideo = previews.getJSONObject("reddit_video");
                    sourceUrl = redditVideo.getString("scrubber_media_url");
                }
                else {
                    //Regular image
                    JSONArray previewsArray = previews.getJSONArray("images");
                    JSONObject previewObject = previewsArray.getJSONObject(0);
                    JSONObject previewSource = previewObject.getJSONObject("source");
                    sourceUrl = previewSource.getString("url");
                    sourceUrl = sourceUrl.replace("&amp;","&");
                }
            }
            if (newsfeedData1.has("is_video")) {
                if (newsfeedData1.getString("is_video").equals("true")) {
                    JSONObject mediaObject = newsfeedData1.getJSONObject("media");
                    JSONObject redditVideo = mediaObject.getJSONObject("reddit_video");
                    isVideoOrGif = true;
                    sourceUrl = redditVideo.getString("scrubber_media_url");
                }
            }


            String convertedTime = DateUtils.getTimeAgo(timeSinceCreation);

            RedditCard redditCard = new RedditCard(title, subreddit, convertedTime,
                    thumbnail, score, author, selftext, commentCount,
                    permalink, sourceUrl, isVideoOrGif, likes, fullname, resultGildings);
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


        //parseRedditComments(rawJson);


        JSONObject indexTwoJsonObject = jsonArray.getJSONObject(1);
        String commentsJson = indexTwoJsonObject.toString();


        JSONObject jsonObject4 = indexTwoJsonObject.getJSONObject("data");
        JSONArray redditCommentsArray = jsonObject4.getJSONArray("children");

        resultCommentsList = parseRedditComments(indexTwoJsonObject.toString());
        /*

        for (int i = 0; i < redditCommentsArray.length(); i++) {

            //Last index of redditCommentsArray is a non-comment data type, thus the length()-1.
            JSONObject jsonObject5 = redditCommentsArray.getJSONObject(i);
            if (!jsonObject5.getString("kind").equals("t1")) continue;
            JSONObject redditComment = jsonObject5.getJSONObject("data");

            String testString = redditComment.getString("author");

            String author = redditComment.getString("author");
            long timeSinceCreated = redditComment.getLong("created_utc");
            String convertedTime = DateUtils.getTimeAgo(timeSinceCreated);
            String score = redditComment.getString("score");
            String body = redditComment.getString("body");
            String id = redditComment.getString("name");
            String likes = redditComment.getString("likes");

            String silverCount = null;
            String goldCount = null;
            String platinumCount = null;

            JSONObject gildings = redditComment.getJSONObject("gildings");
            if (gildings.has("gid_1")) silverCount = gildings.getString("gid_1");
            if (gildings.has("gid_2")) goldCount = gildings.getString("gid_2");
            if (gildings.has("gid_3")) platinumCount = gildings.getString("gid_3");

            Gildings resultGildings = new Gildings(silverCount, goldCount, platinumCount);

            RedditComment resultRedditComment = new RedditComment(author, convertedTime, score, body, id, likes, null, null, resultGildings);
            resultCommentsList.add(resultRedditComment);

            /*
            //REPLY CHAINS
            if (redditComment.optString("replies").equals("")) {
                break;
            } else {
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
        */

        result = new RedditCardDetail(upvoteRatio, resultCommentsList);
        return result;
    }

    public static ArrayList<RedditComment> parseRedditComments(String rawJson) throws JSONException {
        ArrayList<RedditComment> resultRedditCommentList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
        JSONArray jsonArray = jsonObject1.getJSONArray("children");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
            if (!jsonObject2.getString("kind").equals("t1")) continue;
            JSONObject redditComment = jsonObject2.getJSONObject("data");

            String author = redditComment.getString("author");
            long timeSinceCreated = redditComment.getLong("created_utc");
            String convertedTime = DateUtils.getTimeAgo(timeSinceCreated);
            String score = redditComment.getString("score");
            String body = redditComment.getString("body");
            String id = redditComment.getString("name");
            String likes = redditComment.getString("likes");
            int depth = 0;
            if (redditComment.has("depth")) {
                depth = redditComment.getInt("depth");
            }

            String link_permalink = null;
            if (redditComment.has("link_permalink")) {
                link_permalink = redditComment.getString("link_permalink");
            }
            Gildings resultGildings = null;
            String silverCount = null;
            String goldCount = null;
            String platinumCount = null;

            if (jsonObject2.has("gildings")) {
                JSONObject gildings = redditComment.getJSONObject("gildings");
                if (gildings.has("gid_1")) silverCount = gildings.getString("gid_1");
                if (gildings.has("gid_2")) goldCount = gildings.getString("gid_2");
                if (gildings.has("gid_3")) platinumCount = gildings.getString("gid_3");

                resultGildings = new Gildings(silverCount, goldCount, platinumCount);
            }

            ArrayList<RedditComment> nestedReplies = new ArrayList<>();
            //CREATE REPLY CHAIN
            if (!redditComment.get("replies").equals("")) {
                if (redditComment.getJSONObject("replies").length() != 0) {
                    JSONObject repliesObject = redditComment.getJSONObject("replies");
                    JSONObject repliesData = repliesObject.getJSONObject("data");

                    JSONArray repliesArray = repliesData.getJSONArray("children");

                    for (int x = 0; x < repliesArray.length(); x++) {
                        JSONObject jsonObject3 = repliesArray.getJSONObject(x);
                        if (!jsonObject3.getString("kind").equals("t1")) break;
                        JSONObject jsonObject4 = jsonObject3.getJSONObject("data");
                        RedditComment nestedReply = parseNestedReply(jsonObject4);
                        nestedReplies.add(nestedReply);
                    }
                }
            }

            RedditComment resultRedditComment = new RedditComment(author, convertedTime, score,
                    body, id, likes, link_permalink, depth, nestedReplies, resultGildings);
            resultRedditCommentList.add(resultRedditComment);
        }

        return resultRedditCommentList;
    }

    private static RedditComment parseNestedReply(JSONObject jsonObject) throws JSONException {
        String author = jsonObject.getString("author");
        long timeSinceCreated = jsonObject.getLong("created_utc");
        String convertedTime = DateUtils.getTimeAgo(timeSinceCreated);
        String score = jsonObject.getString("score");
        String body = jsonObject.getString("body");
        String id = jsonObject.getString("name");
        String likes = jsonObject.getString("likes");
        int depth = jsonObject.getInt("depth");

        String link_permalink = null;
        if (jsonObject.has("link_permalink")) link_permalink = jsonObject.getString("link_permalink");
        Gildings resultGildings = null;
        String silverCount = null;
        String goldCount = null;
        String platinumCount = null;

        if (jsonObject.has("gildings")) {
            JSONObject gildings = jsonObject.getJSONObject("gildings");
            if (gildings.has("gid_1")) silverCount = gildings.getString("gid_1");
            if (gildings.has("gid_2")) goldCount = gildings.getString("gid_2");
            if (gildings.has("gid_3")) platinumCount = gildings.getString("gid_3");

            resultGildings = new Gildings(silverCount, goldCount, platinumCount);
        }

        ArrayList<RedditComment> nestedReplies = new ArrayList<>();
        //CREATE REPLY CHAIN
        if (!jsonObject.get("replies").equals("")) {
            if (jsonObject.getJSONObject("replies").length() != 0) {
                JSONObject repliesObject = jsonObject.getJSONObject("replies");
                JSONObject repliesData = repliesObject.getJSONObject("data");

                JSONArray repliesArray = repliesData.getJSONArray("children");

                for (int x = 0; x < repliesArray.length(); x++) {
                    JSONObject jsonObject3 = repliesArray.getJSONObject(x);
                    if (!jsonObject3.getString("kind").equals("t1")) break;
                    JSONObject jsonObject4 = jsonObject3.getJSONObject("data");
                    RedditComment nestedReply = parseNestedReply(jsonObject4);
                    nestedReplies.add(nestedReply);
                }
            }
        }


        RedditComment resultRedditComment = new RedditComment(author, convertedTime, score,
                body, id, likes, link_permalink, depth, nestedReplies, resultGildings);

        return resultRedditComment;
    }

    public static ArrayList<RedditSentMail> parseRedditSentMail(String rawJson) throws JSONException {
        ArrayList<RedditSentMail> redditSentMailArrayList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
        JSONArray jsonArray = jsonObject1.getJSONArray("children");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
            if (!jsonObject2.getString("kind").equals("t4")) continue;

            JSONObject jsonObject3 = jsonObject2.getJSONObject("data");

            String subject = jsonObject3.getString("subject");
            long timeSincePost = jsonObject3.getLong("created_utc");
            String convertedTimeSincePost = DateUtils.getTimeAgo(timeSincePost);
            String destination = jsonObject3.getString("dest");
            String body = jsonObject3.getString("body");

            RedditSentMail redditSentMail = new RedditSentMail(destination, convertedTimeSincePost, subject, body);
            redditSentMailArrayList.add(redditSentMail);
        }


        return redditSentMailArrayList;
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

        //Example URL to get top 25 most popular videos
        //https://www.googleapis.com/youtube/v3/videos?part=snippet&maxResults=25&chart=mostPopular&key=AIzaSyD0c6zzMUaBltdZCQ-GJYjdZKodBL74BxQ

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
            String videoDescription = videoSnippet.getString("description");
            String channelId = videoSnippet.getString("channelId");

            String likeCount = "";
            String dislikeCount = "";

            JSONObject videoStatistics = jsonObject1.getJSONObject("statistics");
            String viewCount = videoStatistics.getString("viewCount");
            if (videoStatistics.has("likeCount")) likeCount = videoStatistics.getString("likeCount");
            if (videoStatistics.has("dislikeCount")) dislikeCount = videoStatistics.getString("dislikeCount");
            String commentCount = videoStatistics.getString("commentCount");

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

            /*
            DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String string1 = publishedDate;
            Date result1;
            try {
                result1 = df1.parse(string1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String output = new DateTime( DateTimeZone.UTC );


            publishedDate = DateUtils.getTimeAgo(convertedTime);
            */

            YoutubeVideo resultVideo = new YoutubeVideo(title, channelTitle, publishedDate, videoDescription,
                    viewCount, likeCount, dislikeCount, commentCount,
                    thumbnailUrl, channelId, videoId);
            resultList.add(resultVideo);
        }

        return resultList;
    }

    public static YoutubeChannel parseYoutubeChannel(String rawJson) throws JSONException {
        //
        YoutubeChannel resultChannel = null;

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONArray jsonArray = jsonObject.getJSONArray("items");

        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
        String channelId = jsonObject1.getString("id");
        JSONObject jsonObject2 = jsonObject1.getJSONObject("snippet");
        String channelTitle = jsonObject2.getString("title");
        String description = jsonObject2.getString("description");
        String timeCreated = jsonObject2.getString("publishedAt");
        JSONObject jsonObject3 = jsonObject2.getJSONObject("thumbnails");
        JSONObject jsonObject4 = jsonObject3.getJSONObject("medium");
        String chaanelIconUrl = jsonObject4.getString("url");

        resultChannel = new YoutubeChannel(channelId, channelTitle, description, timeCreated, chaanelIconUrl);

        return resultChannel;
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

    public static ArrayList<SearchResultSubreddit> parseSubredditSearchResult(String data) throws JSONException {
        //endpoint example: https://www.reddit.com/subreddits/search.json?q=dbz
        ArrayList<SearchResultSubreddit> resultsList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(data);
        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
        JSONArray jsonArray = jsonObject1.getJSONArray("children");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
            if (!jsonObject2.getString("kind").equals("t5")) {
                continue;
            }
            JSONObject jsonObject3 = jsonObject2.getJSONObject("data");
            String subredditName = jsonObject3.getString("display_name");
            String publicDescription = jsonObject3.getString("public_description");
            String subscriberCount = jsonObject3.getString("subscribers");
            long timeSinceCreation = jsonObject3.getLong("created_utc");
            String convertedTime = DateUtils.getTimeAgo(timeSinceCreation);


            resultsList.add(new SearchResultSubreddit(subredditName, subscriberCount, publicDescription, convertedTime));
        }

        return resultsList;
    }

    public static ArrayList<RedditUserProfile> parseRedditUserProfiles(String data) throws JSONException {
        ArrayList<RedditUserProfile> resultsList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(data);
        JSONObject dataObject = jsonObject.getJSONObject("data");
        JSONArray jsonArray = dataObject.getJSONArray("children");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            if (!jsonObject1.getString("kind").equals("t2")) continue;
            JSONObject jsonObject2 = jsonObject1.getJSONObject("data");
            if (jsonObject2.has("is_suspended")) continue;

            JSONObject jsonObject3 = jsonObject2.getJSONObject("subreddit");
            String publicDescription = jsonObject3.getString("public_description");

            String name = jsonObject2.getString("name");
            String iconUrl = jsonObject2.getString("icon_img");
            long timeSinceCreation = jsonObject2.getLong("created_utc");
            String karmaCount = String.valueOf(jsonObject2.getInt("comment_karma") + jsonObject2.getInt("link_karma"));

            iconUrl = iconUrl.split("\\?")[0];
            String convertedTime = DateUtils.getTimeAgo(timeSinceCreation);

            resultsList.add(new RedditUserProfile(name, publicDescription, iconUrl, convertedTime, karmaCount));
        }

        return resultsList;
    }
}
