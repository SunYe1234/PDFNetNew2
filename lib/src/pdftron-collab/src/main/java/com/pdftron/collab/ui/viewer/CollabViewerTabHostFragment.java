package com.pdftron.collab.ui.viewer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.annotlist.CollabAnnotationListFragment;
import com.pdftron.collab.ui.annotlist.CollabAnnotationListSortOrder;
import com.pdftron.collab.viewmodel.DocumentViewModel;
import com.pdftron.pdf.controls.BookmarksTabLayout;
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.DialogFragmentTab;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.Utils;

import java.util.Objects;

/**
 * A {@link PdfViewCtrlTabHostFragment} that allows for real-time annotation collaboration. Used
 * in conjunction with {@link CollabViewerTabFragment}.
 * <p>
 * This fragment also provides a customized annotation list fragment that shows additional
 * information on annotation comments.
 */
public class CollabViewerTabHostFragment extends PdfViewCtrlTabHostFragment implements OpenAnnotationListListener {

    protected Class<? extends CollabViewerTabFragment> mTabFragmentClass; // default tab fragment class

    public interface CollabTabHostListener extends TabHostListener {
        /**
         * Called when the tab host has been shown.
         */
        default void onTabHostShown() {
        }

        /**
         * Called when the tab host has been hidden.
         */
        default void onTabHostHidden() {
        }

        /**
         * Called when the last tab in the tab host has been closed, and therefore there is no more tab.
         */
        default void onLastTabClosed() {
        }

        /**
         * Called when a new tab has been selected excluding the initial tab.
         *
         * @param tag the tab tag changed to
         */
        default void onTabChanged(String tag) {
        }

        /**
         * Called when an error has been happened when opening a document.
         */
        default void onOpenDocError() {
        }

        /**
         * Called when navigation button has been pressed.
         */
        default void onNavButtonPressed() {
        }

        /**
         * The implementation should browse to the specified file in the folder.
         *
         * @param fileName   The file name
         * @param filepath   The file path
         * @param itemSource The item source of the file
         */
        default void onShowFileInFolder(String fileName, String filepath, int itemSource) {
        }

        /**
         * The implementation should determine whether the long press on tab widget should show file info.
         *
         * @return true if long press shows file info, false otherwise
         */
        default boolean canShowFileInFolder() {
            return false;
        }

        /**
         * The implementation should determine whether closing a tab should show re-open snackbar.
         *
         * @return true if can show snackbar, false otherwise
         */
        default boolean canShowFileCloseSnackbar() {
            return false;
        }

        /**
         * Called when creating Toolbar options menu
         *
         * @param menu     the menu
         * @param inflater the inflater
         */
        default boolean onToolbarCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            return false;
        }

        /**
         * Called when preparing Toolbar options menu
         *
         * @param menu the menu
         */
        default boolean onToolbarPrepareOptionsMenu(Menu menu) {
            return false;
        }

        /**
         * Called when Toolbar options menu selected
         *
         * @param item the menu item
         */
        default boolean onToolbarOptionsItemSelected(MenuItem item) {
            return false;
        }

        /**
         * Called when search view expanded
         */
        default void onStartSearchMode() {
        }

        /**
         * Called when search view collapsed
         */
        default void onExitSearchMode() {
        }

        /**
         * Called when about the re-create Activity for day/night mode
         */
        default boolean canRecreateActivity() {
            return false;
        }

        /**
         * Called when the fragment is paused.
         *
         * @param fileInfo                  The file shown when tab has been paused
         * @param isDocModifiedAfterOpening True if document has been modified
         *                                  after opening; False otherwise
         */
        default void onTabPaused(FileInfo fileInfo, boolean isDocModifiedAfterOpening) {
        }

        /**
         * Called when an SD card file is opened as a local file
         */
        default void onJumpToSdCardFolder() {
        }

