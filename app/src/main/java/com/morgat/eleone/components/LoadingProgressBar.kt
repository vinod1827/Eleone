package com.morgat.eleone.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.morgat.eleone.R
import kotlinx.android.synthetic.main.layout_progress_bar.*

class LoadingProgressBar(private var context: Context) {

    private var mDialog: Dialog = Dialog(context)

    init {
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.layout_progress_bar)
        mDialog.progressBar.visibility = View.VISIBLE
        val wrapDrawable = DrawableCompat.wrap(mDialog.progressBar.indeterminateDrawable)
        DrawableCompat.setTint(
            wrapDrawable,
            ContextCompat.getColor(context, R.color.colorAccent)
        )
        mDialog.progressBar.indeterminateDrawable = DrawableCompat.unwrap(wrapDrawable)

        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setCancelable(true)
        mDialog.setCanceledOnTouchOutside(false)
    }

    fun showProgress() {
        if (!mDialog.isShowing)
            mDialog.show()
    }

    fun hideProgress() {
        if (mDialog.isShowing)
            mDialog.hide()
    }
}