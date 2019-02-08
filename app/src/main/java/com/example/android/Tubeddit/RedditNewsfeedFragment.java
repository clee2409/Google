package com.example.android.Tubeddit;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;

import static com.example.android.Tubeddit.MainActivity.frontpageUrl;

public class RedditNewsfeedFragment extends Fragment implements NetworkingUtils.OKHttpHandler.AsyncResponse {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public RedditNewsfeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_newsfeed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = getView().findViewById(R.id.reddit_rv);
        NetworkingUtils.get(this, frontpageUrl);
    }

    @Override
    public void processFinish(String output) {
        ArrayList<RedditCard> redditCardArrayList = null;

        try {
            redditCardArrayList = JsonUtils.parseRedditCards(output);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        RedditNewsfeedFragment.Adapter adapter = new RedditNewsfeedFragment.Adapter(redditCardArrayList);
        mRecyclerView.setAdapter(adapter);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        ArrayList<RedditCard> mData;

        public Adapter(ArrayList<RedditCard> data) {
            mData = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView titleTextView;
            TextView subredditTextView;
            ImageView thumbnailImageView;
            TextView scoreTextView;
            TextView authorTextView;

            ViewHolder(View itemView) {
                super(itemView);
                this.titleTextView = itemView.findViewById(R.id.reddit_title_tv);
                this.subredditTextView = itemView.findViewById(R.id.reddit_subreddit_tv);
                this.thumbnailImageView = itemView.findViewById(R.id.reddit_thumbnailUrl_iv);
                this.scoreTextView = itemView.findViewById(R.id.reddit_score_tv);
                this.authorTextView = itemView.findViewById(R.id.reddit_author_tv);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                RedditCardDetailFragment fragment = RedditCardDetailFragment.newInstance(
                        mData.get(getAdapterPosition()));
                Toast.makeText(getActivity(), "clicked", Toast.LENGTH_SHORT).show();

                getFragmentManager().beginTransaction()
                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(getActivity().findViewById(R.id.main_activity_fragment_container).getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reddit_card, parent, false);

            return new ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.titleTextView.setText(mData.get(position).mTitle);
            holder.subredditTextView.setText(mData.get(position).mSubreddit);
            holder.scoreTextView.setText(mData.get(position).mScore);
            holder.authorTextView.setText(mData.get(position).mAuthor);

            Log.d("checkdispls", mData.get(position).mThumbnailUrl);

            Picasso.get().load(mData.get(position).mThumbnailUrl).into(holder.thumbnailImageView, new Callback() {
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
