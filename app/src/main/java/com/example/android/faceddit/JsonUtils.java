package com.example.android.faceddit;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JsonUtils {

    private static OkHttpClient mClient = new OkHttpClient();

    public static void fetchRawJson(OKHttpHandler.AsyncResponse delegate, String... url){
        OKHttpHandler handler = new OKHttpHandler(delegate);
        handler.execute(url);
    }

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
            String thumbnail = newsfeedData1.getString("thumbnail");
            String score = newsfeedData1.getString("score");
            String author = newsfeedData1.getString("author");

            RedditCard redditCard = new RedditCard(title, subreddit, thumbnail, score, author);
            resultArrayList.add(redditCard);
        }
        return resultArrayList;
    }

     public static String parseUserName(String rawJson) throws JSONException {
        String username;

        JSONObject jsonObject = new JSONObject(rawJson);
        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
        JSONArray jsonArray = jsonObject1.getJSONArray("children");
        JSONObject jsonObject2 = jsonArray.getJSONObject(0);
        JSONObject jsonObject3 = jsonObject2.getJSONObject("data");

        username = jsonObject3.getString("body");

        return username;
    }

    public static class OKHttpHandler extends AsyncTask<String, Void, String> {

        public interface AsyncResponse {
            void processFinish(String output);
        }

        public OKHttpHandler(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(String... strings) {

            String output = null;

            try {
                output = run(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            delegate.processFinish(result);

        }
    }

    static String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

}
