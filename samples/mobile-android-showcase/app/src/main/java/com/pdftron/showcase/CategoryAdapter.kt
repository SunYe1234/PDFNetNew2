package com.pdftron.showcase

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.pdftron.showcase.helpers.setPicassoDrawable
import com.pdftron.showcase.models.Feature
import com.pdftron.showcase.models.FeatureCategory
import java.io.Serializable
import java.util.*


class CategoryAdapter(private val context: Context, private val mCategories: ArrayList<FeatureCategory>,
                      private val mFeatures: ArrayList<Feature>, private val hasSearchText: Boolean,
                      private var onItemClick: ((Feature) -> Unit)) :
        RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.ViewHolder {
        val view: View?

        if (hasSearchText) {
            view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feature_detail, parent, false)
        } else {
            view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_list, parent, false)
        }

        return ViewHolder(view, hasSearchText)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!hasSearchText) {
            val category = mCategories[position]
            holder.nameLabel.text = category.name

            if (!hasSearchText) {
                val layout = LinearLayoutManager(holder.recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                layout.isMeasurementCacheEnabled = false

                holder.recyclerView.apply {
                    layoutManager = layout
                    setHasFixedSize(true)
                    adapter = FeatureAdapter(context, category.features, onItemClick)
                }
            }
            holder.seeAllButton.setOnClickListener {
                val intent = Intent(context, CategoryDetailActivity::class.java)
                intent.putExtra("category", category as Serializable)
                startActivity(context, intent, null)
            }
        } else {
            val feature = mFeatures[position]
            holder.apply {
                description.text = feature.cardDescription
                nameLabel.text = feature.name
                imageView.setPicassoDrawable("thumbnail_" + feature.imageName!!)
            }
        }

    }

    override fun getItemCount(): Int {
        if (hasSearchText) {
            return mFeatures.size
        }
        return mCategories.size
    }

    inner class ViewHolder(itemView: View, hasSearchText: Boolean) : RecyclerView.ViewHolder(itemView) {

        var nameLabel: TextView
        lateinit var recyclerView: RecyclerView
        lateinit var imageView: ImageView
        lateinit var description: TextView
        lateinit var seeAllButton: Button

        init {
            if (hasSearchText) {
                nameLabel = itemView.findViewById(R.id.feature_name) as TextView
                description = itemView.findViewById(R.id.feature_description) as TextView
                imageView = itemView.findViewById(R.id.imageView) as ImageView
                itemView.setOnClickListener {
                    onItemClick.invoke(mFeatures[adapterPosition])
                }
            } else {
                nameLabel = itemView.findViewById(R.id.nameLabel) as TextView
                if (!hasSearchText) {
                    recyclerView = itemView.findViewById<View>(R.id.recyclerView) as RecyclerView
                    // decoration
                }
                seeAllButton = itemView.findViewById(R.id.seeAllButton) as Button
            }
        }
    }
}