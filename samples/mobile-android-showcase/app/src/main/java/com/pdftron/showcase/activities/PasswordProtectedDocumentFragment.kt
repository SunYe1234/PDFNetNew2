package com.pdftron.showcase.activities

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.pdftron.showcase.R

// Declares PasswordProtectedDocumentFragment as a subclass of Fragment
class PasswordProtectedDocumentFragment : Fragment() {

    private var fragmentListener: FragmentListener? = null

    private lateinit var passwordField: EditText
    private lateinit var error: TextView
    private var passwordInput: String = ""
    private val TAG = "PasswordDocFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.content_password_input, container, false)
        passwordField = view.findViewById(R.id.password_input_field)
        error = view.findViewById(R.id.error_text)

        passwordField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                passwordInput = passwordField.getText().toString().trim()
            }
        })

        passwordField.setOnEditorActionListener() { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                openFileWithPassword()
                true
            } else {
                false
            }
        }

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.fragmentListener = context as? FragmentListener
    }

    private fun openFileWithPassword() {
        fragmentListener!!.openFileWithPassword(passwordInput)
    }

    private fun finish() {
        fragmentListener!!.finish()
    }


}


