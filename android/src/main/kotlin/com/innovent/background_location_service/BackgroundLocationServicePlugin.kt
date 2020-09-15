package com.innovent.background_location_service

import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** BackgroundLocationServicePlugin */
public class BackgroundLocationServicePlugin constructor() : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var mContext: Context? = null

    constructor(context: Context) : this() {
        mContext = context
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        if (mContext == null) mContext = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "com.innovent/background_location_service")
        channel.setMethodCallHandler(this)
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
            channel.setMethodCallHandler(BackgroundLocationServicePlugin(registrar.context()))
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        val args = call.arguments<ArrayList<*>?>()


        if (call.method == "startLocationService") {
            val callbackDispatcher = args!![0] as Long
            val priority = args[1] as Int
            val fastestIntervalMs = args[2] as Int
            val locationIntervalMs = args[3] as Int
            val minChangeDistanceInMeters = args[4] as Int
            val notificationTitle = args[5] as String
            val notificationContent = args[6] as String
            val intent = Intent(mContext, MyLocationService::class.java)
            intent.putExtra("callbackDispatcher", callbackDispatcher)
            intent.putExtra("priority", priority)
            intent.putExtra("fastestIntervalMs", fastestIntervalMs)
            intent.putExtra("locationIntervalMs", locationIntervalMs)
            intent.putExtra("minChangeDistanceInMeters", minChangeDistanceInMeters)
            intent.putExtra("notificationTitle", notificationTitle)
            intent.putExtra("notificationContent", notificationContent)
            //  intent.putExtra("callback", callback)
            mContext?.startService(intent)
            result.success("true")

        } else if (call.method == "stopLocationService") {
            mContext?.stopService(Intent(mContext, MyLocationService::class.java))
            result.success(true)
        } else if (call.method == "addTopLevelCallback") {
            val callback = args!![0] as Long
            CallbackHolder.addCallback(callback)
            result.success("true")
        } else if (call.method == "getLocation") {
            val args = arrayListOf<Any>()
            args.add(CallbackHolder.location?.latitude ?: 0.0)
            args.add(CallbackHolder.location?.longitude ?: 0.0)
            result.success(args)
        } else {


            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


}
