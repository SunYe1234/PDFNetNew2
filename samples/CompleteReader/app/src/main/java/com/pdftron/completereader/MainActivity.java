package com.pdftron.completereader;

import android.animation.ValueAnimator;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.pdftron.common.PDFNetException;
import com.pdftron.completereader.loginSignup.RegisterActivity;
import com.pdftron.completereader.loginSignup.SQLiteHelper;
import com.pdftron.demo.app.AdminActivity;
import com.pdftron.demo.app.AdvancedReaderActivity;
import com.pdftron.demo.app.SimpleReaderActivity;
import com.pdftron.demo.app.SysApplication;
import com.pdftron.pdf.PDFNet;
import com.pdftron.pdf.config.PDFViewCtrlConfig;
import com.pdftron.pdf.config.ToolManagerBuilder;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.controls.DiffActivity;
import com.pdftron.pdf.utils.AppUtils;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.PdfViewCtrlTabsManager;
import com.pdftron.pdf.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Button LogInButton, RegisterButton ;
    EditText NNI ;
    String NNIHolder;
    Boolean EditTextEmptyHolder;
    SQLiteDatabase sqLiteDatabaseObj;
    SQLiteHelper sqLiteHelper;
    Cursor cursor;
    String TempPassword = "NOT_FOUND" ;
    public static String NNIofCurrentUser;
    URL url;
    public static  File UserFolder=null;
    public static File UsersFile=null;
//    public static String usersNameFileName="UserName.txt";
    public static String usersNameFileName;
