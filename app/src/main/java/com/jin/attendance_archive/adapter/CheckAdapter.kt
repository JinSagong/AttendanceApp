package com.jin.attendance_archive.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.jin.attendance_archive.R
import com.jin.attendance_archive.model.DataCheck
import kotlinx.android.synthetic.main.item_check.view.*

class CheckAdapter(private val list: ArrayList<DataCheck>) :
    RecyclerView.Adapter<CheckAdapter.MyViewHolder>() {

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.checkTextView
        var idx: Int = 0
        val check: CheckBox = v.checkCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                list[idx].check = if (isChecked) "○" else ""
                reason.visibility = if (isChecked) View.INVISIBLE else View.VISIBLE
            }
        }
        val reason: EditText = v.checkEditText.apply {
            addTextChangedListener {
                list[idx].reason = it.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_check, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.idx = position

        if (item.name == "CATEGORY") {
            holder.check.visibility = View.GONE
            holder.reason.visibility = View.GONE
            holder.itemView.setBackgroundResource(R.color.colorPrimary)
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.WHITE))
            holder.name.text = item.check
            holder.itemView.setOnClickListener(null)
        } else {
            holder.check.visibility = View.VISIBLE
            holder.reason.visibility = View.VISIBLE
            holder.itemView.setBackgroundResource(R.color.WHITE)
            holder.name.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.colorText)
            )
            holder.name.text = item.name
            holder.check.isChecked = item.check == "○"
            holder.reason.setText(if (item.reason == "NULL") "" else item.reason)
            holder.reason.visibility = if (holder.check.isChecked) View.INVISIBLE else View.VISIBLE
            holder.itemView.setOnClickListener { holder.check.isChecked = !holder.check.isChecked }
        }
    }

    fun getResult(): ArrayList<DataCheck> {
        val result = arrayListOf<DataCheck>()
        list.forEach {
            if (it.name != "CATEGORY") result.add(
                DataCheck(
                    it.name,
                    if (it.check == "○") "TRUE" else "FALSE",
                    it.reason
                )
            )
        }
        return result
    }
}