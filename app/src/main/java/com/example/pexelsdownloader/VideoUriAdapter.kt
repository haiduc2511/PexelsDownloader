package com.example.pexelsdownloader

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pexelsdownloader.databinding.ItemVideoUriBinding

class VideoUriAdapter(
    private val videoUris: MutableList<Uri>,
) : RecyclerView.Adapter<VideoUriAdapter.VideoUriViewHolder>() {


    inner class VideoUriViewHolder(val binding: ItemVideoUriBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            binding.uri = uri
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoUriViewHolder {
        val binding = ItemVideoUriBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoUriViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoUriViewHolder, position: Int) {
        holder.bind(videoUris[position])
        holder.binding.ibDeleteUri.setOnClickListener {
            videoUris.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = videoUris.size

}