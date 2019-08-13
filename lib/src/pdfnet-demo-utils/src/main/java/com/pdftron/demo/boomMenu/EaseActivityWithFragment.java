package com.pdftron.demo.boomMenu;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.pdftron.demo.R;
import com.pdftron.demo.asynctask.PopulateFolderTask;
import com.pdftron.demo.boomMenu.Animation.Ease;
import com.pdftron.demo.navigation.adapter.BaseFileAdapter;
import com.pdftron.demo.navigation.adapter.LocalFileAdapter;
import com.pdftron.demo.utils.FileInfoComparator;
import com.pdftron.demo.utils.FileListFilter;
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.recyclerview.ItemSelectionHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class EaseActivityWithFragment extends Fragment implements
        BaseFileAdapter.AdapterListener, PopulateFolderTask.Callback,
        SearchView.OnQueryTextListener {
    private EaseFragment formerFragment;
    private static String cpsHome="/Download/PDFcps/";
    private  String user;
    private String filesPath="/storage/0403-0201/DOC SAT digitalisée/";


    private static final int CACHED_SD_CARD_FOLDER_LIMIT = 25;

    private String mFilterText;
    protected LocalFileAdapter mAdapter;
    protected ArrayList<FileInfo> mFileInfoList = new ArrayList<>();
    protected final Object mFileListLock = new Object();
    protected int mSpanCount;
    protected ItemSelectionHelper mItemSelectionHelper;
    private FileListFilter<FileInfo> mFilter;
    private ArrayList<FileInfo> searchResults;
    private PopulateFolderTask mPopulateFolderTask;
    private SearchView mSearchView;
    protected final LruCache<String, Boolean> mSdCardFolderCache = new LruCache<>(CACHED_SD_CARD_FOLDER_LIMIT);

    private Comparator<FileInfo> mSortMode = FileInfoComparator.folderPathOrder();;



    public static EaseActivityWithFragment newInstance(String currentUser){
        EaseActivityWithFragment easeActivityWithFragment=new EaseActivityWithFragment();
        easeActivityWithFragment.setUser(currentUser);
        return easeActivityWithFragment;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_ease_fragment);
        if (savedInstanceState == null) {
            EaseFragment easeFragment=new EaseFragment();
            if (user!=null)
                easeFragment.setFilesPath(getUserDirectory(user));
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.frameLayout, easeFragment)
//                    //.addToBackStack("fname")
//                    .commit();
            getChildFragmentManager().beginTransaction().add(R.id.frameLayout, easeFragment).commit();
        }


    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.activity_ease_fragment, container, false);

    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);
//        View viewCreated =view.findViewById(R.id.activity_ease);
//        relativeLayout=view.findViewById(R.id.show_menu_button);

        mSpanCount = PdfViewCtrlSettingsManager.getGridSize(getActivity(), PdfViewCtrlSettingsManager.KEY_PREF_SUFFIX_FOLDER_FILES);
        mAdapter = createAdapter();
        mPopulateFolderTask = new PopulateFolderTask(getContext(), new File(filesPath),
                mFileInfoList, mFileListLock, getSortMode(), true, true, true, mSdCardFolderCache, this);
        mSearchView=view.findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(this);

