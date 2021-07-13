package com.innovent.background_location_service.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.innovent.background_location_service.ForegroundLocationService
import com.innovent.background_location_service.MyBroadcastReceiver
import com.innovent.background_location_service.prefs.SharedPrefManager
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat


class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var sharedprefs= context!! .getSharedPreferences(SharedPrefManager.PREF_NAME, SharedPrefManager.PRIVATE_MODE)
        SharedPrefManager(sharedprefs).getAlarms().observeOn(AndroidSchedulers.mainThread()).subscribe { it->

            it.list.forEach { item->

                val intent = Intent(context, MyBroadcastReceiver::class.java)
                intent.putExtra(ForegroundLocationService.CALLBACK_DISPATCHER, item.callbackDispatcher)
                intent.putExtra(ForegroundLocationService.PRIORITY, item.priority)
                intent.putExtra(ForegroundLocationService.FASTEST_INTERVAL_IN_MS, item.fastestIntervalMs)
                intent.putExtra(ForegroundLocationService.LOCATION_INTERVAL_IN_MS, item.locationIntervalMs)
                intent.putExtra(ForegroundLocationService.MIN_CHANGE_DISTANCE_IN_METER, item.minChangeDistanceInMeters)
                intent.putExtra(ForegroundLocationService.NOTIFICATION_TITLE, item.notificationTitle)
                intent.putExtra(ForegroundLocationService.NOTIFICATION_CONTENT, item.notificationContent)
                intent.putExtra(MyBroadcastReceiver.NOTIFICATION_TITLE, item.broadCastNotificationTitle)
                intent.putExtra(MyBroadcastReceiver.NOTIFICATION_CONTENT, item.broadCastNotificationContent)
                startAlertAtParticularTime(item.alarmId,intent,item.dateTime,item.repeatEveryMs,context)

            }


        }

    }

    fun startAlertAtParticularTime(id: Int, intent: Intent, dateTime: String, repeatEveryMs: Int,mContext:Context) {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime)

        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.setInexactRepeating(AlarmManager.RTC_WAKEUP, date.time,
                repeatEveryMs.toLong(),  getPendingIntent(id,intent,mContext))
    }


    fun getPendingIntent(id: Int, intent: Intent,mContext:Context): PendingIntent {
        return PendingIntent.getBroadcast(
                mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}