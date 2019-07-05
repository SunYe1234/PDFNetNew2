package com.pdftron.showcase

import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.gson.Gson
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.showcase.helpers.Helpers
import com.pdftron.showcase.helpers.SafeClickListener
import com.pdftron.showcase.models.Feature
import com.pdftron.showcase.models.FeatureCategory
import com.pdftron.showcase.models.FeaturedApps
import kotlinx.android.synthetic.main.content_no_search_results.*
import java.io.IOException
import java.io.Serializable
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mViewAdapter: CategoryAdapter
    private lateinit var mViewManager: RecyclerView.LayoutManager

    lateinit var mSearchText: EditText
    lateinit var mSearchCancelButton: Button
    private var hasSearchText: Boolean = false

    private lateinit var featuredApps: FeaturedApps
    private lateinit var featureCategories: ArrayList<FeatureCategory>
    private lateinit var filteredFeatures: ArrayList<Feature>
    private lateinit var onItemClick: (Feature) -> Unit
    private lateinit var searchText: String
    private lateinit var menu: Menu
    private lateinit var manager: SearchManager
    private lateinit var search: SearchView

    private val FEATURE_REQUEST = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // default day mode
        PdfViewCtrlSettingsManager.setColorMode(this, PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_NORMAL)
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        supportActionBar?.show()
        title = "Explore PDFTron SDK"
        setContentView(R.layout.activity_main)

        var fileText: String = ""
        try {
            fileText = application.applicationContext.assets.open("features_category.json").bufferedReader().use {
                it.readText()
            }
        } catch (e: IOException) {
            Log.e(TAG, "failed in opening features_category")
        }
        this.featuredApps = Gson().fromJson(fileText, FeaturedApps::class.java)
        this.featureCategories = featuredApps?.categories

        filteredFeatures = featureCategories[featureCategories.count() - 1].features

        onItemClick = { feature ->
            startNewFeatureActvity(feature, null)
        }

        mViewManager = LinearLayoutManager(this).apply {
            isMeasurementCacheEnabled = false
        }
        mViewAdapter = CategoryAdapter(this, featureCategories, filteredFeatures, hasSearchText, onItemClick)
        mRecyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = mViewManager
            setHasFixedSize(true)
            adapter = mViewAdapter
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            var activityId = appLinkData.lastPathSegment
            var shareId : String? = null
            if (appLinkData.toString().contains(Helpers.SHARE_ID)) {
                // deep linking with share id will look like
                // DocumentCollaboration/shareId/<id>
                val segments = appLinkData.pathSegments
                activityId = segments[segments.size-3]
                shareId = segments[segments.size-1]
            }
            Uri.parse("app://open.pdftron.showcase/home")
                    .buildUpon()
                    .appendPath(activityId)
                    .build().also { appData ->
                        if (null == shareId) {
                            startNewFeatureActvityById(activityId!!, null)
                        } else {
                            val args = Bundle()
                            var shareIdLink = appLinkData.toString()
                            shareIdLink = shareIdLink.replace("/" + Helpers.SHARE_ID + "/", "?" + Helpers.SHARE_ID + "=")
                            args.putString(Helpers.WVS_LINK_KEY, shareIdLink)
                            startNewFeatureActvityById(activityId!!, args)
                        }
                    }
        }
    }

    private fun startNewFeatureActvityById(activityId: String, args: Bundle?) {
        var feature = findFeatureById(activityId)
        if (feature != null) startNewFeatureActvity(feature, args)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        this.menu = menu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

            search = menu.findItem(R.id.menu_search).actionView as SearchView
            search.maxWidth = Integer.MAX_VALUE

            search.setSearchableInfo(manager.getSearchableInfo(componentName))

            var menuItem = menu.findItem(R.id.menu_search) as MenuItem
            menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    searchText = ""
                    hasSearchText = false
                    updateAdapter()
                    no_results_page.visibility = View.INVISIBLE
                    return true
                }
            })


            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(query: String): Boolean {
                    hasSearchText = true
                    if (query == "") {
                        showCategoryList()
                    } else {
                        search.suggestionsAdapter.changeCursor(null)
                        searchText = query.trim()
                        updateSearch()
                        updateAdapter()
                    }
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    // task HERE
                    hasSearchText = true
                    search.clearFocus()
                    return true
                }
            })

        }
        return true
    }

    private fun showCategoryList() {

        val c = MatrixCursor(arrayOf("_id", "name"))
        for (i in 0..featureCategories.size - 1) {
            c.addRow(arrayOf(i.toString(), featureCategories[i].name!!))
        }

        search.suggestionsAdapter = CategoryListCursorAdapter(this, c, this, search)
    }

    private fun match(result: String, feature: Feature): Boolean {
        if ((feature.name?.toLowerCase()!!.contains(searchText.toLowerCase())) || (feature.category?.toLowerCase()!!.contains(searchText.toLowerCase()))) {
            return true
        }
        for (tag in feature.tags) {
            if (tag.toLowerCase().contains(result) || result.toLowerCase().contains(tag)) {
                return true
            }
        }
        return false
    }

    private fun updateSearch() {
        filteredFeatures = ArrayList<Feature>()
        for (category in featureCategories) {
            for (feature in category.features) if (match(searchText, feature) && filteredFeatures.contains(feature) == false) {
                filteredFeatures.add(feature)
            }
        }

        var newRelatedFeatures = ArrayList<Feature>()
        for (feature in filteredFeatures) {
            for (relatedFeature in feature.relatedFeatures) {
                var f = findFeatureById(relatedFeature)
                if (f != null && !filteredFeatures.contains(f)) {
                    newRelatedFeatures.add(f)
                }
            }
        }
        filteredFeatures.addAll(newRelatedFeatures)
    }


    private fun findFeatureById(id: String): Feature? {
        for (featureCategory in featureCategories) {
            for (feature in featureCategory.features) {
                if (id == feature.id) {
                    return feature
                }
            }
        }
        return null
    }

    private fun updateAdapter() {
        mRecyclerView!!.adapter = CategoryAdapter(this, featureCategories, filteredFeatures, hasSearchText, onItemClick)
        if (filteredFeatures.count() > 0) {
            no_results_page.visibility = View.INVISIBLE
        } else {
            no_results_page.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FEATURE_REQUEST && resultCode == Activity.RESULT_OK) {
            val feature = data?.extras?.get("feature")
            if (feature != null) {
                startNewFeatureActvity(feature as Feature, data, null)
            }
        }
    }

    private fun startNewFeatureActvity(feature: Feature, bundle: Bundle?) {
        startNewFeatureActvity(feature, null, bundle)
    }

    private fun startNewFeatureActvity(feature: Feature, data: Intent?, bundle: Bundle?) {
        var intent = Intent()
        try {
            intent.setClassName(packageName, packageName + ".activities." + feature.id + "Activity")
            intent.putExtra("feature", feature as Serializable)
            if (data != null) {
                intent.putExtras(data)
            }
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            startActivityForResult(intent, FEATURE_REQUEST)
        } catch (error: ActivityNotFoundException) {
            Toast.makeText(this, "Class Not Exist for this feature. Or there's something wrong when opening this activity", LENGTH_SHORT).show()
        }
    }


    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun updateSearchCategory(id: Long) {
        hasSearchText = true
        searchText = featureCategories[id.toInt()].name!!
        search.setQuery(searchText, false)
        filteredFeatures = featureCategories[id.toInt()].features
        updateAdapter()
    }

}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {

    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}