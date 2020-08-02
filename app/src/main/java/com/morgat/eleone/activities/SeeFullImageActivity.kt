package com.morgat.eleone.activities

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.morgat.eleone.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_see_full_image.*

/**
 * A simple [Fragment] subclass.
 */
class SeeFullImageActivity : Fragment() {
    var width = 0
    var height = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_see_full_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels
        val imageUrl = arguments!!.getString("image_url")
        closeGallery.setOnClickListener { requireActivity().onBackPressed() }
        if (imageUrl?.isEmpty() == true) {
            Picasso.get()
                    .load(R.drawable.tempbackground).into(singleImageView)
        } else {
            progressBar.visibility = View.VISIBLE
            Picasso.get().load(imageUrl).placeholder(R.drawable.image_placeholder)
                    .into(singleImageView, object : Callback {
                        override fun onSuccess() {
                            progressBar.visibility = View.GONE
                        }

                        override fun onError(e: Exception) {
                            progressBar.visibility = View.GONE
                        }
                    })
        }
    }
}