package com.example.locationnotifier;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private final int REQUEST_CODE_PERMISSIONS = 1410;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.ACCESS_FINE_LOCATION"};
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private Geocoder geocoder;
    private float radius = 500;
    private GeofenceHelper geofenceHelper;
    private double lat = 28.6482929;
    private double lng = 77.3720005;

    private Button button;
    private EditText editTextLatLong;
    private EditText editTextRadius;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        if (allPermissionsGranted())
            startMaps();
        else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

    }

    private void startMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null)
            return;
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        button = (Button) findViewById(R.id.button);
        editTextLatLong = (EditText) findViewById(R.id.editTextLatLong);
        editTextRadius = (EditText) findViewById(R.id.editTextRadius);
        textView = (TextView) findViewById(R.id.textViewLocality);
        geocoder = new Geocoder(this, Locale.getDefault());
        setLocation();

        setupListener();
    }

    private void setLocation(){
        try {
            String addressLine = geocoder.getFromLocation(lat, lng, 1).get(0).getAddressLine(0);
            String city = geocoder.getFromLocation(lat, lng, 1).get(0).getLocality();
            String pin = geocoder.getFromLocation(lat, lng, 1).get(0).getPostalCode();
            if (city.equals("null"))
                textView.setText(city);
            else if(addressLine.equals("null"))
                textView.setText(addressLine);
            else
                textView.setText(pin);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setupListener() {
        button.setOnClickListener(view -> {
            if (!validateTextFields())
                return;
            LatLng latLng = new LatLng(lat, lng);

            mMap.clear();
            addMarker(latLng);
            addCircle(latLng, radius);
            addGeofence(latLng, radius);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            setLocation();
        });
    }

    private boolean validateTextFields() {
        boolean valid = true;
        if (TextUtils.isEmpty(editTextLatLong.getText().toString())) {
            editTextLatLong.setError(getString(R.string.required_field));
            valid = false;
        }
        if (TextUtils.isEmpty(editTextRadius.getText().toString())) {
            editTextRadius.setError(getString(R.string.required_field));
            valid = false;
        }
        if (valid) {
            String latLong = editTextLatLong.getText().toString();
            if (!latLong.contains(",")) {
                editTextLatLong.setError(getString(R.string.invalid));
                return false;
            }
            String[] latLongArray = latLong.split(",");
            if (latLongArray.length != 2) {
                editTextLatLong.setError(getString(R.string.invalid));
                return false;
            }
            lat = Double.parseDouble(latLongArray[0]);
            lng = Double.parseDouble(latLongArray[1]);
            if (lat < -90 || lat > 90 || lng <= -180 || lng > 180) {
                editTextLatLong.setError(getString(R.string.invalid));
                return false;
            }
            radius = Float.parseFloat(editTextRadius.getText().toString());
        }
        return valid;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Delhi and move the camera
        LatLng delhi = new LatLng(28.6482929, 77.3720005);
        mMap.addMarker(new MarkerOptions().position(delhi).title(getString(R.string.marker_in_delhi)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 15));
        addGeofence(delhi, radius);
        addCircle(delhi, radius);
    }


    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng, float radius) {

        String GEOFENCE_ID = getString(R.string.geo_fence_id);
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.geofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(unused -> Log.d(TAG, getString(R.string.onSuccess)))
                .addOnFailureListener(e -> {
                    String errorMessage = geofenceHelper.getErrorString(e);
                    Log.d(TAG, "onFailure: " + errorMessage);
                });
    }

    private void addMarker(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 231, 242, 118));
        circleOptions.fillColor(Color.argb(200, 227, 182, 183));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        return true;
    }

    public void createNotification(String title, String text){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My Notification","Notification Name",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.cancelAll();
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this,"My Notification");
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.marker);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MapsActivity.this);
        managerCompat.notify(14,builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted())
                startMaps();
            else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }
}