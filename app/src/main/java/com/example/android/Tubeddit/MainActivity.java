package com.example.android.Tubeddit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.android.Tubeddit.utils.NetworkingUtils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final String testUrl = "https://www.reddit.com/user/bobbybryce/comments/.json";
    public static final String secondUrl = "https://www.reddit.com/user/wroughtironhero/comments/.json";
    public static final String frontpageUrl = "https://www.reddit.com/.json";

    private Toolbar mToolbar;
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    DrawerLayout mDrawerLayout;

    private RedditNewsfeedFragment mRedditCardNewsfeedFragment = new RedditNewsfeedFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            case R.id.action_select_content:
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
                try {
                    NetworkingUtils.run(
                            "https://www.reddit.com/r/todayilearned/comments/alqhng/today_i_learned_that_a_czech_cult_member_was/.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void redditLoginAndAuth() {
        Dialog auth_dialog = new Dialog(this);
        auth_dialog.setContentView(R.layout.auth_dialog);
        WebView web = auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);

        String loginUrl = "https://www.reddit.com/api/v1/authorize.compact?client_id=UVcP5_rh69pwAg&response_type=code&" +
                "state=RANDOM_STRING&redirect_uri=https://www.reddit.com/&duration=permanent&scope=identity,privatemessages,mysubreddits";

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

                if (url.contains("?code=") || url.contains("&code=")) {
                    Uri uri = Uri.parse(url);
                    String authCode = uri.getQueryParameter("code");
                    Intent intent = new Intent();
                    intent.putExtra("code",authCode);

                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize");
        auth_dialog.setCancelable(true);
    }


}
