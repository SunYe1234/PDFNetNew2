package com.pdftron.demo.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pdftron.demo.BuildConfig;
import com.pdftron.demo.R;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*Intent intent = getIntent();
        Uri uri = intent.getData();
        try {
             url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        setContentView(R.layout.activity_main1);

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

//                Intent intent = new Intent(MainActivity.this, CompleteReaderActivity.class);
//                startActivity(intent);
                saveUserNameToFile();
                if(LoginFunction())
                openCompleteReaderActivity();
                // Calling login method.
                //LoginFunction();


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
    save the current user name to username.txt under Files
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

             UserFolder=new File("/storage/emulated/0/Download/PDFcps/"+UserName);
            if (!UserFolder.exists())
                    UserFolder.mkdir();
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
    private void openCompleteReaderActivity() {
        PdfViewCtrlSettingsManager.setMultipleTabs(this, true);
        AdvancedReaderActivity.setDebug(BuildConfig.DEBUG);
        AdvancedReaderActivity.open(this);
    }
    // Login function starts from here.
    public boolean LoginFunction(){

        if(EditTextEmptyHolder) {

            // Opening SQLite database write permission.
            //sqLiteDatabaseObj = sqLiteHelper.getWritableDatabase();
            sqLiteDatabaseObj=SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);


            // Adding search email query to cursor.
            cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, " " + SQLiteHelper.Table_Column_1_Name + "=?", new String[]{EmailHolder}, null, null, null);

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

}
