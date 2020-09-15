import 'dart:async';
import 'dart:ui';

import 'package:background_location_service/location.dart';
import 'package:background_location_service/location_settings.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'callback_dispatcher.dart';

class BackgroundLocationService {
  static const MethodChannel _channel =
      const MethodChannel('com.innovent/background_location_service');

  static Future<bool> get stopLocationService async {
    final bool data = await _channel.invokeMethod('stopLocationService');
    return data;
  }

  static Future<bool> startService(LocationSettings settings) async {
    final args = <dynamic>[
      PluginUtilities.getCallbackHandle(callbackDispatcher).toRawHandle(),
    ];
    args.addAll(settings.getArgs());
    var result = await _channel.invokeMethod('startLocationService', args);
    return true;
  }

  static Future<bool> addTopLevelCallback(
      void Function(Location location) callback) async {
    final args = <dynamic>[
      PluginUtilities.getCallbackHandle(callback).toRawHandle()
    ];

    var result =  await _channel.invokeMethod('addTopLevelCallback', args);
    return true;
  }

  static Future<Location> getLatestLocation() async {
    WidgetsFlutterBinding.ensureInitialized();
    var result = await _channel.invokeMethod("getLocation");
    var lat = result[0] as double;
    var lng = result[1] as double;

    return Location(lat, lng);
  }
}
