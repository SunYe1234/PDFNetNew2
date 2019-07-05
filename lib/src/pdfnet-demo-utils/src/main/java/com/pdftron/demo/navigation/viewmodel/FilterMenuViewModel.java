package com.pdftron.demo.navigation.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.pdf.utils.Constants;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;

/**
 * A {@link ViewModel} in charge of the filtering menu functionality
 */
public class FilterMenuViewModel extends AndroidViewModel {

    @Nullable
    private OnFilterTypeChangeListener mListener;

    @Nullable
    private String mSettingsSuffix;

    public FilterMenuViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Callback for updating menu UI
     */
    public interface OnFilterTypeChangeListener {
        void setChecked(int fileType, boolean isChecked);

        void setAllChecked(boolean isChecked);

        void updateFilter(int fileType, boolean isEnabled);
    }

    /**
     * Initialize filtering preferences stored in {@link android.content.SharedPreferences}
     *
     * @param settingsSuffix
     * @param listener
     */
    public void initialize(@NonNull String settingsSuffix, @NonNull OnFilterTypeChangeListener listener) {

        mListener = listener;
        mSettingsSuffix = settingsSuffix;

        // Load shared pref for document filtering
        if (PdfViewCtrlSettingsManager.getFileFilter(getApplication(),
            Constants.FILE_TYPE_PDF, mSettingsSuffix)) {
            mListener.setChecked(Constants.FILE_TYPE_PDF, true);
        }

        if (PdfViewCtrlSettingsManager.getFileFilter(getApplication(),
            Constants.FILE_TYPE_DOC, mSettingsSuffix)) {
            mListener.setChecked(Constants.FILE_TYPE_DOC, true);
        }

        if (PdfViewCtrlSettingsManager.getFileFilter(getApplication(),
            Constants.FILE_TYPE_IMAGE, mSettingsSuffix)) {
            mListener.setChecked(Constants.FILE_TYPE_IMAGE, true);
        }
        checkAllFilesIfNoFilter();
    }

    /**
     * Toggles the filtering for a specified file type
     *
     * @param fileType file type from {@link Constants}
     */
    public void toggleFileFilter(int fileType) {
        boolean wasChecked =
            PdfViewCtrlSettingsManager.getFileFilter(
                getApplication(),
                fileType,
                mSettingsSuffix
            );
        boolean isChecked = !wasChecked;
        setFilter(fileType, isChecked);

        // Check AllFiles menu item if no filters are on
        checkAllFilesIfNoFilter();
    }

    /**
     * Clears all file type filters
     */
    public void clearFileFilters() {
        setFilter(Constants.FILE_TYPE_PDF, false);
        setFilter(Constants.FILE_TYPE_DOC, false);
        setFilter(Constants.FILE_TYPE_IMAGE, false);
        if (mListener != null) {
            mListener.setAllChecked(true);
        }
    }

    /**
     * Enable or disable specified filter type
     *
     * @param fileType  file type from {@link Constants}
     * @param isEnabled true if this file type is enabled
     */
    private void setFilter(int fileType, boolean isEnabled) {
        PdfViewCtrlSettingsManager.updateFileFilter(
            getApplication(),
            fileType,
            mSettingsSuffix,
            isEnabled
        );
        if (mListener != null) {
            mListener.setChecked(fileType, isEnabled);
            mListener.updateFilter(fileType, isEnabled);
        }
    }

    /**
     * If any filtering is used, unchecks mFilterAll
     */
    private void checkAllFilesIfNoFilter() {
        boolean areAllTurnedOff = true;
        if (PdfViewCtrlSettingsManager.getFileFilter(getApplication(),
            Constants.FILE_TYPE_PDF, mSettingsSuffix)) {
            areAllTurnedOff = false;
        }
        if (PdfViewCtrlSettingsManager.getFileFilter(getApplication(),
            Constants.FILE_TYPE_DOC, mSettingsSuffix)) {
            areAllTurnedOff = false;
        }
        if (PdfViewCtrlSettingsManager.getFileFilter(getApplication(),
            Constants.FILE_TYPE_IMAGE, mSettingsSuffix)) {
            areAllTurnedOff = false;
        }

        if (mListener != null) {
            mListener.setAllChecked(areAllTurnedOff);
        }
    }
}
