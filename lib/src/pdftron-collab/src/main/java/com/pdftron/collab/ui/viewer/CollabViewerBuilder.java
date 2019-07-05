package com.pdftron.collab.ui.viewer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.R;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.interfaces.builder.SkeletalFragmentBuilder;

/**
 * Builder to create a {@link CollabViewerTabHostFragment}.
 */
public class CollabViewerBuilder extends SkeletalFragmentBuilder<CollabViewerTabHostFragment> {

    private Uri mFile;                  // required for builder
    private String mPassword;           // required for builder
    private ViewerConfig mConfig;       // default value is from getDefaultConfigBuilder()
    private Class<? extends CollabViewerTabFragment> mTabFragmentClass; // default is CollabPdfViewCtrlTabFragment

    private CollabViewerBuilder() {
        super();
    }

    /**
     * Create a {@link CollabViewerBuilder} with the specified document and password if applicable.
     *
     * @param file     Uri that specifies the location of the document
     * @param password used to open the document if required, null otherwise
     * @return builder with the specified document and password
     * @see #withUri(Uri) for variant without a password paramter
     */
    public static CollabViewerBuilder withUri(@NonNull Uri file, @Nullable String password) {
        CollabViewerBuilder builder = new CollabViewerBuilder();
        builder.mFile = file;
        builder.mPassword = password;
        return builder;
    }

    /**
     * @see #withUri(Uri, String)
     */
    public static CollabViewerBuilder withUri(@NonNull Uri file) {
        return withUri(file, null);
    }

    /**
     * Defines the fragment class that will be used to instantiate viewer tabs.
     * Currently, the viewer only support a single tab fragment (multi-tabs are currently
     * unsupported).
     *
     * @param tabClass the class that the viewer will used to instantiate tabs
     * @return this builder with the specified configurations
     */
    public CollabViewerBuilder usingTabClass(@NonNull Class<? extends CollabViewerTabFragment> tabClass) {
        mTabFragmentClass = tabClass;
        return this;
    }

    /**
     * Used to initialize the reply fragment with a specified {@link ViewerConfig}. Multi-tab
     * is unsupported for the collab documentation viewer and must be disabled in ViewerConfig.
     *
     * @param config to initialize the document viewer
     * @return this builder with the specified configurations
     */
    public CollabViewerBuilder usingConfig(@NonNull ViewerConfig config) {
        mConfig = config;
        return this;
    }

    /**
     * Create a the default {@link CollabViewerTabHostFragment}, initialized with builder settings.
     * Uses the theme from the specified context.
     *
     * @param context the context used to initialize the fragment and its theme
     * @return a {@link CollabViewerTabHostFragment} with specified the parameters from the builder.
     */
    @Override
    public CollabViewerTabHostFragment build(@NonNull Context context) {
        return build(context, CollabViewerTabHostFragment.class);
    }

    @Override
    public Bundle createBundle(@NonNull Context context) {
        Bundle args = CollabViewerTabFragment.createBasicPdfViewCtrlTabBundle(context, mFile, null, mConfig);
        args.putInt(CollabViewerTabHostFragment.BUNDLE_TAB_HOST_NAV_ICON, R.drawable.ic_arrow_back_white_24dp);
        args.putSerializable(CollabViewerTabHostFragment.BUNDLE_TAB_FRAGMENT_CLASS, mTabFragmentClass);
        args.putParcelable(CollabViewerTabHostFragment.BUNDLE_TAB_HOST_CONFIG, mConfig);
        return args;
    }

    /**
     * Helpers to check builder parameters
     */

    @Override
    public void checkArgs(@NonNull Context context) {
        checkRequiredParams();
        checkTabFragmentClass();
        checkAndSetDefaultConfig(context);
    }

    private void checkTabFragmentClass() {

        if (mTabFragmentClass == null) {
            mTabFragmentClass = CollabViewerTabFragment.class; // default tab fragment class
        }
    }

    private void checkRequiredParams() {
        if (mFile == null) {
            throw new IllegalStateException("Must specify a valid document");
        }
    }

    private void checkAndSetDefaultConfig(@NonNull Context context) {
        if (mConfig == null) {
            // Create default viewer config
            mConfig = getDefaultConfigBuilder(context)
                    .build();
        } else {
            if (mConfig.isMultiTabEnabled()) {
                throw new IllegalStateException("Multi tab option must be disabled in ViewerConfig, for the collab viewer.");
            }
        }
    }

    private static ViewerConfig.Builder getDefaultConfigBuilder(@NonNull Context context) {
        return new ViewerConfig.Builder()
                .multiTabEnabled(false)     // multi-tabs unsupported for collab fragment
                .showCloseTabOption(false)  // multi-tabs unsupported for collab fragment
                .saveCopyExportPath(context.getFilesDir().getAbsolutePath())
                .openUrlCachePath(context.getFilesDir().getAbsolutePath());
    }

    // Parcelable methods

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mConfig, flags);
        dest.writeParcelable(this.mFile, flags);
        dest.writeString(this.mPassword);
        dest.writeSerializable(this.mTabFragmentClass);
    }

    /**
     * {@hide}
     */
    @SuppressWarnings({"WeakerAccess", "unchecked"})
    protected CollabViewerBuilder(Parcel in) {
        this.mConfig = in.readParcelable(ViewerConfig.class.getClassLoader());
        this.mFile = in.readParcelable(Uri.class.getClassLoader());
        this.mPassword = in.readString();
        this.mTabFragmentClass = (Class<? extends CollabViewerTabFragment>) in.readSerializable();
    }

    public static final Creator<CollabViewerBuilder> CREATOR = new Creator<CollabViewerBuilder>() {
        @Override
        public CollabViewerBuilder createFromParcel(Parcel source) {
            return new CollabViewerBuilder(source);
        }

        @Override
        public CollabViewerBuilder[] newArray(int size) {
            return new CollabViewerBuilder[size];
        }
    };
}
