package com.pdftron.demo.browser;

import android.support.annotation.NonNull;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;

/**
 * Mock {@link FileInfo} for testing
 */
public class MockFile extends FileInfo {

    private String mFolder;
    private String mFile;

    /**
     * Mock {@link FileInfo} used for testing
     *
     * @param parent   parent directory
     * @param filename the filename
     */
    public MockFile(String parent, String filename) {
        super(BaseFileInfo.FILE_TYPE_FILE, null);
        mFolder = parent;
        mFile = filename;
    }

    @Override
    public String getParentDirectoryPath() {
        return mFolder;
    }

    @NonNull
    @Override
    public String getAbsolutePath() {
        return mFolder + "/" + mFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MockFile)) {
            return false;
        }

        MockFile that = (MockFile) o;
        return mFolder.equals(that.mFolder) && mFile.equals(that.mFile);
    }

    @NonNull
    @Override
    public String getName() {
        return mFile;
    }
}