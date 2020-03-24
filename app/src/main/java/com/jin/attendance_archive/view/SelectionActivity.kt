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
import com.jin.attendance_archive.adapter.MainAdapter
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.client.ClientCallback
import com.jin.attendance_archive.model.DataMainLabel

import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.dialog_alert1.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.startActivity

class SelectionActivity : AppCompatActivity() {
    private val client = Client()

    private var onLoading = false

    private val type by lazy { intent.getStringExtra("type")!! }
    private val label by lazy { intent.getStringExtra("label") }
    private val key by lazy { intent.getStringExtra("key") }
    private val name by lazy { intent.getStringExtra("name") }

    private val itemList by lazy { arrayListOf<DataMainLabel>() }
    private val mAdapter by lazy { MainAdapter(itemList, key, name, conditionDialog) }

    private lateinit var mCallbackGetList: ClientCallback

    private val conditionDialog by lazy { BottomSheetDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        listToolbar.title = label
        setSupportActionBar(listToolbar)

        setConditionDialog()

        listRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        listRecyclerView.adapter = mAdapter

        setLoadingFunction()
    }

    private fun setConditionDialog() {
        conditionDialog.setContentView(R.layout.dialog_alert1)
        conditionDialog.alert1DoneTextView.text = getString(R.string.condition_done)
        conditionDialog.behavior.skipCollapsed = true

        conditionDialog.setOnShowListener {
            conditionDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        conditionDialog.alert1DoneTextView.setOnClickListener { conditionDialog.dismiss() }
    }


    private fun setLoadingFunction() {
        mCallbackGetList = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                itemList.clear()
                val typeIdentifier = type.substring(1, 2)
                if (typeIdentifier == "#") {
                    val typeIdx = type.substring(0, 1)
                    val typeContent = type.substring(2)
                    client.listValue.forEachWithIndex { idx, item ->
                        itemList.add(
                            DataMainLabel(
                                item, client.status[idx], "$typeContent${idx + 1}#$typeIdx"
                            )
                        )
                    }
                } else {
                    client.listValue.forEachWithIndex { idx, item ->
                        itemList.add(DataMainLabel(item, client.status[idx], "$type${idx + 1}"))
                    }
                }
                runOnUiThread {
                    mAdapter.notifyDataSetChanged()
                    listProgressBar.visibility = View.GONE
                    listRecyclerView.visibility = View.VISIBLE
                    onLoading = false
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }
    }

    private fun loadData() {
        if (!onLoading) {
            onLoading = true
            listProgressBar.visibility = View.VISIBLE
            listRecyclerView.visibility = View.GONE
            Thread { client.getList(type, mCallbackGetList) }.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_selection, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionRefreshSelection -> loadData().run { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.closeSocket()
    }
}
