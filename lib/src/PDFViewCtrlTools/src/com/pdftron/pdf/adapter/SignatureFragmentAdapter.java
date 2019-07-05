package com.pdftron.pdf.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pdftron.pdf.dialog.CreateSignatureFragment;
import com.pdftron.pdf.dialog.SavedSignaturePickerFragment;
import com.pdftron.pdf.interfaces.OnCreateSignatureListener;
import com.pdftron.pdf.interfaces.OnSavedSignatureListener;
import com.pdftron.pdf.tools.R;

public class SignatureFragmentAdapter extends FragmentPagerAdapter {

    private static final String TAG = SignatureFragmentAdapter.class.getName();

    private final String mStandardTitle;
    private final String mCustomTitle;
    private Toolbar mToolbar, mCabToolbar;

    private int mColor;
    private float mStrokeWidth;

    private Fragment mCurrentFragment;

    private boolean mShowSavedSignatures;
    private boolean mShowSignatureFromImage;

    private int mConfirmBtnStrRes;
    private boolean mIsPressureSensitive = true;

    private OnCreateSignatureListener mOnCreateSignatureListener;
    private OnSavedSignatureListener mOnSavedSignatureListener;

    public SignatureFragmentAdapter(FragmentManager fm, String standardTitle, String customTitle,
            @NonNull Toolbar toolbar, @NonNull Toolbar cabToolbar,
            int color, float thickness,
            boolean showSavedSignatures,
            boolean showSignatureFromImage,
            int confirmBtnStrRes,
            OnCreateSignatureListener onCreateSignatureListener,
            OnSavedSignatureListener onSavedSignatureListener,
            boolean isPressureSensitive) {
        super(fm);
        mStandardTitle = standardTitle;
        mCustomTitle = customTitle;
        mToolbar = toolbar;
        mCabToolbar = cabToolbar;
        mColor = color;
        mStrokeWidth = thickness;
        mShowSavedSignatures = showSavedSignatures;
        mShowSignatureFromImage = showSignatureFromImage;
        mOnCreateSignatureListener = onCreateSignatureListener;
        mOnSavedSignatureListener = onSavedSignatureListener;
        mConfirmBtnStrRes = confirmBtnStrRes;
        mIsPressureSensitive = isPressureSensitive;
    }

    public SignatureFragmentAdapter(FragmentManager fm, String standardTitle, String customTitle,
                                    @NonNull Toolbar toolbar, @NonNull Toolbar cabToolbar,
                                    int color, float thickness,
                                    boolean showSavedSignatures,
                                    boolean showSignatureFromImage,
                                    int confirmBtnStrRes,
                                    OnCreateSignatureListener onCreateSignatureListener,
                                    OnSavedSignatureListener onSavedSignatureListener) {
        super(fm);
        mStandardTitle = standardTitle;
        mCustomTitle = customTitle;
        mToolbar = toolbar;
        mCabToolbar = cabToolbar;
        mColor = color;
        mStrokeWidth = thickness;
        mShowSavedSignatures = showSavedSignatures;
        mShowSignatureFromImage = showSignatureFromImage;
        mOnCreateSignatureListener = onCreateSignatureListener;
        mOnSavedSignatureListener = onSavedSignatureListener;
        mConfirmBtnStrRes = confirmBtnStrRes;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        Fragment fragment = (Fragment) object;
        if (mCurrentFragment != fragment) {
            mCurrentFragment = fragment;
            if (mCurrentFragment instanceof SavedSignaturePickerFragment) {
                ((SavedSignaturePickerFragment) mCurrentFragment).setOnSavedSignatureListener(mOnSavedSignatureListener);
                ((SavedSignaturePickerFragment) mCurrentFragment).resetToolbar(container.getContext());
                MenuItem menuEdit = mToolbar.getMenu().findItem(R.id.controls_action_edit);
                menuEdit.setTitle(R.string.tools_qm_edit);
            } else if (mCurrentFragment instanceof CreateSignatureFragment) {
                ((CreateSignatureFragment) mCurrentFragment).setOnCreateSignatureListener(mOnCreateSignatureListener);
                ((CreateSignatureFragment) mCurrentFragment).resetToolbar(container.getContext());
                MenuItem menuEdit = mToolbar.getMenu().findItem(R.id.controls_action_edit);
                menuEdit.setTitle(mConfirmBtnStrRes);
            }
        }

        mToolbar.setVisibility(View.VISIBLE);
        mCabToolbar.setVisibility(View.GONE);
    }

    @Override
    public Fragment getItem(int position) {
        if (mShowSavedSignatures) {
            switch (position) {
                case 0:
                    SavedSignaturePickerFragment pickerFragment = SavedSignaturePickerFragment.newInstance();
                    pickerFragment.setToolbars(mToolbar, mCabToolbar);
                    pickerFragment.setOnSavedSignatureListener(mOnSavedSignatureListener);
                    return pickerFragment;
                case 1:
                    CreateSignatureFragment signatureFragment = CreateSignatureFragment.newInstance(mColor,
                            mStrokeWidth, mShowSignatureFromImage, mIsPressureSensitive);
                    signatureFragment.setOnCreateSignatureListener(mOnCreateSignatureListener);
                    signatureFragment.setToolbar(mToolbar);
                    return signatureFragment;
                default:
                    return null;
            }
        } else {
            CreateSignatureFragment signatureFragment = CreateSignatureFragment.newInstance(mColor,
                    mStrokeWidth, mShowSignatureFromImage, mIsPressureSensitive);
            signatureFragment.setOnCreateSignatureListener(mOnCreateSignatureListener);
            signatureFragment.setToolbar(mToolbar);
            return signatureFragment;
        }
    }

    @Override
    public int getCount() {
        return mShowSavedSignatures ? 2 : 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (mShowSavedSignatures) {
            switch (position) {
                case 0:
                    return mStandardTitle;
                case 1:
                    return mCustomTitle;
                default:
                    return null;
            }
        } else {
            return mCustomTitle;
        }
    }
}
