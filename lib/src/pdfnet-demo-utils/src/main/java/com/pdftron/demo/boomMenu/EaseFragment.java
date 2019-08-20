package com.pdftron.demo.boomMenu;

import android.Manifest;
//import android.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pdftron.demo.R;
import com.pdftron.demo.app.AdvancedReaderActivity;
import com.pdftron.demo.app.MainActivity;
import com.pdftron.demo.asynctask.PopulateFolderTask;
import com.pdftron.demo.navigation.adapter.BaseFileAdapter;
import com.pdftron.demo.navigation.adapter.LocalFileAdapter;
import com.pdftron.demo.utils.FileInfoComparator;
import com.pdftron.demo.utils.FileListFilter;
import com.pdftron.pdf.config.ViewerBuilder;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.recyclerview.ItemSelectionHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.pdftron.demo.boomMenu.ButtonEnum.TextOutsideCircle;
import static com.pdftron.pdf.controls.AnnotStyleDialogFragment.TAG;
import static com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment.currentUsersNameFileName;

public  class EaseFragment extends Fragment {

    private static final int CACHED_SD_CARD_FOLDER_LIMIT = 25;
    public static File UsersFile=null;
    public static String usersNameFileName="UserName.txt";

    private GridLayout relativeLayout;
    private String filesPath="/storage/0403-0201/DOC SAT digitalisée/";
    private ArrayList<String> namesOfFiles;
    private File[] files;

    private ArrayList<FileInfo> preDefinedFiles;
    private GridLayout.LayoutParams gl;

    private PdfViewCtrlTabHostFragment mPdfViewCtrlTabHostFragment;
    private SearchView mSearchView;

    private String mFilterText;
    protected LocalFileAdapter mAdapter;
    protected ArrayList<FileInfo> mFileInfoList = new ArrayList<>();
    protected final Object mFileListLock = new Object();
    protected int mSpanCount;
    protected ItemSelectionHelper mItemSelectionHelper;
    private FileListFilter<FileInfo> mFilter;
    private ArrayList<FileInfo> searchResults;
    private PopulateFolderTask mPopulateFolderTask;
    protected final LruCache<String, Boolean> mSdCardFolderCache = new LruCache<>(CACHED_SD_CARD_FOLDER_LIMIT);

    private Comparator<FileInfo> mSortMode = FileInfoComparator.folderPathOrder();;

    private  static  EaseActivityWithFragment parentFragment;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFragment=(EaseActivityWithFragment) getParentFragment();
       // setContentView(R.layout.activity_ease);
//        View view=findViewById(R.id.activity_ease);
//        view.getBackground().setAlpha(130);
//        if (getIntent().getStringExtra("path")!=null)
//            filesPath=getIntent().getStringExtra("path");
//
//        relativeLayout=(GridLayout) findViewById(R.id.show_menu_button);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 300);
        gl = new GridLayout.LayoutParams(layoutParams);
        gl.rightMargin = 40;
        gl.leftMargin = 40;

//        init();

//        initBmb(R.id.bmb1);
//        initBmb(R.id.bmb2);
//        initBmb(R.id.bmb3);
//        initBmb(R.id.bmb4);
//        initBmb(R.id.bmb5);
//        initBmb(R.id.bmb6);
//        initBmb(R.id.bmb7);
//        initBmb(R.id.bmb8);
//        initBmb(R.id.bmb9);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_ease,container,false);

    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);
        View viewCreated =view.findViewById(R.id.activity_ease);
        relativeLayout=view.findViewById(R.id.show_menu_button);

