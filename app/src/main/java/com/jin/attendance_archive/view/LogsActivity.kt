package com.jin.attendance_archive.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jin.attendance_archive.R
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.client.ClientCallback

import kotlinx.android.synthetic.main.activity_logs.*
import org.jetbrains.anko.startActivity

class LogsActivity : AppCompatActivity() {
    val client = Client()

    var onLoading = false

    private lateinit var mCallbackLogs: ClientCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        setSupportActionBar(logsToolbar)

        mCallbackLogs = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                runOnUiThread {
                    logsTextView.text = client.logs
                    logsProgressBar.visibility = View.GONE
                    logsScrollView.visibility = View.VISIBLE
                    onLoading = false
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        loadLogs()
    }

    private fun loadLogs() {
        if (!onLoading) {
            onLoading = true
            logsProgressBar.visibility = View.VISIBLE
            logsScrollView.visibility = View.GONE
            Thread { client.getLogs(mCallbackLogs) }.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_logs, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionRefreshLogs -> loadLogs().run { true }
            else -> super.onOptionsItemSelected(item)
        }
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