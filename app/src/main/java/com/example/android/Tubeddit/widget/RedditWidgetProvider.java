package com.example.android.Tubeddit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.Tubeddit.MainActivity;
import com.example.android.Tubeddit.R;

import static com.example.android.Tubeddit.widget.ListViewService.EXTRA_WIDGET_SUBREDDIT;

public class RedditWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_HOME = "ACTION_HOME";
    public static final String ACTION_GET_SUBREDDIT_FEED = "ACTION_GET_SUBREDDIT_FEED";
    public static final String ACTION_REFRESH = "ACTION_REFRESH";
    public static final String ACTION_OPEN_LINK = "ACTION_OPEN_LINK";

    private static final String SHARED_PREFS_SUBREDDIT = "SHARED_PREFS_SUBREDDIT";
    public static final String EXTRA_PERMALINK = "EXTRA_PERMALINK";



    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, ListViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.widget_subreddit_tv, context.getString(R.string.home_page));
            views.setRemoteAdapter(R.id.widget_rv, intent);
            //views.setOnClickPendingIntent(R.id.widget_refresh_iv, getPendingSelfIntent(context, REFRESH));
            //views.setOnClickPendingIntent(R.id.widget_edit_btn, getPendingSelfIntent(context, SEARCH));
            //views.setOnClickPendingIntent(R.id.widget_home_btn, getPendingSelfIntent(context, HOME));

            Intent openActivityIntent = new Intent(context, WidgetDialogActivity.class);
            openActivityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent openActivityPendingIntent = PendingIntent.getActivity(context, 0, openActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent refreshIntent = new Intent(context, RedditWidgetProvider.class);
            refreshIntent.setAction(ACTION_REFRESH);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent refreshPendingIntent =  PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent homepageIntent = new Intent(context, RedditWidgetProvider.class);
            homepageIntent.setAction(ACTION_HOME);
            homepageIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent homepagePendingIntent = PendingIntent.getBroadcast(context, 0, homepageIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_refresh_iv, refreshPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_edit_btn, openActivityPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_home_btn, homepagePendingIntent);

            Intent openLinkIntent = new Intent(context, RedditWidgetProvider.class);
            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            openLinkIntent.setAction(RedditWidgetProvider.ACTION_OPEN_LINK);
            openLinkIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            openLinkIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, openLinkIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_rv, toastPendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, null);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d("Widget", "updated");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d("Widget Onreceive", intent.getAction());

        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().remove(SHARED_PREFS_SUBREDDIT).commit();
        }

        if (intent.getAction().equals(ACTION_OPEN_LINK)) {
            //int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            String permalink = intent.getStringExtra(EXTRA_PERMALINK);

            Intent startActivityIntent = new Intent(context, MainActivity.class);
            startActivityIntent.setAction(ACTION_OPEN_LINK);
            startActivityIntent.putExtra(EXTRA_PERMALINK, permalink);
            startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startActivityIntent);
        }


        if (ACTION_HOME.equals(intent.getAction())) {
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, intent.getStringExtra(AppWidgetManager.EXTRA_APPWIDGET_ID));
            showSubredditPage(context, intent, null);
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().remove(SHARED_PREFS_SUBREDDIT).commit();
        }

        if (ACTION_GET_SUBREDDIT_FEED.equals(intent.getAction())) {
            String subreddit = intent.getStringExtra(EXTRA_WIDGET_SUBREDDIT);
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
            showSubredditPage(context, intent, subreddit);
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(SHARED_PREFS_SUBREDDIT, subreddit).commit();
        }

        if (ACTION_REFRESH.equals(intent.getAction())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            String subreddit = sharedPreferences.getString(SHARED_PREFS_SUBREDDIT, null);
            intent.putExtra(EXTRA_WIDGET_SUBREDDIT, subreddit);
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
            showSubredditPage(context, intent, subreddit);
        }
    }


    private void showSubredditPage(Context context, Intent intent, String subreddit) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.d("getWidgetId", String.valueOf(widgetId));
        if (subreddit != null) {
            Log.d("showsubredditpage", subreddit);
        } else Log.d("showsubredditpage", "is null");

        Intent listviewIntent = new Intent(context, ListViewService.class);
        listviewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        listviewIntent.putExtra(EXTRA_WIDGET_SUBREDDIT, subreddit);
        listviewIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        if (subreddit != null) views.setTextViewText(R.id.widget_subreddit_tv, "r/" + subreddit);
        else views.setTextViewText(R.id.widget_subreddit_tv, context.getString(R.string.home_page));

        //AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.widget_rv);
        views.setRemoteAdapter(R.id.widget_rv, listviewIntent);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, null);
        bindOnClickListeners(context, views, widgetId);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views);
    }

    private void bindOnClickListeners(Context context, RemoteViews views, int appWidgetId) {
        Intent openActivityIntent = new Intent(context, WidgetDialogActivity.class);
        openActivityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent openActivityPendingIntent = PendingIntent.getActivity(context, 0, openActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent refreshIntent = new Intent(context, RedditWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent refreshPendingIntent =  PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent homepageIntent = new Intent(context, RedditWidgetProvider.class);
        homepageIntent.setAction(ACTION_HOME);
        homepageIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent homepagePendingIntent = PendingIntent.getBroadcast(context, 0, homepageIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_refresh_iv, refreshPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_edit_btn, openActivityPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_home_btn, homepagePendingIntent);

        Intent openLinkIntent = new Intent(context, RedditWidgetProvider.class);
        // Set the action for the intent.
        // When the user touches a particular view, it will have the effect of
        // broadcasting TOAST_ACTION.
        openLinkIntent.setAction(RedditWidgetProvider.ACTION_OPEN_LINK);
        openLinkIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //openLinkIntent.setData(Uri.parse(openLinkIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, openLinkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_rv, toastPendingIntent);
    }
}

