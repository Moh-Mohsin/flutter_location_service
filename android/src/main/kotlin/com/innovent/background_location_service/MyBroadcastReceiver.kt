package com.innovent.background_location_service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MyBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_TITLE = "MyBroadcastReceivernotificationTitle"
        const val NOTIFICATION_CONTENT = "MyBroadcastReceivernotificationContent"
    }


    val CHANNEL_ID = "ForegroundServiceChannel"
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Time Up... Now Vibrating !!!",
                Toast.LENGTH_LONG).show()

        startForegroundService(context,intent)
     /*   val vibrator = context
                .getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(2000)*/


    }


    private fun startForegroundService(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, ForegroundLocationService::class.java)

        serviceIntent.putExtra(ForegroundLocationService.CALLBACK_DISPATCHER, intent.getLongExtra(ForegroundLocationService.CALLBACK_DISPATCHER,0))
        serviceIntent.putExtra(ForegroundLocationService.PRIORITY, intent.getIntExtra(ForegroundLocationService.PRIORITY,0))
        serviceIntent.putExtra(ForegroundLocationService.FASTEST_INTERVAL_IN_MS, intent.getIntExtra(ForegroundLocationService.FASTEST_INTERVAL_IN_MS,0))
        serviceIntent.putExtra(ForegroundLocationService.LOCATION_INTERVAL_IN_MS, intent.getIntExtra(ForegroundLocationService.LOCATION_INTERVAL_IN_MS,0))
        serviceIntent.putExtra(ForegroundLocationService.MIN_CHANGE_DISTANCE_IN_METER, intent.getIntExtra(ForegroundLocationService.MIN_CHANGE_DISTANCE_IN_METER,0))
        serviceIntent.putExtra(ForegroundLocationService.NOTIFICATION_TITLE, intent.getStringExtra(ForegroundLocationService.NOTIFICATION_TITLE))
        serviceIntent.putExtra(ForegroundLocationService.NOTIFICATION_CONTENT, intent.getStringExtra(ForegroundLocationService.NOTIFICATION_CONTENT))
        context.startService(serviceIntent)
        createNotificationChannel(context,intent)
    }

    private fun buildNotification(context: Context,intent: Intent): Notification? {
        println("buildNotification")
        val notificationIntent = Intent(context, ForegroundLocationService::class.java)
        val pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0)

        val title = intent.getStringExtra(MyBroadcastReceiver.NOTIFICATION_TITLE)
        val content = intent.getStringExtra(MyBroadcastReceiver.NOTIFICATION_CONTENT)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build()
        println(content)
        return notification
    }

    private fun createNotificationChannel(context: Context,intent: Intent) {
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name ="Name"
            val descriptionText = "Dews"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            notificationManager.createNotificationChannel(channel)

        }


        with(NotificationManagerCompat.from(context)) {

            notify(2312312, buildNotification(context,intent)!!)
        }
    }
}