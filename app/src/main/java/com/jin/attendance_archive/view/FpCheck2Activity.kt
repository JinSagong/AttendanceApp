package com.jin.attendance_archive.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.attendance_archive.R
import com.jin.attendance_archive.adapter.FpCheck2Adapter1
import com.jin.attendance_archive.adapter.FpCheck2Adapter2
import com.jin.attendance_archive.adapter.SearchAdapter
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.client.ClientCallback
import com.jin.attendance_archive.model.DataBeliever
import com.jin.attendance_archive.model.DataWordMovement
import com.jin.attendance_archive.util.FpFruitsCallback
import com.jin.attendance_archive.util.SearchCallback
import com.jin.attendance_archive.util.myToast
import com.jin.attendance_archive.viewmodel.FpCheckViewmodel
import kotlinx.android.synthetic.main.activity_fp_check2.*
import kotlinx.android.synthetic.main.dialog_alert1.*
import kotlinx.android.synthetic.main.dialog_alert2.*
import kotlinx.android.synthetic.main.dialog_fp_check2_1.*
import kotlinx.android.synthetic.main.dialog_fp_check2_2.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity

import org.jetbrains.anko.toast

class FpCheck2Activity : AppCompatActivity() {
    private val client = Client()

    private val type by lazy { intent.getStringExtra("type") }
    private val label by lazy { intent.getStringExtra("label") }
    private val key by lazy { intent.getStringExtra("key") }

    private val itemList1 by lazy { arrayListOf<DataBeliever>() }
    private lateinit var mAdapter1: FpCheck2Adapter1
    private val itemList2 by lazy { arrayListOf<DataWordMovement>() }
    private lateinit var mAdapter2: FpCheck2Adapter2

    private val fruits1Dialog by lazy { BottomSheetDialog(this) }
    private val fruits2Dialog by lazy { BottomSheetDialog(this) }
    private val postDialog by lazy { BottomSheetDialog(this) }
    private val alertDialog by lazy { BottomSheetDialog(this) }
    private val backDialog by lazy { BottomSheetDialog(this) }
    private val progressDialog by lazy {
        indeterminateProgressDialog(R.string.check_posting) { setCancelable(false) }
    }

    private val viewmodel by lazy { ViewModelProvider(this).get(FpCheckViewmodel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fp_check2)
        fpCheck2Toolbar.title = label
        setSupportActionBar(fpCheck2Toolbar)

        setAdd1()
        setAdd2()
        getFpFruits()
        setFpFruits()
        setBackDialog()
    }

