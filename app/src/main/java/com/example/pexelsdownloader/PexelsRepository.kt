package com.example.pexelsdownloader

import android.content.Context
import retrofit2.Call

class PexelsRepository(ctx: Context) {

    private val pexelsService: PexelsCalls = PexelsCalls(ctx)

    fun getPopular(page: Int): Call<PexelsEntity?>? {
        return pexelsService.getPopular(page)
    }

    fun getVideoSearch(query: String, page: Int): Call<PexelsEntity?>? {
        return pexelsService.getVideoSearch(query, page)
    }

    fun getImageSearch(query: String, page: Int): Call<PexelsEntity?>? {
        return pexelsService.getImageSearch(query, page)
    }

    fun getCurated(page: Int): Call<PexelsEntity?>? {
        return pexelsService.getCurated(page)
    }
}
