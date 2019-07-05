package com.pdftron.pdf.dialog.measure;

import android.os.Parcel;
import android.os.Parcelable;

public class CalibrateResult implements Parcelable {

    public Float userInput;
    public String worldUnit;
    public long annot;
    public int page;

    public CalibrateResult(long annot, int page) {
        this.annot = annot;
        this.page = page;
    }

    protected CalibrateResult(Parcel in) {
        userInput = in.readFloat();
        annot = in.readLong();
        page = in.readInt();
        worldUnit = in.readString();
    }

    public static final Creator<CalibrateResult> CREATOR = new Creator<CalibrateResult>() {
        @Override
        public CalibrateResult createFromParcel(Parcel in) {
            return new CalibrateResult(in);
        }

        @Override
        public CalibrateResult[] newArray(int size) {
            return new CalibrateResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(userInput);
        dest.writeLong(annot);
        dest.writeInt(page);
        dest.writeString(worldUnit);
    }
}
