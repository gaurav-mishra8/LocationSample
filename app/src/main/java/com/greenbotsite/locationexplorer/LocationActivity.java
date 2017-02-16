package com.greenbotsite.locationexplorer;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.greenbotsite.locationexplorer.geocoding.GetAddressService;
import com.greenbotsite.locationexplorer.util.Constants;

public class LocationActivity extends AppCompatActivity implements LocationProvider.LocationResultListener, OnMapReadyCallback {

    private PermissionsChecker permissionsChecker;
    private static final int REQUEST_CODE = 0;
    private LocationProvider locationProvider;
    private AddressResultReceiver mResultReceiver;

    private static final String[] PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;

    Handler handler = new Handler();

    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, GetAddressService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setUp();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setUp() {
        permissionsChecker = new PermissionsChecker(this);
        locationProvider = LocationProvider.newInstance(this);
        mResultReceiver = new AddressResultReceiver(handler);
    }

    @Override
    protected void onStart() {
        super.onStart();

        attachLocationProvider();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //detachLocationProvider();
    }

    private void detachLocationProvider() {
        if (locationProvider != null) {
            locationProvider.stopGeoFencing();
            locationProvider.stopLocationUpdates();
            locationProvider.detachLocationProvider();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!permissionsChecker.hasPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivity(this, REQUEST_CODE, PERMISSIONS);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_CANCELED)
            finish();
        else if (requestCode == LocationProvider.REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            Toast.makeText(this, "success in location change settings", Toast.LENGTH_LONG).show();
        }
    }

    private void attachLocationProvider() {
        if (locationProvider != null) {
            locationProvider.attachLocationProvider();
        }
    }

    @Override
    public void setLocation(Location location) {
        updateMarker(new LatLng(location.getLatitude(), location.getLongitude()));
        startIntentService(location);
        if (location != null)
            showToast("Latitude=" + location.getLatitude() + " Longitude=" + location.getLongitude());
    }

    @Override
    public void setConnectionError() {
        Toast.makeText(this, "connection error ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGeoFenceStatusChange(String message) {
        showToast(message);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void updateMarker(LatLng latlng) {
        mMap.addMarker(new MarkerOptions().position(latlng).title("Marker at your current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String address = resultData.getString(Constants.RESULT_DATA_KEY);

            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(address);
            }

        }
    }

    private void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }


}
