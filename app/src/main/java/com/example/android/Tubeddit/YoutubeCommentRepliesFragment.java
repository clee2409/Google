package com.example.android.Tubeddit;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.Tubeddit.data.YoutubeComment;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */

//THIS IS NOT A SUPPORT FRAGMENT BTW
public class YoutubeCommentRepliesFragment extends android.app.Fragment implements NetworkingUtils.OKHttpHandler.AsyncResponse {

    public String mCommentId;
    private static String EXTRA_COMMENT_ID = "Extra Comment Id";

    public YoutubeCommentRepliesFragment() {
        // Required empty public constructor
    }

    public static YoutubeCommentRepliesFragment newInstance(String commentId) {
        YoutubeCommentRepliesFragment fragment = new YoutubeCommentRepliesFragment();
        Bundle args = new Bundle(1);
        args.putString(EXTRA_COMMENT_ID, commentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommentId = getArguments().getString(EXTRA_COMMENT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_youtube_comment_replies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.youtube_replies_action_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.youtube_video_detail_fragment_container))
                        .commit();
            }
        });

        String commentRepliesUrl = "https://www.googleapis.com/youtube/v3/commentThreads?order=relevance" +
                "&key=AIzaSyD0c6zzMUaBltdZCQ-GJYjdZKodBL74BxQ" +
                "&textFormat=plainText" +
                "&part=replies" +
                "&id=" + mCommentId +
                "&maxResults=50";

        NetworkingUtils.get(this, commentRepliesUrl);
    }

    @Override
    public void processFinish(String output) {
        ArrayList<YoutubeComment> resultCommentList = null;

        try {
            resultCommentList = JsonUtils.parseYoutubeCommentReplies(output);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = getView().findViewById(R.id.youtube_replies_rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        YoutubeCommentRepliesFragment.Adapter adapter = new YoutubeCommentRepliesFragment.Adapter(resultCommentList);
        recyclerView.setAdapter(adapter);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<YoutubeComment> mData;

        public Adapter(ArrayList<YoutubeComment> data) {
            mData = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView authorNameTextView;
            TextView bodyTextView;
            TextView publishedTimeTextView;
            TextView updatedTimeTextView;
            TextView likeCountTextView;
            ImageView authorProfileImageImageView;

            //These are only here so that they can be programatically hidden
            TextView replyCountTextView;
            Button viewRepliesButton;

            public ViewHolder(View itemView) {
                super(itemView);
                authorNameTextView = itemView.findViewById(R.id.youtube_comment_author_tv);
                bodyTextView = itemView.findViewById(R.id.youtube_comment_text_tv);
                publishedTimeTextView = itemView.findViewById(R.id.youtube_comment_published_time_tv);
                updatedTimeTextView = itemView.findViewById(R.id.youtube_comment_updated_time_tv);
                likeCountTextView = itemView.findViewById(R.id.youtube_comment_like_count_tv);
                authorProfileImageImageView = itemView.findViewById(R.id.youtube_comment_profile_image_iv);

                //These are only here so that they can be programatically hidden
                replyCountTextView = itemView.findViewById(R.id.youtube_comment_reply_count_tv);
                viewRepliesButton = itemView.findViewById(R.id.youtube_comment_view_replies_btn);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vh = LayoutInflater.from(getActivity())
                    .inflate(R.layout.list_item_youtube_comment, parent, false);

            ((ViewGroup) vh).removeView(vh.findViewById(R.id.youtube_comment_view_replies_btn));
            ((ViewGroup) vh).removeView(vh.findViewById(R.id.youtube_comment_reply_count_tv));
            return new ViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.authorNameTextView.setText(mData.get(position).mAuthor);
            holder.bodyTextView.setText(mData.get(position).mText);
            holder.likeCountTextView.setText(mData.get(position).mLikeCount);
            holder.publishedTimeTextView.setText(mData.get(position).mPublishedTime);
            holder.updatedTimeTextView.setText(mData.get(position).mUpdatedTime);

            Picasso.get().load(mData.get(position).mProfileImageUrl).into(holder.authorProfileImageImageView);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }
}
