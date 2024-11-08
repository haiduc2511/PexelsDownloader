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
import com.example.pexelsdownloader.adapter.PexelsImageAdapter
import com.example.pexelsdownloader.model.PexelsEntity
import com.example.pexelsdownloader.repository.PexelsRepository
import com.example.pexelsdownloader.adapter.PexelsVideoAdapter
import com.example.pexelsdownloader.databinding.FragmentImageDownloadBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageDownloadFragment : Fragment() {
    lateinit var binding: FragmentImageDownloadBinding
    private lateinit var adapter: PexelsImageAdapter
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
        binding = FragmentImageDownloadBinding.inflate(layoutInflater)
        context = requireContext()
        initRecyclerView()
        initFabDownloadAll()
        return binding.root
    }
    fun initFabDownloadAll() {
        binding.fabDownloadAll.setOnClickListener({
            adapter.downloadAll()
        })
    }

    fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PexelsImageAdapter(context)
        binding.recyclerView.adapter = adapter

        // Initialize Repository
        pexelsRepository = PexelsRepository(context)

        // Call API to get data
        getSearchPhotos(1)

    }

    private fun getSearchPhotos(page: Int) {
        pexelsRepository.getImageSearch("home", page)?.enqueue(object : Callback<PexelsEntity?> {
            override fun onResponse(call: Call<PexelsEntity?>, response: Response<PexelsEntity?>) {
                if (response.isSuccessful && response.body() != null) {
                    val photos = response.body()?.photos
                    if (photos != null) {
                        val photoLinks = mutableListOf<ObservableField<String>>()
                        for (photo in photos) {
                            val src = photo.src
                            val srcLink :String = src?.large2x ?: ""
                            photoLinks.add(ObservableField(srcLink))
                            Log.d("Link url photo", srcLink)

                        }
                        Toast.makeText(context, "${photos.size}", Toast.LENGTH_SHORT).show()
                        adapter.setImageLinks(photoLinks) // Update adapter with video links
                    } else {
                        Toast.makeText(context, "Cannot load photos (fetched but empty)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load photos", Toast.LENGTH_SHORT).show()
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
            ImageDownloadFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}