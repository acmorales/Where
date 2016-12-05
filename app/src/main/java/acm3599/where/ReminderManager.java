package acm3599.where;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

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
    private static ReminderManager manager;
    private GeofencingRequest geoRequest;
    private Context context;

    private Map<Geofence, ArrayList<Reminder>> geofences;

    private ReminderManager() {
        geofences = new HashMap<>();
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
                geofences.put(fence, list);
                Log.d(TAG, "geofence created");
            }
        }
        Log.d(TAG, "Reminder added: " + reminder);
        return fence;
    }

    public ArrayList<Reminder> getReminders() {
        ArrayList<Reminder> result = new ArrayList<>();
        for (Geofence g : geofences.keySet()) {
            for (Reminder r: geofences.get(g)) {
                result.add(r);
            }
        }
        return result;
    }

    public ArrayList<Reminder> getReminders(String tag) {
        ArrayList<Reminder> result = new ArrayList<>();
        for (Geofence g : geofences.keySet()) {
            if(g.getRequestId().equals(tag)) {
                result = geofences.get(g);
                break;
            }
        }
        return result;
    }

    public void setContext(Context c) {
        context = c;
    }
}
