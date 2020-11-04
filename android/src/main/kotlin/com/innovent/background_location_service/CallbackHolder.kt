package com.innovent.background_location_service

import android.location.Location
import android.util.Log

object CallbackHolder {
    var enableToasts: Boolean = false
    val TAG = "CallbackHolder"
    val callbackList = mutableListOf<Long>()
    var isServiceRunning = false
    var location: Location? = null

    var optionalPayload:String=""
    fun addCallback(callback: Long) {
        callbackList.add(callback)
        Log.i(TAG, "Added a new callback with id: $callback")
    }

    fun removeCallback(callback: Long) {
        callbackList.remove(callback)
        Log.i(TAG, "Removed callback with id: $callback")
    }

    fun dispose() {
        callbackList.clear()
        location = null
        isServiceRunning = false
    }
}