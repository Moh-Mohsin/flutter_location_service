import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:background_location_service/background_location_service.dart';

void main() {
  const MethodChannel channel = MethodChannel('background_location_service');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
 //   expect(await BackgroundLocationService.platformVersion, '42');
  });
}
