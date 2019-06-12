package com.example.android.Tubeddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.Tubeddit.data.YoutubeChannel;
import com.example.android.Tubeddit.data.YoutubeVideo;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.squareup.picasso.Picasso;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.example.android.Tubeddit.MainActivity.ACTION_BAR_TITLE_MAIN_PAGE;
import static com.example.android.Tubeddit.MainActivity.SHARED_PREFERENCES_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class YoutubeMainPageFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayoutManager;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;

    String mJsonResponseData;
    Parcelable mLayoutManagerState;

    private static final String SHARED_PREFERENCES_YOUTUBE_AUTH_STATE = "SHARED_PREFERENCES_YOUTUBE_AUTH_STATE";
    private static final int REQUEST_CODE_SIGN_IN = 50;

    public static final String FRAGMENT_TAG_YOUTUBE_MAIN_PAGE = "FRAGMENT_TAG_YOUTUBE_MAIN_PAGE";
    static final String STATE_LAYOUT_MANAGER_STATE = "STATE_LAYOUT_MANAGER_STATE";
    static final String STATE_JSON_RESPONSE_DATA = "STATE_JSON_RESPONSE_DATA";

    private String youtubeMostPopularVidsUrl = "https://www.googleapis.com/youtube/v3/videos" +
            "?part=snippet,statistics&maxResults=25&chart=mostPopular&key=AIzaSyD0c6zzMUaBltdZCQ-GJYjdZKodBL74BxQ";

    public YoutubeMainPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(STATE_LAYOUT_MANAGER_STATE, mLinearLayoutManager.onSaveInstanceState());
        if (mJsonResponseData != null) {
            outState.putString(STATE_JSON_RESPONSE_DATA, mJsonResponseData);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.findItem(R.id.menu_action_toggle_reddit_or_youtube);
        menuItem.setIcon(R.drawable.youtube_icon);
        menu.findItem(R.id.menu_action_search).setEnabled(false);
        menu.findItem(R.id.menu_action_refresh).setEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_youtube_main_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(ACTION_BAR_TITLE_MAIN_PAGE);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);

        mRecyclerView = view.findViewById(R.id.youtube_videos_feed_rv);

        //mRecyclerView.setItemViewCacheSize(25);
        //mRecyclerView.setDrawingCacheEnabled(true);
        //mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.onRestoreInstanceState(mLayoutManagerState);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mDrawerLayout = getActivity().findViewById(R.id.drawer_layout);
        mNavigationView = getActivity().findViewById(R.id.nav_view);
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.menu_youtube_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);



        if (savedInstanceState == null || mJsonResponseData == null) {
            bindVideoFeedsUi(youtubeMostPopularVidsUrl);
        }
        else bindRecyclerView(mJsonResponseData);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.youtube_log_in:
                newYoutubeLoginAndAuth();
                return true;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                final AuthorizationService service = new AuthorizationService(getContext());
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

    @NonNull public AuthState readAuthState() {
        SharedPreferences authPrefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
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
        SharedPreferences authPrefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        authPrefs.edit()
                .putString(SHARED_PREFERENCES_YOUTUBE_AUTH_STATE, state.jsonSerializeString())
                .commit();
    }


    private void bindVideoFeedsUi(final String url) {
        Log.d("logcheck", url);

        NetworkingUtils.newGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                mJsonResponseData = result;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bindRecyclerView(result);
                    }
                });
            }
        });
    }

    private void bindRecyclerView(String data) {
        ArrayList<YoutubeVideo> resultList = null;
        try {
            resultList = JsonUtils.parseYoutubeVideos(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mRecyclerView.removeAllViewsInLayout();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                new LinearLayoutManager(getContext()).getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        YoutubeMainPageFragment.Adapter adapter = new YoutubeMainPageFragment.Adapter(resultList);
        mRecyclerView.setAdapter(adapter);
    }

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

        AuthorizationService authService = new AuthorizationService(getContext());
        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, REQUEST_CODE_SIGN_IN);
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        ArrayList<YoutubeVideo> mData;

        public Adapter(ArrayList<YoutubeVideo> data) {
            mData = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView titleTextView;
            TextView channelTitleTextView;
            TextView publishedDateTextView;
            TextView viewCountTextView;
            ImageView thumbnailImageView;
            ImageView channelIconImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.youtube_card_title_tv);
                channelTitleTextView = itemView.findViewById(R.id.youtube_card_channel_title_tv);
                publishedDateTextView = itemView.findViewById(R.id.youtube_card_time_published_tv);
                viewCountTextView  = itemView.findViewById(R.id.youtube_card_view_count_tv);
                thumbnailImageView = itemView.findViewById(R.id.youtube_card_thumbnail_iv);
                channelIconImageView = itemView.findViewById(R.id.youtube_card_channel_icon_image_iv);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), YoutubeVideoDetailActivity.class);
                Parcelable savedYoutubeVideoData = Parcels.wrap(mData.get(getAdapterPosition()));
                intent.putExtra(YoutubeVideoDetailActivity.EXTRA_YOUTUBE_VIDEO_DATA, savedYoutubeVideoData);
                startActivity(intent);
            }
        }

        @NonNull
        @Override
        public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_youtube_video, parent, false);
            return new ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull final Adapter.ViewHolder holder, int position) {
            holder.titleTextView.setText(mData.get(position).mTitle);
            holder.channelTitleTextView.setText(mData.get(position).mChannelTitle);
            holder.publishedDateTextView.setText(mData.get(position).mPublishedDate);
            holder.viewCountTextView.setText(mData.get(position).mViewCount + " views");
            //Log.d("chec pls", mData.get(position).mThumbnailUrl);

            Picasso.get().load(mData.get(position).mThumbnailUrl).fit().into(holder.thumbnailImageView);
            //Picasso.get().load(mData.get(position).)
            final String channelUrl = "https://www.googleapis.com/youtube/v3/channels?part=snippet&id="
                    + mData.get(position).mChannelId + "&key=AIzaSyD0c6zzMUaBltdZCQ-GJYjdZKodBL74BxQ";
            NetworkingUtils.newGet(channelUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    try {
                        final YoutubeChannel youtubeChannel = JsonUtils.parseYoutubeChannel(result);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Picasso.get().load(youtubeChannel.mChannelIconUrl).fit().into(holder.channelIconImageView, new com.squareup.picasso.Callback() {
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

}
