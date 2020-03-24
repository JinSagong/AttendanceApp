package com.jin.attendance_archive.viewmodel

import androidx.lifecycle.ViewModel
import com.jin.attendance_archive.model.DataFpCheck
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where

class FpCheckViewmodel : ViewModel() {
    private val mRealm = Realm.getDefaultInstance()

    init { mRealm.executeTransaction { it.deleteAll() } }

    fun getSection(type: String) = mRealm.where<DataFpCheck>().equalTo("type", type).findAll()!!

    fun getSearchResult(query: String, type: String, included: Boolean): List<DataFpCheck> {
        val baseList = if (included) mRealm.where<DataFpCheck>()
            .equalTo("checkby", type).equalTo("check", "2.0").sort("name").findAll()
        else mRealm.where<DataFpCheck>().notEqualTo("type", type).sort("name").findAll()
        val list1 = baseList.where().equalTo("name", query).findAll()
        val list2 = baseList.where().beginsWith("name", query).notEqualTo("name", query).findAll()
        val list3 = baseList.where().contains("name", query).not().beginsWith("name", query)
            .notEqualTo("name", query).findAll()

        return list1 + list2 + list3
    }

    fun update(obj: DataFpCheck, column: String, value: String) = mRealm.executeTransaction {
        when (column) {
            "content" -> obj.content = value
            "check" -> obj.check = value
        }
    }

    fun isContained(query: String) = mRealm.where<DataFpCheck>().equalTo("name", query).findAll().isNotEmpty()

    fun setAll(list: ArrayList<ArrayList<String>>) = mRealm.executeTransaction { realm ->
        list.forEach {
            val newObject = realm.createObject<DataFpCheck>()
            newObject.name = it[0]
            newObject.group = it[1]
            newObject.type = it[2]
            newObject.checkby = it[3]
            newObject.content = it[4]
            newObject.check = it[5]
            newObject.category = it[6]
        }
    }

    override fun onCleared() {
        mRealm.close()
        super.onCleared()
    }
}