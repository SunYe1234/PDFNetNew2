//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.controls;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.transition.Fade;
import android.support.transition.Slide;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.DisplayCutoutCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pdftron.common.PDFNetException;
import com.pdftron.filters.SecondaryFileFilter;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.Bookmark;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.PageSet;
import com.pdftron.pdf.TextSearchResult;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.dialog.BookmarksDialogFragment;
import com.pdftron.pdf.dialog.CustomColorModeDialogFragment;
import com.pdftron.pdf.dialog.OptimizeDialogFragment;
import com.pdftron.pdf.dialog.RotateDialogFragment;
import com.pdftron.pdf.dialog.ViewModePickerDialogFragment;
import com.pdftron.pdf.dialog.annotlist.AnnotationListSortOrder;
import com.pdftron.pdf.dialog.pdflayer.PdfLayerDialog;
import com.pdftron.pdf.dialog.pdflayer.PdfLayerUtils;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.ExternalFileInfo;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.model.OptimizeParams;
import com.pdftron.pdf.model.PdfViewCtrlTabInfo;
import com.pdftron.pdf.tools.QuickMenu;
import com.pdftron.pdf.tools.QuickMenuItem;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.tools.ToolManager.ToolMode;
import com.pdftron.pdf.tools.UndoRedoManager;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnalyticsParam;
import com.pdftron.pdf.utils.CommonToast;
import com.pdftron.pdf.utils.DialogFragmentTab;
import com.pdftron.pdf.utils.PaneBehavior;
import com.pdftron.pdf.utils.PdfDocManager;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.PdfViewCtrlTabsManager;
import com.pdftron.pdf.utils.ShortcutHelper;
import com.pdftron.pdf.utils.UserCropUtilities;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.AppBarLayout;
import com.pdftron.sdf.SDFDoc;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * The PdfViewCtrlTabHostFragment shows multiple {@link PdfViewCtrlTabFragment}
 * in tab layout.
 */
public class PdfViewCtrlTabHostFragment extends Fragment implements
        PdfViewCtrlTabFragment.TabListener,
        AnnotationToolbar.AnnotationToolbarListener,
        ToolManager.QuickMenuListener,
        TabLayout.OnTabSelectedListener,
        SearchResultsView.SearchResultsListener,
        ViewModePickerDialogFragment.ViewModePickerDialogFragmentListener,
        BookmarksDialogFragment.BookmarksDialogListener,
        BookmarksTabLayout.BookmarksTabsListener,
        UserCropSelectionDialogFragment.UserCropSelectionDialogFragmentListener,
        UserCropDialogFragment.OnUserCropDialogDismissListener,
        UserCropUtilities.AutoCropInBackgroundTask.AutoCropTaskListener,
        ThumbnailsViewFragment.OnThumbnailsViewDialogDismissListener,
        ThumbnailsViewFragment.OnThumbnailsEditAttemptWhileReadOnlyListener,
        ThumbnailsViewFragment.OnExportThumbnailsListener,
        View.OnLayoutChangeListener,
        View.OnSystemUiVisibilityChangeListener {

    private static final String TAG = PdfViewCtrlTabHostFragment.class.getName();

    public static final String BUNDLE_TAB_HOST_NAV_ICON = "bundle_tab_host_nav_icon";
    public static final String BUNDLE_TAB_HOST_TOOLBAR_MENU = "bundle_tab_host_toolbar_menu";
    public static final String BUNDLE_TAB_HOST_CONFIG = "bundle_tab_host_config";
    public static final String BUNDLE_TAB_HOST_QUIT_APP_WHEN_DONE_VIEWING = "bundle_tab_host_quit_app_when_done_viewing";
    public static final String BUNDLE_TAB_FRAGMENT_CLASS = "PdfViewCtrlTabHostFragment_tab_fragment_class";

    // Customizable fields
    protected Class<? extends PdfViewCtrlTabFragment> mTabFragmentClass; // default tab fragment class

    private static final int MAX_TOOLBAR_ICON_COUNT = 7;

    private static final int HIDE_TOOLBARS_TIMER = 5000; // 5 sec
    private static final int HIDE_NAVIGATION_BAR_TIMER = 3000; // 3 sec (set to match immersive-sticky show duration)

    private static final String KEY_IS_SEARCH_MODE = "is_search_mode";
    private static final String KEY_IS_RESTARTED = "is_fragment_restarted";


    public static String formerUserNameFileName="FormerUserName.txt";
    public static String currentUsersNameFileName="UserName.txt";

    public static final int ANIMATE_DURATION_SHOW = 250;
    public static final int ANIMATE_DURATION_HIDE = 250;

    private static boolean sDebug;

    protected boolean mQuitAppWhenDoneViewing;

    protected int mToolbarNavRes = R.drawable.ic_menu_white_24dp;
    protected int[] mToolbarMenuResArray = new int[]{R.menu.fragment_viewer};
    protected ViewerConfig mViewerConfig;

    protected View mFragmentView;
    protected AppBarLayout mAppBarLayout;
    protected Toolbar mToolbar;
    protected SearchToolbar mSearchToolbar;
    protected CustomFragmentTabLayout mTabLayout;
    protected FrameLayout mFragmentContainer;

    private UndoRedoPopupWindow mUndoRedoPopupWindow;

    protected String mStartupTabTag;
    protected boolean mMultiTabModeEnabled = true;
    private int mCurTabIndex = -1;

    protected ThumbnailsViewFragment mThumbFragment;
    protected BookmarksDialogFragment mBookmarksDialog;

    // controls fields for bookmark dialog
    protected Bookmark mCurrentBookmark;

    protected int mLastSystemUIVisibility;

    protected AtomicBoolean mFileSystemChanged = new AtomicBoolean();

    protected boolean mFragmentPaused = true;

    private UserCropUtilities.AutoCropInBackgroundTask mAutoCropTask;
    private boolean mAutoCropTaskPaused;
    private String mAutoCropTaskTabTag;

    // UI elements
    protected SearchResultsView mSearchResultsView;
    protected boolean mIsSearchMode;
    protected boolean mIsRestarted = false;
    protected boolean mAutoHideEnabled = true;

    protected boolean mWillShowAnnotationToolbar;

    protected MenuItem mMenuShare;
    protected MenuItem mMenuAnnotToolbar;
    protected MenuItem mMenuViewMode;
    protected MenuItem mMenuPrint;
    protected MenuItem mMenuSearch;
    protected MenuItem mMenuUndo;
    protected MenuItem mMenuRedo;
    protected MenuItem mMenuEditPages;
    protected MenuItem mMenuCloseTab;
    protected MenuItem mMenuViewFileAttachment;
    protected MenuItem mMenuPdfLayers;
    protected MenuItem mMenuExport;
    protected MenuItem mMenuPasswordSave;

    protected List<TabHostListener> mTabHostListeners;

    protected int mSystemWindowInsetTop = 0;

    private File currentFile;

    // Disposables
    protected CompositeDisposable mDisposables;

    /**
     * Callback interface to be invoked when an interaction is needed.
     */
    public interface TabHostListener {

        /**
         * Called when the tab host has been shown.
         */
        void onTabHostShown();

        /**
         * Called when the tab host has been hidden.
         */
        void onTabHostHidden();

        /**
         * Called when the last tab in the tab host has been closed, and therefore there is no more tab.
         */
        void onLastTabClosed();

        /**
         * Called when a new tab has been selected excluding the initial tab.
         *
         * @param tag the tab tag changed to
         */
        @SuppressWarnings("unused")
        void onTabChanged(String tag);

        /**
         * Called when an error has been happened when opening a document.
         */
        void onOpenDocError();

        /**
         * Called when navigation button has been pressed.
         */
        void onNavButtonPressed();

        /**
         * The implementation should browse to the specified file in the folder.
         *
         * @param fileName   The file name
         * @param filepath   The file path
         * @param itemSource The item source of the file
         */
        void onShowFileInFolder(String fileName, String filepath, int itemSource);

        /**
         * The implementation should determine whether the long press on tab widget should show file info.
         *
         * @return true if long press shows file info, false otherwise
         */
        boolean canShowFileInFolder();

        /**
         * The implementation should determine whether closing a tab should show re-open snackbar.
         *
         * @return true if can show snackbar, false otherwise
         */
        boolean canShowFileCloseSnackbar();

        /**
         * Called when creating Toolbar options menu
         *
         * @param menu     the menu
         * @param inflater the inflater
         */
        @SuppressWarnings("unused")
        boolean onToolbarCreateOptionsMenu(Menu menu, MenuInflater inflater);

        /**
         * Called when preparing Toolbar options menu
         *
         * @param menu the menu
         */
        @SuppressWarnings("unused")
        boolean onToolbarPrepareOptionsMenu(Menu menu);

        /**
         * Called when Toolbar options menu selected
         *
         * @param item the menu item
         */
        boolean onToolbarOptionsItemSelected(MenuItem item);

        /**
         * Called when search view expanded
         */
        void onStartSearchMode();

        /**
         * Called when search view collapsed
         */
        void onExitSearchMode();


        /**
         * Called when about the re-create Activity for day/night mode
         */
        boolean canRecreateActivity();

        /**
         * Called when the fragment is paused.
         *
         * @param fileInfo                  The file shown when tab has been paused
         * @param isDocModifiedAfterOpening True if document has been modified
         *                                  after opening; False otherwise
         */
        void onTabPaused(FileInfo fileInfo, boolean isDocModifiedAfterOpening);

        /**
         * Called when an SD card file is opened as a local file
         */
        void onJumpToSdCardFolder();

        /**
         * Called when document associated with a tab is loaded
         * @param tag the document tag
         */
        void onTabDocumentLoaded(String tag);
    }

    // Handlers
    // Hide toolbar setup if there is any
    private boolean mToolbarTimerDisabled;
    private Handler mHideToolbarsHandler = new Handler(Looper.getMainLooper());
    private Runnable mHideToolbarsRunnable = new Runnable() {
        @Override
        public void run() {
            hideUI();
        }
    };

    // Hide navigation bar
    private Handler mHideNavigationBarHandler = new Handler(Looper.getMainLooper());
    private Runnable mHideNavigationBarRunnable = new Runnable() {
        @Override
        public void run() {
            final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
            if (currentFragment != null && currentFragment.isAnnotationMode()) {
                showSystemStatusBar();
            }
        }
    };

    /**
     * Returns a new instance of the class
     */
    public static PdfViewCtrlTabHostFragment newInstance(Bundle args) {
        // args has information about the new tab
        // the information about other tabs (already added) is accessible from PdfViewCtrlTabsManager
        PdfViewCtrlTabHostFragment fragment = new PdfViewCtrlTabHostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * The overload implementation of {@link Fragment#onCreate(Bundle)}.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (sDebug)
            Log.v("LifeCycle", "HostFragment.onCreate");

        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mDisposables = new CompositeDisposable();

        Activity activity = getActivity();

        if (canRecreateActivity() && activity instanceof AppCompatActivity && Utils.applyDayNight((AppCompatActivity) activity)) {
            return;
        }

        if (getArguments() != null) {
            mToolbarNavRes = getArguments().getInt(BUNDLE_TAB_HOST_NAV_ICON, R.drawable.ic_menu_white_24dp);
            int[] menus = getArguments().getIntArray(BUNDLE_TAB_HOST_TOOLBAR_MENU);
            if (menus != null) {
                mToolbarMenuResArray = menus;
            }
            mViewerConfig = getArguments().getParcelable(BUNDLE_TAB_HOST_CONFIG);
            mQuitAppWhenDoneViewing = getArguments().getBoolean(BUNDLE_TAB_HOST_QUIT_APP_WHEN_DONE_VIEWING, false);
            //noinspection unchecked
            mTabFragmentClass = (Class<? extends PdfViewCtrlTabFragment>) getArguments().getSerializable(BUNDLE_TAB_FRAGMENT_CLASS);
        }
        mTabFragmentClass = mTabFragmentClass == null ? getDefaultTabFragmentClass() : mTabFragmentClass;

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mIsSearchMode = savedInstanceState.getBoolean(KEY_IS_SEARCH_MODE);
            mIsRestarted = savedInstanceState.getBoolean(KEY_IS_RESTARTED);
        }
    }

    /**
     * The overload implementation of {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (sDebug)
            Log.v("LifeCycle", "HostFragment.onCreateView");

        return inflater.inflate(R.layout.controls_fragment_tabbed_pdfviewctrl, container, false);
    }

    /**
     * The overload implementation of {@link Fragment#onViewCreated(View, Bundle)}.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (sDebug)
            Log.v("LifeCycle", "HostFragment.onViewCreated");

        super.onViewCreated(view, savedInstanceState);

        mFragmentView = view;
        initViews();
        updateFullScreenModeLayout();

        createTabs(getArguments());

        updateTabLayout();

        adjustConfiguration();
    }

    /**
     * The overload implementation of {@link Fragment#onActivityCreated(Bundle)}.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity && useSupportActionBar()) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            appCompatActivity.setSupportActionBar(mToolbar);

            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                if (mViewerConfig != null && !Utils.isNullOrEmpty(mViewerConfig.getToolbarTitle())) {
                    actionBar.setDisplayShowTitleEnabled(true);
                    actionBar.setTitle(mViewerConfig.getToolbarTitle());
                } else {
                    actionBar.setDisplayShowTitleEnabled(false);
                }

                // NOTE: If the Toolbar menu is inflated manually then the visibility listener here
                // and any in the Activity **WILL NOT WORK**!
                actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
                    @Override
                    public void onMenuVisibilityChanged(boolean isVisible) {
                        if (isVisible) {
                            // Do not hide the toolbars while menus are open.
                            final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
                            boolean isAnnotationMode = currentFragment != null && currentFragment.isAnnotationMode();
                            if (!mIsSearchMode && !isAnnotationMode) {
                                stopHideToolbarsTimer();
                            }
                        } else {
                            final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
                            boolean isAnnotationMode = currentFragment != null && currentFragment.isAnnotationMode();
                            if (!mIsSearchMode && !isAnnotationMode) {
                                resetHideToolbarsTimer();
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        AnalyticsHandlerAdapter.getInstance().sendTimedEvent(AnalyticsHandlerAdapter.EVENT_SCREEN_VIEWER);
    }

    /**
     * The overload implementation of {@link Fragment#onResume()}.
     */
    @Override
    public void onResume() {
        if (sDebug)
            Log.v("LifeCycle", "HostFragment.onResume");

        super.onResume();

        if (isHidden()) {
            return;
        }

        resumeFragment();
    }

    /**
     * The overload implementation of {@link Fragment#onPause()}.
     */
    @Override
    public void onPause() {
        if (sDebug)
            Log.v("LifeCycle", "HostFragment.onPause");

        pauseFragment();

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        AnalyticsHandlerAdapter.getInstance().endTimedEvent(AnalyticsHandlerAdapter.EVENT_SCREEN_VIEWER);
    }

    /**
     * The overload implementation of {@link Fragment#onDestroyView()}.
     */
    @Override
    public void onDestroyView() {
        if (sDebug)
            Log.v("LifeCycle", "HostFragment.onDestroy");
        super.onDestroyView();
        mTabLayout.removeOnTabSelectedListener(this);
    }

    /**
     * The overload implementation of {@link Fragment#onDestroy()}.
     */
    @Override
    public void onDestroy() {
        if (sDebug)
            Log.v("LifeCycle", "HostFragment.onDestroy");

        try {
            mTabLayout.removeAllFragments();
        } catch (Exception ignored) {
        }

        PdfViewCtrlTabsManager.getInstance().cleanup();

        // Dispose of all observables
        if (mDisposables != null && !mDisposables.isDisposed()) {
            mDisposables.dispose();
        }

        super.onDestroy();
    }

    /**
     * The overload implementation of {@link Fragment#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_SEARCH_MODE, mIsSearchMode);
        outState.putBoolean(KEY_IS_RESTARTED, true);
    }

    /**
     * The overload implementation of {@link Fragment#onCreateOptionsMenu(Menu, MenuInflater)}.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                if (listener.onToolbarCreateOptionsMenu(menu, inflater)) {
                    return;
                }
            }
        }

        if (useSupportActionBar()) {
            for (int res : mToolbarMenuResArray) {
                inflater.inflate(res, menu);
            }
        }

        mMenuUndo = menu.findItem(R.id.undo);
        mMenuRedo = menu.findItem(R.id.redo);
        mMenuShare = menu.findItem(R.id.action_share);
        adjustShareButtonShowAs(activity);
        mMenuViewMode = menu.findItem(R.id.action_viewmode);
        mMenuAnnotToolbar = menu.findItem(R.id.action_annotation_toolbar);
        mMenuSearch = menu.findItem(R.id.action_search);
        mMenuPrint = menu.findItem(R.id.action_print);
        // Hide print buttons for non-KitKat devices
        if (mMenuPrint != null) {
            mMenuPrint.setVisible(Utils.isKitKat());
        }

        mMenuEditPages = menu.findItem(R.id.action_editpages);
        mMenuCloseTab = menu.findItem(R.id.action_close_tab);

        mMenuViewFileAttachment = menu.findItem(R.id.action_file_attachment);
        if (mMenuViewFileAttachment != null) {
            mMenuViewFileAttachment.setVisible(false);
        }
        mMenuPdfLayers = menu.findItem(R.id.action_pdf_layers);
        if (mMenuPdfLayers != null) {
            mMenuPdfLayers.setVisible(false);
        }
        mMenuExport = menu.findItem(R.id.action_export_options);
        mMenuPasswordSave = menu.findItem(R.id.menu_export_password_copy);

        setOptionsMenuVisible(true);
    }

    /**
     * The overload implementation of {@link Fragment#onPrepareOptionsMenu(Menu)}.
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                if (listener.onToolbarPrepareOptionsMenu(menu)) {
                    return;
                }
            }
        }

        if (menu != null) {
            // Update close menu item for current tab-mode.
            if (!mIsSearchMode) {
                updateCloseTabButtonVisibility(true);
            }

            if (mMenuViewFileAttachment != null) {
                if (currentFragment.getPdfDoc() != null &&
                        Utils.hasFileAttachments(currentFragment.getPdfDoc())) {
                    mMenuViewFileAttachment.setVisible(true);
                } else {
                    mMenuViewFileAttachment.setVisible(false);
                }
            }

            if (mMenuPdfLayers != null) {
                if (currentFragment.getPdfDoc() != null &&
                    PdfLayerUtils.hasPdfLayer(currentFragment.getPdfDoc())) {
                    mMenuPdfLayers.setVisible(true);
                } else {
                    mMenuPdfLayers.setVisible(false);
                }
            }

            if (mMenuPasswordSave != null) {
                if (currentFragment.isPasswordProtected()) {
                    mMenuPasswordSave.setTitle(getString(R.string.action_export_password_existing));
                } else {
                    mMenuPasswordSave.setTitle(getString(R.string.action_export_password));
                }
            }

            // Update undo/redo menu items for current mode.
            MenuItem undoItem = menu.findItem(R.id.undo);
            MenuItem redoItem = menu.findItem(R.id.redo);
            if (undoItem != null && redoItem != null) {
                ToolManager toolManager = currentFragment.getToolManager();
                UndoRedoManager undoRedoManager = (toolManager != null) ? toolManager.getUndoRedoManger() : null;
                // Undo/Redo can be shown when in "normal" viewing mode.
                if (!currentFragment.isReflowMode() && !mIsSearchMode && undoRedoManager != null) {
                    String nextUndoAction = undoRedoManager.getNextUndoAction();
                    if (!Utils.isNullOrEmpty(nextUndoAction)) {
                        undoItem.setTitle(nextUndoAction);
                        undoItem.setVisible(true);
                    } else {
                        undoItem.setVisible(false);
                    }

                    String nextRedoAction = undoRedoManager.getNextRedoAction();
                    if (!Utils.isNullOrEmpty(nextRedoAction)) {
                        redoItem.setTitle(nextRedoAction);
                        redoItem.setVisible(true);
                    } else {
                        redoItem.setVisible(false);
                    }
                } else {
                    undoItem.setVisible(false);
                    redoItem.setVisible(false);
                }
            }
            adjustShareButtonShowAs(activity);
        }
    }

    /**
     * The overload implementation of {@link Fragment#onOptionsItemSelected(MenuItem)}.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                if (listener.onToolbarOptionsItemSelected(item)) {
                    return true;
                }
            }
        }

        FragmentActivity activity = getActivity();
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return false;
        }

        final PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl == null) {
            return false;
        }

        if (!mIsSearchMode) {
            resetHideToolbarsTimer();
        }

        if (currentFragment.getToolManager() != null
                && currentFragment.getToolManager().getTool() != null) {
            ToolMode mode = ToolManager.getDefaultToolMode(currentFragment.getToolManager().getTool().getToolMode());
            if (mode == ToolMode.TEXT_CREATE ||
                    mode == ToolMode.CALLOUT_CREATE ||
                    mode == ToolMode.ANNOT_EDIT ||
                    mode == ToolMode.FORM_FILL) {
                pdfViewCtrl.closeTool();
            }
        }

        final int id = item.getItemId();

        if (id == android.R.id.home) {
            handleNavIconClick();
        } else if (!mIsSearchMode) {
            resetHideToolbarsTimer();
        }

        if (id == R.id.undo) {
            undo();
        } else if (id == R.id.redo) {
            redo();
        } else if (id == R.id.action_share) {
            if (currentFragment.isDocumentReady()) {
                onShareOptionSelected();
                AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_SHARE);
            }
        } else if (id == R.id.action_viewmode) {
            if (currentFragment.isDocumentReady()) {
                onViewModeOptionSelected();
            }
        } else if (id == R.id.action_annotation_toolbar) {
            if (currentFragment.isReflowMode()) {
                CommonToast.showText(activity, R.string.reflow_disable_markup_clicked);
            } else if (currentFragment.isDocumentReady()) {
                showAnnotationToolbar(AnnotationToolbar.START_MODE_NORMAL_TOOLBAR, null, null);
            }
        } else if (id == R.id.action_print) {
            if (currentFragment.isDocumentReady()) {
                currentFragment.handlePrintAnnotationSummary();
            }
        } else if (id == R.id.action_close_tab) {
            // if tabs not enabled, show close tab option
            if (!PdfViewCtrlSettingsManager.getMultipleTabs(activity)) {
                closeTab(currentFragment.getTabTag(), currentFragment.getTabSource());
            }
        } else if (id == R.id.action_addpage) {
            if (!checkTabConversionAndAlert(R.string.cant_edit_while_converting_message, false)) {
                addNewPage();
                AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_EDIT_PAGES_ADD);
            }
        } else if (id == R.id.action_deletepage) {
            if (!checkTabConversionAndAlert(R.string.cant_edit_while_converting_message, false)) {
                requestDeleteCurrentPage();
                AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_EDIT_PAGES_DELETE);
            }
        } else if (id == R.id.action_rotatepage) {
            if (!checkTabConversionAndAlert(R.string.cant_edit_while_converting_message, false)) {
                showRotateDialog();
                AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_EDIT_PAGES_ROTATE);
            }
        } else if (id == R.id.action_export_pages) {
            if (currentFragment.isDocumentReady()) {
                if (!checkTabConversionAndAlert(R.string.cant_edit_while_converting_message, false)) {
                    onViewModeSelected(PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_THUMBNAILS_VALUE, true,
                            pdfViewCtrl.getCurrentPage());
                    AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_EDIT_PAGES_REARRANGE);
                }
            }
        } else if (id == R.id.action_search) {
            if (currentFragment.isReflowMode()) {
                int messageID = R.string.reflow_disable_search_clicked;
                CommonToast.showText(activity, messageID);
                return false;
            }

            if (checkTabConversionAndAlert(R.string.cant_search_while_converting_message, true)) {
                return false;
            }

            if (mSearchToolbar == null || mToolbar == null) {
                return false;
            }
            startSearchMode();
            AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_SEARCH);
        } else if (id == R.id.menu_export_copy) {
            if (currentFragment.isDocumentReady()) {
                onSaveAsOptionSelected();
            }
        } else if (id == R.id.menu_export_flattened_copy) {
            if (currentFragment.isDocumentReady()) {
                onFlattenOptionSelected();
            }
        } else if (id == R.id.menu_export_optimized_copy) {
            if (currentFragment.isDocumentReady()) {
                onSaveOptimizedCopySelected();
            }
        } else if (id == R.id.menu_export_cropped_copy) {
            if (currentFragment.isDocumentReady()) {
                onSaveCroppedCopySelected();
            }
        } else if (id == R.id.menu_export_password_copy) {
            if (currentFragment.isDocumentReady()) {
                onSavePasswordCopySelected();
            }
        } else if (id == R.id.action_file_attachment) {
            if (currentFragment.isDocumentReady()) {
                currentFragment.handleViewFileAttachments();
            }
        } else if (id == R.id.action_pdf_layers) {
            if (currentFragment.isDocumentReady()) {
                PdfLayerDialog pdfLayerDialog = new PdfLayerDialog(
                    activity, pdfViewCtrl);
                pdfLayerDialog.show();
            }
        } else {
            return false;
        }

        return true;
    }

    protected void handleNavIconClick() {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            currentFragment.closeKeyboard();
        }
        stopHideToolbarsTimer();
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onNavButtonPressed();
            }
        }
    }

    /**
     * The overload implementation of {@link View.OnLayoutChangeListener#onLayoutChange(View, int, int, int, int, int, int, int, int)}.
     */
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        // check for setDoc error
        if (currentFragment.isOpenFileFailed()) {
            handleOpenFileFailed(currentFragment.getTabErrorCode());
            if (mTabHostListeners != null) {
                for (TabHostListener listener : mTabHostListeners) {
                    listener.onOpenDocError();
                }
            }
        }
    }

    /**
     * The overload implementation of {@link View.OnSystemUiVisibilityChangeListener#onSystemUiVisibilityChange(int)}.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        FragmentActivity activity = getActivity();
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        int diff = mLastSystemUIVisibility ^ visibility;
        if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
            // Check if the current fragment is in annotation mode.
            if (currentFragment.isAnnotationMode()) {
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                    // The navigation bar was hidden - stop and remove any timers.
                    stopHideNavigationBarTimer();
                } else {
                    // The navigation bar was shown - start a timer to hide it again.
                    resetHideNavigationBarTimer();
                }
            }
        }

        mLastSystemUIVisibility = visibility;
    }

    /**
     * Creates tabs.
     *
     * @param args The arguments
     */
    @CallSuper
    public void createTabs(Bundle args) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        String startupTitle = null;
        String startupFileExtension = null;
        int startupItemSource = BaseFileInfo.FILE_TYPE_UNKNOWN;
        // args will be null if we just want to go back to viewer without adding any new tabs
        if (args != null) {
            mStartupTabTag = mIsRestarted ? null : args.getString(PdfViewCtrlTabFragment.BUNDLE_TAB_TAG);
            startupTitle = args.getString(PdfViewCtrlTabFragment.BUNDLE_TAB_TITLE);
            startupFileExtension = args.getString(PdfViewCtrlTabFragment.BUNDLE_TAB_FILE_EXTENSION);
            startupItemSource = args.getInt(PdfViewCtrlTabFragment.BUNDLE_TAB_ITEM_SOURCE);

            // if startup tag is null
            // assume we want to open last opened file
            if (null != mStartupTabTag) {
                // error checking
                if (Utils.isNullOrEmpty(mStartupTabTag)
                        || Utils.isNullOrEmpty(startupTitle)
                        || (startupItemSource == BaseFileInfo.FILE_TYPE_FILE && !Utils.isNotPdf(mStartupTabTag) && !(new File(mStartupTabTag).exists()))) {
                    CommonToast.showText(activity, getString(R.string.error_opening_doc_message), Toast.LENGTH_SHORT);

                    if (mTabHostListeners != null) {
                        for (TabHostListener listener : mTabHostListeners) {
                            listener.onOpenDocError();
                        }
                    }
                    return;
                }
            }
        }

        // if Tabs is enabled, we add tabs to local tab list and show tab widget
        // else, clear tab list and hide tab widget
        if (!PdfViewCtrlSettingsManager.getMultipleTabs(activity)) {
            mMultiTabModeEnabled = false;
        }

        setTabLayoutVisible(mMultiTabModeEnabled);
