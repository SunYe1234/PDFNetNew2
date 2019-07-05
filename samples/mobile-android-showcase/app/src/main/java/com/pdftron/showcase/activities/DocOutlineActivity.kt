package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.Action
import com.pdftron.pdf.Bookmark
import com.pdftron.pdf.Destination
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class DocOutlineActivity : FeatureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addOutlineToFile()
    }

    private fun addOutlineToFile() {
        try {
            val file = Utils.copyResourceToLocal(this, R.raw.sample, "sample", ".pdf")
            var doc = PDFDoc(file.absolutePath)
            doc.initSecurityHandler()

            // Lets first create the root bookmark items.
            val red = Bookmark.create(doc, "SubOutlines")
            val green = Bookmark.create(doc, "SingleOutline - Page 2")

            doc.addRootBookmark(red)
            doc.addRootBookmark(green)

            // We can now associate new bookmarks with page destinations:

            // The following example creates an 'explicit' destination (see
            // section '8.2.1 Destinations' in PDF Reference for more details)
            val red_dest = Destination.createFit(doc.pageIterator.next())
            red.action = Action.createGoto(red_dest)

            // Create an explicit destination to the first green page in the document
            green.action = Action.createGoto(
                    Destination.createFit(doc.getPage(2)))

            // We can now add children Bookmarks
            val sub_red1 = red.addChild("SubOutlines - Page 1")
            sub_red1.action = Action.createGoto(Destination.createFit(doc.getPage(1)))
            val sub_red2 = red.addChild("SubOutlines - Page 2")
            sub_red2.action = Action.createGoto(Destination.createFit(doc.getPage(2)))
            val sub_red3 = red.addChild("SubOutlines - Page 3")
            sub_red3.action = Action.createGoto(Destination.createFit(doc.getPage(3)))
            val sub_red4 = sub_red3.addChild("SubOutlines - SubOutlines - Page 2")
            sub_red4.action = Action.createGoto(Destination.createFit(doc.getPage(2)))
            val sub_red5 = sub_red3.addChild("SubOutlines - SubOutlines - Page 1")
            sub_red5.action = Action.createGoto(Destination.createFit(doc.getPage(1)))
            val sub_red6 = sub_red3.addChild("SubOutlines - SubOutlines - Page 3")
            sub_red6.action = Action.createGoto(Destination.createFit(doc.getPage(3)))

            // Adding color to Bookmarks. Color and other formatting can help readers
            // get around more easily in large PDF documents.
            red.setColor(1.0, 0.0, 0.0)
            green.setColor(0.0, 1.0, 0.0)
            green.flags = 2            // set bold font

            doc.save()
            getPDFViewCtrl()!!.doc = doc
        } catch (e: Exception) {
            // failed
        }
    }

    private fun addControl() {
        button.text = getString(R.string.open_outline_list)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_white_24dp, 0, 0, 0)
        button.setOnClickListener {
            mPdfViewCtrlTabHostFragment!!.onOutlineOptionSelected(1)
        }
    }
}