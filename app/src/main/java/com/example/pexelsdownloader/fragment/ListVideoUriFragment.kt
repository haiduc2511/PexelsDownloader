package com.example.pexelsdownloader.fragment

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pexelsdownloader.R
import com.example.pexelsdownloader.VideoUriAdapter
import com.example.pexelsdownloader.databinding.FragmentListVideoUriBinding
import com.example.pexelsdownloader.databinding.FragmentVideoPlayerBinding

class ListVideoUriFragment(private var videoUriList: MutableList<Uri>) : Fragment() {
    private lateinit var binding: FragmentListVideoUriBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListVideoUriBinding.inflate(layoutInflater)
        binding.flOutside.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        binding.rvListUri.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListUri.adapter = VideoUriAdapter(videoUriList)

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(listVideoUri : MutableList<Uri>) =
            ListVideoUriFragment(listVideoUri).apply {
                arguments = Bundle().apply {
                }
            }
    }
}