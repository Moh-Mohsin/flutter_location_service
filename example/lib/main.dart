import 'package:background_location_service/background_location_service.dart';
import 'package:background_location_service/callback_data.dart';
import 'package:background_location_service/location_settings.dart';
import 'package:flutter/material.dart';

Future<void> firstTopLevelCallback(CallbackData data) async {
  print(
      "firstTopLevelCallback data is lat: ${data.location.lat} lng:${data.location.lat} optionalPayload:${data.optionalPayload}");
}

void secondTopLevelCallback(CallbackData data) {
  print(
      "secondTopLevelCallback data is lat: ${data.location.lat} lng:${data
          .location.lat} optionalPayload:${data.optionalPayload}");
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
                 /*    await BackgroundLocationService.startService(
                        LocationSettings(locationIntervalMs: 1000)); */

                        var date=DateTime.now().add(Duration(seconds: 1));
                        print(date.toString());
                          await BackgroundLocationService.setAlarm(alarmId:11,
                        settings:LocationSettings(locationIntervalMs: 1000),time: date);
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: RaisedButton(
                  child: Text('Register Callbacks'),
                  onPressed: () async {
                    await BackgroundLocationService.addTopLevelCallback(
                        firstTopLevelCallback, "id_1",
                        optionalPayload: "optionalPayload1 is working");
                    await BackgroundLocationService.addTopLevelCallback(
                        secondTopLevelCallback, "id_2",
                        optionalPayload: "optionalPayload2 is working");
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: RaisedButton(
                  child: Text('Remove Callbacks'),
                  onPressed: () async {
                    
                    await BackgroundLocationService.removeTopLevelCallback(
                        "id_1");
                  },
                ),
              ),

              Padding(
                padding: const EdgeInsets.all(8.0),
                child: RaisedButton(
                  child: Text('Stop Service'),
                  onPressed: () async {
                    //await BackgroundLocationService.removeAlarm(alarmId:11,);
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
