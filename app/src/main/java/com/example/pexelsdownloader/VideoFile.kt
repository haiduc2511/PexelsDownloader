package com.example.pexelsdownloader

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VideoFile(
    @SerializedName("id")
    @Expose
    var id: Int? = null,

    @SerializedName("quality")
    @Expose
    var quality: String? = null,

    @SerializedName("file_type")
    @Expose
    var fileType: String? = null,

    @SerializedName("link")
    @Expose
    var link: String? = null
) : Parcelable {

    // Constructor to read from Parcel
    constructor(parcel: Parcel) : this(
        id = parcel.readValue(Int::class.java.classLoader) as? Int,
        quality = parcel.readString(),
        fileType = parcel.readString(),
        link = parcel.readString()
    )

    // Method to write to Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(quality)
        parcel.writeString(fileType)
        parcel.writeString(link)
    }

    // Describe contents (typically 0 unless using file descriptors)
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoFile> {
        override fun createFromParcel(parcel: Parcel): VideoFile {
            return VideoFile(parcel)
        }

        override fun newArray(size: Int): Array<VideoFile?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "$link\n"
    }
}
