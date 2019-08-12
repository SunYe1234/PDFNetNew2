package com.pdftron.completereader;

import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pdftron.common.PDFNetException;
import com.pdftron.completereader.loginSignup.RegisterActivity;
import com.pdftron.completereader.loginSignup.SQLiteHelper;
import com.pdftron.demo.app.AdvancedReaderActivity;
import com.pdftron.demo.app.SimpleReaderActivity;
import com.pdftron.pdf.PDFNet;
import com.pdftron.pdf.config.PDFViewCtrlConfig;
import com.pdftron.pdf.config.ToolManagerBuilder;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.controls.DiffActivity;
import com.pdftron.pdf.utils.AppUtils;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
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
    EditText Email, Password ;
    String EmailHolder, PasswordHolder;
    Boolean EditTextEmptyHolder;
    SQLiteDatabase sqLiteDatabaseObj;
    SQLiteHelper sqLiteHelper;
    Cursor cursor;
    String TempPassword = "NOT_FOUND" ;
    public static String UserName;
    URL url;
    public static  File UserFolder=null;
    public static File UsersFile=null;
    public static String usersNameFileName="UserName.txt";
    public static String formerUserNameFileName="FormerUserName.txt";

    //public static String PDFcps="/storage/emulated/0/Download/PDFcps/";
    public static String PDFcps;
    public static String cpsHome="/Download/PDFcps/";
    //public static String cpsHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Intent intent = getIntent();
        Uri uri = intent.getData();
        try {
             url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        setContentView(R.layout.activity_main1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //extSdcardPath is the path of the external SD card which depends on the exact SD card we use
        String extSdcardPath = getExtSDCardPath();

        //intnStoragePath is the path of the internal storage of the tablet
        String intnStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        //create the path of PDFcps of all users, cpsHome is the name of root directory where we save users directories
        //cpsHome is "/Download/PDFcps/" by default, it needs to be modified manually if we want to change it
        PDFcps=intnStoragePath+cpsHome;

        LogInButton = (Button)findViewById(R.id.buttonLogin);

        RegisterButton = (Button)findViewById(R.id.buttonRegister);

        Email = (EditText)findViewById(R.id.editEmail);
        Password = (EditText)findViewById(R.id.editPassword);

        sqLiteHelper = new SQLiteHelper(this);

        //Adding click listener to log in button.
        LogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Calling EditText is empty or no method.
                CheckEditTextStatus();


                if(LoginFunction()) {
                    saveUserNameToFile();
                    if (sqLiteDatabaseObj.isOpen())
                        sqLiteDatabaseObj.close();
                    openAdvancedReaderActivity();
                }




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
    save the current user name to username.txt under Files and create user folder if it doesn't exist yet
     */
    private void saveUserNameToFile() {

        File fileDir = getFilesDir();
        String destFileName=UserName+".txt";
        File destFile=new File(destFileName);
        long size=destFile.length();


        try {

            FileOutputStream outStream=this.openFileOutput(usersNameFileName,Context.MODE_PRIVATE);
            outStream.write(new String(UserName).getBytes());
            outStream.flush();
            outStream.close();
            //getUserNameFromFile();

             UserFolder=new File(PDFcps+UserName);
             boolean exits=UserFolder.exists();
            if (!UserFolder.exists()) {
                File PDFcpsFolder=new File(this.PDFcps);
                if (!PDFcpsFolder.exists())
                    PDFcpsFolder.mkdir();
                UserFolder.mkdir();
            }
            exits=UserFolder.exists();
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
            System.out.println("以字符为单位读取文件内容，一次读一个字节：");
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
                //getUserNameFromFile();


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


    private void openAdvancedReaderActivity() {
        PdfViewCtrlSettingsManager.setMultipleTabs(this, true);
        AdvancedReaderActivity.setDebug(BuildConfig.DEBUG);
        AdvancedReaderActivity.open(this);
    }
    // Login function starts from here.
    public boolean LoginFunction(){

        if(EditTextEmptyHolder) {

            // Opening SQLite database write permission.
            //sqLiteDatabaseObj =  openOrCreateDatabase(SQLiteHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
            if (sqLiteDatabaseObj==null||!sqLiteDatabaseObj.isOpen())
                sqLiteDatabaseObj = SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);
            //openOrCreateDatabase(SQLiteHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);

            // Adding search email query to cursor.
           //cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, " " + SQLiteHelper.Table_Column_1_Name + "=?", new String[]{EmailHolder}, null, null, null);
            //cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, null, null, null, null, null);
            //String sql="select * from UserTable";
            String sql = "select * from "+SQLiteHelper.TABLE_NAME+"  where name ='" + EmailHolder + "'; ";
            cursor = sqLiteDatabaseObj.rawQuery(sql, null);
            while (cursor.moveToNext()) {

                if (cursor.isFirst()) {

                    cursor.moveToFirst();

                    // Storing Password associated with entered email.
                    TempPassword = cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_3_Password));
                    UserName=cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_1_Name));

                    // Closing cursor.
                    cursor.close();
                }
            }

            // Calling method to check final result ..
            if (!CheckFinalResult())
                return false;
            else return true;

        }
        else {

            //If any of login EditText empty then this block will be executed.
            Toast.makeText(MainActivity.this,"Please Enter UserName or Password.", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    // Checking EditText is empty or not.
    public void CheckEditTextStatus(){

        // Getting value from All EditText and storing into String Variables.
        EmailHolder = Email.getText().toString();
        PasswordHolder = Password.getText().toString();
        UserName=Email.getText().toString();

        // Checking EditText is empty or no using TextUtils.
        if( TextUtils.isEmpty(EmailHolder) || TextUtils.isEmpty(PasswordHolder)){

            EditTextEmptyHolder = false ;

        }
        else {

            EditTextEmptyHolder = true ;
        }
    }

    // Checking entered password from SQLite database email associated password.
    public boolean CheckFinalResult(){

        if(TempPassword.equalsIgnoreCase(PasswordHolder))
        {

            Toast.makeText(MainActivity.this,"Login Successfully", Toast.LENGTH_LONG).show();

            // Going to Dashboard activity after login success message.
            Intent intent = new Intent(MainActivity.this, AdvancedReaderActivity.class);
            //Intent intent = new Intent(MainActivity.this, AdvancedReaderActivity.class);

            // Sending Email to Dashboard Activity using intent.
            intent.putExtra("userName",UserName );
            intent.setClass(this,AdvancedReaderActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;


        }
        else {

            Toast.makeText(MainActivity.this,"UserName or Password is Wrong, Please Try Again.", Toast.LENGTH_LONG).show();
            return false;
        }
        //TempPassword = "NOT_FOUND" ;

    }

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



}
