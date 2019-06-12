package com.example.android.Tubeddit.RoomDatabaseStuff;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class ReplyOrComment {

    public ReplyOrComment(String uid, String text) {
        this.uid = uid;
        this.mText = text;
    }

    //Comment's fullname/id is saved as UID
    @NonNull
    @PrimaryKey
    public String uid;

    @NonNull
    @ColumnInfo(name = "first_name")
    public String mText;

}
