class Location{
  double lat;
  double lng;
  double accuracy;
  double bearing;
  double speed;
  int time;
  double altitude;
  double speedAccuracy;

  Location(this.lat, this.lng, this.accuracy, this.bearing, this.speed,
      this.time, this.altitude, this.speedAccuracy);
}

class CallbackData {
  Location location;
  String optionalPayload;

  CallbackData(this.location, this.optionalPayload);
}