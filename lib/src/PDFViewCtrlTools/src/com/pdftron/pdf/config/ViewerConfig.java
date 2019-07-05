package com.pdftron.pdf.config;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;

/**
 * This class is responsible for configuration
 * {@link com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment} and
 * {@link com.pdftron.pdf.controls.PdfViewCtrlTabFragment}.
 * See {@link Builder} for details.
 */
@SuppressWarnings("JavaDoc")
public class ViewerConfig implements Parcelable {

    /**
     * @hide
     */
    public boolean isFullscreenModeEnabled() {
        return fullscreenModeEnabled;
    }

    /**
     * @hide
     */
    public boolean isMultiTabEnabled() {
        return multiTabEnabled;
    }

    /**
     * @hide
     */
    public boolean isDocumentEditingEnabled() {
        return documentEditingEnabled;
    }

    /**
     * @hide
     */
    public boolean isLongPressQuickMenuEnabled() {
        return longPressQuickMenuEnabled;
    }

    /**
     * @hide
     */
    public boolean isShowPageNumberIndicator() {
        return showPageNumberIndicator;
    }

    /**
     * @hide
     */
    public boolean isShowBottomNavBar() {
        return showBottomNavBar;
    }

    /**
     * @hide
     */
    public boolean isShowThumbnailView() {
        return showThumbnailView;
    }

    /**
     * @hide
     */
    public boolean isShowBookmarksView() {
        return showBookmarksView;
    }

    /**
     * @hide
     */
    public String getToolbarTitle() {
        return toolbarTitle;
    }

    /**
     * @hide
     */
    public boolean isShowSearchView() {
        return showSearchView;
    }

    /**
     * @hide
     */
    public boolean isShowShareOption() {
        return showShareOption;
    }

    /**
     * @hide
     */
    public boolean isShowDocumentSettingsOption() {
        return showDocumentSettingsOption;
    }

    /**
     * @hide
     */
    public boolean isShowAnnotationToolbarOption() {
        return showAnnotationToolbarOption;
    }

    /**
     * @hide
     */
    public boolean isShowOpenFileOption() {
        return showOpenFileOption;
    }

    /**
     * @hide
     */
    public boolean isShowOpenUrlOption() {
        return showOpenUrlOption;
    }

    /**
     * @hide
     */
    public boolean isShowEditPagesOption() {
        return showEditPagesOption;
    }

    /**
     * @hide
     */
    public boolean isShowPrintOption() {
        return showPrintOption;
    }

    /**
     * @hide
     */
    public boolean isShowCloseTabOption() {
        return showCloseTabOption;
    }

    /**
     * @hide
     */
    public boolean isShowAnnotationsList() {
        return showAnnotationsList;
    }

    /**
     * @hide
     */
    public boolean isShowOutlineList() {
        return showOutlineList;
    }

    /**
     * @hide
     */
    public boolean isShowUserBookmarksList() {
        return showUserBookmarksList;
    }

    /**
     * @hide
     */
    public boolean isRightToLeftModeEnabled() {
        return rightToLeftModeEnabled;
    }

    /**
     * @hide
     */
    public boolean isShowRightToLeftOption() {
        return showRightToLeftOption;
    }

    /**
     * @hide
     */
    public PDFViewCtrlConfig getPdfViewCtrlConfig() {
        return pdfViewCtrlConfig;
    }

    /**
     * @hide
     */
    public int getToolManagerBuilderStyleRes() {
        return toolManagerBuilderStyleRes;
    }

    /**
     * @hide
     */
    public ToolManagerBuilder getToolManagerBuilder() {
        return toolManagerBuilder;
    }

    /**
     * @hide
     */
    public String getConversionOptions() {
        return conversionOptions;
    }

    /**
     * @hide
     */
    public String getOpenUrlCachePath() {
        return openUrlCachePath;
    }

    /**
     * @hide
     */
    public String getSaveCopyExportPath() {
        return saveCopyExportPath;
    }

    /**
     * @hide
     */
    public boolean isUseSupportActionBar() {
        return useSupportActionBar;
    }

    /**
     * @hide
     */
    public boolean isShowSaveCopyOption() {
        return showSaveCopyOption;
    }

    /**
     * @hide
     */
    public boolean isShowCropOption() {
        return showCropOption;
    }

