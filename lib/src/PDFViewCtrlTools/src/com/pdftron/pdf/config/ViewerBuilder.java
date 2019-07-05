package com.pdftron.pdf.config;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.pdftron.pdf.controls.PdfViewCtrlTabFragment;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;
import com.pdftron.pdf.interfaces.builder.SkeletalFragmentBuilder;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.cache.UriCacheManager;

import java.io.File;
import java.util.Arrays;

/**
 * Builder to create a {@link PdfViewCtrlTabHostFragment}.
 */
public class ViewerBuilder extends SkeletalFragmentBuilder<PdfViewCtrlTabHostFragment> {

    @Nullable
    private String mTabTitle;                   // optional for builder, specified title to use
    @Nullable
    private Uri mFile;                          // optional for builder, will show an empty viewer
    @Nullable
    private String mPassword;                   // optional for builder
    private boolean mUseCacheFolder = true;     // default should use cache directory
    private boolean mUseQuitAppMode = false;       // default should not close viewer when done viewing
    @Nullable
    private ViewerConfig mConfig;               // default value is null
    @DrawableRes
    private int mNavigationIcon = R.drawable.ic_menu_white_24dp;   // default uses a menu list icon
    private int[] mCustomToolbarMenu = null;
    private int mFileType = BaseFileInfo.FILE_TYPE_UNKNOWN;

    @NonNull
    private Class<? extends PdfViewCtrlTabFragment> mTabFragmentClass = PdfViewCtrlTabFragment.class; // default is PdfViewCtrlTabFragment

    private ViewerBuilder() {
        super();
    }

    /**
     * Create a {@link ViewerBuilder} with the specified document and password if applicable.
     *
     * @param file     Uri that specifies the location of the document
     * @param password used to open the document if needed, null otherwise
     * @return builder with the specified document and password
     * @see #withUri(Uri) for variant without a password paramter
     */
    public static ViewerBuilder withUri(@Nullable Uri file, @Nullable String password) {
        ViewerBuilder builder = new ViewerBuilder();
        builder.mFile = file;
        builder.mPassword = password;
        return builder;
    }

    /**
     * @see #withUri(Uri, String)
     */
    public static ViewerBuilder withUri(@Nullable Uri file) {
        return withUri(file, null);
    }

    /**
     * Similar to {@link #withUri(Uri, String)), but with a specified File object.
     */
    public static ViewerBuilder withFile(@Nullable File file, @Nullable String password) {
        return withUri(file != null ? Uri.fromFile(file) : null, password);
    }

    /**
     * Similar to {@link #withFile(File, String)), but without a specified password.
     */
    public static ViewerBuilder withFile(@Nullable File file) {
        return withUri(file != null ? Uri.fromFile(file) : null, null);
    }

    /**
     * Call to define the fragment class that will be used to instantiate viewer tabs.
     *
     * @param tabClass the class that the viewer will used to instantiate tabs
     * @return this builder with the specified tab fragment class
     */
    public ViewerBuilder usingTabClass(@NonNull Class<? extends PdfViewCtrlTabFragment> tabClass) {
        mTabFragmentClass = tabClass;
        return this;
    }

    /**
     * Call to define the navigation icon used by this fragment. By default, a menu list icon is used for
     * the navigation button.
     *
     * @param navIconRes the class that the viewer will used to instantiate tabs
     * @return this builder with the specified navigation icon
     */
    public ViewerBuilder usingNavIcon(@DrawableRes int navIconRes) {
        mNavigationIcon = navIconRes;
        return this;
    }

    /**
     * Call to initialize the document viewer with a specified {@link ViewerConfig}. Multi-tab
     * is unsupported for the collab documentation viewer and must be disabled in ViewerConfig.
     *
     * @param config to initialize the document viewer
     * @return this builder with the specified {@link ViewerConfig} configurations
     */
    public ViewerBuilder usingConfig(@NonNull ViewerConfig config) {
        mConfig = config;
        return this;
    }

    /**
     * Call to enable or disable the use of the cache folder when creating temporary files. By default
     * the cache folder is used, and if set to false the Downloads folder is used.
     *
     * @param useCacheFolder true to enable using the cache folder, false to use the downloads folder
     * @return this builder with the specified use of the cache folder
     */
    public ViewerBuilder usingCacheFolder(boolean useCacheFolder) {
        mUseCacheFolder = useCacheFolder;
        return this;
    }

    /**
     * Call to define how the file will be handled by the document viewer. By default, this is
     * unspecified (value of 0) and the document viewer will automatically handle this; this
     * is usually called to fulfill certain requirements and will not be needed in most
     * cases.
     * <p>
     * The file types are  defined in {@link BaseFileInfo}.
     *
     * @param fileType specified to handle the file in a specific way.
     * @return this builder with the specified file type handling
     */
    public ViewerBuilder usingFileType(int fileType) {
        mFileType = fileType;
        return this;
    }

    /**
     * Call to set the tab title in the document viewer with the specified String. If null is specified,
     * then the default title handling in the document viewer will be used.
     *
     * @param title title used for the tab when viewing the specified document
     * @return this builder with the specified tab title
     */
    public ViewerBuilder usingTabTitle(@Nullable String title) {
        mTabTitle = title;
        return this;
    }

    /**
     * Define the custom menu resources to use in document viewer toolbar.
     *
     * @param menu custom toolbar menu XML resources to use in the document viewer
     * @return this builder with the specified custom toolbar menu
     */
    public ViewerBuilder usingCustomToolbar(@MenuRes int[] menu) {
        mCustomToolbarMenu = menu;
        return this;
    }

