package acm3599.where;

import android.*;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew on 11/30/2016.
 *
 */

public class ReminderManager {

    private final String TAG = "ReminderManager";
    private final long ZOOM_LEVEL = (long) 17.5;
    private static ReminderManager manager;
    private Context context;
    private GoogleMap map;

    private Map<Geofence, ArrayList<Reminder>> geofences;
    private int size;

    private Map<Geofence, Marker> markers;

    private ReminderManager() {
        geofences = new HashMap<>();
        size = 0;
        markers = new HashMap<>();
    }

    public static ReminderManager getInstance() {
        if(manager == null) {
            manager = new ReminderManager();
        }
        return manager;
    }

    public Geofence add(Reminder reminder) {
        Geofence fence = null;
        boolean exists = false;
        if(reminder.hasLocation()) {
            for (Geofence g : geofences.keySet()) {
                if (g.getRequestId().equals(reminder.getAddress())) {
                    // geofence already present
                    fence = g;
                    exists = true;
                    ArrayList<Reminder> list = geofences.get(g);
                    list.add(reminder);

                    Log.d(TAG, "geofence already exists");
                    for (Reminder r : list) {
                        Log.d(TAG, "*" + r.getTitle());
                    }
                    break;
                }
            }
            if(!exists) {
                // geofence does not exist yet
                LatLng reminderLatLng = reminder.getLatLng();
                fence = new Geofence.Builder()
                        .setRequestId(reminder.getAddress())
                        .setCircularRegion(reminderLatLng.latitude, reminderLatLng.longitude, 100)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build();

                ArrayList<Reminder> list = new ArrayList<>();
                list.add(reminder);
                size++;
                geofences.put(fence, list);
                updateFences(fence);
                Log.d(TAG, "geofence created");
            }
            markReminder(fence, reminder);
        }
        Log.d(TAG, "Reminder added: " + reminder);
        return fence;
    }

    public void remove(Reminder reminder) {
        for (Geofence g : geofences.keySet()) {
            for (Reminder r: geofences.get(g)) {
                if(r.equals(reminder)) {
                    geofences.get(g).remove(r);
                    Log.d("remove", "size: " + geofences.get(g).size());
                    if(geofences.get(g).size() < 1) {
                        Marker m = markers.get(g);
                        Log.d("remove", m.toString());
                        m.remove();
                        markers.remove(g);
                    }
                    break;
                }
            }
        }
    }

    public int getSize() {
        return size;
    }

    public ArrayList<Reminder> getReminders() {
        ArrayList<Reminder> result = new ArrayList<>();
        for (Geofence g : geofences.keySet()) {
            for (Reminder r: geofences.get(g)) {
                if(r.isActive()) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    public ArrayList<Reminder> getReminders(String tag) {
        ArrayList<Reminder> result = new ArrayList<>();
        for (Geofence g : geofences.keySet()) {
            if (g.getRequestId().equals(tag)) {
                for (Reminder r : geofences.get(g)) {
                    Log.d(TAG, r.getTitle());
                    if (r.isActive()) {
                        Log.d(TAG, "added: " + r.getTitle());
                        result.add(r);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public void setContext(Context c) {
        context = c;
    }

    public void updateFences(Geofence geofence) {
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent("GeofenceReceiver.class");
        intent.setClass(context, GeofenceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.GeofencingApi.addGeofences(
                    GoogleClientManager.getInstance().getClient(context),
                    request,
                    pendingIntent
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "geofence added");
                    } else {
                        switch(status.getStatusCode()) {
                            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                                Log.d(TAG, "geofence not available");
                                break;
                            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                                Log.d(TAG, "too many geofences");
                                break;
                            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                                Log.d(TAG, "too many pending intents");
                                break;
                        }
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        Log.d(TAG, "registered geofence");
    }

    public void markReminder(Geofence geofence, Reminder reminder) {
        LatLng loc = reminder.getLatLng();
        Marker m = markers.get(geofence);
        Log.d("markers.get(geofence)", "" + markers.get(geofence));
        if(m == null || markers.get(geofence) == null) {
            m = map.addMarker(new MarkerOptions().position(loc));
            m.setTag(reminder.getAddress());
            markers.put(geofence, m);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, ZOOM_LEVEL));
    }
}
