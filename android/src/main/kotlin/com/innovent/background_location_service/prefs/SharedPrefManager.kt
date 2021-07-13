package com.innovent.background_location_service.prefs

import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.innovent.background_location_service.models.AlarmData
import com.innovent.background_location_service.models.AlarmDataHolder
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class SharedPrefManager(private val preferences: SharedPreferences) {


    companion object {
        const val PREF_NAME = "SharedPrefManager"
        const val SCHEDULES_FIELD = "SCHEDULES_FIELD"
        const val PRIVATE_MODE = 0
    }

    fun storeAlarmDataHolder(alarmholder: AlarmDataHolder): Single<String> {
        print("Storing :"+alarmholder.toString())
        return Single.fromCallable<String> {
            preferences.edit().putString(SCHEDULES_FIELD, alarmholder.toString()).apply()
            return@fromCallable ""
        }
            .subscribeOn(Schedulers.io())
         
    }

    fun clearAlarmDataHolder(): Single<String> {
        return storeAlarmDataHolder(AlarmDataHolder(mutableListOf()))
    }

    fun storeAlarm(alarm: AlarmData)  {
        getAlarms().subscribe { it->
            println("Current alarms: "+it.toString())
            var newList:MutableList<AlarmData> = mutableListOf()

            it.list.forEach { e->
               if( e.alarmId== alarm.alarmId){
                   newList.remove(e)
               }
            }
            newList.add(alarm)
            println("Storing alarm : "+alarm.toString())
            storeAlarmDataHolder(AlarmDataHolder(newList)).subscribe{
                it->
                println("Storing worked: "+it.toString())
            }

        }

    }


    fun removeAlarmByID(alarm: Int)  {
        getAlarms().subscribe { it->
            var alarmToRemove:AlarmData?=null
            it.list.forEach {  it->
                if(it.alarmId==alarm){
                    alarmToRemove=it
                }
            }
           if(alarmToRemove!=null) {it.list.remove(alarmToRemove!!)}

            storeAlarmDataHolder(AlarmDataHolder(it.list)).subscribe()

        }

    }

    fun getAlarms(): Single<AlarmDataHolder> {
        return Single.fromCallable<AlarmDataHolder> {
            var data=preferences.getString(SCHEDULES_FIELD, "");
            println("Stored data//"+data)
            return@fromCallable Gson().fromJson(
                    data ,
                    AlarmDataHolder::class.java
            )
        }
            .subscribeOn(Schedulers.io()).onErrorReturn { it->
                    println("Errorn: "+it.toString())
                    AlarmDataHolder(mutableListOf()) }

    }



}