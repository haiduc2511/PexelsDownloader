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
import androidx.databinding.ObservableField
import androidx.databinding.ObservableLong
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pexelsdownloader.R
import com.example.pexelsdownloader.databinding.ItemPhotoBinding
import com.example.pexelsdownloader.utils.DownloadObserver
import com.example.pexelsdownloader.utils.DownloadProgressListener
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

class PexelsImageAdapter(
    private var context: Context,
) : RecyclerView.Adapter<PexelsImageAdapter.PexelsViewHolder>() {

    private var photoLinks: MutableList<ObservableField<String>> = mutableListOf()
    private val photoProgressMap: HashMap<ObservableField<String>, ObservableLong> = HashMap()
    private val imageModelsDownloaded: HashSet<String> = HashSet<String>() //Forgot to add ImageModelsShared


    fun setPhotosLinks(newPhotosLinks: MutableList<ObservableField<String>>) {
        photoProgressMap.clear()
        photoLinks = newPhotosLinks
        for (photoLink in photoLinks) {
            photoProgressMap.put(photoLink, ObservableLong(0L))
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PexelsViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPhotoBinding.inflate(layoutInflater, parent, false)
        return PexelsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PexelsViewHolder, position: Int) {
        var photoLink = photoLinks[position]
        holder.binding.ibDownload.setOnClickListener {
            if (!photoLink.get()!!.contains("https:") ?: true) {
                Toast.makeText(context, "You've already downloaded this", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "dang download ${photoLink.get()}", Toast.LENGTH_LONG)
                    .show()
//                downloadFileToGallery(position) ?: ObservableLong(0L)
                val fileName = System.currentTimeMillis().toString()
                val outputFilePath = "/storage/emulated/0/Download/$fileName.jpeg"

                downloadFileToGalleryByHttpsUrlConnection(position, outputFilePath) { progress ->
                    if (photoProgressMap[photoLink]!!.get() != progress) {
                        photoProgressMap[photoLink]!!.set(progress)
                    }

                    Log.d("Download in adapter", "${photoLink.get()} đã tải được $progress %")
                    if (progress == 100L) {
                        holder.binding.ibDownload.setImageResource(R.drawable.ic_download_done)
                        imageModelsDownloaded.add(photoLink.get().toString())
                        Toast.makeText(
                            context,
                            "Đã tải về storage/emulated/0/Download/$fileName.mp4",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("Download in adapter", "${photoLink.get()} đã hoàn thành")
                    }
                }

            }
        }
        holder.binding.link = photoLink
        holder.binding.progress = photoProgressMap[photoLink]


        Glide.with(context).load(photoLink.get() )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.imageView)

        if (imageModelsDownloaded.contains(photoLink.get().toString())) {
            holder.binding.ibDownload.setImageResource(R.drawable.ic_download_done)
        } else {
            holder.binding.ibDownload.setImageResource(R.drawable.ic_download)
        }
    }

    override fun getItemCount(): Int {
        return photoLinks.size
    }

    inner class PexelsViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    fun downloadAll() {
        Toast.makeText(context, "Bắt đầu download all", Toast.LENGTH_LONG).show()
        for (i in 0..photoLinks.size - 1) {
            if (photoLinks[i].get()!!.contains("https")) {
                val position = i
                var photoLink = photoLinks[position]
                val fileName = System.currentTimeMillis().toString()
                val outputFilePath = "/storage/emulated/0/Download/$fileName.jpeg"

                downloadFileToGalleryByHttpsUrlConnection(position, outputFilePath) { progress ->
                    if (photoProgressMap[photoLink]!!.get() != progress) {
                        photoProgressMap[photoLink]!!.set(progress)
                    }

                    Log.d("Download in adapter", "${photoLink.get()} đã tải được $progress %")
                    if (progress == 100L) {
                        photoLinks[position].set("/storage/emulated/0/Download/$fileName.jpeg")
                        Log.d("Download in adapter", "${photoLink.get()} đã hoàn thành")
                    }
                }
            }
        }
    }
//
//    fun downloadAll(items: MutableList<ObservableField<String>>) {
//        Observable.create<Int> { emitter ->
//            for (i in 0..photoLinks.size - 1) {
//                if (photoLinks[i].get() != null) {
//                    if (photoLinks[i].get()!!.contains("https")) {
//                        emitter.onNext(i)
//                    }
//
//                }
//
//            }
//            emitter.onComplete()
//        }
//            .flatMap(
//                { i -> downloadFileToGallery(i) },
//                4 // Giới hạn số lượng tải xuống đồng thời là 4
//            )
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                { result -> println(result) },
//                { error -> println("Error: ${error.message}") },
//                { println("All downloads completed.") }
//            )
//    }


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

    val maxConcurrentDownloads = 2
    val downloadSemaphore = Semaphore(maxConcurrentDownloads)

    fun downloadFileToGalleryByHttpsUrlConnection(position: Int, outputFilePath: String, progressCallback: (Long) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadSemaphore.acquire()
            try {
                val photoLink = photoLinks[position]
                val url = URL(photoLink.get())
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
                progressCallback(0)
                downloadSemaphore.release()
            }
        }
    }

    fun downloadFileToGallery(position: Int) {
        val photoLink = photoLinks[position]
        var fileName = System.currentTimeMillis().toString()
        val request = DownloadManager.Request(Uri.parse(photoLink.get()))
        request.setTitle("Downloading $fileName")
        request.setDescription("Downloading $fileName...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)
        request.setMimeType("image/jpeg")  // Set MIME type for MP4 video file


        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)


        var downloadProgressListener = object : DownloadProgressListener {
            override fun onProgressFetch(progress: Long) {
                photoProgressMap[photoLink]?.set(progress)
                Log.d("Download listener to adapter", "$photoLink đã tải được $progress %")
                if (progress == 100L) {
                    val file: File = File(Environment.DIRECTORY_PICTURES, fileName)
                    photoLinks[position].set(file.absolutePath)
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