//        if (userChanged()) {
//
            PdfViewCtrlTabsManager.getInstance().cleanup();
            PdfViewCtrlTabsManager.getInstance().clearAllPdfViewCtrlTabInfo(activity);
//        }

        // if single tab remove all current tabs
        if (!mMultiTabModeEnabled) {
            if (mStartupTabTag != null) {
                PdfViewCtrlTabsManager.getInstance().cleanup();
                PdfViewCtrlTabsManager.getInstance().clearAllPdfViewCtrlTabInfo(activity);
            } else {
                if(!userChanged()) {
                    // otherwise we should keep the latest viewed document
                    String latestViewedTabTag = PdfViewCtrlTabsManager.getInstance().getLatestViewedTabTag(activity);
                    if (latestViewedTabTag != null) {
                        ArrayList<String> documents = new ArrayList<>(PdfViewCtrlTabsManager.getInstance().getDocuments(activity));
                        for (String document : documents) {
                            if (!latestViewedTabTag.equals(document)) {
                                PdfViewCtrlTabsManager.getInstance().removeDocument(activity, document);
                            }
                        }
                    }
                }
                else
                {
                    String latestViewedTabTag = PdfViewCtrlTabsManager.getInstance().getLatestViewedTabTag(activity);
                    if (latestViewedTabTag != null) {
                        ArrayList<String> documents = new ArrayList<>(PdfViewCtrlTabsManager.getInstance().getDocuments(activity));
                        for (String document : documents) {
                                PdfViewCtrlTabsManager.getInstance().removeDocument(activity, document);

                        }
                    }
                }

            }
        }
//        if (userChanged()) {
//            PdfViewCtrlTabsManager.getInstance().cleanup();
//            PdfViewCtrlTabsManager.getInstance().clearAllPdfViewCtrlTabInfo(activity);
////            PdfViewCtrlTabsManager.getInstance().up();
//        }
        // add new document to tab list
//        if (canAddNewDocumentToTabList(startupItemSource)) {
        PdfViewCtrlTabsManager.getInstance().addDocument(activity, mStartupTabTag);