    /**
     * Set true to enable {@link PdfViewCtrlTabHostFragment#BUNDLE_TAB_HOST_QUIT_APP_WHEN_DONE_VIEWING}
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public ViewerBuilder usingQuitAppMode(boolean useQuitAppMode) {
        mUseQuitAppMode = useQuitAppMode;
        return this;
    }

    @Override
    public PdfViewCtrlTabHostFragment build(@NonNull Context context) {
        return build(context, PdfViewCtrlTabHostFragment.class);
    }

    @Override
    public Bundle createBundle(@NonNull Context context) {
        Bundle args;
        if (mFile == null) {
            args = new Bundle();
        } else {
            args = PdfViewCtrlTabFragment.createBasicPdfViewCtrlTabBundle(context, mFile, mPassword, mConfig);
            if (mFileType != BaseFileInfo.FILE_TYPE_UNKNOWN) {
                args.putInt(PdfViewCtrlTabFragment.BUNDLE_TAB_ITEM_SOURCE, mFileType);
            }
        }
        if (mTabTitle != null) {
            args.putString(PdfViewCtrlTabFragment.BUNDLE_TAB_TITLE, mTabTitle);
        }
        args.putSerializable(PdfViewCtrlTabHostFragment.BUNDLE_TAB_FRAGMENT_CLASS, mTabFragmentClass);
        args.putParcelable(PdfViewCtrlTabHostFragment.BUNDLE_TAB_HOST_CONFIG, mConfig);
        args.putInt(PdfViewCtrlTabHostFragment.BUNDLE_TAB_HOST_NAV_ICON, mNavigationIcon);
        args.putBoolean(UriCacheManager.BUNDLE_USE_CACHE_FOLDER, mUseCacheFolder);
        args.putIntArray(PdfViewCtrlTabHostFragment.BUNDLE_TAB_HOST_TOOLBAR_MENU, mCustomToolbarMenu);
        args.putBoolean(PdfViewCtrlTabHostFragment.BUNDLE_TAB_HOST_QUIT_APP_WHEN_DONE_VIEWING, mUseQuitAppMode);

        return args;
    }

    @Override
    public void checkArgs(@NonNull Context context) {
        checkTabFragmentClass();
    }

    @SuppressWarnings("ConstantConditions")
    private void checkTabFragmentClass() {
        if (mTabFragmentClass == null) { // Need to check just in case a user sets to null somehow
            mTabFragmentClass = PdfViewCtrlTabFragment.class; // default tab fragment class
        }
    }

    // Parcelable methods

    /**
     * {@hide}
     */
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    protected ViewerBuilder(Parcel in) {
        this.mTabTitle = in.readString();
        this.mFile = in.readParcelable(Uri.class.getClassLoader());
        this.mPassword = in.readString();
        this.mUseCacheFolder = in.readByte() != 0;
        this.mUseQuitAppMode = in.readByte() != 0;
        this.mConfig = in.readParcelable(ViewerConfig.class.getClassLoader());
        this.mNavigationIcon = in.readInt();
        this.mCustomToolbarMenu = in.createIntArray();
        this.mFileType = in.readInt();
        this.mTabFragmentClass = (Class<? extends PdfViewCtrlTabFragment>) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTabTitle);
        dest.writeParcelable(this.mFile, flags);
        dest.writeString(this.mPassword);
        dest.writeByte(this.mUseCacheFolder ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mUseQuitAppMode ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.mConfig, flags);
        dest.writeInt(this.mNavigationIcon);
        dest.writeIntArray(this.mCustomToolbarMenu);
        dest.writeInt(this.mFileType);
        dest.writeSerializable(this.mTabFragmentClass);
    }

    public static final Creator<ViewerBuilder> CREATOR = new Creator<ViewerBuilder>() {
        @Override
        public ViewerBuilder createFromParcel(Parcel source) {
            return new ViewerBuilder(source);
        }

        @Override
        public ViewerBuilder[] newArray(int size) {
            return new ViewerBuilder[size];
        }
    };


    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewerBuilder that = (ViewerBuilder) o;

        if (mUseCacheFolder != that.mUseCacheFolder) return false;
        if (mUseQuitAppMode != that.mUseQuitAppMode) return false;
        if (mNavigationIcon != that.mNavigationIcon) return false;
        if (mFileType != that.mFileType) return false;
        if (mTabTitle != null ? !mTabTitle.equals(that.mTabTitle) : that.mTabTitle != null)
            return false;
        if (mFile != null ? !mFile.equals(that.mFile) : that.mFile != null) return false;
        if (mPassword != null ? !mPassword.equals(that.mPassword) : that.mPassword != null)
            return false;
        if (mConfig != null ? !mConfig.equals(that.mConfig) : that.mConfig != null) return false;
        if (!Arrays.equals(mCustomToolbarMenu, that.mCustomToolbarMenu)) return false;
        return mTabFragmentClass.equals(that.mTabFragmentClass);
    }

    @Override
    public int hashCode() {
        int result = mTabTitle != null ? mTabTitle.hashCode() : 0;
        result = 31 * result + (mFile != null ? mFile.hashCode() : 0);
        result = 31 * result + (mPassword != null ? mPassword.hashCode() : 0);
        result = 31 * result + (mUseCacheFolder ? 1 : 0);
        result = 31 * result + (mUseQuitAppMode ? 1 : 0);
        result = 31 * result + (mConfig != null ? mConfig.hashCode() : 0);
        result = 31 * result + mNavigationIcon;
        result = 31 * result + Arrays.hashCode(mCustomToolbarMenu);
        result = 31 * result + mFileType;
        result = 31 * result + mTabFragmentClass.hashCode();
        return result;
    }
}
