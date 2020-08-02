package com.morgat.eleone.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.morgat.eleone.models.UserModel

class Prefs(context: Context) {

    val PREFS_FILENAME = "com.morgat.eleone.prefs"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0);

    val USER_DETAILS = "user_details"
    val VERIFICATION_COMPLETED = "verification_completed"
    val VERIFICATION_ID = "verification_id"
    val SETUP_COMPLETED = "SETUP_COMPLETED"
    val MOBILE_NUMBER = "MOBILE_NUMBER"
    val FIRST_NAME = "FIRST_NAME"
    val LAST_NAME = "LAST_NAME"
    val EMAIL_ADDRESS = "EMAIL_ADDRESS"
    val LOGIN_TYPE = "login_type"
    val DEVICE_ID = "DEVICE_ID"
    val RECEIVED_OTP = "received_otp"
    val FIREBASE_TOKEN = "firebase_token"
    val USER_ID = "user_id"
    val CITY = "city"


    var userId: String?
        get() = prefs.getString(USER_ID, "")
        set(value) = prefs.edit().putString(USER_ID, value).apply()


    var city: String?
        get() = prefs.getString(CITY, "")
        set(value) = prefs.edit().putString(CITY, value).apply()



    var mobileNumber: String?
        get() = prefs.getString(MOBILE_NUMBER, "")
        set(value) = prefs.edit().putString(MOBILE_NUMBER, value).apply()

    var firstName: String?
        get() = prefs.getString(FIRST_NAME, "")
        set(value) = prefs.edit().putString(FIRST_NAME, value).apply()

    var lastName: String?
        get() = prefs.getString(LAST_NAME, "")
        set(value) = prefs.edit().putString(LAST_NAME, value).apply()

    var emailAddress: String?
        get() = prefs.getString(EMAIL_ADDRESS, "")
        set(value) = prefs.edit().putString(EMAIL_ADDRESS, value).apply()

    var receivedOtp: String?
        get() = prefs.getString(RECEIVED_OTP, "")
        set(value) = prefs.edit().putString(RECEIVED_OTP, value).apply()

    var verificationId: String?
        get() = prefs.getString(VERIFICATION_ID, "")
        set(value) = prefs.edit().putString(VERIFICATION_ID, value).apply()


    var deviceId: String?
        get() = prefs.getString(DEVICE_ID, "")
        set(value) = prefs.edit().putString(DEVICE_ID, value).apply()


    var firebasetoken: String?
        get() = prefs.getString(FIREBASE_TOKEN, "")
        set(value) = prefs.edit().putString(FIREBASE_TOKEN, value).apply()

    var userModel: UserModel?
           get() = Gson().fromJson(prefs.getString(USER_DETAILS, ""), UserModel::class.java)
           set(value) = prefs.edit().putString(USER_DETAILS, Gson().toJson(value)).apply()

    var isSetupCompleted: Boolean
        get() = prefs.getBoolean(SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(SETUP_COMPLETED, value).apply()

    var isVerificationCompleted: Boolean
        get() = prefs.getBoolean(VERIFICATION_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(VERIFICATION_COMPLETED, value).apply()
}