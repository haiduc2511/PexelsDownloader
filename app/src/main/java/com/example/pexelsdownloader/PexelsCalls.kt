package com.example.pexelsdownloader

import android.content.Context
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PexelsCalls(private val ctx: Context) {

    companion object {
        const val URL_VIDEO = "https://api.pexels.com/videos/"
        const val URL_IMAGE = "https://api.pexels.com/v1/"
        const val REQUEST_SIZE = 15
    }

    private fun builder(isImage: Boolean): Retrofit {
        val cacheSize = 5 * 1024 * 1024 // 5 MB
        val cacheControl = CacheControl.Builder().maxAge(12, TimeUnit.HOURS).build()
        val cache = Cache(ctx.cacheDir, cacheSize.toLong())

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", BuildConfig.API_KEY)
                    .addHeader("Cache-Control", cacheControl.toString())
                    .build()
                chain.proceed(newRequest)
            }
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .baseUrl(if (isImage) URL_IMAGE else URL_VIDEO)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getPopular(page: Int): Call<PexelsEntity?>? {
        return builder(true).create(PexelsService::class.java)
            .popular(REQUEST_SIZE, page)
    }

    fun getImageSearch(query: String, page: Int): Call<PexelsEntity?>? {
        return builder(true).create(PexelsService::class.java)
            .search(query, REQUEST_SIZE, page)
    }

    fun getVideoSearch(query: String, page: Int): Call<PexelsEntity?>? {
        return builder(false).create(PexelsService::class.java)
            .search(query, REQUEST_SIZE, page)
    }

    fun getCurated(page: Int): Call<PexelsEntity?>? {
        return builder(true).create(PexelsService::class.java)
            .curated(REQUEST_SIZE, page)
    }
}