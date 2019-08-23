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

//import com.example.shiva.loginsignup.RegisterActivity

public class RegisterActivity extends AppCompatActivity {

    //public static boolean NameHolder;
    EditText Email, Password, Name;
    Button Register;
    String NameHolder, PasswordHolder;
    Boolean EditTextEmptyHolder;
    SQLiteDatabase sqLiteDatabaseObj;
    String SQLiteDataBaseQueryHolder;
    SQLiteHelper sqLiteHelper;
    Cursor cursor;
    String F_Result = "Not_Found";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);


//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.fade);
//        getWindow().setEnterTransition(fade);

        setContentView(R.layout.activity_register);



        Register = (Button) findViewById(R.id.buttonRegister);

        Email = (EditText) findViewById(R.id.editEmail);
        Password = (EditText) findViewById(R.id.editPassword);
        Name = (EditText) findViewById(R.id.editName);

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
                CheckingEmailAlreadyExistsOrNot();
                //Sending confirmation email.
                Confirm();

                // Empty EditText After done inserting process.
                EmptyEditTextAfterDataInsert();
                if (F_Result.equalsIgnoreCase("Not_Found"))
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
        sqLiteDatabaseObj.execSQL("CREATE TABLE IF NOT EXISTS " + SQLiteHelper.TABLE_NAME + "(" + SQLiteHelper.Table_Column_1_Name + " VARCHAR PRIMARY KEY, " + SQLiteHelper.Table_Column_3_Password + " VARCHAR);");

    }

    // Insert data into SQLite database method.
    public void InsertDataIntoSQLiteDatabase() {

        // If editText is not empty then this block will executed.
        if (EditTextEmptyHolder == true) {

            // SQLite query to insert data into table.
            SQLiteDataBaseQueryHolder = "INSERT INTO " + SQLiteHelper.TABLE_NAME + " (name,password) VALUES('" + NameHolder + "', '" + PasswordHolder + "');";

            // Executing query.
            sqLiteDatabaseObj.execSQL(SQLiteDataBaseQueryHolder);
//            cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, null, null, null, null, null);
//
//            while (cursor.moveToNext()) {
//
//                if (cursor.isFirst()) {
//
//                    cursor.moveToFirst();
//
//                    // If Email is already exists then Result variable value set as Email Found.
//                    F_Result = "Email Found";
//
//                    // Closing cursor.
//                    cursor.close();
//                }
//            }
            // Closing SQLite database object.
            sqLiteDatabaseObj.close();

            // Printing toast message after done inserting.
            Toast.makeText(RegisterActivity.this, "User Registered Successfully", Toast.LENGTH_LONG).show();

        }
        // This block will execute if any of the registration EditText is empty.
        else {

            // Printing toast message if any of EditText is empty.
            Toast.makeText(RegisterActivity.this, "Please Fill All The Required Fields.", Toast.LENGTH_LONG).show();

        }

    }

    // delete data in the SQLite database .
    public void DeleteDataInSQLiteDatabase() {


        // SQLite query to insert data into table.
        SQLiteDataBaseQueryHolder = "DELETE FROM " + SQLiteHelper.TABLE_NAME + " WHERE " + SQLiteHelper.Table_Column_1_Name + " = '" + NameHolder + "';";

        // Executing query.
        sqLiteDatabaseObj.execSQL(SQLiteDataBaseQueryHolder);

        // Closing SQLite database object.
        sqLiteDatabaseObj.close();

        // Printing toast message after done inserting.
        Toast.makeText(RegisterActivity.this, "User deleted Successfully", Toast.LENGTH_LONG).show();


    }


    // Empty edittext after done inserting process method.
    public void EmptyEditTextAfterDataInsert() {

        Name.getText().clear();


        Password.getText().clear();

    }

    // Method to check EditText is empty or Not.
    public void CheckEditTextStatus() {

        // Getting value from All EditText and storing into String Variables.
        NameHolder = Name.getText().toString();
        PasswordHolder = Password.getText().toString();

        if (TextUtils.isEmpty(NameHolder) || TextUtils.isEmpty(PasswordHolder)) {

            EditTextEmptyHolder = false;

        } else {

            EditTextEmptyHolder = true;
        }
    }

    // Checking Email is already exists or not.
    public void CheckingEmailAlreadyExistsOrNot() {

        // Opening SQLite database write permission.
       // sqLiteDatabaseObj = sqLiteHelper.getWritableDatabase();
        //sqLiteDatabaseObj=SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);

        // Adding search email query to cursor.
        cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, " " + SQLiteHelper.Table_Column_1_Name + "=?", new String[]{NameHolder}, null, null, null);
        //cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, null, null, null, null, null);

        while (cursor.moveToNext()) {

            if (cursor.isFirst()) {

                cursor.moveToFirst();

                // If Email is already exists then Result variable value set as Email Found.
                F_Result = "Email Found";

                // Closing cursor.
                cursor.close();
            }
        }

        // Calling method to check final result and insert data into SQLite database.
        CheckFinalResult();

    }


    // Checking result
    public void CheckFinalResult() {

        // Checking whether email is already exists or not.
        if (F_Result.equalsIgnoreCase("Email Found")) {

            // If email is exists then toast msg will display.
            Toast.makeText(RegisterActivity.this, "Email Already Exists", Toast.LENGTH_LONG).show();

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
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏 stone
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
                // 点击EditText的事件，忽略它
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(getApplicationContext(), "BACK key is forbidden",Toast.LENGTH_LONG).show();
            return true;
        }  else if (keyCode == KeyEvent.KEYCODE_HOME) {
            Toast.makeText(getApplicationContext(), "HOME key is forbidden",
                    Toast.LENGTH_LONG).show();
// 屏蔽Home键
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}