    private fun setAdd1() {
        fruits1Dialog.setContentView(R.layout.dialog_fp_check2_1)
        fruits1Dialog.behavior.isHideable = false
        fruits1Dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        var believer = "---"
        var preacher = "---"
        var modified = false
        var idx = -1
        var beginModification = false

        val searchCallback = object : SearchCallback {
            override fun add(name: String) {
                fruits1Dialog.fpCheck2Dialog1Input2EditText.setText(name)
                fruits1Dialog.fpCheck2Dialog1Input3EditText.requestFocus()
                preacher = name
                val title = "${getString(R.string.fp_check_believer)}:  $believer  [$preacher]"
                fruits1Dialog.fpCheck2Dialog1TitleTextView.text = title
                fruits1Dialog.fpCheck2Dialog1DescriptionTextView.visibility = View.GONE
                fruits1Dialog.fpCheck2Dialog1RecyclerView.visibility = View.GONE
            }
        }
        val itemListForSearch = arrayListOf<Pair<String, String>>()
        val mAdapterForSearch = SearchAdapter(itemListForSearch, searchCallback)
        fruits1Dialog.fpCheck2Dialog1RecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        fruits1Dialog.fpCheck2Dialog1RecyclerView.adapter = mAdapterForSearch
        fruits1Dialog.fpCheck2Dialog1Input2EditText.addTextChangedListener {
            if (!beginModification) {
                preacher = "---"
                val title = "${getString(R.string.fp_check_believer)}:  $believer  [$preacher]"
                fruits1Dialog.fpCheck2Dialog1TitleTextView.text = title

                val query = it.toString().replace(" ", "")
                if (query == "") {
                    fruits1Dialog.fpCheck2Dialog1DescriptionTextView.visibility = View.GONE
                    fruits1Dialog.fpCheck2Dialog1RecyclerView.visibility = View.GONE
                } else {
                    val result = viewmodel.getSearchResult(
                        query,
                        "fp${type!!.replace("fp", "").toInt() - 1}",
                        true
                    )
                    if (result.isEmpty()) {
                        fruits1Dialog.fpCheck2Dialog1DescriptionTextView.text =
                            getString(R.string.fp_check_not_found)
                        fruits1Dialog.fpCheck2Dialog1DescriptionTextView.visibility = View.VISIBLE
                        fruits1Dialog.fpCheck2Dialog1RecyclerView.visibility = View.GONE
                    } else {
                        itemListForSearch.clear()
                        result.forEach { data ->
                            itemListForSearch.add(
                                Pair(
                                    data.group,
                                    data.name
                                )
                            )
                        }
                        fruits1Dialog.fpCheck2Dialog1DescriptionTextView.visibility = View.GONE
                        fruits1Dialog.fpCheck2Dialog1RecyclerView.visibility = View.VISIBLE
                        mAdapterForSearch.notifyDataSetChanged()
                    }
                }
            }
            beginModification = false
        }
        fruits1Dialog.fpCheck2Dialog1Input1EditText.addTextChangedListener {
            val query = it.toString()
            believer = if (query == "") "---" else query
            val title = "${getString(R.string.fp_check_believer)}:  $believer  [$preacher]"
            fruits1Dialog.fpCheck2Dialog1TitleTextView.text = title
        }

        fruits1Dialog.setOnShowListener {
            fruits1Dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            if (!modified) {
                believer = "---"
                preacher = "---"
                fruits1Dialog.fpCheck2Dialog1Input1EditText.setText("")
                fruits1Dialog.fpCheck2Dialog1Input2EditText.setText("")
                fruits1Dialog.fpCheck2Dialog1Input3EditText.setText("")
                fruits1Dialog.fpCheck2Dialog1Input4EditText.setText("")
                fruits1Dialog.fpCheck2Dialog1Input5EditText.setText("")
                fruits1Dialog.fpCheck2Dialog1Input6CheckBox.isChecked = false
            }
            val title = "${getString(R.string.fp_check_believer)}:  $believer  [$preacher]"
            fruits1Dialog.fpCheck2Dialog1TitleTextView.text = title
            fruits1Dialog.fpCheck2Dialog1Input1EditText.requestFocus()
        }
        fruits1Dialog.setOnDismissListener {
            modified = false
            idx = -1
            myToast?.cancel()
        }
        fruits1Dialog.fpCheck2Dialog1DoneTextView.setOnClickListener {
            when {
                believer == "---" -> {
                    myToast?.cancel()
                    myToast = longToast(R.string.fp_fruits_no_believer)
                }
                preacher == "---" -> {
                    myToast?.cancel()
                    myToast = longToast(R.string.fp_fruits_no_preacher)
                }
                believer.contains(",") -> {
                    myToast?.cancel()
                    myToast = longToast(R.string.fp_fruits_multiple)
                }
                else -> {
                    val data = DataBeliever(
                        preacher, believer,
                        fruits1Dialog.fpCheck2Dialog1Input3EditText.text.toString(),
                        fruits1Dialog.fpCheck2Dialog1Input4EditText.text.toString(),
                        fruits1Dialog.fpCheck2Dialog1Input5EditText.text.toString(),
                        if (fruits1Dialog.fpCheck2Dialog1Input6CheckBox.isChecked) "1" else ""
                    )
                    if (modified) {
                        itemList1[idx] = data
                        mAdapter1.notifyItemChanged(idx)
                    } else {
                        itemList1.add(data)
                        mAdapter1.notifyItemInserted(mAdapter1.itemCount)
                    }
                    fruits1Dialog.dismiss()
                }
            }
        }

        fpCheck2AddTextView1.setOnClickListener { fruits1Dialog.show() }

        val mFpFruitsCallback = object : FpFruitsCallback {
            override fun modifyBeliever(position: Int, data: DataBeliever) {
                modified = true
                idx = position
                beginModification = true
                believer = data.believer
                preacher = data.preacher
                fruits1Dialog.fpCheck2Dialog1Input1EditText.setText(believer)
                fruits1Dialog.fpCheck2Dialog1Input2EditText.setText(if (preacher == "---") "" else preacher)
                fruits1Dialog.fpCheck2Dialog1Input3EditText.setText(data.teacher)
                fruits1Dialog.fpCheck2Dialog1Input4EditText.setText(data.age)
                fruits1Dialog.fpCheck2Dialog1Input5EditText.setText(data.phone)
                fruits1Dialog.fpCheck2Dialog1Input6CheckBox.isChecked = data.remeet == "1"
                fruits1Dialog.fpCheck2Dialog1DescriptionTextView.visibility = View.GONE
                fruits1Dialog.fpCheck2Dialog1RecyclerView.visibility = View.GONE
                fruits1Dialog.show()
            }

            override fun modifyWordMovement(position: Int, data: DataWordMovement) {}
        }
        mAdapter1 = FpCheck2Adapter1(itemList1, mFpFruitsCallback)
        fpCheck2RecyclerView1.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        fpCheck2RecyclerView1.adapter = mAdapter1
    }

