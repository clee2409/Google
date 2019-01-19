package com.example.android.faceddit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.example.android.faceddit.MainActivity.frontpageUrl;

public class RedditNewsfeedFragment extends Fragment implements JsonUtils.OKHttpHandler.AsyncResponse {

    public RedditNewsfeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JsonUtils.fetchRawJson(this, frontpageUrl);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_newsfeed, container, false);
    }

    @Override
    public void processFinish(String output) {

    }
}
