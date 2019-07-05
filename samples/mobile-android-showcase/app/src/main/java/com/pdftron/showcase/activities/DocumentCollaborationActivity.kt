package com.pdftron.showcase.activities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.pdftron.collab.ui.viewer.CollabViewerBuilder
import com.pdftron.collab.ui.viewer.CollabViewerTabHostFragment
import com.pdftron.collab.viewmodel.DocumentViewModel
import com.pdftron.collab.webviewerserver.BlackBoxConnection
import com.pdftron.pdf.Annot
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment
import com.pdftron.pdf.dialog.simpleinput.TextInputDialog
import com.pdftron.pdf.dialog.simpleinput.TextInputResult
import com.pdftron.pdf.dialog.simpleinput.TextInputViewModel
import com.pdftron.pdf.tools.QuickMenu
import com.pdftron.pdf.tools.QuickMenuItem
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.CommonToast
import com.pdftron.pdf.utils.Event
import com.pdftron.pdf.utils.Logger
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.BuildConfig
import com.pdftron.showcase.R
import com.pdftron.showcase.helpers.Helpers
import com.pdftron.showcase.helpers.Helpers.WVS_LINK_KEY
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*
import kotlinx.android.synthetic.main.control_button_simple.*
import java.io.Serializable

class DocumentCollaborationActivity : FeatureActivity() {

    private val TAG = "DocumentCollab"

    private val DEFAULT_SHARE_ID = "zxoF-tJL0VXE"
    private val DEFAULT_FILE_URL = "https://pdftron.s3.amazonaws.com/downloads/pl/Report_2011.pdf"

    private val WVS_ROOT = "https://demo.pdftron.com/"

    private lateinit var mBlackBoxConnection: BlackBoxConnection

    private val INPUT_REQUEST_CODE = 1001

    private lateinit var mUsername: TextView

    private var mShareId : String? = DEFAULT_SHARE_ID
    private var mFileUrl = DEFAULT_FILE_URL

    private fun getDocumentIntent(packageContext: Context, wvsLink: String?): Intent {
        val intent = Intent(packageContext, DocumentCollaborationActivity::class.java)
        intent.putExtra("feature", feature as Serializable)
        if (null != wvsLink) {
            intent.putExtra(WVS_LINK_KEY, wvsLink)
        }
        return intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = "Report_2011"
        fullScreenMode = false
        annotationLayerEnabled = false // TODO, check with core if possible to turn this on

        if (intent != null) {
            val wvs = intent.extras?.getString(WVS_LINK_KEY)
            if (wvs != null) {
                val uri = Uri.parse(wvs)
                val shareId = uri.getQueryParameter(Helpers.SHARE_ID)
                if (shareId != null) {
                    mShareId = shareId
                }
                val cId = uri.getQueryParameter("cId")
                if (cId != null) {
                    mFileUrl = cId
                }
            }
        }

        super.onCreate(savedInstanceState)

        mBlackBoxConnection = BlackBoxConnection(applicationContext)

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)

        Logger.INSTANCE.setDebug(BuildConfig.DEBUG)

        cardState = View.INVISIBLE
        closeBottomSheet()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addControl()
    }

    override fun onDestroy() {
        super.onDestroy()

        mBlackBoxConnection.stop()
    }

    override fun handleTabDocumentLoaded(tag: String) {
        super.handleTabDocumentLoaded(tag)

        val documentViewModel = ViewModelProviders.of(this).get(DocumentViewModel::class.java)

        documentViewModel.setCustomConnection(mBlackBoxConnection)
        mBlackBoxConnection.start(WVS_ROOT, mFileUrl, mShareId)

        documentViewModel.user.observe(this, Observer {
            if (it != null) {
                mUsername.text = String.format(getString(R.string.collab_user_name), it.name)
            }
        })
        documentViewModel.document.observe(this, Observer {
            if (it != null) {
                mShareId = it.shareId
            }
        })
    }

    private fun addControl() {
        val changeLinkButton = layoutInflater.inflate(R.layout.content_bottomsheet_button, button_container, false) as Button

        changeLinkButton.text = getString(R.string.collab_import_link)
        changeLinkButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_content_paste_black_24dp, 0, 0, 0)
        changeLinkButton.setOnClickListener {
            showTextInput()
        }

        button.text = getString(R.string.collab_export_link)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy_black_24dp, 0, 0, 0)
        button.setOnClickListener {
            copyLink()
        }

//        button_container.addView(changeLinkButton)

        mUsername = TextView(this, null, 0, R.style.RobotoTextViewStyle).apply {
            val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply {
                        setMargins(0, resources.getDimension(R.dimen.content_text_top_margin).toInt(), 0, 0)
                    }
            layoutParams = lp
        }
        control_button_simple.addView(mUsername)

        getPdfViewCtrlTabFragment()!!.addQuickMenuListener(object : ToolManager.QuickMenuListener {

            override fun onQuickMenuClicked(menuItem: QuickMenuItem?): Boolean {
                if (menuItem?.itemId == R.id.qm_note) {
                    closeBottomSheet()
                    return false
                }
                return false
            }

            override fun onShowQuickMenu(quickmenu: QuickMenu?, annot: Annot?): Boolean {
                return false
            }

            override fun onQuickMenuShown() {
            }

            override fun onQuickMenuDismissed() {
            }

        })

        val textInputViewModel = ViewModelProviders.of(this).get(TextInputViewModel::class.java)
        textInputViewModel.observeOnComplete(this, Observer<Event<TextInputResult>> {
            if (it != null && !it.hasBeenHandled()) {
                val result = it.contentIfNotHandled!!
                if (result.requestCode == INPUT_REQUEST_CODE) {
                    importLink(result.result)
                }
            }
        })
    }

    override fun createPdfViewerFragment(fileName: String): PdfViewCtrlTabHostFragment {
        return CollabViewerBuilder
                .withUri(Uri.parse(mFileUrl))
                .usingConfig(getViewerConfig())
                .build(this, CollabViewerTabHostFragment::class.java)
    }

    private fun copyLink() {
        if (Utils.isNullOrEmpty(mShareId)) {
            return
        }
        try {
            val uri = Uri.parse("https://www.pdftron.com/webviewer/demo/document-collaboration")
                    .buildUpon()
                    .appendQueryParameter(Helpers.SHARE_ID, mShareId)
                    .appendQueryParameter("cId", mFileUrl)
                    .build().toString()
            Log.d(TAG, "copyLink: $uri")
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("text", uri)
            clipboard.primaryClip = clip
            CommonToast.showText(this, resources.getString(R.string.tools_copy_confirmation), Toast.LENGTH_SHORT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showTextInput() {
        val dialog = TextInputDialog.newInstance(
                INPUT_REQUEST_CODE,
                R.string.collab_import_link,
                R.string.collab_import_link_hint,
                R.string.ok,
                R.string.cancel)
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomAppTheme)
        dialog.show(supportFragmentManager, TextInputDialog.TAG)
    }

    private fun importLink(wvsLink: String) {
        setResult(Activity.RESULT_OK, getDocumentIntent(applicationContext, wvsLink))
        finish()
    }
}
