package com.example.locationnotifier;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

// Shared Preference Helper Class
public class SharedPrefHelper {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    // create shared preference
    public static void create(Context context) {
        sharedPreferences = context.getSharedPreferences("SharedPref1", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // store data to shared preference
    public static void store(double lat, double lng, double rad) {
        editor.putString("Latitude", String.valueOf(lat));
        editor.putString("Longitude", String.valueOf(lng));
        editor.putString("Radius", String.valueOf(rad));
        editor.apply();
    }

    // getters

    public static String getLat() {
        return sharedPreferences.getString("Latitude", null);
    }

    public static String getLng() {
        return sharedPreferences.getString("Longitude", null);
    }

    public static String getRad() {
        return sharedPreferences.getString("Radius", null);
    }

}
