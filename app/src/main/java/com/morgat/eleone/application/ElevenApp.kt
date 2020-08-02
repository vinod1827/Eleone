package com.morgat.eleone.application

import android.app.Application
import com.google.firebase.FirebaseApp
import com.morgat.eleone.utils.NukeSSLCerts
import com.morgat.eleone.utils.Prefs


class ElevenApp : Application() {

    val prefs: Prefs? by lazy {
        preffs
    }

    companion object {
        var preffs: Prefs? = null
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        preffs = Prefs(applicationContext)
    }
}