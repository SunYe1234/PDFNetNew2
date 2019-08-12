package com.nightonke.boommenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;

public class EaseActivityWithFragment extends AppCompatActivity {
    private EaseFragment formerFragment;


    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ease_fragment);
        if (savedInstanceState == null) {
            EaseFragment easeFragment=new EaseFragment();
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.frameLayout, easeFragment)
//                    //.addToBackStack("fname")
//                    .commit();
            getFragmentManager().beginTransaction().add(R.id.frameLayout, easeFragment).commit();
        }


    }

    public void saveFormerFragment(EaseFragment easeFragment)
    {
        this.formerFragment=easeFragment;
    }

    public EaseFragment getCurrentFragment()
    {
        return (EaseFragment) getFragmentManager().findFragmentById(R.id.frameLayout);
    }

    public void onBackPressed()
    {
        EaseFragment currentFragment=getCurrentFragment();
        String currentPath=currentFragment.getFilesPath();
        File file=new File(currentPath);
        if (file.getParent()!=null)
        {
            File parent=new File(file.getParent());
            String name=parent.getName();
            if (file.getName().equals("DOC SAT digitalisée"))
            {
                Toast.makeText(getApplicationContext(), "You are already in the root directory", Toast.LENGTH_SHORT).show();

                return;
            }
            EaseFragment parentFrag=new EaseFragment();
            parentFrag.setFilesPath(file.getParent());
            getFragmentManager().beginTransaction().replace(R.id.frameLayout,parentFrag).commit();
        }
//        getFragmentManager().beginTransaction().replace(R.id.frameLayout,formerFragment).commit();
    }

}
