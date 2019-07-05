package com.pdftron.showcase.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.RadioButton
import com.pdftron.showcase.R

class RadioButtonFixSize : RadioButton {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setCompoundDrawablesWithIntrinsicBounds(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)

        val size : Int = this.context.resources.getDimensionPixelSize(R.dimen.radio_button_icon_size)

        left?.setBounds(0, 0, size, size)
        top?.setBounds(0, 0, size, size)
        right?.setBounds(0, 0, size, size)
        bottom?.setBounds(0, 0, size, size)
        setCompoundDrawables(left, top, right, bottom)
    }
}