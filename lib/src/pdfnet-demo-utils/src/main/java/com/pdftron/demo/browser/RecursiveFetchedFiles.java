package com.pdftron.demo.browser;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.pdftron.pdf.utils.Logger;
import com.pdftron.demo.utils.MiscUtils;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.Constants;
import com.pdftron.pdf.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.pdftron.pdf.model.BaseFileInfo.FILE_TYPE_FOLDER;

/**
 * Recursively fetch the local files stored on the device (i.e. shared storage and external
 * storage on SD card).
 */
@SuppressWarnings("RedundantThrows")
class RecursiveFetchedFiles {
    private final String TAG = RecursiveFetchedFiles.class.toString();
    private final Set<String> mSuffixSet = new HashSet<>();
    private final Comparator<FileInfo> mDefaultFolderComparator = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        }
    };

    @Nullable
    private final File[] mRootDirs;
    private boolean mEmulatedExist = false;

    RecursiveFetchedFiles(@Nullable Context context) {
        this(context != null && Utils.isKitKat() ?
            context.getExternalFilesDirs(null) : null);
    }

    private RecursiveFetchedFiles(@Nullable File[] rootDirs) {
        mSuffixSet.addAll(Arrays.asList(Constants.FILE_NAME_EXTENSIONS_VALID));
        mRootDirs = rootDirs;
    }

    /**
     * @return an {@link Observable} that fetches files from disk. Emits list of supported files
     * in each directory on disk.
     */
    @NonNull
    public Observable<List<FileInfo>> getFiles() {
        return Observable.create(new ObservableOnSubscribe<List<FileInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<FileInfo>> emitter) throws Exception {
                Logger.INSTANCE.LogD(TAG, "Subscribe RecursiveFetchedFiles");
                traverseFileEmitter(emitter);
            }
        });
    }

    /**
     * @return The internal storage and external SD card root directories
     */
    @WorkerThread
    private List<FileInfo> getRootFolders() {
        List<FileInfo> rootFolders = new ArrayList<>();
        File storageDirectory = Environment.getExternalStorageDirectory();
        // For API19+, we can get the external SD card directory from system API
        // here, we add the internal storage dir and external storage dir
        // (bumped to Marshmallow to resolve duplicate file issue)
        if (Utils.isMarshmallow()) {
            rootFolders.add(new FileInfo(FILE_TYPE_FOLDER, storageDirectory));
            if (mRootDirs == null) {
                return rootFolders;
            }
            for (File file : mRootDirs) {
                boolean canAdd = true;
                while (file != null) {
                    file = file.getParentFile();
                    if (file == null) {
                        break;
                    }
                    String path = file.getAbsolutePath();
                    if (path.equalsIgnoreCase("/storage")
                        || path.equalsIgnoreCase("/")) {
                        break;
                    }
                    if (file.equals(storageDirectory)) {
                        // we want the internal storage dir from system API instead
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    rootFolders.add(new FileInfo(FILE_TYPE_FOLDER, file));
                }
            }
        } else {
            File rootDir = storageDirectory;
            while (rootDir != null && rootDir.getParentFile() != null && !rootDir.getParentFile()
                .getAbsolutePath()
                .equalsIgnoreCase("/")) {
                rootDir = rootDir.getParentFile();
            }
            rootFolders.add(new FileInfo(FILE_TYPE_FOLDER, rootDir));
        }
        MiscUtils.sortFileInfoList(rootFolders, mDefaultFolderComparator);
        return rootFolders;
    }

    /**
     * Emits files for each folder in the directory
     */
    @WorkerThread
    private void traverseFileEmitter(ObservableEmitter<List<FileInfo>> emitter) {
        mSuffixSet.addAll(Arrays.asList(Constants.FILE_NAME_EXTENSIONS_VALID));
        File emulate = new File("/storage/emulated");
        mEmulatedExist = emulate.exists();

        List<FileInfo> rootFolders = getRootFolders();
        for (FileInfo folderInfo : rootFolders) {
            if (isCancelled(emitter)) {
                emitter.onComplete();
                return;
            }
            File folder = folderInfo.getFile();
            traverseFiles(folder, emitter);
        }
        emitter.onComplete();
    }

    @WorkerThread
    private void traverseFiles(@Nullable File folder, ObservableEmitter<List<FileInfo>> emitter) {
        if (folder == null || !folder.isDirectory() || isCancelled(emitter)) {
            if (isCancelled(emitter)) {
                emitter.onComplete();
                return;
            }
            return;
        }
        try {
            File[] files = folder.listFiles();
            if (files != null) {
                ArrayList<FileInfo> folderInfoList = new ArrayList<>();
                ArrayList<FileInfo> fileInfoList = new ArrayList<>();
                for (File file : files) {
                    if (accept(file)) {
                        if (file.isDirectory()) {
                            folderInfoList.add(new FileInfo(FILE_TYPE_FOLDER, file));
                        } else {
                            fileInfoList.add(new FileInfo(BaseFileInfo.FILE_TYPE_FILE, file));
                        }
                    }
                }
                if (isCancelled(emitter)) {
                    return;
                }
                if (!fileInfoList.isEmpty()) {
                    emitter.onNext(fileInfoList);
                }
                if (!folderInfoList.isEmpty()) {
                    MiscUtils.sortFileInfoList(folderInfoList, mDefaultFolderComparator);
                }
                for (FileInfo folderInfo : folderInfoList) {
                    traverseFiles(folderInfo.getFile(), emitter);
                    if (isCancelled(emitter)) {
                        emitter.onComplete();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            emitter.onError(e);
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        }
    }

    /**
     * @param file to check
     * @return true the file is a supported file ({@link #mSuffixSet}) or a directory.
     */
    private boolean accept(@Nullable File file) {
        if (file == null || file.isHidden()) {
            return false;
        }
        if (!Utils.isMarshmallow()) {
            String path = file.getAbsolutePath();
            // workaround issue where same file shows up multiple times
            if (path.contains("/emulated/legacy/")
                || (mEmulatedExist && path.contains("/storage/sdcard0/"))) {
                return false;
            }
        }
        if (file.isDirectory()) {
            return true;
        }
        String name = file.getName();
        String ext = Utils.getExtension(name);
        return mSuffixSet.contains(ext) && file.canRead();
    }

    private boolean isCancelled(ObservableEmitter<List<FileInfo>> emitter) {
        boolean cancelled = emitter.isDisposed();
        if (cancelled) {
            Logger.INSTANCE.LogD(TAG, "Cancelled RecursiveFetchedFiles");
        }
        return cancelled;
    }
}