//        mSpanCount = PdfViewCtrlSettingsManager.getGridSize(getActivity(), PdfViewCtrlSettingsManager.KEY_PREF_SUFFIX_FOLDER_FILES);
//        mAdapter = createAdapter();
//        mPopulateFolderTask = new PopulateFolderTask(getContext(), new File(filesPath),
//                mFileInfoList, mFileListLock, getSortMode(), true, true, true, mSdCardFolderCache, this);
//        mSearchView=view.findViewById(R.id.searchView);
//        mSearchView.setOnQueryTextListener(this);

        viewCreated.getBackground().setAlpha(130);
        if (getActivity().getIntent().getStringExtra("path")!=null)
            filesPath=getActivity().getIntent().getStringExtra("path");
        init();

    }

    public void setFilesPath(String path)
    {
        this.filesPath=path;
    }

    public void setFileList(ArrayList<FileInfo> files)
    {
        preDefinedFiles=files;
    }

    public String getFilesPath()
    {
        return filesPath;
    }

    private BoomMenuButton initBmb( BoomMenuButton bmb ) {
//        BoomMenuButton bmb = (BoomMenuButton) findViewById(res);
        assert bmb != null;
        for (int i = 0; i < bmb.getPiecePlaceEnum().pieceNumber(); i++)
            bmb.addBuilder(BuilderManager.getSimpleCircleButtonBuilder());
        return bmb;
    }

    public static EaseActivityWithFragment getFatherFragment()
    {
        return parentFragment;
    }

    private void init()
    {
        //if it's not the first time to start showDireActivity, than the filesPath is not the initial filesPath,
        //set it to the value received
//        if (getIntent().getStringExtra("path")!=null) {
//            Log.d("****received path value",filesPath);
//            filesPath = getIntent().getStringExtra("path");
//        }
//        if (getIntent().getStringExtra("userName")!=null) {
//            Log.d("****received path value",filesPath);
//            userName = getIntent().getStringExtra("userName");
//        }
//        filesPath="/storage/";
        //create the file object using the filesPath which is the parent of all the pdfs we want to read
//        File file = new File(filesPath);
//        //get the number of pdfs under filesPath
//        //numOfFile=FileInfoUtils.getFileSize(file);
//        //get the list of pdf names
//        File flist[] = file.listFiles();


        getPermission();
        if (preDefinedFiles==null) {
            File home = new File(filesPath);//初始化File对象
            files = home.listFiles();//噩梦结束了吗？
//        boolean exite=file.exists();
//        String []names=file.list();
//        this.namesOfFiles=new ArrayList<String>(Arrays.asList(names));
//        numOfFile=namesOfFiles.size();

            //filtrePDF();
            generateBtnList(files);
        }
        else {
            generateBtnList(preDefinedFiles);
        };

    }
    protected void generateBtnList( ArrayList<FileInfo> files ){
        File[] preFiles=new File[files.size()];
        int i=0;
        for (FileInfo fileInfo:files)
        {
            preFiles[i]=fileInfo.getFile();
            i++;
        }
        generateBtnList(preFiles);
    }

    protected void generateBtnList( File[] files ){
        int indexInRow=0;
        int index = 0;
        TableRow tableRow=null;
        if (files==null||files.length==0)
        {
            Toast.makeText(getActivity().getApplicationContext(), "Sorry, you haven't any saved copies yet.", Toast.LENGTH_SHORT).show();

        }
        for( final File file : files )
        {
            if (!file.isDirectory())
            {
//                BoomMenuButton menuButton=new BoomMenuButton(this);
//                BoomButtonBuilder buttonBuilder=BuilderManager.getSimpleCircleButtonBuilder();
//                Dot boomPiece=(Dot)PiecePlaceManager.createPiece(menuButton,buttonBuilder);
//                relativeLayout.addView(boomPiece);
                String name=file.getName();
                final Button button=new Button(getContext());
                button.setBackgroundColor(Color.TRANSPARENT);
                Drawable icon=getResources().getDrawable(R.drawable.file_icon_button);
                icon.setBounds(0, 0, icon.getIntrinsicWidth()*3, icon.getIntrinsicHeight()*3);
                button.setCompoundDrawables(button.getCompoundDrawables()[0],icon,button.getCompoundDrawables()[2],button.getCompoundDrawables()[0]);


                button.setPadding(0,0,0,0);
                button.setGravity(Gravity.CENTER_HORIZONTAL);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFileSelected(file,"",true);
                    }
                });
                button.setOnLongClickListener(new View.OnLongClickListener()
                {
                    public boolean onLongClick(View v)
                    {

                        File parent=file.getParentFile();
                        if (parent.getName().equals(getUserNameFromFile())) {
                            dialog(file);
//                            button.setVisibility(View.INVISIBLE);
                            return true;
                        }
                        else
                        {
                            Toast.makeText(getActivity().getApplicationContext(), "Sorry, can't delete the original file.", Toast.LENGTH_SHORT).show();

                            return true;
                        }

                    }
                });
//                MarqueeTextView textView=(MarqueeTextView)findViewById(R.id.textview);
//                textView.setText(name);
                TextView textView=new TextView(getContext());
                textView.setText(name);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setPadding(0,0,0,0);
                textView.setWidth(250);
                textView.setTypeface(Typeface.SERIF);

                if (file.getName().length()>8)
                {
//                    textView .setEllipsize(TextUtils.TruncateAt.MARQUEE);
//                    textView .setSingleLine(true);
//                    textView .setMarqueeRepeatLimit(6);
////                    textView.setMarqueeRepeatLimit(-1);
//                    textView.setHorizontallyScrolling(true);
//                    textView.setFocusable(true);
                    textView.setSingleLine(false);

//                    textView.setFocusableInTouchMode(true);
//                    textView.setFreezesText(true);
                }
