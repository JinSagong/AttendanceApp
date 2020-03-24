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
import com.jin.attendance_archive.adapter.FpCheck1Adapter1
import com.jin.attendance_archive.adapter.FpCheck1Adapter2
import com.jin.attendance_archive.adapter.SearchAdapter
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.client.ClientCallback
import com.jin.attendance_archive.model.DataFpCheck
import com.jin.attendance_archive.util.SearchCallback
import com.jin.attendance_archive.util.myToast
import com.jin.attendance_archive.viewmodel.FpCheckViewmodel
import kotlinx.android.synthetic.main.activity_fp_check1.*
import kotlinx.android.synthetic.main.dialog_alert1.*
import kotlinx.android.synthetic.main.dialog_alert2.*
import kotlinx.android.synthetic.main.dialog_fp_check1_add.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity

class FpCheck1Activity : AppCompatActivity() {
    private val client = Client()

    private val type by lazy { intent.getStringExtra("type") }
    private val label by lazy { intent.getStringExtra("label") }
    private val key by lazy { intent.getStringExtra("key") }

    private val itemList1 by lazy { arrayListOf<DataFpCheck>() }
    private val mAdapter1 by lazy { FpCheck1Adapter1(itemList1, this, viewmodel) }
    private val itemList2 by lazy { arrayListOf<String>() }
    private val mAdapter2 by lazy { FpCheck1Adapter2(itemList2, this) }
    private val itemList3 by lazy { arrayListOf<String>() }
    private val mAdapter3 by lazy { FpCheck1Adapter2(itemList3, this) }

    private val addDialog by lazy { BottomSheetDialog(this) }
    private val dupDialog by lazy { BottomSheetDialog(this) }
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
        setContentView(R.layout.activity_fp_check1)
        fpCheck1Toolbar.title = label
        setSupportActionBar(fpCheck1Toolbar)

        fpCheck1RecyclerView1.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        fpCheck1RecyclerView1.adapter = mAdapter1
        fpCheck1RecyclerView2.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        fpCheck1RecyclerView2.adapter = mAdapter2
        fpCheck1RecyclerView3.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        fpCheck1RecyclerView3.adapter = mAdapter3

