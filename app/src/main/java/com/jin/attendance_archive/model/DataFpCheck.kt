package com.jin.attendance_archive.model

import io.realm.RealmObject

open class DataFpCheck(
    var name: String = "",
    var group: String = "",
    var type: String = "",
    var checkby: String = "",
    var content: String = "",
    var check: String = "",
    var category: String = ""
) : RealmObject()