//                textView.setFocusableInTouchMode(true);
//                textView.setFreezesText(true);

                LinearLayout linearLayout=new LinearLayout(getContext());
                linearLayout.addView(button);
                linearLayout.addView(textView);
                linearLayout.setGravity(Gravity.CENTER);
                linearLayout.setOrientation(LinearLayout.VERTICAL);



                relativeLayout.addView(linearLayout);


                continue;
            }

            BoomMenuButton menuButton=new BoomMenuButton(getContext());

            Log.v("EaseActivity","creating the button ****************");
            menuButton.setButtonEnum(TextOutsideCircle);
            menuButton.setSubFiles(file.listFiles());
            menuButton.setBackgroundEffect(true);
            menuButton.setForegroundGravity(Gravity.CENTER_HORIZONTAL);
            if (file.listFiles()!=null) {

                menuButton.setPiecePlaceEnum(file.list().length);
                menuButton.setButtonPlaceEnum(file.list().length);
            }

            TextView textView=new TextView(getContext());
            textView.setText(file.getName());
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTypeface(Typeface.MONOSPACE);
            textView.setPadding(0,10,0,0);
            textView.setTextSize(18);

            LinearLayout linearLayout=new LinearLayout(getContext());
            linearLayout.addView(menuButton);
            linearLayout.addView(textView);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
            GridLayout.LayoutParams gl = new GridLayout.LayoutParams(layoutParams);
            gl.rightMargin = 20;
            gl.leftMargin = 20;



            if(linearLayout.getParent() != null) {
                ((ViewGroup)linearLayout.getParent()).removeView(linearLayout); // <- fix
            }

            linearLayout.setLayoutParams(gl);
            relativeLayout.addView(linearLayout);



        }



        //add the row which has less than 3 buttons to the table
        //before adding it, remove its parent view if it already has one
