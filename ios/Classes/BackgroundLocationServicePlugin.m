#import "BackgroundLocationServicePlugin.h"
#if __has_include(<background_location_service/background_location_service-Swift.h>)
#import <background_location_service/background_location_service-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "background_location_service-Swift.h"
#endif

@implementation BackgroundLocationServicePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBackgroundLocationServicePlugin registerWithRegistrar:registrar];
}
@end
