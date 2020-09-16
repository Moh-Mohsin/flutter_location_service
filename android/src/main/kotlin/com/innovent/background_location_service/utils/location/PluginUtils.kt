package com.innovent.background_location_service.utils.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.innovent.background_location_service.CallbackHolder


class PluginUtils {
    companion object {
        fun isLocationEnabled(context: Context): Boolean {
            val locationManager: LocationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
            )
        }

        fun launchLocationSettings(context: Context) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        fun checkPermissions(context: Context): Boolean {
            if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            return false
        }

        fun showToast(context: Context, msg: String) {
            if (CallbackHolder.enableToasts) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }

    }
}