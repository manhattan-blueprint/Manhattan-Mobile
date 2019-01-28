package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.icu.util.TimeUnit;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.manhattan.blueprint.BuildConfig;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Managers.LoginManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.R;

import android.support.design.widget.*;
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

public class MapViewActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        MapboxMap.OnMarkerClickListener,
        MapboxMap.OnScaleListener,
        BottomNavigationView.OnNavigationItemSelectedListener {
    private MapView mapView;
    private MapboxMap mapboxMap;
    private BlueprintAPI blueprintAPI;
    private ItemManager itemManager;

    private BottomNavigationView bottomView;
    HashMap<Marker, Resource> markerResourceMap = new HashMap<>();

    // Camera configuration
    private int minZoom = 17;
    private int maxZoom = 20;
    private int minTilt = 40;
    private int maxTilt = 60;

    CheckNetworkConnectionThread checkNetworkConnectionThread;

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

        @Override
        public void run() {
            if (!isNetworkConnected() && !insideDialog) {
                insideDialog = true;

                MapViewActivity.this.runOnUiThread(() -> {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapViewActivity.this);
                    alertDialog.setTitle("No Network Connection");
                    alertDialog.setMessage("Internet not available, to continue please turn on wi-fi.");
                    alertDialog.setPositiveButton("Enable wi-fi", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                        insideDialog = false;
                    });
                    alertDialog.setNegativeButton("No thanks", (dialog, which) ->  {
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

        mapView = findViewById(R.id.mapView);
        Mapbox.getInstance(this, BuildConfig.MapboxAPIKey);
        mapView.onCreate(savedInstanceState);

        // If haven't logged in yet, or have revoked location, redirect
        PermissionManager locationManager = new PermissionManager(0, Manifest.permission.ACCESS_FINE_LOCATION);
        LoginManager loginManager = new LoginManager(this);
        if (!loginManager.isLoggedIn()) {
            toOnboarding();
            return;
        } else if (!locationManager.hasPermission(this)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapViewActivity.this);
            dialog.setTitle(getString(R.string.permission_location_title));
            dialog.setMessage(getString(R.string.permission_location_description));
            dialog.setPositiveButton(getString(R.string.positive_response), (d, which) -> {
                d.dismiss();
                loginManager.logout();
                toOnboarding();
            });
            dialog.create().show();
            return;
        }

        // Periodically check network status
        int connectionRefreshDelay = 10;
        checkNetworkConnectionThread = new CheckNetworkConnectionThread();
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2);
        executor.scheduleWithFixedDelay(checkNetworkConnectionThread, 0, connectionRefreshDelay, java.util.concurrent.TimeUnit.SECONDS);

        // Load data required
        blueprintAPI = new BlueprintAPI(this);
        itemManager = ItemManager.getInstance(this);
        itemManager.fetchData(new APICallback<Void>() {
            @Override
            public void success(Void response) {
                mapView.getMapAsync(MapViewActivity.this);
            }

            @Override
            public void failure(int code, String error) {
                new AlertDialog
                        .Builder(MapViewActivity.this, android.R.style.Theme_Material_Dialog_Alert)
                        .setTitle("Whoops! Could not fetch resource schema")
                        .setMessage(error)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    private void toOnboarding() {
        Intent intent = new Intent(MapViewActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    //region OnMapReadyCallback
    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        // Map Style from URL
        mapboxMap.setStyle(getString(R.string.mapbox_map_style));

        // Configure UI
        mapboxMap.getUiSettings().setCompassEnabled(false);
        mapboxMap.getUiSettings().setDoubleTapGesturesEnabled(false);
        mapboxMap.getUiSettings().setScrollGesturesEnabled(false);

        // Default Camera Position
        mapboxMap.animateCamera(CameraUpdateFactory.tiltTo(minTilt));

        // Action listeners
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.addOnScaleListener(this);

        // Location tracking
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        // Only allow certain zoom options
        LocationComponentOptions options = LocationComponentOptions
                .builder(this)
                .maxZoom(maxZoom)
                .minZoom(minZoom)
                .build();
        locationComponent.activateLocationComponent(this, options);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.GPS);
        locationComponent.setCameraMode(CameraMode.TRACKING);

        // Add resources to map for their location
        addResources(mapboxMap.getLocationComponent().getLastKnownLocation());
    }
    //endregion

    @Override
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

    private void addResources(android.location.Location location) {
        Location blueprintLocation = new Location(location);

        blueprintAPI.makeRequest(
                blueprintAPI.resourceService.fetchResources(blueprintLocation.getLatitude(),
                        blueprintLocation.getLongitude()),
                new APICallback<ResourceSet>() {
            @Override
            public void success(ResourceSet response) {
                if (response.getItems() == null) return;

                for (Resource item : response.getItems()) {
                    LatLng latLng = new LatLng(item.getLocation().getLatitude(),
                            item.getLocation().getLongitude());
                    IconFactory iconFactory = IconFactory.getInstance(MapViewActivity.this);
                    Marker marker = mapboxMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(itemManager.getName(item.getId()).getWithDefault("Item " + item.getId()))
                            .icon(iconFactory.fromResource(R.drawable.resource_default)));
                    markerResourceMap.put(marker, item);
                }
            }

            @Override
            public void failure(int code, String error) {
                new AlertDialog
                        .Builder(MapViewActivity.this, android.R.style.Theme_Material_Dialog_Alert)
                        .setTitle("Whoops! Could not fetch available resources.")
                        .setMessage(error)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
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

    public  boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);

        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()   == NetworkInfo.State.CONNECTED);
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

    // region OnScaleListener
    @Override
    public void onScaleBegin(@NonNull StandardScaleGestureDetector detector) {
    }

    @Override
    public void onScale(@NonNull StandardScaleGestureDetector detector) {
        double zoom = mapboxMap.getCameraPosition().zoom;
        double fractionZoomed = (zoom - minZoom) / (maxZoom - minZoom);
        double cameraTilt = (maxTilt - minTilt) * fractionZoomed + minTilt;
        mapboxMap.animateCamera(CameraUpdateFactory.tiltTo(cameraTilt));
    }

    @Override
    public void onScaleEnd(@NonNull StandardScaleGestureDetector detector) {
    }
    // endregion

    // region Mapbox overrides
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (!isLocationEnabled()) {
            displayLocationServicesRequest();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    // endregion
}
