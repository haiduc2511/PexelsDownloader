package com.example.pexelsdownloader.fragment

import android.app.Activity
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.pexelsdownloader.R
import com.example.pexelsdownloader.databinding.FragmentVideoPlayerBinding


class VideoPlayerFragment : Fragment() {
    private var videoUris: List<Uri> = ArrayList()

    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var mediaCodec: MediaCodec
    private lateinit var mediaExtractor: MediaExtractor

    private val PICK_VIDEO_REQUEST = 1
    private var isPlaying = false
    private var isAutoReplayEnabled = false

    private var currentVideoNumber = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoPlayerBinding.inflate(layoutInflater)

        binding.ibAddVideo.setOnClickListener{
            selectVideo()
        }
        binding.ibPausePlayToggle.setOnClickListener {
            if (!isPaused) {
                pauseVideo()
            }
        }
        binding.ibPlayVideo.setOnClickListener{
            if (isPaused) {
                resumeVideo()
            }
        }

        binding.ibAutoReplay.setOnClickListener {
            isAutoReplayEnabled = !isAutoReplayEnabled
            val iconRes = if (isAutoReplayEnabled) R.drawable.ic_auto_replay_enabled else R.drawable.ic_auto_replay_disabled
            binding.ibAutoReplay.setImageResource(iconRes)
        }


        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: android.graphics.SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
//                clearVideo()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            VideoPlayerFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Getting image Uris"), PICK_VIDEO_REQUEST)
    }

    private fun clearVideo() {
        if (isPlaying) {
            try {
                mediaCodec.stop()
                mediaCodec.release()
                mediaExtractor.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isPlaying = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            val uris = mutableListOf<Uri>()

            if (clipData != null) {
                // Handle multiple selected videos
                for (i in 0 until clipData.itemCount) {
                    uris.add(clipData.getItemAt(i).uri)
                }
            } else {
                // Handle single video selection
                data?.data?.let { uris.add(it) }
            }

            videoUris = uris

            Log.d("Lay URI Video", uris.toString())

            // Process the URIs
            playVideo(videoUris.get(currentVideoNumber))
        }

    }

    private fun playVideo(uri: Uri) {
        try {
            initializeMediaComponents(uri, Surface(binding.textureView.surfaceTexture))
            val startTime = System.nanoTime()
            startDecodingThread(startTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeMediaComponents(uri: Uri, surface: Surface) {
        if (!surface.isValid) {
            throw IllegalArgumentException("Invalid Surface provided for playback")
        }

        mediaExtractor = MediaExtractor().apply {
            setDataSource(requireContext(), uri, null)
        }
        val format = mediaExtractor.getTrackFormat(0)
        val mimeType = format.getString(MediaFormat.KEY_MIME) ?: throw IllegalArgumentException("Invalid MIME type")
        if (!surface.isValid) {
            throw IllegalArgumentException("Invalid Surface provided for playback")
        }

        mediaCodec = MediaCodec.createDecoderByType(mimeType).apply {
            configure(format, surface, null, 0)
            start()
        }
        mediaExtractor.selectTrack(0)
    }
    private var isPaused = false
    private val pauseLock = Object()

    private fun startDecodingThread(startTime: Long) {
        Thread {
            var isEOS = false
            while (!isEOS) {
                // Pause handling
                synchronized(pauseLock) {
                    if (isPaused) {
                        pauseLock.wait() // Wait until the pause is lifted
                    }
                }
                isEOS = processInputBuffer(isEOS)
                processOutputBuffer(startTime)
            }
        }.start()
    }
    private var pauseOffsetNs: Long = 0L
    private var pauseStartNs: Long = 0L

    private fun pauseVideo() {
        isPaused = true
        pauseStartNs = System.nanoTime() // Record pause start time
    }

    private fun resumeVideo() {
        isPaused = false
        pauseOffsetNs += System.nanoTime() - pauseStartNs // Add paused duration
        synchronized(pauseLock) {
            pauseLock.notifyAll() // Wake up the decoding thread
        }
    }


    private fun processInputBuffer(isEOS: Boolean): Boolean {
        if (isEOS) return true

        val inputBufferId = mediaCodec.dequeueInputBuffer(10000)
        if (inputBufferId >= 0) {
            val inputBuffer = mediaCodec.getInputBuffer(inputBufferId)
            inputBuffer?.let {
                val sampleSize = mediaExtractor.readSampleData(it, 0)
                if (sampleSize < 0) {
                    // End of stream
                    mediaCodec.queueInputBuffer(
                        inputBufferId,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )

                    if (isAutoReplayEnabled) {
                        Thread.sleep(500) // Optional delay for smoother restart
                        restartVideo()
                    } else {
                        playNextVideo()
                    }

                    return true
                } else {
                    mediaCodec.queueInputBuffer(
                        inputBufferId,
                        0,
                        sampleSize,
                        mediaExtractor.sampleTime,
                        0
                    )
                    mediaExtractor.advance()
                }
            }
        }
        return false
    }

    private fun processOutputBuffer(startTime: Long) {
        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)

        when {
            outputBufferId >= 0 -> renderFrame(bufferInfo, outputBufferId, startTime)
            outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> handleFormatChange()
            outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER -> handleTryAgainLater()
        }
    }

    private fun renderFrame(bufferInfo: MediaCodec.BufferInfo, outputBufferId: Int, startTime: Long) {
        val presentationTimeNs = bufferInfo.presentationTimeUs * 1000
        val adjustedTimeNs = startTime + presentationTimeNs + pauseOffsetNs
        val delayNs = adjustedTimeNs - System.nanoTime()

        if (delayNs > 0) {
            Thread.sleep(delayNs / 1_000_000, (delayNs % 1_000_000).toInt())
        }
        mediaCodec.releaseOutputBuffer(outputBufferId, true)
    }

    private fun handleFormatChange() {
        // Handle format change logic here if needed
    }

    private fun handleTryAgainLater() {
        // Handle "try again later" logic here if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        clearVideo() // Clean up resources
    }
    private fun restartVideo() {
        val surfaceTexture = binding.textureView.surfaceTexture
        if (surfaceTexture != null) {
            val surface = Surface(surfaceTexture)
            mediaCodec.reset()
            initializeMediaComponents(videoUris[currentVideoNumber], surface)
            startDecodingThread(System.nanoTime())
        } else {
            println("SurfaceTexture is null, cannot replay video.")
        }
    }

    private fun playNextVideo() {
        currentVideoNumber = (currentVideoNumber + 1) % videoUris.size
        val nextVideoUri = videoUris[currentVideoNumber]

        val surfaceTexture = binding.textureView.surfaceTexture
        if (surfaceTexture != null) {
            val surface = Surface(surfaceTexture)
            clearVideo() // Release resources for the current video
            try {
                initializeMediaComponents(nextVideoUri, surface)
                startDecodingThread(System.nanoTime())
            } catch (e: Exception) {
                Log.e("VideoPlayer", "Error while playing next video", e)
            }
        } else {
            println("SurfaceTexture is null, cannot play next video.")
        }
    }

}