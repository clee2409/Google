package com.example.android.Tubeddit;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.RedditCardDetail;
import com.example.android.Tubeddit.data.RedditComment;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RedditCardDetailFragment extends Fragment {

    private static final String STATE_REDDIT_CARD_DETAIL_DATA = "STATE_REDDIT_CARD_DETAIL_DATA";
    private static final String STATE_LAYOUT_MANAGER = "STATE_LAYOUT_MANAGER";
    public static String EXTRA_REDDIT_CARD = "EXTRA_REDDIT_CARD";
    private static final String ARGUMENTS_USER = "ARGUMENTS_USER";

    private Parcelable mLayoutManagerState;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private RedditCardDetail mRedditCardDetailData;
    private RedditCard mRedditCard;
    private Toolbar mToolbar;
    private EditText mAddCommentEditText;
    private String mReplyOrComment;

    boolean upvoteToggle;
    boolean downvoteToggle;
    boolean nullToggle;
    boolean cardUpvoteToggle;

    public RedditCardDetailFragment() {
        // Required empty public constructor
    }

    public static RedditCardDetailFragment newInstance(RedditCard data) {
        RedditCardDetailFragment fragment = new RedditCardDetailFragment();
        Bundle args = new Bundle(1);
        Parcelable redditCardData = Parcels.wrap(data);
        args.putParcelable(EXTRA_REDDIT_CARD, redditCardData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parcelable redditCardParcel = getArguments().getParcelable(EXTRA_REDDIT_CARD);
        mRedditCard = Parcels.unwrap(redditCardParcel);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_REDDIT_CARD_DETAIL_DATA)) {
                Parcelable parcelable = savedInstanceState.getParcelable(STATE_REDDIT_CARD_DETAIL_DATA);
                mRedditCardDetailData = Parcels.unwrap(parcelable);
            }
            mLayoutManagerState = savedInstanceState.getParcelable(STATE_LAYOUT_MANAGER);
        }

        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_reddit_card_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.reddit_comments_rv);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.onRestoreInstanceState(mLayoutManagerState);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (savedInstanceState == null) {
            bindUi(view, false);
        } else bindUi(view, true);

        //createCommentDialog();
        /*
        String jsonUrl = "https://www.reddit.com" + mRedditCard.mPermalink + ".json";
        NetworkingUtils.newGet(jsonUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("RedditCardDetail JSON", result);
                try {
                    mRedditCard = JsonUtils.parseRedditCards(result).get(0);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bindUi(view);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        */


        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mAddCommentEditText = view.findViewById(R.id.reddit_add_comment_etv);
        mAddCommentEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //createCommentDialog();
                ReplyOrCommentDialogFragment dialog = ReplyOrCommentDialogFragment.newInstance(null, mRedditCard);
                dialog.show(getActivity().getSupportFragmentManager(), "commenttag");
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void bindUi(View view, boolean savedInstanceState) {
        boolean isLoggedIn = false;

        if (getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .contains(RedditMainPageFragment.SHARED_PREFERENCES_REDDIT_ACCESS_TOKEN)) {
            isLoggedIn = true;
        }
        TextView titleTextView = view.findViewById(R.id.redditcard_detail_title_tv);
        TextView subredditTextView = view.findViewById(R.id.redditcard_detail_subreddit_tv);
        TextView authorTextView = view.findViewById(R.id.redditcard_detail_author_tv);
        TextView bodyTextView = view.findViewById(R.id.reddit_card_detail_text_body_text_view);
        PlayerView playerView = view.findViewById(R.id.reddit_card_detail_player_view);

        final TextView upvoteRatioTextView = view.findViewById(R.id.redditcard_detail_upvote_ratio_tv);
        ImageView pictureImageView = view.findViewById(R.id.reddit_card_detail_picture_iv);
        TextView commentCountTextView = view.findViewById(R.id.reddit_card_comment_count_tv);
        final TextView scoreCountTextView = view.findViewById(R.id.reddit_score_tv);
        TextView timeSincePostedTextView = view.findViewById(R.id.redditcard_detail_time_since_posted_tv);

        final ImageView upvoteImageView = view.findViewById(R.id.reddit_card_upvote_iv);
        final ImageView downvoteImageView = view.findViewById(R.id.reddit_card_downvote_iv);

        titleTextView.setText(mRedditCard.mTitle);
        subredditTextView.setText("by " + mRedditCard.mSubreddit);
        authorTextView.setText("Posted by " + mRedditCard.mAuthor);
        commentCountTextView.setText(mRedditCard.mCommentCount);
        timeSincePostedTextView.setText(mRedditCard.mTimeSinceCreated);
        scoreCountTextView.setText(mRedditCard.mScore);
        if (mRedditCard.mSelftext == null || !mRedditCard.mSelftext.equals("")) {
            bodyTextView.setText(mRedditCard.mSelftext);
            bodyTextView.setVisibility(View.VISIBLE);
        }
        if (mRedditCard.mIsVideoOrGif) {
            pictureImageView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getActivity());
            playerView.setPlayer(player);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
                    Util.getUserAgent(getActivity(), "yourApplicationName"));
