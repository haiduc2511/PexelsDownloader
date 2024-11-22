package com.example.pexelsdownloader.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.SurfaceTexture
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
import androidx.appcompat.app.AppCompatActivity
import com.example.pexelsdownloader.R
import com.example.pexelsdownloader.databinding.FragmentVideoPlayerBinding
import java.nio.ByteBuffer


class VideoPlayerFragment : Fragment() {
    private lateinit var textureView: TextureView
    private val PICK_VIDEOS_REQUEST = 1002
    private val videoUriList = mutableListOf<Uri>()
    private var currentVideoIndex = 0
    private lateinit var extractor: MediaExtractor
    private lateinit var codec: MediaCodec
    private var videoTrackIndex: Int = -1
    private lateinit var binding: FragmentVideoPlayerBinding

    private var isPaused = false
    private val pauseLock = Object()

    private var isOnAutoReplay = false

    private var playbackSpeed = 1.0f // Default speed is 1.0x

    private var isStopped = true

    private var isFirstTime = true
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

        textureView = binding.textureView
        binding.ibAddVideo.setOnClickListener{
            pickVideosFromDevice()
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
            if (isStopped && videoUriList.isNotEmpty()) {
                if (currentVideoIndex >= videoUriList.size) {
                    currentVideoIndex = 0;
                }
                startVideo()
                Log.d("ibListVideo", "playNextVideo")
            }
        }
        binding.ibListVideo.setOnClickListener{
            openListVideoUri()
        }
        binding.ibSpeedUp.setOnClickListener {
            playbackSpeed += 0.25f // Increase speed by 0.25x
            Log.d("MainActivity", "Playback speed: $playbackSpeed")
        }
        binding.ibSpeedDown.setOnClickListener {
            playbackSpeed = maxOf(0.25f, playbackSpeed - 0.25f) // Decrease speed but keep >= 0.25x
            Log.d("MainActivity", "Playback speed: $playbackSpeed")
        }
        binding.ibAutoReplay.setOnClickListener{
            if (isOnAutoReplay) {
                binding.ibAutoReplay.setImageResource(R.drawable.ic_auto_replay_disabled)
                isOnAutoReplay = false
            } else {
                binding.ibAutoReplay.setImageResource(R.drawable.ic_auto_replay_enabled)
                isOnAutoReplay = true
            }
        }


        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                if (videoUriList.isNotEmpty()) {
                    playNextVideo(Surface(textureView.surfaceTexture))
                }
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
        }

        // Trigger file picker when the app starts

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
    private fun pickVideosFromDevice() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_VIDEOS_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VIDEOS_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
//            videoUriList.clear()

            // Handle multiple video selection
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    videoUriList.add(clipData.getItemAt(i).uri)
                }
            } ?: data?.data?.let { uri ->
                // Single video selection
                videoUriList.add(uri)
            }

            Log.d("MainActivity", "Selected video URIs: $videoUriList")
        }
        if (isFirstTime) {
            startVideo()
            isFirstTime = false
            isStopped = false
        }
    }

    private fun playNextVideo(surface: Surface) {
        if (currentVideoIndex < videoUriList.size) {
            val videoUri = videoUriList[currentVideoIndex]
            currentVideoIndex++
            playVideo(surface, videoUri)
        } else {
            if (isOnAutoReplay && videoUriList.isNotEmpty()) {
                Log.d("MainActivity", "All videos played.")
                currentVideoIndex = 0;
                val videoUri = videoUriList[currentVideoIndex]
                currentVideoIndex++
                playVideo(surface, videoUri)
            } else {
                isStopped = true
            }
        }
    }

    private fun playVideo(surface: Surface, uri: Uri) {
        initializeMediaComponents(surface, uri)
        startDecodingThread(System.nanoTime()) {
            playNextVideo(surface)
        }
    }

    private fun initializeMediaComponents(surface: Surface, uri: Uri) {
        extractor = MediaExtractor()
        extractor.setDataSource(requireContext(), uri, null)

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)

            if (mime != null && mime.startsWith("video/")) {
                videoTrackIndex = i
                extractor.selectTrack(i)
                codec = MediaCodec.createDecoderByType(mime)
                codec.configure(format, surface, null, 0)
                codec.start()
                break
            }
        }
    }

    private fun startDecodingThread(startTime: Long, onComplete: () -> Unit) {
        Thread {
            var isEOS = false
            while (true) {
                synchronized(pauseLock) {
                    if (isPaused) {
                        pauseLock.wait() // Wait until the pause is lifted
                    }
                }

                isEOS = processInputBuffer(isEOS)
                val result = processOutputBuffer(startTime)
                if (!result) break
            }
            codec.stop()
            codec.release()
            extractor.release()

            // Call completion callback after decoding finishes
            requireActivity().runOnUiThread { onComplete() }
        }.start()
    }

    private fun processInputBuffer(isEOS: Boolean): Boolean {
        if (isEOS) return true

        val inputBufferIndex = codec.dequeueInputBuffer(10000)
        if (inputBufferIndex >= 0) {
            val inputBuffer: ByteBuffer = codec.inputBuffers[inputBufferIndex]
            val sampleSize = extractor.readSampleData(inputBuffer, 0)

            if (sampleSize < 0) {
                codec.queueInputBuffer(
                    inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                return true
            } else {
                codec.queueInputBuffer(
                    inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0
                )
                extractor.advance()
            }
        }
        return false
    }

    private fun processOutputBuffer(startTime: Long): Boolean {
        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000)

        when (outputBufferId) {
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> codec.outputBuffers
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> codec.outputFormat
            MediaCodec.INFO_TRY_AGAIN_LATER -> return true
            else -> {
                renderFrame(bufferInfo, outputBufferId, startTime)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    return false
                }
            }
        }
        return true
    }

    private fun renderFrame(
        bufferInfo: MediaCodec.BufferInfo,
        outputBufferId: Int,
        startTime: Long
    ) {
        val adjustedPresentationTime = bufferInfo.presentationTimeUs / playbackSpeed
        val delay = adjustedPresentationTime * 1000 + startTime + pauseOffsetNs - System.nanoTime()
        if (delay > 0) {
            Thread.sleep((delay / 1_000_000).toLong(), (delay % 1_000_000).toInt())
        }
        codec.releaseOutputBuffer(outputBufferId, true)
    }

    private var pauseOffsetNs: Long = 0L
    private var pauseStartNs: Long = 0L

    private fun pauseVideo() {
        isPaused = true
        pauseStartNs = System.nanoTime() // Record pause start time
    }

    private fun startVideo() {
        playNextVideo(Surface(textureView.surfaceTexture))
        isStopped = false
    }
    private fun resumeVideo() {
        isPaused = false
        pauseOffsetNs += System.nanoTime() - pauseStartNs // Add paused duration
        synchronized(pauseLock) {
            pauseLock.notifyAll() // Wake up the decoding thread
        }
    }
    fun openListVideoUri() {
        childFragmentManager.beginTransaction()
            .replace(binding.fragmentVideoUriList.id, ListVideoUriFragment.newInstance(videoUriList)).commit()
    }

}