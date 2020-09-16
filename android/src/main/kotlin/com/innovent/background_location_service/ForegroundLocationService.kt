package com.innovent.background_location_service

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.innovent.background_location_service.utils.location.PluginUtils
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import java.util.*


class ForegroundLocationService : Service(), MethodChannel.MethodCallHandler {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mBackgroundChannel: MethodChannel
    private lateinit var mContext: Context
    private lateinit var mLocation: Location

    private var callbackDispatcherId: Long = 0
    private var mInterval: Int = 0
    private var mSmallestDisplacement: Int = 0
    private var mFastestInterval: Int = 0
    private var mPriority: Int = 0

    val CHANNEL_ID = "ForegroundServiceChannel"

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                updateLocation(location)
            }
        }
    }

    var mLocationRequest: LocationRequest? = null

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

        val notification = buildNotification(intent)
        startForeground(1, notification)
        initVariables(intent)
        //do heavy work on a background thread
        //stopSelf();
        startLocationService(this, callbackDispatcherId)

        return START_NOT_STICKY
    }

    private fun initVariables(intent: Intent) {

        callbackDispatcherId = intent.getLongExtra(CALLBACK_DISPATCHER, 0)
        mPriority = intent.getIntExtra(PRIORITY, LocationRequest.PRIORITY_HIGH_ACCURACY)
        mFastestInterval = intent.getIntExtra(FASTEST_INTERVAL_IN_MS, 0)
        mInterval = intent.getIntExtra(LOCATION_INTERVAL_IN_MS, 0)
        mSmallestDisplacement = intent.getIntExtra(MIN_CHANGE_DISTANCE_IN_METER, 0)
        // callbackId = intent.getLongExtra("callback", 0)
    }

    private fun buildNotification(intent: Intent): Notification? {
        val notificationIntent = Intent(this, ForegroundLocationService::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)

        val title = intent.getStringExtra(NOTIFICATION_TITLE)
        val content = intent.getStringExtra(NOTIFICATION_CONTENT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build()
        return notification;
    }


    private fun updateLocation(location: Location) {
        val msg = "location is ${location.latitude} ${location.longitude}"
        mLocation = location
        CallbackHolder.location = location
        Log.d(TAG, msg)
        CallbackHolder.callbackList.forEach {
            invokeCallback(location, it)
        }
    }

    private fun invokeCallback(location: Location, callbackId: Long) {
        val args = ArrayList<Any>()
        args.add(callbackId)
        args.add(location.latitude)
        args.add(location.longitude)
        args.add(location.accuracy)
        args.add(location.bearing)
        args.add(location.speed)
        args.add(location.time)
        args.add(location.altitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            args.add(location.speedAccuracyMetersPerSecond)
        }else{
            args.add(0)
        }

        mBackgroundChannel.invokeMethod("updateLocation", args)
    }


    private fun startLocationService(context: Context, callbackHandle: Long) {
        mContext = context
        CallbackHolder.isServiceRunning = true

        if (!PluginUtils.isLocationEnabled(context)) {
            PluginUtils.launchLocationSettings(context)
            PluginUtils.showToast(context, "Please turn ON your GPS.")
        }

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
        CallbackHolder.isServiceRunning = false
        CallbackHolder.dispose()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    companion object {
        val TAG = "FLocationService"

        @JvmStatic
        private var sBackgroundFlutterEngine: FlutterEngine? = null
        const val CALLBACK_DISPATCHER = "callbackDispatcher"
        const val PRIORITY = "priority"
        const val FASTEST_INTERVAL_IN_MS = "fastestIntervalMs"
        const val LOCATION_INTERVAL_IN_MS = "locationIntervalMs"
        const val MIN_CHANGE_DISTANCE_IN_METER = "minChangeDistanceInMeters"
        const val NOTIFICATION_TITLE = "notificationTitle"
        const val NOTIFICATION_CONTENT = "notificationContent"
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {

        if (call.method == "getLocation") {
            result.success("")
        }
    }
}
