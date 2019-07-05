package com.pdftron.pdf.controls;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.pdftron.pdf.config.ViewerBuilder;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.dialog.diffing.DiffOptionsDialogFragment;
import com.pdftron.pdf.dialog.diffing.DiffUtils;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.AppUtils;
import com.pdftron.pdf.utils.PdfViewCtrlTabsManager;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.DiffOptionsView;
import com.pdftron.pdf.widget.FragmentLayout;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class DiffActivity extends AppCompatActivity implements
        PdfViewCtrlTabHostFragment.TabHostListener {

    private static String DEFAULT_FILE_1 = "DiffActivity_Default_File_1";
    private static String DEFAULT_FILE_2 = "DiffActivity_Default_File_2";

    private DiffOptionsView mDiffOptions;
    private LinearLayout mDiffLayout;
    private FragmentLayout mFragmentLayout;

    private View mSelectedView;
    private PdfViewCtrlTabHostFragment mPdfViewCtrlTabHostFragment;

    protected ArrayList<Uri> mFileUris = new ArrayList<>();
    private boolean mShouldOpenDocuments;

    private int mDefaultFile1 = 0;
    private int mDefaultFile2 = 0;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public static void open(Context packageContext) {
        Intent intent = new Intent(packageContext, DiffActivity.class);
        packageContext.startActivity(intent);
    }

    public static void open(Context packageContext, @RawRes int defaultFile1, @RawRes int defaultFile2) {
        Intent intent = new Intent(packageContext, DiffActivity.class);
        intent.putExtra(DEFAULT_FILE_1, defaultFile1);
        intent.putExtra(DEFAULT_FILE_2, defaultFile2);
        packageContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            AppUtils.initializePDFNetApplication(getApplicationContext());
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            mDefaultFile1 = getIntent().getExtras().getInt(DEFAULT_FILE_1);
            mDefaultFile2 = getIntent().getExtras().getInt(DEFAULT_FILE_2);
        }

        setContentView(R.layout.activity_diff_tool);
        mDiffLayout = findViewById(R.id.diff_layout);
        mFragmentLayout = findViewById(R.id.container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.diff_compare);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mDiffOptions = findViewById(R.id.diff_options_view);
        mDiffOptions.setAnnotationToggleVisibility(false);
        if (mDefaultFile1 != 0 && mDefaultFile2 != 0) {
            mDiffOptions.setFiles(DiffUtils.getUriInfo(this, Uri.fromFile(Utils.copyResourceToLocal(this, mDefaultFile1, "diff_1", ".pdf"))),
                    DiffUtils.getUriInfo(this, Uri.fromFile(Utils.copyResourceToLocal(this, mDefaultFile2, "diff_2", ".pdf"))));
        }
        mDiffOptions.setDiffOptionsViewListener(new DiffOptionsView.DiffOptionsViewListener() {
            @Override
            public void onSelectFile(View which) {
                mSelectedView = which;
                DiffUtils.selectFile(DiffActivity.this);
            }

            @Override
            public void onCompareFiles(final ArrayList<Uri> files) {
                compareFiles(files,
                        mDiffOptions.getColor1(),
                        mDiffOptions.getColor2(),
                        mDiffOptions.getBlendMode());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FileInfo fileInfo = DiffUtils.handleActivityResult(this, requestCode, resultCode, data);
        if (fileInfo != null) {
            mDiffOptions.handleFileSelected(fileInfo, mSelectedView);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mDisposable.clear();
    }

    private void compareFiles(ArrayList<Uri> files, int color1, int color2, int blendMode) {
        mFileUris.clear();
        mFileUris.addAll(files);

        String fileName = "pdf-diff.pdf";
        File diffFile = new File(this.getFilesDir(), fileName);
        String diffName = Utils.getFileNameNotInUse(diffFile.getAbsolutePath());
        diffFile = new File(diffName);

        mDisposable.add(DiffUtils.compareFiles(this, files, color1, color2, blendMode, diffFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(Uri uri) throws Exception {

                        mFileUris.add(uri);

                        PdfViewCtrlTabsManager.getInstance().clearAllPdfViewCtrlTabInfo(DiffActivity.this);

                        mDiffLayout.setVisibility(View.GONE);
                        mFragmentLayout.setVisibility(View.VISIBLE);
                        onDocumentSelected(uri);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
    }

    private void updateCompare(int color1, int color2, int blendMode) {
        if (mFileUris.size() != 3) {
            return;
        }

        ArrayList<Uri> files = new ArrayList<Uri>(mFileUris.subList(0, 2));

        if (mPdfViewCtrlTabHostFragment != null) {
            try {
                PdfViewCtrlTabFragment pdfViewCtrlTabFragment = mPdfViewCtrlTabHostFragment.getCurrentPdfViewCtrlFragment();
                DiffUtils.updateDiff(pdfViewCtrlTabFragment, files, color1, color2, blendMode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void onDocumentsSelected() {
        if (mFileUris.isEmpty()) {
            return;
        }
        mShouldOpenDocuments = true;
        onDocumentSelected(mFileUris.get(0));
    }

    private void onDocumentSelected(Uri fileUri) {
        ViewerBuilder builder = ViewerBuilder.withUri(fileUri, "")
                .usingConfig(getConfig())
                .usingNavIcon(R.drawable.ic_arrow_back_white_24dp)
                .usingCustomToolbar(new int[]{R.menu.diff_viewer_addon, R.menu.fragment_viewer});
        startTabHostFragment(builder);
    }

    private void startTabHostFragment(@NonNull ViewerBuilder builder) {
        if (isFinishing()) {
            return;
        }

        if (mPdfViewCtrlTabHostFragment != null) {
            mPdfViewCtrlTabHostFragment.onOpenAddNewTab(builder.createBundle(this));
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            mPdfViewCtrlTabHostFragment = builder.build(this);
            mPdfViewCtrlTabHostFragment.addHostListener(this);

            ft.replace(R.id.container, mPdfViewCtrlTabHostFragment, null);
            ft.commitAllowingStateLoss();
        }
    }

    private ViewerConfig getConfig() {
        return new ViewerConfig.Builder()
                .useSupportActionBar(false)
                .multiTabEnabled(false)
                .build();
    }

    @Override
    public void onBackPressed() {
        if (mFragmentLayout.getVisibility() == View.VISIBLE) {
            onNavButtonPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onTabHostShown() {
        if (mShouldOpenDocuments) {
            mShouldOpenDocuments = false;
            for (int i = 0; i < mFileUris.size(); i++) {
                if (i != 0) {
                    Uri fileUri = mFileUris.get(i);
                    onDocumentSelected(fileUri);
                }
            }
        }
    }

    @Override
    public void onTabHostHidden() {

    }

    @Override
    public void onLastTabClosed() {
        onNavButtonPressed();
    }

    @Override
    public void onTabChanged(String tag) {

    }

    @Override
    public void onOpenDocError() {

    }

    @Override
    public void onNavButtonPressed() {
        mDiffLayout.setVisibility(View.VISIBLE);
        mFragmentLayout.setVisibility(View.GONE);
        if (mPdfViewCtrlTabHostFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(mPdfViewCtrlTabHostFragment);
            ft.commit();
            mPdfViewCtrlTabHostFragment = null;
        }
    }

    @Override
    public void onShowFileInFolder(String fileName, String filepath, int itemSource) {

    }

    @Override
    public boolean canShowFileInFolder() {
        return false;
    }

    @Override
    public boolean canShowFileCloseSnackbar() {
        return false;
    }

    @Override
    public boolean onToolbarCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        return false;
    }

    @Override
    public boolean onToolbarPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onToolbarOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_diff_options) {
            if (mFileUris.size() >= 2) {
                DiffOptionsDialogFragment fragment = DiffOptionsDialogFragment.newInstance(
                        mFileUris.get(0), mFileUris.get(1)
                );
                fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomAppTheme);
                fragment.setDiffOptionsDialogListener(new DiffOptionsDialogFragment.DiffOptionsDialogListener() {
                    @Override
                    public void onDiffOptionsConfirmed(int color1, int color2, int blendMode) {
                        updateCompare(color1, color2, blendMode);
                    }
                });
                fragment.show(getSupportFragmentManager(), DiffOptionsDialogFragment.TAG);
            }
        }
        return false;
    }

    @Override
    public void onStartSearchMode() {

    }

    @Override
    public void onExitSearchMode() {

    }

    @Override
    public boolean canRecreateActivity() {
        return false;
    }

    @Override
    public void onTabPaused(FileInfo fileInfo, boolean isDocModifiedAfterOpening) {

    }

    @Override
    public void onJumpToSdCardFolder() {

    }

    @Override
    public void onTabDocumentLoaded(String tag) {

    }
}
