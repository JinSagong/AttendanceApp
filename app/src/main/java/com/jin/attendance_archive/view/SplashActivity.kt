package com.jin.attendance_archive.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.jin.attendance_archive.R
import com.jin.attendance_archive.client.Client
import com.jin.attendance_archive.client.ClientCallback
import com.jin.attendance_archive.util.myToast
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SplashActivity : AppCompatActivity() {
    private val client = Client()

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val listener by lazy {
        InstallStateUpdatedListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) popupSnackbarForCompleteUpdate()
        }
    }
    private val myRequestCode = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Thread { inAppUpdate() }.start()
    }

    private fun inAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UNKNOWN || it.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE)
                loadingApp()
            else if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) appUpdateManager.startUpdateFlowForResult(
                it, AppUpdateType.IMMEDIATE, this, myRequestCode
            )
        }
        appUpdateManager.registerListener(listener)
    }

    private fun popupSnackbarForCompleteUpdate() = Snackbar.make(
        splashLayout, getString(R.string.update_download), Snackbar.LENGTH_INDEFINITE
    ).apply {
        setAction(getString(R.string.update_install)) { appUpdateManager.completeUpdate() }
        show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == myRequestCode && resultCode != RESULT_OK) {
            myToast?.cancel()
            myToast = toast(R.string.update_cancel)
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            } else if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    it, AppUpdateType.IMMEDIATE, this, myRequestCode
                )
            }
        }
    }

    private fun loadingApp() {
        val key = defaultSharedPreferences.getString("key", "")
        val name = defaultSharedPreferences.getString("name", "")

        val mCallbackAutoLogin = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                runOnUiThread {
                    myToast?.cancel()
                    myToast = toast(R.string.auto_login)
                }
                startActivity<MainActivity>("key" to key, "name" to name)
                finish()
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        val mCallbackIsAlive = object : ClientCallback {
            override fun onSuccess() {
                client.closeSocket()
                if (key == "" && name == "") {
                    startActivity<LoginActivity>()
                    finish()
                } else {
                    client.autoLogin(key!!, mCallbackAutoLogin)
                }
            }

            override fun onFailure() {
                client.closeSocket()
                startActivity<NotFoundActivity>()
                finish()
            }
        }

        client.openSocket(mCallbackIsAlive)
    }

    override fun onDestroy() {
        super.onDestroy()
        client.closeSocket()
        appUpdateManager.unregisterListener(listener)
    }
}