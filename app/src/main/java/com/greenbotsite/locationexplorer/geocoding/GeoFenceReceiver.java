package com.greenbotsite.locationexplorer.geocoding;

import android.content.BroadcastReceiver;
import android.content.Context;
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

public class GeoFenceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeoFenceReceiver.GeofenceErrorMessages.getErrorString(
                    geofencingEvent.getErrorCode());

            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    context,
                    geofenceTransition,
                    triggeringGeofences
            );

            sendNotification(context, geofenceTransitionDetails);

        } else {

        }


    }

    private String getGeofenceTransitionDetails(Context context, int geofenceTransition, List triggeringGeofences) {

        return "this is a broadcast receiver geo fence";
    }

    private void sendNotification(Context context, String geofenceTransitionDetails) {

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
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
