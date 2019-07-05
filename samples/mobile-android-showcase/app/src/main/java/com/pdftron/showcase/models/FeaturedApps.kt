package com.pdftron.showcase.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class FeaturedApps(
        @SerializedName("categories") var categories: ArrayList<FeatureCategory> = ArrayList<FeatureCategory>()
)
