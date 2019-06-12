package com.example.android.Tubeddit;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.Tubeddit.data.YoutubeComment;
import com.example.android.Tubeddit.data.YoutubeVideo;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;

public class YoutubeVideoDetailActivity extends YouTubeBaseActivity implements NetworkingUtils.OKHttpHandler.AsyncResponse {

    public static String EXTRA_YOUTUBE_VIDEO_DATA = "Bundle Key Containing Url";
    private YoutubeVideo mYoutubeVideo;
    private String apiKey = "AIzaSyD0c6zzMUaBltdZCQ-GJYjdZKodBL74BxQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_detail);

        Parcelable savedYoutubeVideo = getIntent().getParcelableExtra(EXTRA_YOUTUBE_VIDEO_DATA);
        mYoutubeVideo = Parcels.unwrap(savedYoutubeVideo);
        if (savedInstanceState != null) {
            mYoutubeVideo = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_YOUTUBE_VIDEO_DATA));
        }

        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player);
        youTubePlayerView.initialize(apiKey,
                new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        youTubePlayer.cueVideo(mYoutubeVideo.mId);
                        youTubePlayer.play();
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                        Log.d("check this", youTubeInitializationResult.toString());

                    }
                });

        String videoCommentsUrl = "https://www.googleapis.com/youtube/v3/commentThreads?order=relevance" +
                "&key=" + apiKey +
                "&textFormat=plainText" +
                "&part=snippet" +
                "&videoId=" + mYoutubeVideo.mId +
                "&maxResults=50";

        NetworkingUtils.get(this, videoCommentsUrl);
    }

    @Override
    public void processFinish(String output) {
        ArrayList<YoutubeComment> resultCommentList = null;

        try {
            resultCommentList = JsonUtils.parseYoutubeComments(output);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = findViewById(R.id.youtube_comments_list_rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        Adapter adapter = new Adapter(resultCommentList);
        recyclerView.setAdapter(adapter);

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        ArrayList<YoutubeComment> mData;

        public Adapter(ArrayList<YoutubeComment> data) {
            mData = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView authorNameTextView;
            TextView bodyTextView;
            TextView publishedTimeTextView;
            TextView updatedTimeTextView;
            TextView likeCountTextView;
            TextView replyCountTextView;
            ImageView authorProfileImageImageView;
            Button viewRepliesButton;

            public ViewHolder(View itemView) {
                super(itemView);
                authorNameTextView = itemView.findViewById(R.id.youtube_comment_author_tv);
                bodyTextView = itemView.findViewById(R.id.youtube_comment_text_tv);
                publishedTimeTextView = itemView.findViewById(R.id.youtube_comment_published_time_tv);
                updatedTimeTextView = itemView.findViewById(R.id.youtube_comment_updated_time_tv);
                likeCountTextView = itemView.findViewById(R.id.youtube_comment_like_count_tv);
                replyCountTextView = itemView.findViewById(R.id.youtube_comment_reply_count_tv);
                authorProfileImageImageView = itemView.findViewById(R.id.youtube_comment_profile_image_iv);
                viewRepliesButton = itemView.findViewById(R.id.youtube_comment_view_replies_btn);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.list_item_youtube_comment, parent, false);

            return new ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.authorNameTextView.setText(mData.get(position).mAuthor);
            holder.bodyTextView.setText(mData.get(position).mText);
            holder.replyCountTextView.setText(mData.get(position).mReplyCount);
            holder.likeCountTextView.setText(mData.get(position).mLikeCount);
            holder.publishedTimeTextView.setText(mData.get(position).mPublishedTime);
            holder.updatedTimeTextView.setText(mData.get(position).mUpdatedTime);
            Picasso.get().load(mData.get(position).mProfileImageUrl).into(holder.authorProfileImageImageView);

            holder.viewRepliesButton.setText("View replies");
            holder.viewRepliesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    YoutubeCommentRepliesFragment fragment = YoutubeCommentRepliesFragment.newInstance(
                            mData.get(holder.getAdapterPosition()).mCommentId);

                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.animator.slide_up, 0,0, R.animator.slide_down)
                            .add(R.id.youtube_video_detail_fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();


                }
            });

            if (mData.get(position).mReplyCount.equals("0")) {
                ((ViewGroup) holder.viewRepliesButton.getParent()).removeView(
                        findViewById(R.id.youtube_replies_action_close_btn));
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}
