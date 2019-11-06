package com.fire.base.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.room.Room
import com.fire.base.database.AppDatabase
import java.io.IOException

class Utils {
    companion object{
        var appDb: AppDatabase? = null

        @JvmStatic
        fun getDb(context: Context):AppDatabase{
            if(appDb == null){
                appDb = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "firebase-login"
                ).build()
            }

            return appDb as AppDatabase
        }


        @JvmStatic
        fun dpToPx(value: Int): Int {
            return (value * Resources.getSystem().displayMetrics.density).toInt()
        }


        private fun checkLocationPermission(context:Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val res = context.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                return res == PackageManager.PERMISSION_GRANTED
            }
            return true
        }

        @JvmStatic
        fun getCurrentCountry(context:Context): String? {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager.simState == TelephonyManager.SIM_STATE_READY) {
                val simCountry = telephonyManager.simCountryIso
                if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                    return simCountry.toUpperCase()
                } else if (telephonyManager.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                    val networkCountry = telephonyManager.networkCountryIso
                    if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                        return networkCountry.toUpperCase()
                    }
                }
            }
            if (!checkLocationPermission(context)) {
                return null
            }
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var location: Location? = null
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (location == null) {
                if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                    location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                }
                if (location == null) {
                    if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                }
            }
            if (location != null) {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        val country_code = addresses[0].countryCode
                        if (country_code != null) {
                            return country_code
                        }
                    }
                } catch (ignored: IOException) {
                }

            }
            return null
        }


        private fun getEmojiByUnicode(unicode: String): String {
            return String(Character.toChars(Integer.parseInt(unicode.replace("U+", "0x").substring(2), 16)))
        }

        @JvmStatic
        fun getEmoji(unicode: String): String {
            val flagUnicodes = unicode.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return getEmojiByUnicode(flagUnicodes[0]) + getEmojiByUnicode(flagUnicodes[1])
        }

    }
}