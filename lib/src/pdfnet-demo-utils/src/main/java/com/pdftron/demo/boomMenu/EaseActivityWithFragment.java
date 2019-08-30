package com.pdftron.demo.boomMenu;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.pdftron.demo.R;
import com.pdftron.demo.app.AdvancedReaderActivity;
import com.pdftron.demo.app.FragmentTouchListener;
import com.pdftron.demo.asynctask.PopulateFolderTask;
import com.pdftron.demo.boomMenu.Animation.Ease;
import com.pdftron.demo.navigation.adapter.BaseFileAdapter;
import com.pdftron.demo.navigation.adapter.LocalFileAdapter;
import com.pdftron.demo.utils.FileInfoComparator;
import com.pdftron.demo.utils.FileListFilter;
import com.pdftron.pdf.config.ViewerBuilder;
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.model.PdfViewCtrlTabInfo;
import com.pdftron.pdf.utils.FontAdapter;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.PdfViewCtrlTabsManager;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.recyclerview.ItemSelectionHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class EaseActivityWithFragment extends Fragment implements
        BaseFileAdapter.AdapterListener, PopulateFolderTask.Callback,
        SearchView.OnQueryTextListener {
    private EaseFragment formerFragment;
    private  String user;
    public static String filesPath;
    private String filesPathName;



    private static final int CACHED_SD_CARD_FOLDER_LIMIT = 25;

    private String mFilterText;
    protected LocalFileAdapter mAdapter;
    protected ArrayList<FileInfo> mFileInfoList = new ArrayList<>();
    protected final Object mFileListLock = new Object();
    protected int mSpanCount;
    protected ItemSelectionHelper mItemSelectionHelper;
    private FileListFilter<FileInfo> mFilter;
    private File searchResult;
    private  ArrayList<FileInfo> searchResults;
    private PopulateFolderTask mPopulateFolderTask;
    private SearchView mSearchView;
    protected final LruCache<String, Boolean> mSdCardFolderCache = new LruCache<>(CACHED_SD_CARD_FOLDER_LIMIT);

    private Comparator<FileInfo> mSortMode = FileInfoComparator.folderPathOrder();;


    FragmentTouchListener fragmentTouchListener = new FragmentTouchListener() {
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            mSearchView.clearFocus();
            return false;
        }
    };



    public static EaseActivityWithFragment newInstance(String currentUser){
        EaseActivityWithFragment easeActivityWithFragment=new EaseActivityWithFragment();
        easeActivityWithFragment.setUser(currentUser);
        return easeActivityWithFragment;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        filesPath=getExtSDCardPath()+getPdfhomeName();
//        filesPathName=this.getString(R.string.file_pdf_home);
        filesPathName=getPdfhomeName();
        if (savedInstanceState == null) {
            EaseFragment easeFragment=new EaseFragment();
            easeFragment.setFilesPath(filesPath);

            if (user!=null)
                easeFragment.setFilesPath(getUserDirectory(user));

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

        ((AdvancedReaderActivity) this.getActivity()).registerFragmentTouchListener(fragmentTouchListener);
        mSpanCount = PdfViewCtrlSettingsManager.getGridSize(getActivity(), PdfViewCtrlSettingsManager.KEY_PREF_SUFFIX_FOLDER_FILES);
        mAdapter = createAdapter();

        mSearchView=view.findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(this);

        mSearchView.clearFocus();

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

                if (file.getName().equals(filesPathName.replace("/","")) || (user != null && file.getName().equals(user))) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_root_directory), Toast.LENGTH_SHORT).show();

                    return;
                }
                if (currentFragment.getFilesPath().equals("searched results"))
                {
                    EaseFragment parentFrag = new EaseFragment();
                    parentFrag.setFilesPath(filesPath);

                    getChildFragmentManager().beginTransaction().replace(R.id.frameLayout, parentFrag).commit();
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
            if (file.getName().equals(filesPathName) || (user != null && file.getName().equals(user))) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_root_directory), Toast.LENGTH_SHORT).show();

                return;
            }
            EaseFragment parentFrag = new EaseFragment();
            parentFrag.setFilesPath(file.getParent());
            getChildFragmentManager().beginTransaction().replace(R.id.frameLayout, parentFrag).commit();

        }
    }

    public void changeFragment(EaseFragment easeFragment)
    {
        getChildFragmentManager().beginTransaction().replace(R.id.frameLayout,easeFragment).commit();
    }

    public void changeFragment(PdfViewCtrlTabHostFragment easeFragment)
    {
        FragmentManager fragmentManager=getChildFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout,easeFragment).commit();

    }

    public String  getUserDirectory(String user)
    {
        String usersFolderParentPath=getActivity().getFilesDir().getAbsolutePath()+"/PDFcps/";
        String userFolder= usersFolderParentPath+user;
        return userFolder;

    }









    protected LocalFileAdapter createAdapter() {
        return new LocalFileAdapter(getContext(), mFileInfoList, mFileListLock,
                mSpanCount, this, mItemSelectionHelper);
    }

    public void onShowFileInfo(int position) {

    }
    public void onFilterResultsPublished(int resultCode) {
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {


        if (isInTheDirectory(query))
        {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_search_already_there), Toast.LENGTH_SHORT).show();
            return true;
        }

        query=query.replace(" ","");
        mFilterText=query;

        mPopulateFolderTask = new PopulateFolderTask(getContext(), new File(filesPath),
                mFileInfoList, mFileListLock, getSortMode(), true, true, true, mSdCardFolderCache, this);
        mPopulateFolderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mFilter=(FileListFilter<FileInfo>) mAdapter.getFilter();
        mFilter.filter(query);
        try {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ArrayList<FileInfo> results=mFilter.returnResults(query);
        if (results==null||results.size()==0)
        {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int height = display.getHeight();
            Toast toast=Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_search_no_result), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 3*height / 4);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();

            return false;
        }
        searchResults=mFilter.returnResults(query);
        handleResultsSearched(searchResults);
        return false;
    }

    private boolean isInTheDirectory(String query)
    {

        EaseFragment currentFragment=(EaseFragment)getCurrentFragment();
        String currentPath=currentFragment.getFilesPath();
        if (currentPath.equals("searched results"))
            currentPath=getExtSDCardPath()+"/"+filesPathName;
        ArrayList<String> files=new ArrayList<String>(Arrays.asList(new File(currentPath).list()));
        for (String s :files)
        {
            //s=s.toLowerCase();
            files.set(files.indexOf(s),s.toLowerCase());
        }
        return files.contains(query.toLowerCase());

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

    }
    @Override
    public void onPopulateFolderTaskProgressUpdated(
            File currentFolder
    ) {

        showPopulatedFolder(currentFolder);
        updateFileListFilter();

    }
    @Override
    public void onPopulateFolderTaskFinished(
    ) {

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
        SearchView searchView = mSearchView;
        filterText = searchView.getQuery().toString();
        return filterText;
    }

    void showPopulatedFolder(
            File currentFolder
    ) {


    }

    public void onDestroy()
    {
        super.onDestroy();
        ((AdvancedReaderActivity) this.getActivity()).unRegisterFragmentTouchListener(fragmentTouchListener);
    }

    private void handleResultsSearched(ArrayList<FileInfo> results)
    {

        if (results==null)
            return;
        EaseFragment easeFragment=new EaseFragment();
        easeFragment.setFileList(results);
        easeFragment.setFilesPath("searched results");
        changeFragment(easeFragment);
        mSearchView.clearFocus();
        mSearchView.setQuery("",false);



    }


    public   String getExtSDCardPath()
    {
        StorageManager mStorageManager = (StorageManager) getActivity().getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (true == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;




    }

    public String getFilesPath()
    {
        return filesPath;
    }

    private String getPdfhomeName()
    {
        File pdfHomeNameFile=new File(getActivity().getFilesDir().getAbsolutePath()+"/"+getString(R.string.file_save_pdf_home));
        if (!pdfHomeNameFile.exists())
        {
//            pdfHomeNameFile.mkdir();
            return getString(R.string.emplty_pdfhome_file);
        }
        try {
            FileInputStream inputStream = getActivity().openFileInput(getString(R.string.file_save_pdf_home));
            // read a character each time
            InputStreamReader reader = new InputStreamReader(inputStream);
            String pdfhome = "/";
            int tempchar;
            while ((tempchar = reader.read()) != -1) {

                if (((char) tempchar) != '\r') {
                    System.out.print((char) tempchar);
                    pdfhome += (char) tempchar;
                }

            }
            reader.close();
            return pdfhome+"/";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  null;
    }


}
