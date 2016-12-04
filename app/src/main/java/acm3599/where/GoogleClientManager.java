package acm3599.where;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

// singleton to contain GoogleApiClient
public class GoogleClientManager {
    final private String TAG = "GoogleClientManager";
    private static GoogleClientManager manager;
    private GoogleApiClient  client;
    private Context context;

    public GoogleClientManager() {
        // intentionally left blank
    }

    public static GoogleClientManager getInstance() {
        if(manager == null) {
            manager = new GoogleClientManager();
        }
        return manager;
    }

    public GoogleApiClient getClient(Context c) {
        context = c;
        if(client == null) {
            client = new GoogleApiClient.Builder(context)
                    .enableAutoManage((FragmentActivity) context,
                            new GoogleApiClient.OnConnectionFailedListener() {

                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.d(TAG, "Google Api Client connection failed");
                        }
                    })
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Log.d(TAG, "Google Api Client connected");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(TAG, "Google Api Client suspended");
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }
        return client;
    }
}
