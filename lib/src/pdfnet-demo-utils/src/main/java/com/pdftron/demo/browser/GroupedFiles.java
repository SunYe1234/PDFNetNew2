package com.pdftron.demo.browser;

import android.support.annotation.NonNull;

import com.pdftron.demo.utils.MiscUtils;
import com.pdftron.pdf.model.FileInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

class GroupedFiles {
    private static final String TAG = GroupedFiles.class.toString();

    // memory cached data
    private final TreeSet<Group> mGroupedData = new TreeSet<>(new Comparator<Group>() {
        @Override
        public int compare(Group o1, Group o2) {
            if (o1.size() == 0 || o2.size() == 0) {
                return 0;
            } else {
                return o1.mParentDirectory.compareTo(o2.mParentDirectory);
            }
        }
    });
    private final Set<FileInfo> mAddedFiles = new HashSet<>();
    private final Set<FileInfo> mDeletedFiles = new HashSet<>();
    private final AtomicBoolean mHasFiles = new AtomicBoolean(false);

    static void removeDeletedFiles(List<FileInfo> myList, Set<FileInfo> deletedFiles) {
        // Remove all deleted files in this group
        if (!myList.isEmpty() && !deletedFiles.isEmpty()) {
            Iterator<FileInfo> itr = deletedFiles.iterator();
            while (itr.hasNext()) {
                FileInfo deletedFile = itr.next();
                if (myList.remove(deletedFile)) {
                    itr.remove();
                }
            }
        }
    }

    static void insertAddedFiles(List<FileInfo> myList, Set<FileInfo> addedFiles) {
        // Add all new files in this group
        if (!myList.isEmpty() && !addedFiles.isEmpty()) {
            String header = myList.get(0).getParentDirectoryPath();
            Iterator<FileInfo> itr = addedFiles.iterator();
            while (itr.hasNext()) {
                FileInfo addedFile = itr.next();
                if (header.equals(addedFile.getParentDirectoryPath())) {
                    myList.add(addedFile);
                    itr.remove();
                }
            }
        }
    }

    boolean hasFiles() {
        return mHasFiles.get();
    }

    Observable<List<FileInfo>> fromIterable() {
        return Observable.fromIterable(mGroupedData)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Group, List<FileInfo>>() {
                    @Override
                    public List<FileInfo> apply(Group group) throws Exception {
                        return group.mFileGroup;
                    }
                })
                .doOnNext(new Consumer<List<FileInfo>>() {
                    @Override
                    public void accept(List<FileInfo> group) throws Exception {
                        removeDeletedFiles(group, mDeletedFiles);
                        insertAddedFiles(group, mAddedFiles);
                    }
                });
    }

    List<FileInfo> getFlattenedList() {
        List<FileInfo> flattenedList = new ArrayList<>();
        synchronized (mGroupedData) {
            for (Group group : mGroupedData) {
                flattenedList.addAll(group.mFileGroup);
            }
        }
        return flattenedList;
    }

    TreeSet<Group> getGroupedList() {
        return mGroupedData;
    }

    /**
     * Add a list of files in the same directory into this grouped files object. This method
     * assumes that the list of files is in the same directory and does not check for it.
     *
     * @param fileGroup list of files in the same directory.
     */
    boolean addGroupedData(@NonNull List<FileInfo> fileGroup) {
        if (fileGroup.size() != 0) {
            mHasFiles.set(true);
            synchronized (mGroupedData) {
                return mGroupedData.add(new Group(fileGroup, fileGroup.get(0).getParentDirectoryPath()));
            }
        }
        return false;
    }

    void clearGroupedData() {
        mHasFiles.set(false);
        synchronized (mGroupedData) {
            mGroupedData.clear();
        }
    }

    // Clear all the data held by this object
    void releaseMemory() {
        MiscUtils.throwIfNotOnMainThread();
        mGroupedData.clear();
        mAddedFiles.clear();
        mDeletedFiles.clear();
        mHasFiles.set(false);
    }

    public void deleteFile(FileInfo fileInfo) {
        mDeletedFiles.add(fileInfo);
    }

    public void addFile(FileInfo fileInfo) {
        mAddedFiles.add(fileInfo);
    }

    /**
     * Represents a group of files under some directory.
     */
    static class Group {
        final List<FileInfo> mFileGroup;
        final String mParentDirectory;

        Group(@NonNull List<FileInfo> fileGroup, @NonNull String tag) {
            mFileGroup = fileGroup;
            mParentDirectory = tag;
        }

        int size() {
            return mFileGroup.size();
        }

        boolean contains(FileInfo fileInfo) {
            return mFileGroup.contains(fileInfo);
        }
    }
}
