package com.example.locationnotifier;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ThemedSpinnerAdapter;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.locationnotifier.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private float radius = 200;
    private String city;
    private String pin;
    private GeofenceHelper geofenceHelper;
    private double lat = 28.6482929;
    private double lng = 77.3720005;

    Button button;
    EditText editTextLatLong;
    EditText editTextRadius;
    TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        button = (Button) findViewById(R.id.button);
        editTextLatLong = (EditText) findViewById(R.id.editTextLatLong);
        editTextRadius = (EditText) findViewById(R.id.editTextRadius);
        textView = (TextView) findViewById(R.id.textViewLocality);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            city = geocoder.getFromLocation(lat,lng,1).get(0).getLocality();
            String pin = geocoder.getFromLocation(lat,lng,1).get(0).getPostalCode();
            Log.d(TAG,city);
            Log.d(TAG,pin);
            if(city == null)
                textView.setText(city);
            else
                textView.setText(pin);
        } catch (IOException e) {
            e.printStackTrace();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateTextFields()){
                    return;
                }
                String latLong = editTextLatLong.getText().toString();
                String[] latLongArray = latLong.split(",");
                lat = Double.parseDouble(latLongArray[0]);
                lng = Double.parseDouble(latLongArray[1]);
                radius = Float.parseFloat(editTextRadius.getText().toString());
                LatLng latLng = new LatLng(lat,lng);

                mMap.clear();
                addMarker(latLng);
                addCircle(latLng,radius);
                addGeofence(latLng,radius);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                try {
                    city = geocoder.getFromLocation(lat,lng,1).get(0).getLocality();
                    pin = geocoder.getFromLocation(lat,lng,1).get(0).getPostalCode();
                    if(city.equals(getString(R.string.nullString)))
                        textView.setText(city);
                    else {
                        textView.setText(pin);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        return valid;
    }

            @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Delhi and move the camera
        LatLng delhi = new LatLng(28.6482929,77.3720005);
        mMap.addMarker(new MarkerOptions().position(delhi).title(getString(R.string.marker_in_delhi)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi,15));
        enableUserLocation();
        addGeofence(delhi,radius);
        addCircle(delhi,radius);
    }

    private void enableUserLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }
        else{
            // Ask for permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                // We need to show user a dialog for displaying why the permission is needed and then ask for the permission
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
            else{
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // we have the permission
                mMap.setMyLocationEnabled(true);
            }
            else{
                // we do not have the permission
                Log.d(TAG,getString(R.string.no_permission));
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng, float radius){

        String GEOFENCE_ID = getString(R.string.geo_fence_id);
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID,latLng,radius,Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.geofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,getString(R.string.onSuccess));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG,"onFailure: " + errorMessage);
                    }
                });
    }

    private void addMarker(LatLng latLng){
        mMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void addCircle(LatLng latLng,float radius){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255,231,242,118));
        circleOptions.fillColor(Color.argb(200,227,182,183));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
}