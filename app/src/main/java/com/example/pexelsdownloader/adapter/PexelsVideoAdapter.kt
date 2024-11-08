package com.example.pexelsdownloader.adapter

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableLong
import androidx.recyclerview.widget.RecyclerView
import com.example.pexelsdownloader.utils.DownloadObserver
import com.example.pexelsdownloader.utils.DownloadProgressListener
import com.example.pexelsdownloader.databinding.ItemPhotoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PexelsVideoAdapter(
    private var context: Context,
) : RecyclerView.Adapter<PexelsVideoAdapter.PexelsViewHolder>() {

    private var videoLinks: List<String> = emptyList()
    private val videoProgressMap: HashMap<String, ObservableLong> = HashMap()

    fun setVideoLinks(newVideoLinks: List<String>) {
        videoLinks = newVideoLinks
        for (videoLink in videoLinks) {
            videoProgressMap.put(videoLink, ObservableLong(0L))
        }
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
//            downloadFileWithOkHttp(videoLink)
            downloadFileToGallery(videoLink, holder) ?: ObservableLong(0L)
        })
        holder.binding.link = videoLink
        holder.binding.progress = videoProgressMap[videoLink]
//        holder.binding.tvProgress.text = videoProgressMap[videoLink]?.toString() ?: "0"
//        holder.binding.pbDownloadProgress.progress = (videoProgressMap[videoLink]?.toString() ?: "0").toInt()
    }

    override fun getItemCount(): Int {
        return videoLinks.size
    }

    inner class PexelsViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    fun downloadFileWithOkHttp(url: String) {
        // Launch the download operation on a background thread
        val destinationPath = "${context.getExternalFilesDir(null)}/downloaded_file.zip"
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            try {
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
            } catch (e: Exception) {
                e.printStackTrace() // Handle the exception appropriately in a production app
            }
        }
    }
    fun downloadFileToGallery(videoLink: String, holder: PexelsViewHolder) {
        var fileName = System.currentTimeMillis().toString()
        val request = DownloadManager.Request(Uri.parse(videoLink))
        request.setTitle("Downloading $fileName")
        request.setDescription("Downloading $fileName...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, fileName)
        request.setMimeType("video/mp4")  // Set MIME type for MP4 video file

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)


        var downloadProgressListener = object : DownloadProgressListener {
            override fun onProgressFetch(progress: Long) {
                videoProgressMap[videoLink]?.set(progress)
//                holder.binding.tvProgress.text = videoProgressMap[videoLink]?.toString() ?: "0"
//
//                holder.binding.pbDownloadProgress.progress = (videoProgressMap[videoLink]?.toString() ?: "0").toInt()
                Toast.makeText(context, "$videoLink đã tải được $progress %", Toast.LENGTH_LONG).show()
                Log.d("Download listener to adapter", "$videoLink đã tải được $progress %")
            }
        }

        val myDownloads = Uri.parse("content://downloads/my_downloads")
        context.getContentResolver().registerContentObserver(myDownloads, true, DownloadObserver(
            Handler(), context, downloadId, downloadManager, downloadProgressListener
        )
        )

    }


}