    /**
     * @hide
     */
    public boolean isRestrictDownloadUsage() {
        return restrictDownloadUsage;
    }

    /**
     * @hide
     */
    public int getLayoutInDisplayCutoutMode() {
        return layoutInDisplayCutoutMode;
    }

    /** @hide */
    public boolean isThumbnailViewEditingEnabled() {
        return thumbnailViewEditingEnabled;
    }

    /** @hide */
    public boolean isUserBookmarksListEditingEnabled() {
        return userBookmarksListEditingEnabled;
    }

    /** @hide */
    public boolean annotationsListEditingEnabled() {
        return annotationsListEditingEnabled;
    }

    /**
     * @hide
     */
    public int getMaximumTabCount() {
        return maximumTabCount;
    }

    /**
     * @hide
     */
    public boolean isAutoHideToolbarEnabled() {
        return enableAutoHideToolbar;
    }

    private boolean fullscreenModeEnabled = true;
    private boolean multiTabEnabled = true;
    private boolean documentEditingEnabled = true;
    private boolean longPressQuickMenuEnabled = true;
    private boolean showPageNumberIndicator = true;
    private boolean showBottomNavBar = true;
    private boolean showThumbnailView = true;
    private boolean showBookmarksView = true;
    private String toolbarTitle;
    private boolean showSearchView = true;
    private boolean showShareOption = true;
    private boolean showDocumentSettingsOption = true;
    private boolean showAnnotationToolbarOption = true;
    private boolean showOpenFileOption = true;
    private boolean showOpenUrlOption = true;
    private boolean showEditPagesOption = true;
    private boolean showPrintOption = true;
    private boolean showCloseTabOption = true;
    private boolean showAnnotationsList = true;
    private boolean showOutlineList = true;
    private boolean showUserBookmarksList = true;
    private boolean rightToLeftModeEnabled = false;
    private boolean showRightToLeftOption = false;
    private PDFViewCtrlConfig pdfViewCtrlConfig;
    private int toolManagerBuilderStyleRes = 0;
    private ToolManagerBuilder toolManagerBuilder;
    private String conversionOptions;
    private String openUrlCachePath;
    private String saveCopyExportPath;
    private boolean useSupportActionBar = true;
    private boolean showSaveCopyOption = true;
    private boolean showCropOption = true;
    private boolean restrictDownloadUsage;
    private int layoutInDisplayCutoutMode = 0;
    private boolean thumbnailViewEditingEnabled = true;
    private boolean userBookmarksListEditingEnabled = true;
    private boolean annotationsListEditingEnabled = true;
    private int maximumTabCount = 0;
    private boolean enableAutoHideToolbar = true;

    public ViewerConfig() {
    }

    protected ViewerConfig(Parcel in) {
        fullscreenModeEnabled = in.readByte() != 0;
        multiTabEnabled = in.readByte() != 0;
        documentEditingEnabled = in.readByte() != 0;
        longPressQuickMenuEnabled = in.readByte() != 0;
        showPageNumberIndicator = in.readByte() != 0;
        showBottomNavBar = in.readByte() != 0;
        showThumbnailView = in.readByte() != 0;
        showBookmarksView = in.readByte() != 0;
        toolbarTitle = in.readString();
        showSearchView = in.readByte() != 0;
        showShareOption = in.readByte() != 0;
        showDocumentSettingsOption = in.readByte() != 0;
        showAnnotationToolbarOption = in.readByte() != 0;
        showOpenFileOption = in.readByte() != 0;
        showOpenUrlOption = in.readByte() != 0;
        showEditPagesOption = in.readByte() != 0;
        showPrintOption = in.readByte() != 0;
        showCloseTabOption = in.readByte() != 0;
        showAnnotationsList = in.readByte() != 0;
        showOutlineList = in.readByte() != 0;
        showUserBookmarksList = in.readByte() != 0;
        rightToLeftModeEnabled = in.readByte() != 0;
        showRightToLeftOption = in.readByte() != 0;
        pdfViewCtrlConfig = in.readParcelable(PDFViewCtrlConfig.class.getClassLoader());
        toolManagerBuilderStyleRes = in.readInt();
        toolManagerBuilder = in.readParcelable(ToolManagerBuilder.class.getClassLoader());
        conversionOptions = in.readString();
        openUrlCachePath = in.readString();
        saveCopyExportPath = in.readString();
        useSupportActionBar = in.readByte() != 0;
        showSaveCopyOption = in.readByte() != 0;
        restrictDownloadUsage = in.readByte() != 0;
        showCropOption = in.readByte() != 0;
        layoutInDisplayCutoutMode = in.readInt();
        thumbnailViewEditingEnabled = in.readByte() != 0;
        userBookmarksListEditingEnabled = in.readByte() != 0;
        annotationsListEditingEnabled = in.readByte() != 0;
        maximumTabCount = in.readInt();
        enableAutoHideToolbar = in.readByte() != 0;
    }

