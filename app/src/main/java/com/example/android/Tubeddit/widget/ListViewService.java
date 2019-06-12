package com.example.android.Tubeddit.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.example.android.Tubeddit.R;
import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.utils.JsonUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.android.Tubeddit.RedditCardDetailFragment.EXTRA_REDDIT_CARD;
import static com.example.android.Tubeddit.widget.ListViewService.EXTRA_WIDGET_SUBREDDIT;
import static com.example.android.Tubeddit.widget.RedditWidgetProvider.EXTRA_PERMALINK;


public class ListViewService extends RemoteViewsService {

    public static String EXTRA_WIDGET_SUBREDDIT = "EXTRA_WIDGET_SUBREDDIT";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("Widget", "ongetviewfactory");
        return new ListViewFactory(this.getApplicationContext(), intent);
    }
}

class ListViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private static int mCount = 10;
    private List<RedditCard> mRedditCards = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;
    String mSubredditName;
    Intent mIntent;

    public ListViewFactory(Context context, Intent intent) {
        mSubredditName = intent.getStringExtra(EXTRA_WIDGET_SUBREDDIT);
        mContext = context;
        mIntent = intent;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) Log.d("Listview Service", "invalid Appwidget Id");
    }

    @Override
    public void onCreate() {
        if (mSubredditName != null) {
            Log.d("listviewService", "subreddit: " + mSubredditName);
        } else {
            Log.d("listviewService", "no subreddit");
        }

        for (int i = 0; i < mCount; i++) {

        }
        /*
        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public void onDataSetChanged() {
        String url = "https://reddit.com/.json";
        if (mSubredditName != null) {
            url = "https://reddit.com/r/" + mSubredditName + "/.json";
        }
        Log.d("Widget onDatasetChanged", "calling url:: " + url);

        try {
            String data = run(url);
            mRedditCards = JsonUtils.parseRedditCards(data);

            if (mRedditCards.size() < 10) {
                mCount = mRedditCards.size();
            }

            Log.d("widgetcheck", data);
        } catch (IOException e) {
            e.printStackTrace();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.widget_no_subreddit_error, Toast.LENGTH_LONG).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,R.string.widget_no_subreddit_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        mRedditCards.clear();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public RemoteViews getViewAt(final int i) {
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

        if (mRedditCards.size() == 0) return null;

        if (mRedditCards.get(i).mThumbnailUrl.equals("self") || mRedditCards.get(i).mThumbnailUrl.equals("")) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.text_icon);
            rv.setImageViewBitmap(R.id.reddit_thumbnailUrl_iv, bitmap);
        } else if (mRedditCards.get(i).mThumbnailUrl.equals("spoiler")) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.spoiler_tag);
            rv.setImageViewBitmap(R.id.reddit_thumbnailUrl_iv, bitmap);
        } else {
            Picasso.get().load(mRedditCards.get(i).mThumbnailUrl).fetch(new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Picasso.get().load(mRedditCards.get(i).mThumbnailUrl).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    rv.setImageViewBitmap(R.id.reddit_thumbnailUrl_iv, bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            });
                        }
                    });
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }


        rv.setTextViewText(R.id.reddit_title_tv, mRedditCards.get(i).mTitle);
        rv.setTextViewText(R.id.reddit_subreddit_tv, mRedditCards.get(i).mSubreddit);
        rv.setTextViewText(R.id.reddit_time_since_created_tv, mRedditCards.get(i).mTimeSinceCreated);
        rv.setViewVisibility(R.id.reddit_author_tv, View.GONE);


        //RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(EXTRA_PERMALINK, mRedditCards.get(i).mPermalink);
        fillInIntent.putExtra(EXTRA_REDDIT_CARD, mRedditCards.get(i).mTitle);

        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private String run(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