// This is the MediaSource representing the media to be played.
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mRedditCard.mPreviewUrl));
// Prepare the player with the source.
            player.prepare(videoSource);

        }

        subredditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subredditName = mRedditCard.mSubreddit;

                Fragment fragment = RedditMainPageFragment.newInstance(subredditName);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(getActivity().findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();

            }
        });

        authorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = RedditUserProfileFragment.newInstance(mRedditCard.mAuthor);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(getActivity().findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        switch (mRedditCard.mLikes) {
            case "true":
                //mRedditCard.mScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) - 1);
                upvoteToggle = true;
                downvoteToggle = false;
                nullToggle = false;
                upvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));
                scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));
                break;
            case "false":
                //mRedditCard.mScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) + 1);
                downvoteToggle = true;
                nullToggle = false;
                upvoteToggle = false;
                upvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Blue_Gray));
                scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Blue_Gray));
                break;
            case "null":
                nullToggle = true;
                upvoteToggle = false;
                downvoteToggle = false;
                break;
        }

        if (mRedditCard.mPreviewUrl != null) {
            Picasso.get().load(mRedditCard.mPreviewUrl).fit().into(pictureImageView);
        }
        else pictureImageView.setVisibility(View.GONE);

        String jsonUrl;
        if (isLoggedIn) {
            jsonUrl = "https://oauth.reddit.com" + mRedditCard.mPermalink + ".json";
        } else {
            jsonUrl = "https://www.reddit.com" + mRedditCard.mPermalink + ".json";
        }
        Log.d("urlchecknow", jsonUrl);

        final String voteUrl = "https://oauth.reddit.com/api/vote";

        upvoteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("comment pre-click", mRedditCard.mScore);
                    //Colo orange = mActivity.getResources().getColor(R.color.Basket_Ball_Orange);
                    RequestBody requestBody;
                    if (upvoteToggle) {
                        nullToggle = true;
                        downvoteToggle = false;
                        upvoteToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","0")
                                .add("id", mRedditCard.mFullname)
                                .build();

                        upvoteImageView.setColorFilter(null);
                        downvoteImageView.setColorFilter(null);
                        scoreCountTextView.setTextColor(getActivity().getResources().getColor(android.R.color.black));
                        String newScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) - 1);
                        scoreCountTextView.setText(newScore);
                    }
                    else {
                        String newScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) + 1);
                        scoreCountTextView.setText(newScore);

                        upvoteToggle = true;
                        downvoteToggle = false;
                        nullToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","1")
                                .add("id", mRedditCard.mFullname)
                                .build();
                        upvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));
                        downvoteImageView.setColorFilter(null);
                        scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));

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

            downvoteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RequestBody requestBody;
                    if (downvoteToggle) {
                        nullToggle = true;
                        upvoteToggle = false;
                        downvoteToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","0")
                                .add("id", mRedditCard.mFullname)
                                .build();

                        upvoteImageView.setColorFilter(null);
                        downvoteImageView.setColorFilter(null);
                        scoreCountTextView.setTextColor(getActivity().getResources().getColor(android.R.color.black));
                        String newScore = String.valueOf(Integer.parseInt(mRedditCard.mScore));
                        scoreCountTextView.setText(newScore);
                    } else {
                        String newScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) - 1);
                        scoreCountTextView.setText(newScore);

                        upvoteToggle = false;
                        downvoteToggle = true;
                        nullToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","-1")
                                .add("id", mRedditCard.mFullname)
                                .build();
                        downvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Blue_Gray));
                        upvoteImageView.setColorFilter(null);
                        scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Blue_Gray));
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

            if (savedInstanceState) {
                Log.d("statesaved", "it is true");
                double decimal= Double.parseDouble(mRedditCardDetailData.mUpvoteRatio);
                double decimal2 = decimal*100;
                String upvoteRatioAsPercentage = String.format("%.0f", decimal2);
                upvoteRatioTextView.setText(upvoteRatioAsPercentage + "% Upvoted");

                switch (mRedditCard.mLikes) {
                    case "true":
                        mRedditCard.mScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) - 1);
                        upvoteToggle = true;
                        downvoteToggle = false;
                        nullToggle = false;
                        upvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));
                        scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));
                        break;
                    case "false":
                        mRedditCard.mScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) + 1);
                        downvoteToggle = true;
                        nullToggle = false;
                        upvoteToggle = false;
                        upvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Blue_Gray));
                        scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Blue_Gray));
                        break;
                    case "null":
                        nullToggle = true;
                        upvoteToggle = false;
                        downvoteToggle = false;
                        break;
                }

                //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                //mRecyclerView.setLayoutManager(linearLayoutManager);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                        new LinearLayoutManager(getContext()).getOrientation());
                mRecyclerView.addItemDecoration(dividerItemDecoration);
                CommentsAdapter commentsAdapter = new CommentsAdapter(mRedditCardDetailData.mCommentsList, (AppCompatActivity) getActivity());
                mRecyclerView.setAdapter(commentsAdapter);
                return;
            } else {
                Log.d("statesaved", "nottrue");
            }

            NetworkingUtils.tryAuthGet(jsonUrl, isLoggedIn, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mRedditCardDetailData = JsonUtils.parseRedditCardDetail(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        double decimal= Double.parseDouble(mRedditCardDetailData.mUpvoteRatio);
                        double decimal2 = decimal*100;
                        String upvoteRatioAsPercentage = String.format("%.0f", decimal2);

                        upvoteRatioTextView.setText(upvoteRatioAsPercentage + "% Upvoted");

                        switch (mRedditCard.mLikes) {
                            case "true":
                                mRedditCard.mScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) - 1);
                                upvoteToggle = true;
                                downvoteToggle = false;
                                nullToggle = false;
                                upvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));
                                scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Basket_Ball_Orange));
                                break;
                            case "false":
                                mRedditCard.mScore = String.valueOf(Integer.parseInt(mRedditCard.mScore) + 1);
                                downvoteToggle = true;
                                nullToggle = false;
                                upvoteToggle = false;
                                upvoteImageView.setColorFilter(getActivity().getResources().getColor(R.color.Blue_Gray));
                                scoreCountTextView.setTextColor(getActivity().getResources().getColor(R.color.Blue_Gray));
                                break;
                            case "null":
                                nullToggle = true;
                                upvoteToggle = false;
                                downvoteToggle = false;
                                break;
                        }

                        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        //mRecyclerView.setLayoutManager(linearLayoutManager);
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                                new LinearLayoutManager(getContext()).getOrientation());
                        mRecyclerView.addItemDecoration(dividerItemDecoration);
                        CommentsAdapter commentsAdapter = new CommentsAdapter(mRedditCardDetailData.mCommentsList, (AppCompatActivity) getActivity());
                        mRecyclerView.setAdapter(commentsAdapter);
                    }
                });
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_LAYOUT_MANAGER, mLayoutManager.onSaveInstanceState());
        if (mRedditCardDetailData != null) {
            Parcelable data = Parcels.wrap(mRedditCardDetailData);
            outState.putParcelable(STATE_REDDIT_CARD_DETAIL_DATA, data);
        }
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        //((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ColorDrawable color = new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark));
        mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public void onStop() {
        super.onStop();
        //((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ColorDrawable color = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
        mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                Log.d("clicked", "true");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.Viewholder> {
        public ArrayList<RedditComment> mData;
        public AppCompatActivity mActivity;

        public CommentsAdapter(ArrayList<RedditComment> data, AppCompatActivity appCompatActivity) {
            mData = data;
            mActivity = appCompatActivity;
        }

        public class Viewholder extends RecyclerView.ViewHolder {
            TextView authorTextView;
            TextView timeTextView;
            TextView scoreTextView;
            TextView bodyTextView;

            ImageView upvoteButton;
            ImageView downvoteButton;
            TextView replyTextView;

            RecyclerView repliesRecyclerView;

            private boolean upvoteToggle;
            private boolean downvoteToggle;
            private boolean nullToggle;

            public Viewholder(View itemView) {
                super(itemView);
                this.authorTextView = itemView.findViewById(R.id.reddit_comment_author_tv);
                this.timeTextView = itemView.findViewById(R.id.reddit_comment_time_tv);
                this.scoreTextView = itemView.findViewById(R.id.reddit_comment_score_tv);
                this.bodyTextView = itemView.findViewById(R.id.reddit_comment_body_tv);

                this.upvoteButton = itemView.findViewById(R.id.reddit_comment_upvote_iv);
                this.downvoteButton = itemView.findViewById(R.id.reddit_comment_downvote_iv);
                this.replyTextView = itemView.findViewById(R.id.reddit_comment_reply_tv);
                this.repliesRecyclerView = itemView.findViewById(R.id.reddit_comment_replies_rv);

                LinearLayoutManager linearLayoutManager= new LinearLayoutManager(mActivity);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(repliesRecyclerView.getContext(),
                        new LinearLayoutManager(mActivity).getOrientation());
                this.repliesRecyclerView.addItemDecoration(dividerItemDecoration);
                this.repliesRecyclerView.setLayoutManager(linearLayoutManager);


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
            }
        }


        @NonNull
        @Override
        public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reddit_comment, parent, false);

            return new Viewholder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull final Viewholder holder, int position) {
            holder.authorTextView.setText(mData.get(position).mAuthor);
            holder.bodyTextView.setText(mData.get(position).mBody);
            holder.scoreTextView.setText(mData.get(position).mScore);
            holder.timeTextView.setText(mData.get(position).mTimeSincePost);

            //handle replies to comment
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(mData.get(position).mDepth * 32, 0, 0, 0);
            holder.itemView.setLayoutParams(params);
            if (mData.get(position).mDepth != 0) {
                holder.itemView.setBackgroundColor(mActivity.getResources().getColor(R.color.Platinum));
            }
            ArrayList<RedditComment> replies = mData.get(position).mReplies;
            if (replies != null && mData.get(position).mDepth <3) {
                holder.repliesRecyclerView.setAdapter(new CommentsAdapter(replies, mActivity));
            }

            //handle upvote /downvote toggling
            //Log.d("likeornot", mData.get(position).mLikes);
            switch (mData.get(position).mLikes) {
                case "true":
                    mData.get(position).mScore = String.valueOf(Integer.parseInt(mData.get(position).mScore) - 1);
                    holder.upvoteToggle = true;
                    holder.downvoteToggle = false;
                    holder.nullToggle = false;
                    holder.upvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));
                    holder.scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));
                    break;
                case "false":
                    mData.get(position).mScore = String.valueOf(Integer.parseInt(mData.get(position).mScore) + 1);
                    holder.downvoteToggle = true;
                    holder.nullToggle = false;
                    holder.upvoteToggle = false;
                    holder.downvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Blue_Gray));
                    holder.scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Blue_Gray));
                    break;
                case "null":
                    holder.nullToggle = true;
                    holder.upvoteToggle = false;
                    holder.downvoteToggle = false;
                    break;
            }

            final String voteUrl = "https://oauth.reddit.com/api/vote";

            holder.upvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("comment pre-click", mData.get(holder.getAdapterPosition()).mScore);
                    //Colo orange = mActivity.getResources().getColor(R.color.Basket_Ball_Orange);
                    RequestBody requestBody;
                    if (holder.upvoteToggle) {
                        holder.nullToggle = true;
                        holder.downvoteToggle = false;
                        holder.upvoteToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","0")
                                .add("id", mData.get(holder.getAdapterPosition()).mFullname)
                                .build();

                        holder.upvoteButton.setColorFilter(null);
                        holder.downvoteButton.setColorFilter(null);
                        holder.scoreTextView.setTextColor(mActivity.getResources().getColor(android.R.color.black));
                        String newScore = String.valueOf(Integer.parseInt(mData.get(holder.getAdapterPosition()).mScore));
                        holder.scoreTextView.setText(newScore);
                    }
                    else {
                        String newScore = String.valueOf(Integer.parseInt(mData.get(holder.getAdapterPosition()).mScore) + 1);
                        holder.scoreTextView.setText(newScore);

                        holder.upvoteToggle = true;
                        holder.downvoteToggle = false;
                        holder.nullToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","1")
                                .add("id", mData.get(holder.getAdapterPosition()).mFullname)
                                .build();
                        holder.upvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));
                        holder.downvoteButton.setColorFilter(null);
                        holder.scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Basket_Ball_Orange));

                    }
                    NetworkingUtils.post(voteUrl, requestBody, new Callback() {
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

            holder.downvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RequestBody requestBody;
                    if (holder.downvoteToggle) {
                        holder.nullToggle = true;
                        holder.upvoteToggle = false;
                        holder.downvoteToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","0")
                                .add("id", mData.get(holder.getAdapterPosition()).mFullname)
                                .build();

                        holder.upvoteButton.setColorFilter(null);
                        holder.downvoteButton.setColorFilter(null);
                        holder.scoreTextView.setTextColor(mActivity.getResources().getColor(android.R.color.black));
                        String newScore = String.valueOf(Integer.parseInt(mData.get(holder.getAdapterPosition()).mScore));
                        holder.scoreTextView.setText(newScore);
                    } else {
                        String newScore = String.valueOf(Integer.parseInt(mData.get(holder.getAdapterPosition()).mScore) - 1);
                        holder.scoreTextView.setText(newScore);

                        holder.upvoteToggle = false;
                        holder.downvoteToggle = true;
                        holder.nullToggle = false;
                        requestBody = new FormBody.Builder()
                                .add("dir","-1")
                                .add("id", mData.get(holder.getAdapterPosition()).mFullname)
                                .build();
                        holder.downvoteButton.setColorFilter(mActivity.getResources().getColor(R.color.Blue_Gray));
                        holder.upvoteButton.setColorFilter(null);
                        holder.scoreTextView.setTextColor(mActivity.getResources().getColor(R.color.Blue_Gray));
                    }

                    NetworkingUtils.post(voteUrl, requestBody, new Callback() {
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

            holder.replyTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReplyOrCommentDialogFragment dialog = ReplyOrCommentDialogFragment.newInstance(mData.get(holder.getAdapterPosition()), null);
                    dialog.show(mActivity.getSupportFragmentManager(), "commenttag");
                }
            });
        }
        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private void createCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Comment on " + mRedditCard.mTitle);
        builder.setMessage(mRedditCard.mTimeSinceCreated +"\n" + "by " + mRedditCard.mAuthor);
        final EditText input = new EditText(getActivity());
        //pref.getString("")
        input.setHint("Your Comment");
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String replyText = input.getText().toString();
                Log.d("reply", "submitted");
                String url = "https://oauth.reddit.com/api/comment";
                RequestBody requestBody = new FormBody.Builder()
                        .add("parent",mRedditCard.mFullname)
                        .add("text", replyText)
                        .build();
                NetworkingUtils.post(url, requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        Log.d("reply response", result);
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /*
    private static void createReplyDialog(AppCompatActivity activity, final RedditComment redditComment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("ReplyOrComment to " + redditComment.mAuthor);
        builder.setMessage(redditComment.mTimeSincePost +"\n" + redditComment.mBody);
        final EditText input = new EditText(activity);
        //pref.getString("")
        input.setHint("Your ReplyOrComment");
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String replyText = input.getText().toString();
                Log.d("reply", "submitted");
                String url = "https://oauth.reddit.com/api/comment";
                RequestBody requestBody = new FormBody.Builder()
                        .add("parent",redditComment.mFullname)
                        .add("text", replyText)
                        .build();
                NetworkingUtils.post(url, requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        Log.d("reply response", result);
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        /*
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Log.d("chxitnow", "dialog dismissed");
                String replyText = input.getText().toString();
                Log.d("chxitnow", replyText);
                dialogInterface.cancel();
            }
        });

        builder.show();
    }
    */
}
