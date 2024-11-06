package com.example.pexelsdownloader
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Src(
    @SerializedName("original")
    @Expose
    var original: String? = null,

    @SerializedName("large2x")
    @Expose
    var large2x: String? = null,

    @SerializedName("large")
    @Expose
    var large: String? = null,

    @SerializedName("medium")
    @Expose
    var medium: String? = null,

    @SerializedName("small")
    @Expose
    var small: String? = null,

    @SerializedName("portrait")
    @Expose
    var portrait: String? = null,

    @SerializedName("square")
    @Expose
    var square: String? = null,

    @SerializedName("landscape")
    @Expose
    var landscape: String? = null,

    @SerializedName("tiny")
    @Expose
    var tiny: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        original = parcel.readString(),
        large2x = parcel.readString(),
        large = parcel.readString(),
        medium = parcel.readString(),
        small = parcel.readString(),
        portrait = parcel.readString(),
        square = parcel.readString(),
        landscape = parcel.readString(),
        tiny = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(original)
        parcel.writeString(large2x)
        parcel.writeString(large)
        parcel.writeString(medium)
        parcel.writeString(small)
        parcel.writeString(portrait)
        parcel.writeString(square)
        parcel.writeString(landscape)
        parcel.writeString(tiny)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Src> {
        override fun createFromParcel(parcel: Parcel): Src {
            return Src(parcel)
        }

        override fun newArray(size: Int): Array<Src?> {
            return arrayOfNulls(size)
        }
    }
}
