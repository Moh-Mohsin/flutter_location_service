
package io.flutter.plugins;

import androidx.annotation.Keep;

import androidx.annotation.NonNull;


import io.flutter.embedding.engine.FlutterEngine;

import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry;

public final class MyRegistrarJ {
    public static void registerWith(@NonNull FlutterEngine flutterEngine) {
        ShimPluginRegistry shimPluginRegistry = new ShimPluginRegistry(flutterEngine);
        flutterEngine.getPlugins().add(new com.innovent.background_location_service.BackgroundLocationServicePlugin());

    }
}