    public static final Creator<ViewerConfig> CREATOR = new Creator<ViewerConfig>() {
        @Override
        public ViewerConfig createFromParcel(Parcel in) {
            return new ViewerConfig(in);
        }

        @Override
        public ViewerConfig[] newArray(int size) {
            return new ViewerConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (fullscreenModeEnabled ? 1 : 0));
        parcel.writeByte((byte) (multiTabEnabled ? 1 : 0));
        parcel.writeByte((byte) (documentEditingEnabled ? 1 : 0));
        parcel.writeByte((byte) (longPressQuickMenuEnabled ? 1 : 0));
        parcel.writeByte((byte) (showPageNumberIndicator ? 1 : 0));
        parcel.writeByte((byte) (showBottomNavBar ? 1 : 0));
        parcel.writeByte((byte) (showThumbnailView ? 1 : 0));
        parcel.writeByte((byte) (showBookmarksView ? 1 : 0));
        parcel.writeString(toolbarTitle);
        parcel.writeByte((byte) (showSearchView ? 1 : 0));
        parcel.writeByte((byte) (showShareOption ? 1 : 0));
        parcel.writeByte((byte) (showDocumentSettingsOption ? 1 : 0));
        parcel.writeByte((byte) (showAnnotationToolbarOption ? 1 : 0));
        parcel.writeByte((byte) (showOpenFileOption ? 1 : 0));
        parcel.writeByte((byte) (showOpenUrlOption ? 1 : 0));
        parcel.writeByte((byte) (showEditPagesOption ? 1 : 0));
        parcel.writeByte((byte) (showPrintOption ? 1 : 0));
        parcel.writeByte((byte) (showCloseTabOption ? 1 : 0));
        parcel.writeByte((byte) (showAnnotationsList ? 1 : 0));
        parcel.writeByte((byte) (showOutlineList ? 1 : 0));
        parcel.writeByte((byte) (showUserBookmarksList ? 1 : 0));
        parcel.writeByte((byte) (rightToLeftModeEnabled ? 1 : 0));
        parcel.writeByte((byte) (showRightToLeftOption ? 1 : 0));
        parcel.writeParcelable(pdfViewCtrlConfig, i);
        parcel.writeInt(toolManagerBuilderStyleRes);
        parcel.writeParcelable(toolManagerBuilder, i);
        parcel.writeString(conversionOptions);
        parcel.writeString(openUrlCachePath);
        parcel.writeString(saveCopyExportPath);
        parcel.writeByte((byte) (useSupportActionBar ? 1 : 0));
        parcel.writeByte((byte) (showSaveCopyOption ? 1 : 0));
        parcel.writeByte((byte) (restrictDownloadUsage ? 1 : 0));
        parcel.writeByte((byte) (showCropOption ? 1 : 0));
        parcel.writeInt(layoutInDisplayCutoutMode);
        parcel.writeByte((byte) (thumbnailViewEditingEnabled ? 1 : 0));
        parcel.writeByte((byte) (userBookmarksListEditingEnabled ? 1 : 0));
        parcel.writeByte((byte) (annotationsListEditingEnabled ? 1 : 0));
        parcel.writeInt(maximumTabCount);
        parcel.writeByte((byte) (enableAutoHideToolbar ? 1 : 0));
    }

    /**
     * Builder class used to create an instance of {@link ViewerConfig}.
     */
    public static class Builder {
        private ViewerConfig mViewerConfig = new ViewerConfig();

