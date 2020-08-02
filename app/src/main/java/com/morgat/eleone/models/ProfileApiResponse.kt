package com.morgat.eleone.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProfileApiResponse(@SerializedName("user_info")
                              var userInfo: UserModel? = null,
                              @SerializedName("total_following")
                              var totalFollowing: String = "0",
                              @SerializedName("total_fans")
                              var totalFans: String = "0",
                              @SerializedName("total_heart")
                              var totalLikes: String = "0") : Parcelable