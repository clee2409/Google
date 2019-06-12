package com.example.android.Tubeddit;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.example.android.Tubeddit.RoomDatabaseStuff.AppDatabase;
import com.example.android.Tubeddit.RoomDatabaseStuff.ReplyOrComment;
import com.example.android.Tubeddit.RoomDatabaseStuff.ReplyOrCommentViewModel;
import com.example.android.Tubeddit.data.RedditCard;
import com.example.android.Tubeddit.data.RedditComment;
import com.example.android.Tubeddit.utils.NetworkingUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReplyOrCommentDialogFragment extends DialogFragment {

    private static final String EXTRA_REDDIT_CARD = "EXTRA_REDDIT_CARD";
    private static final String EXTRA_COMMENT = "EXTRA_COMMENT";
    private RedditCard mRedditCard;
    private RedditComment mRedditComment;
    private EditText mEditText;

    private String mSavedText;

    public interface YesNoListener {
        void onYes();
        void onNo();
    }

    public static ReplyOrCommentDialogFragment newInstance(@Nullable RedditComment redditComment, @Nullable RedditCard redditCard) {
        
        Bundle args = new Bundle(2);

        Parcelable comment = Parcels.wrap(redditComment);
        Parcelable card = Parcels.wrap(redditCard);

        args.putParcelable(EXTRA_COMMENT, comment);
        args.putParcelable(EXTRA_REDDIT_CARD, card);

        ReplyOrCommentDialogFragment fragment = new ReplyOrCommentDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parcelable redditCardParcel = getArguments().getParcelable(EXTRA_REDDIT_CARD);
        mRedditCard = Parcels.unwrap(redditCardParcel);

        Parcelable commentParcel = getArguments().getParcelable(EXTRA_COMMENT);
        mRedditComment = Parcels.unwrap(commentParcel);

        //set up view model
        ReplyOrCommentViewModel viewModel = ViewModelProviders.of(this).get(ReplyOrCommentViewModel.class);
        viewModel.getAllReplyOrComments().observe(this, new Observer<List<ReplyOrComment>>() {
            @Override
            public void onChanged(@Nullable List<ReplyOrComment> replyOrComments) {
                Log.d("livedata", "onchange");
            }
        });

        if (mRedditComment != null) {
            if (AppDatabase.getDatabase(getActivity()).replyOrCommentDao().findByFullname(mRedditComment.mFullname) != null) {
                mSavedText = AppDatabase.getDatabase(getActivity()).replyOrCommentDao().findByFullname(mRedditComment.mFullname).uid;
            }
        } else {
            if (AppDatabase.getDatabase(getActivity()).replyOrCommentDao().findByFullname(mRedditCard.mFullname) != null) {
                mSavedText = AppDatabase.getDatabase(getActivity()).replyOrCommentDao().findByFullname(mRedditCard.mFullname).uid;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        if (!(context instanceof YesNoListener)) {
            throw new ClassCastException(context.toString() + " must implement YesNoListener");
        }
        */
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a Comment Dialog, or else create a reply dialog
        if (mRedditCard != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Comment on " + mRedditCard.mTitle);
            builder.setMessage(mRedditCard.mTimeSinceCreated +"\n" + "by " + mRedditCard.mAuthor);
            mEditText = new EditText(getActivity());
            //pref.getString("")
            mEditText.setHint("Your Comment");
            if (mSavedText != null) {
                mEditText.setText(AppDatabase.getDatabase(getActivity()).replyOrCommentDao()
                        .findByFullname(mRedditCard.mFullname).mText);
            }
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            builder.setView(mEditText);
            builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String replyText = mEditText.getText().toString();
                    Log.d("add comment btn", "clicked");
                    String url = "https://oauth.reddit.com/api/comment";
                    RequestBody requestBody = new FormBody.Builder()
                            .add("parent",mRedditCard.mFullname)
                            .add("text", replyText)
                            .build();
                    NetworkingUtils.post(url, requestBody, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String result = response.body().string();
                            Log.d("reply response", result);
                        }
                    });
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Log.d("replydialog", "cancelled");
                }
            });

            //builder.show();

            return builder.create();
        }

        // IT IS A REPLY TO A COMMENT
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("ReplyOrComment to " + mRedditComment.mAuthor);
            builder.setMessage(mRedditComment.mTimeSincePost +"\n" + mRedditComment.mBody);
            mEditText = new EditText(getActivity());
            //pref.getString("")
            mEditText.setHint("Your ReplyOrComment");
            if (mSavedText != null) {
                mEditText.setText(AppDatabase.getDatabase(getActivity()).replyOrCommentDao()
                        .findByFullname(mRedditComment.mFullname).mText);
            }
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            builder.setView(mEditText);
            builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String replyText = mEditText.getText().toString();
                    Log.d("reply", "submitted");
                    String url = "https://oauth.reddit.com/api/comment";
                    RequestBody requestBody = new FormBody.Builder()
                            .add("parent",mRedditComment.mFullname)
                            .add("text", replyText)
                            .build();
                    NetworkingUtils.post(url, requestBody, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String result = response.body().string();
                            Log.d("reply response", result);
                        }
                    });
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });


        /*
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Log.d("chxitnow", "dialog dismissed");
                String replyText = input.getText().toString();
                Log.d("chxitnow", replyText);
                dialogInterface.cancel();
            }
        });
        */
            return builder.create();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mSavedText = mEditText.getText().toString();
        Log.d("EditText input",mEditText.getText().toString());
        if (mSavedText != null) {
            if (mRedditComment != null) {
                saveTextToDatabase(mSavedText, mRedditComment.mFullname);
            } else {
                saveTextToDatabase(mSavedText, mRedditCard.mFullname);
            }
        }

    }

    private void saveTextToDatabase(String text, String id) {
        //Replies / Comments each have their own unique IDs
        ReplyOrComment data = new ReplyOrComment(id, text);
        AppDatabase.getDatabase(getActivity()).replyOrCommentDao().insertAll(data);
    }
}
