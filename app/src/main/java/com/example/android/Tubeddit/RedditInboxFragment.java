package com.example.android.Tubeddit;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.Tubeddit.data.RedditComment;
import com.example.android.Tubeddit.data.RedditSentMail;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class RedditInboxFragment extends Fragment {

    private static final CharSequence TAB_TITLE_SENT = "Sent";
    private static final CharSequence TAB_TITLE_INBOX = "Inbox";
    private String mToolbarTitle = "Inbox";
    private Toolbar mToolbar;
    ViewPager mViewPager;
    InboxAdapter mAdapter;
    TabLayout mTabLayout;

    public RedditInboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mToolbarTitle);
        ColorDrawable color = new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(color);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_reddit_inbox, container, false);
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
        mViewPager = view.findViewById(R.id.reddit_inbox_pager);
        mAdapter = new InboxAdapter(getChildFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public class InboxAdapter extends FragmentPagerAdapter {

        public InboxAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //Fragment fragment = RedditSearchFragment.SearchResultsFragment.newInstance(position);
            InboxResultsFragment fragment = InboxResultsFragment.newInstance(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return TAB_TITLE_INBOX;
                case 1:
                    return TAB_TITLE_SENT;
            }
            return super.getPageTitle(position);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        mToolbar.setTitle(mToolbarTitle);
    }

    @Override
    public void onStop() {
        super.onStop();
        //((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    public static class InboxResultsFragment extends Fragment {
        private static final String ARGS_TAB_POSITON = "ARGS_TAB_POSITON";

        ///Tab positions: 0 = Subreddit, 1 = Posts, 2 = Users

        int mTabPosition;
        RecyclerView mRecyclerView;

        public static InboxResultsFragment newInstance(int currentTab) {
            InboxResultsFragment fragment = new InboxResultsFragment();
            Bundle args = new Bundle(1);
            args.putInt(ARGS_TAB_POSITON, currentTab);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null && getArguments().containsKey(ARGS_TAB_POSITON)) {
                mTabPosition = getArguments().getInt(ARGS_TAB_POSITON);
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.reusable_recycler_view_layout, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mRecyclerView = view.findViewById(R.id.reusable_rv);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                    new LinearLayoutManager(getActivity()).getOrientation());
            mRecyclerView.addItemDecoration(dividerItemDecoration);

            switch (mTabPosition) {
                case 0:
                    String inboxUrl = "https://oauth.reddit.com/message/inbox?limit=2";
                    bindInboxUi(inboxUrl);
                    break;
                case 1:
                    String sentUrl = "https://oauth.reddit.com/message/sent/";
                    bindInboxUi(sentUrl);
                    break;
            }
        }

        private void bindInboxUi(final String url) {
            Log.d("Network Call", url);

            NetworkingUtils.authGet(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    ArrayList<RedditComment> redditComments = null;
                    ArrayList<RedditSentMail> redditSentMails = null;
                    try {
                        switch (mTabPosition) {
                            case 0:
                                redditComments = JsonUtils.parseRedditComments(result);
                            case 1:
                                redditSentMails = JsonUtils.parseRedditSentMail(result);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    final ArrayList<RedditComment> finalRedditComments = redditComments;
                    final ArrayList<RedditSentMail> finalRedditSentMails = redditSentMails;
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            mRecyclerView.setLayoutManager(linearLayoutManager);
                            switch (mTabPosition) {
                                case 0:
                                    mRecyclerView.setAdapter(new RedditCardDetailFragment.CommentsAdapter(finalRedditComments, (AppCompatActivity) getActivity()));
                                    break;
                                case 1:
                                    mRecyclerView.setAdapter(new InboxSentAdapter(finalRedditSentMails,
                                            (AppCompatActivity) getActivity()));
                                    break;
                            }
                        }
                    });
                    Log.d(url, result);

                }
            });
        }

        private static class InboxSentAdapter extends RecyclerView.Adapter<InboxSentAdapter.ViewHolder> {
            ArrayList<RedditSentMail> mData;
            AppCompatActivity mActivity;

            public InboxSentAdapter(ArrayList<RedditSentMail> data, AppCompatActivity activity) {
                mData = data;
                mActivity = activity;
            }

            public class ViewHolder extends RecyclerView.ViewHolder {
                TextView subjectTextView;
                TextView destinationTextView;
                TextView timeSincePostedTextView;
                TextView bodyTextView;

                public ViewHolder(View itemView) {
                    super(itemView);
                    subjectTextView = itemView.findViewById(R.id.reddit_inbox_sent_subject_tv);
                    destinationTextView = itemView.findViewById(R.id.reddit_inbox_sent_destination_tv);
                    timeSincePostedTextView = itemView.findViewById(R.id.reddit_inbox_sent_time_since_post_tv);
                    bodyTextView = itemView.findViewById(R.id.reddit_inbox_sent_body_tv);
                }
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View vh = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_reddit_inbox_sent, parent, false);
                return new ViewHolder(vh);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.subjectTextView.setText(mData.get(position).mSubject);
                holder.bodyTextView.setText(mData.get(position).mBody);
                holder.destinationTextView.setText(mData.get(position).mDestination);
                holder.timeSincePostedTextView.setText(mData.get(position).mTimeSincePost);
            }

            @Override
            public int getItemCount() {
                return mData.size();
            }

        }
    }
}