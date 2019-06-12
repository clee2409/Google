package com.example.android.Tubeddit;

import android.app.SearchManager;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.SearchResultSubreddit;
import com.example.android.Tubeddit.data.RedditUserProfile;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */

public class RedditSearchFragment extends Fragment {
    static String mQuery = "";
    //Tabs are: Subreddits, Posts, Users
    static int mCurrentTabPosition;

    ViewPager mViewPager;
    SearchResultsAdapter mAdapter;
    Toolbar mToolbar;

    static final String TAG_REDDIT_SEARCH_FRAGMENT = "TAG_REDDIT_SEARCH_FRAGMENT";
    static final String ARGUMENTS_SEARCH_QUERY = "ARGUMENTS_SEARCH_QUERY";
    static final String ARGS_TAB_TITLE = "ARGS_TAB_TITLE";
    static final String TAB_TITLE_SUBREDDITS = "Subreddits";
    static final String TAB_TITLE_POSTS = "Posts";
    static final String TAB_TITLE_USERS = "Users";
    static final String STATE_QUERY = "STATE_QUERY";

    public RedditSearchFragment() {
        // Required empty public constructor
    }

    public static RedditSearchFragment newInstance(String searchQuery) {
        Bundle args = new Bundle(1);
        args.putString(ARGUMENTS_SEARCH_QUERY, searchQuery);
        RedditSearchFragment fragment = new RedditSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null && getArguments().containsKey(ARGUMENTS_SEARCH_QUERY)) {
            mQuery = getArguments().getString(ARGUMENTS_SEARCH_QUERY, "");
        }
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(STATE_QUERY);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.menu_search_activity);
        setToolbarMenu();
        mViewPager = view.findViewById(R.id.reddit_search_pager);
        mAdapter = new SearchResultsAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        }

    private void setToolbarMenu() {
        Menu menu = mToolbar.getMenu();

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.menu_action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(mQuery, false);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("listen", query);
                mQuery = query;
                mViewPager.setAdapter(mAdapter);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    public class SearchResultsAdapter extends FragmentPagerAdapter {

        public SearchResultsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = SearchResultsFragment.newInstance(position);

            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return TAB_TITLE_SUBREDDITS;
                case 1:
                    return TAB_TITLE_POSTS;
                case 2:
                    return TAB_TITLE_USERS;
            }
            return super.getPageTitle(position);
        }
    }

    public static class SearchResultsFragment extends Fragment {

        ///Tab positions: 0 = Subreddit, 1 = Posts, 2 = Users

        int mTabPosition;
        RecyclerView mRecyclerView;

        public static SearchResultsFragment newInstance(int currentTab) {
            SearchResultsFragment fragment = new SearchResultsFragment();
            Bundle args = new Bundle(1);
            args.putInt(ARGS_TAB_TITLE, currentTab);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null && getArguments().containsKey(ARGS_TAB_TITLE)) {
                mTabPosition = getArguments().getInt(ARGS_TAB_TITLE);
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.reusable_recycler_view_layout, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mRecyclerView =  view.findViewById(R.id.reusable_rv);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                    new LinearLayoutManager(getActivity()).getOrientation());
            mRecyclerView.addItemDecoration(dividerItemDecoration);

            switch (mTabPosition) {
                case 0:
                    bindSubredditSearchesUi(mQuery);
                    break;
                case 1:
                    bindPostsSearchesUi(mQuery);
                    break;
                case 2:
                    bindUsersSearchesUi(mQuery);
                    break;
            }
        }
        private void bindRecyclerView(String data) {
            mRecyclerView.removeAllViewsInLayout();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(linearLayoutManager);
            switch (mTabPosition) {
                case 0:
                    ArrayList<SearchResultSubreddit> subredditsList = null;
                    try {
                        subredditsList = JsonUtils.parseSubredditSearchResult(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SubredditSearchAdapter subredditSearchAdapter = new SubredditSearchAdapter(subredditsList, (AppCompatActivity) getActivity());
                    mRecyclerView.setAdapter(subredditSearchAdapter);
                    break;
                case 1:
                    ArrayList<RedditCard> redditCardsList = null;
                    try {
                        redditCardsList = JsonUtils.parseRedditCards(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RedditMainPageFragment.RedditCardAdapter redditCardAdapter = new RedditMainPageFragment.RedditCardAdapter(redditCardsList, ((AppCompatActivity) getActivity()), true);
                    mRecyclerView.setAdapter(redditCardAdapter);
                    break;
                case 2:
                    ArrayList<RedditUserProfile> resultList = null;
                    try {
                        resultList = JsonUtils.parseRedditUserProfiles(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    UserSearchAdapter userSearchAdapter = new UserSearchAdapter(resultList, (AppCompatActivity) getActivity());
                    mRecyclerView.setAdapter(userSearchAdapter);
                    break;
            }

        }

        private void bindSubredditSearchesUi(String query) {
            String endpointUrl = "https://www.reddit.com/subreddits/search.json?q=" + query;

            NetworkingUtils.newGet(endpointUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String result = response.body().string();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bindRecyclerView(result);
                        }
                    });
                }
            });
        }

        private void bindPostsSearchesUi(String query) {
            String endpointUrl = "https://www.reddit.com/search.json?q=" + query;

            NetworkingUtils.newGet(endpointUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String result = response.body().string();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bindRecyclerView(result);
                        }
                    });
                }
            });
        }

        private void bindUsersSearchesUi(String query) {
            String endpointUrl = "https://www.reddit.com/users/search.json?q=" + query;

            NetworkingUtils.newGet(endpointUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String result = response.body().string();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bindRecyclerView(result);
                        }
                    });
                }
            });
        }

    }

    private static class SubredditSearchAdapter extends RecyclerView.Adapter<SubredditSearchAdapter.ViewHolder> {
        ArrayList<SearchResultSubreddit> mData;
        AppCompatActivity mActivity;

        public SubredditSearchAdapter(ArrayList<SearchResultSubreddit> data, AppCompatActivity activity) {
            mActivity = activity;
            mData = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView subredditNameTextView;
            TextView subscriberCountTextView;
            TextView timeSinceCreationTextView;
            TextView publicDescriptionTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                subredditNameTextView = itemView.findViewById(R.id.subreddit_search_subreddit_name_tv);
                subscriberCountTextView = itemView.findViewById(R.id.subreddit_search_subscriber_count_tv);
                timeSinceCreationTextView = itemView.findViewById(R.id.subreddit_search_time_since_creation_tv);
                publicDescriptionTextView = itemView.findViewById(R.id.subreddit_search_public_description_tv);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                Fragment fragment =  RedditMainPageFragment.newInstance(
                        mData.get(getAdapterPosition()).mSubredditName);
               mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(mActivity.findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        @NonNull
        @Override
        public SubredditSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_subreddit_search, parent, false);
            return new SubredditSearchAdapter.ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull SubredditSearchAdapter.ViewHolder holder, int position) {
            holder.subredditNameTextView.setText("r/" + mData.get(position).mSubredditName);
            holder.subscriberCountTextView.setText(mData.get(position).mSubscriberCount + " subscribers");
            holder.publicDescriptionTextView.setText(mData.get(position).mPublicDescription);
            holder.timeSinceCreationTextView.setText("Created " + mData.get(position).mTimeSinceCreation);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    /*
    private static class RedditCardAdapter extends RecyclerView.ProfileFeedAdapter<RedditCardAdapter.ViewHolder> {
        ArrayList<RedditCard> mData;
        AppCompatActivity mActivity;

        public RedditCardAdapter(ArrayList<RedditCard> data, AppCompatActivity activity) {
            mData = data;
            mActivity = activity;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView titleTextView;
            TextView subredditTextView;
            ImageView thumbnailImageView;
            TextView commentScoreTextView;
            TextView destinationTextView;

            ViewHolder(View itemView) {
                super(itemView);
                this.titleTextView = itemView.findViewById(R.id.reddit_title_tv);
                this.subredditTextView = itemView.findViewById(R.id.reddit_subreddit_tv);
                this.thumbnailImageView = itemView.findViewById(R.id.reddit_thumbnailUrl_iv);
                this.commentScoreTextView = itemView.findViewById(R.id.reddit_score_tv);
                this.destinationTextView = itemView.findViewById(R.id.reddit_author_tv);

                subredditTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String subredditName = mData.get(getAdapterPosition()).mSubreddit;

                        Fragment fragment = RedditMainPageFragment.newInstance(subredditName);
                        mActivity.getSupportFragmentManager().beginTransaction()
                                .replace(mActivity.findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                                .addToBackStack(null)
                                .commit();
                        Toast.makeText(mActivity, "clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                Fragment fragment = RedditCardDetailFragment.newInstance(
                        mData.get(getAdapterPosition()));
                Toast.makeText(mActivity, "clicked", Toast.LENGTH_SHORT).show();

                //mActivity.findViewById(R.id.reddit_activity_fragment_container).getId()
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
            View vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reddit_card, parent, false);

            return new RedditCardAdapter.ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(RedditCardAdapter.ViewHolder holder, int position) {
            holder.titleTextView.setText(mData.get(position).mTitle);
            holder.subredditTextView.setText(mData.get(position).mSubreddit);
            holder.commentScoreTextView.setText(mData.get(position).mScore);
            holder.destinationTextView.setText(mData.get(position).mDestination);

            Picasso.get().load(mData.get(position).mThumbnailUrl).into(holder.thumbnailImageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
    */

    private static class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

        ArrayList<RedditUserProfile> mData;
        AppCompatActivity mActivity;

        public UserSearchAdapter(ArrayList<RedditUserProfile> data, AppCompatActivity activity) {
            mData = data;
            mActivity = activity;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView nameTextView;
            TextView publicDescriptionTextView;
            TextView timeSinceCreationTextView;
            TextView karmaCountTextView;
            ImageView iconImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.user_search_result_name_tv);
                publicDescriptionTextView = itemView.findViewById(R.id.user_search_result_public_description_tv);
                timeSinceCreationTextView = itemView.findViewById(R.id.user_search_result_time_since_creation_tv);
                karmaCountTextView = itemView.findViewById(R.id.user_search_result_karma_count_tv);
                iconImageView = itemView.findViewById(R.id.user_search_result_icon_iv);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                Fragment fragment = RedditUserProfileFragment.newInstance(mData.get(getAdapterPosition()).mUserName);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(mActivity.findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();

            }
        }

        @Override
        public UserSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_user_search, parent, false);

            return new UserSearchAdapter.ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull UserSearchAdapter.ViewHolder holder, int position) {
            holder.nameTextView.setText(mData.get(position).mUserName);
            holder.karmaCountTextView.setText(mData.get(position).mKarmaCount + " Karma");
            holder.publicDescriptionTextView.setText(mData.get(position).mPublicDescription);
            holder.timeSinceCreationTextView.setText(mData.get(position).mTimeSinceCreation);

            //Picasso.get().load(mData.get(position).mThumbnailUrl).into(holder.thumbnailImageView, new com.squareup.picasso.Callback() {

            Picasso.get().load(mData.get(position).mIconUrl).into(holder.iconImageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

}
