package com.roimaa.reminderer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private static final String TAG = LocationActivity.class.getSimpleName();
    private static final String GPS = "gps";
    private static final int PERMISSION_REQ_CODE = 546;
    private static final int GEOFENCE_RADIUS = 100;

    private GoogleMap mMap;
    private Marker mChosenLocation;
    private Circle mChosenCircle;
    private Button mOk;
    private boolean mPickLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent startIntent = getIntent();
        mPickLocation = startIntent.getBooleanExtra("pickLocation", false);

        mOk = findViewById(R.id.Ok);
        if (!mPickLocation) {
            mOk.setVisibility(View.GONE);
        } else {
            mOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    if (null != mChosenLocation) {
                        returnIntent.putExtra("latitude", mChosenLocation.getPosition().latitude);
                        returnIntent.putExtra("longitude", mChosenLocation.getPosition().longitude);
                        setResult(Activity.RESULT_OK, returnIntent);
                    } else {
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                    }
                    LocationActivity.this.finish();
                }
            });
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        String[] permissions = {ACCESS_FINE_LOCATION};
        mMap = googleMap;

        if (!isMyLocationEnabled()) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_CODE);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (null != mChosenLocation) {
                    mChosenLocation.remove();
                }
                if (null != mChosenCircle) {
                    mChosenCircle.remove();
                }

                mChosenLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("PICKED_LOCATION"));

                if (!mPickLocation) {
                    setMockLocation(latLng);
                } else {
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(latLng)
                            .radius(Double.valueOf(GEOFENCE_RADIUS))
                            .strokeColor(Color.argb(50, 70, 70, 70))
                            .fillColor(Color.argb(70, 150, 150, 150));

                    mChosenCircle = mMap.addCircle(circleOptions);
                }
            }
        });
    }

    private boolean isMyLocationEnabled() {
        return (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() " + PERMISSION_REQ_CODE);
        switch (requestCode) {
            case PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions granted, good to go!");
                }
                break;

            default:
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void setMockLocation(LatLng latLng) {
        LocationManager locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        locationManager.addTestProvider(GPS, false, false,
                false, false, true, true, true, 0, 5);

        locationManager.setTestProviderEnabled(GPS, true);

        locationManager.requestLocationUpdates(GPS, 0, 0, this);

        Location mockLocation = new Location(GPS);
        mockLocation.setLatitude(latLng.latitude);
        mockLocation.setLongitude(latLng.longitude);
        mockLocation.setAltitude(10);
        mockLocation.setAccuracy(5);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(System.nanoTime());
        locationManager.setTestProviderStatus(GPS, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

        locationManager.setTestProviderLocation(GPS, mockLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: " + provider + " " + status);

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
       }
}