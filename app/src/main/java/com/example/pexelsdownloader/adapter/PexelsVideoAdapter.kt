package com.example.pexelsdownloader.adapter

import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableLong
import androidx.recyclerview.widget.RecyclerView
import com.example.pexelsdownloader.utils.DownloadObserver
import com.example.pexelsdownloader.utils.DownloadProgressListener
import com.example.pexelsdownloader.databinding.ItemPhotoBinding
import com.example.pexelsdownloader.databinding.ItemVideoBinding
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PexelsVideoAdapter(
    private var context: Context,
) : RecyclerView.Adapter<PexelsVideoAdapter.PexelsViewHolder>() {

    private var videoLinks: MutableList<ObservableField<String>> = mutableListOf()
    private val videoProgressMap: HashMap<ObservableField<String>, ObservableLong> = HashMap()

    fun setVideoLinks(newVideoLinks: MutableList<ObservableField<String>>) {
        videoLinks = newVideoLinks
        for (videoLink in videoLinks) {
            videoProgressMap.put(videoLink, ObservableLong(0L))
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PexelsViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemVideoBinding.inflate(layoutInflater, parent, false)
        return PexelsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PexelsViewHolder, position: Int) {
        var videoLink = videoLinks[position]
        holder.binding.button.setOnClickListener {
            if (!videoLink.get()!!.contains("https://videos.pexels.com/") ?: true) {
                Toast.makeText(context, "You've already downloaded this", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "dang download $videoLink", Toast.LENGTH_LONG).show()
                val fileName = System.currentTimeMillis().toString()
                val outputFilePath = "/storage/emulated/0/Download/$fileName.mp4"

                downloadFileToGalleryByHttpsUrlConnection(position, outputFilePath) { progress ->
                    if (videoProgressMap[videoLink]!!.get() != progress) {
                        videoProgressMap[videoLink]!!.set(progress)
                    }

                    Log.d("Download in adapter", "${videoLink.get()} đã tải được $progress %")
                    if (progress == 100L) {
                        videoLinks[position].set("/storage/emulated/0/Download/$fileName.mp4")
                        Log.d("Download in adapter", "${videoLink.get()} đã hoàn thành")
                    }
                }
//                downloadFileToGallery(position) ?: ObservableLong(0L)
            }
        }
        holder.binding.link = videoLink
        holder.binding.progress = videoProgressMap[videoLink]
    }
    fun animateProgress(progressIndicator: LinearProgressIndicator, progress: Int) {
        val animator = ObjectAnimator.ofInt(progressIndicator, "progress", progressIndicator.progress, progress)
        animator.duration = 500 // Adjust the duration for smoothness
        animator.start()
    }

    override fun getItemCount(): Int {
        return videoLinks.size
    }

    inner class PexelsViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    fun downloadAll() {
        Toast.makeText(context, "Bắt đầu download all", Toast.LENGTH_LONG).show()
        for (i in 0..videoLinks.size - 1) {
            if (videoLinks[i].get()!!.contains("https")) {
                val position = i
                var videoLink = videoLinks[position]
                val fileName = System.currentTimeMillis().toString()
                val outputFilePath = "/storage/emulated/0/Download/$fileName.mp4"

                downloadFileToGalleryByHttpsUrlConnection(position, outputFilePath) {progress ->
                    videoProgressMap[videoLink]?.set(progress)
                    Log.d("Download listener to adapter", "$videoLink đã tải được $progress %")
                    if (progress == 100L) {
                        videoLinks[position].set("$outputFilePath")
                    }
                }

            }
        }
    }

    val maxConcurrentDownloads = 2
    val downloadSemaphore = Semaphore(maxConcurrentDownloads)

    fun downloadFileToGalleryByHttpsUrlConnection(position: Int, outputFilePath: String, progressCallback: (Long) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        downloadSemaphore.acquire()

        try {
            val videoLink = videoLinks[position]
            val url = URL(videoLink.get())
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val totalSize = connection.contentLength
            var downloadedSize = 0


            BufferedInputStream(connection.inputStream).use { inputStream ->
                FileOutputStream(outputFilePath).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        val progress = (downloadedSize * 100L) / totalSize
                        progressCallback(progress)
                    }
                    outputStream.flush()
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Release the permit when done
            progressCallback(0)
            downloadSemaphore.release()
        }
    }
}
    fun downloadFileToGallery(position: Int) {
        val videoLink = videoLinks[position]
        var fileName = System.currentTimeMillis().toString()
        val request = DownloadManager.Request(Uri.parse(videoLink.get()))
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
                Log.d("Download listener to adapter", "$videoLink đã tải được $progress %")
                if (progress == 100L) {
                    val file: File = File(Environment.DIRECTORY_MOVIES, fileName)
                    videoLinks[position].set(file.absolutePath)
                }
            }
        }

        val myDownloads = Uri.parse("content://downloads/my_downloads")
        context.getContentResolver().registerContentObserver(myDownloads, true, DownloadObserver(
            Handler(), context, downloadId, downloadManager, downloadProgressListener
        )
        )

    }


}