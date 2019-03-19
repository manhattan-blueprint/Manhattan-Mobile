package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.manhattan.blueprint.BuildConfig;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.Location;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Managers.LoginManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.LocationUtils;
import com.manhattan.blueprint.Utils.NetworkUtils.CheckNetworkConnectionThread;
import com.manhattan.blueprint.Utils.ViewUtils;
import com.manhattan.blueprint.View.BackpackView;
import com.manhattan.blueprint.View.MapGestureListener;
import com.mapbox.android.gestures.AndroidGesturesManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMarkerClickListener, MapGestureListener.GestureDelegate {

    private enum MenuState {
        CLOSED, NORMAL, BACKPACK
    }

    private final int DEFAULT_ZOOM = 18;
    private final int MAX_REFRESH_DISTANCE = 500; // metres
    private final int MAX_COLLECT_DISTANCE = 20; // metres

    private final int DESIRED_GPS_INTERVAL = 1000; // ms
    private final int FASTEST_GPS_INTERVAL = 500; // ms
    private final int NETWORK_CHECK_REFRESH = 10000; // ms
    private final int MENU_ANIMATION_DURATION = 300; // ms

    private Button developerButton;
    private Button menuButton;
    private Button backpackButton;
    private Button settingsButton;
    private Button blueprintButton;
    private Button closeButton;
    private ImageView blurView;
    private ViewGroup viewGroup;
    private BackpackView backpackView;

    private BlueprintAPI blueprintAPI;
    private ItemManager itemManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LoginManager loginManager;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private HashMap<Marker, Resource> markerResourceMap = new HashMap<>();
    private LatLng lastResourceLocation;

    // Default to VR lab
    private LatLng currentLocation = new LatLng(51.449946, -2.599858);
    private Marker currentLocationMarker;
    private boolean inDeveloperMode = false;
    private MenuState menuState = MenuState.CLOSED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MapboxAPIKey);
        setContentView(R.layout.activity_map_view);

        menuButton = findViewById(R.id.menuButton);
        developerButton = findViewById(R.id.developerButton);
        backpackButton = findViewById(R.id.backpackButton);
        settingsButton = findViewById(R.id.settingsButton);
        blueprintButton = findViewById(R.id.blueprintButton);
        closeButton = findViewById(R.id.closeButton);
        blurView = findViewById(R.id.blurView);
        mapView = findViewById(R.id.mapView);
        viewGroup = findViewById(R.id.mapConstraintLayout);

        // Menu Buttons
        menuButton.setOnClickListener(menuButtonClickListener);
        closeButton.setOnClickListener(menuButtonClickListener);
        developerButton.setOnClickListener(developerButtonClickListener);
        settingsButton.setOnClickListener(settingsButtonClickListener);
        backpackButton.setOnClickListener(backpackButtonClickListener);
        blueprintButton.setOnClickListener(blueprintButtonClickListener);

        blurView.setOnClickListener(blurViewClickListener);
        blurView.setVisibility(View.INVISIBLE);

        viewGroup.post(() -> {
            backpackView = new BackpackView(MapViewActivity.this, viewGroup);
            updateInventory();
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        loginManager = new LoginManager(this);

        // If haven't logged in yet, or have revoked location, redirect
        PermissionManager locationManager = new PermissionManager(0, Manifest.permission.ACCESS_FINE_LOCATION);
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

        // Hide views as necessary
        if (!loginManager.isDeveloper()) {
            developerButton.setVisibility(View.GONE);
        }
        blurView.animate().alpha(0);
        closeButton.animate().scaleX(0).scaleY(0);

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
                ViewUtils.showError(MapViewActivity.this, "Whoops! Could not fetch resource schema", error);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LocationUtils.isLocationEnabled(this)) {
            displayLocationServicesRequest();
        }
        mapView.onResume();

    }

    private void updateInventory(){
        // Reload inventory
        blueprintAPI.makeRequest(blueprintAPI.inventoryService.fetchInventory(), new APICallback<Inventory>() {
            @Override
            public void success(Inventory response) {
                if (backpackView == null) return;
                backpackView.update(response);
            }

            @Override
            public void failure(int code, String error) {
                ViewUtils.showError(MapViewActivity.this, "Failed to retrieve inventory", error);
            }
        });
    }

    private void configureNetworkChecker() {
        // Periodically check network status
        CheckNetworkConnectionThread connectionThread = new CheckNetworkConnectionThread(this);
        connectionThread.setCallback(value ->
                MapViewActivity.this.runOnUiThread(() -> {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapViewActivity.this);
                    alertDialog.setTitle(getString(R.string.no_network_title));
                    alertDialog.setMessage(getString(R.string.no_network_description));
                    alertDialog.setPositiveButton(
                            getString(R.string.no_network_positive_response),
                            (dialog, which) -> {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                dialog.dismiss();
                                connectionThread.canContinue(true);
                            });
                    alertDialog.setNegativeButton(getString(R.string.negative_response), (dialog, which) -> {
                        dialog.cancel();
                        connectionThread.canContinue(true);
                    });
                    alertDialog.show();
                }));
        Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(
                connectionThread, 0, NETWORK_CHECK_REFRESH, TimeUnit.MILLISECONDS);
    }

    private void toOnboarding() {
        startActivity(new Intent(MapViewActivity.this, OnboardingActivity.class));
        finish();
    }

    //region OnMapReadyCallback
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.setOnMapLongClickListener(this.mapLongPressListener);

        // Only enable all gestures if developer
        if (inDeveloperMode) {
            mapboxMap.setGesturesManager(new AndroidGesturesManager(MapViewActivity.this), true, true);
            mapboxMap.getUiSettings().setAllGesturesEnabled(true);
            mapboxMap.addOnCameraMoveListener(() -> mapboxMap.getMarkers().forEach(Marker::hideInfoWindow));
            // Hide marker info when move screen
            mapboxMap.setStyle(getString(R.string.developer_map_style));
        } else {
            mapboxMap.getUiSettings().setAllGesturesEnabled(false);
            MapGestureListener mapGestureListener = new MapGestureListener(this);
            AndroidGesturesManager gesturesManager = mapboxMap.getGesturesManager();
            gesturesManager.setMoveGestureListener(mapGestureListener);
            gesturesManager.setStandardScaleGestureListener(mapGestureListener);

            mapboxMap.setStyle(getString(R.string.player_map_style));
        }

        // Get user's location
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(DESIRED_GPS_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_GPS_INTERVAL);
        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationDidUpdate(), Looper.myLooper());

        // Move to default location
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
        addResourcesIfNeeded(false);
    }

    private LocationCallback locationDidUpdate() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = new LatLng(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                // Add icon to map
                if (currentLocationMarker != null) {
                    currentLocationMarker.remove();
                }
                int manMarker = inDeveloperMode ? R.drawable.will : R.drawable.man;
                Icon icon = IconFactory.getInstance(MapViewActivity.this).fromResource(manMarker);
                MarkerOptions options = new MarkerOptions().setIcon(icon).position(currentLocation);

                currentLocationMarker = mapboxMap.addMarker(options);

                // Don't move if in developer mode - may have panned to another location
                if (!inDeveloperMode) {
                    mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            currentLocation, mapboxMap.getCameraPosition().zoom));
                }
                addResourcesIfNeeded(false);
            }
        };
    }

    //endregion

    private void addResourcesIfNeeded(boolean force) {
        // Request resources if we've moved more than max distance, or is first run
        if (!force && lastResourceLocation != null &&
                LocationUtils.distanceBetween(lastResourceLocation, currentLocation)
                        < MAX_REFRESH_DISTANCE) {
            return;
        }

        lastResourceLocation = currentLocation;

        blueprintAPI.makeRequest(blueprintAPI.resourceService.fetchResources(currentLocation.getLatitude(), currentLocation.getLongitude()),
                new APICallback<ResourceSet>() {
                    @Override
                    public void success(ResourceSet response) {
                        markerResourceMap.forEach((marker, resource) -> marker.remove());
                        markerResourceMap.clear();
                        for (Resource item : response.getItems()) {
                            LatLng itemLocation = new LatLng(item.getLocation().getLatitude(),
                                    item.getLocation().getLongitude());
                            Icon icon = IconFactory.getInstance(MapViewActivity.this).fromResource(R.drawable.sprite_default);
                            Marker marker = mapboxMap.addMarker(new MarkerOptions()
                                    .icon(icon)
                                    .title(itemManager.getName(item.getId()).withDefault("Item " + item.getId()))
                                    .position(itemLocation));
                            markerResourceMap.put(marker, item);
                        }
                    }

                    @Override
                    public void failure(int code, String error) {
                        ViewUtils.showError(MapViewActivity.this,
                                "Whoops! Could not fetch available resources", error);
                    }
                });
    }

    // region OnMarkerClickListener
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.equals(currentLocationMarker)) return false;

        Resource resource = markerResourceMap.get(marker);
        marker.showInfoWindow(mapboxMap, mapView);

        // Developers delete instead of AR
        if (inDeveloperMode) {
            new AlertDialog.Builder(this)
                    .setTitle(String.format(getString(R.string.developer_delete_resource),
                            resource.getQuantity(),
                            ItemManager.getInstance(this).getName(resource.getId()).withDefault("items")))
                    .setPositiveButton(getString(R.string.positive_response), (dialog, which) -> {
                        dialog.dismiss();
                        deleteResource(resource);
                    })
                    .setNegativeButton(getString(R.string.negative_response), null)
                    .show();

            // Only collect if close enough
        } else if (LocationUtils.distanceBetween(marker.getPosition(), currentLocation) <= MAX_COLLECT_DISTANCE) {
            Intent intentAR = new Intent(MapViewActivity.this, ARActivity.class);
            Bundle resourceToCollect = new Bundle();
            resourceToCollect.putString("resource", new Gson().toJson(resource));
            intentAR.putExtras(resourceToCollect);
            startActivity(intentAR);
        }
        return true;
    }
    // endregion

    private void displayLocationServicesRequest() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.enable_location_title));
        alertDialog.setMessage(getString(R.string.enable_location_description));
        alertDialog.setPositiveButton(getString(R.string.enable_location_positive_response), (dialog, which) -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });
        alertDialog.setNegativeButton(getString(R.string.negative_response), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    private MapboxMap.OnMapLongClickListener mapLongPressListener = latLng -> {
        if (inDeveloperMode) this.displayAddResourceDialog(latLng);
    };

    private void displayAddResourceDialog(LatLng latLng) {
        // Configure Spinner
        View view = getLayoutInflater().inflate(R.layout.alert_add_resource, null);
        Spinner resourceNameSpinner = view.findViewById(R.id.resource_name_spinner);
        Spinner resourceQuantitySpinner = view.findViewById(R.id.resource_quantity_spinner);

        ItemManager itemManager = ItemManager.getInstance(this);

        // Data for resources
        ArrayList<Integer> quantities = new ArrayList<>();
        for (int i = 0; i < itemManager.getNames().size(); i++) {
            quantities.add(i + 1);
        }

        // Creating adapter for spinners
        resourceNameSpinner.setAdapter(makeSpinner(new ArrayList<>(itemManager.getNames())));
        resourceQuantitySpinner.setAdapter(makeSpinner(quantities));

        new AlertDialog.Builder(this)
                .setTitle("Add Resource")
                .setView(view)
                .setPositiveButton(R.string.positive_response, (dialog, which) -> {
                    dialog.dismiss();

                    int resourceId = itemManager.getId((String) resourceNameSpinner.getSelectedItem()).withDefault(0);
                    Location location = new Location(latLng.getLatitude(), latLng.getLongitude());
                    int quantity = quantities.get(resourceQuantitySpinner.getSelectedItemPosition());

                    addResource(new Resource(resourceId, location, quantity));
                })
                .show();
    }

    private <T> ArrayAdapter<T> makeSpinner(List<T> l) {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, l);
    }

    // region  Menu Button Handlers
    private View.OnClickListener menuButtonClickListener = v -> {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
        AnticipateInterpolator anticipateInterpolator = new AnticipateInterpolator();
        float backpackY = menuButton.getY() - backpackButton.getLayoutParams().height - 100;
        float developerY = menuButton.getY() - (developerButton.getLayoutParams().height * 2) - 175;
        float settingsBlueprintY = menuButton.getY() - (settingsButton.getLayoutParams().height / 2) - 100;
        float blueprintX = (displayMetrics.widthPixels / 2) - (blueprintButton.getLayoutParams().width * 2);
        float settingsX = (displayMetrics.widthPixels / 2) + (settingsButton.getLayoutParams().width);

        switch (menuState) {
            case NORMAL:
                // Normal -> Closed
                for (Button button : new Button[]{ backpackButton, settingsButton, blueprintButton, developerButton}) {
                    button.animate()
                            .y(menuButton.getY() + menuButton.getHeight() / 8)
                            .x(menuButton.getX() + menuButton.getWidth() / 8)
                            .setDuration(MENU_ANIMATION_DURATION)
                            .setStartDelay(0)
                            .setInterpolator(anticipateInterpolator);
                }

                menuButton.animate()
                        .scaleX(1)
                        .scaleY(1)
                        .setStartDelay(MENU_ANIMATION_DURATION)
                        .setDuration(MENU_ANIMATION_DURATION)
                        .setInterpolator(overshootInterpolator);

                closeButton.animate()
                        .scaleX(0)
                        .scaleY(0)
                        .setStartDelay(0)
                        .setDuration(MENU_ANIMATION_DURATION)
                        .setInterpolator(anticipateInterpolator);

                blurView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        blurView.setVisibility(View.INVISIBLE);
                    }
                }, MENU_ANIMATION_DURATION + 100);
                blurView.animate()
                        .alpha(0)
                        .setDuration(MENU_ANIMATION_DURATION);
                menuState = MenuState.CLOSED;
                break;

            case CLOSED:
                // Closed -> Normal
                blueprintButton.animate()
                        .y(settingsBlueprintY)
                        .x(blueprintX)
                        .setStartDelay(0)
                        .setDuration(MENU_ANIMATION_DURATION)
                        .setInterpolator(overshootInterpolator);

                backpackButton.animate()
                        .y(backpackY)
                        .setDuration(MENU_ANIMATION_DURATION)
                        .setStartDelay(MENU_ANIMATION_DURATION / 3)
                        .setInterpolator(overshootInterpolator);

                settingsButton.animate()
                        .y(settingsBlueprintY)
                        .x(settingsX)
                        .setStartDelay(MENU_ANIMATION_DURATION * 2 / 3)
                        .setDuration(MENU_ANIMATION_DURATION)
                        .setInterpolator(overshootInterpolator);

                if (loginManager.isDeveloper()) {
                    developerButton.animate()
                            .y(developerY)
                            .setStartDelay(MENU_ANIMATION_DURATION)
                            .setDuration(MENU_ANIMATION_DURATION)
                            .setInterpolator(overshootInterpolator);
                }

                menuButton.animate()
                        .scaleX(0)
                        .scaleY(0)
                        .setStartDelay(MENU_ANIMATION_DURATION)
                        .setDuration(MENU_ANIMATION_DURATION)
                        .setInterpolator(anticipateInterpolator);

                closeButton.animate()
                        .scaleX(1)
                        .scaleY(1)
                        .setStartDelay(MENU_ANIMATION_DURATION * 2)
                        .setDuration(MENU_ANIMATION_DURATION)
                        .setInterpolator(overshootInterpolator);

                blurView.animate()
                        .alpha(1)
                        .setDuration(MENU_ANIMATION_DURATION);
                blurView.setVisibility(View.VISIBLE);
                menuState = MenuState.NORMAL;
                break;
            case BACKPACK:
                // Backpack -> Normal
                backpackView.hide(500);
                backpackButton.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(200)
                        .setInterpolator(new LinearInterpolator())
                        .setStartDelay(300);

                new ArrayList<>(Arrays.asList(developerButton, settingsButton, blueprintButton)).forEach(b -> {
                    b.animate().alpha(1).setDuration(500).setStartDelay(700);
                });

                new Handler().postDelayed(() -> {
                    backpackButton.animate()
                            .scaleX(1)
                            .scaleY(1)
                            .y(backpackY)
                            .setDuration(300)
                            .setStartDelay(0)
                            .setInterpolator(overshootInterpolator);
                    menuState = MenuState.NORMAL;
                    backpackView.remove();
                }, 550);
                menuButton.bringToFront();
                break;

        }
    };

    private View.OnClickListener developerButtonClickListener = v -> {
        if (menuState != MenuState.NORMAL) return;
        inDeveloperMode = !inDeveloperMode;
        onMapReady(mapboxMap);
    };

    private View.OnClickListener settingsButtonClickListener = v -> {
        if (menuState != MenuState.NORMAL) return;
        // TODO
    };

    private View.OnClickListener backpackButtonClickListener = v -> {
        if (menuState != MenuState.NORMAL) return;

        menuState = MenuState.BACKPACK;

        // Move backpack to center
        viewGroup.bringChildToFront(backpackButton);
        backpackButton.animate()
                .x(viewGroup.getWidth() / 2 - backpackButton.getWidth() / 2)
                .y(viewGroup.getHeight() / 2 - backpackButton.getHeight() / 2)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .setStartDelay(0)
                .setDuration(500);

        // Hide everything else
        new ArrayList<>(Arrays.asList(developerButton, settingsButton, blueprintButton)).forEach(b -> {
            b.animate().alpha(0).setDuration(500).setStartDelay(0);
        });

        // Spawn cells
        backpackView.animate(550);

    };

    private View.OnClickListener blueprintButtonClickListener = v -> {
        // TODO
    };

    private View.OnClickListener blurViewClickListener = v -> {
        if (menuState == MenuState.BACKPACK){
            backpackView.jumpToEndPosition();
        } else if (menuState == MenuState.NORMAL){
            menuButtonClickListener.onClick(v);
        }
    };

    // endregion

    // region Developer mode functions
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
    // endregion

    // region GestureDelegate
    @Override
    public void panBy(float dx) {
        if (menuState != MenuState.CLOSED) return;
        mapboxMap.moveCamera(CameraUpdateFactory.bearingTo(mapboxMap.getCameraPosition().bearing - dx));
    }

    @Override
    public void scaleBy(float amount) {
        if (menuState != MenuState.CLOSED) return;
        float minZoom = 17;
        float maxZoom = 20;
        float minTilt = 40;
        float maxTilt = 75;

        float hardnessFactor = 0.1f;

        // Scale factor needs to be negative if zooming out
        // Also easier to zoom in than out, so add additional "hardness"
        float delta = amount > 1 ? amount * (hardnessFactor * 0.2f) : -amount * hardnessFactor;
        float scaleFactor = (float) (mapboxMap.getCameraPosition().zoom + delta);

        // Bound zoom between min and max values
        float zoomLevel = Math.max(minZoom, Math.min(scaleFactor, maxZoom));
        mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));

        // Zoom level will be scaled  between min and max zoom
        // Apply that same scaling to the tilt

        float zoomPercentage = (scaleFactor - minZoom) / (maxZoom - minZoom);
        float tilt = minTilt + (zoomPercentage * (maxTilt - minTilt));
        mapboxMap.moveCamera(CameraUpdateFactory.tiltTo(tilt));
    }
    // endregion

    // region Mapbox overrides
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    // endregion
}