    private fun setAdd2() {
        fruits2Dialog.setContentView(R.layout.dialog_fp_check2_2)
        fruits2Dialog.behavior.isHideable = false
        fruits2Dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        var believer = "---"
        var teacher = "---"
        var modified = false
        var idx = -1
        var beginModification = false

        val searchCallback = object : SearchCallback {
            override fun add(name: String) {
                fruits2Dialog.fpCheck2Dialog2Input2EditText.setText(name)
                fruits2Dialog.fpCheck2Dialog2Input3EditText.requestFocus()
                teacher = name
                val title =
                    "${getString(R.string.fp_check_word_movement)}:  $believer  [$teacher]"
                fruits2Dialog.fpCheck2Dialog2TitleTextView.text = title
                fruits2Dialog.fpCheck2Dialog2DescriptionTextView.visibility = View.GONE
                fruits2Dialog.fpCheck2Dialog2RecyclerView.visibility = View.GONE
            }
        }
        val itemListForSearch = arrayListOf<Pair<String, String>>()
        val mAdapterForSearch = SearchAdapter(itemListForSearch, searchCallback)
        fruits2Dialog.fpCheck2Dialog2RecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        fruits2Dialog.fpCheck2Dialog2RecyclerView.adapter = mAdapterForSearch
        fruits2Dialog.fpCheck2Dialog2Input2EditText.addTextChangedListener {
            if (!beginModification) {
                teacher = "---"
                val title = "${getString(R.string.fp_check_word_movement)}:  $believer  [$teacher]"
                fruits2Dialog.fpCheck2Dialog2TitleTextView.text = title

                val query = it.toString().replace(" ", "")
                if (query == "") {
                    fruits2Dialog.fpCheck2Dialog2DescriptionTextView.visibility = View.GONE
                    fruits2Dialog.fpCheck2Dialog2RecyclerView.visibility = View.GONE
                } else {
                    val result = viewmodel.getSearchResult(
                        query,
                        "fp${type!!.replace("fp", "").toInt() - 1}",
                        true
                    )
                    if (result.isEmpty()) {
                        fruits2Dialog.fpCheck2Dialog2DescriptionTextView.text =
                            getString(R.string.fp_check_not_found)
                        fruits2Dialog.fpCheck2Dialog2DescriptionTextView.visibility = View.VISIBLE
                        fruits2Dialog.fpCheck2Dialog2RecyclerView.visibility = View.GONE
                    } else {
                        itemListForSearch.clear()
                        result.forEach { data ->
                            itemListForSearch.add(
                                Pair(
                                    data.group,
                                    data.name
                                )
                            )
                        }
                        fruits2Dialog.fpCheck2Dialog2DescriptionTextView.visibility = View.GONE
                        fruits2Dialog.fpCheck2Dialog2RecyclerView.visibility = View.VISIBLE
                        mAdapterForSearch.notifyDataSetChanged()
                    }
                }
            }
            beginModification = false
        }
        fruits2Dialog.fpCheck2Dialog2Input1EditText.addTextChangedListener {
            val query = it.toString()
            believer = if (query == "") "---" else query
            val title = "${getString(R.string.fp_check_word_movement)}:  $believer  [$teacher]"
            fruits2Dialog.fpCheck2Dialog2TitleTextView.text = title
        }

        fruits2Dialog.setOnShowListener {
            fruits2Dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            if (!modified) {
                believer = "---"
                teacher = "---"
                fruits2Dialog.fpCheck2Dialog2Input1EditText.setText("")
                fruits2Dialog.fpCheck2Dialog2Input2EditText.setText("")
                fruits2Dialog.fpCheck2Dialog2Input3EditText.setText("")
                fruits2Dialog.fpCheck2Dialog2Input4EditText.setText("")
            }
            val title = "${getString(R.string.fp_check_word_movement)}:  $believer  [$teacher]"
            fruits2Dialog.fpCheck2Dialog2TitleTextView.text = title
            fruits2Dialog.fpCheck2Dialog2Input1EditText.requestFocus()
        }
        fruits2Dialog.setOnDismissListener {
            modified = false
            idx = -1
            myToast?.cancel()
        }
        fruits2Dialog.fpCheck2Dialog2DoneTextView.setOnClickListener {
            when {
                believer == "---" -> {
                    myToast?.cancel()
                    myToast = longToast(R.string.fp_fruits_no_believer)
                }
                teacher == "---" -> {
                    myToast?.cancel()
                    myToast = longToast(R.string.fp_fruits_no_teacher)
                }
                believer.contains(",") -> {
                    myToast?.cancel()
                    myToast = longToast(R.string.fp_fruits_multiple)
                }
                else -> {
                    val data = DataWordMovement(
                        teacher, believer,
                        fruits2Dialog.fpCheck2Dialog2Input3EditText.text.toString(),
                        fruits2Dialog.fpCheck2Dialog2Input4EditText.text.toString()
                    )
                    if (modified) {
                        itemList2[idx] = data
                        mAdapter2.notifyItemChanged(idx)
                    } else {
                        itemList2.add(data)
                        mAdapter2.notifyItemInserted(mAdapter2.itemCount)
                        fpCheck2Layout.fullScroll(View.FOCUS_DOWN)
                    }
                    fruits2Dialog.dismiss()
                }
            }
        }

        fpCheck2AddTextView2.setOnClickListener { fruits2Dialog.show() }

        val mFpFruitsCallback = object : FpFruitsCallback {
            override fun modifyBeliever(position: Int, data: DataBeliever) {}

            override fun modifyWordMovement(position: Int, data: DataWordMovement) {
                modified = true
                idx = position
                beginModification = true
                believer = data.believer
                teacher = data.teacher
                fruits2Dialog.fpCheck2Dialog2Input1EditText.setText(believer)
                fruits2Dialog.fpCheck2Dialog2Input2EditText.setText(if (teacher == "---") "" else teacher)
                fruits2Dialog.fpCheck2Dialog2Input3EditText.setText(data.frequency)
                fruits2Dialog.fpCheck2Dialog2Input4EditText.setText(data.place)
                fruits2Dialog.fpCheck2Dialog2DescriptionTextView.visibility = View.GONE
                fruits2Dialog.fpCheck2Dialog2RecyclerView.visibility = View.GONE
                fruits2Dialog.show()
            }
        }
        mAdapter2 = FpCheck2Adapter2(itemList2, mFpFruitsCallback)
        fpCheck2RecyclerView2.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        fpCheck2RecyclerView2.adapter = mAdapter2
    }