        /**
         * Whether to enable full screen mode.
         */
        public Builder fullscreenModeEnabled(boolean fullscreenModeEnabled) {
            mViewerConfig.fullscreenModeEnabled = fullscreenModeEnabled;
            return this;
        }

        /**
         * Whether to enable multi-tab mode.
         */
        public Builder multiTabEnabled(boolean multiTab) {
            mViewerConfig.multiTabEnabled = multiTab;
            return this;
        }

        /**
         * Whether to enable document editing.
         */
        public Builder documentEditingEnabled(boolean documentEditingEnabled) {
            mViewerConfig.documentEditingEnabled = documentEditingEnabled;
            return this;
        }

        /**
         * Whether to enable long press quick menu.
         */
        public Builder longPressQuickMenuEnabled(boolean longPressQuickMenuEnabled) {
            mViewerConfig.longPressQuickMenuEnabled = longPressQuickMenuEnabled;
            return this;
        }

        /**
         * Whether to show page number indicator overlay.
         */
        public Builder showPageNumberIndicator(boolean showPageNumberIndicator) {
            mViewerConfig.showPageNumberIndicator = showPageNumberIndicator;
            return this;
        }

        /**
         * Whether to show bottom navigation bar.
         */
        public Builder showBottomNavBar(boolean showBottomNavBar) {
            mViewerConfig.showBottomNavBar = showBottomNavBar;
            return this;
        }

        /**
         * If {@link ViewerConfig#showBottomNavBar} returns false,
         * then this value is ignored.
         * Whether to show thumbnail view icon.
         */
        public Builder showThumbnailView(boolean showThumbnailView) {
            mViewerConfig.showThumbnailView = showThumbnailView;
            return this;
        }

