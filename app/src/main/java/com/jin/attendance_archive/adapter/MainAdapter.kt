package com.jin.attendance_archive.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.attendance_archive.R
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.view.FpCheck1Activity
import com.jin.attendance_archive.model.DataMainLabel
import com.jin.attendance_archive.view.CheckActivity
import com.jin.attendance_archive.view.FpCheck2Activity
import com.jin.attendance_archive.view.SelectionActivity
import kotlinx.android.synthetic.main.dialog_alert1.*
import kotlinx.android.synthetic.main.item_label.view.*
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.collections.ArrayList

class MainAdapter(
    private val list: ArrayList<DataMainLabel>,
    private val key: String, private val name: String,
    private val conditionDialog: BottomSheetDialog
) :
    RecyclerView.Adapter<MainAdapter.MyViewHolder>() {
    private var fpType = "fp"
    private var homeType = "#"
    private var typeList = listOf("general", "group", "bc", "fp")

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val label: TextView = v.labelTextView
        val identifier: ImageView = v.identifierImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_label, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        holder.itemView.setBackgroundResource(
            if (item.status == "NULL") R.color.colorPrimary else R.color.WHITE
        )
        holder.label.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                if (item.status == "NULL") R.color.WHITE else R.color.colorText
            )
        )
        holder.label.text = item.label
        holder.identifier.setImageResource(
            when (item.status) {
                "TRUE" -> R.drawable.item_status_true
                "ONGOING" -> R.drawable.item_status_ongoing
                "FALSE" -> R.drawable.item_status_false
                else -> 0
            }
        )
        holder.itemView.setOnClickListener {
            when {
                // 라벨
                item.status == "NULL" -> return@setOnClickListener

                // 오프라인 출석
                item.type in typeList ->
                    if (Client.offline || key == "jin") it.context.startActivity<SelectionActivity>(
                        "type" to item.type, "label" to item.label, "key" to key, "name" to name
                    ) else conditionDialog.run {
                        alert1DescriptionTextView.text =
                            holder.itemView.context.getString(R.string.condition_offline)
                        show()
                    }

                // 온라인 출석
                item.type.substring(1, 2) == homeType ->
                    if (Client.online || key == "jin") it.context.startActivity<SelectionActivity>(
                        "type" to item.type, "label" to item.label, "key" to key, "name" to name
                    ) else conditionDialog.run {
                        alert1DescriptionTextView.text =
                            holder.itemView.context.getString(R.string.condition_online)
                        show()
                    }

                // 현장전도
                item.type.substring(0, 2) == fpType -> when {
                    key != "jin" && key != "fpadmin"
                            && key != "bc${(item.type.replace("fp", "").toInt() + 1) / 2 - 7}"
                    -> conditionDialog.run {
                        alert1DescriptionTextView.text = getInfo()
                        show()
                    }
                    item.type.substring(2).toInt() % 2 == 1 -> it.context.startActivity<FpCheck1Activity>(
                        "type" to item.type, "label" to item.label, "key" to key
                    )
                    list[position - 1].status == "TRUE" -> it.context.startActivity<FpCheck2Activity>(
                        "type" to item.type, "label" to item.label, "key" to key
                    )
                    else -> conditionDialog.run {
                        alert1DescriptionTextView.text =
                            holder.itemView.context.getString(R.string.condition_fp_fruits)
                        show()
                    }
                }

                // 온라인 출석 체크
                item.type.elementAt(item.type.length - 2) == '#' -> {
                    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    val idx = item.type.substring(item.type.length - 1).toInt()
                    if (idx <= 3 || (idx == 4 && day >= 4) || (idx == 5 && day >= 6))
                        if (key == "jin" || key == "supervisor"
                            || key == item.type.substring(0, item.type.length - 2)
                            || (key.substring(0, 2) == item.type.substring(0, 2)
                                    && key.contains("admin"))
                        ) it.context.startActivity<CheckActivity>(
                            "type" to item.type, "label" to item.label, "key" to key
                        ) else conditionDialog.run {
                            alert1DescriptionTextView.text = getInfo()
                            show()
                        }
                    else conditionDialog.run {
                        alert1DescriptionTextView.text =
                            holder.itemView.context.getString(R.string.condition_time)
                        show()
                    }
                }

                // 오프라인 출석 체크
                else -> if (key == "jin" || key == "supervisor" || key == item.type
                    || (key.substring(0, 2) == item.type.substring(0, 2) && key.contains("admin"))
                ) it.context.startActivity<CheckActivity>(
                    "type" to item.type, "label" to item.label, "key" to key
                ) else conditionDialog.run {
                    alert1DescriptionTextView.text = getInfo()
                    show()
                }
            }
        }
    }

    private fun getInfo(): String = when (key) {
        "jin" -> "$name 계정입니다.\n전체 시스템을 관리하실 수 있습니다."
        "supervisor" -> "$name 계정입니다.\n전체 출석을 관리하실 수 있습니다."
        "generaladmin" -> "$name 계정입니다.\n일반남여전도회 출석을 관리하실 수 있습니다."
        "groupadmin" -> "$name 계정입니다.\n기관 출석을 관리하실 수 있습니다."
        "bcadmin" -> "$name 계정입니다.\n지교회 출석을 관리하실 수 있습니다."
        "fpadmin" -> "$name 계정입니다.\n현장전도 출석/열매를 관리하실 수 있습니다."
        else -> "$name 계정입니다.\n관련부분 출석을 관리하실 수 있습니다."
    }
}