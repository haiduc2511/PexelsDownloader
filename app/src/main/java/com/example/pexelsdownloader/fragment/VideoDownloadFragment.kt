package com.example.pexelsdownloader.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pexelsdownloader.model.PexelsEntity
import com.example.pexelsdownloader.repository.PexelsRepository
import com.example.pexelsdownloader.adapter.PexelsVideoAdapter
import com.example.pexelsdownloader.databinding.FragmentVideoDownloadBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoDownloadFragment : Fragment() {
    lateinit var binding: FragmentVideoDownloadBinding
    private lateinit var adapter: PexelsVideoAdapter
    private lateinit var pexelsRepository: PexelsRepository
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVideoDownloadBinding.inflate(layoutInflater)
        context = requireContext()
        initRecyclerView()
        return binding.root
    }
    public fun downloadAll() {
        adapter.downloadAll()
    }
    fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PexelsVideoAdapter(context)
        binding.recyclerView.adapter = adapter

        // Initialize Repository
        pexelsRepository = PexelsRepository(context)

        // Call API to get data
        getSearchVideos(1)

    }

    private fun getSearchVideos(page: Int) {
        pexelsRepository.getVideoSearch("home", page)?.enqueue(object : Callback<PexelsEntity?> {
            override fun onResponse(call: Call<PexelsEntity?>, response: Response<PexelsEntity?>) {
                if (response.isSuccessful && response.body() != null) {
                    val videos = response.body()?.videos
                    if (videos != null) {
                        val videoLinks = mutableListOf<ObservableField<String>>()
                        for (video in videos) {
                            val videoFiles = video.videoFiles
                            for (videoFile in videoFiles) {
                                val videoFileLink = videoFile.link ?: ""
                                if (videoFile.link!!.contains("720")) {
                                    videoLinks.add(ObservableField(videoFileLink))
                                    Log.d("Link url", videoFileLink)
                                    break
                                }
                            }
                        }
                        Toast.makeText(context, "${videos.size}", Toast.LENGTH_SHORT).show()
                        adapter.setVideoLinks(videoLinks) // Update adapter with video links
                    } else {
                        Toast.makeText(context, "Cannot load videos (fetched but empty)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load videos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PexelsEntity?>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VideoDownloadFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}