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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pdftron.demo.R;
import com.pdftron.demo.app.AdvancedReaderActivity;
import com.pdftron.demo.asynctask.PopulateFolderTask;
import com.pdftron.demo.boomMenu.BoomButtons.ButtonPlaceEnum;
import com.pdftron.demo.boomMenu.Piece.PiecePlaceEnum;
import com.pdftron.demo.navigation.adapter.LocalFileAdapter;
import com.pdftron.demo.utils.FileInfoComparator;
import com.pdftron.demo.utils.FileListFilter;
import com.pdftron.pdf.config.ViewerBuilder;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.model.PdfViewCtrlTabInfo;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.utils.PdfViewCtrlTabsManager;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.recyclerview.ItemSelectionHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;

import static com.pdftron.demo.boomMenu.ButtonEnum.TextOutsideCircle;
import static com.pdftron.pdf.controls.AnnotStyleDialogFragment.TAG;

public  class EaseFragment extends Fragment {

    private static final int CACHED_SD_CARD_FOLDER_LIMIT = 25;
    public static File UsersFile=null;
//    public static String usersNameFileName="UserName.txt";
public static String usersNameFileName;
    private GridLayout relativeLayout;
//    private String filesPath="/storage/0403-0201/DOC SAT digitalisée/";
    private  String filesPath;
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

    private static boolean firstRun;


//    public static EaseFragment newInstance(){
//        EaseFragment easeFragment=new EaseFragment();
//        easeFragment.filesPath2=easeFragment.getExtSDCardPath()+easeFragment.filesPath2;
//        return easeFragment;
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFragment=(EaseActivityWithFragment) getParentFragment();
        usersNameFileName=this.getString(R.string.file_current_username);
//        filesPath=parentFragment.getExtSDCardPath()+filesPath;
//        filesPath=getExtSDCardPath()+filesPath;
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


        getPermission();
        if (preDefinedFiles==null) {
            File home = new File(filesPath);//初始化File对象
            files = home.listFiles();//噩梦结束了吗？

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



            Display display = getActivity().getWindowManager().getDefaultDisplay();

            if (new File(filesPath).getName().equals(getUserNameFromFile())) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_zero_cps), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_no_home_directory), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            return;

        }
        for( final File file : files )
        {
            if (!file.isDirectory())
            {

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
                            return true;
                        }
                        else
                        {
                            Display display = getActivity().getWindowManager().getDefaultDisplay();
                            int height = display.getHeight();

                            Toast toast=Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_delete_original_file), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.show();

                            return true;
                        }

                    }
                });

                TextView textView=new TextView(getContext());
                textView.setText(name);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setPadding(0,0,0,0);
                textView.setWidth(250);
                textView.setTypeface(Typeface.SERIF);

                if (file.getName().length()>8)
                {

                    textView.setSingleLine(false);


                }

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
            if (file.listFiles().length>12)
            {
                menuButton.setPiecePlaceEnum(PiecePlaceEnum.DOT_12);
                menuButton.setButtonPlaceEnum(ButtonPlaceEnum.SC_12);
                menuButton.doLayoutJobs();
            }

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
//                    Toast.makeText(getActivity().getApplicationContext(), "Sorry, can't open this type of file, it's not a pdf.", Toast.LENGTH_SHORT).show();

                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    int height = display.getHeight();

                    Toast toast=Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_not_pdf), Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.TOP, 0, 3*height / 4);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();

                    return;
                }
            }
            if (Utils.isNullOrEmpty(password)) {
                password = Utils.getPassword(getContext(), file.getAbsolutePath());
            }


            startTabHostFragment(
                    ViewerBuilder.withFile(file, password)
                            .usingFileType(BaseFileInfo.FILE_TYPE_FILE),file
            );
            openedSucessfully = true;

        }
    }


    private void startTabHostFragment(@Nullable ViewerBuilder viewerBuilder, File file) {


        if (viewerBuilder == null) {
            viewerBuilder = ViewerBuilder.withUri(null, "");
        }

        PdfViewCtrlTabsManager.getInstance().addDocument(getActivity(), file.getAbsolutePath());
        PdfViewCtrlTabInfo info = PdfViewCtrlTabsManager.getInstance().getPdfFViewCtrlTabInfo(getActivity(), file.getAbsolutePath());
        int itemSource = BaseFileInfo.FILE_TYPE_UNKNOWN;
        String title = "";
        String fileExtension = null;
        String password = "";

        if (info != null) {
            itemSource = info.tabSource;
            title = info.tabTitle;
            fileExtension = info.fileExtension;
            password = Utils.decryptIt(getActivity(), info.password);
        }

           mPdfViewCtrlTabHostFragment = viewerBuilder.build(getContext());
           mPdfViewCtrlTabHostFragment.setCurrentFile(file);
           mPdfViewCtrlTabHostFragment.addHostListener((AdvancedReaderActivity) EaseFragment.getFatherFragment().getActivity());

        Logger.INSTANCE.LogD(TAG, "replace with " + mPdfViewCtrlTabHostFragment);

        ((EaseActivityWithFragment)(EaseFragment.getFatherFragment())).changeFragment(mPdfViewCtrlTabHostFragment);

    }








    private void dialog(final File file)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.dialoge_delete_cp));

        builder.setTitle("");

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                boolean deleted=file.delete();
                reload();


            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();


    }

    private void reload()
    {
        relativeLayout.removeAllViews();
        File home = new File(filesPath);//初始化File对象
        files = home.listFiles();//噩梦结束了吗？
        generateBtnList(files);
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
            InputStreamReader reader = new InputStreamReader(inputStream);
            String usName="";
            int tempchar;
            while ((tempchar = reader.read()) != -1) {

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
