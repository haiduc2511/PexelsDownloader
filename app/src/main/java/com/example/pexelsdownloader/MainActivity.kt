package com.example.pexelsdownloader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.pexelsdownloader.databinding.ActivityMainBinding
import com.example.pexelsdownloader.fragment.ImageDownloadFragment
import com.example.pexelsdownloader.fragment.VideoDownloadFragment
import com.example.pexelsdownloader.model.VideoFile
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageDownloadFragment: ImageDownloadFragment
    private lateinit var videoDownloadFragment: VideoDownloadFragment
    private var currentFragmentNumber: Int = 0
    private var isTablet : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        isTablet = resources.configuration.smallestScreenWidthDp >= 600
        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (isTablet) {
            initTablayoutAndBothFragments()
        } else {
            initTablayoutViewPager()
        }
        initFabDownloadAll()
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                openFilePicker()
                // Handle search action
                true
            }
            R.id.action_clear -> {
                // Handle settings action
                clearAdapter()
                true
            }
            R.id.action_video_play -> {
                // Handle settings action
                startActivity(Intent(this, VideoPlayerActivity::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun clearAdapter() {
        imageDownloadFragment.clearAdapter()
    }
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/json"  // Limit to JSON files
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_JSON)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_JSON && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                importJsonFile(uri)
            }
        }
    }
    private fun importJsonFile(uri: Uri) {
        val contentResolver = applicationContext.contentResolver
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonText = inputStream.bufferedReader().use { it.readText() }
                // Parse the JSON text
                processJsonData(jsonText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions, such as showing a Toast message
        }
    }
    private fun processJsonData(jsonText: String) {
        try {
            val jsonObject = JSONObject(jsonText)
            val imagesArray = jsonObject.getJSONArray("images")
            val imageUrls = mutableListOf<String>()

            for (i in 0 until imagesArray.length()) {
                val url = imagesArray.getString(i)
                imageUrls.add(url)
            }
            imageDownloadFragment.setPhotos(imageUrls)
            // Log or use the list of image URLs as needed
            Log.d("ImageURLs", imageUrls.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            // Handle JSON parsing error
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_JSON = 1001
    }

    fun initFabDownloadAll() {
        binding.fabDownloadAll.setOnClickListener {
            if (!isTablet) {
                // Phone mode
                if (currentFragmentNumber == 0) {
                    imageDownloadFragment.downloadAll()
                } else {
                    videoDownloadFragment.downloadAll()
                }
            } else {
                // Tablet mode
                imageDownloadFragment.downloadAll()
                videoDownloadFragment.downloadAll()
            }
        }
    }
    fun initTablayoutAndBothFragments() {
        imageDownloadFragment = supportFragmentManager.findFragmentById(R.id.imageDownloadFragment) as ImageDownloadFragment
        videoDownloadFragment = supportFragmentManager.findFragmentById(R.id.videoDownloadFragment) as VideoDownloadFragment

    }

    fun initTablayoutViewPager() {
        var tabLayout: TabLayout = binding.tabLayout!!
        var viewPager: ViewPager2 = binding.viewPager!!
        viewPager.adapter = ViewPagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentFragmentNumber = position
            }
        })

        // Kết nối TabLayout với ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position -> {}
            tab.text = if (position == 0) "Image" else "Video"
        }.attach()


    }

    private val REQUEST_CODE = 1
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        }
    }

    private fun filterQuality1280_720(videoFiles: List<VideoFile>): VideoFile {
        return videoFiles.firstOrNull { it.link!!.contains("1280_720") } ?: videoFiles[0]
    }

    private inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2 // Số tab (fragment)

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    imageDownloadFragment = ImageDownloadFragment()
                    return imageDownloadFragment
                } // Tab 1
                1 -> {
                    videoDownloadFragment = VideoDownloadFragment()
                    return videoDownloadFragment
                }  // Tab 2
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }


}
