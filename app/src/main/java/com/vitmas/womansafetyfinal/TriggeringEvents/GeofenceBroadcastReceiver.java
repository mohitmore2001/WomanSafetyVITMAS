package com.vitmas.womansafetyfinal.TriggeringEvents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.vitmas.womansafetyfinal.LocationsActivities.MapsActivity;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG ="GeofenceBroad";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
     //   Toast.makeText(context,"Geofence Triggered",Toast.LENGTH_LONG).show();
        NotificationHelper notificationHelper=new NotificationHelper(context);
        GeofencingEvent geofencingEvent=GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()){
            Log.d(TAG,"OnReceive : Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence:geofenceList){
            Log.d(TAG,"OnReceive :" +geofence.getRequestId());
        }
        int transitationtype=geofencingEvent.getGeofenceTransition();
        switch (transitationtype){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context,"GEOFENCE_TRANSITION_ENTER",Toast.LENGTH_LONG).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER","", MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context,"GEOFENCE_TRANSITION_DWELL",Toast.LENGTH_LONG).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL","",MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context,"GEOFENCE_TRANSITION_EXIT",Toast.LENGTH_LONG).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT","",MapsActivity.class);
                break;
        }
    }
}
