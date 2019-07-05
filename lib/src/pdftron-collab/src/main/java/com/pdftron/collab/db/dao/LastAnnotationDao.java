package com.pdftron.collab.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.pdftron.collab.db.entity.LastAnnotationEntity;

import java.util.List;

/**
 * Data Access Object for the last annotation table
 */
@Dao
public interface LastAnnotationDao {

    /**
     * Gets the last annotations merged
     * @return the last annotations
     */
    @Query("SELECT * from last_annotation_table ORDER BY id ASC")
    LiveData<List<LastAnnotationEntity>> getLastAnnotations();

    @Query("SELECT * from last_annotation_table ORDER BY id ASC")
    List<LastAnnotationEntity> getLastAnnotationsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LastAnnotationEntity lastAnnotationEntity);

    @Query("DELETE FROM last_annotation_table")
    void deleteAll();
}
