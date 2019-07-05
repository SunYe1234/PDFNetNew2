package com.pdftron.pdf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.StampManager;
import com.pdftron.pdf.widget.recyclerview.SimpleRecyclerViewAdapter;
import com.pdftron.pdf.widget.recyclerview.ViewHolderBindListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SavedSignatureAdapter extends SimpleRecyclerViewAdapter<Bitmap, SavedSignatureAdapter.ViewHolder> {

    private WeakReference<Context> mContextRef;
    private List<WeakReference<Bitmap>> mBitmapsRef = new ArrayList<>();

    private List<File> mSignatureFiles = new ArrayList<>();

    public SavedSignatureAdapter(@NonNull Context context, ViewHolderBindListener bindListener) {
        super(bindListener);

        mContextRef = new WeakReference<>(context);
        File[] files = StampManager.getInstance().getSavedSignatures(context);
        if (files != null) {
            mSignatureFiles = new ArrayList<>(Arrays.asList(files));
            int count = files.length;
            for (int i = 0; i < count; ++i) {
                mBitmapsRef.add(new WeakReference<Bitmap>(null));
            }
        }
    }

    public void setBitmaps(Bitmap[] bitmaps) {
        if (bitmaps == null) {
            return;
        }
        if (mSignatureFiles.size() == bitmaps.length) {
            for (int i=0;i<bitmaps.length; i++) {
                Bitmap bitmap = bitmaps[i];
                mBitmapsRef.set(i, new WeakReference<>(bitmap));
            }
        }

        notifyDataSetChanged();
    }

    public File getFileItem(int position) {
        if (position < 0 || position >= mSignatureFiles.size()) {
            return null;
        }
        return mSignatureFiles.get(position);
    }

    @Override
    public Bitmap getItem(int position) {
        if (position < 0 || position >= mBitmapsRef.size()) {
            return null;
        }
        Context context = mContextRef.get();
        if (context == null) {
            return null;
        }
        return mBitmapsRef.get(position).get();
    }

    @Override
    public void add(Bitmap item) {

    }

    @Override
    public boolean remove(Bitmap item) {
        return false;
    }

    @Override
    public Bitmap removeAt(int position) {
        File file = mSignatureFiles.get(position);
        boolean success = file.delete();
        if (success) {
            mSignatureFiles.remove(position);
            return mBitmapsRef.remove(position).get();
        }
        return null;
    }

    @Override
    public void insert(Bitmap item, int position) {

    }

    @Override
    public void updateSpanCount(int count) {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_rubber_stamp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.mImageView.setImageBitmap(getItem(position));
    }

    @Override
    public int getItemCount() {
        return mSignatureFiles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.stamp_image_view);
        }

    }
}
