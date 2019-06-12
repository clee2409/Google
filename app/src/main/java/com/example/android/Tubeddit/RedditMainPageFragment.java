package com.example.android.Tubeddit;

//This is in release
import android.Manifest;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.RedditUserProfile;
import com.example.android.Tubeddit.data.SearchResultSubreddit;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.example.android.Tubeddit.MainActivity.ACTION_BAR_TITLE_MAIN_PAGE;
import static com.example.android.Tubeddit.MainActivity.ADMOB_APP_ID;
import static com.example.android.Tubeddit.MainActivity.SHARED_PREFERENCES_NAME;

public class RedditMainPageFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener,
        LocationListener, View.OnTouchListener{

    public static final String FRAGMENT_TAG_REDDIT_MAIN_PAGE = "FRAGMENT_TAG_REDDIT_MAIN_PAGE";
    public static final String EXTRA_SUBREDDIT_NAME = "EXTRA_SUBREDDIT_NAME";
    private static final String PATH_NAME_HOT = "hot";
    private static final String PATH_NAME_NEW = "new";
    private static final String PATH_NAME_TOP = "top";
    private static final String PATH_NAME_CONTROVERSIAL = "controversial";
    private static final String PATH_NAME_BEST = "best";
    private static final String PATH_NAME_HOUR = "hour";
    private static final String PATH_NAME_24_HOURS = "day";
    private static final String PATH_NAME_WEEK = "week";
    private static final String PATH_NAME_MONTH = "month";
    private static final String PATH_NAME_YEAR = "year";
    private static final String PATH_NAME_ALL_TIME = "all";


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    //best is a field exclusive to the front page


    static final String STATE_SUBREDDIT_NAME = "STATE_SUBREDDIT_NAME";
    static final String STATE_IS_FRONT_PAGE = "STATE_IS_FRONT_PAGE";
    static final String STATE_CURRENT_SORT_BY = "STATE_CURRENT_SORT_BY";
    static final String STATE_JSON_RESPONSE_DATA = "STATE_JSON_RESPONSE_DATA";
    private static final String STATE_USER = "STATE_USER";
    private static final String STATE_LAYOUY_MANAGER_STATE = "STATE_LAYOUY_MANAGER_STATE";

    public static final String SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN = "reddit api access token pref";
    public static final String SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED = "time retrieved reddit access token";
    public static final String SHARED_PREFERENCES_REDDIT_REFRESH_TOKEN = "Reddit OAuth Refresh Token";
    private static final String STATE_COMPACT_PREFERENCE = "STATE_COMPACT_PREFERENCE";


    private static String mSubredditName = "";
    public static boolean mIsFrontPage;
    private Parcelable mLayoutManagerState;
    private String mJsonResponseData;
    //Default Value for sortby
    private String mCurrentSortBy = "";
    LocationCallback mLocationCallback;
    private Toolbar mToolbar;
    private String mQuery;
    private RedditUserProfile mRedditUser;

    List<String> mSubredditSubscriptions;


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    Spinner mSortBySpinner;
    Spinner mSortByTimeSpinner;
    Spinner mSortByLocationSpinner;

    private Boolean mIsLoggedIn;
    private boolean mCompactPreference;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mSaveLayoutState;

    private boolean mUserSelect = false;

    public RedditMainPageFragment() {
        // Required empty public constructor
    }

    public static RedditMainPageFragment newInstance(String subredditUrl) {
        RedditMainPageFragment fragment = new RedditMainPageFragment();
        if (subredditUrl != null) {
            Bundle args = new Bundle(1);
            args.putString(EXTRA_SUBREDDIT_NAME, subredditUrl);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_SUBREDDIT_NAME, mSubredditName);
        outState.putString(STATE_CURRENT_SORT_BY, mCurrentSortBy);
        outState.putBoolean(STATE_COMPACT_PREFERENCE, mCompactPreference);
        outState.putParcelable(STATE_LAYOUY_MANAGER_STATE, mLinearLayoutManager.onSaveInstanceState());
        if (mJsonResponseData != null) {
            outState.putString(STATE_JSON_RESPONSE_DATA, mJsonResponseData);
        }
        if (mRedditUser != null && mIsLoggedIn) {
            Parcelable user = Parcels.wrap(mRedditUser);
            outState.putParcelable(STATE_USER, user);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mSubredditName = savedInstanceState.getString(STATE_SUBREDDIT_NAME, "");
            //mIsFrontPage = savedInstanceState.getBoolean(STATE_IS_FRONT_PAGE);
            mCurrentSortBy = savedInstanceState.getString(STATE_CURRENT_SORT_BY, "");
            mCompactPreference = savedInstanceState.getBoolean(STATE_COMPACT_PREFERENCE);
            mLayoutManagerState = savedInstanceState.getParcelable(STATE_LAYOUY_MANAGER_STATE);
            mJsonResponseData = savedInstanceState.getString(STATE_JSON_RESPONSE_DATA, null);
            if (savedInstanceState.containsKey(STATE_USER)) {
                Parcelable parcelable = savedInstanceState.getParcelable(STATE_USER);
                mRedditUser = Parcels.unwrap(parcelable);
            }
        }
        if (getArguments() != null && getArguments().containsKey(EXTRA_SUBREDDIT_NAME)) {
            mSubredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME, "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_main_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIsLoggedIn = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                .contains(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN);
        mIsFrontPage = (mSubredditName.equals("") || mSubredditName == null);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(ACTION_BAR_TITLE_MAIN_PAGE);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mRecyclerView = view.findViewById(R.id.reddit_card_feed_rv);
        //mRecyclerView.setItemViewCacheSize(25);
        //mRecyclerView.setDrawingCacheEnabled(true);
        //mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.onRestoreInstanceState(mLayoutManagerState);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        ToggleButton toggle = (ToggleButton) view.findViewById(R.id.view_preference_toggle_btn);
        //Default is card view, (2 bar icon) selectable is compact (list view icon)
        mCompactPreference = toggle.isChecked();
        toggle.setChecked(mCompactPreference);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCompactPreference = true;
                    refreshPage(mCurrentSortBy, true);
                } else {
                    mCompactPreference = false;
                    refreshPage(mCurrentSortBy, true);
                }
            }
        });

        mSortBySpinner = view.findViewById(R.id.reddit_sort_by_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.reddit_sort_by_array, R.layout.sortby_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortBySpinner.setAdapter(adapter);
        mSortBySpinner.setSelection(0, false);
        mSortBySpinner.setOnItemSelectedListener(this);
        mSortBySpinner.setOnTouchListener(this);

        mSortByTimeSpinner = view.findViewById(R.id.reddit_sort_by_time_spinner);
        ArrayAdapter<CharSequence> sortByTimeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.reddit_sort_by_time_array, R.layout.sortby_spinner_item);
        sortByTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortByTimeSpinner.setAdapter(sortByTimeAdapter);
        mSortByTimeSpinner.setSelection(1, false);
        mSortByTimeSpinner.setOnItemSelectedListener(this);
        mSortByTimeSpinner.setOnTouchListener(this);

        mSortByLocationSpinner = view.findViewById(R.id.reddit_sort_by_location_spinner);
        ArrayAdapter<CharSequence> sortByLocationAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.reddit_sort_by_location_array, R.layout.sortby_spinner_item);
        sortByLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortByLocationSpinner.setAdapter(sortByLocationAdapter);
        mSortByLocationSpinner.setSelection(1, false);
        mSortByLocationSpinner.setOnItemSelectedListener(this);
        mSortByLocationSpinner.setOnTouchListener(this);
        if (mSubredditName.equals("popular") && mSortBySpinner.getSelectedItem().toString().equals("Hot")) {
            mSortByLocationSpinner.setVisibility(View.VISIBLE);
        } else mSortByLocationSpinner.setVisibility(View.GONE);

        if (mCurrentSortBy.contains(PATH_NAME_CONTROVERSIAL) || mCurrentSortBy.contains(PATH_NAME_TOP)) {
            mSortByTimeSpinner.setVisibility(View.VISIBLE);
        } else mSortByTimeSpinner.setVisibility(View.GONE);
        if (mSubredditName.equals("popular") && mSortBySpinner.getSelectedItem().toString().equals("Hot")) {
            mSortByLocationSpinner.setVisibility(View.VISIBLE);
        } else mSortByLocationSpinner.setVisibility(View.GONE);

        Log.d("check pref", String.valueOf(mCompactPreference));

        /*
        Spinner spinner = view.findViewById(R.id.reddit_sort_by_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.reddit_sort_by_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0, false);
        spinner.setOnItemSelectedListener(this);
        */

        setUserNavHeader();
        getAndSetToolbarSpinner();
        if (mRedditUser == null && mIsLoggedIn) {
            mRedditUser = getUserProfile();
        }

        AdView adView = view.findViewById(R.id.adView);
        adView.bringToFront();
        MobileAds.initialize(getContext(), ADMOB_APP_ID);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("29B935DEB900277953A8290EF5939CB8").build();
        adView.loadAd(adRequest);

        if (mJsonResponseData == null) {
            Log.d("maincall", "true");
            if (mIsFrontPage) {
                if (mIsLoggedIn) {
                    bindAuthFrontPageUi(PATH_NAME_BEST);
                } else bindDefaultFrontPageUi(PATH_NAME_HOT);
            } else bindDefaultFrontPageUi(PATH_NAME_HOT);
        } else bindRecyclerView(mJsonResponseData);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mSubredditName.equals("")) {
            //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mSubredditName);
            //mSubredditSubscriptions.add(mSubredditName);
            //mSubredditSubscriptions.
            //ArrayAdapter<String> toolbarSpinnerAdapter = new ArrayAdapter<>(getActivity(),
             //       R.layout.sortby_spinner_item, mSubredditSubscriptions);

            //((ArrayAdapter<String>) ((Spinner) getView().findViewById(R.id.toolbar_spinner)).getAdapter()).add(mSubredditName);
            //        ColorDrawable color = new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark));
            //((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(color);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mSubredditName.equals("")) {
            //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(ACTION_BAR_TITLE_MAIN_PAGE);
            //ColorDrawable color = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
            //((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(color);
        }
        if (mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        if (!mUserSelect) return;
        mUserSelect = false;
        String dropdownItem = adapterView.getSelectedItem().toString();
        Log.d("SpinnerItemSelected", dropdownItem);
        //Hot, New, Top, Controversial
        if (adapterView.getId() == R.id.toolbar_spinner && !adapterView.getSelectedItem().toString().equals(mSubredditName)) {
            if (dropdownItem.equals("Home Page")) {
                Log.d("home page", "dropdown");
                Fragment fragment = RedditMainPageFragment.newInstance("");
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(getActivity().findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.d("nothomepage", "dropdown");
                Fragment fragment = RedditMainPageFragment.newInstance(dropdownItem);
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(getActivity().findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }

            /*
            Fragment fragment = RedditMainPageFragment.newInstance(dropdownItem);
            if (mIsFrontPage) {
                //add to backstack
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(getActivity().findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(getActivity().findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }
            */

        }

        if (adapterView.getId() == R.id.reddit_sort_by_location_spinner) {
            String sortby = ".json?geo_filter=";
            switch (adapterView.getSelectedItem().toString()) {
                case "My Location":
                    getLocation();
                    break;
                case "Everywhere":
                    refreshPage(sortby + "GLOBAL", false);
                    break;
                case "United States":
                    refreshPage(sortby + "US", false);
                    break;
                case "United Kingdom":
                    refreshPage(sortby + "GB", false);
                    break;
                case "Japan":
                    refreshPage(sortby + "JP", false);
                    break;
            }
        }

        //POSSIBLE CHOICES:
        //Past Hour, Past 24 Hours, Past Week, Past Month, Past Year, All Time
        if (adapterView.getId() == R.id.reddit_sort_by_time_spinner) {
            String sortBy = null;
            if (mCurrentSortBy.contains(PATH_NAME_TOP)) sortBy = "top/.json?sort=top&t=";
            if (mCurrentSortBy.contains(PATH_NAME_CONTROVERSIAL)) sortBy = "controversial/.json?sort=controversial&t=";
            switch (adapterView.getSelectedItem().toString()) {
                case "Past Hour":
                    refreshPage(sortBy + PATH_NAME_HOUR, false);
                    break;
                case "Past 24 Hours":
                    refreshPage(sortBy + PATH_NAME_24_HOURS, false);
                    break;
                case "Past Week":
                    refreshPage(sortBy + PATH_NAME_WEEK, false);
                    break;
                case "Past Month":
                    refreshPage(sortBy + PATH_NAME_MONTH, false);
                    break;
                case "Past Year":
                    refreshPage(sortBy + PATH_NAME_YEAR, false);
                    break;
                case "All Time":
                    refreshPage(sortBy + PATH_NAME_ALL_TIME, false);
                    break;
            }
        }

        if (adapterView.getId() == R.id.reddit_sort_by_spinner) {
            switch (adapterView.getSelectedItem().toString()) {
                case "Hot":
                    refreshPage(PATH_NAME_HOT + "/.json", false);
                    break;
                case "New":
                    refreshPage(PATH_NAME_NEW + "/.json", false);
                    break;
                case "Top":
                    refreshPage("top/.json?sort=top&t=day", false);
                    mSortByTimeSpinner.setSelection(1, false);
                    break;
                case "Controversial":
                    refreshPage("controversial/.json?sort=controversial&t=day",  false);
                    mSortByTimeSpinner.setSelection(1, false);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem toggleItem = menu.findItem(R.id.menu_action_toggle_reddit_or_youtube);
        toggleItem.setIcon(R.drawable.reddit_icon);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_refresh:
                if (mIsFrontPage) {
                    if (mIsLoggedIn) bindAuthFrontPageUi(mCurrentSortBy);
                    else bindDefaultFrontPageUi(mCurrentSortBy);
                } else bindDefaultFrontPageUi(mCurrentSortBy);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshPage(String sortBy, boolean keepState) {
        Log.d("page is", "refreshed");
        //Sort by must be the PATH static strings
        mCurrentSortBy = sortBy;
        mSaveLayoutState = keepState;
        if (mIsLoggedIn && mIsFrontPage) bindAuthFrontPageUi(sortBy);
        else bindDefaultFrontPageUi(sortBy);

        if (mCurrentSortBy.contains(PATH_NAME_CONTROVERSIAL) || mCurrentSortBy.contains(PATH_NAME_TOP)) {
            mSortByTimeSpinner.setVisibility(View.VISIBLE);
        } else mSortByTimeSpinner.setVisibility(View.GONE);
        if (mSubredditName.equals("popular") && mSortBySpinner.getSelectedItem().toString().equals("Hot")) {
            mSortByLocationSpinner.setVisibility(View.VISIBLE);
        } else mSortByLocationSpinner.setVisibility(View.GONE);
    }

    private void bindDefaultFrontPageUi(String sortBy) {
        String defaultUrl = "https://reddit.com/" + sortBy;

        if (!mIsFrontPage) {
            if (mIsLoggedIn) defaultUrl = "https://oauth.reddit.com/r/" + mSubredditName + "/" + sortBy;
            else defaultUrl = "https://reddit.com/r/" + mSubredditName + "/" + sortBy;
        }
        if (!defaultUrl.contains("/.json")) defaultUrl += "/.json";

        NetworkingUtils.tryAuthGet(defaultUrl, mIsLoggedIn, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();
                Log.d("chexnn", responseString);
                mJsonResponseData = responseString;
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bindRecyclerView(responseString);
                    }
                });
            }
        });
    }

    private void bindAuthFrontPageUi(String sortBy) {
        String loggedInFrontPageUrl = "https://oauth.reddit.com/" + sortBy;
        Log.d("jsoncall", loggedInFrontPageUrl);

        NetworkingUtils.authGet(loggedInFrontPageUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseString = response.body().string();
                mJsonResponseData = responseString;
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bindRecyclerView(responseString);
                    }
                });
            }
        });
    }

    private void bindRecyclerView(String data) {
        ArrayList<RedditCard> redditCardArrayList = null;

        try {
            redditCardArrayList = JsonUtils.parseRedditCards(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //mRecyclerView.removeAllViewsInLayout();
        Parcelable savedLayout = mLinearLayoutManager.onSaveInstanceState();


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                new LinearLayoutManager(getContext()).getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        RedditCardAdapter adapter = new RedditCardAdapter(redditCardArrayList, (AppCompatActivity) getActivity(), mCompactPreference);
        mRecyclerView.setAdapter(adapter);

        if (mSaveLayoutState) {
            mLinearLayoutManager.onRestoreInstanceState(savedLayout);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_in:
                redditLoginAndAuth();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.log_out:
                redditLogout();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.reddit_profile:
                RedditUserProfileFragment redditUserProfileFragment = RedditUserProfileFragment.newInstance(mRedditUser.mUserName);
                getFragmentManager().beginTransaction()
                        .replace(R.id.reddit_activity_fragment_container, redditUserProfileFragment)
                        .addToBackStack(null)
                        .commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.reddit_inbox:
                RedditInboxFragment redditInboxFragment = new RedditInboxFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.reddit_activity_fragment_container, redditInboxFragment)
                        .addToBackStack(null)
                        .commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(getActivity(), R.string.unable_to_get_location, Toast.LENGTH_SHORT).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private void getLocation() {
        Log.d("getlocation", "clicked");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            Log.d("locationchex", "pt2");

            //LocationManager mLocManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            //mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

            mLocationCallback = new LocationCallback() {

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (!locationAvailability.isLocationAvailable()) {
                        Toast.makeText(getActivity(), R.string.unable_to_get_location, Toast.LENGTH_SHORT).show();
                        //mFusedLocationClient.removeLocationUpdates(this);
                    } else {
                        Log.d("location", "is available");
                    }
                }

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.d("Locationresult", "procced");
                    if (locationResult == null) {
                        Log.d("callbackcheck", "isnull");
                        return;
                    }
                    for (final Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        // ...
                        Log.d("checkLocation", location.toString());
                        Log.d("Latitude: ", String.valueOf(location.getLatitude()));
                        Log.d("Longitude: ", String.valueOf(location.getLongitude()));
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());

                        Toast.makeText(getContext(), getString(R.string.location_latitude_and_longitude)  + latitude +", "+  longitude, Toast.LENGTH_SHORT).show();

                        if (android.location.Geocoder.isPresent()) {
                            Log.d("Geocoder :", "is present");
                            Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (addresses.size() > 0) {
                                String countryName = addresses.get(0).getCountryName();
                                Log.d("checkcountry", countryName);
                            }
                            else Log.d("checkcountry", "returning null");
                        }
                        else Log.d("Geocoder :", "is not present");

                        /*
                        new AsyncTask<Void, Void, Void>() {

                            Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());

                            @Override
                            protected Void doInBackground(Void... voids) {
                                List<Address> addresses = null;
                                try {
                                    addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1, false);
                                } catch (GeocoderException e) {
                                    e.printStackTrace();
                                }
                                Log.d("check this pls", addresses.get(0).getCountry());

                                String countryName = null;
                                if (addresses.size() > 0) {
                                    countryName = addresses.get(0).getCountry();
                                    Log.d(countryName, "checkcountry");
                                }

                                return null;
                            }
                        }.execute();
                        */
                        mFusedLocationClient.removeLocationUpdates(this);
                    }
                }
            };

            final LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);

            /*
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.d("locationchex", location.toString());
                                Toast.makeText(getActivity(), location.toString(), Toast.LENGTH_SHORT).show();
                                // Logic to handle location object
                            } else {
                                if (ActivityCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("location get", "failed");
                            Toast.makeText(getActivity(), "Location is not available", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                    */
        }

    }

    private void redditLogout() {
        Log.d("log out",  "attempt");
        getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit()
                .remove(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN)
                .remove(SHARED_PREFERENCES_REDDIT_REFRESH_TOKEN)
                .remove(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED)
                .commit();
        RedditMainPageFragment fragment = new RedditMainPageFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.reddit_activity_fragment_container, fragment).commit();
    }


    private void redditLoginAndAuth() {
        final Dialog auth_dialog = new Dialog(getContext());
        auth_dialog.setContentView(R.layout.auth_dialog);
        WebView web = auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);

        String scopeList = "identity,privatemessages,mysubreddits,read,report,save,creddits,vote,submit     ";
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
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String string = response.body().string();
                                        Log.d("ResponseTest", string);
                                        JSONObject jsonObject = new JSONObject(string);

                                        String accessToken = jsonObject.getString("access_token");
                                        String refreshToken = jsonObject.getString("refresh_token");
                                        long dateInSeconds = (new Date().getTime())/1000;

                                        SharedPreferences.Editor edit = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE)
                                                .edit();
                                        edit.putString(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN, accessToken);
                                        edit.putString(SHARED_PREFERENCES_REDDIT_REFRESH_TOKEN, refreshToken);
                                        edit.putLong(SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN_TIME_LAST_RETRIEVED, dateInSeconds);
                                        edit.commit();
                                        auth_dialog.dismiss();
                                        RedditMainPageFragment fragment = new RedditMainPageFragment();

                                        getFragmentManager().beginTransaction().
                                                replace(R.id.reddit_activity_fragment_container, fragment).commit();
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
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize");
        auth_dialog.setCancelable(true);
    }

    private void getAndSetToolbarSpinner() {
        mSubredditSubscriptions = new ArrayList<>();
        mSubredditSubscriptions.add("Home Page");
        if (!mSubredditName.equals("popular")) mSubredditSubscriptions.add(1, "popular");

        final String subredditsUrl = "https://oauth.reddit.com/subreddits/mine/.json";
        final Spinner toolbarSpinner = getView().findViewById(R.id.toolbar_spinner);

        if (!mIsLoggedIn) {
            ArrayAdapter<String> toolbarSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                    R.layout.title_spinner_item, mSubredditSubscriptions);

            toolbarSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            toolbarSpinner.setAdapter(toolbarSpinnerAdapter);
            toolbarSpinner.setSelection(0, false);
            toolbarSpinner.setOnItemSelectedListener(RedditMainPageFragment.this);
            toolbarSpinner.setOnTouchListener(this);

            if (!(mIsFrontPage)) {
                toolbarSpinnerAdapter.add(mSubredditName);

                int index = 0;

                for (int i = 0; i < toolbarSpinner.getCount(); i++) {
                    if (toolbarSpinner.getItemAtPosition(i).equals(mSubredditName)) {
                        index = i;
                    }
                }

                toolbarSpinner.setSelection(index);
                //toolbarSpinner.
            }
            return;
        }

        NetworkingUtils.authGet(subredditsUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("subreddits", result);

                ArrayList<SearchResultSubreddit> subreddits = null;
                try {
                    subreddits = JsonUtils.parseSubredditSearchResult(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < subreddits.size(); i++) {
                    if (i > 4) break;
                    mSubredditSubscriptions.add(subreddits.get(i).mSubredditName);
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> toolbarSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                                R.layout.title_spinner_item, mSubredditSubscriptions);
                        toolbarSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                        toolbarSpinner.setAdapter(toolbarSpinnerAdapter);
                        toolbarSpinner.setSelection(0, false);
                        toolbarSpinner.setOnItemSelectedListener(RedditMainPageFragment.this);
                        toolbarSpinner.setOnTouchListener(RedditMainPageFragment.this);

                        if (!(mIsFrontPage)) {
                            toolbarSpinnerAdapter.add(mSubredditName);

                            int index = 0;

                            for (int i=0;i<toolbarSpinner.getCount();i++){
                                if (toolbarSpinner.getItemAtPosition(i).equals(mSubredditName)){
                                    index = i;
                                }
                            }

                            toolbarSpinner.setSelection(index);
                            //toolbarSpinner.
                        }

                    }
                });
            }
        });
        //mSubredditSubscriptions;
    }

    private RedditUserProfile getUserProfile() {
        String userUrl = "https://oauth.reddit.com/api/v1/me";
        NetworkingUtils.authGet(userUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                JSONObject jsonObject = null;
                String user = null;
                String iconUrl = null;
                Log.d("userData", result);
                try {
                    jsonObject = new JSONObject(result);
                    user = jsonObject.getString("name");
                    iconUrl = jsonObject.getString("icon_img");
                    mRedditUser = new RedditUserProfile(user, null, iconUrl, null, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }

    private void setUserNavHeader() {
        //mDrawerLayout = ((ViewGroup) view.getParent().getParent().getParent()).findViewById(R.id.drawer_layout);
        //mNavigationView = ((ViewGroup) view.getParent().getParent().getParent()).findViewById(R.id.nav_view);

        mDrawerLayout = getActivity().findViewById(R.id.drawer_layout);
        mNavigationView = getActivity().findViewById(R.id.nav_view);
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.menu_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);

        View view = mNavigationView.getHeaderView(0);
        final TextView headerTitle = view.findViewById(R.id.navigation_header_title_tv);
        final ImageView headerIcon =  view.findViewById(R.id.navigation_header_icon_iv);
        TextView headerLoginPrompt = view.findViewById(R.id.navigation_header_login_prompt_tv);

        if (mIsLoggedIn) {
            mNavigationView.getMenu().findItem(R.id.log_out).setVisible(true);
            mNavigationView.getMenu().findItem(R.id.log_in).setVisible(false);
            headerLoginPrompt.setVisibility(View.GONE);

            String userUrl = "https://oauth.reddit.com/api/v1/me";
            NetworkingUtils.authGet(userUrl, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    JSONObject jsonObject = null;
                    String user = null;
                    String iconUrl = null;
                    Log.d("userData", result);
                    try {
                        jsonObject = new JSONObject(result);
                        user = jsonObject.getString("name");
                        iconUrl = jsonObject.getString("icon_img");
                    /*
                    getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                            .edit()
                            .putString(SHARED_PREFERENCES_REDDIT_USER, null)
                            .commit();
                    */

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    final String finalIconUrl = iconUrl;
                    final String finalUser = user;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            headerTitle.setText(finalUser);
                            Picasso.get().load(finalIconUrl).fit().into(headerIcon, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
                }
            });
        }
        else {
            //((LinearLayout) mNavigationView.getHeaderView(0)).setGravity(Gravity.CENTER);
            mNavigationView.getMenu().findItem(R.id.reddit_profile).setVisible(false);
            mNavigationView.getMenu().findItem(R.id.reddit_inbox).setVisible(false);
            headerIcon.setImageResource(R.drawable.guest_user);
            headerTitle.setText(R.string.guest);
            headerLoginPrompt.setText(R.string.login_prompt);
            headerLoginPrompt.setVisibility(View.VISIBLE);
            //headerTitle.setGravity(Gravity.CENTER);

        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        String message = String.format(
                "New Location \n Longitude: %1$s \n Latitude: %2$s",
                loc.getLongitude(), loc.getLatitude()
        );
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mUserSelect = true;
        return false;
    }

    public static class RedditCardAdapter extends RecyclerView.Adapter<RedditCardAdapter.ViewHolder> {
        ArrayList<RedditCard> mData;
        FragmentActivity mActivity;
        boolean mIsCompact;
        final String voteUrl = "https://oauth.reddit.com/api/vote";
        private SimpleExoPlayer mExoPlayer;
        DataSource.Factory mDataSourceFactory;


        public RedditCardAdapter(ArrayList<RedditCard> data, AppCompatActivity activity, boolean isCompact) {
            mData = data;
            mActivity = activity;
            mIsCompact = isCompact;
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(mActivity);
            mDataSourceFactory = new DefaultDataSourceFactory(mActivity,
                    Util.getUserAgent(mActivity, mActivity.getString(R.string.app_name)));

        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView titleTextView;
            TextView subredditTextView;
            ImageView thumbnailImageView;
            TextView scoreTextView;
            TextView authorTextView;
            TextView timePostedTextView;
            TextView commentCountTextView;
            ImageView previewImageView;

            ImageView upvoteButton;
            ImageView downvoteButton;

            PlayerView playerView;

            ViewHolder(View itemView) {
                super(itemView);
                this.titleTextView = itemView.findViewById(R.id.reddit_title_tv);
                this.subredditTextView = itemView.findViewById(R.id.reddit_subreddit_tv);
                this.thumbnailImageView = itemView.findViewById(R.id.reddit_thumbnailUrl_iv);
                this.scoreTextView = itemView.findViewById(R.id.reddit_score_tv);
                this.authorTextView = itemView.findViewById(R.id.reddit_author_tv);
                this.timePostedTextView = itemView.findViewById(R.id.reddit_time_since_created_tv);
                this.previewImageView = itemView.findViewById(R.id.reddit_preview_url_iv);
                this.commentCountTextView = itemView.findViewById(R.id.reddit_card_comment_count_tv);
                this.upvoteButton = itemView.findViewById(R.id.reddit_card_upvote_iv);
                this.downvoteButton = itemView.findViewById(R.id.reddit_card_downvote_iv);
                this.playerView = itemView.findViewById(R.id.reddit_card_video_player);


                authorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment fragment = RedditUserProfileFragment.newInstance(mData.get(getAdapterPosition()).mAuthor);
                        mActivity.getSupportFragmentManager().beginTransaction()
                                .replace(mActivity.findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });

                subredditTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String subredditName = mData.get(getAdapterPosition()).mSubreddit;

                        Fragment fragment = RedditMainPageFragment.newInstance(subredditName);
                        mActivity.getSupportFragmentManager().beginTransaction()
                                .replace(mActivity.findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });

                final String voteUrl = "https://oauth.reddit.com/api/vote";
                upvoteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("comment pre-click", mData.get(getAdapterPosition()).mScore);
                        //Colo orange = mActivity.getResources().getColor(R.color.Basket_Ball_Orange);
                        RequestBody requestBody;
                        if (mData.get(getAdapterPosition()).mLikes.equals("true")) {
                            requestBody = new FormBody.Builder()
                                    .add("dir","0")
                                    .add("id", mData.get(getAdapterPosition()).mFullname)
                                    .build();

                            mData.get(getAdapterPosition()).mScore = String.valueOf(Integer.parseInt(
                                    mData.get(getAdapterPosition()).mScore) - 1);
                            mData.get(getAdapterPosition()).mLikes = "null";
                            upvoteButton.setColorFilter(null);
                            downvoteButton.setColorFilter(null);
                            scoreTextView.setTextColor(mActivity.getResources().getColor(android.R.color.black));
                            String newScore = String.valueOf(Integer.parseInt(mData.get(getAdapterPosition()).mScore));
                            scoreTextView.setText(newScore);
                        }
                        else {

                            mData.get(getAdapterPosition()).mScore = String.valueOf(Integer.parseInt(
                                    mData.get(getAdapterPosition()).mScore) + 1);
                            mData.get(getAdapterPosition()).mLikes = "true";

                            String newScore = mData.get(getAdapterPosition()).mScore;
                            scoreTextView.setText(newScore);

                            requestBody = new FormBody.Builder()
                                    .add("dir","1")
                                    .add("id", mData.get(getAdapterPosition()).mFullname)
                                    .build();
                            upvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));
                            downvoteButton.setColorFilter(null);
                            scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));

                        }
                        NetworkingUtils.post(voteUrl, requestBody, new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String result = response.body().string();
                                Log.d("onresponse", result);
                                //If empty and not Error 403, it is a success
                            }
                        });
                    }
                });

                downvoteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RequestBody requestBody;
                        if (mData.get(getAdapterPosition()).mLikes.equals("false")) {
                            requestBody = new FormBody.Builder()
                                    .add("dir","0")
                                    .add("id", mData.get(getAdapterPosition()).mFullname)
                                    .build();

                            upvoteButton.setColorFilter(null);
                            downvoteButton.setColorFilter(null);
                            scoreTextView.setTextColor(mActivity.getResources().getColor(android.R.color.black));

                            mData.get(getAdapterPosition()).mScore = String.valueOf(Integer.parseInt(
                                    mData.get(getAdapterPosition()).mScore) + 1);
                            mData.get(getAdapterPosition()).mLikes = "null";
                            String newScore = mData.get(getAdapterPosition()).mScore;
                            scoreTextView.setText(newScore);
                        } else {
                            mData.get(getAdapterPosition()).mScore = String.valueOf(Integer.parseInt(
                                    mData.get(getAdapterPosition()).mScore) - 1);
                            mData.get(getAdapterPosition()).mLikes = "false";

                            String newScore = mData.get(getAdapterPosition()).mScore;
                            scoreTextView.setText(newScore);

                            requestBody = new FormBody.Builder()
                                    .add("dir","-1")
                                    .add("id", mData.get(getAdapterPosition()).mFullname)
                                    .build();
                            downvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Blue_Gray));
                            upvoteButton.setColorFilter(null);
                            scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Blue_Gray));
                        }

                        NetworkingUtils.post(voteUrl, requestBody, new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String result = response.body().string();
                                //If empty and not Error 403, it is a success
                                Log.d("downvote click", result);
                            }
                        });
                    }
                });

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                Fragment fragment = RedditCardDetailFragment.newInstance(
                        mData.get(getAdapterPosition()));

                //R.id.reddit_activity_fragment_container).getId()
                mActivity.getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(mActivity.findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        @NonNull
        @Override
        public RedditCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh;
            if (!mIsCompact) {
                vh = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_reddit_card, parent, false);
            }
            else {
                vh = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_reddit_card_compact, parent,false);
            }
            return new RedditCardAdapter.ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(final RedditCardAdapter.ViewHolder holder, int position) {
            holder.titleTextView.setText(mData.get(position).mTitle);
            holder.subredditTextView.setText("r/" + mData.get(position).mSubreddit);
            holder.scoreTextView.setText(mData.get(position).mScore);
            holder.timePostedTextView.setText(mData.get(position).mTimeSinceCreated);
            holder.authorTextView.setText(" by " + mData.get(position).mAuthor);
            holder.commentCountTextView.setText(mData.get(position).mCommentCount);

            if (!mIsFrontPage && !mSubredditName.equals("popular")) {
                holder.subredditTextView.setVisibility(View.GONE);
            }

            if (mIsCompact) {
                //holder.authorTextView.setVisibility(View.GONE);
                String thumbnailUrl = mData.get(position).mThumbnailUrl;
                if (thumbnailUrl.equals("spoiler")) {
                    holder.thumbnailImageView.setImageResource(R.drawable.spoiler_tag);
                } else if (thumbnailUrl.equals("self") || thumbnailUrl.equals("")) {
                    holder.thumbnailImageView.setImageResource(R.drawable.text_icon);
                } else Picasso.get().load(mData.get(position).mThumbnailUrl).placeholder(R.drawable.progress_animation)
                        .into(holder.thumbnailImageView);
            }
            /*
            else {
                Picasso.get().load(mData.get(position).mPreviewUrl).fit().into(holder.previewImageView);
            }
            */
            if (!mIsCompact) {
                holder.thumbnailImageView.setVisibility(View.GONE);
                if (mData.get(position).mPreviewUrl == null || mData.get(position).mPreviewUrl.equals("")) {
                    //if (holder.previewImageView != null)
                        holder.previewImageView.setVisibility(View.GONE);
                }
                else if (mData.get(position).mIsVideoOrGif) {
                        holder.previewImageView.setVisibility(View.GONE);
                        holder.playerView.setVisibility(View.VISIBLE);
                        holder.playerView.setPlayer(mExoPlayer);

// This is the MediaSource representing the media to be played.
                        MediaSource videoSource = new ExtractorMediaSource.Factory(mDataSourceFactory)
                                .createMediaSource(Uri.parse(mData.get(position).mPreviewUrl));
// Prepare the player wi// th the source.
                        mExoPlayer.prepare(videoSource);
                }
                else {
                    holder.previewImageView.setVisibility(View.VISIBLE);
                    holder.playerView.setVisibility(View.GONE);
                    Picasso.get().load(mData.get(position).mPreviewUrl).fit().placeholder(R.drawable.progress_animation)
                            .into(holder.previewImageView);
                    //Glide.with(mActivity).load(mData.get(position).mPreviewUrl).fitCenter().into(holder.previewImageView);
                }
                //holder.thumbnailImageView.setImageResource(R.drawable.text_icon);
                //handle if upvote/downvote buttons are toggled
                //Log.d("likeornot", mData.get(position).mLikes);
                switch (mData.get(position).mLikes) {
                    case "true":
                        //mData.get(position).mScore = String.valueOf(Integer.parseInt(mData.get(position).mScore) - 1);
                        holder.upvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));
                        holder.scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));
                        break;
                    case "false":
                        //mData.get(position).mScore = String.valueOf(Integer.parseInt(mData.get(position).mScore) + 1);
                        holder.downvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Blue_Gray));
                        holder.scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Blue_Gray));
                        break;
                }
            }
        }
        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}