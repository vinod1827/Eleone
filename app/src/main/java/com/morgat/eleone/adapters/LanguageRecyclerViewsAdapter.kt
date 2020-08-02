package com.morgat.eleone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morgat.eleone.R
import com.morgat.eleone.models.LanguageModel
import kotlinx.android.synthetic.main.single_language_layout.view.*

class LanguageRecyclerViewsAdapter :
        RecyclerView.Adapter<LanguageRecyclerViewsAdapter.LanguageViewHolder>() {

    private var originalList = ArrayList<LanguageModel>()

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.single_language_layout,
                        parent,
                        false
                )
        )
    }

    fun updateList(newList: ArrayList<LanguageModel>) {
        originalList.clear()
        originalList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setPatientClickListener(LanguageelectedListener: LanguageSelectedListener) {
        this.LanguageelectedListener = LanguageelectedListener
    }

    override fun getItemCount(): Int = originalList.size

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val item = originalList[position]
        holder.itemView.languageTextView.text = item.languageName
        holder.itemView.setOnClickListener {
            LanguageelectedListener.onLanguageelected(item)
        }
    }

    private lateinit var LanguageelectedListener: LanguageSelectedListener

    interface LanguageSelectedListener {
        fun onLanguageelected(
                Language: LanguageModel
        )
    }
}