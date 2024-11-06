package com.example.pexelsdownloader

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pexelsdownloader.databinding.ItemPhotoBinding

class PexelsAdapter(
    private var videoLinks: List<String> = emptyList()
) : RecyclerView.Adapter<PexelsAdapter.PexelsViewHolder>() {

    fun setVideoLinks(newVideoLinks: List<String>) {
        videoLinks = newVideoLinks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PexelsViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: PexelsViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    inner class PexelsViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}