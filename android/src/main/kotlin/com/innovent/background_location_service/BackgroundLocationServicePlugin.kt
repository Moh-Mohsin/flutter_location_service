package com.innovent.background_location_service

import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** BackgroundLocationServicePlugin */
public class BackgroundLocationServicePlugin : FlutterPlugin,EventChannel.StreamHandler, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private lateinit var context: Context
    var count = 0;

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context= flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "background_location_service")
        channel.setMethodCallHandler(this);
        eventChannel = EventChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(),  "background_location_service2")
        eventChannel.setStreamHandler(this)

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
            val channel = MethodChannel(registrar.messenger(), "background_location_service")
            channel.setMethodCallHandler(BackgroundLocationServicePlugin())

            val eventChannel = EventChannel(registrar.messenger(),   "background_location_service2")
            eventChannel.setStreamHandler(BackgroundLocationServicePlugin())
        }
    }

    override fun onListen(p0: Any?, p1: EventChannel.EventSink) {
        Log.d("MyLocationService", "add new listener 2")
        ChannelHolder.add(p1)
    }

    override fun onCancel(p0: Any) {
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "startLocationService") {
            context.startService(Intent(context, MyLocationService::class.java))
        } else if (call.method == "stopLocationService") {
            context.stopService(Intent(context, MyLocationService::class.java))
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
    }



}
