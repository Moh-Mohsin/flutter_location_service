package com.innovent.background_location_service.models

import com.google.gson.Gson


data class AlarmDataHolder(var list:MutableList<AlarmData>){
    override fun toString(): String {
        return Gson().toJson(this).toString()
    }
}


data class AlarmData(val alarmId :Int,var priority:Int,val fastestIntervalMs :Int,val locationIntervalMs :Int,val minChangeDistanceInMeters :Int,
                           val notificationTitle :String, val notificationContent :String,val enableToasts :Boolean,val dateTime :String,val callbackDispatcher :Long,val broadCastNotificationTitle :String,val broadCastNotificationContent :String,val repeatEveryMs :Int){

    companion object{
      fun fromGson(  data:String):AlarmData {
          return  Gson().fromJson(data,AlarmData::class.java)
      }
    }
    override fun toString(): String {
        return Gson().toJson(this).toString()
    }
}

