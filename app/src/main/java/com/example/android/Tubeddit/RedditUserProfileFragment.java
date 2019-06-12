package com.example.android.Tubeddit;


import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.RedditComment;
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
public class RedditUserProfileFragment extends Fragment {

    private static final String ARGUMENTS_USER = "ARGUMENTS_USER";

    String mUserName;
    ArrayList<RedditComment> mUserCommentsList;
    ArrayList<RedditCard> mUserCardsList;
    RecyclerView mRecyclerView;
    LinearLayoutManager mCardsLinearLayoutManager;
    Toolbar mToolbar;

    public static RedditUserProfileFragment newInstance(String userName) {

        RedditUserProfileFragment fragment = new RedditUserProfileFragment();
        Bundle args = new Bundle(1);
        args.putString(ARGUMENTS_USER, userName);
        fragment.setArguments(args);
        return fragment;
    }

    public RedditUserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARGUMENTS_USER)) {
            mUserName = getArguments().getString(ARGUMENTS_USER);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("profile", mUserName);
        mCardsLinearLayoutManager = new LinearLayoutManager(getActivity());
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mRecyclerView = view.findViewById(R.id.reddit_user_profile_rv);
        mRecyclerView.setLayoutManager(mCardsLinearLayoutManager);
        //mRecyclerView.setItemViewCacheSize(25);
        //mRecyclerView.setDrawingCacheEnabled(true);
        //mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        //mCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final String url = "https://www.reddit.com/user/" + mUserName + "/.json";

        bindCard(url);
    }


    @Override
    public void onResume() {
        super.onResume();
        //((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        mToolbar.setTitle(mUserName + "'s Profile");
    }

    @Override
    public void onStop() {
        super.onStop();
        //((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    private void bindComments() {
        final String url = "https://www.reddit.com/user/" + mUserName + "/.json";
        NetworkingUtils.newGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("Network Response", url + ": " + result);
                try {
                    mUserCommentsList = JsonUtils.parseRedditComments(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                                new LinearLayoutManager(getContext()).getOrientation());
                        mRecyclerView.addItemDecoration(dividerItemDecoration);
                        mRecyclerView.setAdapter(new ProfileFeedAdapter(mUserCommentsList, mUserCardsList));
                    }
                });

            }
        });
    }

    private class ProfileFeedAdapter extends RecyclerView.Adapter<ProfileFeedAdapter.Viewholder> {
        public ArrayList<RedditComment> mComments;
        public ArrayList<RedditCard> mCards;

        public ProfileFeedAdapter(ArrayList<RedditComment> data, ArrayList<RedditCard> cards) {
            mComments = data;
            mCards = cards;
        }

        public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView titleTextView;
            TextView subredditTextView;
            ImageView thumbnailImageView;
            TextView scoreTextView;
            TextView authorTextView;
            TextView timePostedTextView;
            TextView commentCountTextView;
            ImageView previewImageView;

            TextView commentAuthorTextView;
            TextView commentTimeSincePostTextView;
            TextView commentScoreTextView;
            TextView commentBodyTextView;

            public Viewholder(View itemView) {
                super(itemView);
                this.titleTextView = itemView.findViewById(R.id.reddit_title_tv);
                this.subredditTextView = itemView.findViewById(R.id.reddit_subreddit_tv);
                this.thumbnailImageView = itemView.findViewById(R.id.reddit_thumbnailUrl_iv);
                this.scoreTextView = itemView.findViewById(R.id.reddit_score_tv);
                this.authorTextView = itemView.findViewById(R.id.reddit_author_tv);
                this.timePostedTextView = itemView.findViewById(R.id.reddit_time_since_created_tv);
                this.previewImageView = itemView.findViewById(R.id.reddit_preview_url_iv);
                this.commentCountTextView = itemView.findViewById(R.id.reddit_card_comment_count_tv);

                this.commentAuthorTextView = itemView.findViewById(R.id.reddit_comment_author_tv);
                this.commentTimeSincePostTextView = itemView.findViewById(R.id.reddit_comment_time_tv);
                this.commentScoreTextView = itemView.findViewById(R.id.reddit_comment_score_tv);
                this.commentBodyTextView = itemView.findViewById(R.id.reddit_comment_body_tv);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                Fragment fragment = RedditCardDetailFragment.newInstance(
                        mCards.get(getAdapterPosition()));

                //R.id.reddit_activity_fragment_container).getId()
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(getActivity().findViewById(R.id.reddit_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }


        @NonNull
        @Override
        public ProfileFeedAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reddit_user_profile, parent, false);
            return new ProfileFeedAdapter.Viewholder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull final ProfileFeedAdapter.Viewholder holder, int position) {
            Log.d("sizecheck", String.valueOf(mComments.size()));
            holder.commentAuthorTextView.setText(mComments.get(position).mAuthor);
            holder.commentBodyTextView.setText(mComments.get(position).mBody);
            holder.commentScoreTextView.setText(mComments.get(position).mScore);
            holder.commentTimeSincePostTextView.setText(mComments.get(position).mTimeSincePost);

            holder.titleTextView.setText(mCards.get(position).mTitle);
            holder.subredditTextView.setText("r/" + mCards.get(position).mSubreddit);
            holder.scoreTextView.setText(mCards.get(position).mScore);
            holder.timePostedTextView.setText(mCards.get(position).mTimeSinceCreated);
            holder.authorTextView.setText(" by " + mCards.get(position).mAuthor);
            holder.commentCountTextView.setText(mCards.get(position).mCommentCount);

            String thumbnailUrl = mUserCardsList.get(position).mThumbnailUrl;
            Log.d("wtf", "isDis");

            holder.authorTextView.setVisibility(View.GONE);
            if (thumbnailUrl.equals("spoiler")) {
                holder.thumbnailImageView.setImageResource(R.drawable.spoiler_tag);
            } else if (thumbnailUrl.equals("self") || thumbnailUrl.equals("")) {
                holder.thumbnailImageView.setImageResource(R.drawable.text_icon);
            } else //Glide.with(getContext()).load(mUserCardsList.get(position).mThumbnailUrl).fitCenter().into(holder.thumbnailImageView);
            Picasso.get().load(mUserCardsList.get(position).mThumbnailUrl).into(holder.thumbnailImageView);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }
    }

    private void bindCard(String url) {
        NetworkingUtils.newGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("resultchex", result);

                ArrayList<RedditCard> redditCards = null;

                try {
                    mUserCardsList = JsonUtils.parseRedditCards(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //final RecyclerView mCardsRecyclerView = getView().findViewById(R.id.reddit_card_feed_rv);
                //final ArrayList<RedditCard> finalRedditCards = redditCards;

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        bindComments();
                    }
                });

            }
        });
    }

}
