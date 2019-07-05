package com.pdftron.showcase.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class FeatureCategory(
        @SerializedName("name") var name: String? = null,
        @SerializedName("features") var features: ArrayList<Feature> = ArrayList<Feature>(),
        @SerializedName("type") var type: String? = null,
        @SerializedName("description") var description: String? = null
) : Serializable
