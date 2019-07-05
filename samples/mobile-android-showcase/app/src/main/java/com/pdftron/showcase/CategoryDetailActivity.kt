package com.pdftron.showcase

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.widget.Toast
import com.pdftron.showcase.models.Feature
import com.pdftron.showcase.models.FeatureCategory
import java.io.Serializable

class CategoryDetailActivity : AppCompatActivity() {

    private lateinit var category: FeatureCategory

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mViewAdapter: RecyclerView.Adapter<*>
    private lateinit var mViewManager: RecyclerView.LayoutManager

    private lateinit var onItemClick: (Feature) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)

        getIncomingIntent()

        onItemClick = { feature ->
            startNewFeatureActvity(feature)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = category.name

        mViewManager = LinearLayoutManager(this)
        mViewAdapter = CategoryDetailAdapter(this, category.features, onItemClick)

        mRecyclerView = findViewById<RecyclerView>(R.id.category_detail_recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = mViewManager

            // specify an viewAdapter (see also next example)
            adapter = mViewAdapter

        }

    }

    private fun startNewFeatureActvity(feature: Feature) {
        var intent = Intent()
        try {
            val packageName = packageName
            intent.setClassName(packageName, packageName + ".activities." + feature.id + "Activity")
            intent.putExtra("feature", feature as Serializable)
            startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            Toast.makeText(this, "Class Not Exist for this feature. Or there's something wrong when opening this activity", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun getIncomingIntent() {
        if (intent.hasExtra("category")) {

            this.category = intent.extras.get("category") as FeatureCategory
        }
    }


    companion object {

        private val TAG = "CategoryDetailActivity"
    }

}
