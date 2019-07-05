//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.demo.navigation.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pdftron.demo.R;
import com.pdftron.demo.model.FileHeader;
import com.pdftron.demo.navigation.adapter.viewholder.HeaderViewHolder;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.FavoriteFilesManager;
import com.pdftron.pdf.utils.FileInfoManager;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.recyclerview.ViewHolderBindListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AllFilesAdapter extends BaseFileAdapter<FileInfo> {
    private AllFilesAdapterHelper mHelper;

    private HashMap<String, List<FileInfo>> headerChildMap = new HashMap<>();

    private class HeaderClickListener implements View.OnClickListener {
        HeaderViewHolder viewHolder;

        HeaderClickListener(HeaderViewHolder vh) {
            viewHolder = vh;
        }

        @Override
        public void onClick(View v) {
            int myPos = viewHolder.getAdapterPosition();
            if (myPos == RecyclerView.NO_POSITION) {
                return;
            }
            clickHeader(myPos);
        }
    }

    public AllFilesAdapter(Context context, int spanCount, AdapterListener adapterListener, ViewHolderBindListener bindListener) {
        super(context, new ArrayList<FileInfo>(), null, spanCount, adapterListener, bindListener);
        mHelper = new AllFilesAdapterHelper(
            new AllFilesAdapterHelper.NotifyAdapterListener() {
                @Override
                public void notifyItemInserted(int idx) {
                    AllFilesAdapter.this.notifyItemInserted(idx);
                }

                @Override
                public void notifyItemRangeInserted(int idx, int size) {
                    AllFilesAdapter.this.notifyItemRangeInserted(idx, size);
                }

                @Override
                public void notifyItemRemoved(int idx) {
                    AllFilesAdapter.this.notifyItemRemoved(idx);
                }

                @Override
                public void notifyItemRangeRemoved(int idx, int size) {
                    AllFilesAdapter.this.notifyItemRangeRemoved(idx, size);
                }

                @Override
                public void addChildrenToMap(String header, List<FileInfo> children) {
                    headerChildMap.put(header, children);
                }
            }
        );
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

    public void setFiles(List<FileInfo> files) {
        mFiles.clear();
        mFiles.addAll(files);
        notifyItemRangeInserted(0, mFiles.size());
    }

    @Override
    public boolean isFavoriteFile(int position, FileInfo fileInfo) {
        Context context = getContext();
        return context != null && getFileInfoManager().containsFile(context, fileInfo);
    }

    /**
     * Add a group of {@link FileInfo} under the same header
     *
     * @param files group of {@link FileInfo} with the same header/folder
     */
    public void addGroupedFiles(List<FileInfo> files) {
        mHelper.addGroupedFiles(mFiles, files, mSpanCount);
    }

    /**
     * Clear files from the adapter.
     */
    public void clearFiles() {
        mHelper.clearFiles(mFiles);
    }

    /**
     * Delete {@link FileInfo} from the adapter, keeping in mind of the header for this file
     *
     * @param fileInfo to delete from the adapter
     */
    public void deleteFile(FileInfo fileInfo) {
        mHelper.deleteFile(mFiles, fileInfo, mSpanCount);
    }

    /**
     * See {@link #deleteFile(FileInfo)}
     *
     * @param files to delete from the adapter
     */
    public void deleteFiles(List<FileInfo> files) {
        for (FileInfo file : files) {
            mHelper.deleteFile(mFiles, file, mSpanCount);
        }
    }

    /**
     * Add {@link FileInfo} to adapter, keeping n mind of the sort mode and header for this file
     *
     * @param fileInfo to add to adapter
     * @param sortMode comparator used to locate the position to add the file
     */
    public void addFile(FileInfo fileInfo, Comparator<FileInfo> sortMode) {
        mHelper.addFile(mFiles, fileInfo, sortMode, mSpanCount);
    }

    /**
     * See {@link #addFile(FileInfo, Comparator)}.
     *
     * @param files     to add to adapter
     * @param mSortMode comparator used to locate the position to add the files
     */
    public void addFiles(List<FileInfo> files, Comparator<FileInfo> mSortMode) {
        for (FileInfo file : files) {
            mHelper.addFile(mFiles, file, mSortMode, mSpanCount);
        }
    }

    /**
     * Asynchronously save all the header information (i.e. expanded state)
     */
    public void saveHeaders() {
        mHelper.saveHeaders();
    }

    protected FileInfoManager getFileInfoManager() {
        return FavoriteFilesManager.getInstance();
    }

    private void expandChildView(int headerPos, List<FileInfo> childInfoList) {

        for (int i = 0, cnt = childInfoList.size(); i < cnt; i++) {
            FileInfo child = childInfoList.get(i);
            insert(child, headerPos + 1 + i);
        }
        Utils.safeNotifyItemChanged(this, headerPos);
        notifyItemRangeInserted(headerPos + 1, childInfoList.size());
        childInfoList.clear();
        FileHeader header = (FileHeader) getItem(headerPos);
        if (header != null) {
            header.setCollapsed(false);
        }
    }

    private void collapseChildView(String header, int headerPos) {
        List<FileInfo> children;
        if (headerChildMap.containsKey(header)) {
            //noinspection ConstantConditions
            headerChildMap.get(header).clear();
            children = headerChildMap.get(header);
        } else {
            children = new ArrayList<>();
            headerChildMap.put(header, children);
        }

        if (children == null) {
            return;
        }

        // while next pos is a content, remove it
        while (headerPos + 1 < mFiles.size() && getItemViewType(headerPos + 1) == VIEW_TYPE_CONTENT) {
            FileInfo child = getItem(headerPos + 1);
            if (child != null) {
                children.add(child);
                mFiles.remove(child);
                remove(child);
            }
        }
        Utils.safeNotifyItemChanged(this, headerPos);
        notifyItemRangeRemoved(headerPos + 1, children.size());
    }

    public void clickHeader(int myPos) {
        if (getItemViewType(myPos) != VIEW_TYPE_HEADER || !(getItem(myPos) instanceof FileHeader)) {
            return;
        }
        FileHeader header = (FileHeader) getItem(myPos);
        header.setCollapsed(true);
        String myDir = header.getAbsolutePath();
        int childCount = getChildViewCount(myPos);

        if (childCount <= 0) { // no child
            List<FileInfo> childInfoList = headerChildMap.get(myDir);

            if (childInfoList == null || childInfoList.isEmpty()) {
                return;
            }
            expandChildView(myPos, childInfoList);
            return;
        }

        // has children
        collapseChildView(myDir, myPos);
    }

    private int getChildViewCount(int headerPosition) {
        int childCount = 0;
        int i = 1;
        while (headerPosition + i < mFiles.size() && getItemViewType(headerPosition + i) == VIEW_TYPE_CONTENT) {
            childCount++;
            i++;
        }
        return childCount;
    }

    public int findHeader(int contentPosition) {
        int i = contentPosition - 1;
        while (i >= 0) {
            if (getItemViewType(i) == VIEW_TYPE_HEADER) {
                return i;
            }
            i--;
        }
        return i;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder holder;

        switch (viewType) {
            case VIEW_TYPE_HEADER:
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View headerView = inflater.inflate(mHeaderLayoutResourceId, parent, false);
                holder = new HeaderViewHolder(headerView);
                final HeaderViewHolder headHolder = (HeaderViewHolder) holder;
                headHolder.header_view.setOnClickListener(new HeaderClickListener(headHolder));
                return holder;
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                onBindViewHolderHeader(holder, position);
                break;
            default:
                super.onBindViewHolder(holder, position);
        }
    }

    private void onBindViewHolderHeader(final RecyclerView.ViewHolder holder, int position) {
        final FileInfo file = mFiles.get(position);
        final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        String headerTxt = file.getAbsolutePath();
        headerViewHolder.textViewTitle.setText(headerTxt);
        FileHeader header = (FileHeader) file;
        if (header.getCollapsed()) {
            headerViewHolder.foldingBtn.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
            headerViewHolder.divider.setVisibility(View.VISIBLE);
        } else {
            headerViewHolder.foldingBtn.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            headerViewHolder.divider.setVisibility(View.GONE);
        }
    }
}
