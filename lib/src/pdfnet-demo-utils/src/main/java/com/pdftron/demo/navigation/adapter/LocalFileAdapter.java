//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.demo.navigation.adapter;

import android.content.Context;

import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.FavoriteFilesManager;
import com.pdftron.pdf.utils.FileInfoManager;
import com.pdftron.pdf.widget.recyclerview.ViewHolderBindListener;

import java.util.ArrayList;
import java.util.List;

public class LocalFileAdapter extends BaseFileAdapter<FileInfo> {
    private static final String TAG = LocalFileAdapter.class.getName();

    public LocalFileAdapter(Context context, ArrayList<FileInfo> objects, Object objectsLock,
                            int spanCount,
                            AdapterListener adapterListener, ViewHolderBindListener bindListener) {
        super(context, objects, objectsLock, spanCount, adapterListener, bindListener);
    }

    @Override
    public boolean isHeader(int position) {
        List<FileInfo> files = getItems();
        if (files == null || position < 0 || position >= files.size()) {
            return false;
        }
        FileInfo fileInfo = files.get(position);
        return fileInfo.isHeader();
    }

    protected FileInfoManager getFileInfoManager() {
        return FavoriteFilesManager.getInstance();
    }

    @Override
    public boolean isFavoriteFile(int position, FileInfo fileInfo) {
        Context context = getContext();
        return context != null && getFileInfoManager().containsFile(context, fileInfo);
    }
}
