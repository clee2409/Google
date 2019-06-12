package com.example.android.Tubeddit.RoomDatabaseStuff;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ReplyOrCommentDao {
    @Query("SELECT * FROM ReplyOrComment")
    LiveData<List<ReplyOrComment>> getAll();

    @Query("SELECT * FROM ReplyOrComment WHERE uid IN (:userIds)")
    List<ReplyOrComment> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM ReplyOrComment WHERE uid LIKE :fullname")
    ReplyOrComment findByFullname(String fullname);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ReplyOrComment... replies);

    @Delete
    void delete(ReplyOrComment replyOrComment);

    @Query("DELETE FROM ReplyOrComment")
    public void nukeTable();
}

