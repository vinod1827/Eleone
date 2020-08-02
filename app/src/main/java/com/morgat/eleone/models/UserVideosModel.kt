package com.morgat.eleone.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserVideosModel(@SerializedName("count")
                           var countModel: VideoCountModel? = null,
                           @SerializedName("sound")
                           var soundModel: SoundModel? = null,
                           var id: String = "",
                           var liked: String = "",
                           var gif: String = "",
                           @SerializedName("video_url")
                           var videoUrl: String = "",
                           @SerializedName("thum")
                           var thumbnail: String = "",
                           @SerializedName("created")
                           var createdDate: String = "",
                           @SerializedName("description")
                           var videoDescription: String = "") : Parcelable