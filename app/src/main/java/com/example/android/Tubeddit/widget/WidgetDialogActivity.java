package com.example.android.Tubeddit.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.Tubeddit.R;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.example.android.Tubeddit.widget.ListViewService.EXTRA_WIDGET_SUBREDDIT;
import static com.example.android.Tubeddit.widget.RedditWidgetProvider.ACTION_GET_SUBREDDIT_FEED;

public class WidgetDialogActivity extends AppCompatActivity {

    Activity mActivity;
    int mApplicationId;
    String mSubredditName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_APPWIDGET_ID)) mApplicationId = getIntent().getIntExtra(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            else {
                Toast.makeText(mActivity, R.string.error_no_intent, Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        }
        //setContentView(R.layout.activity_widget_dialog);
        displayEditTextDialog();
    }

    private void displayEditTextDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter A Subreddit");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);


        input.setText("/r/");
        Selection.setSelection(input.getText(), input.getText().length());

        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("/r/")){
                    input.setText("/r/");
                    Selection.setSelection(input.getText(), input.getText().length());
                }

            }
        });

        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String subredditName = input.getText().toString().substring(3);
                Log.d("onclickcheck", subredditName);
                updateWidget(subredditName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Log.d("it is", "dismissed");
                mActivity.finish();
            }
        });

        builder.show();
    }

    private void updateWidget(String subredditName) {

        /*
        Intent intent = new Intent(this, RedditWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), RedditWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        sendBroadcast(intent);
        */



        Intent getSubredditFeed = new Intent(this, RedditWidgetProvider.class);
        getSubredditFeed.putExtra(EXTRA_WIDGET_SUBREDDIT, subredditName);
        getSubredditFeed.setAction(ACTION_GET_SUBREDDIT_FEED);
        getSubredditFeed.putExtra(EXTRA_APPWIDGET_ID, mApplicationId);
        this.sendBroadcast(getSubredditFeed);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mActivity.finish();
    }
}
