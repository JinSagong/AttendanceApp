package com.jin.attendance_archive.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.attendance_archive.R
import com.jin.attendance_archive.adapter.CheckAdapter
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.client.ClientCallback
import com.jin.attendance_archive.model.DataCheck
import com.jin.attendance_archive.util.myToast

import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.dialog_alert1.*
import kotlinx.android.synthetic.main.dialog_alert2.*
import org.jetbrains.anko.*

class CheckActivity : AppCompatActivity() {
    private val client = Client()

    private val type by lazy { intent.getStringExtra("type") }
    private val label by lazy { intent.getStringExtra("label") }
    private val key by lazy { intent.getStringExtra("key") }

    private val itemList by lazy { arrayListOf<DataCheck>() }
    private val mAdapter by lazy { CheckAdapter(itemList) }

    private val addDialog by lazy { BottomSheetDialog(this) }
    private val postDialog by lazy { BottomSheetDialog(this) }
    private val backDialog by lazy { BottomSheetDialog(this) }
    private val progressDialog by lazy {
        indeterminateProgressDialog(R.string.check_posting) { setCancelable(false) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        listToolbar.title = label
        setSupportActionBar(listToolbar)

        listRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        listRecyclerView.adapter = mAdapter

        getAttendance()
        setAttendance()
        addAttendance()
        setBackDialog()
    }

    private fun getAttendance() {
        val mCallbackGetAttendance = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                client.attendance.forEach {
                    itemList.add(DataCheck(it[0], it[1], it[2]))
                }
                runOnUiThread {
                    mAdapter.notifyDataSetChanged()
                    listProgressBar.visibility = View.GONE
                    listRecyclerView.visibility = View.VISIBLE
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        listProgressBar.visibility = View.VISIBLE
        listRecyclerView.visibility = View.GONE
        Thread { client.getAttendance(type, mCallbackGetAttendance) }.start()
    }

    private fun setAttendance() {
        val mCallbackSetAttendance = object : ClientCallback {
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
            Thread {
                client.setAttendance(key, type, mAdapter.getResult(), mCallbackSetAttendance)
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
        if (key == "jin" || key == "supervisor") menuInflater.inflate(R.menu.menu_check1, menu)
        else menuInflater.inflate(R.menu.menu_check2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionAdd -> addDialog.show().run { true }
            R.id.actionCheck1 -> postDialog.show().run { true }
            R.id.actionCheck2 -> postDialog.show().run { true }
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