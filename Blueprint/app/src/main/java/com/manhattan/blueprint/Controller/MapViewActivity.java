package com.manhattan.blueprint.Controller;

import android.Manifest;
<<<<<<< HEAD
import android.content.Intent;
import android.os.PersistableBundle;
=======
import android.app.ActivityOptions;
import android.content.pm.PackageManager;
import android.os.Build;
>>>>>>> Finish inventory page
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.manhattan.blueprint.BuildConfig;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Location;
import com.manhattan.blueprint.Model.LoginManager;
import com.manhattan.blueprint.Model.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.R;
import android.support.design.widget.*;
import android.view.MenuItem;
import android.content.Intent;

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
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMarkerClickListener, MapboxMap.OnScaleListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private MapView mapView;
    private MapboxMap mapboxMap;
    private BlueprintAPI blueprintAPI;

    private BottomNavigationView bottomMenuView;

    // Camera configuration
    private int minZoom = 17;
    private int maxZoom = 20;
    private int minTilt = 40;
    private int maxTilt = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        mapView = findViewById(R.id.mapView);
        Mapbox.getInstance(this, BuildConfig.MapboxAPIKey);
        mapView.onCreate(savedInstanceState);

        // If haven't logged in yet, or have revoked location, redirect
        PermissionManager locationManager = new PermissionManager(0, Manifest.permission.ACCESS_FINE_LOCATION);
        LoginManager loginManager = new LoginManager(this);
        if (!loginManager.isLoggedIn()){
            toOnboarding();
        } else if (!locationManager.hasPermission(this)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapViewActivity.this);
            dialog.setTitle("Location required");
            dialog.setMessage("Please grant access to your location so Blueprint can show resources around you.");
            dialog.setPositiveButton("Ok", (d, which) -> {
                d.dismiss();
                loginManager.setLoggedIn(false);
                toOnboarding();
            });
            dialog.create().show();
        } else {
            blueprintAPI = new BlueprintAPI();
            mapView.getMapAsync(this);
        }
    }

    private void toOnboarding(){
        Intent intent = new Intent(MapViewActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
        
        // Configure MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

<<<<<<< HEAD
        bottomView = findViewById(R.id.bottom_menu);
        bottomView.setOnNavigationItemSelectedListener(this);
=======
        bottomMenuView = findViewById(R.id.bottom_menu);
        bottomMenuView.setOnNavigationItemSelectedListener(this);

>>>>>>> Finish inventory page
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
    // endregion

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int mMenuId = item.getItemId();
        for (int i = 0; i < bottomMenuView.getMenu().size(); i++) {
            MenuItem menuItem = bottomMenuView.getMenu().getItem(i);
            boolean isChecked = menuItem.getItemId() == item.getItemId();
            menuItem.setChecked(isChecked);
        }

        switch (item.getItemId()) {
            case R.id.inventory:
                Log.d(" x ", "inventory selected");
                Intent toInventory = new Intent(MapViewActivity.this, InventoryActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(toInventory, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(toInventory);
                }
                break;
            case R.id.shopping_list:
                Log.d(" x ", "shopping_list selected");
                break;
            case R.id.settings:
                Log.d(" x ", "settings selected");
                break;
        }
        return true;
    }

    private void addResources(android.location.Location location){
        Location blueprintLocation = new Location(location);

        blueprintAPI.fetchResources(blueprintLocation, new APICallback<ResourceSet>() {
            @Override
            public void success(ResourceSet response) {
                for (Resource item : response.getItems()){
                    LatLng latLng = new LatLng(item.getLocation().getLatitude(),
                            item.getLocation().getLongitude());
                    IconFactory iconFactory = IconFactory.getInstance(MapViewActivity.this);
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("ITEM " + item.getId())
                            .icon(iconFactory.fromResource(R.drawable.resource_default)));
                }
            }

            @Override
            public void failure(String error) {
                new AlertDialog
                        .Builder(MapViewActivity.this, android.R.style.Theme_Material_Dialog_Alert)
                        .setTitle("Whoops! Could not fetch available resources.")
                        .setMessage(error)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    // region OnMarkerClickListener
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("MARKER", "MARKER: " + marker.getTitle() + " tapped");
        return false;
    }
    // endregion

    // region OnScaleListener
    @Override
    public void onScaleBegin(@NonNull StandardScaleGestureDetector detector) { }

    @Override
    public void onScale(@NonNull StandardScaleGestureDetector detector) {
        double zoom = mapboxMap.getCameraPosition().zoom;
        double fractionZoomed = (zoom - minZoom) / (maxZoom - minZoom);
        double cameraTilt = (maxTilt - minTilt) * fractionZoomed + minTilt;
        mapboxMap.animateCamera(CameraUpdateFactory.tiltTo(cameraTilt));
    }

    @Override
    public void onScaleEnd(@NonNull StandardScaleGestureDetector detector) { }
    // endregion

<<<<<<< HEAD
    //region Mapbox overrides
=======
    // region OnRequestPermissionsResult
>>>>>>> Finish inventory page
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
<<<<<<< HEAD

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
    //endregion
=======
>>>>>>> Add inventory list
}
