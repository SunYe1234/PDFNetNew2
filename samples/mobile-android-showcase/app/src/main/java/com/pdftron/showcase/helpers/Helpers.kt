package com.pdftron.showcase.helpers

import android.content.Context
import android.support.v4.text.HtmlCompat
import android.text.Spanned
import android.util.DisplayMetrics
import android.widget.ImageView
import com.pdftron.showcase.R
import com.squareup.picasso.Picasso


object Helpers {

    val WVS_LINK_KEY = "webviewer_server_link"
    val SHARE_ID = "shareId"

    // Returns the drawable resource id given a drawable file name
    fun getDrawableResId(drawableResourceName: String): Int {
        return try {
            val idField = R.drawable::class.java.getDeclaredField(drawableResourceName)
            idField.getInt(idField)
        } catch (e: NoSuchFieldError) {
            e.printStackTrace()
            -1
        }
    }

    fun fromHtml(html: String): Spanned {
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}

fun ImageView.setPicassoDrawable(drawableString: String) {
    Picasso.get()
            .load(Helpers.getDrawableResId(drawableString))
            .into(this)
}
