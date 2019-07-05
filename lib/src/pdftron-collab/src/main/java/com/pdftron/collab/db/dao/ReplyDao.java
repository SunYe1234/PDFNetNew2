package com.pdftron.collab.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.pdftron.collab.db.entity.ReplyEntity;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for the annotation reply table
 */
@Dao
public interface ReplyDao {

    /**
     * Gets annotation replies associated with an annotation
     * @param annotId the unique identifier of the annotation
     * @return the annotation replies
     */
    @Query("SELECT * from reply_table WHERE in_reply_to=:annotId ORDER BY creation_date ASC")
    LiveData<List<ReplyEntity>> getReplies(String annotId);

    @Query("SELECT * from reply_table WHERE id=:id")
    ReplyEntity getReplySync(String id);

    @Query("SELECT * from reply_table WHERE in_reply_to=:annotId ORDER BY creation_date DESC")
    List<ReplyEntity> getSortedRepliesSync(String annotId);

    @Query("SELECT * from reply_table WHERE in_reply_to=:annotId ORDER BY creation_date DESC LIMIT 1")
    ReplyEntity getLastReplySync(String annotId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReplyEntity replyEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReplies(List<ReplyEntity> replyEntities);

    @Query("UPDATE reply_table SET contents=:contents, page=:page, date=:date WHERE id=:id")
    void update(String id, String contents, int page, Date date);

    @Query("DELETE FROM reply_table WHERE id = :id")
    void delete(String id);

    @Query("DELETE FROM reply_table")
    void deleteAll();
}
