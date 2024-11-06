package com.example.pexelsdownloader

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pexelsdownloader.databinding.ItemPhotoBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PexelsAdapter(
    private var context: Context
) : RecyclerView.Adapter<PexelsAdapter.PexelsViewHolder>() {

    private var videoLinks: List<String> = emptyList()

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
        var videoLink = videoLinks[position]
        holder.binding.tvDetails.text = videoLink
        holder.binding.button.setOnClickListener({
            Toast.makeText(context, "dang download $videoLink", Toast.LENGTH_LONG).show()
            downloadFileWithOkHttp3(videoLink)
        })
    }

    override fun getItemCount(): Int {
        return videoLinks.size
    }

    inner class PexelsViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    fun downloadFileWithOkHttp3(url: String) {
        val destinationPath = "${context.getExternalFilesDir(null)}/downloaded_file.zip"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            response.body()?.let { responseBody ->
                responseBody.byteStream().use { inputStream ->
                    val file = File(destinationPath)
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } ?: throw IOException("Response body is null")
        }
    }


}