        setAddEtc()
        setAddSearch()
        getFpAttendance()
        setFpAttendance()
        addAttendance()
        setBackDialog()
    }

    private fun setAddEtc() {
        var query = ""

        dupDialog.setContentView(R.layout.dialog_alert2)
        dupDialog.alert2DescriptionTextView.text = getString(R.string.dup_description)
        dupDialog.alert2CancelTextView.text = getString(R.string.dup_cancel)
        dupDialog.alert2DoneTextView.text = getString(R.string.dup_done)
        dupDialog.behavior.skipCollapsed = true

        dupDialog.setOnShowListener {
            dupDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dupDialog.alert2CancelTextView.setOnClickListener {
            dupDialog.dismiss()
        }
        dupDialog.alert2DoneTextView.setOnClickListener {
            itemList2.add(query)
            mAdapter2.notifyItemInserted(mAdapter2.itemCount)
            fpCheck1AddEtcEditText.setText("")
            dupDialog.dismiss()
        }

        fpCheck1AddEtcTextView.setOnClickListener {
            query = fpCheck1AddEtcEditText.text.toString()
            when (query) {
                "" -> {
                    myToast?.cancel()
                    myToast = longToast(R.string.hint_fp_check_input)
                }
                in itemList2 -> {
                    myToast?.cancel()
                    myToast = longToast("${getString(R.string.fp_check_dup)}  [$query]")
                }
                else -> {
                    if (viewmodel.isContained(query)) {
                        dupDialog.show()
                    } else {
                        itemList2.add(query)
                        mAdapter2.notifyItemInserted(mAdapter2.itemCount)
                        fpCheck1AddEtcEditText.setText("")
                    }
                }
            }
        }
    }

    private fun setAddSearch() {
        val searchDialog = BottomSheetDialog(this)
        searchDialog.setContentView(R.layout.dialog_fp_check1_add)
        searchDialog.behavior.isHideable = false
        searchDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val searchCallback = object : SearchCallback {
            override fun add(name: String) {
                if (name !in itemList3) {
                    searchDialog.dismiss()
                    itemList3.add(name)
                    mAdapter3.notifyItemInserted(mAdapter3.itemCount)
                    fpCheck1Layout.fullScroll(View.FOCUS_DOWN)
                } else {
                    myToast?.cancel()
                    myToast = longToast("${getString(R.string.fp_check_dup)}  [$name]")
                }
            }
        }
        val itemListForSearch = arrayListOf<Pair<String, String>>()
        val mAdapterForSearch = SearchAdapter(itemListForSearch, searchCallback)
        searchDialog.fpCheck1DialogRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        searchDialog.fpCheck1DialogRecyclerView.adapter = mAdapterForSearch
        searchDialog.fpCheck1DialogEditText.addTextChangedListener {
            val query = it.toString().replace(" ", "")
            if (query == "") {
                searchDialog.fpCheck1DialogDescriptionTextView.text =
                    getString(R.string.fp_check_do_search)
                searchDialog.fpCheck1DialogDescriptionTextView.visibility = View.VISIBLE
                searchDialog.fpCheck1DialogRecyclerView.visibility = View.GONE
            } else {
                val result = viewmodel.getSearchResult(query, type, false)
                if (result.isEmpty()) {
                    searchDialog.fpCheck1DialogDescriptionTextView.text =
                        getString(R.string.fp_check_not_found)
                    searchDialog.fpCheck1DialogDescriptionTextView.visibility = View.VISIBLE
                    searchDialog.fpCheck1DialogRecyclerView.visibility = View.GONE
                } else {
                    itemListForSearch.clear()
                    result.forEach { data -> itemListForSearch.add(Pair(data.group, data.name)) }
                    searchDialog.fpCheck1DialogDescriptionTextView.visibility = View.GONE
                    searchDialog.fpCheck1DialogRecyclerView.visibility = View.VISIBLE
                    mAdapterForSearch.notifyDataSetChanged()
                }
            }
        }

        searchDialog.setOnShowListener {
            searchDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            searchDialog.fpCheck1DialogEditText.setText("")
        }
        searchDialog.setOnDismissListener { myToast?.cancel() }

        fpCheck1AddTextView.setOnClickListener { searchDialog.show() }
    }

    private fun getFpAttendance() {
        val mCallbackGetAttendance = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                runOnUiThread {
                    viewmodel.setAll(client.fpAttendance)
                    itemList1.addAll(viewmodel.getSection(type))
                    itemList2.addAll(client.fpEtcAttendance)
                    itemList3.addAll(client.fpSearchAttendance)
                    mAdapter1.notifyDataSetChanged()
                    mAdapter2.notifyDataSetChanged()
                    mAdapter3.notifyDataSetChanged()
                    fpCheck1ProgressBar.visibility = View.GONE
                    fpCheck1Layout.visibility = View.VISIBLE
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        fpCheck1ProgressBar.visibility = View.VISIBLE
        fpCheck1Layout.visibility = View.GONE
        Thread { client.getFpAttendance(type, mCallbackGetAttendance) }.start()
    }

    private fun setFpAttendance() {
        val mCallbackSetStatus = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                finish()
                runOnUiThread {
                    myToast?.cancel()
                    myToast = longToast("$label${getString(R.string.fp_check_change_status)}")
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
        val mCallbackGetStatus = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                if (client.status[type!!.replace("fp", "").toInt()] == "TRUE") {
                    Thread { client.setStatus(type, mCallbackSetStatus) }.start()
                } else {
                    finish()
                    runOnUiThread {
                        myToast?.cancel()
                        myToast = longToast(R.string.check_done)
                    }
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
        val mCallbackSetAttendance = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                Thread { client.getStatus("fp", mCallbackGetStatus) }.start()
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
        postDialog.alert2DescriptionTextView.text = getString(R.string.post_description)
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
            val itemList1Temp = arrayListOf<ArrayList<String>>()
            itemList1.forEach { itemList1Temp.add(arrayListOf(it.name, it.content, it.check)) }
            Thread {
                client.setFpAttendance(
                    key, type, itemList1Temp, itemList2, itemList3,
                    mCallbackSetAttendance
                )
            }.start()
            postDialog.dismiss()
        }
    }

    private fun addAttendance() {
        addDialog.setContentView(R.layout.dialog_alert1)
        addDialog.alert1DescriptionTextView.text = getString(R.string.add_description)
        addDialog.alert1DoneTextView.text = getString(R.string.add_done)
        addDialog.behavior.skipCollapsed = true

        addDialog.setOnShowListener {
            addDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        addDialog.alert1DoneTextView.setOnClickListener {
            addDialog.dismiss()
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
        menuInflater.inflate(R.menu.menu_fpcheck1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionFpAdd -> addDialog.show().run { true }
            R.id.actionFpCheck1 -> {
                var flag = false
                run {
                    itemList1.forEach {
                        if (it.check == "1.0" && (it.content == "" || it.content == "NULL")) {
                            flag = true
                            val description = "[${it.name}]  ${getString(R.string.fp_check_reason)}"
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