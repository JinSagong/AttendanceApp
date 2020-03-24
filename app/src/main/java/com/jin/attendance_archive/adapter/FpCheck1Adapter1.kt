package com.jin.attendance_archive.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.jin.attendance_archive.R
import com.jin.attendance_archive.model.DataFpCheck
import com.jin.attendance_archive.viewmodel.FpCheckViewmodel
import kotlinx.android.synthetic.main.item_fp_check1.view.*

class FpCheck1Adapter1(
    private val list: ArrayList<DataFpCheck>,
    private val activity: Activity,
    private val viewmodel: FpCheckViewmodel
) :
    RecyclerView.Adapter<FpCheck1Adapter1.MyViewHolder>() {

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.fpCheck1TextView
        var idx: Int = 0
        val content: EditText = v.fpCheck1EditText.apply {
            addTextChangedListener { viewmodel.update(list[idx], "content", it.toString()) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fp_check1, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.idx = position

        val checkby = if (item.check == "2.0" && item.type != item.checkby) " <${item.category}>" else ""
        holder.name.text = when (item.check) {
            "0.0" -> "[결석] ${item.name}"
            "1.0" -> "[헌신] ${item.name}"
            else -> "[출석] ${item.name}$checkby"
        }
        holder.itemView.setBackgroundResource(
            when (item.check) {
                "0.0" -> R.color.WHITE
                "1.0" -> R.color.colorDedication
                else -> R.color.colorAttendance
            }
        )
        holder.content.setText(if (item.content == "NULL") "" else item.content)
        holder.content.visibility = if (item.check == "2.0") View.INVISIBLE else View.VISIBLE
        holder.itemView.setOnClickListener {
            activity.currentFocus?.clearFocus()
            viewmodel.update(
                item, "check", when (item.check) {
                    "0.0" -> "2.0"
                    "1.0" -> "0.0"
                    else -> "1.0"
                }
            )
            notifyItemChanged(position)
        }
    }
}