    private fun getFpFruits() {
        val mCallbackGetAttendance = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                runOnUiThread {
                    viewmodel.setAll(client.fpAttendance)
                    val etcList = arrayListOf<ArrayList<String>>()
                    client.fpEtcAttendance.forEach {
                        val etcObject = arrayListOf<String>()
                        etcObject.add(it)
                        etcObject.add("기타")
                        etcObject.add("fp${type!!.replace("fp", "").toInt() - 1}")
                        etcObject.add("fp${type!!.replace("fp", "").toInt() - 1}")
                        etcObject.add("")
                        etcObject.add("2.0")
                        etcObject.add("기타")
                        etcList.add(etcObject)
                    }
                    viewmodel.setAll(etcList)
                    fpCheck2ProgressBar.visibility = View.GONE
                    fpCheck2Layout.visibility = View.VISIBLE
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        val mCallbackGetFpFruits = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                runOnUiThread {
                    itemList1.addAll(client.believer)
                    itemList2.addAll(client.wordMovement)
                    mAdapter1.notifyDataSetChanged()
                    mAdapter2.notifyDataSetChanged()
                    Thread {
                        client.getFpAttendance(
                            "fp${type!!.replace("fp", "").toInt() - 1}",
                            mCallbackGetAttendance
                        )
                    }.start()
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        fpCheck2ProgressBar.visibility = View.VISIBLE
        fpCheck2Layout.visibility = View.GONE
        Thread { client.getFpFruits(type, mCallbackGetFpFruits) }.start()
    }

    private fun setFpFruits() {
        val mCallbackSetFpFruits = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                finish()
                runOnUiThread {
                    myToast?.cancel()
                    myToast = toast(R.string.check_done)
                }
            }

            override fun onFailure() {
                client.closeSocket()
                progressDialog.dismiss()
                runOnUiThread {
                    myToast?.cancel()
                    myToast = longToast(R.string.check_failure)
                }
            }
        }

        alertDialog.setContentView(R.layout.dialog_alert1)
        alertDialog.alert1DoneTextView.text = getString(R.string.fp_check_done)
        alertDialog.behavior.skipCollapsed = true

        alertDialog.setOnShowListener {
            alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        alertDialog.alert1DoneTextView.setOnClickListener {
            alertDialog.dismiss()
        }

        postDialog.setContentView(R.layout.dialog_alert2)
        postDialog.alert2DescriptionTextView.text = getString(R.string.fp_fruits_post_description)
        postDialog.alert2CancelTextView.text = getString(R.string.post_cancel)
        postDialog.alert2DoneTextView.text = getString(R.string.post_done)
        postDialog.behavior.skipCollapsed = true

        postDialog.setOnShowListener {
            postDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        postDialog.alert2CancelTextView.setOnClickListener {
            postDialog.dismiss()
        }
        postDialog.alert2DoneTextView.setOnClickListener {
            progressDialog.show()
            Thread {
                client.setFpFruits(key, type, itemList1, itemList2, mCallbackSetFpFruits)
            }.start()
            postDialog.dismiss()
        }
    }

    private fun setBackDialog() {
        backDialog.setContentView(R.layout.dialog_alert2)
        backDialog.alert2DescriptionTextView.text = getString(R.string.back_description)
        backDialog.alert2CancelTextView.text = getString(R.string.back_cancel)
        backDialog.alert2DoneTextView.text = getString(R.string.back_done)
        backDialog.behavior.skipCollapsed = true

        backDialog.setOnShowListener {
            backDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        backDialog.alert2CancelTextView.setOnClickListener {
            backDialog.dismiss()
        }
        backDialog.alert2DoneTextView.setOnClickListener {
            backDialog.dismiss()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_fpcheck2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionFpCheck2 -> {
                var flag = false
                run {
                    itemList1.forEach {
                        if (it.preacher == "---") {
                            flag = true
                            val description =
                                "[${it.believer}]  ${getString(R.string.fp_fruits_reason1)}"
                            alertDialog.alert1DescriptionTextView.text = description
                            return@run
                        }
                    }
                    itemList2.forEach {
                        if (it.teacher == "---") {
                            flag = true
                            val description =
                                "[${it.believer}]  ${getString(R.string.fp_fruits_reason2)}"
                            alertDialog.alert1DescriptionTextView.text = description
                            return@run
                        }
                    }
                }
                if (flag) alertDialog.show() else postDialog.show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() = backDialog.show()

    override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
        progressDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.closeSocket()
    }
}