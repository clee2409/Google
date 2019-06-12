package com.example.android.Tubeddit;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.android.Tubeddit.widget.RedditWidgetProvider.ACTION_OPEN_LINK;
import static com.example.android.Tubeddit.widget.RedditWidgetProvider.EXTRA_PERMALINK;

// TODO: 5/7/2019
/*
TO DO FEATURES:
        - Add "git push origin Posts" tab to User Profile Fragmengit credential-manager uninstallt
        - Make links in comments clickable
        - Add Fullscreen Image viewing on click
        - Collapse Comment Reply Tree  option
        - Fix lack of internet handling
        - Notifications On Inbox Receiving Mail
        - Show Gildings on comments/posts
        - Ability to post content

        TO DO UI ENHANCEMENTS -
        - Make Toolbar peek on scroll up in RedditCardDetail (Collapsing toolbar) and then disapear on scrolldown
        - Append new reply view on the bottom of a replied comment
        - Convert RedditCardDetailFragment layout into constraint layout and constrain "add comment" to bottom
        - upvoting scorecount UI
        - Display listview items properly for widget
        - Set onclicklisteners or right adapter for RedditUserProfile
        - Add loading indicator for comment list
        - Pressing back from RedditCardDetail page briefly shows the standard current sort before reverting to the compact sort
        - Currently selected spinner items have no effect on toggling it again (Either remove them from spinner or give it a refresh function)
        - cache exoplayer onsave
        - Add offline-handling for Reply/Comment buttons (does not crash but it returns a "error 401" network response instead of post

        TO DO CODE READABILITY
        - Simplify MainPageFragment "BindAuth" Views and "BindDefault" views into one
        - Maybe make spinner handling into one function

        TO DO OPTIMIZATION
        - Cache userdata and make it call only once to get user profile and subreddits (currently calls it twice)
        - Remove the lag on loading comments on RedditCardDetail (maybe make a comment limit or asynctask it)
        - Possibly Memory leak, maybe Spinner and Picasso caches don't clear themselves?

        FATAL SCENARIOS TO FIX
        - Memory Leak on too much recycling pictures leads to crashes
        - If Reddit is on downtime, any network call returns HTML instead of Json, leading to a crash
*/

public class MainActivity extends AppCompatActivity {

    public static final String frontpageUrl = "https://www.reddit.com/.json";
    public static final String ACTION_BAR_TITLE_MAIN_PAGE = "Main Page";
    public static final String SHARED_PREFERENCES_NAME = "Shared Preferences Name";
    private static final int REQUEST_CODE_SIGN_IN = 50;

    public static final String ADMOB_APP_ID = "ca-app-pub-9010336558845846~5093922463";
    private static final String ADMOB_UNIT_ID = "ca-app-pub-9010336558845846/5190125385";

    private Toolbar mToolbar;
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    DrawerLayout mDrawerLayout;
    private String mQuery;
    private String mRedditUser;
    private AdView mAdView;
    private FusedLocationProviderClient mFusedLocationClient;


    private RedditMainPageFragment mRedditCardNewsfeedFragment = new RedditMainPageFragment();
    private YoutubeMainPageFragment mYoutubeMainPageFragment = new YoutubeMainPageFragment();

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d("Handle Intent", intent.getAction());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mQuery = query;
            Log.d("searched", query);

            RedditSearchFragment fragment = RedditSearchFragment.newInstance(mQuery);
            mFragmentManager.beginTransaction()
                    .replace(findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                    .addToBackStack(null)
                    .commit();
        }


