import 'dart:async';
import 'dart:ui';

import 'package:background_location_service/callback_data.dart';
import 'package:background_location_service/location_settings.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';

import 'callback_dispatcher.dart';

class BackgroundLocationService {
  static const MethodChannel _channel =
      const MethodChannel('com.innovent/background_location_service');

  static Future<bool> startService(LocationSettings settings) async {
    final args = <dynamic>[
      PluginUtilities.getCallbackHandle(callbackDispatcher).toRawHandle(),
    ];
    args.addAll(settings.getArgs());
    print("BackgroundLocationService starting the service ..");
    var result = await _channel.invokeMethod('startLocationService', args);
    return result;
  }

  static Future<bool> addTopLevelCallback(
      void Function(CallbackData data) callback, String callbackId,
      {String optionalPayload = ""}) async {
    final args = <dynamic>[
      PluginUtilities.getCallbackHandle(callback).toRawHandle()
    ];
    args.add(callbackId);
    args.add(optionalPayload);
    var result = await _channel.invokeMethod('addTopLevelCallback', args);
    return result;
  }

  static Future<bool> setAlarm(
      {int alarmId,
      LocationSettings settings,
      DateTime time,
      String notificationTitle = "Alarm",
      String notificationContent = "You have an alarm"}) async {
    final args = <dynamic>[alarmId];
    args.addAll(settings.getArgs());

    args.add(DateFormat('yyyy-MM-dd HH:mm:ss').format(time));
    args.add(
        PluginUtilities.getCallbackHandle(callbackDispatcher).toRawHandle());
    args.add(notificationTitle);
    args.add(notificationContent);
    var result = await _channel.invokeMethod('setAlarm', args);
    return result;
  }

  static Future<bool> removeAlarm({int alarmId}) async {
    final args = <dynamic>[alarmId];
    var result = await _channel.invokeMethod('removeAlarm', args);
    return result;
  }

  static Future<bool> removeTopLevelCallback(String callbackId) async {
    final args = <dynamic>[callbackId];
    var result = await _channel.invokeMethod('removeTopLevelCallback', args);
    return result;
  }

  static Future<Location> getLatestLocation() async {
    WidgetsFlutterBinding.ensureInitialized();
    var result = await _channel.invokeMethod("getLocation");
    try {
      var lat = result[0] as double;
      var lng = result[1] as double;
      var accuracy = result[2] as double;
      var bearing = result[3] as double;
      var speed = result[4] as double;
      var time = result[5] as int;
      var altitude = result[6] as double;
      var speedAccuracy = result[7] as double;

      Location location = Location(
          lat, lng, accuracy, bearing, speed, time, altitude, speedAccuracy);
      return location;
    } catch (e) {
      return Location(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0);
    }
  }

  static Future<bool> get stopLocationService async {
    final bool data = await _channel.invokeMethod('stopLocationService');
    print("BackgroundLocationService stopping the service ..");
    return data;
  }
}
