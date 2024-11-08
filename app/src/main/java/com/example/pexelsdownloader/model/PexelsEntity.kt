package com.example.pexelsdownloader.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import android.os.Parcel
import android.os.Parcelable
data class PexelsEntity(
    @SerializedName("total_results")
    @Expose
    var totalResults: Int,

    @SerializedName("page")
    @Expose
    var page: Int,

    @SerializedName("per_page")
    @Expose
    var perPage: Int,

    @SerializedName("photos")
    @Expose
    var photos: List<Photo>?,

    @SerializedName("videos")
    @Expose
    var videos: List<Video>?,

    @SerializedName("next_page")
    @Expose
    var nextPage: String?,

    @SerializedName("prev_page")
    @Expose
    var prevPage: String?
) : Parcelable {
    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(Photo),
        parcel.createTypedArrayList(Video),
        parcel.readString(),
        parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(totalResults)
        parcel.writeInt(page)
        parcel.writeInt(perPage)
        parcel.writeTypedList(photos)
        parcel.writeTypedList(videos)
        parcel.writeString(nextPage)
        parcel.writeString(prevPage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PexelsEntity> {
        override fun createFromParcel(parcel: Parcel): PexelsEntity {
            return PexelsEntity(parcel)
        }

        override fun newArray(size: Int): Array<PexelsEntity?> {
            return arrayOfNulls(size)
        }
    }
}