        if (ACTION_OPEN_LINK.equals(intent.getAction())) {
            String permalink = intent.getStringExtra(EXTRA_PERMALINK);
            String jsonUrl;
            boolean isLoggedIn = false;

            if (getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                    .contains(RedditMainPageFragment.SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED)) {
                jsonUrl = "https://oauth.reddit.com" + permalink + ".json";
                isLoggedIn = true;
            } else {
                jsonUrl = "https://www.reddit.com" + permalink + ".json";
            }
            Log.d("Activity Handle", "Action Open Link Url: " + jsonUrl);
            NetworkingUtils.tryAuthGet(jsonUrl, isLoggedIn, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    RedditCard redditCard = null;
                    try {
                        redditCard = JsonUtils.parseRedditCards(response.body().string()).get(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RedditCardDetailFragment fragment = RedditCardDetailFragment.newInstance(redditCard);
                    mFragmentManager.beginTransaction()
                            .replace(findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
            });

        }
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
            AuthorizationException ex = AuthorizationException.fromIntent(data);

            //mYoutubeAuthState = new AuthState(resp, ex);

            AuthState authState = new AuthState(resp, ex);
            writeAuthState(authState);

            if (resp != null) {
                Log.d("authresult", resp.toString());
                final AuthorizationService service = new AuthorizationService(this);
                service.performTokenRequest(resp.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {

                    @Override
                    public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                        AuthState authState1 = readAuthState();
                        authState1.update(response, ex);
                        writeAuthState(authState1);
                        Log.i("tokenRequest", String.format("Token Response [ Access Token: %s, Refresh Token: %s ]", response.accessToken, response.refreshToken));
                        //Example url
                        final String authCode = response.accessToken;
                        final String example = "https://www.googleapis.com/youtube/v3/activities?part=contentDetails&maxResults=10&home=true&key=AIzaSyD0c6zzMUaBltdZCQ-GJYjdZKodBL74BxQ";
                        readAuthState().performActionWithFreshTokens(service, new AuthState.AuthStateAction() {
                            @Override
                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                                NetworkingUtils.innerAuthGet(example, accessToken, new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        Log.d("response", response.body().string());
                                    }
                                });
                            }
                        });

                    }
                });

            } else {
                Log.d("authresult", "failed");
                Log.d("authresult", ex.toString());
            }

        }
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NetworkingUtils myClass = new NetworkingUtils(this);

        /*
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        */

        RedditMainPageFragment fragment = RedditMainPageFragment.newInstance(null);
        RedditMainPageFragment test = (RedditMainPageFragment) mFragmentManager
                .findFragmentByTag(RedditMainPageFragment.FRAGMENT_TAG_REDDIT_MAIN_PAGE);

        /*
        if (widget_layout == null) {
            Log.d("newsfeed", "created");
            mFragmentManager.beginTransaction()
                    .add(R.id.reddit_activity_fragment_container, fragment, RedditMainPageFragment.FRAGMENT_TAG_REDDIT_MAIN_PAGE)
                    .commit();
        }
        */
        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                    .add(R.id.reddit_activity_fragment_container, fragment, RedditMainPageFragment.FRAGMENT_TAG_REDDIT_MAIN_PAGE)
                    .commit();
        }

        /*
        if (getSupportFragmentManager().findFragmentByTag(RedditMainPageFragment.FRAGMENT_TAG_REDDIT_MAIN_PAGE) == null ) {
            mFragmentManager.beginTransaction()
                    .add(R.id.reddit_activity_fragment_container, fragment, RedditMainPageFragment.FRAGMENT_TAG_REDDIT_MAIN_PAGE)
                    .commit();
        }
        */
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_reddit_activity, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.menu_action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                searchItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        //mFragmentManager.popBackStack();
                        setItemsVisibility(menu, searchItem, true);
                        return true;  // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        setItemsVisibility(menu, searchItem, false);
                        return true;  // Return true to expand action view
                    }
                });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_select_youtube:
                mFragmentManager.beginTransaction()
                        .replace(R.id.reddit_activity_fragment_container, mYoutubeMainPageFragment, YoutubeMainPageFragment.FRAGMENT_TAG_YOUTUBE_MAIN_PAGE)
                        .commit();
                return true;

            case R.id.menu_action_select_reddit:
                mFragmentManager.beginTransaction()
                        .replace(R.id.reddit_activity_fragment_container, mRedditCardNewsfeedFragment, RedditMainPageFragment.FRAGMENT_TAG_REDDIT_MAIN_PAGE)
                        .commit();
                return true;

            case android.R.id.home:
                mDrawerLayout = findViewById(R.id.drawer_layout);
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
                else mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }

        return false;
    }

    /*
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_in:
                redditLoginAndAuth();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.reddit_profile:
                //youtubeLoginAndAuth();
                RedditUserProfileFragment redditProfileFragment = RedditUserProfileFragment.newInstance(mRedditUser);
                mFragmentManager.beginTransaction()
                        .replace(R.id.reddit_activity_fragment_container, redditProfileFragment)
                        .commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.reddit_inbox:
                RedditInboxFragment redditInboxFragment = new RedditInboxFragment();
                mFragmentManager.beginTransaction()
                        .replace(R.id.reddit_activity_fragment_container, redditInboxFragment)
                        .commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @NonNull public AuthState readAuthState() {
        SharedPreferences authPrefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String stateJson = authPrefs.getString(SHARED_PREFERENCES_YOUTUBE_AUTH_STATE, null);
        if (stateJson != null) {
            try {
                return AuthState.jsonDeserialize(stateJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //else
        return new AuthState();
    }

    public void writeAuthState(@NonNull AuthState state) {
        Log.d("writeAuthState", state.jsonSerializeString());
        SharedPreferences authPrefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        authPrefs.edit()
                .putString(SHARED_PREFERENCES_YOUTUBE_AUTH_STATE, state.jsonSerializeString())
                .commit();
    }
    */

    private void newYoutubeLoginAndAuth() {
        String clienttId = "102995914155-umpr83819qd25ai5uepduh0mkolofnn1.apps.googleusercontent.com";
        String youtubeScope = "https://www.googleapis.com/auth/youtube";
        String redirectUri = "com.example.android.Tubeddit:/oauth2redirect";
        Uri myUri = Uri.parse(redirectUri);

        String loginUrl2 = "https://accounts.google.com/o/oauth2/v2/auth";

        String loginUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "scope=" + youtubeScope + "&" +
                "response_type=code&" +
                "redirect_uri=" + redirectUri + "&" +
                "client_id=" + clienttId;

        String tokenEndpoint = "https://www.googleapis.com/oauth2/v4/token";

        AuthorizationServiceConfiguration serviceConfig =
                new AuthorizationServiceConfiguration(
                        Uri.parse(loginUrl2), // authorization endpoint
                        Uri.parse(tokenEndpoint)); // token endpoint

        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfig, // the authorization service configuration
                        clienttId, // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        myUri);// the redirect URI to which the auth response is sent

        AuthorizationRequest authRequest = authRequestBuilder
                .setScope(youtubeScope)
                .build();

        Log.d("checkAuthUrl", authRequest.toString());

        AuthorizationService authService = new AuthorizationService(this);
        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, REQUEST_CODE_SIGN_IN);
    }

    /*
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Log.d("loggedIn", "true");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("log in", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

    private void youtubeLoginAndAuth() {
        String clienttId = "102995914155-umpr83819qd25ai5uepduh0mkolofnn1.apps.googleusercontent.com";
        String youtubeScope = "https://www.googleapis.com/auth/youtube";
        String webClientId = "102995914155-384h72a4gp6ea6f9g33j84skhsh27kns.apps.googleusercontent.com";

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(youtubeScope))
                .build();


        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //UpdateUI(account)

        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
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
                    NetworkingUtils.getToken(postUrl, postData, new okhttp3.Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String string = response.body().string();
                                        Log.d("ResponseTest", string);
                                        JSONObject jsonObject = new JSONObject(string);

                                        String accessToken = jsonObject.getString("access_token");
                                        String refreshToken = jsonObject.getString("refresh_token");
                                        long dateInSeconds = (new Date().getTime())/1000;

                                        SharedPreferences.Editor edit = getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE)
                                                .edit();
                                        edit.putString(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN, accessToken);
                                        edit.putString(SHARED_PREFERENCES_REDDIT_REFRESH_TOKEN, refreshToken);
                                        edit.putLong(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED, dateInSeconds);
                                        edit.commit();
                                        auth_dialog.dismiss();
                                        mFragmentManager.beginTransaction()
                                                .detach(mRedditCardNewsfeedFragment)
                                                .attach(mRedditCardNewsfeedFragment)
                                                .commit();
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
                    Toast.makeText(getParent(), "Error Occured", Toast.LENGTH_SHORT).show();
                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize");
        auth_dialog.setCancelable(true);
    }
    */
    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

}
