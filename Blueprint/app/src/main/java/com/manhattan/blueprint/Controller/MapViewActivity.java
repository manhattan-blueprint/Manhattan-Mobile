package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.content.Intent;
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


public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMarkerClickListener, MapboxMap.OnScaleListener {
    private MapView mapView;
    private MapboxMap mapboxMap;
    private BlueprintAPI blueprintAPI;

    // Camera configuration
    private int minZoom = 17;
    private int maxZoom = 20;
    private int minTilt = 40;
    private int maxTilt = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // If haven't logged in yet, redirect
        LoginManager loginManager = new LoginManager(this);
        if (!loginManager.isLoggedIn()){
            Intent intent = new Intent(MapViewActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        }


        blueprintAPI = new BlueprintAPI();
        Mapbox.getInstance(this, BuildConfig.MapboxAPIKey);

        mapView = findViewById(R.id.mapView);

        // Configure MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
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

    //region OnMarkerClickListener
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("MARKER", "MARKER: " + marker.getTitle() + " tapped");
        return false;
    }
    //endregion

    //region OnScaleListener
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
    //endregion
}
