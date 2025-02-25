//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2018 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples

import java.util.ArrayList

abstract class PDFNetSample {

    /*
     * Title used to identify the sample in the detail fragment.
     */
    var title: String? = null
        protected set

    /*
     * Sample description shown in the detail fragment.
     */
    var description: String? = null
        protected set

    /*
     * Stores the files that were generated by the sample.
     */
    var files: ArrayList<String>
        protected set

    /*
     * Indicates that this sample is enabled so it can be executed.
     */
    protected var isEnabled: Boolean? = null
        private set

    init {
        this.title = "{Sample Title}"
        this.description = "{Sample Description}"
        this.files = ArrayList()
        this.isEnabled = true
    }

    open fun run(outputListener: OutputListener?) {
        this.files.clear()
    }

    protected fun setTitle(resId: Int) {
        this.title = MiscellaneousSamplesApplication.instance?.getString(resId)
    }

    protected fun setDescription(resId: Int) {
        this.description = MiscellaneousSamplesApplication.instance?.getString(resId)
    }

    fun addToFileList(fileName: String) {
        this.files.add(fileName)
    }

    protected fun printHeader(outputListener: OutputListener) {
        val header = MiscellaneousSamplesApplication.instance?.getString(R.string.str_running_sample_header, this.title)
        outputListener.println(header + "\n")
    }

    fun printFooter(outputListener: OutputListener) {
        val footer = MiscellaneousSamplesApplication.instance?.getString(R.string.str_running_sample_footer)
        outputListener.println("\n" + footer)
        outputListener.println("--------------------")
    }

    protected fun EnableRun() {
        this.isEnabled = true
    }

    protected fun DisableRun() {
        this.isEnabled = false
    }

    /**
     * This will be used by ArrayAdapter when creating the list of PDFNetSamples.
     */
    override fun toString(): String {
        return this.title.toString()
    }

    companion object {

        /*
     * Path in assets folder where the input files are stored.
     */
        public val INPUT_PATH = "TestFiles/"
        public val OFFICE_FOLDER_NAME = "DocxFiles"
    }
}
