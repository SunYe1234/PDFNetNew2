package com.pdftron.completereader.loginSignup;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pdftron.completereader.MainActivity;
import com.pdftron.completereader.R;
import com.pdftron.demo.app.SysApplication;

import java.util.regex.Pattern;

//import com.example.shiva.loginsignup.RegisterActivity

public class RegisterActivity extends AppCompatActivity {

    //public static boolean NameHolder;
    EditText NNI;
    Button Register;
    String NameHolder;
    Boolean EditTextEmptyHolder=false;
    Boolean wrongEditTextFormat;
    SQLiteDatabase sqLiteDatabaseObj;
    String SQLiteDataBaseQueryHolder;
    SQLiteHelper sqLiteHelper;
    Cursor cursor;
    String F_Result = "Not_Found";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);

        setContentView(R.layout.activity_register);



        Register = (Button) findViewById(R.id.buttonRegister);

        NNI = (EditText) findViewById(R.id.editNNI);

        sqLiteHelper = new SQLiteHelper(this);

        // Adding click listener to register button.
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Creating SQLite database if dose n't exists
                SQLiteDataBaseBuild();

                // Creating SQLite table if dose n't exists.
                boolean noTable = userTableExist();
                if (!userTableExist())
                    SQLiteTableBuild();

                // Checking EditText is empty or Not.
                CheckEditTextStatus();

                // Method to check Email is already exists or not.
                CheckingUserAlreadyExistsOrNot();
                //Sending confirmation email.
                Confirm();

                // Empty EditText After done inserting process.
                EmptyEditTextAfterDataInsert();
                if (F_Result.equalsIgnoreCase("Not_Found")&&!wrongEditTextFormat)
                    goBackToLoginPage();


            }
        });

    }

    public void Confirm() {
        if (sqLiteDatabaseObj.isOpen())
        sqLiteDatabaseObj.close();

    }

    public boolean userTableExist() {
        boolean result = false;
        if (SQLiteHelper.TABLE_NAME == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"+SQLiteHelper.TABLE_NAME.trim()+"';";
            cursor = sqLiteDatabaseObj.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }

    // SQLite database build method.
    public void SQLiteDataBaseBuild() {

        //sqLiteDatabaseObj = openOrCreateDatabase(SQLiteHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
        sqLiteDatabaseObj=SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);
    }

    // SQLite table build method.
    public void SQLiteTableBuild() {

        //sqLiteDatabaseObj.execSQL("CREATE TABLE IF NOT EXISTS " + SQLiteHelper.TABLE_NAME + "(" + SQLiteHelper.Table_Column_ID + "  PRIMARY KEY  NOT NULL, " + SQLiteHelper.Table_Column_1_Name + " VARCHAR, " + SQLiteHelper.Table_Column_2_Email + " VARCHAR, " + SQLiteHelper.Table_Column_3_Password + " VARCHAR);");
        // sqLiteDatabaseObj.execSQL("DROP TABLE if exists "+SQLiteHelper.TABLE_NAME+";");
        sqLiteDatabaseObj.execSQL("CREATE TABLE IF NOT EXISTS " + SQLiteHelper.TABLE_NAME + "(" + SQLiteHelper.Table_Column_1_NNI + " VARCHAR PRIMARY KEY); " );

    }






    // Insert data into SQLite database method.
    public void InsertDataIntoSQLiteDatabase() {

        // If editText is not empty then this block will executed.
        if (EditTextEmptyHolder == true&&!wrongEditTextFormat) {

            // SQLite query to insert data into table.
            SQLiteDataBaseQueryHolder = "INSERT INTO " + SQLiteHelper.TABLE_NAME + " ("+SQLiteHelper.Table_Column_1_NNI+") VALUES('" + NameHolder + "'); " ;

            // Executing query.
            sqLiteDatabaseObj.execSQL(SQLiteDataBaseQueryHolder);

            // Closing SQLite database object.
            sqLiteDatabaseObj.close();

            // Printing toast message after done inserting.
            Toast.makeText(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_LONG).show();

        }
        if (wrongEditTextFormat)
        {
            Toast.makeText(RegisterActivity.this, "Désolé, mauvais format de NNI. Réessayez s'il vous plaît. ", Toast.LENGTH_LONG).show();
            return;

        }
        // This block will execute if any of the registration EditText is empty.
        if (!EditTextEmptyHolder){

            // Printing toast message if any of EditText is empty.
            Toast.makeText(RegisterActivity.this, getString(R.string.fill_all_blank), Toast.LENGTH_LONG).show();

        }

    }



    // Empty edittext after done inserting process method.
    public void EmptyEditTextAfterDataInsert() {

        NNI.getText().clear();



    }

    // Method to check EditText is empty or Not.
    public void CheckEditTextStatus() {

        // Getting value from All EditText and storing into String Variables.
        NameHolder = NNI.getText().toString();

        if (TextUtils.isEmpty(NameHolder) ) {

            EditTextEmptyHolder = false;

        }


        else {

            EditTextEmptyHolder = true;
        }
        if(NameHolder.toCharArray().length!=6||!CheckEditTextPattern(NameHolder))
            wrongEditTextFormat=true;
        else wrongEditTextFormat=false;
    }

    private boolean CheckEditTextPattern(String s)
    {
        Pattern pattern = Pattern.compile("^[a-zA-Z][0-9]{5}$");
        return pattern.matcher(s).matches();
    }

    // Checking Email is already exists or not.
    public void CheckingUserAlreadyExistsOrNot() {

        // Opening SQLite database write permission.
       // sqLiteDatabaseObj = sqLiteHelper.getWritableDatabase();
        //sqLiteDatabaseObj=SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);

        // Adding search email query to cursor.
        cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, " " + SQLiteHelper.Table_Column_1_NNI + "=?", new String[]{NameHolder}, null, null, null);
        //cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, null, null, null, null, null);

//        while (cursor.moveToNext()) {
//
//            if (cursor.isFirst()) {
//
//                cursor.moveToFirst();

        int count=cursor.getCount();
            if (cursor.getCount()>0) {
                // If Email is already exists then Result variable value set as Email Found.
                F_Result = "NNI Found";

                // Closing cursor.
                cursor.close();
            }
//            }
//        }

        // Calling method to check final result and insert data into SQLite database.
        CheckFinalResult();

    }


    // Checking result
    public void CheckFinalResult() {

        // Checking whether email is already exists or not.
        if (F_Result.equalsIgnoreCase("NNI Found")) {

            // If email is exists then toast msg will display.
            Toast.makeText(RegisterActivity.this, getString(R.string.user_exits), Toast.LENGTH_LONG).show();
            F_Result="Not_Found";

        } else {

            // If email already dose n't exists then user registration details will entered to SQLite database.
            InsertDataIntoSQLiteDatabase();
            F_Result = "Not_Found";
            //goBackToLoginPage();

        }


    }
    private void goBackToLoginPage ()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹球或者实体按键会移动焦点）
            View view = this.getCurrentFocus();
            if (isShouldHideInput(view, motionEvent)) {
                closeKeyboard(view);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    /**
     * Decide whether need to hide the keyboard by the coordination of the clicked point
     *
     * @param view
     * @param motionEvent
     * @return
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