package com.jin.attendance_archive.client

import android.util.Log
import com.jin.attendance_archive.model.DataBeliever
import com.jin.attendance_archive.model.DataCheck
import com.jin.attendance_archive.model.DataWordMovement
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.*

class Client {
    @Suppress("JAVA_CLASS_ON_COMPANION")
    companion object {
        private const val PORT = 8888
        private val socketAddress = arrayOf(
            "***.***.***.***", // IP_CHURCH_EXTERNAL
            "***.***.***.***", // IP_CHURCH_INTERNAL
            "***.***.***.***", // IP_CHURCH2_EXTERNAL
            "***.***.***.***", // 집 외부 IP
            "***.***.***.***", // 집 노트북 IP
            "***.***.***.***" // 집 컴퓨터 IP
        )
        private lateinit var address: String

        var offline = true
        var online = true
    }

    private lateinit var socket: Socket
    private lateinit var outputWriter: PrintWriter
    private lateinit var inputReader: BufferedReader

    private var isConnected = false
    private var isOpen = true

    val people = arrayListOf<String>()
    val status = arrayListOf<String>()
    val listValue = arrayListOf<String>()
    var logs = ""
    val attendance = arrayListOf<ArrayList<String>>()
    val fpAttendance = arrayListOf<ArrayList<String>>()
    val fpEtcAttendance = arrayListOf<String>()
    val fpSearchAttendance = arrayListOf<String>()
    val believer = arrayListOf<DataBeliever>()
    val wordMovement = arrayListOf<DataWordMovement>()

    fun openSocket(mCallBack: ClientCallback) {
        isOpen = true
        var count = 0
        socketAddress.forEach {
            Thread {
                val tempSocket = Socket()
                try {
                    tempSocket.connect(InetSocketAddress(it, PORT), 2000)
                    if (tempSocket.isConnected) {
                        Log.d(this.javaClass.name, "Connect [$it]")
                        isConnected = true
                        address = it
                        socket = tempSocket
                        outputWriter = PrintWriter(socket.getOutputStream())
                        inputReader = BufferedReader(
                            InputStreamReader(socket.getInputStream(), "EUC-KR")
                        )
                        outputWriter.println("getOnOff")
                        outputWriter.flush()
                        offline = inputReader.readLine()?.toBoolean() ?: true
                        online = inputReader.readLine()?.toBoolean() ?: true
                        if (isOpen) mCallBack.onSuccess()
                    }
                } catch (e: Exception) {
                    // ConnectException: 같은 네트워크 상에서 공용 IP로 접근하면 발생
                    // SocketTimeException: 소켓 연결 타임아웃
                    synchronized(true) {
                        Log.d(this.javaClass.name, "${e.javaClass.name} [$it]")
                        count++
                        tempSocket.close()
                        if (count == socketAddress.size) if (isOpen) mCallBack.onFailure()
                    }
                }
            }.start()
        }
    }

    private fun getSocket() {
        isOpen = true
        socket = Socket()
        socket.connect(InetSocketAddress(address, PORT), 2000)
        outputWriter = PrintWriter(socket.getOutputStream())
        inputReader = BufferedReader(
            InputStreamReader(socket.getInputStream(), "EUC-KR")
        )
        isConnected = true
    }

    fun closeSocket() {
        isOpen = false
        if (isConnected) {
            isConnected = false
            outputWriter.close()
            inputReader.close()
            socket.close()
        }
    }

