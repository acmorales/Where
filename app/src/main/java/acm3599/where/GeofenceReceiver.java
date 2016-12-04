package acm3599.where;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by Andrew on 12/4/2016.
 */

public class GeofenceReceiver extends BroadcastReceiver {

    private String TAG = "GeofenceReceiver";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory("com.survey.android.geofence.CATEGORY_LOCATION_SERVICES");

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
                list = ReminderManager.getInstance().getReminders(fence);
                for(int j = 0; j < list.size(); j++) {
                    showNotification(list.get(j), (1 * j));
                }
            }
        }

        broadcastIntent.setAction("com.survey.android.geofence.ACTION_GEOFENCE_TRANSITION");
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    public void showNotification(Reminder reminder, int tag) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(reminder.getTitle())
                .setContentText(reminder.getContent())
                .setSmallIcon(R.drawable.ic_gps_small)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_gps_large));

        Intent notifyIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notifyIntent);

        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(notificationPendingIntent);
        notificationBuilder.setAutoCancel(true);
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(tag, notificationBuilder.build());
    }
}
