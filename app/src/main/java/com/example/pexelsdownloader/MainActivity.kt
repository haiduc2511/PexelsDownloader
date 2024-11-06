package com.example.pexelsdownloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pexelsdownloader.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PexelsAdapter
    private lateinit var pexelsRepository: PexelsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PexelsAdapter(this)
        binding.recyclerView.adapter = adapter

        // Initialize Repository
        pexelsRepository = PexelsRepository(this)

        // Call API to get data
        getSearchVideos(25)
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

    private fun getSearchVideos(page: Int) {
        pexelsRepository.getVideoSearch("home", page)?.enqueue(object : Callback<PexelsEntity?> {
            override fun onResponse(call: Call<PexelsEntity?>, response: Response<PexelsEntity?>) {
                if (response.isSuccessful && response.body() != null) {
                    val videos = response.body()?.videos
                    if (videos != null) {
                        val videoLinks = mutableListOf<String>()
                        for (video in videos) {
                            val videoFiles = video.videoFiles
                            for (videoFile in videoFiles) {
                                val videoFileLink = videoFile.link ?: ""
                                if (videoFile.link!!.contains("360")) {
                                    videoLinks.add(videoFileLink)
                                    Log.d("Link url", videoFileLink)
                                    break
                                }
                            }
                        }
                        Toast.makeText(this@MainActivity, "${videos.size}", Toast.LENGTH_SHORT).show()
                        adapter.setVideoLinks(videoLinks) // Update adapter with video links
                    } else {
                        Toast.makeText(this@MainActivity, "Cannot load videos (fetched but empty)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load videos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PexelsEntity?>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterQuality1280_720(videoFiles: List<VideoFile>): VideoFile {
        return videoFiles.firstOrNull { it.link!!.contains("1280_720") } ?: videoFiles[0]
    }



}
