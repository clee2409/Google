package com.example.android.faceddit;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements JsonUtils.OKHttpHandler.AsyncResponse{

    public static final String testUrl = "https://www.reddit.com/user/bobbybryce/comments/.json";
    public static final String secondUrl = "https://www.reddit.com/user/wroughtironhero/comments/.json";
    public static final String frontpageUrl = "https://www.reddit.com/.json";

    private TextView mTestTextView;
    private TextView mRedditTitleTextView;
    private TextView mRedditUsernameTextView;

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private FragmentManager mFragmentManager;

    private RedditCardDetailFragment mRedditCardDetailFragment;

    private ArrayList<String> mockArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        mRedditCardDetailFragment = new RedditCardDetailFragment();

        mRecyclerView = findViewById(R.id.reddit_rv);

    }

    @Override
    public void processFinish(String output) {

        ArrayList<RedditCard> redditCardArrayList = null;

        try {
            redditCardArrayList = JsonUtils.parseRedditCards(output);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        Adapter adapter = new Adapter(redditCardArrayList);
        mRecyclerView.setAdapter(adapter);
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        ArrayList<RedditCard> mData;

        public Adapter(ArrayList<RedditCard> data) {
            mData = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView titleTextView;
            TextView subredditTextView;
            TextView thumbnailUrlTextView;
            TextView scoreTextView;
            TextView authorTextView;

            ViewHolder(View itemView) {
                super(itemView);
                this.titleTextView = itemView.findViewById(R.id.reddit_title_tv);
                this.subredditTextView = itemView.findViewById(R.id.reddit_subreddit_tv);
                this.thumbnailUrlTextView = itemView.findViewById(R.id.reddit_thumbnailUrl_tv);
                this.scoreTextView = itemView.findViewById(R.id.reddit_score_tv);
                this.authorTextView = itemView.findViewById(R.id.reddit_author_tv);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                mFragmentManager.beginTransaction()
                        .add(R.id.main_activity_fragment, mRedditCardDetailFragment)
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

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

}
