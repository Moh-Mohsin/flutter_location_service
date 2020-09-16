import 'package:background_location_service/background_location_service.dart';
import 'package:background_location_service/location.dart';
import 'package:background_location_service/location_settings.dart';
import 'package:flutter/material.dart';

void firstTopLevelCallback(Location location) {
  print(
      "firstTopLevelCallback data is lat: ${location.lat} lng:${location.lat}");
}

void secondTopLevelCallback(Location location) {
  print(
      "secondTopLevelCallback data is lat: ${location.lat} lng:${location.lat}");
}

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _currentLocationText="No location";
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Android foreground service'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: RaisedButton(
                  child: Text('Start Service'),
                  onPressed: () async {
                    await BackgroundLocationService.startService(
                        LocationSettings());
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: RaisedButton(
                  child: Text('Register Callbacks'),
                  onPressed: () async {
                    await BackgroundLocationService.addTopLevelCallback(
                        firstTopLevelCallback);
                    await BackgroundLocationService.addTopLevelCallback(
                        secondTopLevelCallback);
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: RaisedButton(
                  child: Text('Stop Service'),
                  onPressed: () async {
                    await BackgroundLocationService.stopLocationService;
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: RaisedButton(
                  child: Text('Get latest location'),
                  onPressed: () async {
                   var location = await BackgroundLocationService.getLatestLocation();
                   setState(() {
                     _currentLocationText=  "Current location is lat: ${location.lat} lng:${location.lat}";
                   });
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: Text(_currentLocationText),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
