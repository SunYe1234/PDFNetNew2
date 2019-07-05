package com.pdftron.pdf.dialog.pagelabel;

import android.os.Parcel;
import android.os.Parcelable;

import com.pdftron.pdf.PageLabel;

/**
 * Data class containing information needed to modify page labels
 */
public final class PageLabelSetting implements Parcelable {

    public static final Parcelable.Creator<PageLabelSetting> CREATOR = new Parcelable.Creator<PageLabelSetting>() {
        @Override
        public PageLabelSetting createFromParcel(Parcel source) {
            return new PageLabelSetting(source);
        }

        @Override
        public PageLabelSetting[] newArray(int size) {
            return new PageLabelSetting[size];
        }
    };
    final int selectedPage;
    final int numPages;

    private boolean mIsAll = false;
    private boolean mSelectedPage = true; // default use currently selected page
    private int mFromPage = 1;
    private int mToPage = 1;
    private PageLabelStyle mStyle = PageLabelStyle.values()[0];
    private String mPrefix = "";
    private int mStartNum = 1;

    PageLabelSetting(int selectedPage, int numPages) {
        this(selectedPage, selectedPage, numPages);
    }

    PageLabelSetting(int fromPage, int toPage, int numPages) {
        this(fromPage, toPage, numPages, "");
    }

    PageLabelSetting(int fromPage, int toPage, int numPages, String prefix) {
        this.mFromPage = fromPage;
        this.mToPage = toPage;
        this.selectedPage = fromPage;
        this.numPages = numPages;
        this.mPrefix = prefix;

        // Since we specified range, do not make this the default
        this.mSelectedPage = false;
        this.mIsAll = false;
    }

    public int getFromPage() {
        return mFromPage;
    }

    public int getToPage() {
        return mToPage;
    }

    public int getStartNum() {
        return mStartNum;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public int getPageLabelStyle() {
        return mStyle.mPageLabelStyle;
    }

    boolean isAll() {
        return mIsAll;
    }

    void setAll(boolean all) {
        mIsAll = all;
    }

    boolean isSelectedPage() {
        return mSelectedPage;
    }

    void setSelectedPage(boolean selectedPage) {
        this.mSelectedPage = selectedPage;
    }

    void setFromPage(int fromPage) {
        this.mFromPage = fromPage;
    }

    void setToPage(int toPage) {
        this.mToPage = toPage;
    }

    PageLabelStyle getStyle() {
        return mStyle;
    }

    void setStyle(PageLabelStyle style) {
        this.mStyle = style;
    }

    void setPrefix(String prefix) {
        this.mPrefix = prefix;
    }

    void setStartNum(int startNum) {
        this.mStartNum = startNum;
    }

    protected PageLabelSetting(Parcel in) {
        this.selectedPage = in.readInt();
        this.numPages = in.readInt();
        this.mIsAll = in.readByte() != 0;
        this.mSelectedPage = in.readByte() != 0;
        this.mFromPage = in.readInt();
        this.mToPage = in.readInt();
        int tmpMStyle = in.readInt();
        this.mStyle = tmpMStyle == -1 ? null : PageLabelStyle.values()[tmpMStyle];
        this.mPrefix = in.readString();
        this.mStartNum = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.selectedPage);
        dest.writeInt(this.numPages);
        dest.writeByte(this.mIsAll ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mSelectedPage ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mFromPage);
        dest.writeInt(this.mToPage);
        dest.writeInt(this.mStyle == null ? -1 : this.mStyle.ordinal());
        dest.writeString(this.mPrefix);
        dest.writeInt(this.mStartNum);
    }

    enum PageLabelStyle {
        DECIMAL("1, 2, 3", PageLabel.e_decimal),
        ROMAN_UPPER("I, II, III", PageLabel.e_roman_uppercase),
        ROMAN_LOWER("i, ii, iii", PageLabel.e_roman_lowercase),
        ALPHA_UPPER("A, B, C", PageLabel.e_alphabetic_uppercase),
        ALPHA_LOWER("a, b, c", PageLabel.e_alphabetic_lowercase),
        NONE("None", PageLabel.e_none);

        final String mLabel;
        final int mPageLabelStyle;

        PageLabelStyle(String label, int pageLabelStyle) {
            this.mLabel = label;
            this.mPageLabelStyle = pageLabelStyle;
        }
    }
}