//    public static String formerUserNameFileName="FormerUserName.txt";
    public static String formerUserNameFileName;

    //public static String PDFcps="/storage/emulated/0/Download/PDFcps/";
    public static String PDFcps;
    public static String cpsHome;

    public boolean isAdmin=false;
    //public static String cpsHome;

    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main1);

        String extSdcardPath = getExtSDCardPath();
        usersNameFileName=this.getString(R.string.file_current_username);
        formerUserNameFileName=this.getString(R.string.file_former_username);

        String intnStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();


        PDFcps=getFilesDir().getAbsolutePath()+"/PDFcps";
        File cps=new File(PDFcps);
        if (!cps.exists())
        {
            cps.mkdir();
        }

        View viewCreated =findViewById(R.id.activity_main);
        viewCreated.getBackground().setAlpha(200);
        LogInButton = (Button) findViewById(R.id.buttonLogin);

        RegisterButton = (Button) findViewById(R.id.buttonRegister);

        NNI = (EditText)findViewById(R.id.editNNI);

        sqLiteHelper = new SQLiteHelper(this);

        //Adding click listener to log in button.
        LogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Calling EditText is empty or no method.
                CheckEditTextStatus();


                if(LoginFunction()&&!isAdmin) {
                    saveFormerUserName();
                    saveUserNameToFile();
                    createCpDirectory();
                    String formerUser=getFormerUserNameFromFile();
                    if (sqLiteDatabaseObj.isOpen())
                        sqLiteDatabaseObj.close();
                    openAdvancedReaderActivity();
                }
                if (isAdmin)
                    openAdminActivity();





            }
        });

        // Adding click listener to register button.
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Opening new user registration activity using intent on button click.
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

    }


    /*
    create user's own copy folder and the parent folder of all of the user folders if they don't exist yet
     */

    private void createCpDirectory()
    {
        File cpHome=new File(PDFcps);
        if (!cpHome.exists())
            cpHome.mkdir();
        File myCpHome=new File(cpHome.getAbsolutePath()+"/"+NNIofCurrentUser);
        if (!myCpHome.exists())
            myCpHome.mkdir();
        return;

    }


    /*
    save the current user name to username.txt under Files and create user folder if it doesn't exist yet
     */
    private void saveUserNameToFile() {

        try {

            FileOutputStream outStream=this.openFileOutput(usersNameFileName,Context.MODE_PRIVATE);
            outStream.write(new String(NNIofCurrentUser).getBytes());
            outStream.flush();
            outStream.close();
            //getUserNameFromFile();

             UserFolder=new File(PDFcps+NNIofCurrentUser);
             boolean exits=UserFolder.exists();
            if (!UserFolder.exists()) {
                File PDFcpsFolder=new File(this.PDFcps);
                if (!PDFcpsFolder.exists())
                    PDFcpsFolder.mkdir();
                UserFolder.mkdir();
            }
            getUserNameFromFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    /*
        get the  user name stored in username.txt under Files
         */
    public  String getUserNameFromFile()
    {
        File fileDir = getFilesDir();

        try {
            File UsersName=new File(getFilesDir().getAbsolutePath()+"/"+usersNameFileName);
            if (!UsersName.exists())
                UsersName.createNewFile();
            FileInputStream inputStream = openFileInput(usersNameFileName);
            UsersFile=new File(getFilesDir().getAbsolutePath()+"/"+usersNameFileName);
            // read a character each time
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
    private void saveFormerUserName()
    {
        String formerUserName=getUserNameFromFile();
        if (formerUserName!=null)
        {
            try {

                FileOutputStream outStream=this.openFileOutput(formerUserNameFileName,Context.MODE_PRIVATE);
                outStream.write(new String(formerUserName).getBytes());
                outStream.flush();
                outStream.close();


            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public  String getFormerUserNameFromFile()
    {
        File fileDir = getFilesDir();

        try {
            File UsersName=new File(getFilesDir().getAbsolutePath()+"/"+formerUserNameFileName);
            if (!UsersName.exists())
                UsersName.createNewFile();
            FileInputStream inputStream = openFileInput(formerUserNameFileName);
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


    private void openAdvancedReaderActivity() {
        PdfViewCtrlSettingsManager.setMultipleTabs(this, true);
        AdvancedReaderActivity.setDebug(BuildConfig.DEBUG);
        AdvancedReaderActivity.open(this);
    }

    private void openAdminActivity() {
//        AdminActivity.open(this);
        Intent intent=new Intent(this,AdminActivity.class);
        startActivity(intent);
    }


    /**
     * LoginFunction check whether the username exist and whether the password is correct
     * @return
     *  true if log in successfully
     *  false if not
     */

    public boolean LoginFunction(){

        if(EditTextEmptyHolder) {

            //if it's administrator who try to login
            if (NNIHolder.toLowerCase().equals(getString(R.string.admin_NNI)))
            {
                Toast.makeText(MainActivity.this,getString(R.string.log_in_admin), Toast.LENGTH_LONG).show();
                isAdmin=true;

                return true;
            }


            if (sqLiteDatabaseObj==null||!sqLiteDatabaseObj.isOpen())
                sqLiteDatabaseObj = SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);


            String sql = "select * from "+SQLiteHelper.TABLE_NAME+"  where "+SQLiteHelper.Table_Column_1_NNI+" ='" + NNIHolder + "'; ";
            try{
                cursor = sqLiteDatabaseObj.rawQuery(sql, null);
                //if there is no such NNI in the database, show alert message and return false
                if (cursor.getCount()==0)
                {
                    Toast.makeText(MainActivity.this,getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
                    return false;

                }
            while (cursor.moveToNext()) {

                if (cursor.isFirst()) {

                    cursor.moveToFirst();

                    NNIofCurrentUser=cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_1_NNI));

                    // Closing cursor.
                    cursor.close();
                }
            }
            }catch (Exception e)
            {
                Toast.makeText(MainActivity.this,getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
                return false;

            }


            // Calling method to check final result ..
//            if (!CheckFinalResult())
//                return false;
//            else
                return true;

        }
        else {

            //If any of login EditText empty then this block will be executed.
            Toast.makeText(MainActivity.this,getString(R.string.toast_enter_name), Toast.LENGTH_LONG).show();
            return false;
        }

    }

    // Checking EditText is empty or not.
    public void CheckEditTextStatus(){

        // Getting value from All EditText and storing into String Variables.
        NNIHolder = NNI.getText().toString();
        NNIofCurrentUser=NNI.getText().toString();

        // Checking EditText is empty or no using TextUtils.
        if( TextUtils.isEmpty(NNIHolder) ){

            EditTextEmptyHolder = false ;

        }
        else {

            EditTextEmptyHolder = true ;
        }
    }

    // Checking entered password from SQLite database email associated password.
//    public boolean CheckFinalResult(){
//
//        if(TempPassword.equalsIgnoreCase(PasswordHolder))
//        {
//
//            Toast.makeText(MainActivity.this,getString(R.string.log_in_success), Toast.LENGTH_LONG).show();
//
//            // Going to Dashboard activity after login success message.
//            Intent intent = new Intent(MainActivity.this, AdvancedReaderActivity.class);
//            //Intent intent = new Intent(MainActivity.this, AdvancedReaderActivity.class);
//
//            // Sending Email to Dashboard Activity using intent.
//            intent.putExtra("userName",NNIofCurrentUser );
//            intent.setClass(this,AdvancedReaderActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            return true;
//
//
//        }
//        else {
//
//            Toast.makeText(MainActivity.this,getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
//            return false;
//        }
//        //TempPassword = "NOT_FOUND" ;
//
//    }

    private String getExtSDCardPath()
    {
        StorageManager mStorageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            // get the current view which has the focus
            View view = this.getCurrentFocus();
//            if (isShouldHideInput(view, motionEvent)) {
                closeKeyboard(view);
//            }
            view.clearFocus();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    /**
     * decide whether we need to hide the keyboard or not according to the coordiation of the click point
     * @param view
     * @param motionEvent
     * @return
     *  true if we can ignore the click event
     *  false if not
     */
    private boolean isShouldHideInput(View view, MotionEvent motionEvent) {
        if (view != null && (view instanceof EditText)) {
            int[] l = {0, 0};
            view.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + view.getHeight(), right = left
                    + view.getWidth();
            if (motionEvent.getX() > left && motionEvent.getX() < right
                    && motionEvent.getY() > top && motionEvent.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void closeKeyboard(View currentFocus) {
        if (currentFocus == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) currentFocus.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        boolean active=imm.isActive();
    }









}




