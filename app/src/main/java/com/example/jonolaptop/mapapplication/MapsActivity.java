package com.example.jonolaptop.mapapplication;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnMarkerClickListener,
        com.google.android.gms.location.LocationListener {

    public static final int REQUEST_LOCATION = 1;
    private boolean followingUserLocation = true;
    private boolean mapReady = false;
    private GoogleMap mMap;
    private Marker userMarker;
    private Location mLastLocation;
    private LocationManager locationManager;
    private boolean connected = false;
    private GoogleApiClient client;
    private Map<Marker, Integer> markers = new HashMap<>();

    /*
    protected void onCreate(Bundle savedInstanceState)
    Runs when activity is created
    Sets up mapFragment, toolbar, and GoogleApiClient client
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up toolbar -- get reference, then set it to be the action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }

    /*
    public boolean onCreateOptionsMenu(Menu menu)
    Inflates the menu with the contents of map_menu when activity is loaded
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    /*
    public boolean onOptionsItemSelected(MenuItem item)
    Handles something being selected in the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent aboutIntent = new Intent(MapsActivity.this, AboutActivity.class);
                MapsActivity.this.startActivity(aboutIntent);
                return true;

            case R.id.action_favorite:
                //Focus camera on user location
                followingUserLocation = true;
                safeLocationUpdate();
                // User chose the "Add Marker" item, add a new location somewhere
                Intent myIntent = new Intent(MapsActivity.this, CreateActivity.class);
                //Start activity to create new marker, sending intent with location
                myIntent.putExtra("lat", mLastLocation.getLatitude());
                myIntent.putExtra("lng", mLastLocation.getLongitude());
                MapsActivity.this.startActivity(myIntent);
                return true;

            case R.id.action_here:
                // User chose the "Current Location" action, jump to current location
                // and start following as user moves around
                followingUserLocation = true;
                safeLocationUpdate();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /*
    public void onMapReady(GoogleMap googleMap)
    This is called back when the map is ready to go.
    Sets mMap.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMarkerClickListener(this);
        populateMarkers();
    }

    /*
    public void onStart()
    Starts the client and attempts to connect.
     */
    @Override
    public void onStart() {
        super.onStart();
        onResume();
    }

    /*
    public void onResume()
    Starts client and attempts to connect.
     */
    @Override
    public void onResume() {
        super.onResume();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.jonolaptop.mapapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
        if (mMap!= null&& mapReady) {
            populateMarkers();
        }

    }

    /*
    public void onPause()
    Disconnects client and stops location updates for now.
     */
    @Override
    public void onPause() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, (LocationListener)this);
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.jonolaptop.mapapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
        super.onPause();
    }

    /*
    public void onStop()
    Disconnects client and stops location updates for now.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /*
    public void onConnected(Bundle connectionHint)
    Called when connection is established.
    Used here to make sure we know we've got a connection.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        connected = true;
        requestUpdates();
    }

    /*
    public void onConnectionSuspended(int cause)
    Called when connection is suspended.
    Used here to make sure we know we've lost the connection.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        connected = false;
    }

    /*
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    Called when permission result comes in.
    If we don't get permission for fine location, we send a message to that effect.
    If we do get permission, we request location updates.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                    requestUpdates();
            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(getApplicationContext(), "DEBUG: Location unavailable: Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    public void moveCameraToUser()
    Animates the camera to the user's most recent position.
     */
    public void moveCameraToUser() {
        // If we got a location, then move the camera
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), (float) 14.0));
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "DEBUG: Null location", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    public void requestUpdates()
    If we have permission for fine location, we request for location updates.
    Otherwise we check for permission; onRequestPermissionsResult will get called back when we get an answer,
    and call this method again if permission was granted.
     */
    public void requestUpdates() {
        // Do a real-time permission check for fine location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            request.setInterval(10000);
            LocationServices.FusedLocationApi.requestLocationUpdates(client,request,(LocationListener)this);
        }
    }

    /*
    public void onLocationChanged(Location location)
    Called when LocationListener gets an update (after requestUpdates has been called).
    Calls safeLocationUpdate.
     */
    @Override
    public void onLocationChanged(Location location) {
        safeLocationUpdate();
    }

    /*
      public void onCameraMoveStarted(int reason)
      Called back when camera moves. We then limit our response to movements
      called by user gestures. We respond by turning off camera following.
     */
    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            followingUserLocation = false;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            ;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            ;
        }
    }

    /*
    public boolean safeLocationUpdate()
    Ensure we're connected and still have permission, and if so update mLastLocation and user marker.
    If followingUserLocation is set, camera moves too.
     */
    public boolean safeLocationUpdate() {
        if (connected) { // If we're connected to google play services

            // Update last known location
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            }
            catch (SecurityException e) {
                Toast.makeText(getApplicationContext(), "DEBUG: Location unavailable: Permission denied", Toast.LENGTH_SHORT).show();
            }

            try {
                // If usermarker hasn't been initialized, please do
                if (userMarker == null) {
                    userMarker = mMap.addMarker(new MarkerOptions()
                            .zIndex(1)
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                } else {
                    // If it's initialized, update its position
                    userMarker.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }
                // If we're following the user's marker, move the camera too
                if (followingUserLocation) {
                    moveCameraToUser();
                }

                return true;
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "DEBUG: Location not available", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "DEBUG: Not connected", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Populates all report markers from db on map screen
    public void populateMarkers() {

        //Change thread policy to make things simple
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {

            //Prepare to connect
            String urlString = "http://csis.svsu.edu/~jmbenso2/cs403/getall.php";

            //Get url and connect
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            //Read
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // We're going to build a string from the recieved data line by line
            StringBuilder builder = new StringBuilder();
            String oneLine = null;
            while((oneLine = reader.readLine()) != null) {
                builder.append(oneLine);
            }

            String resultString = builder.toString();
            int responseCode = connection.getResponseCode();

            markers.clear();
            mMap.clear(); // Remove old markers
            // Replace user marker
            if (mLastLocation !=null)
                userMarker = mMap.addMarker(new MarkerOptions()
                        .zIndex(1)
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));


            // Break apart what we got back, turn into marker
            String[] results = resultString.split("#");
            for(int i = 1; i < results.length; i++) {

                String[] words = results[i].split("~");

                Marker newMarker = mMap.addMarker(new MarkerOptions()
                        .zIndex(10)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_accessibility_black_24dp))
                        .position(new LatLng(Double.parseDouble(words[2]),Double.parseDouble(words[3])))
                        .alpha(Float.parseFloat(words[4]) * (float)0.2)
                        .title(words[1]));
                newMarker.setTag(Integer.parseInt(words[4]));
                markers.put(newMarker,Integer.parseInt(words[0]));

            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "DEBUG:" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent myIntent = new Intent(MapsActivity.this, ReadActivity.class);
        //Start activity to read marker, sending data to it
        myIntent.putExtra("tuid",markers.get(marker));
        myIntent.putExtra("description",marker.getTitle());
        myIntent.putExtra("lat", marker.getPosition().latitude);
        myIntent.putExtra("lng", marker.getPosition().longitude);
        myIntent.putExtra("hp", (Integer)(marker.getTag()));
        MapsActivity.this.startActivity(myIntent);
        return true;
    }
}
