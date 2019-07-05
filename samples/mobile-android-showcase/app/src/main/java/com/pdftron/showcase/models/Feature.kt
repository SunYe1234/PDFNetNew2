package com.pdftron.showcase.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Feature(
        @SerializedName("id") var id: String? = null,
        @SerializedName("name") var name: String? = null,
        @SerializedName("category") var category: String? = null,
        @SerializedName("imageName") var imageName: String? = null,
        @SerializedName("description") var description: String? = null,
        @SerializedName("relatedFeatures") var relatedFeatures: ArrayList<String> = ArrayList(),
        @SerializedName("cardDescription") var cardDescription: String? = null,
        @SerializedName("tags") var tags: ArrayList<String> = ArrayList(),
        @SerializedName("link") var link: String? = null,
        @SerializedName("codeSnippet") var codeSnippet: ArrayList<String> = ArrayList(),
        @SerializedName("codeSnippets") var codeSnippets: ArrayList<CodeSnippet>?,
        @SerializedName("codeSnippetDescription") var codeSnippetDescription: String? = null
) : Serializable
