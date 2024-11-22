package com.example.pexelsdownloader.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pexelsdownloader.R
import com.example.pexelsdownloader.databinding.FragmentListVideoUriBinding
import com.example.pexelsdownloader.databinding.FragmentVideoPlayerBinding

class ListVideoUriFragment : Fragment() {
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
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ListVideoUriFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}