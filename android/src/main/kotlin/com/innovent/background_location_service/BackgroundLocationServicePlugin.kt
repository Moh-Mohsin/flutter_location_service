package com.innovent.background_location_service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.innovent.background_location_service.models.AlarmData
import com.innovent.background_location_service.prefs.SharedPrefManager
import com.innovent.background_location_service.utils.location.PluginUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import timber.log.Timber
import java.text.SimpleDateFormat


/** BackgroundLocationServicePlugin */
public class BackgroundLocationServicePlugin constructor() : FlutterPlugin,
        ActivityAware, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var mContext: Context? = null
    private val locationPermissionCode = 72
    private var isLocationPermissionGranted: Boolean = false
    private var currentActivity: Activity? = null
    private var mArgs: ArrayList<*>? = null

    constructor(context: Context) : this() {
        mContext = context
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        if (mContext == null) mContext = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "com.innovent/background_location_service")
        channel.setMethodCallHandler(this)
        Timber.plant(Timber.DebugTree())
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.innovent/background_location_service")
            val plugin = BackgroundLocationServicePlugin(registrar.context())
            registrar.addRequestPermissionsResultListener(plugin)
            channel.setMethodCallHandler(plugin)
        }
        const val TAG="BLServicePlugin"
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "startLocationService" -> {
                handleStartServiceMethod(call, result)
                Timber.i("Staring the service")
            }
            "stopLocationService" -> {
                handleStopServiceMethod(call, result)
                Timber.i("Stopping the service")
            }
            "addTopLevelCallback" -> {
                handleAddTopLevelCallbackMethod(call, result)
                Timber.i("adding a callback")
            }
            "removeTopLevelCallback" -> {
                handleRemoveTopLevelCallbackMethod(call, result)
                Timber.i("removing callback")
            }
            "getLocation" -> {
                handleGetLocationMethod(call, result)
                Timber.i("Removing a callback")
            }
            "setAlarm" -> {
                handleSetAlarm(call, result)
                Timber.i("setting a new Alarm")
            }
            "removeAlarm" -> {
                handleRemoveAlarm(call, result)
                Timber.i("Removing alarm")
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun handleRemoveAlarm(call: MethodCall, result: Result) {
        val args = call.arguments<ArrayList<*>?>()
        val alarmId = args!![0] as Int
        val intent = Intent(mContext, MyBroadcastReceiver::class.java)
        val alarmManager = this.mContext?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(getPendingIntent(alarmId,intent) )
        var sharedprefs= mContext!! .getSharedPreferences(SharedPrefManager.PREF_NAME, Context.MODE_PRIVATE)
        SharedPrefManager(sharedprefs).removeAlarmByID(alarmId)
        println("Alarm Canceled")
        result.success(true)
    }

    private fun handleSetAlarm(call: MethodCall, result: Result) {
        val args = call.arguments<ArrayList<*>?>()
        val alarmId = args!![0] as Int
        val priority = args[1] as Int
        val fastestIntervalMs = args[2] as Int
        val locationIntervalMs = args[3] as Int
        val minChangeDistanceInMeters = args[4] as Int
        val notificationTitle = args[5] as String
        val notificationContent = args[6] as String
        val enableToasts = args[7] as Boolean
        val dateTime = args[8] as String
        val callbackDispatcher = args!![9] as Long
        val broadCastNotificationTitle = args[10] as String
        val broadCastNotificationContent = args[11] as String
        val repeatEveryMs = args[12] as Int
        CallbackHolder.enableToasts = enableToasts

        val intent = Intent(mContext, MyBroadcastReceiver::class.java)
        intent.putExtra(ForegroundLocationService.CALLBACK_DISPATCHER, callbackDispatcher)
        intent.putExtra(ForegroundLocationService.PRIORITY, priority)
        intent.putExtra(ForegroundLocationService.FASTEST_INTERVAL_IN_MS, fastestIntervalMs)
        intent.putExtra(ForegroundLocationService.LOCATION_INTERVAL_IN_MS, locationIntervalMs)
        intent.putExtra(ForegroundLocationService.MIN_CHANGE_DISTANCE_IN_METER, minChangeDistanceInMeters)
        intent.putExtra(ForegroundLocationService.NOTIFICATION_TITLE, notificationTitle)
        intent.putExtra(ForegroundLocationService.NOTIFICATION_CONTENT, notificationContent)
        intent.putExtra(MyBroadcastReceiver.NOTIFICATION_TITLE, broadCastNotificationTitle)
        intent.putExtra(MyBroadcastReceiver.NOTIFICATION_CONTENT, broadCastNotificationContent)
        startAlertAtParticularTime(alarmId,intent,dateTime,repeatEveryMs)
        result.success(true)

        var alarmData= AlarmData(alarmId,priority,fastestIntervalMs,locationIntervalMs,minChangeDistanceInMeters,notificationTitle,notificationContent,enableToasts,dateTime,callbackDispatcher,broadCastNotificationTitle,broadCastNotificationContent,repeatEveryMs)
        print(alarmData.toString())
        var sharedprefs= mContext!! .getSharedPreferences(SharedPrefManager.PREF_NAME, Context.MODE_PRIVATE)
        SharedPrefManager(sharedprefs).storeAlarm(alarmData)
    }


    fun getPendingIntent(id: Int, intent: Intent):PendingIntent{
        return PendingIntent.getBroadcast(
                mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }


    fun startAlertAtParticularTime(id: Int, intent: Intent, dateTime: String, repeatEveryMs: Int) {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime)

        println(date.time)
       val alarmManager = this.mContext?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.setInexactRepeating(AlarmManager.RTC_WAKEUP, date.time,
                repeatEveryMs.toLong(), getPendingIntent(id,intent))
    }


    private fun handleGetLocationMethod(call: MethodCall, result: Result) {
        var responseDelayMs = 0L
        if (!CallbackHolder.isServiceRunning && mArgs != null) {
            Timber.d("getLocation: service is not running, killed? attempting restarting service")
            PluginUtils.showToast(currentActivity!!.applicationContext,
                    "Unable to get the location, Please make sure that the service is running !");
            startLocationService()
            responseDelayMs = 2000L
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (responseDelayMs != 0L) {
                if (!CallbackHolder.isServiceRunning) {
                    Timber.d("location service still Not active after $responseDelayMs ms")
                } else {
                    Timber.d("location service STARTED successfully, current location: ${CallbackHolder.location.toString()}")
                }
            }
            val args = arrayListOf<Any>()
            args.add(CallbackHolder.location?.latitude ?: 0.0)
            args.add(CallbackHolder.location?.longitude ?: 0.0)
            args.add(CallbackHolder.location?.accuracy ?: 0.0)
            args.add(CallbackHolder.location?.bearing ?: 0.0)
            args.add(CallbackHolder.location?.speed ?: 0.0)
            args.add(CallbackHolder.location?.time ?: 0)
            args.add(CallbackHolder.location?.altitude ?: 0.0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                args.add(CallbackHolder.location?.speedAccuracyMetersPerSecond ?: 0.0)
            } else {
                args.add(0.0)
            }
            result.success(args)
        }, responseDelayMs)

    }


    private fun handleRemoveTopLevelCallbackMethod(call: MethodCall, result: Result) {
        val args = call.arguments<ArrayList<*>?>()
        val callbackId = args!![0] as String
        CallbackHolder.removeCallback(callbackId)
        result.success(true)
    }

    private fun handleAddTopLevelCallbackMethod(call: MethodCall, result: Result) {
        val args = call.arguments<ArrayList<*>?>()
        val callback = args!![0] as Long
        val callbackId = args!![1] as String
        val optionalPayload = args!![2] as String
        val data = CallBackData(callback, callbackId, optionalPayload)
        CallbackHolder.addCallback(data)
        if (!CallbackHolder.isServiceRunning) {
            PluginUtils.showToast(currentActivity!!.applicationContext,
                    "The callback is added but you need to start the service to get the location updates !");
        }
        result.success(true)
    }

    private fun handleStopServiceMethod(call: MethodCall, result: Result) {
        mArgs = null
        if (CallbackHolder.isServiceRunning) {
            mContext?.stopService(Intent(mContext, ForegroundLocationService::class.java))
            result.success(true)
            PluginUtils.showToast(currentActivity!!.applicationContext,
                    "Stopped successfully !")
        } else {
            PluginUtils.showToast(currentActivity!!.applicationContext,
                    "Service is not running !")
            result.success(false)
        }
    }

    private fun handleStartServiceMethod(call: MethodCall, result: Result) {
        mArgs = call.arguments()!!
        startLocationService()
        result.success(true)
    }

    private fun startLocationService() {
        if(mArgs == null){
            Timber.d("aborting starting location service, mArgs = null")
            return
        }
        checkLocationPermission(currentActivity!!.application)
        if (isLocationPermissionGranted && !CallbackHolder.isServiceRunning) startForegroundService(
            mArgs
        )
        else if (CallbackHolder.isServiceRunning) {
            PluginUtils.showToast(
                currentActivity!!.applicationContext,
                "Service is already running !"
            )
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>,context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    private fun startForegroundService(args: ArrayList<*>?) {
        val callbackDispatcher = args!![0] as Long
        val priority = args[1] as Int
        val fastestIntervalMs = args[2] as Int
        val locationIntervalMs = args[3] as Int
        val minChangeDistanceInMeters = args[4] as Int
        val notificationTitle = args[5] as String
        val notificationContent = args[6] as String
        val enableToasts = args[7] as Boolean

        CallbackHolder.enableToasts = enableToasts

        val intent = Intent(mContext, ForegroundLocationService::class.java)
        intent.putExtra(ForegroundLocationService.CALLBACK_DISPATCHER, callbackDispatcher)
        intent.putExtra(ForegroundLocationService.PRIORITY, priority)
        intent.putExtra(ForegroundLocationService.FASTEST_INTERVAL_IN_MS, fastestIntervalMs)
        intent.putExtra(ForegroundLocationService.LOCATION_INTERVAL_IN_MS, locationIntervalMs)
        intent.putExtra(ForegroundLocationService.MIN_CHANGE_DISTANCE_IN_METER, minChangeDistanceInMeters)
        intent.putExtra(ForegroundLocationService.NOTIFICATION_TITLE, notificationTitle)
        intent.putExtra(ForegroundLocationService.NOTIFICATION_CONTENT, notificationContent)
        //  intent.putExtra("callback", callback)
        Timber.d("sending location service intent")
        mContext?.startService(intent)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>?,
                                            grantResults: IntArray?): Boolean {

        when (requestCode) {
            locationPermissionCode -> {
                if (null != grantResults) {
                    isLocationPermissionGranted = grantResults.isNotEmpty() &&
                            grantResults.get(0) == PackageManager.PERMISSION_GRANTED
                }
                // do something here to handle the permission state
                if (isLocationPermissionGranted) updatePluginState()
                else
                    PluginUtils.showToast(currentActivity!!.applicationContext, "Location permission is needed to start the service.")

                // only return true if handling the request code
                return true
            }
        }
        return false
    }

    private fun updatePluginState() {
        startForegroundService(mArgs)
    }

    private fun checkLocationPermission(context: Application) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!isLocationPermissionGranted) {
            ActivityCompat.requestPermissions(currentActivity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationPermissionCode)
        }
    }

    override fun onDetachedFromActivity() {
        currentActivity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        currentActivity = null
    }

}
