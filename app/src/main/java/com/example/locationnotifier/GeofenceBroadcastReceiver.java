package com.example.locationnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

// Broadcast receiver
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "Geofence";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Geofence Triggered", Toast.LENGTH_SHORT).show();
        GeofenceHelper geofenceHelper = new GeofenceHelper(context.getApplicationContext());

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event");
            return;
        }

        // generate notification
        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType) {
            case Geofence
                    .GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                geofenceHelper.createNotification("Geofence", "GEOFENCE_TRANSITION_ENTER");
                break;
            case Geofence
                    .GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                geofenceHelper.createNotification("Geofence", "GEOFENCE_TRANSITION_DWELL");
                break;
            case Geofence
                    .GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                geofenceHelper.createNotification("Geofence", "GEOFENCE_TRANSITION_EXIT");
                break;
        }
    }

}