//        }
        // remove extra tabs
        if (!mMultiTabModeEnabled) {
            removeExtraTabs();
        }

        // add tabs
        for (String tag : PdfViewCtrlTabsManager.getInstance().getDocuments(activity)) {
            if (mTabLayout.getTabByTag(tag) != null) {
                // it has already been added
                continue;
            }
            if (!mMultiTabModeEnabled && mStartupTabTag != null) {
                if (!tag.equals(mStartupTabTag)) {
                    continue;
                }
            }
            PdfViewCtrlTabInfo info = PdfViewCtrlTabsManager.getInstance().getPdfFViewCtrlTabInfo(activity, tag);
            int itemSource = BaseFileInfo.FILE_TYPE_UNKNOWN;
            String title = "";
            String fileExtension = null;
            String password = "";

            if (info != null) {
                itemSource = info.tabSource;
                title = info.tabTitle;
                fileExtension = info.fileExtension;
                password = Utils.decryptIt(activity, info.password);
            }

            if (args != null && tag.equals(mStartupTabTag)) {
                itemSource = startupItemSource;
                password = args.getString(PdfViewCtrlTabFragment.BUNDLE_TAB_PASSWORD);
                title = startupTitle;
                try {
                    // get rid of the extension so tab title looks more user friendly
                    int index = FilenameUtils.indexOfExtension(title);
                    if (index != -1 && title != null) {
                        title = title.substring(0, index);
                        args.putString(PdfViewCtrlTabFragment.BUNDLE_TAB_TITLE, title);
                    }
                } catch (Exception e) {
                    AnalyticsHandlerAdapter.getInstance().sendException(e);
                }
                fileExtension = startupFileExtension;
            }
            if (canAddNewDocumentToTabList(itemSource) && !Utils.isNullOrEmpty(title)) {
                // args may contain more info that should pass to the viewer fragment
                addTab(tag.equals(mStartupTabTag) ? args : null, tag, title, fileExtension, password, itemSource);
            }
        }

        if (mStartupTabTag == null) {
            mStartupTabTag = PdfViewCtrlTabsManager.getInstance().getLatestViewedTabTag(activity);
        }
        setCurrentTabByTag(mStartupTabTag);
    }

    /**
     * The overload implementation of {@link SearchResultsView.SearchResultsListener#onSearchResultClicked(TextSearchResult)}.
     */
    @Override
    public void onSearchResultClicked(TextSearchResult result) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        Activity activity = getActivity();
        if (activity == null || currentFragment == null) {
            return;
        }

        PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl != null && pdfViewCtrl.getCurrentPage() != result.getPageNum()) {
            currentFragment.resetHidePageNumberIndicatorTimer();
        }

        currentFragment.highlightFullTextSearchResult(result);
        currentFragment.setCurrentPageHelper(result.getPageNum(), false);

        if (!Utils.isTablet(activity)) {
            hideSearchResults();
        }
    }


    private boolean userChanged()
    {
        String formerUser=getFormerUserNameFromFile();
        String currentUser=getCurrentUserNameFromFile();
        if (formerUser==null)
            return false;
        if (formerUser.equals(currentUser))
            return false;
        return true;


    }

    private  String getCurrentUserNameFromFile()
    {
        File fileDir = new File("/data/user/0/com.pdftron.completereader/files");

        try {
            File currentUsersName=new File(fileDir.getAbsolutePath()+"/"+currentUsersNameFileName);
            if (!currentUsersName.exists())
                currentUsersName.createNewFile();
            FileInputStream inputStream = new FileInputStream(currentUsersName);
//            UsersFile=new File(getFilesDir().getAbsolutePath()+"/"+formerUserNameFileName);
            System.out.println("读取前一个用户账户名：");
            // 一次读一个字符
            InputStreamReader reader = new InputStreamReader(inputStream);
            String usName="";
            int tempchar;
            while ((tempchar = reader.read()) != -1) {
                // 对于windows下，\r\n这两个字符在一起时，表示一个换行。
                // 但如果这两个字符分开显示时，会换两次行。
                // 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
                if (((char) tempchar) != '\r') {
                    System.out.print((char) tempchar);
                    usName+=(char)tempchar;
                }

            }
            reader.close();
            return usName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    private  String getFormerUserNameFromFile()
    {
        File fileDir = new File("/data/user/0/com.pdftron.completereader/files");

        try {
            File FormerUsersName=new File(fileDir.getAbsolutePath()+"/"+formerUserNameFileName);
            if (!FormerUsersName.exists())
                FormerUsersName.createNewFile();
            FileInputStream inputStream = new FileInputStream(FormerUsersName);
//            UsersFile=new File(getFilesDir().getAbsolutePath()+"/"+formerUserNameFileName);
            System.out.println("读取前一个用户账户名：");
            // 一次读一个字符
            InputStreamReader reader = new InputStreamReader(inputStream);
            String usName="";
            int tempchar;
            while ((tempchar = reader.read()) != -1) {
                // 对于windows下，\r\n这两个字符在一起时，表示一个换行。
                // 但如果这两个字符分开显示时，会换两次行。
                // 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
                if (((char) tempchar) != '\r') {
                    System.out.print((char) tempchar);
                    usName+=(char)tempchar;
                }

            }
            reader.close();
            return usName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }



    /**
     * The overload implementation of {@link SearchResultsView.SearchResultsListener#onFullTextSearchStart()}.
     */
    @Override
    public void onFullTextSearchStart() {
        if (mSearchToolbar != null) {
            mSearchToolbar.setSearchProgressBarVisible(true);
        }
    }

    /**
     * The overload implementation of {@link SearchResultsView.SearchResultsListener#onSearchResultFound(TextSearchResult)}.
     */
    @Override
    public void onSearchResultFound(TextSearchResult result) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (mSearchToolbar != null) {
            mSearchToolbar.setSearchProgressBarVisible(false);
        }
        if (result != null && currentFragment != null) {
            currentFragment.highlightFullTextSearchResult(result);
        }
    }

    /**
     * The overload implementation of {@link Fragment#onConfigurationChanged(Configuration)}.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (isHidden()) {
            return;
        }

        if (PdfViewCtrlSettingsManager.getFullScreenMode(activity)) {
            setToolbarsVisible(false);
            hideSystemUI();
        }

        if (mUndoRedoPopupWindow != null && mUndoRedoPopupWindow.isShowing()) {
            mUndoRedoPopupWindow.dismiss();
        }

        if (mSearchResultsView != null) {
            PaneBehavior paneBehavior = PaneBehavior.from(mSearchResultsView);
            if (paneBehavior != null) {
                paneBehavior.onOrientationChanged(mSearchResultsView, newConfig.orientation);
            }
        }

        updateTabLayout();
    }

    /**
     * The overload implementation of {@link BookmarksTabLayout.BookmarksTabsListener#onUserBookmarkClicked(int)}.
     */
    @Override
    public void onUserBookmarkClick(int pageNum) {
        if (mBookmarksDialog != null) {
            mBookmarksDialog.dismiss();
        }

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            currentFragment.setCurrentPageHelper(pageNum, true);
        }
    }

    /**
     * The overload implementation of {@link BookmarksTabLayout.BookmarksTabsListener#onOutlineClicked(Bookmark, Bookmark)}.
     */
    @Override
    public void onOutlineClicked(Bookmark parent, Bookmark bookmark) {
        if (mBookmarksDialog != null) {
            mBookmarksDialog.dismiss();
        }
        // Save the parent bookmark of the clicked bookmark
        mCurrentBookmark = parent;

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
            if (pdfViewCtrl != null) {
                currentFragment.setCurrentPageHelper(pdfViewCtrl.getCurrentPage(), false);
            }
        }
    }

    /**
     * The overload implementation of {@link BookmarksTabLayout.BookmarksTabsListener#onAnnotationClicked(Annot, int)}.
     */
    @Override
    public void onAnnotationClicked(Annot annotation, int pageNum) {
        if (mBookmarksDialog != null) {
            mBookmarksDialog.dismiss();
        }

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            currentFragment.setCurrentPageHelper(pageNum, false);
        }
    }

    /**
     * The overload implementation of {@link BookmarksTabLayout.BookmarksTabsListener#onExportAnnotations(PDFDoc)}.
     */
    @Override
    public void onExportAnnotations(PDFDoc pdfDoc) {
        if (mBookmarksDialog != null) {
            mBookmarksDialog.dismiss();
        }

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            currentFragment.onExportAnnotations(pdfDoc);
        }
    }

    /**
     * The overload implementation of {@link UserCropSelectionDialogFragment.UserCropSelectionDialogFragmentListener#onUserCropMethodSelected(int)}.
     */
    @Override
    public void onUserCropMethodSelected(int cropMode) {
        FragmentActivity activity = getActivity();
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.save(false, true, false);

        final PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl == null) {
            return;
        }

        if (cropMode == UserCropSelectionDialogFragment.MODE_AUTO_CROP) {
            if (mAutoCropTask != null && mAutoCropTask.getStatus() == AsyncTask.Status.RUNNING) {
                mAutoCropTask.cancel(true);
            }
            mAutoCropTask = new UserCropUtilities.AutoCropInBackgroundTask(activity, pdfViewCtrl, this);
            mAutoCropTask.execute();
        } else {
            UserCropDialogFragment userCropDialog = UserCropDialogFragment.newInstance(
                    cropMode == UserCropSelectionDialogFragment.MODE_RESET_CROP, false);
            userCropDialog.setOnUserCropDialogDismissListener(this);
            userCropDialog.setPdfViewCtrl(pdfViewCtrl);
            // Creates the dialog in full screen mode
            userCropDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomAppTheme);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                userCropDialog.show(fragmentManager, "usercrop_dialog");
            }
        }

        currentFragment.clearPageBackAndForwardStacks();
    }

    /**
     * The overload implementation of {@link UserCropSelectionDialogFragment.UserCropSelectionDialogFragmentListener#onUserCropSelectionDialogFragmentDismiss()}.
     */
    @Override
    public void onUserCropSelectionDialogFragmentDismiss() {
        resetHideToolbarsTimer();
    }

    /**
     * The overload implementation of {@link UserCropDialogFragment.OnUserCropDialogDismissListener#onUserCropDialogDismiss(int)}.
     */
    @Override
    public void onUserCropDialogDismiss(int pageNumberAtDismiss) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }
        currentFragment.setCurrentPageHelper(pageNumberAtDismiss, true);
        currentFragment.userCropDialogDismiss();
    }

    /**
     * The overload implementation of {@link UserCropUtilities.AutoCropInBackgroundTask.AutoCropTaskListener#onAutoCropTaskDone()}.
     */
    @Override
    public void onAutoCropTaskDone() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }
        currentFragment.userCropDialogDismiss();
    }

    /**
     * The overload implementation of {@link ThumbnailsViewFragment.OnThumbnailsViewDialogDismissListener#onThumbnailsViewDialogDismiss(int, boolean)}.
     */
    @Override
    public void onThumbnailsViewDialogDismiss(int pageNum, boolean docPagesModified) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        currentFragment.onThumbnailsViewDialogDismiss(pageNum, docPagesModified);
    }

    /**
     * The overload implementation of {@link ThumbnailsViewFragment.OnThumbnailsEditAttemptWhileReadOnlyListener#onThumbnailsEditAttemptWhileReadOnly()}.
     */
    @Override
    public void onThumbnailsEditAttemptWhileReadOnly() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        currentFragment.showReadOnlyAlert(mThumbFragment);
    }

    /**
     * The overload implementation of {@link ThumbnailsViewFragment.OnExportThumbnailsListener#onExportThumbnails(SparseBooleanArray)}.
     */
    @Override
    public void onExportThumbnails(SparseBooleanArray pageNums) {
        FragmentActivity activity = getActivity();
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }
        if (currentFragment.mTabSource == BaseFileInfo.FILE_TYPE_FILE) {
            handleThumbnailsExport(currentFragment.mCurrentFile.getParentFile(), pageNums);
        } else if (currentFragment.mTabSource == BaseFileInfo.FILE_TYPE_EXTERNAL) {
            ExternalFileInfo fileInfo = Utils.buildExternalFile(activity, currentFragment.mCurrentUriFile);
            if (fileInfo != null) {
                handleThumbnailsExport(fileInfo.getParent(), pageNums);
            }
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onTabDocumentLoaded(String)}.
     */
    @Override
    public void onTabDocumentLoaded(String tag) {
        setToolbarsVisible(true, false);

        // update print summary annotations modes
        updatePrintDocumentMode();
        updatePrintAnnotationsMode();
        updatePrintSummaryMode();

        // update view mode button icon
        updateButtonViewModeIcon();

        // update share button visibility
        updateShareButtonVisibility(true);

        // update buttons when in reflow mode
        updateIconsInReflowMode();

        if (mStartupTabTag != null && mStartupTabTag.equals(tag)) {
            setCurrentTabByTag(mStartupTabTag);
        }

        if (mAutoCropTaskPaused && mAutoCropTaskTabTag != null && mAutoCropTaskTabTag.equals(getCurrentTabTag())) {
            mAutoCropTaskPaused = false;
            onUserCropMethodSelected(UserCropSelectionDialogFragment.MODE_AUTO_CROP);
        }

        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onTabDocumentLoaded(tag);
            }
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onTabError(int, String)}.
     */
    @Override
    public void onTabError(int errorCode, String info) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        if (currentFragment.isOpenFileFailed()) {
            AnalyticsHandlerAdapter.getInstance().setString(AnalyticsHandlerAdapter.CustomKeys.TAB_ERROR,
                    String.format(Locale.US,"Error code %d: %s", errorCode, info));
            handleOpenFileFailed(errorCode, info);
            if (mTabHostListeners != null) {
                for (TabHostListener listener : mTabHostListeners) {
                    listener.onOpenDocError();
                }
            }
        }
    }

    /**
     * Creates and opens a new tab.
     *
     * @param args The arguments needed to create a new tab
     */
    public void onOpenAddNewTab(Bundle args) {
        if (args != null) {
            String tag = args.getString(PdfViewCtrlTabFragment.BUNDLE_TAB_TAG);
            String title = args.getString(PdfViewCtrlTabFragment.BUNDLE_TAB_TITLE);
            String password = args.getString(PdfViewCtrlTabFragment.BUNDLE_TAB_PASSWORD);
            int itemSource = args.getInt(PdfViewCtrlTabFragment.BUNDLE_TAB_ITEM_SOURCE);
            onOpenAddNewTab(itemSource, tag, title, password);
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onOpenAddNewTab(int, String, String, String)}.
     */
    @Override
    public void onOpenAddNewTab(int itemSource, String tag, String title, String password) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        // error checking
        if (Utils.isNullOrEmpty(tag)
                || Utils.isNullOrEmpty(title)
                || (itemSource == BaseFileInfo.FILE_TYPE_FILE && !Utils.isNotPdf(tag) && !(new File(tag).exists()))) {
            CommonToast.showText(activity, R.string.error_opening_doc_message, Toast.LENGTH_SHORT);

            if (mTabHostListeners != null) {
                for (TabHostListener listener : mTabHostListeners) {
                    listener.onOpenDocError();
                }
            }
            return;
        }

        mFileSystemChanged.set(true);

        String fileExtension = FilenameUtils.getExtension(title);
        String name = FilenameUtils.removeExtension(title);
        TabLayout.Tab newTab = addTab(null, tag, name, fileExtension, password, itemSource);
        newTab.select();

        // remove tabs after adding new tab to avoid any confusions from indexes shift
        // add new document to tab list
        PdfViewCtrlTabsManager.getInstance().addDocument(activity, tag);
        removeExtraTabs();
    }

    @Override
    public void onShowTabInfo(String tag, String title, String fileExtension, int itemSource, int duration) {
        handleShowTabInfo(tag, title, fileExtension, itemSource, duration);
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onTabIdentityChanged(String, String, String, String, int)}.
     */
    @Override
    public void onTabIdentityChanged(String oldTabTag, String newTabTag, String newTabTitle,
                                     String newFileExtension, int newTabSource) {
        mFileSystemChanged.set(true);

        if (mTabLayout != null) {
            TabLayout.Tab tab = mTabLayout.getTabByTag(oldTabTag);
            if (tab != null) {
                mTabLayout.replaceTag(tab, newTabTag);
                setTabView(tab.getCustomView(), newTabTag, newTabTitle, newFileExtension, newTabSource);
            }
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onPageThumbnailOptionSelected(boolean, Integer)}.
     */
    @Override
    public void onPageThumbnailOptionSelected(boolean thumbnailEditMode, Integer checkedItem) {
        FragmentActivity activity = getActivity();
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        final PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();

        // keep previously selected mode
        // display thumbnails view control
        if (checkTabConversionAndAlert(R.string.cant_edit_while_converting_message, true)) {
            return;
        }

        currentFragment.save(false, true, false);
        pdfViewCtrl.pause();

        boolean readonly = currentFragment.isTabReadOnly();
        if (!readonly && mViewerConfig != null && !mViewerConfig.isThumbnailViewEditingEnabled()) {
            // if document is editable, user can specify if a particular control is editable
            readonly = true;
        }
        mThumbFragment = ThumbnailsViewFragment.newInstance(readonly, thumbnailEditMode);
        mThumbFragment.setPdfViewCtrl(pdfViewCtrl);
        mThumbFragment.setOnExportThumbnailsListener(this);
        mThumbFragment.setOnThumbnailsViewDialogDismissListener(this);
        mThumbFragment.setOnThumbnailsEditAttemptWhileReadOnlyListener(this);
        mThumbFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomAppTheme);
        mThumbFragment.setTitle(getString(R.string.pref_viewmode_thumbnails_title));
        if (checkedItem != null) {
            mThumbFragment.setItemChecked(checkedItem - 1);
        }

        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            mThumbFragment.show(fragmentManager, "thumbnails_fragment");
        }
    }

    @Override
    public boolean onBackPressed() {
        return handleBackPressed();
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onTabPaused(FileInfo, boolean)}.
     */
    @Override
    public void onTabPaused(
            FileInfo fileInfo,
            boolean isDocModifiedAfterOpening) {

        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onTabPaused(fileInfo, isDocModifiedAfterOpening);
            }
        }

    }

    @Override
    public void onTabJumpToSdCardFolder() {
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onJumpToSdCardFolder();
            }
        }
    }

    @Override
    public void onUpdateOptionsMenu() {
        if (!useSupportActionBar()) {
            onPrepareOptionsMenu(mToolbar.getMenu());
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onInkEditSelected(Annot)}.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onInkEditSelected(Annot inkAnnot) {
        showAnnotationToolbar(AnnotationToolbar.START_MODE_EDIT_TOOLBAR, inkAnnot, ToolMode.INK_CREATE);
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onOpenAnnotationToolbar(ToolMode)}.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onOpenAnnotationToolbar(ToolMode mode) {

        showAnnotationToolbar(AnnotationToolbar.START_MODE_NORMAL_TOOLBAR, null, mode);

    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onOpenEditToolbar(ToolMode)}.
     */
    @Override
    public void onOpenEditToolbar(ToolMode mode) {
        showAnnotationToolbar(AnnotationToolbar.START_MODE_EDIT_TOOLBAR, null, mode);
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onToggleReflow()}.
     */
    @Override
    public void onToggleReflow() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        currentFragment.toggleReflow();
        updateIconsInReflowMode();
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onFullTextSearchFindText(boolean)}.
     */
    @Override
    public SearchResultsView.SearchResultStatus onFullTextSearchFindText(boolean searchUp) {
        SearchResultsView.SearchResultStatus status = SearchResultsView.SearchResultStatus.NOT_HANDLED;
        if (mSearchResultsView != null && mSearchResultsView.isActive()) {
            status = mSearchResultsView.getResult(searchUp);
        }
        return status;
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onTabThumbSliderStopTrackingTouch()}.
     */
    @Override
    public void onTabThumbSliderStopTrackingTouch() {
        resetHideToolbarsTimer();
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onTabSingleTapConfirmed()}.
     */
    @Override
    public void onTabSingleTapConfirmed() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        if (!currentFragment.isAnnotationMode() && !mIsSearchMode) {
            if (currentFragment.isThumbSliderVisible()) {
                hideUI();
            } else {
                showUI();
            }
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onSearchProgressShow()}.
     */
    @Override
    public void onSearchProgressShow() {
        if (mSearchToolbar != null) {
            mSearchToolbar.setSearchProgressBarVisible(true);
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onSearchProgressHide()}.
     */
    @Override
    public void onSearchProgressHide() {
        if (mSearchToolbar != null) {
            mSearchToolbar.setSearchProgressBarVisible(false);
        }
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#getToolbarHeight()}.
     */
    @Override
    public int getToolbarHeight() {
        if (mToolbar != null && mToolbar.isShown()) {
            return mAppBarLayout.getHeight();
        }
        return -1;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onDownloadedSuccessful()}.
     */
    @Override
    public void onDownloadedSuccessful() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        PdfViewCtrlTabsManager.getInstance().addDocument(activity, mStartupTabTag);
        removeExtraTabs();
    }

    /**
     * Adds the {@link TabHostListener} listener.
     *
     * @param listener The listener
     */
    public void addHostListener(TabHostListener listener) {
        if (mTabHostListeners == null) {
            mTabHostListeners = new ArrayList<>();
        }
        if (!mTabHostListeners.contains(listener)) {
            mTabHostListeners.add(listener);
        }
    }

    /**
     * Removes the {@link TabHostListener} listener.
     *
     * @param listener The listener
     */
    @SuppressWarnings("unused")
    public void removeHostListener(TabHostListener listener) {
        if (mTabHostListeners != null) {
            mTabHostListeners.remove(listener);
        }
    }

    /**
     * Removes all {@link TabHostListener} listeners.
     */
    @SuppressWarnings("unused")
    public void clearHostListeners() {
        if (mTabHostListeners != null) {
            mTabHostListeners.clear();
        }
    }

    public void onSaveAsOptionSelected() {
        final PdfViewCtrlTabFragment fragment = getCurrentPdfViewCtrlFragment();
        if (fragment != null) {
            fragment.save(false, true, true);
            fragment.handleSaveAsCopy();
        }
    }

    public void onFlattenOptionSelected() {
        if (checkTabConversionAndAlert(R.string.cant_save_while_converting_message, false, true)) {
            return;
        }

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        String msg = String.format(getString(R.string.dialog_flatten_message), getString(R.string.app_name));
        String title = getString(R.string.dialog_flatten_title);

        Utils.getAlertDialogBuilder(activity, msg, title)
                .setPositiveButton(R.string.tools_qm_flatten, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final PdfViewCtrlTabFragment fragment = getCurrentPdfViewCtrlFragment();
                        if (fragment != null) {
                            fragment.save(false, true, true);
                            fragment.handleSaveFlattenedCopy();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }

    public void onSaveOptimizedCopySelected() {
        if (checkTabConversionAndAlert(R.string.cant_save_while_converting_message, false, true)) {
            return;
        }

        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        currentFragment.save(false, true, true);

        OptimizeDialogFragment dialog = OptimizeDialogFragment.newInstance();
        dialog.setListener(new OptimizeDialogFragment.OptimizeDialogFragmentListener() {
            @Override
            public void onOptimizeClicked(OptimizeParams result) {
                final PdfViewCtrlTabFragment fragment = getCurrentPdfViewCtrlFragment();
                if (fragment == null) {
                    return;
                }
                fragment.handleSaveOptimizedCopy(result);
            }
        });
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            dialog.show(fragmentManager, "optimize_dialog");
        }
    }

    public void onSavePasswordCopySelected() {
        if (checkTabConversionAndAlert(R.string.cant_save_while_converting_message, false, true)) {
            return;
        }

        final PdfViewCtrlTabFragment fragment = getCurrentPdfViewCtrlFragment();
        if (fragment != null) {
            fragment.save(false, true, true);
            fragment.handleSavePasswordCopy();
        }
    }

    public void onSaveCroppedCopySelected() {
        if (checkTabConversionAndAlert(R.string.cant_save_while_converting_message, false)) {
            return;
        }

        final FragmentActivity activity = getActivity();
        final PdfViewCtrlTabFragment fragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || fragment == null) {
            return;
        }
        final PDFViewCtrl pdfViewCtrl = fragment.getPDFViewCtrl();
        if (pdfViewCtrl == null) {
            return;
        }
        fragment.save(false, true, true);

        final ProgressDialog progressDialog = new ProgressDialog(activity);

        mDisposables.add(fragment.hasUserCropBoxDisposable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        progressDialog.setMessage(getString(R.string.save_crop_wait));
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setIndeterminate(true);
                    }
                })
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean params) throws Exception {
                                   progressDialog.dismiss();

                                   pdfViewCtrl.requestRendering();
                                   if (progressDialog.isShowing()) {
                                       progressDialog.dismiss();
                                   }
                                   if (params != null) {
                                       if (params) {
                                           fragment.handleSaveCroppedCopy();
                                       } else {
                                           AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                           builder.setMessage(getString(R.string.save_crop_no_cropbox_warning_msg))
                                                   .setCancelable(true);
                                           int posButton = R.string.save_crop_no_cropbox_warning_positive;
                                           int negButton = R.string.cancel;

                                           builder.setPositiveButton(posButton, new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   dialog.dismiss();

                                                   UserCropSelectionDialogFragment cropDialog = UserCropSelectionDialogFragment.newInstance();
                                                   cropDialog.setUserCropSelectionDialogFragmentListener(PdfViewCtrlTabHostFragment.this);
                                                   FragmentManager fragmentManager = getFragmentManager();
                                                   if (fragmentManager != null) {
                                                       cropDialog.show(fragmentManager, "user_crop_mode_picker");
                                                   }
                                                   stopHideToolbarsTimer();
                                               }
                                           }).setNegativeButton(negButton, new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   dialog.dismiss();
                                               }
                                           }).create().show();
                                       }
                                   }
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   progressDialog.dismiss();
                               }
                           }
                ));
    }

    @SuppressLint("RestrictedApi")
    private void initViews() {
        FragmentActivity activity = getActivity();
        if (activity == null || mFragmentView == null) {
            return;
        }

        mFragmentView.addOnLayoutChangeListener(this);

        if (Utils.isKitKat()) {
            // See {@link #onSystemUiVisibilityChange(int)}.
            mFragmentView.setOnSystemUiVisibilityChangeListener(this);
            mLastSystemUIVisibility = mFragmentView.getWindowSystemUiVisibility();
        }

        mTabLayout = mFragmentView.findViewById(R.id.doc_tabs);
        mTabLayout.setup(activity,
                getChildFragmentManager(),
                mViewerConfig != null && !mViewerConfig.isAutoHideToolbarEnabled() ?
                        R.id.adjust_fragment_container : R.id.realtabcontent
        );
        mTabLayout.addOnTabSelectedListener(this);

        mToolbar = mFragmentView.findViewById(R.id.toolbar);
        if (!useSupportActionBar()) {
            for (int res : mToolbarMenuResArray) {
                mToolbar.inflateMenu(res);
            }
            onCreateOptionsMenu(mToolbar.getMenu(), new MenuInflater(activity));
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return onOptionsItemSelected(menuItem);
                }
            });
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleNavIconClick();
                }
            });
        }

        mSearchToolbar = mFragmentView.findViewById(R.id.searchToolbar);
        mSearchToolbar.setSearchToolbarListener(new SearchToolbar.SearchToolbarListener() {

            @Override
            public void onExitSearch() {
                // onBackPressed is skipped when a support ActionBar menu-item is expanded,
                // so hide the search results list before exiting search mode.
                if (mSearchResultsView != null && mSearchResultsView.getVisibility() == View.VISIBLE) {
                    hideSearchResults();
                } else {
                    exitSearchMode();
                }
            }

            @Override
            public void onClearSearchQuery() {
                // Cancel search
                final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
                if (currentFragment == null) {
                    return;
                }
                currentFragment.cancelFindText();
                if (mSearchResultsView != null) {
                    if (mSearchResultsView.isActive()) {
                        mSearchResultsView.cancelGetResult();
                    }
                    hideSearchResults();
                }
            }

            @Override
            public void onSearchQuerySubmit(String s) {
                final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
                if (currentFragment != null) {
                    currentFragment.queryTextSubmit(s);
                }

                if (mSearchResultsView != null) {
                    mSearchResultsView.findText(s);
                }
            }

            @Override
            public void onSearchQueryChange(String s) {
                final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
                if (currentFragment != null) {
                    currentFragment.setSearchQuery(s);
                }

                if (mSearchResultsView != null && mSearchResultsView.isActive() && !mSearchResultsView.getSearchPattern().equals(s)) {
                    mSearchResultsView.cancelGetResult();
                }
            }

            @Override
            public void onSearchOptionsItemSelected(MenuItem item, String searchQuery) {
                final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
                if (currentFragment == null) {
                    return;
                }
                final int id = item.getItemId();
                if (id == R.id.action_list_all) {
                    if (currentFragment.isDocumentReady()) {
                        onListAllOptionSelected(searchQuery);
                        AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_SEARCH_LIST_ALL);
                    }
                } else if (id == R.id.action_match_case) {
                    if (currentFragment.isDocumentReady()) {
                        boolean isChecked = item.isChecked();
                        onSearchMatchCaseOptionSelected(!isChecked);
                        item.setChecked(!isChecked);
                    }
                } else if (id == R.id.action_whole_word) {
                    if (currentFragment.isDocumentReady()) {
                        boolean isChecked = item.isChecked();
                        onSearchWholeWordOptionSelected(!isChecked);
                        item.setChecked(!isChecked);
                    }
                }
            }
        });

        updateToolbarDrawable();

        mAppBarLayout = mFragmentView.findViewById(R.id.app_bar_layout);

        mFragmentContainer = mFragmentView.findViewById(R.id.realtabcontent);
        if (mFragmentContainer != null) {
            // The following listener will only be used for v21+.
            // When *not* in fullscreen mode, the tab fragment container needs to use the insets as
            // padding so that the tab fragments are not obscured by the system bars.
            ViewCompat.setOnApplyWindowInsetsListener(mFragmentContainer, new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                    WindowInsetsCompat result = insets;
                    Context context = (view != null) ? view.getContext() : null;
                    if (context != null && !PdfViewCtrlSettingsManager.getFullScreenMode(context)) {
                        // Apply the default insets policy handler.
                        result = ViewCompat.onApplyWindowInsets(view, insets);
                    }

                    // screen cutout
                    DisplayCutoutCompat cutout = insets.getDisplayCutout();
                    if (cutout != null) {
                        // we found cutout!
                        // only handle cutout if hiding toolbars
                        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();

                        if (currentFragment != null && mAppBarLayout != null && !mWillShowAnnotationToolbar) {
                            if (mAppBarLayout.getVisibility() == View.VISIBLE) {
                                currentFragment.applyCutout(0, 0);
                            } else {
                                currentFragment.applyCutout(cutout.getSafeInsetTop(), cutout.getSafeInsetBottom());
                            }
                        }
                    }

                    mSystemWindowInsetTop = result.getSystemWindowInsetTop();
                    return result;
                }
            });
        }
    }

    /**
     * Adds a new tab
     *
     * @param args          The argument needed to create PdfViewCtrlTabFragment
     * @param tag           The tab tag
     * @param title         The title
     * @param fileExtension The file extension
     * @param password      The password
     * @param itemSource    The item source of the document
     * @return The created tab
     */
    public TabLayout.Tab addTab(@Nullable Bundle args, String tag, String title, String fileExtension, String password, int itemSource) {
        if (args == null) {
            args = PdfViewCtrlTabFragment.createBasicPdfViewCtrlTabBundle(tag, title,
                    fileExtension, password, itemSource, mViewerConfig);
        }

        TabLayout.Tab tab = createTab(tag, title, fileExtension, itemSource);
        if (tab != null) {
            mTabLayout.addTab(tab, mTabFragmentClass, args);
        }

        return tab;
    }

    /**
     * Creates a tab.
     *
     * @param tag           The tab tag
     * @param title         The title of tab
     * @param fileExtension The file extension
     * @param itemSource    The item source of the file
     * @return The tab
     */
    protected TabLayout.Tab createTab(final String tag, final String title, final String fileExtension, final int itemSource) {
        TabLayout.Tab tab = mTabLayout.newTab().setTag(tag).setCustomView(R.layout.controls_fragment_tabbed_pdfviewctrl_tab);
        setTabView(tab.getCustomView(), tag, title, fileExtension, itemSource);
        return tab;
    }

    /**
     * Sets the tab view.
     *
     * @param view          The view
     * @param tag           The tab tag
     * @param title         The title fo tab
     * @param fileExtension The file extension
     * @param itemSource    The item source of the file
     */
    protected void setTabView(View view, final String tag, final String title,
                              final String fileExtension, final int itemSource) {
        if (view == null) {
            return;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTabLayout != null) {
                    TabLayout.Tab currentTab = mTabLayout.getTabByTag(tag);
                    if (currentTab != null) {
                        currentTab.select();
                    }
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleShowTabInfo(tag, title, fileExtension, itemSource, Snackbar.LENGTH_LONG);
                return true;
            }
        });

        if (Utils.isMarshmallow()) {
            view.setOnContextClickListener(new View.OnContextClickListener() {
                @Override
                public boolean onContextClick(View v) {
                    return v.performLongClick();
                }
            });
        }

        TextView textView = view.findViewById(R.id.tab_pdfviewctrl_text);
        if (textView != null) {
            textView.setText(title);
        }

        View closeButton = view.findViewById(R.id.tab_pdfviewctrl_close_button);
        if (closeButton != null) {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeTab(tag, itemSource);
                }
            });
        }
    }

    /**
     * Removes the specified tab at index.
     * @param index index of the tab
     */
    public void removeTabAt(int index) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (mTabLayout.getTabCount() > index && index >= 0) {
            TabLayout.Tab tab = mTabLayout.getTabAt(index);
            if (tab != null) {
                PdfViewCtrlTabsManager.getInstance().removeDocument(activity, (String) tab.getTag());
                mTabLayout.removeTab(tab);
            }
        }
    }

    /**
     * Removes the specified tab.
     *
     * @param filepath The file path
     */
    public void removeTab(String filepath) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final String currentTabTag = getCurrentTabTag();
        if (currentTabTag == null) {
            return;
        }

        PdfViewCtrlTabsManager.getInstance().removeDocument(activity, filepath);

        // calculate new tab to be selected
        String nextTabTagToSelect = null;
        if (currentTabTag.equals(filepath)) {
            // if current tab is closed, set new current tab to the most recently viewed tab
            nextTabTagToSelect = PdfViewCtrlTabsManager.getInstance().getLatestViewedTabTag(activity);
        }

        removeTab(filepath, nextTabTagToSelect);
    }

    /**
     * Removes the specified tab.
     *
     * @param filepath           The file path
     * @param nextTabTagToSelect The tab tag of the tab that should be selected thereafter
     */
    public void removeTab(String filepath, final String nextTabTagToSelect) {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return;
        }

        if (Utils.isNullOrEmpty(filepath)) {
            return;
        }

        // first select the target tab and then remove the current tab; otherwise, TabLayout
        // will select the previous tab if the current tab is removed
        setCurrentTabByTag(nextTabTagToSelect);

        TabLayout.Tab closedTab = mTabLayout.getTabByTag(filepath);
        if (closedTab != null) {
            mTabLayout.removeTab(closedTab);
        }

        // selecting and removing tabs in one UI thread run may result in undesired behavior;
        // for example, if closedTab has lower index compared to nextTabTagToSelect then the tab
        // indicator will be set incorrectly. As a workaround we select tab later in the
        // next UI thread run
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                setCurrentTabByTag(nextTabTagToSelect);
            }
        });

        if (mTabLayout.getTabCount() == 0) {
            onLastTabClosed();
        }
    }

    private void onLastTabClosed() {
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onLastTabClosed();
            }
        }
    }

    /**
     * Closes all tabs.
     */
    public void closeAllTabs() {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return;
        }
        while (mTabLayout.getTabCount() > 0) {
            TabLayout.Tab tab = mTabLayout.getTabAt(0);
            if (tab != null) {
                PdfViewCtrlTabsManager.getInstance().removeDocument(activity, (String) tab.getTag());
                mTabLayout.removeTab(tab);
            }
        }
    }

    /**
     * Closes the specified tab.
     *
     * @param tag the tab tag
     */
    public void closeTab(final String tag) {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return;
        }

        // if the tab is closed then shouldn't be added to PDFViewCrtTabsManager
        Fragment fragment = mTabLayout.getFragmentByTag(tag);
        if (fragment instanceof PdfViewCtrlTabFragment) {
            closeTab(tag, ((PdfViewCtrlTabFragment) fragment).getTabSource());
        }
    }

    private void closeTab(final String tag, final int itemSource) {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return;
        }

        // if the tab is closed then shouldn't be added to PDFViewCrtTabsManager
        Fragment fragment = mTabLayout.getFragmentByTag(tag);
        boolean isDocModified = false;
        boolean isTabReadOnly = true;
        PdfViewCtrlTabFragment pdfViewCtrlTabFragment = null;
        if (fragment instanceof PdfViewCtrlTabFragment) {
            pdfViewCtrlTabFragment = (PdfViewCtrlTabFragment) fragment;
            isDocModified = pdfViewCtrlTabFragment.isDocModifiedAfterOpening();
            isTabReadOnly = pdfViewCtrlTabFragment.isTabReadOnly();
            pdfViewCtrlTabFragment.setCanAddToTabInfo(false);
        }

        if (mTabLayout.getTabCount() > 1) {
            final PdfViewCtrlTabInfo info = PdfViewCtrlTabsManager.getInstance().getPdfFViewCtrlTabInfo(activity, tag);

            if (itemSource != BaseFileInfo.FILE_TYPE_OPEN_URL) {
                String desc = getString(isDocModified && !isTabReadOnly ? R.string.snack_bar_tab_saved_and_closed : R.string.snack_bar_tab_closed);
                showSnackbar(desc, getString(R.string.reopen),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (info != null) {
                                    PdfViewCtrlTabsManager.getInstance().addDocument(v.getContext(), tag);
                                    String password = info.password == null ? "" : Utils.decryptIt(v.getContext(), info.password);
                                    addTab(null, tag, info.tabTitle, info.fileExtension, password, itemSource);
                                    setCurrentTabByTag(tag);

                                    AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_UNDO,
                                            AnalyticsParam.viewerUndoRedoParam("close_tab", AnalyticsHandlerAdapter.LOCATION_VIEWER));
                                }
                            }
                        });
                if (pdfViewCtrlTabFragment != null) {
                    pdfViewCtrlTabFragment.setSavedAndClosedShown();
                }
            }
        }

        removeTab(tag);

        if (mTabLayout.getTabCount() == 0) {
            onLastTabClosed();
        }
    }

    /**
     * Removes extra tabs.
     */
    public void removeExtraTabs() {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return;
        }

        if (!PdfViewCtrlSettingsManager.getMultipleTabs(activity)) {
            while (mTabLayout.getTabCount() > 1) {
                TabLayout.Tab tab = mTabLayout.getTabAt(0);
                if (tab != null) {
                    PdfViewCtrlTabsManager.getInstance().removeDocument(activity, (String) tab.getTag());
                    mTabLayout.removeTab(tab);
                }
            }
            return;
        }

        while (PdfViewCtrlTabsManager.getInstance().getDocuments(activity).size() > getMaxTabCount()) {
            String removedTabTag = PdfViewCtrlTabsManager.getInstance().removeOldestViewedTab(activity);
            TabLayout.Tab removedTab = mTabLayout.getTabByTag(removedTabTag);
            if (removedTab != null) {
                mTabLayout.removeTab(removedTab);
            }
        }
    }

    private int getMaxTabCount() {
        if (mViewerConfig != null && mViewerConfig.getMaximumTabCount() > 0) {
            return mViewerConfig.getMaximumTabCount();
        }
        Activity activity = getActivity();
        if (activity != null) {
            return Utils.isTablet(activity)
                    ? PdfViewCtrlTabsManager.MAX_NUM_TABS_TABLET
                    : PdfViewCtrlTabsManager.MAX_NUM_TABS_PHONE;
        }
        return 0;
    }

    /**
     * The overload implementation of {@link PdfViewCtrlTabFragment.TabListener#onUndoRedoPopupClosed()}.
     */
    @Override
    public void onUndoRedoPopupClosed() {
        if (mUndoRedoPopupWindow != null && mUndoRedoPopupWindow.isShowing()) {
            mUndoRedoPopupWindow.dismiss();
        }
    }

    /**
     * Undoes the last operation.
     */
    protected void undo() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.undo(false);

        if (currentFragment.getToolManager() != null) {
            UndoRedoManager undoRedoManager = currentFragment.getToolManager().getUndoRedoManger();
            if (undoRedoManager != null) {
                setToolbarsVisible(false);

                try {
                    if (mUndoRedoPopupWindow != null && mUndoRedoPopupWindow.isShowing()) {
                        mUndoRedoPopupWindow.dismiss();
                    }
                    mUndoRedoPopupWindow = new UndoRedoPopupWindow(activity, undoRedoManager,
                            new UndoRedoPopupWindow.OnUndoRedoListener() {
                                @Override
                                public void onUndoRedoCalled() {
                                    currentFragment.refreshPageCount();
                                }
                            }, AnalyticsHandlerAdapter.LOCATION_VIEWER);
                    mUndoRedoPopupWindow.showAtLocation(currentFragment.getView(), Gravity.TOP | Gravity.END, 0, 0);
                } catch (Exception ex) {
                    AnalyticsHandlerAdapter.getInstance().sendException(ex);
                }
            }
        }
    }

    /**
     * Redoes the last undo.
     */
    protected void redo() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.redo(false);

        if (currentFragment.getToolManager() != null) {
            UndoRedoManager undoRedoManager = currentFragment.getToolManager().getUndoRedoManger();
            if (undoRedoManager != null) {
                setToolbarsVisible(false);

                try {
                    if (mUndoRedoPopupWindow != null && mUndoRedoPopupWindow.isShowing()) {
                        mUndoRedoPopupWindow.dismiss();
                    }
                    mUndoRedoPopupWindow = new UndoRedoPopupWindow(activity, undoRedoManager,
                            new UndoRedoPopupWindow.OnUndoRedoListener() {
                                @Override
                                public void onUndoRedoCalled() {
                                    currentFragment.refreshPageCount();
                                }
                            }, AnalyticsHandlerAdapter.LOCATION_VIEWER);
                    mUndoRedoPopupWindow.showAtLocation(currentFragment.getView(), Gravity.TOP | Gravity.END, 0, 0);
                } catch (Exception ex) {
                    AnalyticsHandlerAdapter.getInstance().sendException(ex);
                }
            }
        }
    }

    /**
     * Checks whether can add new document to the tab list.
     *
     * @param itemSource The item source
     * @return True if can add new document to the tab list
     */
    protected boolean canAddNewDocumentToTabList(int itemSource) {
        return true;
    }

    /**
     * Sets all needed listeners for PdfViewCtrlTabFragment fragment
     *
     * @param fragment The PdfViewCtrlTabFragment fragment
     */
    protected void setFragmentListeners(Fragment fragment) {
        if (fragment instanceof PdfViewCtrlTabFragment) {
            PdfViewCtrlTabFragment tabFragment = (PdfViewCtrlTabFragment) fragment;
            tabFragment.setTabListener(this);
            tabFragment.addAnnotationToolbarListener(this);
            tabFragment.addQuickMenuListener(this);
        }
    }

    protected void removeFragmentListeners(Fragment fragment) {
        if (fragment instanceof PdfViewCtrlTabFragment) {
            PdfViewCtrlTabFragment tabFragment = (PdfViewCtrlTabFragment) fragment;
            tabFragment.removeAnnotationToolbarListener(this);
            tabFragment.removeQuickMenuListener(this);
        }
    }

    /**
     * Returns the selected {@link PdfViewCtrlTabFragment}.
     *
     * @return The PdfViewCtrlTabFragment
     */
    public PdfViewCtrlTabFragment getCurrentPdfViewCtrlFragment() {
        if (mTabLayout == null) {
            return null;
        }
        Fragment fragment = mTabLayout.getCurrentFragment();
        if (fragment instanceof PdfViewCtrlTabFragment) {
            return (PdfViewCtrlTabFragment) fragment;
        }

        return null;
    }

    /**
     * Shows tab information in a snack bar
     *
     * @param message    The message
     * @param path       The file path
     * @param tag        The tab tag
     * @param itemSource The item source of the document
     */
    public void showTabInfo(String message, String path, String tag, final int itemSource, final int duration) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment fragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || fragment == null || fragment.getView() == null) {
            return;
        }

        final String filepath;
        if (itemSource == BaseFileInfo.FILE_TYPE_EXTERNAL) {
            Uri uri = Uri.parse(tag);
            ExternalFileInfo info = Utils.buildExternalFile(activity, uri);
            if (info != null) {
                String uriFilename = Uri.encode(info.getFileName());
                if (!Utils.isNullOrEmpty(uriFilename) && tag.endsWith(uriFilename)) {
                    filepath = tag.substring(0, tag.length() - uriFilename.length());
                } else {
                    filepath = "";
                }
            } else {
                filepath = "";
            }
        } else {
            filepath = path;
        }
        if (sDebug) {
            if (Utils.isNullOrEmpty(filepath)) {
                String tempPath = "";
                FileInfo info = fragment.getCurrentFileInfo();
                if (info != null) {
                    tempPath = info.getAbsolutePath();
                }
                CommonToast.showText(activity, "DEBUG: [" + itemSource + "] [" + tempPath + "]");
            } else {
                CommonToast.showText(activity, "DEBUG: [" + filepath + "]");
            }
        }
        final String filename = message;
        if ((itemSource == BaseFileInfo.FILE_TYPE_FILE
                || itemSource == BaseFileInfo.FILE_TYPE_OPEN_URL
                || itemSource == BaseFileInfo.FILE_TYPE_EXTERNAL
                || itemSource == BaseFileInfo.FILE_TYPE_EDIT_URI
                || itemSource == BaseFileInfo.FILE_TYPE_OFFICE_URI)
                && !Utils.isNullOrEmpty(filepath)) {
            View.OnClickListener snackbarActionListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String path = null;
                    String name = null;
                    if (mTabHostListeners != null) {
                        if (mTabLayout == null) {
                            return;
                        }
                        if (itemSource == BaseFileInfo.FILE_TYPE_OPEN_URL) {
                            ArrayList<Fragment> fragments = mTabLayout.getLiveFragments();
                            for (Fragment fragment : fragments) {
                                if (fragment instanceof PdfViewCtrlTabFragment) {
                                    PdfViewCtrlTabFragment pdfViewCtrlTabFragment = (PdfViewCtrlTabFragment) fragment;
                                    if (pdfViewCtrlTabFragment.mTabTag.contains(filepath)
                                            && pdfViewCtrlTabFragment.mTabTag.contains(filename)) {
                                        String fullFilePath = pdfViewCtrlTabFragment.getFilePath();
                                        if (!Utils.isNullOrEmpty(fullFilePath)) {
                                            path = FilenameUtils.getPath(fullFilePath);
                                            name = FilenameUtils.getName(fullFilePath);
                                        }
                                    }
                                }
                            }
                        } else {
                            path = filepath;
                            name = filename;
                        }

                        for (TabHostListener listener : mTabHostListeners) {
                            listener.onShowFileInFolder(name, path, itemSource);
                        }
                    }
                }
            };
            showSnackbar(message, getString(R.string.snack_bar_file_info_message), snackbarActionListener, duration);
        } else {
            showSnackbar(message, null, null, duration);
        }
    }

    /**
     * Sets the visibility of thumbnail slider.
     *
     * @param visible            True if visible
     * @param animateThumbSlider True if visibility should be changed with animation
     */
    public void setThumbSliderVisibility(boolean visible, boolean animateThumbSlider) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.setThumbSliderVisible(visible, animateThumbSlider);
    }

    /**
     * Selects the tab that has the specified tag.
     *
     * @param tag The tab tag
     */
    public void setCurrentTabByTag(String tag) {
        if (tag == null || mTabLayout == null) {
            return;
        }

        try {
            for (int i = 0, sz = mTabLayout.getTabCount(); i < sz; ++i) {
                TabLayout.Tab tab = mTabLayout.getTabAt(i);
                if (tab != null) {
                    String tabTag = (String) tab.getTag();
                    if (tabTag != null && tabTag.equals(tag)) {
                        tab.select();
                        return;
                    }
                }
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * @return The current selected tab tag
     */
    protected String getCurrentTabTag() {
        if (mTabLayout == null) {
            return null;
        }
        int curPosition = mTabLayout.getSelectedTabPosition();
        if (curPosition != -1) {
            TabLayout.Tab tab = mTabLayout.getTabAt(curPosition);
            if (tab != null) {
                return (String) tab.getTag();
            }
        }

        return null;
    }

    /**
     * The overloaded implementation of {@link TabLayout.OnTabSelectedListener#onTabSelected(TabLayout.Tab)}.
     **/
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (sDebug) {
            Log.d(TAG, "Tab " + tab.getTag() + " is selected");
        }
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            // the document has not yet been ready, wait until it will be ready which will be notified
            // by PdfViewCtrlTabFragment.TabListener.onTabDocumentLoaded
            return;
        }

        String tabTag = (String) tab.getTag();
        if (tabTag != null) {
            setFragmentListeners(mTabLayout.getFragmentByTag(tabTag));
        }

        if (mTabHostListeners != null && mCurTabIndex != -1 && mCurTabIndex != tab.getPosition()) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onTabChanged(tabTag);
            }
            mQuitAppWhenDoneViewing = false;
        }
        mCurTabIndex = tab.getPosition();

        // reset last-used bookmark
        mCurrentBookmark = null;

        exitSearchMode();
        updateTabLayout();
        setToolbarsVisible(true, false);
        if (!currentFragment.isDocumentReady()) {
            // reset hide toolbars timer later when document is loaded
            stopHideToolbarsTimer();
        }

        // update print summary annotations modes
        updatePrintDocumentMode();
        updatePrintAnnotationsMode();
        updatePrintSummaryMode();

        // update view mode button icon
        updateButtonViewModeIcon();

        // update share button visibility
        updateShareButtonVisibility(true);

        // update buttons when in reflow mode
        updateIconsInReflowMode();
    }

    /**
     * The overloaded implementation of {@link TabLayout.OnTabSelectedListener#onTabUnselected(TabLayout.Tab)}.
     **/
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (sDebug) {
            Log.d(TAG, "Tab " + tab.getTag() + " is unselected");
        }
        String tabTag = (String) tab.getTag();
        if (tabTag != null) {
            removeFragmentListeners(mTabLayout.getFragmentByTag(tabTag));
        }
    }

    /**
     * The overloaded implementation of {@link TabLayout.OnTabSelectedListener#onTabReselected(TabLayout.Tab)}.
     **/
    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

    /**
     * The overloaded implementation of {@link ViewModePickerDialogFragment.ViewModePickerDialogFragmentListener#onViewModeColorSelected(int)}.
     **/
    @Override
    public boolean onViewModeColorSelected(int colorMode) {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }

        PdfViewCtrlSettingsManager.setColorMode(activity, colorMode);
        return updateColorMode();
    }

    /**
     * The overloaded implementation of {@link ViewModePickerDialogFragment.ViewModePickerDialogFragmentListener#onViewModeSelected(String)}.
     **/
    @Override
    public void onViewModeSelected(String viewMode) {
        onViewModeSelected(viewMode, false, null);
    }

    /**
     * Handles when view mode is selected.
     *
     * @param viewMode          the view mode
     * @param thumbnailEditMode True if thumbnail is in edit mode
     * @param checkedItem       The checked item
     */
    public void onViewModeSelected(String viewMode, boolean thumbnailEditMode, Integer checkedItem) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }
        final PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl == null) {
            return;
        }

        // Update per document view mode setting
        PDFViewCtrl.PagePresentationMode mode = PDFViewCtrl.PagePresentationMode.SINGLE;
        boolean updateViewMode = false;
        switch (viewMode) {
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_CONTINUOUS_VALUE:
                mode = PDFViewCtrl.PagePresentationMode.SINGLE_CONT;
                PdfViewCtrlSettingsManager.updateViewMode(activity, PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_CONTINUOUS_VALUE);
                updateViewMode = true;
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_SINGLEPAGE_VALUE:
                mode = PDFViewCtrl.PagePresentationMode.SINGLE;
                PdfViewCtrlSettingsManager.updateViewMode(activity, PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_SINGLEPAGE_VALUE);
                updateViewMode = true;
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACING_VALUE:
                mode = PDFViewCtrl.PagePresentationMode.FACING;
                PdfViewCtrlSettingsManager.updateViewMode(activity, PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACING_VALUE);
                updateViewMode = true;
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACINGCOVER_VALUE:
                mode = PDFViewCtrl.PagePresentationMode.FACING_COVER;
                PdfViewCtrlSettingsManager.updateViewMode(activity, PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACINGCOVER_VALUE);
                updateViewMode = true;
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACING_CONT_VALUE:
                mode = PDFViewCtrl.PagePresentationMode.FACING_CONT;
                PdfViewCtrlSettingsManager.updateViewMode(activity, PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACING_CONT_VALUE);
                updateViewMode = true;
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACINGCOVER_CONT_VALUE:
                mode = PDFViewCtrl.PagePresentationMode.FACING_COVER_CONT;
                PdfViewCtrlSettingsManager.updateViewMode(activity, PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACINGCOVER_CONT_VALUE);
                updateViewMode = true;
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_ROTATION_VALUE:
                pdfViewCtrl.rotateClockwise();
                try {
                    pdfViewCtrl.updatePageLayout();
                } catch (Exception e) {
                    AnalyticsHandlerAdapter.getInstance().sendException(e);
                }
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_THUMBNAILS_VALUE:
                onPageThumbnailOptionSelected(thumbnailEditMode, checkedItem);
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_USERCROP_VALUE:
                if (checkTabConversionAndAlert(R.string.cant_edit_while_converting_message, false)) {
                    return;
                }
                UserCropSelectionDialogFragment dialog = UserCropSelectionDialogFragment.newInstance();
                dialog.setUserCropSelectionDialogFragmentListener(this);
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    dialog.show(fragmentManager, "user_crop_mode_picker");
                }
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_REFLOWMODE:
                onToggleReflow();
                break;
            case PdfViewCtrlSettingsManager.KEY_PREF_RTLMODE:
                onToggleRtlMode();
                break;
        }

        if (updateViewMode) {
            if (currentFragment.isReflowMode()) {
                // Switch off reflow mode
                onToggleReflow();
            }
            // Update the PDFViewCtrl with the new mode
            currentFragment.updateViewMode(mode);

            // Update view mode button icon
            updateButtonViewModeIcon();
        }

        // Reset the toolbars timer
        resetHideToolbarsTimer();
    }

    /**
     * The overloaded implementation of {@link ViewModePickerDialogFragment.ViewModePickerDialogFragmentListener#onViewModePickerDialogFragmentDismiss()}.
     **/
    @Override
    public void onViewModePickerDialogFragmentDismiss() {
        resetHideToolbarsTimer();
    }

    /**
     * The overloaded implementation of {@link CustomColorModeDialogFragment.CustomColorModeSelectedListener#onCustomColorModeSelected(int, int)}.
     **/
    @Override
    public boolean onCustomColorModeSelected(int bgColor, int txtColor) {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }

        PdfViewCtrlSettingsManager.setCustomColorModeTextColor(activity, txtColor);
        PdfViewCtrlSettingsManager.setCustomColorModeBGColor(activity, bgColor);
        PdfViewCtrlSettingsManager.setColorMode(activity, PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_CUSTOM);

        return updateColorMode();
    }

    /**
     * The overloaded implementation of {@link BookmarksDialogFragment.BookmarksDialogListener#onBookmarksDialogDismissed(int)}.
     **/
    @Override
    public void onBookmarksDialogDismissed(int tabIndex) {
        resetHideToolbarsTimer();

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            currentFragment.setBookmarkDialogCurrentTab(tabIndex);
            currentFragment.resetHidePageNumberIndicatorTimer();
        }
    }

    /**
     * The overloaded implementation of {@link ViewModePickerDialogFragment.ViewModePickerDialogFragmentListener#onReflowZoomInOut(boolean)}.
     **/
    @Override
    public int onReflowZoomInOut(boolean flagZoomIn) {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();

        if (currentFragment == null) {
            return 0;
        }

        currentFragment.zoomInOutReflow(flagZoomIn);
        return currentFragment.getReflowTextSize();
    }

    /**
     * Whether to disable the auto hide Toolbar timer
     *
     * @param disable true if timer is disabled, false otherwise
     */
    public void setToolbarTimerDisabled(boolean disable) {
        mToolbarTimerDisabled = disable;
    }

    /**
     * The overloaded implementation of {@link PdfViewCtrlTabFragment.TabListener#resetHideToolbarsTimer()}.
     **/
    @Override
    public void resetHideToolbarsTimer() {
        stopHideToolbarsTimer();
        if (mToolbarTimerDisabled) {
            return;
        }
        if (mHideToolbarsHandler != null) {
            mHideToolbarsHandler.postDelayed(mHideToolbarsRunnable, HIDE_TOOLBARS_TIMER);
        }
    }

    /**
     * Stops timer for hiding toolbar.
     */
    public void stopHideToolbarsTimer() {
        if (mHideToolbarsHandler != null) {
            mHideToolbarsHandler.removeCallbacksAndMessages(null);
        }
    }

    private void resetHideNavigationBarTimer() {
        stopHideNavigationBarTimer();
        if (mHideNavigationBarHandler != null) {
            mHideNavigationBarHandler.postDelayed(mHideNavigationBarRunnable, HIDE_NAVIGATION_BAR_TIMER);
        }
    }

    private void stopHideNavigationBarTimer() {
        if (mHideNavigationBarHandler != null) {
            mHideNavigationBarHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Called when share option has been selected.
     */
    protected void onShareOptionSelected() {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        // Tries to save most recent changes before sharing
        if (!checkTabConversionAndAlert(R.string.cant_share_while_converting_message, true)) {
            currentFragment.save(false, true, true);
            currentFragment.handleOnlineShare();
        }
    }

    /**
     * Adds a new page.
     */
    public void addNewPage() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null || !currentFragment.isDocumentReady()) {
            return;
        }

        double pageWidth = 0;
        double pageHeight = 0;
        PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl != null) {
            boolean shouldUnlockRead = false;
            try {
                pdfViewCtrl.docLockRead();
                shouldUnlockRead = true;
                Page lastPage = pdfViewCtrl.getDoc().getPage(pdfViewCtrl.getDoc().getPageCount());
                if (lastPage == null)
                    return;
                pageWidth = lastPage.getPageWidth();
                pageHeight = lastPage.getPageHeight();
            } catch (Exception e) {
                AnalyticsHandlerAdapter.getInstance().sendException(e);
                return;
            } finally {
                if (shouldUnlockRead) {
                    pdfViewCtrl.docUnlockRead();
                }
            }
        }

        AddPageDialogFragment addPageDialogFragment = AddPageDialogFragment.newInstance(pageWidth, pageHeight)
                .setInitialPageSize(AddPageDialogFragment.PageSize.Custom);
        addPageDialogFragment.setOnAddNewPagesListener(new AddPageDialogFragment.OnAddNewPagesListener() {
            @Override
            public void onAddNewPages(Page[] pages) {
                if (pages == null || pages.length == 0) {
                    return;
                }

                currentFragment.onAddNewPages(pages);
            }
        });
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            addPageDialogFragment.show(fragmentManager, "add_page_overflow_menu");
        }
    }

    /**
     * Handles deleting the current page.
     */
    protected void requestDeleteCurrentPage() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null || !currentFragment.isDocumentReady()) {
            return;
        }

        final PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl == null) {
            return;
        }

        PDFDoc doc = pdfViewCtrl.getDoc();
        try {
            if (doc.getPageCount() < 2) {
                CommonToast.showText(activity, R.string.controls_thumbnails_view_delete_msg_all_pages);
                return;
            }
        } catch (PDFNetException e) {
            return;
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setTitle(R.string.action_delete_current_page);
        alertBuilder.setMessage(R.string.dialog_delete_current_page);
        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentFragment.onDeleteCurrentPage();
                dialog.dismiss();

            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertBuilder.setNeutralButton(R.string.action_delete_multiple, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onViewModeSelected(PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_THUMBNAILS_VALUE, true,
                        pdfViewCtrl.getCurrentPage());
            }
        });
        alertBuilder.create().show();
    }

    /**
     * Shows the rotate dialog.
     */
    protected void showRotateDialog() {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null || currentFragment == null || !currentFragment.isDocumentReady()) {
            return;
        }

        PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl != null) {
            RotateDialogFragment.newInstance()
                    .setPdfViewCtrl(pdfViewCtrl)
                    .show(fragmentManager, "rotate_dialog");
        }
    }


    /**
     * Called when view mode option has been selected.
     */
    protected void onViewModeOptionSelected() {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        // save the current page state for the back button - when the user
        // changes pages through grid view
        currentFragment.updateCurrentPageInfo();
        PDFViewCtrl.PagePresentationMode currentViewMode = PDFViewCtrl.PagePresentationMode.SINGLE_CONT;
        final PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl != null) {
            currentViewMode = pdfViewCtrl.getPagePresentationMode();
        }
        boolean isRtlMode = currentFragment.isRtlMode();
        boolean isReflowMode = currentFragment.isReflowMode();
        int reflowTextSize = currentFragment.getReflowTextSize();
        ArrayList<Integer> hiddenViewModeItems = new ArrayList<>();
        if (mViewerConfig != null && !mViewerConfig.isShowCropOption()) {
            hiddenViewModeItems.add(ViewModePickerDialogFragment.ITEM_ID_USERCROP);
        }
        ViewModePickerDialogFragment dialog =
                ViewModePickerDialogFragment.newInstance(
                        currentViewMode,
                        isRtlMode,
                        isReflowMode,
                        reflowTextSize,
                        hiddenViewModeItems
                );
        dialog.setViewModePickerDialogFragmentListener(this);
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomAppTheme);
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            dialog.show(fragmentManager, "view_mode_picker");
        }

        stopHideToolbarsTimer();
    }

    /**
     * Called when outline option has been selected.
     */
    @Override
    public void onOutlineOptionSelected() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null && currentFragment.isDocumentReady()) {
            onOutlineOptionSelected(currentFragment.getBookmarkDialogCurrentTab());
        }
    }

    /**
     * Called when outline option has been selected.
     *
     * @param initialTabIndex The tab index which should be selected after bookmarks dialog is created
     */
    public void onOutlineOptionSelected(int initialTabIndex) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }
        PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl == null) {
            return;
        }

        // save the current page state for the back button - when the user
        // changes pages through the annotation list or outline
        currentFragment.updateCurrentPageInfo();

        // Creates the dialog in full screen mode
        if (mBookmarksDialog != null) {
            mBookmarksDialog.dismiss();
        }
        mBookmarksDialog = createBookmarkDialogFragmentInstance();

        DialogFragmentTab userBookmarkTab = createUserBookmarkDialogTab();
        DialogFragmentTab outlineTab = createOutlineDialogTab();
        DialogFragmentTab annotationTab = createAnnotationDialogTab();
        ArrayList<DialogFragmentTab> dialogFragmentTabs = new ArrayList<>(3);
        if (userBookmarkTab != null) {
            boolean canAdd = mViewerConfig == null || mViewerConfig.isShowUserBookmarksList();
            if (canAdd) {
                dialogFragmentTabs.add(userBookmarkTab);
            }
        }
        if (outlineTab != null) {
            boolean canAdd = mViewerConfig == null || mViewerConfig.isShowOutlineList();
            if (canAdd) {
                dialogFragmentTabs.add(outlineTab);
            }
        }
        if (annotationTab != null) {
            boolean canAdd = mViewerConfig == null || mViewerConfig.isShowAnnotationsList();
            if (canAdd) {
                dialogFragmentTabs.add(annotationTab);
            }
        }

        mBookmarksDialog.setPdfViewCtrl(pdfViewCtrl)
                .setDialogFragmentTabs(dialogFragmentTabs, initialTabIndex)
                .setCurrentBookmark(mCurrentBookmark);
        mBookmarksDialog.setBookmarksDialogListener(this);
        mBookmarksDialog.setBookmarksTabsListener(this);
        mBookmarksDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomAppTheme);
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            mBookmarksDialog.show(fragmentManager, "bookmarks_dialog");
        }

        stopHideToolbarsTimer();
    }

    /**
     * Creates an instance of {@link BookmarksDialogFragment}.
     *
     * @return an instance of {@link BookmarksDialogFragment}
     */
    protected BookmarksDialogFragment createBookmarkDialogFragmentInstance() {
        return BookmarksDialogFragment.newInstance();
    }

    /**
     * Creates the user bookmark dialog fragment tab
     *
     * @return The user bookmark dialog fragment tab
     */
    protected DialogFragmentTab createUserBookmarkDialogTab() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return null;
        }

        PDFViewCtrl pdfViewCtrl = currentFragment.getPDFViewCtrl();
        if (pdfViewCtrl == null) {
            return null;
        }

        Bundle bundle = new Bundle();
        boolean readonly = currentFragment.isTabReadOnly();
        if (!readonly && mViewerConfig != null && !mViewerConfig.isUserBookmarksListEditingEnabled()) {
            // if document is editable, user can specify if a particular control is editable
            readonly = true;
        }
        bundle.putBoolean(UserBookmarkDialogFragment.BUNDLE_IS_READ_ONLY, readonly);
        return new DialogFragmentTab(UserBookmarkDialogFragment.class,
                BookmarksTabLayout.TAG_TAB_BOOKMARK,
                Utils.getDrawable(getContext(), R.drawable.ic_bookmarks_white_24dp),
                null,
                getString(R.string.bookmark_dialog_fragment_bookmark_tab_title),
                bundle);
    }

    /**
     * Creates the outline dialog fragment tab
     *
     * @return The outline dialog fragment tab
     */
    protected DialogFragmentTab createOutlineDialogTab() {
        return new DialogFragmentTab(OutlineDialogFragment.class,
                BookmarksTabLayout.TAG_TAB_OUTLINE,
                Utils.getDrawable(getContext(), R.drawable.ic_outline_white_24dp),
                null,
                getString(R.string.bookmark_dialog_fragment_outline_tab_title),
                null);
    }

    /**
     * Creates the annotation dialog fragment tab
     *
     * @return The annotation dialog fragment tab
     */
    protected DialogFragmentTab createAnnotationDialogTab() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        final Context context = getContext();
        if (currentFragment == null || context == null) {
            return null;
        }

        Bundle bundle = new Bundle();
        boolean readonly = currentFragment.isTabReadOnly();
        if (!readonly && mViewerConfig != null && !mViewerConfig.annotationsListEditingEnabled()) {
            // if document is editable, user can specify if a particular control is editable
            readonly = true;
        }
        bundle.putBoolean(AnnotationDialogFragment.BUNDLE_IS_READ_ONLY, readonly);
        bundle.putBoolean(AnnotationDialogFragment.BUNDLE_IS_RTL, currentFragment.isRtlMode());
        bundle.putInt(AnnotationDialogFragment.BUNDLE_KEY_SORT_MODE,
                PdfViewCtrlSettingsManager.getAnnotListSortOrder(context,
                        AnnotationListSortOrder.DATE_ASCENDING) // default sort order
        );
        return new DialogFragmentTab(AnnotationDialogFragment.class,
                BookmarksTabLayout.TAG_TAB_ANNOTATION,
                Utils.getDrawable(context, R.drawable.ic_annotations_white_24dp),
                null,
                getString(R.string.bookmark_dialog_fragment_annotation_tab_title),
                bundle);
    }

    /**
     * Called when list all option has been selected.
     */
    protected void onListAllOptionSelected(String searchQuery) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        if (mSearchResultsView != null && mSearchResultsView.getVisibility() == View.VISIBLE) {
            hideSearchResults();
        } else if (!Utils.isNullOrEmpty(searchQuery)) {
            showSearchResults(searchQuery);
        }
    }

    /**
     * Called when search match case option has been selected.
     *
     * @param isChecked True if checked
     */
    protected void onSearchMatchCaseOptionSelected(boolean isChecked) {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }
        currentFragment.setSearchMatchCase(isChecked);
        currentFragment.resetFullTextResults();

        if (mSearchResultsView == null) {
            return;
        }
        if (mSearchResultsView.getDoc() == null || mSearchResultsView.getDoc() != currentFragment.getPdfDoc()) {
            mSearchResultsView.setPdfViewCtrl(currentFragment.getPDFViewCtrl());
        }
        mSearchResultsView.setMatchCase(isChecked);
    }

    /**
     * Called when search whole word option has been selected.
     *
     * @param isChecked True if checked
     */
    protected void onSearchWholeWordOptionSelected(boolean isChecked) {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }
        currentFragment.setSearchWholeWord(isChecked);
        currentFragment.resetFullTextResults();

        if (mSearchResultsView == null) {
            return;
        }
        if (mSearchResultsView.getDoc() == null || mSearchResultsView.getDoc() != currentFragment.getPdfDoc()) {
            mSearchResultsView.setPdfViewCtrl(currentFragment.getPDFViewCtrl());
        }
        mSearchResultsView.setWholeWord(isChecked);
    }

    /**
     * The overloaded implementation of {@link AnnotationToolbar.AnnotationToolbarListener#onAnnotationToolbarShown()}.
     **/
    @Override
    public void onAnnotationToolbarShown() {
        mWillShowAnnotationToolbar = false;
    }

    /**
     * The overloaded implementation of {@link AnnotationToolbar.AnnotationToolbarListener#onAnnotationToolbarClosed()}.
     **/
    @Override
    public void onAnnotationToolbarClosed() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        setToolbarsVisible(true);
        showSystemUI();
    }

    /**
     * The overloaded implementation of {@link AnnotationToolbar.AnnotationToolbarListener#onShowAnnotationToolbarByShortcut(int)}.
     **/
    @Override
    public void onShowAnnotationToolbarByShortcut(int mode) {
        showAnnotationToolbar(mode, null, null);
    }

    /**
     * Shows an annotation toolbar starting with the certain mode and selected (ink) annotation
     *
     * @param mode     The mode that annotation toolbar should start with. Possible values are
     *                 {@link AnnotationToolbar#START_MODE_NORMAL_TOOLBAR},
     *                 {@link AnnotationToolbar#START_MODE_EDIT_TOOLBAR}
     * @param inkAnnot The selected (ink) annotation
     * @return <code>true</code> if annotation toolbar is shown; <code>false</code> otherwise
     */
    protected boolean showAnnotationToolbar(final int mode, final Annot inkAnnot, final ToolMode toolMode) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return false;
        }

        currentFragment.localFileWriteAccessCheck();

        if (checkTabConversionAndAlert(R.string.cant_edit_while_converting_message, false)) {
            return false;
        }

        mWillShowAnnotationToolbar = true;

        // should force top toolbars hide since they should be replaced with annotation toolbar even for large screen devices
        boolean autoHideEnabled = mAutoHideEnabled;
        mAutoHideEnabled = true;
        setToolbarsVisible(false);
        mAutoHideEnabled = autoHideEnabled;

