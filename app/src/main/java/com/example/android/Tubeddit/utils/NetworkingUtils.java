package com.example.android.Tubeddit.utils;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkingUtils {

    private static OkHttpClient mClient = new OkHttpClient();

    public static String post(String url, String json) throws IOException {
        final MediaType JSON
                = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    public static void get(OKHttpHandler.AsyncResponse delegate, String... url) {
        OKHttpHandler handler = new OKHttpHandler(delegate);
        handler.execute(url);
    }

    public static String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = mClient.newCall(request).execute();
        return response.body().string();
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
}
