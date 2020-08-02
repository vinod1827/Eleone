package com.morgat.eleone.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserModel(@SerializedName("fb_id")
                     var id: String? = "0",
                     var username: String? = "",
                     @SerializedName("first_name")
                     var firstName: String? = "",
                     @SerializedName("last_name")
                     var lastName: String? = "",
                     var gender: String? = "",
                     var bio: String? = "",
                     var age: String? = "",
                     @SerializedName("profile_pic")
                     var profileImageUrl: String? = "",
                     @SerializedName("background_image")
                     var backgroundImageUrl: String? = "",
                     var created: String? = "") : Parcelable