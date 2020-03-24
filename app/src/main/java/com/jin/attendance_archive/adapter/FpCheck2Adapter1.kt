package com.jin.attendance_archive.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jin.attendance_archive.R
import com.jin.attendance_archive.model.DataBeliever
import com.jin.attendance_archive.util.FpFruitsCallback
import kotlinx.android.synthetic.main.item_fp_check2.view.*

class FpCheck2Adapter1(
    private val list: ArrayList<DataBeliever>,
    private val mCallback: FpFruitsCallback
) : RecyclerView.Adapter<FpCheck2Adapter1.MyViewHolder>() {

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.fpCheck2LabelTextView
        val delete: TextView = v.fpCheck2DeleteTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fp_check2, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        val label = "[${item.preacher}] ${item.believer}"
        holder.name.text = label
        holder.delete.setOnClickListener {
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
        holder.itemView.setOnClickListener { mCallback.modifyBeliever(position, item) }
    }
}