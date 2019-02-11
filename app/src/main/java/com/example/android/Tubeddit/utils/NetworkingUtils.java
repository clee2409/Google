package com.example.android.Tubeddit.utils;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class NetworkingUtils {

    private static OkHttpClient mClient = new OkHttpClient();

    public static Call newFetch(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = mClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call newFetch(String url, String authCode, Callback callback) {

        Log.d("newGetAuth", authCode);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authCode)
                .build();

        Call call = mClient.newCall(request);
        call.enqueue(callback);
        return call;
        }

    public static void get(OKHttpHandler.AsyncResponse delegate, String... url) {
        OKHttpHandler handler = new OKHttpHandler(delegate);
        handler.execute(url);
    }

    public static Call post(String url, String message, Callback callback) {
        final MediaType JSON
                = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, message);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = mClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call getToken(String url, String message, Callback callback) {
        final String clientId = "UVcP5_rh69pwAg";
        final String clientSecret = "";

        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        if (response.request().header("Authorization") != null) {
                            Log.d("checkauthheader", "returned null");
                            return null; // Give up, we've already attempted to authenticate.
                        }

                        Log.d("authcheck1","Authenticating for response: " + response);
                        Log.d("authcheck2","Challenges: " + response.challenges());
                        String credential = Credentials.basic(clientId, clientSecret);
                        return response.request().newBuilder()
                                .header("Authorization", credential)
                                .build();
                    }
                })
                .build();

        final MediaType URLENCODED
                = MediaType.get("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(URLENCODED, message);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
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

        private String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = mClient.newCall(request).execute();
            return response.body().string();
        }
    }
}
