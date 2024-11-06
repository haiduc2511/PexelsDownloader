package com.example.pexelsdownloader

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface PexelsService {
    @GET("popular")
    fun popular(@Query("per_page") perPage: Int, @Query("page") page: Int): Call<PexelsEntity?>?

    @GET("search")
    fun search(
        @Query("query") query: String?,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Call<PexelsEntity?>?

    @GET("search")
    fun searchAll(
        @Query("query") query: String?,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Observable<PexelsEntity?>?

    @GET("curated")
    fun curated(@Query("per_page") perPage: Int, @Query("page") page: Int): Call<PexelsEntity?>?
}