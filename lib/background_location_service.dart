import 'dart:async';

import 'package:flutter/services.dart';

class BackgroundLocationService {
  static const MethodChannel _channel =
      const MethodChannel('background_location_service');

  static Future<String> get startLocationService async {
    final String version = await _channel.invokeMethod('startLocationService');
    return version;
  }


  static Future<String> get stopLocationService async {
    final String version = await _channel.invokeMethod('stopLocationService');
    return version;
  }

static void setMethodCallHandler(){}
}
