package com.morgat.eleone.dialog

import android.app.Dialog
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.morgat.eleone.R
import com.morgat.eleone.adapters.SpinnerRecyclerViewAdapter
import kotlinx.android.synthetic.main.dialog_recycler_view.*

class CustomSpinnerDialog(
    context: Context, list: ArrayList<String>,
    private val customSpinnerDialog: CustomSpinnerDialogListener
) : Dialog(context) {

    init {
        setContentView(R.layout.dialog_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = SpinnerRecyclerViewAdapter(list)
        adapter.setSpinnerItemSelected(object :
            SpinnerRecyclerViewAdapter.SpinnerItemSelectedListener {
            override fun onSpinnerItemSelected(item: String) {
                this@CustomSpinnerDialog.dismiss()
                customSpinnerDialog.onItemSelected(item)
            }

        })
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    interface CustomSpinnerDialogListener {
        fun onItemSelected(item: String)
    }
}