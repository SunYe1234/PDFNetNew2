package com.pdftron.collab.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.pdftron.collab.db.converter.DateConverter;
import com.pdftron.collab.db.dao.AnnotationDao;
import com.pdftron.collab.db.dao.DocumentDao;
import com.pdftron.collab.db.dao.LastAnnotationDao;
import com.pdftron.collab.db.dao.ReplyDao;
import com.pdftron.collab.db.dao.UserDao;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.DocumentEntity;
import com.pdftron.collab.db.entity.LastAnnotationEntity;
import com.pdftron.collab.db.entity.ReplyEntity;
import com.pdftron.collab.db.entity.UserEntity;

/**
 * The Room database that contains the collaboration information
 */
@Database(entities = {UserEntity.class, DocumentEntity.class, AnnotationEntity.class, LastAnnotationEntity.class, ReplyEntity.class}, version = 6)
@TypeConverters(DateConverter.class)
public abstract class CollabDatabase extends RoomDatabase {

    private static CollabDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "pdftron-collab.db";

    public abstract UserDao userDao();

    public abstract DocumentDao documentDao();

    public abstract AnnotationDao annotationDao();

    public abstract LastAnnotationDao lastAnnotationDao();

    public abstract ReplyDao replyDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static CollabDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (CollabDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            CollabDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}
