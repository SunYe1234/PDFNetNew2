package com.pdftron.demo.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pdftron.demo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {
    private ArrayList<String> usersNIIs=new ArrayList<String>();
    private SQLiteDatabase sqLiteDatabaseObj;
    private SQLiteHelper sqLiteHelper;
    private Cursor cursor;
    private EditText pdfhome;
    private Button changePdfHome;
    private Button deleteUsers;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


    }

    public void onResume()
    {
        super.onResume();
        findViewById(R.id.activity_admin).getBackground().setAlpha(130);

        setUsersNIIs();


        GridLayout gridLayout=findViewById(R.id.users_list);

        TextView titreUserList=findViewById(R.id.users_list_titre);
        titreUserList.setTextSize(23);
        titreUserList.setHeight(40);
        titreUserList.setGravity(Gravity.CENTER);

        TextView titrePdfhome=findViewById(R.id.pdfhome_titre);
        titrePdfhome.setTextSize(23);
//        titrePdfhome.setHeight(40);
        titrePdfhome.setGravity(Gravity.CENTER);

         pdfhome=findViewById(R.id.pdfhome);
        pdfhome.setTextSize(20);
        pdfhome.setGravity(Gravity.CENTER);
        pdfhome.setHint(getPdfhomeName());
        pdfhome.clearFocus();
        pdfhome.setMaxLines(1);


        setButtons();
        if (gridLayout.getChildCount()>0)
            return;
        if (usersNIIs.size()==0)
        {
            TextView textView=new TextView(this);
            textView.setText("Aucun utilisateur pour le moment");
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(30);
            textView.setHeight(400);
            textView.setMaxLines(5);
            gridLayout.addView(textView);

            deleteUsers=findViewById(R.id.delete_users);
            deleteUsers.setClickable(false);
            deleteUsers.setTextColor(getResources().getColor(R.color.tools_colors_white));

        }
        for (int i=0;i<usersNIIs.size();i++)
        {
            TextView textView=new TextView(this);
            textView.setText(usersNIIs.get(i));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(20);
            textView.setHeight(60);

            gridLayout.addView(textView);
        }


    }


    private void setButtons()
    {
         changePdfHome=findViewById(R.id.change_pdfhome);
        changePdfHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPdfHome=pdfhome.getText().toString();
                if (TextUtils.isEmpty(newPdfHome))
                {
                    Toast.makeText(AdminActivity.this,"Désolé, vous n'avez rien saisi.", Toast.LENGTH_LONG).show();
                    return;
                }
                savePdfhomeNameToFile(newPdfHome);
                Toast.makeText(AdminActivity.this,"Vous avec configuré le répertoire racine de consignes avec succès", Toast.LENGTH_LONG).show();


            }
        });

        deleteUsers=findViewById(R.id.delete_users);
        deleteUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogDeleteAccountsCopies();
            }
        });

    }

    private void setUsersNIIs()
    {
//        usersNIIs=new ArrayList<String>();
//        for (int i=0;i<400;i++)
//        {
//            usersNIIs.add(""+i);
//        }


        if (sqLiteDatabaseObj==null||!sqLiteDatabaseObj.isOpen())
            sqLiteDatabaseObj = SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);


        String sql = "select * from "+SQLiteHelper.TABLE_NAME+"; ";
        try{
            cursor = sqLiteDatabaseObj.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                    usersNIIs.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_1_NNI)));

                    // Closing cursor.

            }


        }catch (Exception e)
        {
            e.printStackTrace();

        }
    }

    /**
     * Opens the AdminActivity demo app.
     *
     * @param packageContext the context
     */
    public static void open(Context packageContext) {
        Intent intent = new Intent(packageContext, AdvancedReaderActivity.class);
        packageContext.startActivity(intent);
    }


    private String getPdfhomeName()
    {
        File pdfHomeNameFile=new File(getFilesDir().getAbsolutePath()+"/"+getString(R.string.file_save_pdf_home));
        if (!pdfHomeNameFile.exists())
        {
//            pdfHomeNameFile.mkdir();
            return getString(R.string.emplty_pdfhome_file);
        }
        try {
            FileInputStream inputStream = openFileInput(getString(R.string.file_save_pdf_home));
            // read a character each time
            InputStreamReader reader = new InputStreamReader(inputStream);
            String pdfhome = "";
            int tempchar;
            while ((tempchar = reader.read()) != -1) {

                if (((char) tempchar) != '\r') {
                    System.out.print((char) tempchar);
                    pdfhome += (char) tempchar;
                }

            }
            reader.close();
            return pdfhome;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  null;
    }

    /*
  save the current user name to username.txt under Files and create user folder if it doesn't exist yet
   */
    private void savePdfhomeNameToFile(String pdfHome) {

        try {

            FileOutputStream outStream=this.openFileOutput(getString(R.string.file_save_pdf_home),Context.MODE_PRIVATE);
            outStream.write(new String(pdfHome).getBytes());
            outStream.flush();
            outStream.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

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

    private void closeKeyboard(View currentFocus) {
        if (currentFocus == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) currentFocus.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        boolean active=imm.isActive();
    }


    /**
     * show an alert dialog when user try to delete his account
     */
    private void dialogDeleteAccountsCopies()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialoge_delete_all_account_cp));

        builder.setTitle("");

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//                System.exit(0);
                deleteUserCopies();
                //delete his account in the database
                deleteUserAccount();
                GridLayout gridLayout=findViewById(R.id.users_list);
                gridLayout.removeAllViews();
                SysApplication.getInstance().exit();


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

    private void deleteUserCopies( )
    {
        File currentUserCpsDirec=new File(getFilesDir().getAbsolutePath()+"/PDFcps/");
        if (currentUserCpsDirec.isDirectory())
        {
            deleteAllFiles(currentUserCpsDirec);
        }

    }
    private void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) {
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        root.delete();
    }


    /**
     * Delete the current user account record in the database
     */
    private void deleteUserAccount()
    {
        String SQLiteDataBaseQueryHolder ;
        SQLiteHelper sqLiteHelper=new SQLiteHelper(this);
        SQLiteDatabase sqLiteDatabaseObj= SQLiteDatabase.openOrCreateDatabase(getFilesDir()+"/my.db",null);;

        SQLiteDataBaseQueryHolder = "DELETE FROM " + SQLiteHelper.TABLE_NAME + " WHERE 1=1;" ;

        // Executing query.
        sqLiteDatabaseObj.execSQL(SQLiteDataBaseQueryHolder);

        // Closing SQLite database object.
        sqLiteDatabaseObj.close();

        // Printing toast message after done inserting.
        Toast.makeText(AdminActivity.this, "Vous avez supprimé tous les comptes d'utilisateur.", Toast.LENGTH_LONG).show();



    }



}
