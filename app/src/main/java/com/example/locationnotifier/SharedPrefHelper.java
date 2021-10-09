package com.example.locationnotifier;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

// Shared Preference Helper Class
public class SharedPrefHelper {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String RADIUS = "Radius";

    // create shared preference
    public static void create(Context context) {
        sharedPreferences = context.getSharedPreferences("SharedPref1", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // store data to shared preference
    public static void store(double lat, double lng, int rad) {
        editor.putString(LATITUDE, String.valueOf(lat));
        editor.putString(LONGITUDE, String.valueOf(lng));
        editor.putString(RADIUS, String.valueOf(rad));
        editor.apply();
    }

    // getters

    public static String getLat() {
        return sharedPreferences.getString(LATITUDE, null);
    }

    public static String getLng() {
        return sharedPreferences.getString(LONGITUDE, null);
    }

    public static String getRad() {
        return sharedPreferences.getString(RADIUS, null);
    }

}
