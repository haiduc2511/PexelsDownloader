package com.example.pexelsdownloader

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VideoUriAdapter(
    private val videoUris: MutableList<Uri>,
) : RecyclerView.Adapter<VideoUriAdapter.VideoUriViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoUriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_uri, parent, false)
        return VideoUriViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoUriViewHolder, position: Int) {
        val videoUri = videoUris[position]
        holder.tvVideoUri.text = videoUri.toString()
    }

    override fun getItemCount(): Int = videoUris.size

    inner class VideoUriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVideoUri: TextView = itemView.findViewById(R.id.tv_video_uri)
    }

}