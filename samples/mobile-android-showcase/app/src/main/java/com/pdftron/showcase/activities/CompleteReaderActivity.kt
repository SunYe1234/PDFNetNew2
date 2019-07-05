package com.pdftron.showcase.activities

import android.os.Bundle
import android.view.View

class CompleteReaderActivity: FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardState = View.INVISIBLE
        closeBottomSheet()
    }

}