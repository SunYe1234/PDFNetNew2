package com.pdftron.pdf.controls;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.pdftron.pdf.config.ViewerBuilder;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.Utils;

/** @hide */
public class DocumentView extends FrameLayout implements
    PdfViewCtrlTabHostFragment.TabHostListener {

    private static final String TAG = DocumentView.class.getSimpleName();

    public PdfViewCtrlTabHostFragment mPdfViewCtrlTabHostFragment;
    public FragmentManager mFragmentManager;

    public int mNavIconRes = R.drawable.ic_arrow_back_white_24dp;
    public boolean mShowNavIcon = true;
    public Uri mDocumentUri;
    public String mPassword = "";
    public ViewerConfig mViewerConfig;
    public PdfViewCtrlTabHostFragment.TabHostListener mTabHostListener;

    public DocumentView(@NonNull Context context) {
        super(context);
    }

    public DocumentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DocumentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDocumentUri(Uri documentUri) {
        mDocumentUri = documentUri;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setViewerConfig(ViewerConfig config) {
        mViewerConfig = config;
    }

    public void setSupportFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void setNavIconResName(String resName) {
        if (resName == null) {
            return;
        }
        int res = Utils.getResourceDrawable(getContext(), resName);
        if (res != 0) {
            mNavIconRes = res;
        }
    }

    public void setShowNavIcon(boolean showNavIcon) {
        mShowNavIcon = showNavIcon;
    }

    public void setTabHostListener(PdfViewCtrlTabHostFragment.TabHostListener listener) {
        mTabHostListener = listener;
    }

    private void prepView() {
        if (mDocumentUri == null) {
            return;
        }

        // Create a viewer builder with the specified parameters
        ViewerBuilder builder = ViewerBuilder.withUri(mDocumentUri, mPassword)
                .usingConfig(mViewerConfig)
                .usingNavIcon(mShowNavIcon ? mNavIconRes : 0);

        if (mPdfViewCtrlTabHostFragment != null) {
            mPdfViewCtrlTabHostFragment.onOpenAddNewTab(builder.createBundle(getContext()));
        } else {
            mPdfViewCtrlTabHostFragment = builder.build(getContext());

            if (mFragmentManager != null) {
                mFragmentManager.beginTransaction()
                        .add(mPdfViewCtrlTabHostFragment, TAG)
                        .commitNow();

                View fragmentView = mPdfViewCtrlTabHostFragment.getView();
                if (fragmentView != null) {
                    addView(fragmentView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                }
            }
        }
    }

    private void cleanup() {
        if (mFragmentManager != null) {
            PdfViewCtrlTabHostFragment fragment = (PdfViewCtrlTabHostFragment) mFragmentManager.findFragmentByTag(TAG);
            if (fragment != null) {
                mFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
            }
        }
        mPdfViewCtrlTabHostFragment = null;
        mFragmentManager = null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        prepView();

        if (mPdfViewCtrlTabHostFragment != null) {
            mPdfViewCtrlTabHostFragment.addHostListener(this);
            if (mTabHostListener != null) {
                mPdfViewCtrlTabHostFragment.addHostListener(mTabHostListener);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mPdfViewCtrlTabHostFragment != null) {
            mPdfViewCtrlTabHostFragment.removeHostListener(this);
            if (mTabHostListener != null) {
                mPdfViewCtrlTabHostFragment.removeHostListener(mTabHostListener);
                mTabHostListener = null;
            }
        }

        cleanup();
    }

    @Override
    public void onTabHostShown() {

    }

    @Override
    public void onTabHostHidden() {

    }

    @Override
    public void onLastTabClosed() {

    }

    @Override
    public void onTabChanged(String tag) {

    }

    @Override
    public void onOpenDocError() {

    }

    @Override
    public void onNavButtonPressed() {

    }

    @Override
    public void onShowFileInFolder(String fileName, String filepath, int itemSource) {

    }

    @Override
    public boolean canShowFileInFolder() {
        return false;
    }

    @Override
    public boolean canShowFileCloseSnackbar() {
        return true;
    }

    @Override
    public boolean onToolbarCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        return false;
    }

    @Override
    public boolean onToolbarPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onToolbarOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onStartSearchMode() {

    }

    @Override
    public void onExitSearchMode() {

    }

    @Override
    public boolean canRecreateActivity() {
        return false;
    }

    @Override
    public void onTabPaused(FileInfo fileInfo, boolean b) {

    }

    @Override
    public void onJumpToSdCardFolder() {

    }

    @Override
    public void onTabDocumentLoaded(String tag) {

    }
}
