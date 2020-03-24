package com.jin.attendance_archive.util

import com.jin.attendance_archive.model.DataBeliever
import com.jin.attendance_archive.model.DataWordMovement

interface FpFruitsCallback {
    fun modifyBeliever(position: Int, data: DataBeliever)

    fun modifyWordMovement(position: Int, data: DataWordMovement)
}