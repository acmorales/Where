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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        ResultCallback<Status>, AddReminderFragment.OnSubmitCallback,
        NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener {

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

        // sets main activity to listen to fragment back stack changes
        getSupportFragmentManager().addOnBackStackChangedListener(this);

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
        Marker m = gMap.addMarker(new MarkerOptions().position(loc));
        m.setTag(reminder.getAddress());
        gMap.addCircle(new CircleOptions()
                .center(loc)
                .radius(30)
                .strokeColor(0xff00796B)
                .strokeWidth((float) 5)
                .fillColor(0x50009688));

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, ZOOM_LEVEL));
    }

    public void updateFences(Geofence geofence) {
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent("GeofenceReceiver.class");
        intent.setClass(this, GeofenceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.GeofencingApi.addGeofences(
                    client,
                    request,
                    pendingIntent
            ).setResultCallback(this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        Log.d(TAG, "registered geofence");
    }

    public void addReminder() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        getSupportFragmentManager().beginTransaction()
                .detach(f)
                .add(R.id.main_fragment, new AddReminderFragment(), "addReminderFrag")
                .addToBackStack("")
                .commit();
    }

    public void reattachMap() {
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction()
                .attach(mapFrag)
                .commit();
    }

    public void viewReminders() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
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
                    Log.d(TAG, "too many geofences");
                    break;
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    Log.d(TAG, "too many pending intents");
                    break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomGesturesEnabled(true);
        gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Log.d(TAG, "info window");
                View v = getLayoutInflater().inflate(R.layout.info_window, null);
                RecyclerView rView = (RecyclerView) v.findViewById(R.id.info_list);

                ArrayList<Reminder> list = ReminderManager.getInstance().getReminders();
                RecyclerView.LayoutManager layout = new LinearLayoutManager(getApplicationContext());
                InfoAdapter adapter = new InfoAdapter(list);
                rView.setLayoutManager(layout);
                rView.setAdapter(adapter);
                return v;
            }
        });
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

    @Override
    public void onBackStackChanged() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if(f instanceof SupportMapFragment) {
            addReminderBut.setVisibility(View.VISIBLE);
        } else {
            addReminderBut.setVisibility(View.GONE);
        }
    }
}
