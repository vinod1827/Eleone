package com.morgat.eleone.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LanguageModel(val languageId : String = "",val languageName : String = "") : Parcelable