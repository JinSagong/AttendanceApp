package com.jin.attendance_archive.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jin.attendance_archive.R
import kotlinx.android.synthetic.main.item_fp_check2.view.*

class FpCheck1Adapter2(private val list: ArrayList<String>, private val activity: Activity) :
    RecyclerView.Adapter<FpCheck1Adapter2.MyViewHolder>() {

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

        holder.name.text = item
        holder.delete.setOnClickListener {
            activity.currentFocus?.clearFocus()
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
    }
}