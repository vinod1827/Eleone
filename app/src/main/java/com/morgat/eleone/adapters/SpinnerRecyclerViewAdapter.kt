package com.morgat.eleone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morgat.eleone.R
import kotlinx.android.synthetic.main.single_spinner_item.view.*
import kotlin.collections.ArrayList

class SpinnerRecyclerViewAdapter(private var genders: ArrayList<String>) :
    RecyclerView.Adapter<SpinnerRecyclerViewAdapter.SpinnerViewholder>() {

    inner class SpinnerViewholder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerViewholder {
        return SpinnerViewholder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.single_spinner_item,
                parent,
                false
            )
        )
    }

    fun setSpinnerItemSelected(spinnerItemSelectedListener: SpinnerItemSelectedListener) {
        this.spinnerItemSelectedListener = spinnerItemSelectedListener
    }

    override fun getItemCount(): Int = genders.size

    override fun onBindViewHolder(holder: SpinnerViewholder, position: Int) {
        holder.itemView.spinnerTextView.text = genders[position]
        holder.itemView.setOnClickListener {
            spinnerItemSelectedListener.onSpinnerItemSelected(genders[position])
        }
    }

    private lateinit var spinnerItemSelectedListener: SpinnerItemSelectedListener

    interface SpinnerItemSelectedListener {
        fun onSpinnerItemSelected(
            item: String
        )
    }
}