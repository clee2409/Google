package com.example.android.Tubeddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.Tubeddit.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

import static com.example.android.Tubeddit.RedditMainPageFragment.SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN;
import static com.example.android.Tubeddit.RedditMainPageFragment.SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED;
import static com.example.android.Tubeddit.RedditMainPageFragment.SHARED_PREFERENCES_REDDIT_REFRESH_TOKEN;

public class NetworkingUtils {
    private Context mContext;
    private static SharedPreferences mSharedPreferences;

    public NetworkingUtils(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    private static OkHttpClient mClient = new OkHttpClient();

    public static String synchronousGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = mClient.newCall(request).execute();

        return response.body().string();
    }

    public static Call newGet(String url, Callback callback) {
        Log.d("Network call url", url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = mClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call innerAuthGet(String url, String authCode, Callback callback) {
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


    public static Call post(String url, RequestBody body, Callback callback) {
        /*
        final MediaType JSON
                = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, message);
        */
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " +
                        mSharedPreferences.getString(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN, null))
                .build();

        Log.d("request message", request.body().toString());

        Call call = mClient.newCall(request);
        call.enqueue(callback);
        return call;
    }


    public static Call getToken(String url, String message, Callback callback) {
        //Used to get Auth Bearer Token and Refresh Token
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

    public static void tryAuthGet(final String url, boolean authenticated, Callback callback) {
        if (authenticated) {
            authGet(url, callback);
        }
        else {
            newGet(url, callback);
        }
    }


    public static void authGet(final String url, final Callback callback) {
        Log.d("Network call url", url);
        //Attempts to Refresh Token if it's expired, then network get with the new auth code.
        //Otherwise network get normally.
        String accessToken = mSharedPreferences.getString(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN, null);

        if (!isAuthExpired()) {
            innerAuthGet(url, accessToken, callback);
        }
        else {
            //REFRESH AUTH TOKEN IF EXPIRED
            String accessTokenUrl = "https://www.reddit.com/api/v1/access_token";
            String refreshToken = mSharedPreferences.getString(SHARED_PREFERENCES_REDDIT_REFRESH_TOKEN, null);
            String postMessage = "grant_type=refresh_token&refresh_token=" + refreshToken;

            NetworkingUtils.getToken(accessTokenUrl, postMessage, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    Log.d("checkthispls", responseString);
                    JSONObject jsonObject;
                    String refreshedAccessToken = null;
                    try {
                        jsonObject = new JSONObject(responseString);
                        refreshedAccessToken = jsonObject.getString("access_token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Long curentDateInSeconds = (new Date().getTime()) / 1000;
                    mSharedPreferences.edit()
                            .putString(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN, refreshedAccessToken)
                            .putLong(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED, curentDateInSeconds)
                            .commit();

                    innerAuthGet(url, refreshedAccessToken, callback);
                    //Callback do your own thing after asnyc task finishes
                }
            });
        }
    }

    private static boolean isAuthExpired() {
        Long dateRetrievedInSeconds = mSharedPreferences.getLong(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED, 0);
        Long curentDateInSeconds = (new Date().getTime())/1000;

        return (curentDateInSeconds - dateRetrievedInSeconds) >= 3600;
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
