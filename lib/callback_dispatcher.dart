// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:ui';

import 'package:background_location_service/location.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void callbackDispatcher() {
  const MethodChannel _backgroundChannel =
      MethodChannel('com.innovent/background_channel');
  WidgetsFlutterBinding.ensureInitialized();

  _backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final List<dynamic> args = call.arguments;
    final Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));

    var lat = args[1] as double;
    var lng = args[2] as double;
    var accuracy = args[3] as double;
    var bearing = args[4] as double;
    var speed = args[5] as double;

    callback(Location(lat, lng, accuracy, bearing, speed));
    print("callbackDispatcher lat:$lat lng:$lng accuracy:$accuracy bearing:$bearing speed:$speed");
  });
}
