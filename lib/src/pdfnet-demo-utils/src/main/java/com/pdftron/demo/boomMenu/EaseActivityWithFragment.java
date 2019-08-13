package com.pdftron.demo.boomMenu;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pdftron.demo.R;
import com.pdftron.demo.boomMenu.Animation.Ease;
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;

import java.io.File;

public class EaseActivityWithFragment extends Fragment {
    private EaseFragment formerFragment;
    private static String cpsHome="/Download/PDFcps/";
    private  String user;

    public static EaseActivityWithFragment newInstance(String currentUser){
        EaseActivityWithFragment easeActivityWithFragment=new EaseActivityWithFragment();
        easeActivityWithFragment.setUser(currentUser);
        return easeActivityWithFragment;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_ease_fragment);
        if (savedInstanceState == null) {
            EaseFragment easeFragment=new EaseFragment();
            if (user!=null)
                easeFragment.setFilesPath(getUserDirectory(user));
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.frameLayout, easeFragment)
//                    //.addToBackStack("fname")
//                    .commit();
            getChildFragmentManager().beginTransaction().add(R.id.frameLayout, easeFragment).commit();
        }


    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.activity_ease_fragment, container, false);

    }

    public void setUser(String currentUser)
    {
        this.user=currentUser;
    }

    public void saveFormerFragment(EaseFragment easeFragment)
    {
        this.formerFragment=easeFragment;
    }

    public Fragment getCurrentFragment()
    {
        return getChildFragmentManager().findFragmentById(R.id.frameLayout);
    }

    public void onBackPressed()
    {
        EaseFragment currentFragment=null;
        PdfViewCtrlTabHostFragment currentPdfFragment=null;
        if (getCurrentFragment() instanceof EaseFragment)
         currentFragment=(EaseFragment) getCurrentFragment();
        else if (getCurrentFragment() instanceof PdfViewCtrlTabHostFragment)
            currentPdfFragment=(PdfViewCtrlTabHostFragment)getCurrentFragment();
        if (currentFragment!=null) {
            String currentPath = currentFragment.getFilesPath();
            File file = new File(currentPath);
            if (file != null) {
                // File parent=new File(file.getParent());
                String name = file.getName();
                if (file.getName().equals("DOC SAT digitalisée") || (user != null && file.getName().equals(user))) {
                    Toast.makeText(getActivity().getApplicationContext(), "You are already in the root directory", Toast.LENGTH_SHORT).show();

                    return;
                }
                EaseFragment parentFrag = new EaseFragment();
                parentFrag.setFilesPath(file.getParent());
                getChildFragmentManager().beginTransaction().replace(R.id.frameLayout, parentFrag).commit();
            }
        }
        else if (currentPdfFragment!=null)
        {
            File file=currentPdfFragment.getCurrentFile();
            String name = file.getName();
            if (file.getName().equals("DOC SAT digitalisée") || (user != null && file.getName().equals(user))) {
                Toast.makeText(getActivity().getApplicationContext(), "You are already in the root directory", Toast.LENGTH_SHORT).show();

                return;
            }
            EaseFragment parentFrag = new EaseFragment();
            parentFrag.setFilesPath(file.getParent());
            getChildFragmentManager().beginTransaction().replace(R.id.frameLayout, parentFrag).commit();

        }
//        getFragmentManager().beginTransaction().replace(R.id.frameLayout,formerFragment).commit();
    }

    public void changeFragment(EaseFragment easeFragment)
    {
        getChildFragmentManager().beginTransaction().replace(R.id.frameLayout,easeFragment).commit();
    }

    public void changeFragment(PdfViewCtrlTabHostFragment easeFragment)
    {
        getChildFragmentManager().beginTransaction().replace(R.id.frameLayout,easeFragment).commit();
    }

    public String  getUserDirectory(String user)
    {
        String usersFolderParentPath= Environment.getExternalStorageDirectory().getAbsolutePath()+cpsHome;
        String userFolder= usersFolderParentPath+user;
        return userFolder;
//        EaseFragment parentFrag=new EaseFragment();
//        parentFrag.setFilesPath(userFolder);
//        getChildFragmentManager().beginTransaction().add(R.id.frameLayout,parentFrag).commit();

    }

}
