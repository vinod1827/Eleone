package com.morgat.eleone.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SoundModel(var id: String = "",
                      @SerializedName("sound_name")
                      var soundName: String = "",
                      @SerializedName("thum")
                      var soundPicture: String = "") : Parcelable