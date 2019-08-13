package com.pdftron.demo.boomMenu.BoomButtons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.pdftron.demo.R;
import com.pdftron.demo.app.AdvancedReaderActivity;
//import com.pdftron.demo.app.CheckDifferentFileTypeResult;
import com.pdftron.demo.boomMenu.ButtonEnum;
import com.pdftron.demo.boomMenu.EaseActivityWithFragment;
import  com.pdftron.demo.boomMenu.EaseFragment;
import com.pdftron.demo.boomMenu.Util;
import com.pdftron.demo.navigation.FileBrowserViewFragment;
import com.pdftron.demo.navigation.callbacks.MainActivityListener;
import com.pdftron.demo.utils.FileManager;
import com.pdftron.pdf.config.ViewerBuilder;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnalyticsParam;
import com.pdftron.pdf.utils.FavoriteFilesManager;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.utils.PdfViewCtrlTabsManager;
import com.pdftron.pdf.utils.RecentFilesManager;
import com.pdftron.pdf.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import static com.pdftron.pdf.controls.AnnotStyleDialogFragment.TAG;
import static com.pdftron.pdf.controls.PdfViewCtrlTabFragment.BUNDLE_TAB_TITLE;

/**
 * Created by Weiping Huang at 22:44 on 16/11/23
 * For Personal Open Source
 * Contact me at 2584541288@qq.com or nightonke@outlook.com
 * For more projects: https://github.com/Nightonke
 */

@SuppressWarnings("unused")
public class TextOutsideCircleButton extends BoomButton {
    private boolean touchable = true;
    private PdfViewCtrlTabHostFragment mPdfViewCtrlTabHostFragment;


    private TextOutsideCircleButton(Builder builder, Context context) {
        super(context);
        this.context = context;
        this.buttonEnum = ButtonEnum.TextOutsideCircle;
        init(builder);
    }

    private void init(Builder builder) {
        LayoutInflater.from(context).inflate(R.layout.bmb_text_outside_circle_button, this, true);
        initAttrs(builder);
        initTextOutsideCircleButtonLayout();
        if (isRound) initShadow(buttonRadius + shadowRadius);
        else initShadow(shadowCornerRadius);
        initCircleButton();
        initText(layout);
        initImage();
        centerPoint = new PointF(trueRadius, trueRadius);
    }