//        if (tableRow != null) {
//            ViewGroup parentViewGroup = (ViewGroup) tableRow.getParent();
//            if (parentViewGroup != null ) {
//                parentViewGroup.removeView(tableRow);
//            }
//        }


    }


    /**
     * if folderName is a folder, return true, otherwise return false
     * @param folderName    folderName=filesPath+name of the folder
     * @return
     */
    protected boolean isFolder(String folderName)
    {
        File file=new File(folderName);
        if (file.isDirectory())
            return true;
        else
            return false;
    }


    void getPermission()
    {
        int permissionCheck1 = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    124);
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 124) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                Log.d("heihei","获取到权限了！");
                File home = new File(filesPath);//初始化File对象
                 files = home.listFiles();//噩梦结束了吗？
            } else { Log.d("heihei","搞不定啊！"); } }}




    /**
     * Opens a local document in the document viewer.
     *
     * @param file that represents a local document
     * @param password password used to open document, can be null
     * @param skipPasswordCheck true to skip password check when opening the document
     */
    public void onFileSelected(final File file, String password, boolean skipPasswordCheck) {
        boolean openedSucessfully = false;
        if (file != null && file.exists()) {
            if (file.isFile())
            {
                String name=file.getName();
                String type=name.substring(name.lastIndexOf(".") + 1);
                if (!type.equals("pdf")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Sorry, can't open this type of file, it's not a pdf.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (Utils.isNullOrEmpty(password)) {
                password = Utils.getPassword(getContext(), file.getAbsolutePath());
            }

//            if (Utils.isOfficeDocument(file.getAbsolutePath())) {
//                startTabHostFragment(
//                        ViewerBuilder.withFile(file, password)
//                                .usingFileType(BaseFileInfo.FILE_TYPE_FILE)
//                );
//
//                return;
//            }

//            CheckDifferentFileTypeResult result = checkDifferentFileType(file, password, BaseFileInfo.FILE_TYPE_FILE, "", skipPasswordCheck);
//
//            if (result.getOpenDocument()) {
//                // Perform any operation needed on the current fragment before launching the viewer.
//                if (mCurrentFragment != null && hasMainActivityListener(mCurrentFragment)) {
//                    ((MainActivityListener) mCurrentFragment).onPreLaunchViewer();
//                }
//            }

//            if (result.getOpenDocument() // PDF document
//                    || FileManager.checkIfFileTypeIsInList(file.getAbsolutePath())) { // non-PDF document
            startTabHostFragment(
                    ViewerBuilder.withFile(file, password)
                            .usingFileType(BaseFileInfo.FILE_TYPE_FILE),file
            );
            openedSucessfully = true;
//            }
//        } else {
//            Utils.showAlertDialog(this, R.string.file_does_not_exist_message, R.string.error_opening_file);
//        }

//        if (!openedSucessfully) {
//            // Update recent and favorite files lists
//            FileInfo fileInfo = new FileInfo(BaseFileInfo.FILE_TYPE_FILE, file);
//            RecentFilesManager.getInstance().removeFile(this, fileInfo);
//            FavoriteFilesManager.getInstance().removeFile(this, fileInfo);
//            if (file != null) {
//                PdfViewCtrlTabsManager.getInstance().removePdfViewCtrlTabInfo(this, file.getAbsolutePath());
//            }
//
//            // Try to update fragment since underlying data has changed
//            reloadBrowser();
//
//            onOpenDocError();
//        }
        }
    }


    private void startTabHostFragment(@Nullable ViewerBuilder viewerBuilder, File file) {
//        if (isFinishing()) {
//            return;
//        }
//        if (null == findViewById(R.id.frameLayout)) {
//            // wrong states
//            return;
//        }

        if (viewerBuilder == null) {
            viewerBuilder = ViewerBuilder.withUri(null, "");
        }

//        viewerBuilder.usingCacheFolder(mUseCacheDir)
//                .usingQuitAppMode(mQuitAppWhenDoneViewing);
//
//        Bundle args = viewerBuilder.createBundle(this);
//        if (args.containsKey(BUNDLE_TAB_TITLE)) {
//            String title = args.getString(BUNDLE_TAB_TITLE);
//            if (mLastAddedBrowserFragment != null && mLastAddedBrowserFragment instanceof FileBrowserViewFragment) {
//                ((FileBrowserViewFragment) mLastAddedBrowserFragment).setCurrentFile(title);
//            }
//        }
//        mProcessedFragmentViewId = R.id.item_viewer;
//        selectNavItem(mProcessedFragmentViewId);


//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.setCustomAnimations(R.anim.tab_fragment_slide_in_bottom, R.anim.tab_fragment_slide_out_bottom);
        mPdfViewCtrlTabHostFragment = viewerBuilder.build(getContext());
        mPdfViewCtrlTabHostFragment.setCurrentFile(file);
        mPdfViewCtrlTabHostFragment.addHostListener((AdvancedReaderActivity)EaseFragment.getFatherFragment().getActivity());

        Logger.INSTANCE.LogD(TAG, "replace with " + mPdfViewCtrlTabHostFragment);
//        ft.replace(R.id.container, mPdfViewCtrlTabHostFragment, null);
        ((EaseActivityWithFragment)(EaseFragment.getFatherFragment())).changeFragment(mPdfViewCtrlTabHostFragment);
//        ft.commit();

//        setCurrentFragment(mPdfViewCtrlTabHostFragment);

//        updateNavTab(); // update navigation tab in case the activity will be resumed
//
//        toggleInfoDrawer(false);
    }


//    protected LocalFileAdapter createAdapter() {
//        return new LocalFileAdapter(getContext(), mFileInfoList, mFileListLock,
//                mSpanCount, this, mItemSelectionHelper);
//    }

    public void onShowFileInfo(int position) {
//        if (mFileUtilCallbacks != null) {
//            mSelectedFile = mAdapter.getItem(position);
//            mFileInfoDrawer = mFileUtilCallbacks.showFileInfoDrawer(mFileInfoDrawerCallback);
//        }
    }
    public void onFilterResultsPublished(int resultCode) {

    }

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
    public void onPopulateFolderTaskProgressUpdated(File currentFolder) {

        showPopulatedFolder(currentFolder);
        updateFileListFilter();
//        setReloadActionButtonState(false);

    }
    public void onPopulateFolderTaskFinished() {

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
            SearchView searchView = mSearchView;
            filterText = searchView.getQuery().toString();
        return filterText;
    }

    void showPopulatedFolder(File currentFolder) {

    }

    private void dialog(final File file)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to delete this document?");

        builder.setTitle("");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                boolean deleted=file.delete();


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();


    }
    /*
        get the  user name stored in username.txt under Files
         */
    public  String getUserNameFromFile()
    {
        File fileDir = getActivity().getFilesDir();

        try {
            File UsersName=new File(getActivity().getFilesDir().getAbsolutePath()+"/"+usersNameFileName);
            if (!UsersName.exists())
                UsersName.createNewFile();
            FileInputStream inputStream = getActivity().openFileInput(usersNameFileName);
            UsersFile=new File(getActivity().getFilesDir().getAbsolutePath()+"/"+usersNameFileName);
//            System.out.println("以字符为单位读取文件内容，一次读一个字节：");
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




}
