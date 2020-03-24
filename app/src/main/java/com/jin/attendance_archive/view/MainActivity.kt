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
import com.jin.attendance_archive.util.myToast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_alert1.*
import kotlinx.android.synthetic.main.dialog_alert2.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {
    private val client = Client()

    private var onLoading = false
    private var onHidden = false

    private val key by lazy { intent.getStringExtra("key") }
    private val name by lazy { intent.getStringExtra("name") }

    private val itemList by lazy { arrayListOf<DataMainLabel>() }
    private val mAdapter by lazy { MainAdapter(itemList, key, name, conditionDialog) }

    private lateinit var mCallbackGetList: ClientCallback

    private val conditionDialog by lazy { BottomSheetDialog(this) }
    private val logoutDialog by lazy { BottomSheetDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)

        setConditionDialog()

        mainRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mainRecyclerView.adapter = mAdapter

        mainInfoTextView.text = getInfo()

        setLoadingFunction()
        setLogoutFunction()
        setHiddenFunction()
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

    private fun changeData() {
        itemList.clear()
        itemList.add(DataMainLabel("출석체크", "NULL", "NULL"))
        itemList.add(DataMainLabel("일반남여전도회", client.status[0], "general"))
        itemList.add(DataMainLabel("기관", client.status[1], "group"))
        itemList.add(DataMainLabel("지교회", client.status[2], "bc"))
        itemList.add(DataMainLabel("현장전도", client.status[3], "fp"))
        itemList.add(DataMainLabel("[온라인] 주일1부예배", "NULL", "home1"))
        itemList.add(DataMainLabel("[주일1부예배] 일반남여전도회", client.status[4], "1#general"))
        itemList.add(DataMainLabel("[주일1부예배] 기관", client.status[5], "1#group"))
        itemList.add(DataMainLabel("[주일1부예배] 지교회", client.status[6], "1#bc"))
        itemList.add(DataMainLabel("[온라인] 주일2부예배", "NULL", "home2"))
        itemList.add(DataMainLabel("[주일2부예배] 일반남여전도회", client.status[7], "2#general"))
        itemList.add(DataMainLabel("[주일2부예배] 기관", client.status[8], "2#group"))
        itemList.add(DataMainLabel("[주일2부예배] 지교회", client.status[9], "2#bc"))
        itemList.add(DataMainLabel("[온라인] 주일오후예배", "NULL", "home3"))
        itemList.add(DataMainLabel("[주일오후예배] 일반남여전도회", client.status[10], "3#general"))
        itemList.add(DataMainLabel("[주일오후예배] 기관", client.status[11], "3#group"))
        itemList.add(DataMainLabel("[주일오후예배] 지교회", client.status[12], "3#bc"))
        itemList.add(DataMainLabel("[온라인] 수요예배", "NULL", "home4"))
        itemList.add(DataMainLabel("[수요예배] 일반남여전도회", client.status[13], "4#general"))
        itemList.add(DataMainLabel("[수요예배] 기관", client.status[14], "4#group"))
        itemList.add(DataMainLabel("[수요예배] 지교회", client.status[15], "4#bc"))
        itemList.add(DataMainLabel("[온라인] 금요기도회", "NULL", "home5"))
        itemList.add(DataMainLabel("[금요기도회] 일반남여전도회", client.status[16], "5#general"))
        itemList.add(DataMainLabel("[금요기도회] 기관", client.status[17], "5#group"))
        itemList.add(DataMainLabel("[금요기도회] 지교회", client.status[18], "5#bc"))
        runOnUiThread { mAdapter.notifyDataSetChanged() }
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
                changeData()
                onLoading = false
                runOnUiThread {
                    mainProgressBar.visibility = View.GONE
                    mainRecyclerView.visibility = View.VISIBLE
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }
    }

    private fun setLogoutFunction() {
        val mCallbackLogout = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                defaultSharedPreferences.edit().putString("key", "").apply()
                defaultSharedPreferences.edit().putString("name", "").apply()
                myToast?.cancel()
                startActivity<LoginActivity>()
                finish()
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        logoutDialog.setContentView(R.layout.dialog_alert2)
        logoutDialog.alert2DescriptionTextView.text = getString(R.string.logout_description)
        logoutDialog.alert2CancelTextView.text = getString(R.string.logout_cancel)
        logoutDialog.alert2DoneTextView.text = getString(R.string.logout_done)
        logoutDialog.behavior.skipCollapsed = true

        logoutDialog.setOnShowListener {
            logoutDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        logoutDialog.alert2CancelTextView.setOnClickListener { logoutDialog.dismiss() }
        logoutDialog.alert2DoneTextView.setOnClickListener {
            Thread { client.logout(key, mCallbackLogout) }.start()
            logoutDialog.dismiss()
        }
    }

    private fun setHiddenFunction() {
        if (key == "jin") mainInfoTextView.setOnClickListener {
            if (!onHidden) {
                onHidden = true
                startActivity<LogsActivity>()
            }
        }
    }

    private fun loadData() {
        if (!onLoading) {
            onLoading = true
            mainProgressBar.visibility = View.VISIBLE
            mainRecyclerView.visibility = View.GONE
            Thread { client.getStatus("main", mCallbackGetList) }.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionLogout -> logoutDialog.show().run { true }
            R.id.actionRefreshMain -> loadData().run { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        onHidden = false
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.closeSocket()
    }
}