    @SuppressLint("NewApi")
    protected void initCircleButton() {
        button = (FrameLayout) findViewById(R.id.button);
        LayoutParams params = (LayoutParams) button.getLayoutParams();
        params.width = buttonRadius * 2;
        params.height = buttonRadius * 2;
        button.setLayoutParams(params);
        button.setEnabled(!unable);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!touchable) return;
                if (listener != null) listener.onButtonClick(index, TextOutsideCircleButton.this);
                if (onBMClickListener != null) onBMClickListener.onBoomButtonClick(index);
            }
        });

        initCircleButtonDrawable();

        button.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!touchable) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (Util.pointInView(new PointF(event.getX(), event.getY()), button)) {
                            toHighlighted();
                            ableToHighlight = true;
                            File file=new File(normalText);
                            if (file.isDirectory()) {
                                if (file.listFiles()==null||file.listFiles().length==0) {
                                    Toast.makeText(getContext(), "no documents inside", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
//                                Intent intent = new Intent(getContext(), EaseActivityWithFragment.class);
//                                intent.putExtra("path", normalText);
//                                getContext().startActivity(intent);
//                                Context context=getContext();
//
//                                if (context instanceof EaseActivityWithFragment)
//                                {
                                    EaseFragment easeFragment=new EaseFragment();
                                    easeFragment.setFilesPath(normalText);
                                ((EaseActivityWithFragment)(EaseFragment.getFatherFragment())).changeFragment(easeFragment);
//                                    EaseActivityWithFragment eActWithFragment=(EaseActivityWithFragment) context;
//                                    eActWithFragment.saveFormerFragment(eActWithFragment.getCurrentFragment());
//                                eActWithFragment.getChildFragmentManager().beginTransaction().replace(R.id.frameLayout,easeFragment).commit();
//                                }

                            }
                            else{
                                FileInfo fileInfo=new FileInfo(BaseFileInfo.FILE_TYPE_FILE,file);
                                AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_OPEN_FILE,
                                        AnalyticsParam.openFileParam(fileInfo, AnalyticsHandlerAdapter.SCREEN_FOLDERS));
                                onFileSelected(file,"",true);
//                                if (mCallbacks != null) {
//                                    mCallbacks.onFileSelected(fileInfo.getFile(), "");
//                                }
                            }

                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Util.pointInView(new PointF(event.getX(), event.getY()), button)) {
                            toHighlighted();
                        } else {
                            ableToHighlight = false;
                            toNormal();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        ableToHighlight = false;
                        toNormal();
                        break;
                }
                return false;
            }
        });
    }

    private void initAttrs(Builder builder) {
        super.initAttrs(builder);
    }

    @Override
    public ButtonEnum type() {
        return ButtonEnum.TextOutsideCircle;
    }

    @Override
    public ArrayList<View> goneViews() {
        ArrayList<View> goneViews = new ArrayList<>();
        goneViews.add(image);
        goneViews.add(text);
        return goneViews;
    }

    @Override
    public ArrayList<View> rotateViews() {
        ArrayList<View> rotateViews = new ArrayList<>();
        if (rotateImage) rotateViews.add(image);
        if (rotateText) rotateViews.add(text);
        return rotateViews;
    }

    @Override
    public int trueWidth() {
        return trueRadius * 2;
    }

    @Override
    public int trueHeight() {
        return trueRadius * 2;
    }

    @Override
    public int contentWidth() {
        return buttonRadius * 2;
    }

    @Override
    public int contentHeight() {
        return buttonRadius * 2;
    }

    @Override
    public void toHighlighted() {
        if (lastStateIsNormal && ableToHighlight) {
            toHighlightedImage();
            toHighlightedText();
            lastStateIsNormal = false;
        }
    }

    @Override
    public void toNormal() {
        if (!lastStateIsNormal) {
            toNormalImage();
            toNormalText();
            lastStateIsNormal = true;
        }
    }

    @Override
    public void setRotateAnchorPoints() {
        image.setPivotX(buttonRadius - imageRect.left);
        image.setPivotY(buttonRadius - imageRect.top);
        text.setPivotX(trueRadius - textRect.left);
        text.setPivotY(trueRadius - textRect.top);
    }

    @Override
    public void setSelfScaleAnchorPoints() {

    }

    public static class Builder extends BoomButtonWithTextBuilder<Builder> {

        /**
         * Whether the text-view should rotate.
         *
         * @param rotateText rotate or not
         * @return the builder
         */
        public Builder rotateText(boolean rotateText) {
            this.rotateText = rotateText;
            return this;
        }

        /**
         * Set the top-margin between text-view and the circle button.
         * Don't need to mind the shadow, BMB will mind this in code.
         *
         * @param textTopMargin top-margin between text-view and the circle button, bigger or
         *                      equal to zero.
         * @return the builder
         */
        public Builder textTopMargin(int textTopMargin) {
            if (textTopMargin < 0) textTopMargin = 0;
            this.textTopMargin = textTopMargin;
            return this;
        }

        /**
         * The width of text-view.
         *
         * @param textWidth width of text-view
         * @return the builder
         */
        public Builder textWidth(int textWidth) {
            this.textWidth = textWidth;
            return this;
        }

        /**
         * The height of text-view.
         *
         * @param textHeight height of text-view
         * @return the builder
         */
        public Builder textHeight(int textHeight) {
            this.textHeight = textHeight;
            return this;
        }

        /**
         * The radius of boom-button, in pixel.
         *
         * @param buttonRadius the button radius
         * @return the builder
         */
        public Builder buttonRadius(int buttonRadius) {
            this.buttonRadius = buttonRadius;
            return this;
        }

        /**
         * Set the corner-radius of button.
         *
         * @param buttonCornerRadius corner-radius of button
         * @return the builder
         */
        public Builder buttonCornerRadius(int buttonCornerRadius) {
            this.buttonCornerRadius = buttonCornerRadius;
            return this;
        }

        /**
         * Whether the button is a circle shape.
         *
         * @param isRound is or not
         * @return the builder
         */
        public Builder isRound(boolean isRound) {
            this.isRound = isRound;
            return this;
        }

        /**
         * Get the height of content.
         * This method is used to calculate the position of boom-button on the screen,
         * don't use it outside.
         *
         * @return the width of content in text-outside-circle-button
         */
        public int getButtonContentWidth() {
            int width = buttonRadius * 2;
            if (textRect != null) width = Math.max(width, textWidth);
            return width;
        }

        /**
         * Get the height of content.
         * This method is used to calculate the position of boom-button on the screen,
         * don't use it outside.
         *
         * @return the height of content in text-outside-circle-button
         */
        public int getButtonContentHeight() {
            int height = buttonRadius * 2;
            if (textRect != null) height = Math.max(height, textRect.bottom - shadowOffsetY - shadowRadius);
            return height;
        }

        /**
         * Gets button radius.
         *
         * @return the button radius
         */
        public int getButtonRadius() {
            return buttonRadius;
        }

        /**
         * Build text-inside circle button, only used in BMB package.
         *
         * @param context the context
         * @return the simple circle button
         */
        @Override
        public TextOutsideCircleButton build(Context context) {
            TextOutsideCircleButton button = new TextOutsideCircleButton(this, context);
            weakReferenceButton(button);
            return button;
        }




    }


    /**
     * Opens a local document in the document viewer.
     *
     * @param file that represents a local document
     * @param password password used to open document, can be null
     * @param skipPasswordCheck true to skip password check when opening the document
     */
    public void onFileSelected(final File file, String password, boolean skipPasswordCheck) {
        boolean openedSucessfully = false;
        if (file != null && file.exists()) {
            if (Utils.isNullOrEmpty(password)) {
                password = Utils.getPassword(getContext(), file.getAbsolutePath());
            }

//            if (Utils.isOfficeDocument(file.getAbsolutePath())) {
//                startTabHostFragment(
//                        ViewerBuilder.withFile(file, password)
//                                .usingFileType(BaseFileInfo.FILE_TYPE_FILE)
//                );
//
//                return;
//            }

//            CheckDifferentFileTypeResult result = checkDifferentFileType(file, password, BaseFileInfo.FILE_TYPE_FILE, "", skipPasswordCheck);
//
//            if (result.getOpenDocument()) {
//                // Perform any operation needed on the current fragment before launching the viewer.
//                if (mCurrentFragment != null && hasMainActivityListener(mCurrentFragment)) {
//                    ((MainActivityListener) mCurrentFragment).onPreLaunchViewer();
//                }
//            }

//            if (result.getOpenDocument() // PDF document
//                    || FileManager.checkIfFileTypeIsInList(file.getAbsolutePath())) { // non-PDF document
            startTabHostFragment(
                    ViewerBuilder.withFile(file, password)
                            .usingFileType(BaseFileInfo.FILE_TYPE_FILE),file
            );
            openedSucessfully = true;
//            }
//        } else {
//            Utils.showAlertDialog(this, R.string.file_does_not_exist_message, R.string.error_opening_file);
//        }

//        if (!openedSucessfully) {
//            // Update recent and favorite files lists
//            FileInfo fileInfo = new FileInfo(BaseFileInfo.FILE_TYPE_FILE, file);
//            RecentFilesManager.getInstance().removeFile(this, fileInfo);
//            FavoriteFilesManager.getInstance().removeFile(this, fileInfo);
//            if (file != null) {
//                PdfViewCtrlTabsManager.getInstance().removePdfViewCtrlTabInfo(this, file.getAbsolutePath());
//            }
//
//            // Try to update fragment since underlying data has changed
//            reloadBrowser();
//
//            onOpenDocError();
//        }
        }
    }


    private void startTabHostFragment(@Nullable ViewerBuilder viewerBuilder,File file) {
//        if (isFinishing()) {
//            return;
//        }
//        if (null == findViewById(R.id.frameLayout)) {
//            // wrong states
//            return;
//        }

        if (viewerBuilder == null) {
            viewerBuilder = ViewerBuilder.withUri(null, "");
        }

//        viewerBuilder.usingCacheFolder(mUseCacheDir)
//                .usingQuitAppMode(mQuitAppWhenDoneViewing);
//
//        Bundle args = viewerBuilder.createBundle(this);
//        if (args.containsKey(BUNDLE_TAB_TITLE)) {
//            String title = args.getString(BUNDLE_TAB_TITLE);
//            if (mLastAddedBrowserFragment != null && mLastAddedBrowserFragment instanceof FileBrowserViewFragment) {
//                ((FileBrowserViewFragment) mLastAddedBrowserFragment).setCurrentFile(title);
//            }
//        }
//        mProcessedFragmentViewId = R.id.item_viewer;
//        selectNavItem(mProcessedFragmentViewId);


//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.setCustomAnimations(R.anim.tab_fragment_slide_in_bottom, R.anim.tab_fragment_slide_out_bottom);
        mPdfViewCtrlTabHostFragment = viewerBuilder.build(getContext());
        mPdfViewCtrlTabHostFragment.setCurrentFile(file);
        mPdfViewCtrlTabHostFragment.addHostListener((AdvancedReaderActivity)EaseFragment.getFatherFragment().getActivity());

        Logger.INSTANCE.LogD(TAG, "replace with " + mPdfViewCtrlTabHostFragment);
//        ft.replace(R.id.container, mPdfViewCtrlTabHostFragment, null);
        ((EaseActivityWithFragment)(EaseFragment.getFatherFragment())).changeFragment(mPdfViewCtrlTabHostFragment);
//        ft.commit();

//        setCurrentFragment(mPdfViewCtrlTabHostFragment);

//        updateNavTab(); // update navigation tab in case the activity will be resumed
//
//        toggleInfoDrawer(false);
    }
}
