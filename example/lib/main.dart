import 'dart:async';

import 'package:background_location_service/background_location_service.dart';
import 'package:background_location_service/location.dart';
import 'package:background_location_service/location_settings.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void handler(Location location) {
  //location.add("value");
  print("LocationFromDart");
  print("data is ${location.lng} ${location.lat}");
}

void handler2(Location location) {
  //location.add("value");
  print("LocationFromDart2");
  print("data2 is ${location.lng} ${location.lat}");
}

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    try {
      await BackgroundLocationService.startService(LocationSettings());

      await BackgroundLocationService.addTopLevelCallback(handler);
      await BackgroundLocationService.addTopLevelCallback(handler2);
      var location = await BackgroundLocationService.getLatestLocation();
      print("data3 is ${location.lng} ${location.lat}");
    } on PlatformException {
      print("Eroorrrr");
    }

    if (!mounted) return;

    /* setState(() {
      _platformVersion = platformVersion;
    });*/
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: InkWell(
            child: Text('Running on: $_platformVersion\n'),
            onTap: () async {
              await BackgroundLocationService.stopLocationService;
            },
          ),
        ),
      ),
    );
  }
}