        /**
         * Called when document associated with a tab is loaded
         *
         * @param tag the document tag
         */
        void onTabDocumentLoaded(String tag);
    }

    /**
     * Adds the {@link CollabTabHostListener} listener.
     *
     * @param listener The listener
     */
    @SuppressWarnings("unused")
    public void addCollabHostListener(CollabTabHostListener listener) {
        addHostListener(listener);
    }

    /**
     * Removes the {@link CollabTabHostListener} listener.
     *
     * @param listener The listener
     */
    @SuppressWarnings("unused")
    public void removeCollabHostListener(CollabTabHostListener listener) {
        removeHostListener(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //noinspection unchecked
            mTabFragmentClass = (Class<? extends CollabViewerTabFragment>) getArguments().getSerializable(BUNDLE_TAB_FRAGMENT_CLASS);
        }
        mTabFragmentClass = mTabFragmentClass == null ? getDefaultTabFragmentClass() : mTabFragmentClass;

        Objects.requireNonNull(mTabFragmentClass,
                "CollabPdfViewCtrlTabFragment must return a non null " +
                        "Class object in getDefaultTabFragmentClass()");
    }

    /**
     * Returns a {@link CollabViewerTabFragment} class object that will be used to
     * instantiate viewer tabs.
     *
     * @return a {@code CollabPdfViewCtrlTabFragment} class to instantiate later
     */
    @NonNull
    @Override
    protected Class<? extends CollabViewerTabFragment> getDefaultTabFragmentClass() {
        return CollabViewerTabFragment.class;
    }

    @Override
    public TabLayout.Tab addTab(@Nullable Bundle args, String tag, String title, String fileExtension, String password, int itemSource) {
        if (args == null) {
            args = CollabViewerTabFragment.createBasicPdfViewCtrlTabBundle(tag, title,
                    fileExtension, password, itemSource, mViewerConfig);
        }

        TabLayout.Tab tab = createTab(tag, title, fileExtension, itemSource);
        if (tab != null) {
            mTabLayout.addTab(tab, mTabFragmentClass, args);
        }

        return tab;
    }

    @Override
    protected void setFragmentListeners(Fragment fragment) {
        if (fragment instanceof CollabViewerTabFragment) {
            CollabViewerTabFragment tabFragment = (CollabViewerTabFragment) fragment;
            tabFragment.setTabListener(this);
            tabFragment.addAnnotationToolbarListener(this);
            tabFragment.addQuickMenuListener(this);
            tabFragment.setOpenAnnotationListListener(this);
        }
    }

    @Override
    protected void removeFragmentListeners(Fragment fragment) {
        if (fragment instanceof CollabViewerTabFragment) {
            CollabViewerTabFragment tabFragment = (CollabViewerTabFragment) fragment;
            tabFragment.removeAnnotationToolbarListener(this);
            tabFragment.removeQuickMenuListener(this);
            tabFragment.setOpenAnnotationListListener(null);
        }
    }

    @Override
    @Nullable
    protected DialogFragmentTab createAnnotationDialogTab() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        final Context context = getContext();
        if (!(currentFragment instanceof CollabViewerTabFragment) || context == null) {
            return null;
        }
        String docId = ((CollabViewerTabFragment) currentFragment).getDocumentId();

        if (null == docId) {
            AnalyticsHandlerAdapter.getInstance().sendException(new IllegalStateException("Invalid document id"));
            return null;
        }

        DocumentViewModel documentViewModel = ViewModelProviders.of(currentFragment).get(DocumentViewModel.class);

        Bundle bundle = CollabAnnotationListFragment.newBundle(docId);
        bundle.putBoolean(CollabAnnotationListFragment.BUNDLE_IS_READ_ONLY, currentFragment.isTabReadOnly());
        bundle.putBoolean(CollabAnnotationListFragment.BUNDLE_IS_RTL, currentFragment.isRtlMode());
        bundle.putInt(CollabAnnotationListFragment.BUNDLE_KEY_SORT_MODE,
                PdfViewCtrlSettingsManager.getAnnotListSortOrder(context,
                        CollabAnnotationListSortOrder.DATE_DESCENDING) // default sort order
        );
        return new DialogFragmentTab(CollabAnnotationListFragment.class,
                BookmarksTabLayout.TAG_TAB_ANNOTATION,
                Utils.getDrawable(context,
                        documentViewModel.hasUnreadReplies() ?
                                R.drawable.ic_annotations_white_with_notification_24dp :
                                R.drawable.ic_annotations_white_24dp
                ),
                null,
                getString(com.pdftron.pdf.tools.R.string.bookmark_dialog_fragment_annotation_tab_title),
                bundle);
    }

    @Override
    public void openAnnotationList() {
        this.onOutlineOptionSelected(2);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mMenuEditPages != null) {
            mMenuEditPages.setVisible(false);
        }
        if (mMenuExport != null) {
            mMenuExport.setVisible(false);
        }
    }
}
