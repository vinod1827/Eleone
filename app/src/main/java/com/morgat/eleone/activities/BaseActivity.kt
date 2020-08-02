package com.morgat.eleone.activities

import androidx.appcompat.app.AppCompatActivity
import com.morgat.eleone.R

open class BaseActivity : AppCompatActivity() {

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.slide_from_right)
    }
}