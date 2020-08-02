package com.morgat.eleone.utils

import android.util.Log


object WebApi {
    const val URL = "http://morgat.in/eleone/API/eleone/index.php?p="
    const val GET_LANGUAGES = "${URL}get_language"
    const val CHECK_IF_REGISTERED = "${URL}get_existing_user"
    const val SIGNUP = "${URL}signup"
}

object WebParameterConstants {
    const val FID = "fbid"
    const val F_ID = "fb_id"
    const val FIRST_NAME ="first_name"
    const val LAST_NAME ="last_name"
    const val PROFILE_PIC ="profile_pic"
    const val GENDER ="gender"
    const val VERSION ="version"
    const val SIGN_UP_TYPE ="signup_type"
    const val DEVICE ="device"
    const val AGE ="age"

}

object IntentParameters {
    const val MOBILE_NUMBER = "mobile_number"
    const val EDIT_PROFILE = "edit_profile"
}

object AppConstants {
    const val TAG = "TAG"
    const val PATIENT_MODEL = "patients_model"
    const val APPOINTMENT_DETAIL_MODEL = "appointment_detail_model"
    const val ANDROID = "android"
    const val DOCTOR = "5ec76ad29727ef0024c1ea96"
    const val START_SERVICE = "startservice"
}




fun log(tag: String, message: String) = Log.e(tag, message)
