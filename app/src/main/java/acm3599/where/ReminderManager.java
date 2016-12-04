package acm3599.where;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

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

    // the ints in the ArrayList in the geofences Map serves as keys for the reminders Map
    private Map<Integer, Reminder> reminders;
    private Map<Geofence, ArrayList<Integer>> geofences;

    private ReminderManager() {
        reminders = new HashMap<>();
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
        int pos = getReminderSize();
        reminders.put(pos, reminder);
        if(reminder.hasLocation()) {
            ArrayList<Integer> list;
            for (Geofence g : geofences.keySet()) {
                if (g.getRequestId().equals(reminder.getAddress())) {
                    // geofence already present
                    fence = g;
                    list = geofences.get(g);
                    list.add(pos);
                    exists = true;
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

                list = new ArrayList<>();
                list.add(pos);
                geofences.put(fence, list);
                Log.d(TAG, "geofence created");
            }
        }
        Log.d(TAG, "Reminder added: " + reminder);
        Log.d(TAG, "Current reminder map");
        for (int i : reminders.keySet()) {
            Log.d(TAG, i + " " + reminders.get(i));
        }
        return fence;
    }

    public ArrayList<Reminder> getReminders() {
        return new ArrayList<>(reminders.values());
    }

    public int getReminderSize() {
        return reminders.size();
    }

    public void setContext(Context c) {
        context = c;
    }
}
