package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.app.AlertDialog;
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
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.HololensClient;
import com.manhattan.blueprint.Model.Location;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Managers.LoginManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.LocationUtils;
import com.manhattan.blueprint.Utils.NetworkUtils;
import com.manhattan.blueprint.Utils.ViewUtils;

import android.support.design.widget.*;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapViewActivity extends FragmentActivity implements OnMapReadyCallback,
        BottomNavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    // Default to the VR Lab
    private final int DEFAULT_ZOOM = 18;
    private final int MAX_DISTANCE_REFRESH = 500;
    private final int MAX_DISTANCE_COLLECT = 20;
    // Times in MS
    private final int DESIRED_GPS_INTERVAL = 1000;
    private final int FASTEST_GPS_INTERVAL = 500;
    private final int NETWORK_CHECK_REFRESH = 10000;

    private BlueprintAPI blueprintAPI;
    private HololensClient hololensClient;
    private int hololensCounter;
    private ItemManager itemManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private BottomNavigationView bottomView;
    private Button developerModeButton;
    private GoogleMap googleMap;
    private HashMap<Marker, Resource> markerResourceMap = new HashMap<>();
    private LatLng lastLocationRequestedForResources;
    // Default to VR lab
    private LatLng currentLocation = new LatLng(51.449946, -2.599858);
    private boolean inDeveloperMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        bottomView = findViewById(R.id.bottom_menu);
        bottomView.setOnNavigationItemSelectedListener(this);

        developerModeButton = findViewById(R.id.developer_button);
        developerModeButton.setVisibility(View.GONE);
        developerModeButton.setOnClickListener(v -> {
            inDeveloperMode = !inDeveloperMode;
            if (inDeveloperMode) {
                developerModeButton.setBackground(new ColorDrawable(getColor(R.color.green)));
            } else {
                developerModeButton.setBackground(new ColorDrawable(getColor(R.color.red)));
            }
            onMapReady(googleMap);
        });
        developerModeButton.setBackground(new ColorDrawable(getColor(R.color.red)));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // If haven't logged in yet, or have revoked location, redirect
        PermissionManager locationManager = new PermissionManager(0, Manifest.permission.ACCESS_FINE_LOCATION);
        LoginManager loginManager = new LoginManager(this);
        if (!loginManager.isLoggedIn()) {
            toOnboarding();
            return;
        } else if (!locationManager.hasPermission(this)) {
            ViewUtils.showError(this,
                    getString(R.string.permission_location_title),
                    getString(R.string.permission_location_description),
                    (dialog, which) -> {
                        dialog.dismiss();
                        loginManager.logout();
                        toOnboarding();
                    });
            return;
        }

        configureNetworkChecker();

        // Load data required
        blueprintAPI = new BlueprintAPI(this);
        itemManager = ItemManager.getInstance(this);

        if (loginManager.isDeveloper()){
            developerModeButton.setVisibility(View.VISIBLE);
        }

        itemManager.fetchData(new APICallback<Void>() {
            @Override
            public void success(Void response) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapViewActivity.this);
            }

            @Override
            public void failure(int code, String error) {
                ViewUtils.showError(MapViewActivity.this, "Whoops! Could not fetch resource schema", error);
            }
        });

        hololensClient = new HololensClient(getApplicationContext());
        hololensCounter = 0;
        BlueprintDAO.getInstance(this).getSession().ifPresent(session -> {
            hololensClient.run(session.hololensIP);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LocationUtils.isLocationEnabled(this)) {
            displayLocationServicesRequest();
        }
    }

    private void configureNetworkChecker() {
        // Periodically check network status
        NetworkUtils.CheckNetworkConnectionThread connectionThread = new NetworkUtils.CheckNetworkConnectionThread(this);
        connectionThread.setCallback(value ->
                MapViewActivity.this.runOnUiThread(() -> {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapViewActivity.this);
                    alertDialog.setTitle(getString(R.string.no_network_title));
                    alertDialog.setMessage(getString(R.string.no_network_description));
                    alertDialog.setPositiveButton(getString(R.string.no_network_positive_response), (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                        connectionThread.canContinue(true);
                    });
                    alertDialog.setNegativeButton(getString(R.string.negative_response), (dialog, which) -> {
                        dialog.cancel();
                        connectionThread.canContinue(true);
                    });
                    alertDialog.show();
                }));
        Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(connectionThread, 0, NETWORK_CHECK_REFRESH, TimeUnit.MILLISECONDS);
    }

    private void toOnboarding() {
        Intent intent = new Intent(MapViewActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    //region OnMapReadyCallback
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Configure UI
        googleMap.setMyLocationEnabled(true);
        // Only enable gestures if developer
        googleMap.getUiSettings().setMyLocationButtonEnabled(inDeveloperMode);
        googleMap.getUiSettings().setAllGesturesEnabled(inDeveloperMode);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Get user's location
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(DESIRED_GPS_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_GPS_INTERVAL);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                // Don't move if in developer mode - may have panned to another location
                if (!inDeveloperMode) googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, googleMap.getCameraPosition().zoom));
                addResourcesIfNeeded(false);
            }
        }, Looper.myLooper());

        // Configure Developer Mode
        if (inDeveloperMode) {
            googleMap.setOnMapLongClickListener(this::displayAddResourceDialog);
        } else {
            googleMap.setOnMapLongClickListener(null);
        }

        // Move to default location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
        addResourcesIfNeeded(false);
    }
    //endregion

    private void addResourcesIfNeeded(boolean force) {
        // Request resources if we've moved more than max distance, or is first run
        if (lastLocationRequestedForResources != null &&
                LocationUtils.distanceBetween(lastLocationRequestedForResources, currentLocation) < MAX_DISTANCE_REFRESH && !force) {
            return;
        }

        lastLocationRequestedForResources = currentLocation;

        blueprintAPI.makeRequest(blueprintAPI.resourceService.fetchResources(
                currentLocation.latitude, currentLocation.longitude), new APICallback<ResourceSet>() {
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
                ViewUtils.showError(MapViewActivity.this, "Whoops! Could not fetch available resources", error);
            }
        });
    }

    // region OnMarkerClickListener
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Resource resource = markerResourceMap.get(marker);
        marker.showInfoWindow();

        // Developers delete instead of AR
        if (inDeveloperMode) {
            new AlertDialog.Builder(this)
                    .setTitle(String.format(getString(R.string.developer_delete_resource),
                            resource.getQuantity(),
                            ItemManager.getInstance(this).getName(resource.getId()).getWithDefault("items")))
                    .setPositiveButton(getString(R.string.positive_response), (dialog, which) -> {
                        dialog.dismiss();
                        deleteResource(resource);
                    })
                    .setNegativeButton(getString(R.string.negative_response), null)
                    .show();

        // Only collect if close enough
        } else if (LocationUtils.distanceBetween(marker.getPosition(), currentLocation) <= MAX_DISTANCE_COLLECT) {
            BlueprintDAO.getInstance(this).getSession().ifPresent(session -> {
                if (session.isHololensConnected()) {
                    // Connect to Hololens
                    hololensClient.addItem(resource.getId(), resource.getQuantity(), hololensCounter++);
                } else {
                    // Go to AR View
                    Intent intentAR = new Intent(MapViewActivity.this, ARActivity.class);
                    Bundle resourceToCollect = new Bundle();
                    resourceToCollect.putString("resource", (new Gson()).toJson(resource));
                    intentAR.putExtras(resourceToCollect);
                    startActivity(intentAR);
                }
            });
        }
        return true;
    }
    // endregion

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

    private void displayAddResourceDialog(LatLng latlng){
        // Configure Spinner
        View view = getLayoutInflater().inflate(R.layout.alert_add_resource, null);
        Spinner resourceNameSpinner = view.findViewById(R.id.resource_name_spinner);
        Spinner resourceQuantitySpinner = view.findViewById(R.id.resource_quantity_spinner);


        ItemManager itemManager = ItemManager.getInstance(this);

        // Data for resources
        ArrayList<Integer> quantities = new ArrayList<>();
        for (int i = 0; i < itemManager.getNames().size(); i++){
            quantities.add(i + 1);
        }

        // Creating adapter for spinners
        resourceNameSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new ArrayList<>(itemManager.getNames())));
        resourceQuantitySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                quantities));

        new AlertDialog.Builder(this)
            .setTitle("Add Resource")
            .setView(view)
            .setPositiveButton(R.string.positive_response, (dialog, which) -> {
                dialog.dismiss();

                int resourceId = itemManager.getId((String) resourceNameSpinner.getSelectedItem()).getWithDefault(0);
                Location location = new Location(latlng.latitude, latlng.longitude);
                int quantity = quantities.get(resourceQuantitySpinner.getSelectedItemPosition());

                addResource(new Resource(resourceId, location, quantity));
            })
            .show();
    }

    // Developer mode functions
    private void addResource(Resource resource) {
        blueprintAPI.makeRequest(blueprintAPI.resourceService.addResources(new ResourceSet(resource)), new APICallback<Void>() {
            @Override
            public void success(Void response) {
                addResourcesIfNeeded(true);
            }

            @Override
            public void failure(int code, String error) {
                ViewUtils.showError(MapViewActivity.this, "Error adding resource", error);
            }
        });
    }

    private void deleteResource(Resource resource) {
        blueprintAPI.makeRequest(blueprintAPI.resourceService.deleteResources(new ResourceSet(resource)), new APICallback<Void>() {
            @Override
            public void success(Void response) {
                addResourcesIfNeeded(true);
            }

            @Override
            public void failure(int code, String error) {
                ViewUtils.showError(MapViewActivity.this, "Couldn't delete resource", error);
            }
        });
    }

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
                Intent toSettings = new Intent(MapViewActivity.this, SettingsActivity.class);
                startActivity(toSettings);
                break;
        }
        return true;
    }
}