        /**
         * If {@link ViewerConfig#showBottomNavBar} returns false,
         * then this value is ignored.
         * If all of {@link ViewerConfig#showAnnotationsList},
         * {@link ViewerConfig#showOutlineList}, and
         * {@link ViewerConfig#showUserBookmarksList} return false,
         * then this value is ignored.
         * Whether to show bookmarks view icon.
         */
        public Builder showBookmarksView(boolean showBookmarksView) {
            mViewerConfig.showBookmarksView = showBookmarksView;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Toolbar title.
         */
        public Builder toolbarTitle(String toolbarTitle) {
            mViewerConfig.toolbarTitle = toolbarTitle;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show search view icon.
         */
        public Builder showSearchView(boolean showSearchView) {
            mViewerConfig.showSearchView = showSearchView;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show share icon.
         */
        public Builder showShareOption(boolean showShareOption) {
            mViewerConfig.showShareOption = showShareOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show bookmarks view icon.
         */
        public Builder showDocumentSettingsOption(boolean showDocumentSettingsOption) {
            mViewerConfig.showDocumentSettingsOption = showDocumentSettingsOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show annotation toolbar view icon.
         */
        public Builder showAnnotationToolbarOption(boolean showAnnotationToolbarOption) {
            mViewerConfig.showAnnotationToolbarOption = showAnnotationToolbarOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show open file option.
         */
        public Builder showOpenFileOption(boolean showOpenFileOption) {
            mViewerConfig.showOpenFileOption = showOpenFileOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show open url option.
         */
        public Builder showOpenUrlOption(boolean showOpenUrlOption) {
            mViewerConfig.showOpenUrlOption = showOpenUrlOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show edit pages option.
         */
        public Builder showEditPagesOption(boolean showEditPagesOption) {
            mViewerConfig.showEditPagesOption = showEditPagesOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show print option.
         */
        public Builder showPrintOption(boolean showPrintOption) {
            mViewerConfig.showPrintOption = showPrintOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show save a copy option.
         */
        public Builder showSaveCopyOption(boolean showSaveCopyOption) {
            mViewerConfig.showSaveCopyOption = showSaveCopyOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * <p>
         * Set showCropOption to true to show the cropping
         * option in {@link com.pdftron.pdf.dialog.ViewModePickerDialogFragment}
         */
        public Builder showCropOption(boolean showCropOption) {
            mViewerConfig.showCropOption = showCropOption;
            return this;
        }

        /**
         * If Activity or Fragment supply its own Toolbar,
         * then this value is ignored.
         * Whether to show close document option in the overflow menu.
         */
        public Builder showCloseTabOption(boolean showCloseTabOption) {
            mViewerConfig.showCloseTabOption = showCloseTabOption;
            return this;
        }

        /**
         * If {@link ViewerConfig#showBookmarksView} returns false,
         * then this value is ignored.
         * Whether to show annotation list.
         */
        public Builder showAnnotationsList(boolean showAnnotationsList) {
            mViewerConfig.showAnnotationsList = showAnnotationsList;
            return this;
        }

        /**
         * If {@link ViewerConfig#showBookmarksView} returns false,
         * then this value is ignored.
         * Whether to show outline list.
         */
        public Builder showOutlineList(boolean showOutlineList) {
            mViewerConfig.showOutlineList = showOutlineList;
            return this;
        }

        /**
         * If {@link ViewerConfig#showBookmarksView} returns false,
         * then this value is ignored.
         * Whether to show user bookmarks list.
         */
        public Builder showUserBookmarksList(boolean showUserBookmarksList) {
            mViewerConfig.showUserBookmarksList = showUserBookmarksList;
            return this;
        }

        /**
         * Whether to view documents from right to left.
         * If {@link ViewerConfig#showRightToLeftOption} return false,
         * then this value is ignored.
         */
        public Builder rightToLeftModeEnabled(boolean rightToLeftModeEnabled) {
            mViewerConfig.rightToLeftModeEnabled = rightToLeftModeEnabled;
            return this;
        }

        /**
         * Whether to enable RTL option in the view mode dialog.
         */
        public Builder showRightToLeftOption(boolean showRightToLeftOption) {
            mViewerConfig.showRightToLeftOption = showRightToLeftOption;
            return this;
        }

        /**
         * Sets the {@link PDFViewCtrlConfig} for {@link com.pdftron.pdf.PDFViewCtrl}
         */
        public Builder pdfViewCtrlConfig(PDFViewCtrlConfig config) {
            mViewerConfig.pdfViewCtrlConfig = config;
            return this;
        }

        /**
         * Sets the style resource ID used for {@link ToolManagerBuilder}
         */
        public Builder toolManagerBuilderStyleRes(@StyleRes int styleRes) {
            mViewerConfig.toolManagerBuilderStyleRes = styleRes;
            return this;
        }

        /**
         * @deprecated replaced by {@link #toolManagerBuilder} instead
         * Sets tool manager builder for building tool manager
         */
        @Deprecated
        public Builder setToolManagerBuilder(ToolManagerBuilder toolManagerBuilder) {
            mViewerConfig.toolManagerBuilder = toolManagerBuilder;
            return this;
        }

        /**
         * Sets tool manager builder for building tool manager
         */
        public Builder toolManagerBuilder(ToolManagerBuilder toolManagerBuilder) {
            mViewerConfig.toolManagerBuilder = toolManagerBuilder;
            return this;
        }

        /**
         * Sets {@link com.pdftron.pdf.ConversionOptions} for non-pdf conversion
         */
        public Builder conversionOptions(String conversionOptions) {
            mViewerConfig.conversionOptions = conversionOptions;
            return this;
        }

        /**
         * Sets the cache folder path for open URL files
         */
        public Builder openUrlCachePath(String openUrlCachePath) {
            mViewerConfig.openUrlCachePath = openUrlCachePath;
            return this;
        }

        /**
         * Sets the folder path for all save a copy options
         */
        public Builder saveCopyExportPath(String exportPath) {
            mViewerConfig.saveCopyExportPath = exportPath;
            return this;
        }

        /**
         * Sets whether to use SupportActionBar for inflating ToolBar menu
         */
        public Builder useSupportActionBar(boolean useSupportActionBar) {
            mViewerConfig.useSupportActionBar = useSupportActionBar;
            return this;
        }

        /**
         * Sets whether to restrict data used when viewing an online PDF
         */
        public Builder restrictDownloadUsage(boolean restrictDownloadUsage) {
            mViewerConfig.restrictDownloadUsage = restrictDownloadUsage;
            return this;
        }

        /**
         * Sets the display cutout mode. Only available when in full screen mode.
         */
        @RequiresApi(api = Build.VERSION_CODES.P)
        public Builder layoutInDisplayCutoutMode(int cutoutMode) {
            mViewerConfig.layoutInDisplayCutoutMode = cutoutMode;
            return this;
        }

        /**
         * Sets whether the {@link com.pdftron.pdf.controls.ThumbnailsViewFragment} can modify the document
         * If {@link #documentEditingEnabled} is false, this value is ignored
         */
        public Builder thumbnailViewEditingEnabled(boolean thumbnailViewEditingEnabled) {
            mViewerConfig.thumbnailViewEditingEnabled = thumbnailViewEditingEnabled;
            return this;
        }

        /**
         * Sets whether the {@link com.pdftron.pdf.controls.UserBookmarkDialogFragment} can modify the document
         * If {@link #documentEditingEnabled} is false, this value is ignored
         */
        public Builder userBookmarksListEditingEnabled(boolean userBookmarksListEditingEnabled) {
            mViewerConfig.userBookmarksListEditingEnabled = userBookmarksListEditingEnabled;
            return this;
        }

        /**
         * Sets whether the {@link com.pdftron.pdf.controls.AnnotationDialogFragment} can modify the document
         * If {@link #documentEditingEnabled} is false, this value is ignored
         */
        public Builder annotationsListEditingEnabled(boolean annotationsListEditingEnabled) {
            mViewerConfig.annotationsListEditingEnabled = annotationsListEditingEnabled;
            return this;
        }

        /**
         * Sets the maximum number of tabs allowed. By default, the maximum count is 3 on phone and 5 on tablet.
         * Adding subsequent tabs will remove other tabs to respect the limit.
         */
        public Builder maximumTabCount(int count) {
            mViewerConfig.maximumTabCount = count;
            return this;
        }

        /**
         * Sets whether the options toolbar should automatically hide when the user is interacting
         * with the document viewer. By default, the toolbar will automatically hide.
         */
        public Builder autoHideToolbarEnabled(boolean shouldAutoHide) {
            mViewerConfig.enableAutoHideToolbar = shouldAutoHide;
            return this;
        }

        public ViewerConfig build() {
            return mViewerConfig;
        }
    }

    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewerConfig that = (ViewerConfig) o;

        if (fullscreenModeEnabled != that.fullscreenModeEnabled) return false;
        if (multiTabEnabled != that.multiTabEnabled) return false;
        if (documentEditingEnabled != that.documentEditingEnabled) return false;
        if (longPressQuickMenuEnabled != that.longPressQuickMenuEnabled) return false;
        if (showPageNumberIndicator != that.showPageNumberIndicator) return false;
        if (showBottomNavBar != that.showBottomNavBar) return false;
        if (showThumbnailView != that.showThumbnailView) return false;
        if (showBookmarksView != that.showBookmarksView) return false;
        if (showSearchView != that.showSearchView) return false;
        if (showShareOption != that.showShareOption) return false;
        if (showDocumentSettingsOption != that.showDocumentSettingsOption) return false;
        if (showAnnotationToolbarOption != that.showAnnotationToolbarOption) return false;
        if (showOpenFileOption != that.showOpenFileOption) return false;
        if (showOpenUrlOption != that.showOpenUrlOption) return false;
        if (showEditPagesOption != that.showEditPagesOption) return false;
        if (showPrintOption != that.showPrintOption) return false;
        if (showCloseTabOption != that.showCloseTabOption) return false;
        if (showAnnotationsList != that.showAnnotationsList) return false;
        if (showOutlineList != that.showOutlineList) return false;
        if (showUserBookmarksList != that.showUserBookmarksList) return false;
        if (rightToLeftModeEnabled != that.rightToLeftModeEnabled) return false;
        if (showRightToLeftOption != that.showRightToLeftOption) return false;
        if (toolManagerBuilderStyleRes != that.toolManagerBuilderStyleRes) return false;
        if (useSupportActionBar != that.useSupportActionBar) return false;
        if (showSaveCopyOption != that.showSaveCopyOption) return false;
        if (showCropOption != that.showCropOption) return false;
        if (restrictDownloadUsage != that.restrictDownloadUsage) return false;
        if (layoutInDisplayCutoutMode != that.layoutInDisplayCutoutMode) return false;
        if (thumbnailViewEditingEnabled != that.thumbnailViewEditingEnabled) return false;
        if (userBookmarksListEditingEnabled != that.userBookmarksListEditingEnabled) return false;
        if (annotationsListEditingEnabled != that.annotationsListEditingEnabled) return false;
        if (maximumTabCount != that.maximumTabCount) return false;
        if (enableAutoHideToolbar != that.enableAutoHideToolbar) return false;
        if (toolbarTitle != null ? !toolbarTitle.equals(that.toolbarTitle) : that.toolbarTitle != null)
            return false;
        if (pdfViewCtrlConfig != null ? !pdfViewCtrlConfig.equals(that.pdfViewCtrlConfig) : that.pdfViewCtrlConfig != null)
            return false;
        if (toolManagerBuilder != null ? !toolManagerBuilder.equals(that.toolManagerBuilder) : that.toolManagerBuilder != null)
            return false;
        if (conversionOptions != null ? !conversionOptions.equals(that.conversionOptions) : that.conversionOptions != null)
            return false;
        if (openUrlCachePath != null ? !openUrlCachePath.equals(that.openUrlCachePath) : that.openUrlCachePath != null)
            return false;
        return saveCopyExportPath != null ? saveCopyExportPath.equals(that.saveCopyExportPath) : that.saveCopyExportPath == null;
    }

    @Override
    public int hashCode() {
        int result = (fullscreenModeEnabled ? 1 : 0);
        result = 31 * result + (multiTabEnabled ? 1 : 0);
        result = 31 * result + (documentEditingEnabled ? 1 : 0);
        result = 31 * result + (longPressQuickMenuEnabled ? 1 : 0);
        result = 31 * result + (showPageNumberIndicator ? 1 : 0);
        result = 31 * result + (showBottomNavBar ? 1 : 0);
        result = 31 * result + (showThumbnailView ? 1 : 0);
        result = 31 * result + (showBookmarksView ? 1 : 0);
        result = 31 * result + (toolbarTitle != null ? toolbarTitle.hashCode() : 0);
        result = 31 * result + (showSearchView ? 1 : 0);
        result = 31 * result + (showShareOption ? 1 : 0);
        result = 31 * result + (showDocumentSettingsOption ? 1 : 0);
        result = 31 * result + (showAnnotationToolbarOption ? 1 : 0);
        result = 31 * result + (showOpenFileOption ? 1 : 0);
        result = 31 * result + (showOpenUrlOption ? 1 : 0);
        result = 31 * result + (showEditPagesOption ? 1 : 0);
        result = 31 * result + (showPrintOption ? 1 : 0);
        result = 31 * result + (showCloseTabOption ? 1 : 0);
        result = 31 * result + (showAnnotationsList ? 1 : 0);
        result = 31 * result + (showOutlineList ? 1 : 0);
        result = 31 * result + (showUserBookmarksList ? 1 : 0);
        result = 31 * result + (rightToLeftModeEnabled ? 1 : 0);
        result = 31 * result + (showRightToLeftOption ? 1 : 0);
        result = 31 * result + (pdfViewCtrlConfig != null ? pdfViewCtrlConfig.hashCode() : 0);
        result = 31 * result + toolManagerBuilderStyleRes;
        result = 31 * result + (toolManagerBuilder != null ? toolManagerBuilder.hashCode() : 0);
        result = 31 * result + (conversionOptions != null ? conversionOptions.hashCode() : 0);
        result = 31 * result + (openUrlCachePath != null ? openUrlCachePath.hashCode() : 0);
        result = 31 * result + (saveCopyExportPath != null ? saveCopyExportPath.hashCode() : 0);
        result = 31 * result + (useSupportActionBar ? 1 : 0);
        result = 31 * result + (showSaveCopyOption ? 1 : 0);
        result = 31 * result + (showCropOption ? 1 : 0);
        result = 31 * result + (restrictDownloadUsage ? 1 : 0);
        result = 31 * result + layoutInDisplayCutoutMode;
        result = 31 * result + (thumbnailViewEditingEnabled ? 1 : 0);
        result = 31 * result + (userBookmarksListEditingEnabled ? 1 : 0);
        result = 31 * result + (annotationsListEditingEnabled ? 1 : 0);
        result = 31 * result + maximumTabCount;
        result = 31 * result + (enableAutoHideToolbar ? 1 : 0);
        return result;
    }
}
