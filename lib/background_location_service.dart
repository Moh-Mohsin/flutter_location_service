import 'dart:async';

import 'package:flutter/services.dart';

class BackgroundLocationService {
  static const MethodChannel _channel =
      const MethodChannel('background_location_service');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
