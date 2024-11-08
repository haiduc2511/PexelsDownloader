package com.example.pexelsdownloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTablayoutViewPager()
        // Set up RecyclerView
    }
    fun initTablayoutViewPager() {
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout
        viewPager.adapter = ViewPagerAdapter(this)


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
                0 -> ImageDownloadFragment()  // Tab 1
                1 -> VideoDownloadFragment() // Tab 2
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }


}
