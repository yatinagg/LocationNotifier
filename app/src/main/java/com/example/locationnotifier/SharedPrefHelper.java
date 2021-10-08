package com.example.locationnotifier;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

// Shared Preference Helper Class
public class SharedPrefHelper {

    private static SharedPreferences sharedPreferences;
    private static double lat = -1000;
    private static double lng = -1000;
    private static float rad = -1000;
    private static SharedPreferences.Editor editor;
    private static final String[] keys = {"Latitude","Longitude","Radius"};

    // create shared preference
    public static void create(Context context) {
        sharedPreferences = context.getSharedPreferences("SharedPref1", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // get data from shared preference
    public static void getData(){
        if(sharedPreferences.getString(keys[0],null) == null)
            return;
        SharedPrefHelper.setLat(Double.parseDouble(sharedPreferences.getString(keys[0],null)));
        SharedPrefHelper.setLng(Double.parseDouble(sharedPreferences.getString(keys[1],null)));
        SharedPrefHelper.setRad(Float.parseFloat(sharedPreferences.getString(keys[2],null)));
    }

    // store data to shared preference
    public static void store(double lat, double lng, double rad){
        editor.putString(keys[0], String.valueOf(lat));
        editor.putString(keys[1], String.valueOf(lng));
        editor.putString(keys[2], String.valueOf(rad));
        editor.apply();
    }

    // getters and setters

    public static double getLat() {
        return lat;
    }

    public static void setLat(double lat) {
        SharedPrefHelper.lat = lat;
    }

    public static double getLng() {
        return lng;
    }

    public static void setLng(double lng) {
        SharedPrefHelper.lng = lng;
    }

    public static float getRad() {
        return rad;
    }

    public static void setRad(float rad) {
        SharedPrefHelper.rad = rad;
    }
}
