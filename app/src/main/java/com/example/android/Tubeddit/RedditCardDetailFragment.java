package com.example.android.Tubeddit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.RedditCardDetail;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;

import org.json.JSONException;
import org.parceler.Parcels;

public class RedditCardDetailFragment extends Fragment implements NetworkingUtils.OKHttpHandler.AsyncResponse {

    private static String EXTRA_PERMALINK = "Extra Permalink";
    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private RedditCard mRedditCard;

    public RedditCardDetailFragment() {
        // Required empty public constructor
    }

    public static RedditCardDetailFragment newInstance(RedditCard data) {
        RedditCardDetailFragment fragment = new RedditCardDetailFragment();
        Bundle args = new Bundle(1);
        Parcelable redditCardData = Parcels.wrap(data);
        args.putParcelable(EXTRA_PERMALINK, redditCardData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parcelable redditCardParcel = getArguments().getParcelable(EXTRA_PERMALINK);
        mRedditCard = Parcels.unwrap(redditCardParcel);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_card_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.reddit_comments_rv);

        TextView titleTextView = view.findViewById(R.id.redditcard_detail_title_tv);
        TextView subredditTextView = view.findViewById(R.id.redditcard_detail_subreddit_tv);
        TextView thumbnailUrlTextView = view.findViewById(R.id.redditcard_detail_thumbnailUrl_tv);
        TextView scoreTextView = view.findViewById(R.id.redditcard_detail_score_tv);
        TextView authorTextView = view.findViewById(R.id.redditcard_detail_author_tv);



        titleTextView.setText(mRedditCard.mTitle);
        subredditTextView.setText(mRedditCard.mSubreddit);
        scoreTextView.setText(mRedditCard.mScore);
        authorTextView.setText(mRedditCard.mAuthor);

        String jsonUrl = "https://www.reddit.com" + mRedditCard.mPermalink + ".json";
        Log.d("check this", jsonUrl);
        NetworkingUtils.get(this, jsonUrl);

        super.onViewCreated(view, savedInstanceState);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void processFinish(String output) {
        RedditCardDetail redditCardDetail = null;

        try {
            redditCardDetail = JsonUtils.parseRedditCardDetail(output);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        RedditCardDetailFragment.Adapter adapter = new RedditCardDetailFragment.Adapter(redditCardDetail);
        mRecyclerView.setAdapter(adapter);

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.Viewholder> {
        public RedditCardDetail mData;

        public Adapter(RedditCardDetail data) {
            mData = data;
        }

        public class Viewholder extends RecyclerView.ViewHolder {
            TextView authorTextView;
            TextView timeTextView;
            TextView scoreTextView;
            TextView bodyTextView;

            public Viewholder(View itemView) {
                super(itemView);
                this.authorTextView = itemView.findViewById(R.id.reddit_comment_author_tv);
                this.timeTextView = itemView.findViewById(R.id.reddit_comment_time_tv);
                this.scoreTextView = itemView.findViewById(R.id.reddit_comment_score_tv);
                this.bodyTextView = itemView.findViewById(R.id.reddit_comment_body_tv);

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
        public void onBindViewHolder(@NonNull Viewholder holder, int position) {
            holder.authorTextView.setText(mData.mCommentsList.get(position).mAuthor);
            holder.bodyTextView.setText(mData.mCommentsList.get(position).mBody);
            holder.scoreTextView.setText(mData.mCommentsList.get(position).mScore);
            holder.timeTextView.setText(mData.mCommentsList.get(position).mTimeSincePost);
        }

        @Override
        public int getItemCount() {
            return mData.mCommentsList.size();
        }

    }
}
