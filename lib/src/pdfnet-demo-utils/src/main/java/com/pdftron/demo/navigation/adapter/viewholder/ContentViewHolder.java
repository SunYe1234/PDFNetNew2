//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------


package com.pdftron.demo.navigation.adapter.viewholder;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdftron.demo.R;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.demo.widget.ImageViewTopCrop;

public class ContentViewHolder extends RecyclerView.ViewHolder {

    public ImageViewTopCrop imageViewFileIcon;
    public ImageView imageViewFileLockIcon;
    public TextView docTextPlaceHolder;
    public TextView textViewFileName;
    public TextView textViewFileInfo;
    public ImageView imageViewInfoIcon;
    public View infoButton;
    public ImageView divider;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public ContentViewHolder(View itemView) {
        super(itemView);
        imageViewFileIcon = itemView.findViewById(R.id.file_icon);
        imageViewFileLockIcon = itemView.findViewById(R.id.file_lock_icon);
        docTextPlaceHolder = itemView.findViewById(R.id.docTextPlaceHolder);
        textViewFileName = itemView.findViewById(R.id.file_name);
        textViewFileInfo = itemView.findViewById(R.id.file_info);
        imageViewInfoIcon = itemView.findViewById(R.id.info_icon);
        infoButton = itemView.findViewById(R.id.info_button);
        divider = itemView.findViewById(R.id.divider);

        if (Utils.isJellyBeanMR1() && textViewFileName != null && textViewFileInfo != null) {
            // instead of creating a different layout for v17 we set alignment in the code:
            if (textViewFileName.getGravity() != Gravity.CENTER) {
                textViewFileName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            }
            textViewFileName.setTextDirection(View.TEXT_DIRECTION_LTR);
            textViewFileInfo.setTextDirection(View.TEXT_DIRECTION_LOCALE);
        }
    }
}
