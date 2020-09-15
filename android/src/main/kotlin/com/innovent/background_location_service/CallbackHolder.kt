package com.innovent.background_location_service

import android.location.Location
import android.util.Log

object CallbackHolder {
val TAG="CallbackHolder"
    val callbackList= mutableListOf<Long>()

    var location:Location?=null
    fun addCallback(callback:Long){
        callbackList.add(callback)
        Log.i(TAG,"Added a new TAG $callback")
    }

    fun removeCallback(callback:Long){
        callbackList.remove(callback)
        Log.i(TAG,"Removed TAG $callback")
    }
}