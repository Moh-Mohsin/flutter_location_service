import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:background_location_service/background_location_service.dart';

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
        await BackgroundLocationService.startLocationService;
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
          child: InkWell(child: Text('Running on: $_platformVersion\n'),onTap: () async {
             await BackgroundLocationService.stopLocationService;
          },),
        ),
      ),
    );
  }
}
