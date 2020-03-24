package com.jin.attendance_archive.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jin.attendance_archive.R

class NotFoundActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_found)
    }

    override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
    }
}