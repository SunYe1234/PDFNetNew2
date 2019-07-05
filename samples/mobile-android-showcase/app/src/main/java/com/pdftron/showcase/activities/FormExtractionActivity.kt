package com.pdftron.showcase.activities

import android.os.Bundle
import android.view.View
import com.pdftron.pdf.Annot
import com.pdftron.pdf.Field
import com.pdftron.pdf.annots.Widget
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.Utils
import kotlinx.android.synthetic.main.control_extraction.*

class FormExtractionActivity : TextExtractionActivity(), ToolManager.AnnotationModificationListener {

    override val sFileName: String
        get() = "form_1040"

    override fun addControl() {
        getToolManager()!!.addAnnotationModificationListener(this)

        inner_scroll_view.apply {
            val lp = layoutParams
            lp.height = Utils.convDp2Pix(context, 90f).toInt()
            layoutParams = lp
        }
        inner_scroll_view.visibility = View.GONE
    }

    override fun onAnnotationsAdded(annots: MutableMap<Annot, Int>?) {

    }

    override fun onAnnotationsPreModify(annots: MutableMap<Annot, Int>?) {

    }

    override fun onAnnotationsModified(annots: MutableMap<Annot, Int>?, extra: Bundle?) {
        annots!!.forEach {
            val annot = it.key
            if (annot.isValid) {
                if (annot.type == Annot.e_Widget) {
                    val widget = Widget(annot)
                    val field = widget.field
                    val name = field.name
                    var fieldValueStr = ""
                    val fieldType = field.type
                    if (fieldType == Field.e_check) {
                        fieldValueStr = field.valueAsBool.toString()
                    } else if (fieldType == Field.e_text) {
                        fieldValueStr = field.valueAsString
                    }
                    inner_scroll_view.visibility = View.VISIBLE
                    extraction_text.text = "$name : $fieldValueStr"
                }
            }
        }
    }

    override fun onAnnotationsPreRemove(annots: MutableMap<Annot, Int>?) {

    }

    override fun onAnnotationsRemoved(annots: MutableMap<Annot, Int>?) {

    }

    override fun onAnnotationsRemovedOnPage(pageNum: Int) {

    }

    override fun annotationsCouldNotBeAdded(errorMessage: String?) {

    }
}