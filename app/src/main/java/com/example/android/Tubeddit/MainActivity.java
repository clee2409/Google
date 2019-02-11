package com.example.android.Tubeddit;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.android.Tubeddit.utils.NetworkingUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String testUrl = "https://www.reddit.com/user/bobbybryce/comments/.json";
    public static final String secondUrl = "https://www.reddit.com/user/wroughtironhero/comments/.json";
    public static final String frontpageUrl = "https://www.reddit.com/.json";
    public static final String SHARED_PREFERENCES_NAME = "Shared Preferences Name";
    public static final String SHARED_PREFERENCES_REDDIT_AUTH_CODE = "reddit api auth code pref";


    private Toolbar mToolbar;
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    DrawerLayout mDrawerLayout;
    private Activity mActivity = this;
    private SharedPreferences mSharedPreferences;

    private RedditNewsfeedFragment mRedditCardNewsfeedFragment = new RedditNewsfeedFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentManager.beginTransaction()
                .add(R.id.main_activity_fragment_container, mRedditCardNewsfeedFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_select_youtube:
                YoutubeMainPageFragment fragment = new YoutubeMainPageFragment();
                mFragmentManager.beginTransaction().replace(R.id.main_activity_fragment_container, fragment)
                        .commit();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_in:
                Toast.makeText(this, "logged in", Toast.LENGTH_SHORT).show();
                redditLoginAndAuth();
                return true;
            case R.id.reddit_profile:
                callRedditApi();
                Toast.makeText(this, "profile clicked", Toast.LENGTH_SHORT).show();
                return true;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void callRedditApi() {
        String authCode = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).getString(SHARED_PREFERENCES_REDDIT_AUTH_CODE, null);
        NetworkingUtils.newFetch("https://oauth.reddit.com/", authCode, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("newGetCheck", response.body().string());
            }
        });
    }

    private void redditLoginAndAuth() {
        final Dialog auth_dialog = new Dialog(this);
        auth_dialog.setContentView(R.layout.auth_dialog);
        WebView web = auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);

        String scopeList = "identity,privatemessages,mysubreddits,read,report,save,creddits";

        String loginUrl = "https://www.reddit.com/api/v1/authorize.compact?client_id=UVcP5_rh69pwAg&response_type=code&" +
                "state=RANDOM_STRING&redirect_uri=https://www.reddit.com&duration=permanent" +
                "&scope=" + scopeList;

        String clientId = "UVcP5_rh69pwAg";
        final String redirectUri = "https://www.reddit.com";

        String newUrl = "https://www.reddit.com/api/v1/authorize?client_id=UVcP5_rh69pwAg" +
                "&response_type=token" +
                "&state=RANDOM_STRING&redirect_uri=https://www.reddit.com&scope=identity,privatemessages,mysubreddits";

        web.loadUrl(loginUrl);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.d("pageFinishedCheck", url);

                if (url.contains("?code=") || url.contains("&code=")) {
                    Uri uri = Uri.parse(url);
                    String authCode = uri.getQueryParameter("code");
                    Log.d("checkAuthCode", authCode);

                    String postUrl = "https://www.reddit.com/api/v1/access_token";
                    String postData = "grant_type=authorization_code&code=" + authCode + "&redirect_uri=" + redirectUri;
                    NetworkingUtils.getToken(postUrl, postData, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String string = response.body().string();
                                        Log.d("ResponseTest", string);
                                        JSONObject jsonObject = new JSONObject(string);
                                        String token = jsonObject.getString("access_token");
                                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                                        edit.putString(SHARED_PREFERENCES_REDDIT_AUTH_CODE, token);
                                        edit.commit();
                                        auth_dialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    // resultIntent.putExtra("code", authCode);
                    // authComplete = true;
                    // setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize");
        auth_dialog.setCancelable(true);
    }
}
