package com.example.pexelsdownloader

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pexelsdownloader.databinding.ActivityVideoPlayerBinding
import com.example.pexelsdownloader.fragment.VideoPlayerFragment

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentTopLeft.id, VideoPlayerFragment()).commit()

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentTopRight.id, VideoPlayerFragment()).commit()

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentBottomLeft.id, VideoPlayerFragment()).commit()

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentBottomRight.id, VideoPlayerFragment()).commit()




    }
}