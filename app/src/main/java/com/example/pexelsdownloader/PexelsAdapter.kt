package com.example.pexelsdownloader

import android.view.LayoutInflater
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
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPhotoBinding.inflate(layoutInflater, parent, false)
        return PexelsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PexelsViewHolder, position: Int) {
        holder.binding.tvDetails.text = videoLinks[position]
    }

    override fun getItemCount(): Int {
        return videoLinks.size
    }

    inner class PexelsViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}