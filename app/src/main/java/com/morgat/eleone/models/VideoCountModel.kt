package com.morgat.eleone.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoCountModel(@SerializedName("like_count")
                           var likeCount: String = "0",
                           @SerializedName("video_comment_count")
                           var videoCommentCount: String = "0",
                           @SerializedName("view")
                           var views: String = "") : Parcelable