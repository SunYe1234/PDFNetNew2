//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.demo.navigation.adapter.viewholder;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.pdftron.demo.R;


public class FooterViewHolder extends RecyclerView.ViewHolder {

    ProgressBar mProgressBar;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public FooterViewHolder(View itemView) {
        super(itemView);
        mProgressBar = itemView.findViewById(R.id.footer_progress_bar);
    }
}

