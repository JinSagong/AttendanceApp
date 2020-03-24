package com.jin.attendance_archive.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jin.attendance_archive.R
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.client.ClientCallback
import com.jin.attendance_archive.util.myToast
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {
    private val client = Client()

    private var onButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val mCallback = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                runOnUiThread {
                    loginProgressBar.visibility = View.INVISIBLE
                }
                if (client.people.firstOrNull() == "FALSE") {
                    runOnUiThread {
                        myToast?.cancel()
                        myToast = toast(R.string.login_wrong)
                        onButton = false
                    }
                } else {
                    val key = client.people[0]
                    val name = client.people[1]
                    defaultSharedPreferences.edit().putString("key", key).apply()
                    defaultSharedPreferences.edit().putString("name", name).apply()
                    myToast?.cancel()
                    startActivity<MainActivity>(
                        "key" to key, "name" to name
                    )
                    finish()
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        loginButtonImageView.setOnClickListener {
            if (loginEditText.text.toString() == "") {
                myToast?.cancel()
                myToast = toast(R.string.hint_login)
            } else if (!onButton) {
                onButton = true
                loginProgressBar.visibility = View.VISIBLE
                Thread { client.login(loginEditText.text.toString(), mCallback) }.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.closeSocket()
    }
}