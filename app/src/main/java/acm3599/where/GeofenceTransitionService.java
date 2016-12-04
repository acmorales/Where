package acm3599.where;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by Andrew on 11/30/2016.
 */

public class GeofenceTransitionService extends IntentService {

    String TAG = "TransitionService";

    public GeofenceTransitionService() {
        super("GeofenceTransitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "handling transition");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error: " + geofencingEvent.getErrorCode());
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Geofence fence;
            List<Reminder> list;
            for(int i = 0; i < triggeringGeofences.size(); i++) {
                fence = triggeringGeofences.get(i);
                list = ReminderManager.getInstance().getReminders();
                for(int j = 0; j < list.size(); j++) {
                    showNotification(list.get(j));
                }
            }
        }
    }

    public void showNotification(Reminder reminder) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(reminder.getTitle())
                .setContentText(reminder.getContent())
                .setSmallIcon(R.drawable.ic_gps_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_gps_large));

        Intent notifyIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notifyIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(notificationPendingIntent);
        notificationBuilder.setAutoCancel(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notificationBuilder.build());
    }
}
