package com.pdftron.showcase.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.utils.Utils
import com.pdftron.sdf.SecurityHandler
import com.pdftron.showcase.CustomBottomSheetBehavior
import com.pdftron.showcase.R
import com.pdftron.showcase.helpers.Helpers.fromHtml
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

interface FragmentListener {
    fun openFileWithPassword(password: String)
    fun finish()
}

class PasswordProtectedDocumentActivity : FeatureActivity(), FragmentListener {

    private val TAG = "PasswordDocActivity"

    private lateinit var doc: PDFDoc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addPasswordToFile()
        setupPasswordInputView()
        addControl()
    }

    private fun addControl() {
        val string = feature_description.text.toString()
        feature_description.text = fromHtml(string)
        button.text = getString(R.string.lock_document)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0)
        button.setOnClickListener {
            addPasswordToFile()
            setupPasswordInputView()
        }
    }

    private fun addPasswordToFile() {
        try {
            val file = Utils.copyResourceToLocal(this, R.raw.sample, "sample_security", ".pdf")
            doc = PDFDoc(file.absolutePath)
            val new_handler = SecurityHandler()

            // Set a new password required to open a document
            val user_password = "123"
            new_handler.changeUserPassword(user_password)

            // Set Permissions
            new_handler.setPermission(SecurityHandler.e_print, true)
            new_handler.setPermission(SecurityHandler.e_extract_content, false)

            // Note: document takes the ownership of new_handler.
            doc.securityHandler = new_handler

            // Save the changes.
            doc.save()
            button.isEnabled = false
            button.alpha = 0.5f

        } catch (e: PDFNetException) {
            e.printStackTrace()
        }

    }

    private fun setupPasswordInputView() {
        var encryptFragment = PasswordProtectedDocumentFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, encryptFragment, null)
        ft.commit()

        // add listener into password input field

    }

    override fun openFileWithPassword(passwordInput: String) {
        val error = findViewById<TextView>(R.id.error_text)
        if (doc.initStdSecurityHandler(passwordInput)) {
            error.visibility = View.GONE
            doc.removeSecurity()
            doc.save()
            doc.close()
            findViewById<View>(R.id.password_page_view).hideKeyboard()

            setUpSampleView("sample")
            CustomBottomSheetBehavior.from(bottom_sheet).setState(CustomBottomSheetBehavior.STATE_COLLAPSED)
            button.isEnabled = true
            button.alpha = 1f
        } else {
            error.visibility = View.VISIBLE
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}