//        viewCreated.getBackground().setAlpha(130);
//        if (getActivity().getIntent().getStringExtra("path")!=null)
//            filesPath=getActivity().getIntent().getStringExtra("path");
//        init();

    }
    public void setUser(String currentUser)
    {
        this.user=currentUser;
    }

    public void saveFormerFragment(EaseFragment easeFragment)
    {
        this.formerFragment=easeFragment;
    }

    public Fragment getCurrentFragment()
    {
        return getChildFragmentManager().findFragmentById(R.id.frameLayout);
    }

    public void onBackPressed()
    {
        EaseFragment currentFragment=null;
        PdfViewCtrlTabHostFragment currentPdfFragment=null;
        if (getCurrentFragment() instanceof EaseFragment)
         currentFragment=(EaseFragment) getCurrentFragment();
        else if (getCurrentFragment() instanceof PdfViewCtrlTabHostFragment)
            currentPdfFragment=(PdfViewCtrlTabHostFragment)getCurrentFragment();
        if (currentFragment!=null) {
            String currentPath = currentFragment.getFilesPath();
            File file = new File(currentPath);
            if (file != null) {
                // File parent=new File(file.getParent());
                String name = file.getName();
                if (file.getName().equals("DOC SAT digitalisée") || (user != null && file.getName().equals(user))) {
                    Toast.makeText(getActivity().getApplicationContext(), "You are already in the root directory", Toast.LENGTH_SHORT).show();

                    return;
                }
                EaseFragment parentFrag = new EaseFragment();
                parentFrag.setFilesPath(file.getParent());
                getChildFragmentManager().beginTransaction().replace(R.id.frameLayout, parentFrag).commit();
            }
        }
        else if (currentPdfFragment!=null)
        {
            File file=currentPdfFragment.getCurrentFile();
            String name = file.getName();
            if (file.getName().equals("DOC SAT digitalisée") || (user != null && file.getName().equals(user))) {
                Toast.makeText(getActivity().getApplicationContext(), "You are already in the root directory", Toast.LENGTH_SHORT).show();

                return;
            }
            EaseFragment parentFrag = new EaseFragment();
            parentFrag.setFilesPath(file.getParent());
            getChildFragmentManager().beginTransaction().replace(R.id.frameLayout, parentFrag).commit();

        }
//        getFragmentManager().beginTransaction().replace(R.id.frameLayout,formerFragment).commit();
    }

    public void changeFragment(EaseFragment easeFragment)
    {
        getChildFragmentManager().beginTransaction().replace(R.id.frameLayout,easeFragment).commit();
    }

    public void changeFragment(PdfViewCtrlTabHostFragment easeFragment)
    {
        getChildFragmentManager().beginTransaction().replace(R.id.frameLayout,easeFragment).commit();
    }

    public String  getUserDirectory(String user)
    {
        String usersFolderParentPath= Environment.getExternalStorageDirectory().getAbsolutePath()+cpsHome;
        String userFolder= usersFolderParentPath+user;
        return userFolder;
//        EaseFragment parentFrag=new EaseFragment();
//        parentFrag.setFilesPath(userFolder);
//        getChildFragmentManager().beginTransaction().add(R.id.frameLayout,parentFrag).commit();

    }









    protected LocalFileAdapter createAdapter() {
        return new LocalFileAdapter(getContext(), mFileInfoList, mFileListLock,
                mSpanCount, this, mItemSelectionHelper);
    }

    public void onShowFileInfo(int position) {
//        if (mFileUtilCallbacks != null) {
//            mSelectedFile = mAdapter.getItem(position);
//            mFileInfoDrawer = mFileUtilCallbacks.showFileInfoDrawer(mFileInfoDrawerCallback);
//        }
    }
    public void onFilterResultsPublished(int resultCode) {
//        if (mAdapter != null) {
//            if (mEmptyTextView != null) {
//                if (mAdapter.getItemCount() > 0) {
//                    mEmptyTextView.setVisibility(View.GONE);
//                    scrollToCurrentFile(mRecyclerView);
//                } else if (mIsFullSearchDone) {
//                    switch (resultCode) {
//                        case FileListFilter.FILTER_RESULT_NO_STRING_MATCH:
//                            mEmptyTextView.setText(R.string.textview_empty_because_no_string_match);
//                            break;
//                        case FileListFilter.FILTER_RESULT_NO_ITEMS_OF_SELECTED_FILE_TYPES:
//                            mEmptyTextView.setText(R.string.textview_empty_because_no_files_of_selected_type);
//                            break;
//                        default:
//                            mEmptyTextView.setText(R.string.textview_empty_file_list);
//                            break;
//                    }
//                    mEmptyTextView.setVisibility(View.VISIBLE);
//                }
//            }
//
//            mNotSupportedTextView.setVisibility(View.GONE);
//            if (mCurrentFolder != null && getContext() != null) {
//                String[] list = mCurrentFolder.list();
//                if (list != null) {
//                    int fileCount = list.length;
//                    if (fileCount > mAdapter.getItemCount()) {
//                        int extraCount = fileCount - mAdapter.getItemCount();
//                        String fileStr = extraCount > 1 ? getString(R.string.files) : getString(R.string.file);
//                        mNotSupportedTextView.setText(getString(R.string.num_files_not_supported, extraCount, fileStr));
//                        //mNotSupportedTextView.setVisibility(View.VISIBLE);
//                        mNotSupportedTextView.setVisibility(View.INVISIBLE);
//                    }
//                }
//            }
//        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
//        // prevent clearing filter text when the fragment is hidden
//        if (mAdapter != null && Utils.isNullOrEmpty(mFilterText)) {
//            mAdapter.cancelAllThumbRequests(true);
//            mFilter=(FileListFilter<FileInfo>) mAdapter.getFilter();
//            mFilter.filter(newText);
//            searchResults=mFilter.returnResults();
//            boolean isEmpty = Utils.isNullOrEmpty(newText);
//            mAdapter.setInSearchMode(!isEmpty);
//        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mFilterText=query;
        mPopulateFolderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mFilter=(FileListFilter<FileInfo>) mAdapter.getFilter();
        mFilter.filter(query);
        searchResults=mFilter.returnResults(query);
        return false;
    }
    private Comparator<FileInfo> getSortMode(
    ) {

        if (mSortMode != null) {
            return mSortMode;
        }

        return FileInfoComparator.folderPathOrder();

    }

    @Override
    public void onPopulateFolderTaskStarted(
    ) {

        Context context = getContext();
        if (context == null) {
            return;
        }

        synchronized (mFileListLock) {
            mFileInfoList.clear();
        }
        updateFileListFilter();

//        if (mEmptyTextView != null) {
//            mLoadingFileHandler.sendEmptyMessageDelayed(0, 100);
//        }
//        if (mProgressBarView != null) {
//            mProgressBarView.setVisibility(View.VISIBLE);
//        }

//        setReloadActionButtonState(true);
//        mIsFullSearchDone = false;

    }
    @Override
    public void onPopulateFolderTaskProgressUpdated(
            File currentFolder
    ) {

//        mLoadingFileHandler.removeMessages(0);
//        if (mProgressBarView != null) {
//            mProgressBarView.setVisibility(View.GONE);
//        }

        showPopulatedFolder(currentFolder);
        updateFileListFilter();
//        setReloadActionButtonState(false);

    }
    @Override
    public void onPopulateFolderTaskFinished(
    ) {

//        mIsFullSearchDone = true;
        updateFileListFilter();

    }

    private void updateFileListFilter() {
        if (mAdapter == null) {
            return;
        }

        String constraint = getFilterText();
        if (constraint == null) {
            constraint = "";
        }
        mAdapter.getFilter().filter(constraint);
        boolean isEmpty = Utils.isNullOrEmpty(constraint);
        mAdapter.setInSearchMode(!isEmpty);
    }
    public String getFilterText() {
        if (!Utils.isNullOrEmpty(mFilterText)) {
            return mFilterText;
        }

        String filterText = "";
//        if (mSearchMenuItem != null) {
        SearchView searchView = mSearchView;
        filterText = searchView.getQuery().toString();
//        }
        return filterText;
    }

    void showPopulatedFolder(
            File currentFolder
    ) {

//        if (currentFolder == null) {
//            return;
//        }
//
////        mInSDCardFolder = false;
////        Boolean isSdCardFolder = mSdCardFolderCache.get(currentFolder.getAbsolutePath());
////        if (isSdCardFolder != null && isSdCardFolder) {
////            mInSDCardFolder = true;
////        }
//
////        if (mGoToSdCardView == null || mRecyclerView == null || mFabMenu == null) {
////            return;
////        }
//
//        if (Utils.isLollipop()) {
//            if (mInSDCardFolder) {
//                if (mSnackBar == null) {
//                    mSnackBar = Snackbar.make(mRecyclerView, R.string.snack_bar_local_folder_read_only, Snackbar.LENGTH_INDEFINITE);
//                    mSnackBar.setAction(getString(R.string.snack_bar_local_folder_read_only_redirect).toUpperCase(),
//                            new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    // go to external tab
//                                    if (null != mJumpNavigationCallbacks) {
//                                        finishActionMode();
//                                        mJumpNavigationCallbacks.gotoExternalTab();
//                                    }
//                                }
//                            });
//
//                    mSnackBar.addCallback(new Snackbar.Callback() {
//                        @Override
//                        public void onDismissed(Snackbar snackbar, int event) {
//                            mSnackBar = null;
//                        }
//                    });
//                    //mSnackBar.show();
//                }
//                // Show dialog re-direct user to SD card tab
//                if (mRecyclerView!=null)mRecyclerView.setVisibility(View.GONE);
//                if (mFabMenu!=null)mFabMenu.setVisibility(View.GONE);
//                if(mGoToSdCardView!=null)mGoToSdCardView.setVisibility(View.VISIBLE);
//            } else {
//                if (mSnackBar != null) {
//                    if (mSnackBar.isShown()) {
//                        mSnackBar.dismiss();
//                    }
//                    mSnackBar = null;
//                }
//                if(mGoToSdCardView!=null) mGoToSdCardView.setVisibility(View.GONE);
//                if (mRecyclerView!=null)mRecyclerView.setVisibility(View.VISIBLE);
//                if (mFabMenu!=null)mFabMenu.setVisibility(View.VISIBLE);
//            }
//        }

    }

}
