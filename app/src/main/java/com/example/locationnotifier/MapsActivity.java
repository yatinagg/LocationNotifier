package com.example.locationnotifier;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private Geocoder geocoder;
    private int radius = 500;
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

        // create shared preference helper
        SharedPrefHelper.create(this);
        setupView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // check whether all permissions are granted or not
        if (allPermissionsGranted()) {
            // start maps
            startMaps();
        } else {
            // request the required permissions
            //Log.d("perm", String.valueOf(REQUIRED_PERMISSIONS0)));
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
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

        geocoder = new Geocoder(this, Locale.getDefault());

        // retrieve the previously stored data
        String latString = SharedPrefHelper.getLat();
        if (latString != null) {
            lat = Double.parseDouble(latString);
            lng = Double.parseDouble(SharedPrefHelper.getLng());
            radius = Integer.parseInt(SharedPrefHelper.getRad());
            setupTextFields();
        }

        // set location for any previous location
        setLocation();

        // listeners
        setupListener();
    }

    public void setupView() {
        button = findViewById(R.id.button);
        editTextLatLong = findViewById(R.id.editTextLatLong);
        editTextRadius = findViewById(R.id.editTextRadius);
        textView = findViewById(R.id.textViewLocality);
    }

    private void setupTextFields() {

        editTextLatLong.setText(getString(R.string.lat_lng, lat, lng));
        editTextRadius.setText(String.valueOf(radius));
    }

    // set the location for display text
    private void setLocation() {
        try {
            String addressLine = geocoder.getFromLocation(lat, lng, 1).get(0).getAddressLine(0);
            String city = geocoder.getFromLocation(lat, lng, 1).get(0).getLocality();
            String pin = geocoder.getFromLocation(lat, lng, 1).get(0).getPostalCode();
            if (addressLine != null)
                textView.setText(addressLine);
            else if (city != null)
                textView.setText(city);
            else
                textView.setText(pin);
            SharedPrefHelper.store(lat, lng, radius);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setupListener() {

        button.setOnClickListener(view -> {
            // check for validation of text fields
            if (!validateTextFields())
                return;
            LatLng latLng = new LatLng(lat, lng);

            mMap.clear();
            addMarker(latLng);
            addCircle(latLng, radius);
            addGeofence(latLng, radius);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            setLocation();
            closeKeyboard();
        });
    }

    private void closeKeyboard() {
        // this will give us the view which is currently focus in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently focus then this will protect the app from crash
        if (view != null) {
            // now assign the system service to InputMethodManager
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // validate text fields
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
            radius = Integer.parseInt(editTextRadius.getText().toString());
            if (radius == 0) {
                editTextRadius.setError(getString(R.string.invalid));
                return false;
            }
        }
        return valid;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Ghaziabad and move the camera
        LatLng ghaziabad = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(ghaziabad).title(getString(R.string.marker_in_ghaziabad)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ghaziabad, 15));
        addGeofence(ghaziabad, radius);
        addCircle(ghaziabad, radius);
    }


    // add geo fence
    private void addGeofence(LatLng latLng, float radius) {

        String GEOFENCE_ID = getString(R.string.geo_fence_id);
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.geofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(unused -> Log.d(TAG, getString(R.string.onSuccess)))
                .addOnFailureListener(e -> {
                    String errorMessage = geofenceHelper.getErrorString(e);
                    Log.d(TAG, "onFailure: " + errorMessage);
                });
    }

    // add marker
    private void addMarker(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
    }

    // add circle
    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 231, 242, 118));
        circleOptions.fillColor(Color.argb(200, 227, 182, 183));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    // check whether all permissions are granted or not
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted())
                startMaps();
            else {
                // create toast alert
                Toast.makeText(this, R.string.app_required_location_permission, Toast.LENGTH_SHORT).show();
                // open settings page for permissions
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null)));
            }
        }
    }

}