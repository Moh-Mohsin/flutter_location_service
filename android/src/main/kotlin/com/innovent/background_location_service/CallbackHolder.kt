package com.innovent.background_location_service

import android.location.Location
import android.util.Log

object CallbackHolder {
    var enableToasts: Boolean = false
    val TAG = "CallbackHolder"

    val callbackList = mutableMapOf<String, CallBackData>()
    var isServiceRunning = false
    var location: Location? = null

    fun addCallback(data: CallBackData) {
        callbackList.put(data.id, data)
        Log.i(TAG, "Added a new callback with id: $data")
    }

    fun removeCallback(callback: String) {
        callbackList.remove(callback)
        Log.i(TAG, "Removed callback with id: $callback")
    }

    fun dispose() {
        callbackList.clear()
        location = null
        isServiceRunning = false
    }
}

data class CallBackData(val dartId: Long, val id: String, val optionalPayload: String)