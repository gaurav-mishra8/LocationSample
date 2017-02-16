package com.greenbotsite.locationexplorer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.greenbotsite.locationexplorer.geocoding.GeoFencingService;
import com.greenbotsite.locationexplorer.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaurav on 14/2/17.
 */

public class LocationProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    public static final int REQUEST_CHECK_SETTINGS = 1;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Activity activity;
    private LocationResultListener locationResultListener;
    private GoogleApiClient mGoogleApiClient;

    private List<Geofence> mGeofenceList = new ArrayList<>();

    public interface LocationResultListener {

        void setLocation(Location location);

        void setConnectionError();

        void onGeoFenceStatusChange(String status);

    }

    public static LocationProvider newInstance(Activity activity) {
        return new LocationProvider(activity);
    }

    private LocationProvider(Activity activity) {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        this.activity = activity;
        if (activity instanceof LocationResultListener)
            locationResultListener = (LocationResultListener) activity;

        getCurrentLocationSettings();
    }

    protected LocationRequest createLocationRequest() {
        mLocationRequest = new LocationRequest();
        /*mLocationRequest.setInterval(100000);
        mLocationRequest.setFastestInterval(5000);*/
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }

    protected void getCurrentLocationSettings() {

        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            status.startResolutionForResult(
                                    activity,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });

    }

    public void attachLocationProvider() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    public void detachLocationProvider() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        locationResultListener.setConnectionError();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location == null) {
            try {
                sendLocationResult(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            } catch (SecurityException e) {
                throw new RuntimeException("location permission not granted");
            }
        } else {
            sendLocationResult(location);
        }

        startGeoFencing();

    }

    private void sendLocationResult(Location location) {
        mLastLocation = location;
        locationResultListener.setLocation(location);
    }

    public void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            throw new RuntimeException("location permission not granted");
        }
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    public void startGeoFencing() {

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeoFenceReceiver()
                    //getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {

        }
    }

    public void stopGeoFencing() {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(activity, GeoFencingService.class);
        return PendingIntent.getService(activity, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getGeoFenceReceiver() {
        Intent intent = new Intent("com.greenbotsite.locationexplorer.ACTION_GEOFENCE_RECEIVER");
        return PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        addGeoFence();
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private void addGeoFence() {

        if (mGeofenceList != null && !mGeofenceList.isEmpty())
            return;

        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("Office")
                .setCircularRegion(
                        mLastLocation.getLatitude(),
                        mLastLocation.getLongitude(),
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(10000)
                .build());

    }

    @Override
    public void onResult(@NonNull Status status) {

        locationResultListener.onGeoFenceStatusChange(status.getStatusMessage());
    }


}
