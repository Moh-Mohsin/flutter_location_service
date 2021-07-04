class LocationSettings {
  static const int PRIORITY_HIGH_ACCURACY = 100;
  static const int PRIORITY_BALANCED_POWER_ACCURACY = 102;
  static const int PRIORITY_LOW_POWER = 104;
  static const int PRIORITY_NO_POWER = 105;

  LocationSettings(
      {this.locationIntervalMs = 1000,
      this.fastestIntervalMs = 500,
      this.minChangeDistanceInMeters = 1,
      this.priority = LocationSettings.PRIORITY_HIGH_ACCURACY,
      this.enableToastNotifications = true,
      this.notificationTitle = "Supervisor",
      this.notificationContent = "Service is running.."});

  int locationIntervalMs;
  int fastestIntervalMs;
  int minChangeDistanceInMeters;
  int priority;
  String notificationTitle;
  String notificationContent;
  bool enableToastNotifications;
  List<dynamic> getArgs() {
    return <dynamic>[
      priority,
      fastestIntervalMs,
      locationIntervalMs,
      minChangeDistanceInMeters,
      notificationTitle,
      notificationContent,
      enableToastNotifications
    ];
  }
}
