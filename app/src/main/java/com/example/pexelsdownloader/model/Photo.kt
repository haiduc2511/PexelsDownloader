package com.example.pexelsdownloader.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import android.os.Parcel
import android.os.Parcelable
data class Photo(
    @SerializedName("id")
    var id: Int? = null,

    @SerializedName("width")
    @Expose
    var width: Int? = null,

    @SerializedName("height")
    @Expose
    var height: Int? = null,

    @SerializedName("url")
    @Expose
    var url: String? = null,

    @SerializedName("photographer")
    @Expose
    var photographer: String? = null,

    @SerializedName("photographer_url")
    @Expose
    var photographerUrl: String? = null,

    @SerializedName("src")
    @Expose
    var src: Src? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readValue(Int::class.java.classLoader) as? Int,
        width = parcel.readValue(Int::class.java.classLoader) as? Int,
        height = parcel.readValue(Int::class.java.classLoader) as? Int,
        url = parcel.readString(),
        photographer = parcel.readString(),
        photographerUrl = parcel.readString(),
        src = parcel.readParcelable(Src::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(width)
        parcel.writeValue(height)
        parcel.writeString(url)
        parcel.writeString(photographer)
        parcel.writeString(photographerUrl)
        parcel.writeParcelable(src, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "$url\n\n${src?.original}\n\n day la id: $id"
    }

    companion object CREATOR : Parcelable.Creator<Photo> {
        override fun createFromParcel(parcel: Parcel): Photo {
            return Photo(parcel)
        }

        override fun newArray(size: Int): Array<Photo?> {
            return arrayOfNulls(size)
        }
    }
}
