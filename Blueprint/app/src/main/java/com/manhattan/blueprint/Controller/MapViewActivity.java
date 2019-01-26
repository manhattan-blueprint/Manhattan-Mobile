package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Location;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Managers.LoginManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.R;

import android.support.design.widget.*;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.mapbox.android.gestures.StandardScaleGestureDetector;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class MapViewActivity extends FragmentActivity implements OnMapReadyCallback,
        BottomNavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    // Default to the VR Lab
    private final int DEFAULT_ZOOM = 18;
    private final LatLng defaultLocation = new LatLng(51.449946, -2.599858);

    private BlueprintAPI blueprintAPI;
    private ItemManager itemManager;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private BottomNavigationView bottomView;
    private GoogleMap googleMap;
    HashMap<Marker, Resource> markerResourceMap = new HashMap<>();

    class CheckNetworkConnectionThread extends Thread {

        boolean threadRunning;
        boolean insideDialog;

        private CheckNetworkConnectionThread() {
            threadRunning = true;
            insideDialog = false;
        }

        private void onStop() {
            threadRunning = false;
        }

        private  boolean isNetworkConnected() {
            ConnectivityManager connectivityManager = (ConnectivityManager) MapViewActivity.this.getSystemService(MapViewActivity.this.CONNECTIVITY_SERVICE);

            return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED    ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()   == NetworkInfo.State.CONNECTING ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()   == NetworkInfo.State.CONNECTED    ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()   == NetworkInfo.State.CONNECTING);
        }

        @Override
        public void run() {
            if (!isNetworkConnected() && !insideDialog) {
                insideDialog = true;

                MapViewActivity.this.runOnUiThread(() -> {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapViewActivity.this);
                    alertDialog.setTitle(getString(R.string.no_network_title));
                    alertDialog.setMessage(getString(R.string.no_network_description));
                    alertDialog.setPositiveButton(getString(R.string.no_network_positive_response), (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                        insideDialog = false;
                    });
                    alertDialog.setNegativeButton(getString(R.string.negative_response), (dialog, which) ->  {
                        dialog.cancel();
                        insideDialog = false;
                    });
                    alertDialog.show();
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        bottomView = findViewById(R.id.bottom_menu);
        bottomView.setOnNavigationItemSelectedListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // If haven't logged in yet, or have revoked location, redirect
        PermissionManager locationManager = new PermissionManager(0, Manifest.permission.ACCESS_FINE_LOCATION);
        LoginManager loginManager = new LoginManager(this);
        if (!loginManager.isLoggedIn()) {
            toOnboarding();
            return;
        } else if (!locationManager.hasPermission(this)) {
            new AlertDialog.Builder(MapViewActivity.this)
                    .setTitle("Location required")
                    .setMessage("Please grant access to your location so Blueprint can show resources around you.")
                    .setPositiveButton("Ok", (d, which) -> {
                        d.dismiss();
                        loginManager.logout();
                        toOnboarding();
                    })
                    .create().show();
            return;
        }

        // Periodically check network status
        int connectionRefreshDelay = 10; // seconds
        CheckNetworkConnectionThread checkNetworkConnectionThread = new CheckNetworkConnectionThread();
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2);
        executor.scheduleWithFixedDelay(checkNetworkConnectionThread, 0, connectionRefreshDelay, java.util.concurrent.TimeUnit.SECONDS);

        // Load data required
        blueprintAPI = new BlueprintAPI(this);
        itemManager = ItemManager.getInstance(this);
        itemManager.fetchData(new APICallback<Void>() {
            @Override
            public void success(Void response) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapViewActivity.this);
            }

            @Override
            public void failure(int code, String error) {
                showError("Whoops! Could not fetch resource schema", error);
            }
        });
    }

    private void toOnboarding() {
        Intent intent = new Intent(MapViewActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    //region OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Configure UI
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnMarkerClickListener(this);

        // Get user's location
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(2000);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LatLng location = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, googleMap.getCameraPosition().zoom));
                // TODO: Only request resources if location has significantly changed
                addResources(location);
            }
        }, Looper.myLooper());

        // Move to default location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
        addResources(defaultLocation);
    }
    //endregion

    private void addResources(android.location.Location location) {
        Location blueprintLocation = new Location(location);
        blueprintAPI.makeRequest(blueprintAPI.resourceService.fetchResources(), new APICallback<ResourceSet>() {
            @Override
            public void success(ResourceSet response) {
                markerResourceMap.forEach((marker, resource) -> marker.remove());
                markerResourceMap.clear();

                for (Resource item : response.getItems()) {
                    LatLng itemLocation = new LatLng(item.getLocation().getLatitude(),
                            item.getLocation().getLongitude());
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .title(itemManager.getName(item.getId()).getWithDefault("Item " + item.getId()))
                            .position(itemLocation));
                    markerResourceMap.put(marker, item);
                }
            }

            @Override
            public void failure(int code, String error) {
                showError("Whoops! Could not fetch available resources", error);
            }
        });
    }

    private boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void displayLocationServicesRequest() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(getString(R.string.enable_location_title));
        alertDialog.setMessage(getString(R.string.enable_location_description));
        alertDialog.setPositiveButton(getString(R.string.enable_location_positive_response), (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        alertDialog.setNegativeButton(getString(R.string.negative_response), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    // region OnMarkerClickListener
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Intent intentAR = new Intent(MapViewActivity.this, ARActivity.class);
        Bundle resourceToCollect = new Bundle();
        resourceToCollect.putString("resource", (new Gson()).toJson(markerResourceMap.get(marker)));
        intentAR.putExtras(resourceToCollect);
        startActivity(intentAR);
        return false;
    }
    // endregion

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        for (int i = 0; i < bottomView.getMenu().size(); i++) {
            MenuItem menuItem = bottomView.getMenu().getItem(i);
            boolean isChecked = menuItem.getItemId() == item.getItemId();
            menuItem.setChecked(isChecked);
        }

        switch (item.getItemId()) {
            case R.id.inventory:
                Intent toInventory = new Intent(MapViewActivity.this, InventoryActivity.class);
                startActivity(toInventory);
                break;
            case R.id.shopping_list:
                break;
            case R.id.settings:
                break;
        }
        return true;
    }

    private void showError(String title, String message) {
        new AlertDialog
                .Builder(MapViewActivity.this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.ok, null)
                .show();

    }
}
