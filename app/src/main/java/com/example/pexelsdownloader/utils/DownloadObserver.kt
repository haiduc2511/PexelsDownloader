package com.example.pexelsdownloader.utils

import android.app.DownloadManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.widget.Toast

class DownloadObserver(
    handler: Handler,
    private val context: Context,
    private val downloadId: Long,
    private val downloadManager: DownloadManager,
    private var downloadProgressListener: DownloadProgressListener
) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d("DownloadObserver", "Download $uri updated")
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        if (cursor != null && cursor.moveToFirst()) {
            val bytesDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val bytesTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            if (bytesTotal > 0) {
                val progress = (bytesDownloaded * 100L) / bytesTotal
                Log.d("DownloadObserver", "Download progress: $progress%")

                // Optional: Show progress as a Toast (for demo purposes)
                Toast.makeText(context, "Download progress: $progress%", Toast.LENGTH_SHORT).show()
                downloadProgressListener.onProgressFetch(progress);
            }
        }
        cursor?.close()

    }
}
