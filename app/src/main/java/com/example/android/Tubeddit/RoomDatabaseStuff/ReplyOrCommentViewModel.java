package com.example.android.Tubeddit.RoomDatabaseStuff;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class ReplyOrCommentViewModel extends AndroidViewModel {
    private LiveData<List<ReplyOrComment>> mAllReplyOrComments;
    private Application mApplication;

    public ReplyOrCommentViewModel(@NonNull Application application) {
        super(application);
        mApplication = application;
        mAllReplyOrComments = AppDatabase.getDatabase(mApplication.getApplicationContext()).replyOrCommentDao().getAll();
    }

    public LiveData<List<ReplyOrComment>> getAllReplyOrComments() { return mAllReplyOrComments; }

    public void insert(ReplyOrComment replyOrComment) {AppDatabase.getDatabase(mApplication.getApplicationContext()).replyOrCommentDao().getAll();}
    public void findByFullname(String fullname) {
        AppDatabase.getDatabase(mApplication.getApplicationContext()).replyOrCommentDao().findByFullname(fullname);
    }

}