    fun autoLogin(key: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("AutoLogIn")
            outputWriter.println(key)
            outputWriter.flush()
            inputReader.readLine()
            if (isOpen) mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun login(id: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("LogIn")
            outputWriter.println(id)
            outputWriter.flush()
            val num = inputReader.readLine().toInt()
            people.clear()
            for (i in 0 until num) people.add(inputReader.readLine())
            if (isOpen) mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun logout(key: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("LogOut")
            outputWriter.println(key)
            outputWriter.flush()
            inputReader.readLine()
            if (isOpen) mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun getStatus(type: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("getStatus")
            outputWriter.println(type)
            outputWriter.flush()
            val num = inputReader.readLine().toInt()
            status.clear()
            for (i in 0 until num) status.add(inputReader.readLine())
            if (isOpen) mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun getList(type: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("getList")
            outputWriter.println(type)
            outputWriter.flush()
            val num = inputReader.readLine().toInt()
            listValue.clear()
            status.clear()
            for (i in 0 until num) {
                listValue.add(inputReader.readLine())
                status.add(inputReader.readLine())
            }
            if (isOpen) mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun getLogs(mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("getLogs")
            outputWriter.flush()
            val num = inputReader.readLine().toInt()
            logs = ""
            for (i in 0 until num) {
                logs += "${inputReader.readLine()}\n"
            }
            if (isOpen) mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun getAttendance(type: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("getAttendance")
            outputWriter.println(type)
            outputWriter.flush()
            val num = inputReader.readLine().toInt()
            attendance.clear()
            for (i in 0 until num) {
                attendance.add(arrayListOf<String>().apply {
                    add(inputReader.readLine())
                    add(inputReader.readLine())
                    add(inputReader.readLine())
                })
            }
            if (isOpen) mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun setAttendance(
        key: String,
        type: String,
        attendance: ArrayList<DataCheck>,
        mCallBack: ClientCallback
    ) {
        try {
            getSocket()
            outputWriter.println("setAttendance")
            outputWriter.println(key)
            outputWriter.flush()
            outputWriter.println(type)
            outputWriter.flush()
            outputWriter.println(attendance.size)
            outputWriter.flush()
            attendance.forEach {
                outputWriter.println(it.name)
                outputWriter.println(it.check)
                outputWriter.println(it.reason)
            }
            outputWriter.flush()
            inputReader.readLine()
            mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun getFpAttendance(type: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("getFpAttendance")
            outputWriter.println(type)
            outputWriter.flush()
            var num = inputReader.readLine().toInt()
            fpAttendance.clear()
            for (i in 0 until num) {
                fpAttendance.add(arrayListOf<String>().apply {
                    for (j in 0 until 7) add(inputReader.readLine())
                })
            }
            num = inputReader.readLine().toInt()
            fpEtcAttendance.clear()
            for (i in 0 until num) fpEtcAttendance.add(inputReader.readLine())
            num = inputReader.readLine().toInt()
            fpSearchAttendance.clear()
            for (i in 0 until num) fpSearchAttendance.add(inputReader.readLine())
            mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun setFpAttendance(
        key: String,
        type: String,
        list: ArrayList<ArrayList<String>>,
        list_etc: ArrayList<String>,
        list_search: ArrayList<String>,
        mCallBack: ClientCallback
    ) {
        try {
            getSocket()
            outputWriter.println("setFpAttendance")
            outputWriter.println(key)
            outputWriter.flush()
            outputWriter.println(type)
            outputWriter.flush()
            outputWriter.println(list.size)
            outputWriter.flush()
            list.forEach {
                outputWriter.println(it[0])
                outputWriter.println(it[1])
                outputWriter.println(
                    when (it[2]) {
                        "0.0" -> 0
                        "1.0" -> 1
                        else -> 2
                    }
                )
            }
            outputWriter.flush()
            outputWriter.println(list_etc.size)
            outputWriter.flush()
            list_etc.forEach { outputWriter.println(it) }
            outputWriter.flush()
            outputWriter.println(list_search.size)
            outputWriter.flush()
            list_search.forEach { outputWriter.println(it) }
            outputWriter.flush()
            inputReader.readLine()
            mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun getFpFruits(type: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("getFpFruits")
            outputWriter.println(type)
            outputWriter.flush()
            var num = inputReader.readLine().toInt()
            believer.clear()
            for (i in 0 until num) {
                believer.add(
                    DataBeliever(
                        inputReader.readLine(),
                        inputReader.readLine(),
                        inputReader.readLine(),
                        inputReader.readLine(),
                        inputReader.readLine(),
                        inputReader.readLine()
                    )
                )
            }
            num = inputReader.readLine().toInt()
            wordMovement.clear()
            for (i in 0 until num) {
                wordMovement.add(
                    DataWordMovement(
                        inputReader.readLine(),
                        inputReader.readLine(),
                        inputReader.readLine(),
                        inputReader.readLine()
                    )
                )
            }
            mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun setFpFruits(
        key: String,
        type: String,
        listBeliever: ArrayList<DataBeliever>,
        listWordMovement: ArrayList<DataWordMovement>,
        mCallBack: ClientCallback
    ) {
        try {
            getSocket()
            outputWriter.println("setFpFruits")
            outputWriter.println(key)
            outputWriter.flush()
            outputWriter.println(type)
            outputWriter.flush()
            outputWriter.println(listBeliever.size)
            outputWriter.flush()
            listBeliever.forEach {
                outputWriter.println(it.preacher)
                outputWriter.println(it.believer)
                outputWriter.println(it.teacher)
                outputWriter.println(it.age)
                outputWriter.println(it.phone)
                outputWriter.println(it.remeet)
            }
            outputWriter.flush()
            outputWriter.println(listWordMovement.size)
            outputWriter.flush()
            listWordMovement.forEach {
                outputWriter.println(it.teacher)
                outputWriter.println(it.believer)
                outputWriter.println(it.frequency)
                outputWriter.println(it.place)
            }
            outputWriter.flush()
            inputReader.readLine()
            mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }

    fun setStatus(type: String, mCallBack: ClientCallback) {
        try {
            getSocket()
            outputWriter.println("setStatus")
            outputWriter.println(type)
            outputWriter.flush()
            inputReader.readLine()
            mCallBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isOpen) mCallBack.onFailure()
        }
    }
}