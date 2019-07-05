package com.pdftron.showcase.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pdftron.showcase.R

// Declares PasswordProtectedDocumentFragment as a subclass of Fragment
class SplitViewFragment : Fragment() {

    private val TAG = "SplitViewFragment"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_split_view, container, false)
    }
}