//        if (Utils.isLollipop()) {
//            showSystemStatusBar();
//        } else {
//            showSystemUI();
//        }

        hideSystemUI();
        // Showing the annotation toolbar should be after hiding the other toolbars.
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Ensure that system UI is not hidden by the timer.
                stopHideToolbarsTimer();

                currentFragment.showAnnotationToolbar(mode, inkAnnot, toolMode, !currentFragment.isAnnotationMode());
            }
        }, ANIMATE_DURATION_HIDE);
        return true;
    }

    /**
     * The overloaded implementation of {@link ToolManager.QuickMenuListener#onQuickMenuClicked(QuickMenuItem)}.
     */
    @Override
    public boolean onQuickMenuClicked(QuickMenuItem menuItem) {
        hideUI();
        if (menuItem.getItemId() == R.id.qm_free_text) {
            showSystemStatusBar();
        }
        return false;
    }

    @Override
    public boolean onShowQuickMenu(QuickMenu quickmenu, Annot annot) {
        return false;
    }

    /**
     * The overloaded implementation of {@link ToolManager.QuickMenuListener#onQuickMenuShown()}.
     **/
    @Override
    public void onQuickMenuShown() {
        hideUI();
    }

    /**
     * The overloaded implementation of {@link ToolManager.QuickMenuListener#onQuickMenuDismissed()}.
     **/
    @Override
    public void onQuickMenuDismissed() {
        hideUI();
    }

    /**
     * The overloaded implementation of {@link ViewModePickerDialogFragment.ViewModePickerDialogFragmentListener#checkTabConversionAndAlert(int, boolean)}.
     **/
    @Override
    public boolean checkTabConversionAndAlert(int messageID, boolean allowConverted) {
        return checkTabConversionAndAlert(messageID, allowConverted, false);
    }

    /**
     * Checks tab conversion and shows the alert.
     *
     * @param messageID            The message ID
     * @param allowConverted       True if conversion is allowed
     * @param skipSpecialFileCheck True if spcecial files should be skipped
     * @return True if handled
     */
    public boolean checkTabConversionAndAlert(int messageID, boolean allowConverted, boolean skipSpecialFileCheck) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        return currentFragment != null && currentFragment.checkTabConversionAndAlert(messageID, allowConverted, skipSpecialFileCheck);
    }

    /**
     * Handles exporting pages.
     *
     * @param folder    The file folder to put the new document
     * @param positions The page positions to be exported
     */
    protected void handleThumbnailsExport(File folder, SparseBooleanArray positions) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        PDFDoc newDoc = null;
        boolean error = true;
        boolean shouldUnlock = false;
        try {
            newDoc = exportPages(getPageSet(positions));
            if (newDoc != null) {
                newDoc.lock();
                shouldUnlock = true;
                File tempFile = new File(folder.getAbsolutePath(), currentFragment.getTabTitle() + " export.pdf");
                String filename = Utils.getFileNameNotInUse(tempFile.getAbsolutePath());
                File outputFile = new File(filename);
                newDoc.save(filename, SDFDoc.SaveMode.REMOVE_UNUSED, null);
                showExportPagesSuccess(BaseFileInfo.FILE_TYPE_FILE, filename, outputFile.getName());
                error = false;
            }
        } catch (PDFNetException e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlock) {
                Utils.unlockQuietly(newDoc);
            }
            Utils.closeQuietly(newDoc);
        }
        if (error) {
            Utils.showAlertDialog(activity, getString(R.string.error_export_file), getString(R.string.error));
        }
    }

    /**
     * Handles exporting pages.
     *
     * @param folder    The external file folder to put the new document
     * @param positions The page positions to be exported
     */
    protected void handleThumbnailsExport(ExternalFileInfo folder, SparseBooleanArray positions) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        String filename = Utils.getFileNameNotInUse(folder, currentFragment.getTabTitle() + " export.pdf");
        if (folder == null || Utils.isNullOrEmpty(filename)) {
            Utils.showAlertDialog(activity, getString(R.string.error_export_file), getString(R.string.error));
            return;
        }
        ExternalFileInfo file = folder.createFile("application/pdf", filename);
        if (file == null) {
            return;
        }
        boolean error = true;
        PDFDoc newDoc = null;
        SecondaryFileFilter filter = null;
        try {
            newDoc = exportPages(getPageSet(positions));
            if (newDoc != null) {
                filter = new SecondaryFileFilter(activity, file.getUri());
                newDoc.save(filter, SDFDoc.SaveMode.REMOVE_UNUSED);
                showExportPagesSuccess(BaseFileInfo.FILE_TYPE_EXTERNAL, file.getUri().toString(), file.getName());
                error = false;
            }
        } catch (PDFNetException | IOException e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            Utils.closeQuietly(newDoc, filter);
        }
        if (error) {
            Utils.showAlertDialog(activity, getString(R.string.error_export_file), getString(R.string.error));
        }
    }

    /**
     * Exports pages.
     *
     * @param pageSet The page set to be exported
     * @return The new pdf doc with exported pages
     * @throws PDFNetException PDFNet Exception
     */
    protected PDFDoc exportPages(PageSet pageSet) throws PDFNetException {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            PDFDoc newDoc = new PDFDoc();
            newDoc.insertPages(0, currentFragment.getPdfDoc(), pageSet, PDFDoc.InsertBookmarkMode.NONE, null);
            return newDoc;
        }
        return null;
    }

    /**
     * Converts page positions to the page set
     *
     * @param positions The page positions as boolean
     * @return The page set
     */
    static private PageSet getPageSet(SparseBooleanArray positions) {
        PageSet set = new PageSet();
        int rangeBegin = -1;
        int rangeEnd = -1;

        for (int i = 0; i < positions.size(); i++) {
            int key = positions.keyAt(i);
            boolean isSelected = positions.get(key);
            int page = key + 1;
            if (isSelected) {
                if (rangeBegin < 0) {
                    rangeBegin = page;
                    rangeEnd = page;
                } else if (rangeBegin > 0) {
                    if (rangeEnd + 1 == page) {
                        rangeEnd++;
                    } else {
                        set.addRange(rangeBegin, rangeEnd);
                        rangeBegin = rangeEnd = page;
                    }
                }
            } else {
                if (rangeBegin > 0) {
                    set.addRange(rangeBegin, rangeEnd);
                    rangeBegin = -1;
                    rangeEnd = -1;
                }
            }
        }

        if (rangeBegin > 0) {
            set.addRange(rangeBegin, rangeEnd);
        }

        return set;
    }

    /**
     * Lets the user know the pages have been successfully exported
     *
     * @param itemSource The item source of the file
     * @param tag        The tab tag
     * @param filename   The file name
     */
    protected void showExportPagesSuccess(final int itemSource, final String tag, final String filename) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        AlertDialog.Builder successDialogBuilder = Utils.getAlertDialogBuilder(activity, "", "");
        successDialogBuilder.setNegativeButton(R.string.open, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onOpenAddNewTab(itemSource, tag, filename, "");
                mThumbFragment.dismiss();
            }
        });
        successDialogBuilder.setPositiveButton(R.string.ok, null);
        successDialogBuilder.setMessage(Html.fromHtml(getString(R.string.export_success, filename)));
        successDialogBuilder.create().show();
    }

    protected void adjustConfiguration() {
        Activity activity = getActivity();
        if (null == activity || null == mViewerConfig) {
            return;
        }
        PdfViewCtrlSettingsManager.setFullScreenMode(activity, mViewerConfig.isFullscreenModeEnabled());
        PdfViewCtrlSettingsManager.setMultipleTabs(activity, mViewerConfig.isMultiTabEnabled());
        mMultiTabModeEnabled = mViewerConfig.isMultiTabEnabled();
        setTabLayoutVisible(mMultiTabModeEnabled);
    }

    private void onToggleRtlMode() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null) {
            currentFragment.toggleRtlMode();
        }
    }

    private void updateTabLayout() {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return;
        }

        // push the PDFView down below the toolbar in large screens
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        boolean isLargeScreen = Utils.isLargeScreen(activity);
        params.addRule(RelativeLayout.BELOW, isLargeScreen ? R.id.app_bar_layout : R.id.parent);
        mFragmentContainer.setLayoutParams(params);
        mAutoHideEnabled = !isLargeScreen;
        if (!mAutoHideEnabled) {
            showUI();
        }

        if (getMaxTabCount() <= PdfViewCtrlTabsManager.MAX_NUM_TABS_PHONE) {
            mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        } else {
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

        final int tabCount = mTabLayout.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                View view = tab.getCustomView();
                if (view != null) {
                    ImageButton button = view.findViewById(R.id.tab_pdfviewctrl_close_button);
                    if (button != null) {
                        // Set close button icon's tint colour according to its current state (selected or not).
                        ColorStateList tint = AppCompatResources.getColorStateList(activity, R.color.selector_tab_color_fg);
                        button.setColorFilter(tint.getColorForState(button.getDrawableState(), tint.getDefaultColor()), PorterDuff.Mode.SRC_IN);

                        if (!Utils.isTablet(getContext()) && Utils.isPortrait(getContext()) && !tab.isSelected()) {
                            button.setVisibility(View.GONE);
                        } else {
                            button.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates color mode.
     *
     * @return True if the view mode picker dialog should be dismissed
     */
    public boolean updateColorMode() {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return false;
        }

        if (canRecreateActivity() && activity instanceof AppCompatActivity && Utils.applyDayNight((AppCompatActivity) activity)) {
            return true;
        }

        ArrayList<Fragment> fragments = mTabLayout.getLiveFragments();
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        for (Fragment fragment : fragments) {
            if (fragment instanceof PdfViewCtrlTabFragment) {
                PdfViewCtrlTabFragment pdfViewCtrlTabFragment = (PdfViewCtrlTabFragment) fragment;
                if (fragment == currentFragment) {
                    pdfViewCtrlTabFragment.updateColorMode();
                } else {
                    pdfViewCtrlTabFragment.setColorModeChanged();
                }
            }
        }
        return false;
    }

    private void updatePrintDocumentMode() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.updatePrintDocumentMode(PdfViewCtrlSettingsManager.isPrintDocumentMode(activity));
    }

    private void updatePrintAnnotationsMode() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.updatePrintAnnotationsMode(PdfViewCtrlSettingsManager.isPrintAnnotationsMode(activity));
    }

    private void updatePrintSummaryMode() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.updatePrintSummaryMode(PdfViewCtrlSettingsManager.isPrintSummaryMode(activity));
    }

    /**
     * Updates the icons (enable/disable) when reflow mode has been changed.
     */
    protected void updateIconsInReflowMode() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }
        if (currentFragment.isReflowMode()) {
            if (mMenuAnnotToolbar != null && mMenuAnnotToolbar.getIcon() != null) {
                mMenuAnnotToolbar.getIcon().setAlpha(150);
            }
            if (mMenuSearch != null && mMenuSearch.getIcon() != null) {
                mMenuSearch.getIcon().setAlpha(150);
            }
        } else {
            if (mMenuAnnotToolbar != null && mMenuAnnotToolbar.getIcon() != null) {
                mMenuAnnotToolbar.getIcon().setAlpha(255);
            }
            if (mMenuSearch != null && mMenuSearch.getIcon() != null) {
                mMenuSearch.getIcon().setAlpha(255);
            }
        }
    }

    private void updateButtonViewModeIcon() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        if (mMenuViewMode != null) {
            if (currentFragment.isContinuousPageMode()) {
                // update icon here if desired
            } else {
                // update icon here if desired
            }
        }
    }

    /**
     * Shows the UI.
     */
    // TODO: Rename.
    public void showUI() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        final boolean canShowToolbars = currentFragment.onShowToolbar();
        final boolean canExitFullscreenMode = currentFragment.onExitFullscreenMode();
        final boolean isThumbSliderVisible = currentFragment.isThumbSliderVisible();
        final boolean isAnnotationMode = currentFragment.isAnnotationMode();

        // Toolbars can only be shown if fullscreen mode will be exited.
        if (!isThumbSliderVisible && canShowToolbars && canExitFullscreenMode) {
            setToolbarsVisible(true);
        }

        if (!isAnnotationMode && canExitFullscreenMode) {
            showSystemUI();
        }
    }

    /**
     * Hides the UI.
     */
    // TODO: Rename.
    public void hideUI() {
        if (mViewerConfig != null && !mViewerConfig.isAutoHideToolbarEnabled()) {
            setThumbSliderVisibility(false, true);
        } else {
            Activity activity = getActivity();
            final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
            if (activity == null || currentFragment == null) {
                return;
            }

            final boolean canHideToolbars = currentFragment.onHideToolbars();
            final boolean canEnterFullscreenMode = currentFragment.onEnterFullscreenMode();
            final boolean isThumbSliderVisible = currentFragment.isThumbSliderVisible();
            final boolean isAnnotationMode = currentFragment.isAnnotationMode();

            if (isThumbSliderVisible && canHideToolbars) {
                setToolbarsVisible(false);
            }

            // Fullscreen mode only be entered if the toolbars will hide or if they are not visible.
            if ((isThumbSliderVisible && canHideToolbars && canEnterFullscreenMode)
                    || (!isThumbSliderVisible && canEnterFullscreenMode)) {
                if (isAnnotationMode) {
                    showSystemStatusBar();
                } else {
                    hideSystemUI();
                }
            }
        }
    }

    /**
     * Handles changing the visibility of toolbars.
     *
     * @param visible True if toolbar is visible
     */
    @Override
    public void setToolbarsVisible(boolean visible) {
        setToolbarsVisible(visible, true);
    }

    @Override
    public void setViewerOverlayUIVisible(boolean visible) {
        if (!mAutoHideEnabled) {
            return;
        }
        if (visible) {
            showUI();
        } else {
            hideUI();
        }
    }

    /**
     * Handles changing the visibility of toolbars.
     *
     * @param visible            True if toolbar is visible
     * @param animateThumbSlider True if visibility should be changed with animation
     */
    public void setToolbarsVisible(boolean visible, boolean animateThumbSlider) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        boolean isAnnotationMode = currentFragment != null && currentFragment.isAnnotationMode();
        if (isAnnotationMode || mIsSearchMode) {
            // Do nothing if in annotation or search mode.
            return;
        }

        if (visible) {
            resetHideToolbarsTimer();
            if (currentFragment != null) {
                currentFragment.resetHidePageNumberIndicatorTimer();
            }
        } else {
            stopHideToolbarsTimer();
            if (currentFragment != null) {
                currentFragment.hidePageNumberIndicator();
            }
        }
        if (visible || mAutoHideEnabled) {
            animateToolbars(visible);
        }
        setThumbSliderVisibility(visible, animateThumbSlider);
    }

    /**
     * Sets the visibility of tab layout.
     *
     * @param visible True if visible
     */
    protected void setTabLayoutVisible(boolean visible) {
        Activity activity = getActivity();
        if (activity == null || mTabLayout == null) {
            return;
        }

        boolean canHide = mAutoHideEnabled || mIsSearchMode;

        visible |= !canHide; // always show tab layout for large screen devices

        if (!mMultiTabModeEnabled) {
            // Ensure that tab layout is not shown when multi-tab mode is disabled.
            if (mTabLayout.getVisibility() == View.VISIBLE) {
                mTabLayout.setVisibility(View.GONE);
            }
        } else if ((mTabLayout.getVisibility() == View.VISIBLE) != visible) {
            // Requested visibility is different from current value.
            mTabLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Shows the system status bar.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void showSystemStatusBar() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        View view = getView();
        if (activity == null || currentFragment == null || view == null) {
            return;
        }
        if (Utils.isKitKat() && PdfViewCtrlSettingsManager.getFullScreenMode(activity)) {
            int oldFlags = view.getSystemUiVisibility();
            int newFlags = oldFlags;

            // Remove the system UI flag to hide the status bar.
            newFlags &= ~(View.SYSTEM_UI_FLAG_FULLSCREEN);

            // Add the system UI flags to hide the navigation bar.
            // (Sticky immersion means the navigation bar can be "peeked").
            newFlags |= (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            if (newFlags != oldFlags) {
                view.setSystemUiVisibility(newFlags);
                view.requestLayout(); // Force a layout invalidation.
            }
        }
    }

    /**
     * Shows the system UI.
     */
    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void showSystemUI() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        View view = getView();
        if (activity == null || currentFragment == null || view == null) {
            return;
        }

        if (Utils.isKitKat() && PdfViewCtrlSettingsManager.getFullScreenMode(activity)) {
            int oldFlags = view.getSystemUiVisibility();
            int newFlags = oldFlags;

            // Remove the fullscreen system UI flags.
            newFlags &= ~(View.SYSTEM_UI_FLAG_FULLSCREEN // show status bar
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // show nav bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            if (newFlags != oldFlags) {
                view.setSystemUiVisibility(newFlags);
                view.requestLayout(); // Force a layout invalidation.
            }
        }

        if (sDebug)
            Log.d(TAG, "show system UI called");
    }

    /**
     * Hides the system UI.
     */
    // This snippet hides the system bars.
    // http://stackoverflow.com/a/33551538
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void hideSystemUI() {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        View view = getView();
        if (activity == null || currentFragment == null || view == null) {
            return;
        }

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        if (Utils.isKitKat() && PdfViewCtrlSettingsManager.getFullScreenMode(activity)) {
            int oldFlags = view.getSystemUiVisibility();
            int newFlags = oldFlags;

            // Add the fullscreen system UI flags.
            newFlags |= (View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);

            if (newFlags != oldFlags) {
                view.setSystemUiVisibility(newFlags);
                view.requestLayout(); // Force a layout invalidation.
            }
        }

        if (sDebug)
            Log.d(TAG, "hide system UI called");
    }

    /**
     * Update the system UI layout flags as appropriate for the current
     * fullscreen mode setting.
     * <p>
     * NOTE:
     * The {@link AppBarLayout} can only request stable system window insets when in fullscreen mode.
     * This is due to an apparent issue with {@link android.support.design.widget.CoordinatorLayout}
     * where if the CoordinatorLayout is not receiving stable insets (as required for the tab
     * fragments' root-level CoordinatorLayout), {@link View#requestLayout()} becomes broken
     * (All layouts passes skip the CoordinatorLayout and its descendants).
     */
    private void updateFullScreenModeLayout() {
        Activity activity = getActivity();
        final View view = getView();
        if (activity == null || view == null || mAppBarLayout == null) {
            return;
        }

        if (Utils.isKitKat()) {
            int oldRootFlags = view.getSystemUiVisibility();
            int newRootFlags = oldRootFlags;

            int oldAppBarFlags = mAppBarLayout.getSystemUiVisibility();
            int newAppBarFlags = oldAppBarFlags;

            if (PdfViewCtrlSettingsManager.getFullScreenMode(activity)) {
                // Add the fullscreen system UI layout flags.
                newRootFlags |= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

                // Add the stable layout flag.
                newAppBarFlags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                // Remove the fullscreen system UI layout flags.
                newRootFlags &= ~(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

                // Remove the stable layout flag.
                newAppBarFlags &= ~View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            }

            view.setSystemUiVisibility(newRootFlags);
            mAppBarLayout.setSystemUiVisibility(newAppBarFlags);

            // View has an internal check for whether the flags changed,
            // but we need our own to prevent unnecessary requestLayout()'s.
            if (newRootFlags != oldRootFlags || newAppBarFlags != oldAppBarFlags) {
                view.requestLayout(); // Force a layout invalidation.
            }
        }

        // Request a new dispatch of system window insets.
        ViewCompat.requestApplyInsets(view);
    }

    public void startSearchMode() {
        FragmentActivity activity = getActivity();
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null || mTabLayout == null) {
            return;
        }
        if (mToolbar == null || mSearchToolbar == null) {
            return;
        }


        Transition fade = new Fade();
        TransitionManager.beginDelayedTransition(mToolbar, fade);
        mToolbar.setVisibility(View.GONE);

        TransitionManager.beginDelayedTransition(mSearchToolbar, fade);
        mSearchToolbar.setVisibility(View.VISIBLE);

        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onStartSearchMode();
            }
        }

        // hide tab widget
        setToolbarsVisible(true);
        setTabLayoutVisible(false);
        setThumbSliderVisibility(false, true);
        stopHideToolbarsTimer();
        setSearchNavButtonsVisible(true);

        mIsSearchMode = true;
        currentFragment.setSearchMode(true);

        currentFragment.hideBackAndForwardButtons();

        if (!Utils.isLargeScreen(activity)) {
            mSearchToolbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int toolbarHeight = mSearchToolbar.getMeasuredHeight();
            currentFragment.setViewerTopMargin(toolbarHeight + mSystemWindowInsetTop);
        }
    }

    /**
     * Exits the search mode.
     */
    public void exitSearchMode() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (!mIsSearchMode || currentFragment == null || mTabLayout == null) {
            return;
        }

        mIsSearchMode = false;
        currentFragment.setSearchMode(false);

        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onExitSearchMode();
            }
        }

        Transition fade = new Fade();
        if (mSearchToolbar != null) {
            TransitionManager.beginDelayedTransition(mSearchToolbar, fade);
            mSearchToolbar.setVisibility(View.GONE);
        }
        if (mToolbar != null) {
            TransitionManager.beginDelayedTransition(mToolbar, fade);
            mToolbar.setVisibility(View.VISIBLE);
        }

        // show tab widget
        setTabLayoutVisible(true);
        setToolbarsVisible(true);
        if (mSearchToolbar != null) {
            mSearchToolbar.setSearchProgressBarVisible(false);
        }
        setSearchNavButtonsVisible(false);

        // Cancel search and hide progress bar
        currentFragment.cancelFindText();

        currentFragment.exitSearchMode();
        setThumbSliderVisibility(true, true);
        // Dismiss and reset full doc search
        if (mSearchResultsView != null) {
            hideSearchResults();
            mSearchResultsView.reset();
        }

        currentFragment.setViewerTopMargin(0);
    }

    /**
     * Hides the search results.
     */
    protected void hideSearchResults() {
        if (mSearchResultsView != null) {
            mSearchResultsView.setVisibility(View.GONE);
        }
    }

    private void showSearchResults(String searchQuery) {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return;
        }

        if (mSearchResultsView == null) {
            mSearchResultsView = inflateSearchResultsView(this);
        }
        if (mSearchResultsView != null) {
            if (mSearchResultsView.getDoc() == null || mSearchResultsView.getDoc() != currentFragment.getPdfDoc()) {
                mSearchResultsView.setPdfViewCtrl(currentFragment.getPDFViewCtrl());
            }

            mSearchResultsView.setVisibility(View.VISIBLE);
            mSearchResultsView.findText(searchQuery);

            onShowSearchResults(searchQuery);
        }
    }

    private void onShowSearchResults(String searchQuery) {
        PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (mSearchResultsView == null || currentFragment == null) {
            return;
        }

        mSearchResultsView.requestFocus();

        currentFragment.setSearchQuery(searchQuery);
        currentFragment.highlightSearchResults();
    }

    private SearchResultsView inflateSearchResultsView(SearchResultsView.SearchResultsListener listener) {
        View view = getView();
        if (view == null) {
            return null;
        }

        ViewStub stub = view.findViewById(R.id.controls_search_results_stub);
        if (stub != null) {
            SearchResultsView searchResultsView = (SearchResultsView) stub.inflate();
            CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) searchResultsView.getLayoutParams();
            clp.setBehavior(new PaneBehavior());
            clp.gravity = PaneBehavior.getGravityForOrientation(getContext(), getResources().getConfiguration().orientation);
            if (Utils.isLollipop()) {
                searchResultsView.setElevation(getResources().getDimension(R.dimen.actionbar_elevation));
            }
            searchResultsView.setSearchResultsListener(listener);
            return searchResultsView;
        }
        return null;
    }

    private void adjustShareButtonShowAs(Activity activity) {
        if (mMenuShare == null) {
            return;
        }
        if (Utils.isScreenTooNarrow(activity)) {
            int count = Utils.toolbarIconMaxCount(activity);
            if (count >= MAX_TOOLBAR_ICON_COUNT) {
                mMenuShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                mMenuShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        } else {
            mMenuShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    /**
     * Updates the visibility of the share button
     *
     * @param visible True if visible
     */
    protected void updateShareButtonVisibility(boolean visible) {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment != null && mMenuShare != null) {
            mMenuShare.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowShareOption()));
        }
    }

    /**
     * Updates the visibility of the close tab button
     *
     * @param visible True if visible
     */
    protected void updateCloseTabButtonVisibility(boolean visible) {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        if (mMenuCloseTab != null) {
            if (!PdfViewCtrlSettingsManager.getMultipleTabs(activity)) {
                mMenuCloseTab.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowCloseTabOption()));
            } else {
                mMenuCloseTab.setVisible(false);
            }
        }
    }

    /**
     * Sets the visibility of options menu
     *
     * @param visible True if visible
     */
    protected void setOptionsMenuVisible(boolean visible) {
        if (mMenuSearch != null) {
            mMenuSearch.setVisible(mViewerConfig == null || mViewerConfig.isShowSearchView());
        }
        if (mMenuUndo != null) {
            mMenuUndo.setVisible(visible);
        }
        if (mMenuRedo != null) {
            mMenuRedo.setVisible(visible);
        }
        if (mMenuShare != null) {
            mMenuShare.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowShareOption()));
        }
        if (mMenuAnnotToolbar != null) {
            mMenuAnnotToolbar.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowAnnotationToolbarOption()));
        }
        if (mMenuViewMode != null) {
            mMenuViewMode.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowDocumentSettingsOption()));
        }
        if (mMenuPrint != null) {
            mMenuPrint.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowPrintOption()));
        }
        if (mMenuEditPages != null) {
            mMenuEditPages.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowEditPagesOption()));
        }
        if (mMenuExport != null) {
            mMenuExport.setVisible(visible && (mViewerConfig == null || mViewerConfig.isShowSaveCopyOption()));
        }

        updateShareButtonVisibility(visible);
        updateCloseTabButtonVisibility(visible);
        updateIconsInReflowMode();
    }

    private void setSearchNavButtonsVisible(boolean visible) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || currentFragment == null) {
            return;
        }

        currentFragment.setSearchNavButtonsVisible(visible);
    }

    /**
     * Handles when opening file has been failed.
     *
     * @param errorCode The error code
     */
    protected void handleOpenFileFailed(int errorCode) {
        handleOpenFileFailed(errorCode, "");
    }

    /**
     * Handles when opening file has been failed.
     *
     * @param errorCode The error code
     * @param info      The extra information
     */
    protected void handleOpenFileFailed(int errorCode, String info) {
        Activity activity = getActivity();
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (activity == null || activity.isFinishing() || currentFragment == null) {
            return;
        }

        int messageId = R.string.error_opening_doc_message;
        boolean shouldShowErrorMessage = true;
        switch (errorCode) {
            case PdfDocManager.DOCUMENT_SETDOC_ERROR_ZERO_PAGE:
                messageId = R.string.error_empty_file_message;
                break;
            case PdfDocManager.DOCUMENT_SETDOC_ERROR_OPENURL_CANCELLED:
                messageId = R.string.download_cancelled_message;
                break;
            case PdfDocManager.DOCUMENT_SETDOC_ERROR_WRONG_PASSWORD:
                messageId = R.string.password_not_valid_message;
                break;
            case PdfDocManager.DOCUMENT_SETDOC_ERROR_NOT_EXIST:
                messageId = R.string.file_does_not_exist_message;
                break;
            case PdfDocManager.DOCUMENT_SETDOC_ERROR_DOWNLOAD_CANCEL:
                messageId = R.string.download_size_cancelled_message;
                break;
            case PdfDocManager.DOCUMENT_ERROR_MISSING_PERMISSIONS:
                shouldShowErrorMessage = false; //  we don't want to show an error for missing permissions, just close tabs and clear cache
                break;
        }

        if (shouldShowErrorMessage) {
            String message = getString(messageId);
            if (mQuitAppWhenDoneViewing) {
                CommonToast.showText(activity, message, Toast.LENGTH_LONG);
            } else {
                String title = currentFragment.getTabTitle();
                title = shortenTitle(title);
                Utils.showAlertDialog(activity, message, title);
            }
        }

        if (errorCode != PdfDocManager.DOCUMENT_SETDOC_ERROR_WRONG_PASSWORD) {
            currentFragment.removeFromRecentList();
        }

        removeTab(currentFragment.getTabTag());
    }

    /**
     * Returns the short-version of title
     *
     * @param title The title
     * @return The shorten title
     */
    protected String shortenTitle(String title) {
        // let's substring the title to make sure it's not too large
        final int maxTitleCount = 20;
        if ((title.length() - 1) > maxTitleCount) {
            title = title.substring(0, maxTitleCount);
            title = title + "...";
        }

        return title;
    }

    /**
     * Called when user clicked on tab widget.
     *
     * @param tag           The tab tag
     * @param title         The title of tab
     * @param fileExtension The file extension
     * @param itemSource    The item source of the file
     * @param duration      The snackbar duration
     */
    protected void handleShowTabInfo(String tag, String title, String fileExtension, int itemSource, int duration) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                if (!listener.canShowFileInFolder()) {
                    return;
                }
            }
        }

        String message;
        String path = "";
        try {
            if (itemSource == BaseFileInfo.FILE_TYPE_EXTERNAL) {
                Uri uri = Uri.parse(tag);
                message = Utils.getUriDisplayName(activity, uri);
                path = Utils.getUriDocumentPath(uri);
            } else if (itemSource == BaseFileInfo.FILE_TYPE_EDIT_URI
                    || itemSource == BaseFileInfo.FILE_TYPE_OFFICE_URI) {
                message = title;
            } else {
                message = FilenameUtils.getName(tag);
                path = FilenameUtils.getPath(tag);
            }
        } catch (Exception e) {
            message = title;
        }

        if (message == null) {
            message = title;
        }

        showTabInfo(message, path, tag, itemSource, duration);
    }

    /**
     * Handles when back button is pressed.
     *
     * @return Ture if the back event is handled
     */
    public boolean handleBackPressed() {
        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null) {
            return false;
        }

        if (currentFragment.isAnnotationMode()) {
            currentFragment.hideAnnotationToolbar();
            return true;
        }

        if (mIsSearchMode) {
            if (mSearchResultsView != null && mSearchResultsView.getVisibility() == View.VISIBLE) {
                hideSearchResults();
            } else {
                exitSearchMode();
            }
            return true;
        }
        return false;
    }

    /**
     * Reads and Unsets file system changed.
     *
     * @return True if file system changed
     */
    public boolean readAndUnsetFileSystemChanged() {
        return mFileSystemChanged.getAndSet(false);
    }

    /**
     * Handles key when pressed up.
     *
     * @param keyCode The key code
     * @param event   The key event
     * @return True if the key is handled
     */
    public boolean handleKeyUp(int keyCode, KeyEvent event) {
        if (sDebug) {
            String output = "";
            if (event.isShiftPressed()) {
                output += "SHIFT ";
            }
            if (event.isCtrlPressed()) {
                output += "CTRL ";
            }
            if (event.isAltPressed()) {
                output += "ALT ";
            }
            output += keyCode;
            Log.d(TAG, "key: " + output);
        }

        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER && mSearchToolbar != null && mSearchToolbar.isJustSubmittedQuery()) {
            mSearchToolbar.setJustSubmittedQuery(false);
            return false;
        }

        if (ShortcutHelper.isCloseApp(keyCode, event)) {
            activity.finish();
            return true;
        }

        final PdfViewCtrlTabFragment currentFragment = getCurrentPdfViewCtrlFragment();
        if (currentFragment == null || !currentFragment.isDocumentReady()) {
            return false;
        }

        if (mSearchToolbar != null &&
                mSearchToolbar.getSearchView() != null &&
                currentFragment.isSearchMode()) {
            if (ShortcutHelper.isGotoNextSearch(keyCode, event)) {
                currentFragment.gotoNextSearch();
                // shouldn't be focused otherwise the shortcut for previous search (Shift+Enter)
                // just adds a space
                mSearchToolbar.getSearchView().clearFocus();
                return true;
            }
            if (ShortcutHelper.isGotoPreviousSearch(keyCode, event)) {
                currentFragment.gotoPreviousSearch();
                // shouldn't be focused otherwise the shortcut for previous search (Shift+Enter)
                // just adds a space
                mSearchToolbar.getSearchView().clearFocus();
                return true;
            }

            // in search mode, swallow all other shortcuts
            return false;
        }

        if (currentFragment.handleKeyUp(keyCode, event)) {
            return true;
        }

        if (mTabLayout != null) {
            boolean isNextDoc = ShortcutHelper.isGotoNextDoc(keyCode, event);
            boolean isPreviousDoc = ShortcutHelper.isGotoPreviousDoc(keyCode, event);

            if (isNextDoc || isPreviousDoc) {
                int currentPosition = mTabLayout.getSelectedTabPosition();
                int tabCounts = mTabLayout.getTabCount();
                if (currentPosition == -1) {
                    return false;
                }
                if (isNextDoc) {
                    ++currentPosition;
                } else {
                    currentPosition += tabCounts - 1;
                }
                currentPosition %= tabCounts;
                TabLayout.Tab tab = mTabLayout.getTabAt(currentPosition);
                if (tab != null) {
                    tab.select();
                    return true;
                }
            }
        }

        if (mSearchToolbar != null) {
            if (ShortcutHelper.isFind(keyCode, event)) {
                if (mSearchToolbar.isShown()) {
                    if (mSearchToolbar.getSearchView() != null) {
                        mSearchToolbar.getSearchView().setFocusable(true);
                        mSearchToolbar.getSearchView().requestFocus();
                    }
                } else {
                    if (mMenuSearch != null) {
                        setToolbarsVisible(true);
                        onOptionsItemSelected(mMenuSearch);
                    }
                }
                return true;
            }
        }

        if (ShortcutHelper.isCloseTab(keyCode, event)) {
            closeTab(currentFragment.getTabTag(), currentFragment.getTabSource());
            return true;
        }

        if (ShortcutHelper.isOpenDrawer(keyCode, event)) {
            if (mTabHostListeners != null) {
                for (TabHostListener listener : mTabHostListeners) {
                    listener.onNavButtonPressed();
                }
                return true;
            }
        }

        return false;
    }

    private void animateToolbars(final boolean visible) {
        Activity activity = getActivity();
        if (activity == null || mAppBarLayout == null) {
            return;
        }

        if ((mAppBarLayout.getVisibility() == View.VISIBLE) == visible) {
            return;
        }

        // workaround for not showing hand icon when mouse pointer is over tabs but tab layout
        // is not visible
        // it is a mystery why if tab layout is forced to be hidden in onAnimationEnd
        // then it doesn't work properly!
        // even this workaround doesn't solve hiding hand icon completely since if mouse goes
        // over a narrow bar on top of tabs it still shows hand icon
        if (Utils.isNougat()) {
            if (getCurrentPdfViewCtrlFragment() != null && getCurrentPdfViewCtrlFragment().getPDFViewCtrl() != null) {
                PointF point = getCurrentPdfViewCtrlFragment().getPDFViewCtrl().getCurrentMousePosition();
                if (point.x != 0f || point.y != 0f) {
                    setTabLayoutVisible(visible);
                }
            }
        }

        int duration = visible ? ANIMATE_DURATION_SHOW : ANIMATE_DURATION_HIDE;

        Transition slide = new Slide(Gravity.TOP).setDuration(duration);
        TransitionManager.beginDelayedTransition(mAppBarLayout, slide);
        if (visible) {
            mAppBarLayout.setVisibility(View.VISIBLE);
        } else {
            mAppBarLayout.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showSnackbar(String mainMessage, String actionMessage, final View.OnClickListener clickListener) {
        showSnackbar(mainMessage, actionMessage, clickListener, Snackbar.LENGTH_LONG);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showSnackbar(String mainMessage, String actionMessage, final View.OnClickListener clickListener, final int duration) {
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                if (!listener.canShowFileCloseSnackbar()) {
                    return;
                }
            }
        }
        View snackbarHolderView = mFragmentView.findViewById(R.id.controls_pane_coordinator_layout);
        final Snackbar snackbar = Snackbar.make(snackbarHolderView, mainMessage, duration);
        if (actionMessage != null && clickListener != null) {
            View.OnClickListener listener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    clickListener.onClick(v);
                }
            };

            snackbar.setAction(actionMessage.toUpperCase(), listener);
        }
        snackbar.show();
    }

    /**
     * Called when the fragment is resumed.
     */
    protected void resumeFragment() {
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onTabHostShown();
            }
        }

        if (!mFragmentPaused) {
            return;
        }
        mFragmentPaused = false;

        if (sDebug) {
            Log.d(TAG, "resume HostFragment");
        }

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        updateIconsInReflowMode();

        if (PdfViewCtrlSettingsManager.getScreenStayLock(activity)) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (Utils.isPie() && PdfViewCtrlSettingsManager.getFullScreenMode(activity)) {
            int mode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            if (mViewerConfig != null) {
                mode = mViewerConfig.getLayoutInDisplayCutoutMode();
            }
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = mode;
        }

        updateFullScreenModeLayout();
        showSystemUI();

        if (mIsSearchMode) {
            // Re-start search mode to ensure toolbar visibility is correct
            startSearchMode();
        }
    }

    /**
     * Called when the fragment is paused.
     */
    protected void pauseFragment() {
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                listener.onTabHostHidden();
            }
        }

        if (mFragmentPaused) {
            return;
        }
        mFragmentPaused = true;

        if (sDebug) {
            Log.d(TAG, "pause HostFragment");
        }

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        stopHideToolbarsTimer();
        if (mSearchToolbar != null) {
            mSearchToolbar.pause();
        }

        if (PdfViewCtrlSettingsManager.getScreenStayLock(activity)) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (Utils.isPie() && PdfViewCtrlSettingsManager.getFullScreenMode(activity)) {
            // reset cutout
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        }

        // release document lock
        if (mAutoCropTask != null && mAutoCropTask.getStatus() == AsyncTask.Status.RUNNING) {
            mAutoCropTask.cancel(true);
            mAutoCropTaskPaused = true;
            mAutoCropTaskTabTag = getCurrentTabTag();
        } else {
            mAutoCropTaskPaused = false;
        }

        if (mUndoRedoPopupWindow != null && mUndoRedoPopupWindow.isShowing()) {
            mUndoRedoPopupWindow.dismiss();
        }
        if (mMenuSearch != null) {
            mMenuSearch.getIcon().setAlpha(255);
        }
    }

    /**
     * Sets if the host can dispatch long press event.
     *
     * @param enabled True to make the host able to read long press event
     */
    public void setLongPressEnabled(boolean enabled) {
        PdfViewCtrlTabFragment tabFragment = getCurrentPdfViewCtrlFragment();
        if (tabFragment != null) {
            PDFViewCtrl pdfViewCtrl = tabFragment.getPDFViewCtrl();
            if (pdfViewCtrl != null) {
                pdfViewCtrl.setLongPressEnabled(enabled);
            }
        }
    }

    /**
     * Returns the number of tabs.
     *
     * @return The number of tabs
     */
    public int getTabCount() {
        if (mTabLayout == null) {
            return 0;
        }
        return mTabLayout.getTabCount();
    }

    public void updateToolbarDrawable() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (mToolbar != null) {
            if (Utils.isLargeScreenWidth(activity) && null == mViewerConfig) {
                mToolbar.setNavigationIcon(null);
            } else {
                if (mToolbarNavRes == 0) {
                    mToolbar.setNavigationIcon(null);
                } else {
                    mToolbar.setNavigationIcon(mToolbarNavRes);
                }
            }
        }
    }

    protected boolean canRecreateActivity() {
        boolean canRecreate = true;
        if (mTabHostListeners != null) {
            for (TabHostListener listener : mTabHostListeners) {
                if (!listener.canRecreateActivity()) {
                    canRecreate = false;
                }
            }
        }
        return canRecreate;
    }

    private boolean useSupportActionBar() {
        return mViewerConfig == null || mViewerConfig.isUseSupportActionBar();
    }

    public static void setDebug(boolean debug) {
        sDebug = debug;
    }

    /**
     * Returns a {@link PdfViewCtrlTabFragment} class object that will be used to
     * instantiate viewer tabs.
     *
     * @return a {@code CollabPdfViewCtrlTabFragment} class to instantiate later
     */
    @NonNull
    protected Class<? extends PdfViewCtrlTabFragment> getDefaultTabFragmentClass() {
        return PdfViewCtrlTabFragment.class;
    }

    public void setCurrentFile(File file)
    {
        this.currentFile=file;
    }

    public File getCurrentFile()
    {
        return this.currentFile;
    }
}
