package com.pdftron.pdf.dialog.pdflayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.widget.recyclerview.ItemClickHelper;
import com.pdftron.pdf.widget.recyclerview.SimpleRecyclerView;

import java.util.ArrayList;

/**
 * PDF layer view.
 */
public class PdfLayerView extends LinearLayout {

    private SimpleRecyclerView mRecyclerView;
    private PdfLayerViewAdapter mAdapter;
    private ItemClickHelper mItemClickHelper;

    public PdfLayerView(Context context) {
        this(context, null);
    }

    public PdfLayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PdfLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_pdf_layer, this);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setOrientation(VERTICAL);

        mRecyclerView = findViewById(R.id.layer_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mItemClickHelper = new ItemClickHelper();
        mItemClickHelper.attachToRecyclerView(mRecyclerView);
    }

    public void setup(@NonNull ArrayList<PdfLayerUtils.LayerInfo> layers) {
        mAdapter = new PdfLayerViewAdapter(layers);
        mRecyclerView.setAdapter(mAdapter);
    }

    @NonNull
    public SimpleRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Nullable
    public PdfLayerViewAdapter getAdapter() {
        return mAdapter;
    }

    @NonNull
    public ItemClickHelper getItemClickHelper() {
        return mItemClickHelper;
    }
}
