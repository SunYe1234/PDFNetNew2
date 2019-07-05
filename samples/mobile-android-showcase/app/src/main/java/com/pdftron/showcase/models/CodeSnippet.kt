package com.pdftron.showcase.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CodeSnippet(
        @SerializedName("id") var id: String?,
        @SerializedName("code") var code: ArrayList<String>
) : Serializable