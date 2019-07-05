package com.pdftron.showcase

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pdftron.showcase.helpers.setPicassoDrawable
import com.pdftron.showcase.models.Feature
import java.util.*


class CategoryDetailAdapter(private val context: Context, private val mFeatures: ArrayList<Feature>, private var onItemClick: ((Feature) -> Unit)) :
        RecyclerView.Adapter<CategoryDetailAdapter.ViewHolder>() {

    // Disable touch detection for parent recyclerView if we use vertical nested recyclerViews
    private val mTouchListener = View.OnTouchListener { v, event ->
        v.parent.requestDisallowInterceptTouchEvent(true)
        false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feature_detail, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = mFeatures[position]
        holder.apply{
            description.text = feature.cardDescription
            nameLabel.text = feature.name
            imageView.setPicassoDrawable("thumbnail_" + feature.imageName!!)
        }
    }

    override fun getItemCount(): Int {
        return mFeatures.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameLabel: TextView
        var imageView: ImageView
        var description: TextView

        init {
            itemView.setOnClickListener {
                onItemClick.invoke(mFeatures[adapterPosition])
            }
            nameLabel = itemView.findViewById(R.id.feature_name) as TextView
            description = itemView.findViewById(R.id.feature_description) as TextView
            imageView = itemView.findViewById(R.id.imageView) as ImageView
        }
    }
}
