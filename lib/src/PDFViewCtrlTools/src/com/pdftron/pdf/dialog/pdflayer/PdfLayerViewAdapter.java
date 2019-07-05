package com.pdftron.pdf.dialog.pdflayer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pdftron.pdf.ocg.Group;
import com.pdftron.pdf.tools.R;

import java.util.ArrayList;

/**
 * Adapter for {@link PdfLayerView}.
 */
public class PdfLayerViewAdapter extends RecyclerView.Adapter<PdfLayerViewAdapter.ViewHolder> {

    @NonNull
    final private ArrayList<PdfLayerUtils.LayerInfo> mLayers;

    public PdfLayerViewAdapter(@NonNull ArrayList<PdfLayerUtils.LayerInfo> layers) {
        mLayers = layers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_pdf_layer_item, viewGroup, false);
        return new PdfLayerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        try {
            Group group = mLayers.get(i).group;
            boolean checked = mLayers.get(i).checked;
            viewHolder.mSwitch.setText(group.getName());
            viewHolder.mSwitch.setChecked(checked);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Nullable
    public PdfLayerUtils.LayerInfo getItem(int i) {
        return mLayers.get(i);
    }

    @Override
    public int getItemCount() {
        return mLayers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        SwitchCompat mSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mSwitch = itemView.findViewById(R.id.layer_switch);
        }
    }
}
