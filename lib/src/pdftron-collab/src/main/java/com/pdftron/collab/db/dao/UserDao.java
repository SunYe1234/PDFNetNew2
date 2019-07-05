package com.pdftron.collab.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.pdftron.collab.db.entity.UserEntity;

/**
 * Data Access Object for the user table
 */
@Dao
public interface UserDao {

    /**
     * Gets current user
     * @return the user
     */
    @Query("SELECT * from user_table ORDER BY date DESC LIMIT 1")
    LiveData<UserEntity> getUser();

    @Query("SELECT * from user_table ORDER BY date DESC LIMIT 1")
    UserEntity getUserSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity userEntity);

    @Query("UPDATE user_table SET active_annotation=:activeAnnotation WHERE id=:id")
    void update(String id, String activeAnnotation);

    @Query("DELETE FROM user_table")
    void deleteAll();
}
