package acm3599.where;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ResultCallback<Status>,
        NavigationView.OnNavigationItemSelectedListener, AddReminderFragment.OnSubmitCallback {

    private String TAG = "MainMapActivity";
    private final long ZOOM_LEVEL = (long) 17.5;
    private int currentFrag;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    FloatingActionButton addReminderBut;
    private GoogleApiClient client;
    private GoogleMap gMap;
    private SupportMapFragment mapFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sets toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // inits navigation drawer and toggle
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // set listener for navigation drawer
        NavigationView navView = (NavigationView) findViewById(R.id.nav_drawer);
        navView.setNavigationItemSelectedListener(this);

        // inits GoogleApiClient
        client = GoogleClientManager.getInstance().getClient(this);

        // inits map and starts fragment
        mapFrag = new SupportMapFragment();
        mapFrag.getMapAsync(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_fragment, mapFrag, "mapFrag")
                .commit();

        // sets onClickListener for action button
        addReminderBut = (FloatingActionButton) findViewById(R.id.fab);
        addReminderBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReminder();
            }
        });
    }

    public void submit(Reminder reminder) {
        Log.d(TAG, "Reminder received: " + reminder);

        // closes AddReminderFragment and reattaches mapFrag
        reattachMap();

        // minimizes keyboard
        // found on http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        ReminderManager.getInstance().setContext(this);
        Geofence geofence = ReminderManager.getInstance().add(reminder);
        if(reminder.hasLocation()) {
            markReminder(reminder);
            updateFences(geofence);
        }
    }

    public void markReminder(Reminder reminder) {
            LatLng loc = reminder.getLatLng();
            gMap.addMarker(new MarkerOptions().position(loc));
            gMap.addCircle(new CircleOptions()
                    .center(loc)
                    .radius(30)
                    .strokeColor(Color.CYAN)
                    .strokeWidth((float) 5)
                    .fillColor(0x5000ffff));

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, ZOOM_LEVEL));
    }

    public void updateFences(Geofence geofence) {
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(this, GeofenceTransitionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.GeofencingApi.addGeofences(
                    client,
                    request,
                    pendingIntent
            ).setResultCallback(this);

            Log.d(TAG, "updating fences");
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void addReminder() {
        addReminderBut.setVisibility(View.GONE);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        Log.d(TAG, "detaching " + f.getTag());
        getSupportFragmentManager().beginTransaction()
                .detach(f)
                .add(R.id.main_fragment, new AddReminderFragment(), "addReminderFrag")
                .addToBackStack("")
                .commit();
    }

    public void reattachMap() {
        addReminderBut.setVisibility(View.VISIBLE);
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction()
                .attach(mapFrag)
                .commit();
    }

    public void viewReminders() {
        addReminderBut.setVisibility(View.GONE);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        Log.d(TAG, "detaching " + f.getTag());
        getSupportFragmentManager().beginTransaction()
                .detach(f)
                .add(R.id.main_fragment, new RemindersFragment(), "remindersFrag")
                .addToBackStack("")
                .commit();
    }


    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "geofence added");
        } else {
            switch(status.getStatusCode()) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    Log.d(TAG, "geofence not available");
                    break;
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    Log.d(TAG, "geofence not available");
                    break;
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    Log.d(TAG, "geofence not available");
                    break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            gMap.setMyLocationEnabled(true);
            Location location = LocationServices.FusedLocationApi.getLastLocation(client);

            // focus current location on open
            if(location != null) {
                LatLng lastLoc = new LatLng(location.getLatitude(), location.getLongitude());
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLoc, ZOOM_LEVEL));
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "menu item clicked");
        switch(id) {
            case R.id.nav_home:
                reattachMap();
                break;
            case R.id.nav_reminders:
                viewReminders();
                break;
            case R.id.nav_add:
                addReminder();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
