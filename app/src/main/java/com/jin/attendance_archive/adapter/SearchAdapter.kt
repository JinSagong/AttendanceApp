package com.jin.attendance_archive.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jin.attendance_archive.R
import com.jin.attendance_archive.util.SearchCallback
import kotlinx.android.synthetic.main.item_search.view.*

class SearchAdapter(private val list: ArrayList<Pair<String, String>>, private val mCallback: SearchCallback) :
    RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.searchLabelTextView
        val add: TextView = v.searchAddTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        val label = "[${item.first}] ${item.second}"
        holder.name.text = label
        holder.add.setOnClickListener { mCallback.add(item.second) }
    }
}