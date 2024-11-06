package com.example.pexelsdownloader

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("video_files")
    @Expose
    var videoFiles: List<VideoFile> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        videoFiles = parcel.createTypedArrayList(VideoFile.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(videoFiles)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Video> {
        override fun createFromParcel(parcel: Parcel): Video {
            return Video(parcel)
        }

        override fun newArray(size: Int): Array<Video?> {
            return arrayOfNulls(size)
        }
    }
}
