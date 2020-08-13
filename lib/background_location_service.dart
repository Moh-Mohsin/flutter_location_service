import 'dart:async';

import 'package:flutter/services.dart';

class BackgroundLocationService {
  static const MethodChannel _channel =
      const MethodChannel('background_location_service');

  static Future<String> get startLocationService async {
    final String data = await _channel.invokeMethod('startLocationService');
    return data;
  }


  static Future<String> get stopLocationService async {
    final String data = await _channel.invokeMethod('stopLocationService');
    return data;
  }

  static Future<String> get getLocationStream  async {

    print("Calling the plugin getLocationStream");
    const EventChannel _stream = EventChannel('background_location_service2');
     _stream.receiveBroadcastStream().listen(_speechResultsHandler, onError: _speechResultErrorHandler);
  }

  static _speechResultsHandler(dynamic event) {
    final String normalizedEvent = event.toLowerCase();
    print("Received error: ${normalizedEvent}");
  }

  static  _speechResultErrorHandler(dynamic error) => print('Received error: ${error.message}');



  static void setMethodCallHandler(){}
}
