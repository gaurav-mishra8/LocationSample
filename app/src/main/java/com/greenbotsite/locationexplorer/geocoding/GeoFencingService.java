package com.greenbotsite.locationexplorer.geocoding;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.greenbotsite.locationexplorer.R;

import java.util.List;

/**
 * Created by gaurav on 15/2/17.
 */

public class GeoFencingService extends IntentService {

    private static final String TAG = GeoFencingService.class.getSimpleName();

    public GeoFencingService() {
        super("GeoFencingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
        }


    }

    private String getGeofenceTransitionDetails(GeoFencingService geoFencingService, int geofenceTransition, List triggeringGeofences) {

        return "this is a geo fence";
    }

    private void sendNotification(String geofenceTransitionDetails) {

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(GeoFencingService.this);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(GeoFencingService.this)
                        .setSmallIcon(R.drawable.ic_audiotrack)
                        .setContentTitle("Geo Fencing Notification")
                        .setContentText(geofenceTransitionDetails);

        notificationManagerCompat.notify(101, builder.build());


    }


    private static class GeofenceErrorMessages {

        public static String getErrorString(int errorCode) {
            return "error occurred";
        }

    }
}
