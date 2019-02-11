package com.example.android.Tubeddit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.Tubeddit.data.YoutubeVideo;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.example.android.Tubeddit.utils.NetworkingUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class YoutubeMainPageFragment extends Fragment implements NetworkingUtils.OKHttpHandler.AsyncResponse {

    private String youtubeMostPopularVidsUrl = "https://www.googleapis.com/youtube/v3/videos" +
            "?part=snippet&maxResults=25&chart=mostPopular&key=AIzaSyD0c6zzMUaBltdZCQ-GJYjdZKodBL74BxQ";

    public YoutubeMainPageFragment() {
        // Required empty public constructor
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
        NetworkingUtils.get(this, youtubeMostPopularVidsUrl);
    }

    @Override
    public void processFinish(String output) {
        ArrayList<YoutubeVideo> resultList = null;

        try {
            resultList = JsonUtils.parseYoutubeVideos(output);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = getView().findViewById(R.id.youtube_videos_feed_rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        YoutubeMainPageFragment.Adapter adapter = new YoutubeMainPageFragment.Adapter(resultList);
        recyclerView.setAdapter(adapter);

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
            ImageView thumbnailImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.youtube_card_title_tv);
                channelTitleTextView = itemView.findViewById(R.id.youtube_card_channel_title_tv);
                publishedDateTextView = itemView.findViewById(R.id.youtube_card_time_published_tv);
                thumbnailImageView = itemView.findViewById(R.id.youtube_card_thumbnail_iv);

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
        public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
            holder.titleTextView.setText(mData.get(position).mTitle);
            holder.channelTitleTextView.setText(mData.get(position).mChannelTitle);
            holder.publishedDateTextView.setText(mData.get(position).mPublishedDate);

            //Log.d("chec pls", mData.get(position).mThumbnailUrl);

            Picasso.get().load(mData.get(position).mThumbnailUrl).into(holder.thumbnailImageView);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

}
