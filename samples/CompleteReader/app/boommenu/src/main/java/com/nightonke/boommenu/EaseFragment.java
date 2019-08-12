package com.nightonke.boommenu;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import static com.nightonke.boommenu.ButtonEnum.TextOutsideCircle;

public class EaseFragment extends Fragment {

    private GridLayout relativeLayout;
    private String filesPath="/storage/0403-0201/DOC SAT digitalisée/";
    private ArrayList<String> namesOfFiles;
    private File[] files;
    private GridLayout.LayoutParams gl;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);
        View viewCreated =view.findViewById(R.id.activity_ease);
        relativeLayout=view.findViewById(R.id.show_menu_button);
        viewCreated.getBackground().setAlpha(130);
        if (getActivity().getIntent().getStringExtra("path")!=null)
            filesPath=getActivity().getIntent().getStringExtra("path");
        init();

    }

    public void setFilesPath(String path)
    {
        this.filesPath=path;
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
        File home = new File(filesPath);//初始化File对象
        files = home.listFiles();//噩梦结束了吗？
//        boolean exite=file.exists();
//        String []names=file.list();
//        this.namesOfFiles=new ArrayList<String>(Arrays.asList(names));
//        numOfFile=namesOfFiles.size();

        //filtrePDF();
        generateBtnList(files);

    }

    protected void generateBtnList( File[] files ){
        int indexInRow=0;
        int index = 0;
        TableRow tableRow=null;
        for( File file : files )
        {
            if (!file.isDirectory())
            {
//                BoomMenuButton menuButton=new BoomMenuButton(this);
//                BoomButtonBuilder buttonBuilder=BuilderManager.getSimpleCircleButtonBuilder();
//                Dot boomPiece=(Dot)PiecePlaceManager.createPiece(menuButton,buttonBuilder);
//                relativeLayout.addView(boomPiece);
                String name=file.getName();
                Button button=new Button(getContext());
                button.setBackgroundColor(Color.TRANSPARENT);
                Drawable icon=getResources().getDrawable(R.drawable.ic_file_blank_white_24dp);
                icon.setBounds(0, 0, icon.getIntrinsicWidth()*2, icon.getIntrinsicHeight()*2);
                button.setCompoundDrawables(button.getCompoundDrawables()[0],icon,button.getCompoundDrawables()[2],button.getCompoundDrawables()[0]);

                button.setPadding(0,0,0,0);
                button.setGravity(Gravity.CENTER_HORIZONTAL);
//                MarqueeTextView textView=(MarqueeTextView)findViewById(R.id.textview);
//                textView.setText(name);
                TextView textView=new TextView(getContext());
                textView.setText(name);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setPadding(20,0,20,0);
                textView.setWidth(250);
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
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                linearLayout.setOrientation(LinearLayout.VERTICAL);



                relativeLayout.addView(linearLayout);


                continue;
            }
//            //if it's a new row, create a TableRow
//            if (indexInRow==0)  tableRow=new TableRow(this);
//            //create the button corresponding with name btnContent
//            // and set its text and background color which will be transparent
//            Button codeBtn = new Button( this );
//            codeBtn.setText(btnContent);
//            codeBtn.setBackgroundColor(Color.TRANSPARENT);
//            //set the animation of a button when it's clicked
//            codeBtn.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            Animation animation = AnimationUtils.loadAnimation(showDireActivity.this, R.anim.nomal_to_large);
//                            v.startAnimation(animation);
//                            break;
//                    }
//                    return false;
//                }
//
//            });
            BoomMenuButton menuButton=new BoomMenuButton(getContext());
//            Drawable icon;
//            //use different images for directory and pdf
//            if (isFolder(filesPath+btnContent))
//                icon=getResources().getDrawable(R.drawable.folder);
//            else
//                icon=getResources().getDrawable(R.drawable.pdf);
            //set bound of icons, otherwise it won't be displayed
//            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
//            codeBtn.setCompoundDrawables(codeBtn.getCompoundDrawables()[0],icon,codeBtn.getCompoundDrawables()[2],codeBtn.getCompoundDrawables()[0]);
//            codeBtn.setOnClickListener( new View.OnClickListener( ) {
//                @Override
//                public void onClick(View v) {
//                    if (isFolder(filesPath+btnContent))
//                        startShowFolderActivity(filesPath+btnContent+"/");
//                    else
//                        startShowPdfActivity(filesPath+btnContent);                }
//            });

//            index++;

//            tableRow.addView(codeBtn);
//            //if it's the 3rd button of one row, add this row to the table
//            if (indexInRow==2)  mButnsLayout.addView(tableRow);
//            indexInRow=(indexInRow+1)%3;

//            menuButton.setButtonBottomMargin(500);
//            menuButton.setPieceHorizontalMargin(5000);
//            menuButton.setLayoutParams(gl);

//            ArrayList<BoomButtonBuilder> builders=new ArrayList<BoomButtonBuilder>();
//            builders.add(BuilderManager.getSimpleCircleButtonBuilder());
//            menuButton.setBuilders(builders);
//            menuButton.setButtonHorizontalMargin(800);
//            menuButton.setBoomEnum(PARABOLA_2 );
//            menuButton.setButtonEnum(TextOutsideCircle);
//            menuButton.setPiecePlaceEnum( DOT_3_3);
//            menuButton.setOrderEnum(DEFAULT);
//            menuButton.setHideEaseEnum(EaseOutSine);
//            menuButton.setShowEaseEnum(EaseOutSine);
//            menuButton.setButtonPlaceEnum(SC_4_2);
            Log.v("EaseActivity","creating the button ****************");
            menuButton.setButtonEnum(TextOutsideCircle);
            menuButton.setSubFiles(file.listFiles());
            menuButton.setBackgroundEffect(true);
            if (file.listFiles()!=null) {

                menuButton.setPiecePlaceEnum(file.list().length);
                menuButton.setButtonPlaceEnum(file.list().length);
            }

            TextView textView=new TextView(getContext());
            textView.setText(file.getName());
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout linearLayout=new LinearLayout(getContext());
            linearLayout.addView(menuButton);
            linearLayout.addView(textView);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
            GridLayout.LayoutParams gl = new GridLayout.LayoutParams(layoutParams);
            gl.rightMargin = 20;
            gl.leftMargin = 20;


//            for (int i = 0; i < 9; i++)
//                menuButton.addBuilder(BuilderManager.getSimpleCircleButtonBuilder());

            //initBmb(menuButton);
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
}
