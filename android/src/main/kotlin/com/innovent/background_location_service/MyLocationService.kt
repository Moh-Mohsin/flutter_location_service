package com.innovent.background_location_service

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import java.util.*


class MyLocationService : Service(), MethodChannel.MethodCallHandler {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mBackgroundChannel: MethodChannel
    private lateinit var mContext: Context
    private lateinit var mLocation: Location
    val CHANNEL_ID = "ForegroundServiceChannel"

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                updateLocation(location)
            }
        }
    }

    var mLocationRequest: LocationRequest?=null

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mLocationRequest = LocationRequest.create().apply {
            interval = mInterval.toLong()
            smallestDisplacement = mSmallestDisplacement.toFloat()
            fastestInterval = mFastestInterval.toLong()
            priority = mPriority
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                null
        )
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(baseContext)
        createNotificationChannel()

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.also {
                        updateLocation(location)
                    }
                }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val notificationIntent = Intent(this, MyLocationService::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)

        val title = intent.getStringExtra("notificationTitle")
        val content = intent.getStringExtra("notificationContent")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)

        callbackDispatcherId = intent.getLongExtra("callbackDispatcher", 0)
        mPriority = intent.getIntExtra("priority", LocationRequest.PRIORITY_HIGH_ACCURACY)
        mFastestInterval = intent.getIntExtra("fastestIntervalMs", 0)
        mInterval = intent.getIntExtra("locationIntervalMs", 0)
        mSmallestDisplacement = intent.getIntExtra("minChangeDistanceInMeters", 0)
        // callbackId = intent.getLongExtra("callback", 0)

        //do heavy work on a background thread
        //stopSelf();
        // invokeCallback()
        startLocationService(this, callbackDispatcherId)

        return START_NOT_STICKY
    }

    private var callbackDispatcherId: Long = 0
    private var callbackId: Long = 0

    private var mInterval: Int = 0
    private var mSmallestDisplacement: Int = 0
    private var mFastestInterval: Int = 0
    private var mPriority: Int = 0


    private fun updateLocation(location: Location) {
        val msg = "location is ${location.latitude} ${location.longitude}"
        mLocation = location
        CallbackHolder.location = location
        Log.d(TAG, msg)
        CallbackHolder.callbackList.forEach { it ->
            invokeCallback(location, it)
        }
        // invokeCallback(location,callbackId)
        //    ChannelHolder.brodcast(msg)
    }

    fun invokeCallback(location: Location, callbackId: Long) {
        val l = ArrayList<Any>()
        l.add(callbackId)
        l.add(location.latitude)
        l.add(location.longitude)
        mBackgroundChannel.invokeMethod("updateLocation", l)
    }


    private fun startLocationService(context: Context, callbackHandle: Long) {

        mContext = context

        if (sBackgroundFlutterEngine == null) {
            if (callbackHandle == 0L) {
                Log.e(TAG, "Fatal: no callback registered")
                return
            }

            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
            if (callbackInfo == null) {
                Log.e(TAG, "Fatal: failed to find callback")
                return
            }
            Log.i(TAG, "Starting Service...")
            sBackgroundFlutterEngine = FlutterEngine(context)

            val args = DartExecutor.DartCallback(
                    context.getAssets(),
                    FlutterMain.findAppBundlePath(context)!!,
                    callbackInfo
            )
            sBackgroundFlutterEngine!!.getDartExecutor().executeDartCallback(args)

        }

        mBackgroundChannel = MethodChannel(sBackgroundFlutterEngine!!.getDartExecutor().getBinaryMessenger(),
                "com.innovent/background_channel")
        mBackgroundChannel.setMethodCallHandler(this)
        startLocationUpdates()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    companion object {
        val TAG = "MyLocationService"

        @JvmStatic
        private var sBackgroundFlutterEngine: FlutterEngine? = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {

        if (call.method == "getLocation") {


            result.success("")
        }
    }
}
