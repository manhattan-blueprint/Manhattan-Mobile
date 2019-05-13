package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.ar.core.Config;
import com.google.ar.core.Session;

import com.google.gson.Gson;
import com.manhattan.blueprint.BuildConfig;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.GameSession;
import com.manhattan.blueprint.Model.HololensClient;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.Location;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Managers.LoginManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.LocationUtils;
import com.manhattan.blueprint.Utils.MediaUtils;
import com.manhattan.blueprint.Utils.NetworkUtils;
import com.manhattan.blueprint.Utils.SpriteManager;
import com.manhattan.blueprint.Utils.ViewUtils;
import com.manhattan.blueprint.View.BackpackView;
import com.manhattan.blueprint.View.HelpPopupFragment;
import com.manhattan.blueprint.View.MapGestureListener;
import com.manhattan.blueprint.View.BackpackPopupFragment;
import com.mapbox.android.gestures.AndroidGesturesManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.InfoWindow;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMarkerClickListener, MapGestureListener.GestureDelegate, BackpackView.BackpackDelegate {

    private enum MenuState {
        CLOSED, NORMAL, BACKPACK, BACKPACK_POPUP
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
    private View popupBackgroundBlurView;
    private ConstraintLayout mapConstraintLayout;
    private ViewGroup viewGroup;
    private FrameLayout inventoryPopupLayout;
    private BackpackView backpackView;

    private MediaUtils mediaUtils;
    private MediaPlayer mediaPlayer;
    private BlueprintAPI blueprintAPI;
    private HololensClient hololensClient;
    private int hololensCounter;
    private ItemManager itemManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LoginManager loginManager;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private HashMap<Marker, Resource> markerResourceMap = new HashMap<>();
    private LatLng lastResourceLocation;
    private BackpackPopupFragment backpackPopupFragment;
    private HelpPopupFragment helpPopupFragment;

    // Default to VR lab
    private LatLng currentLocation = new LatLng(51.4560, -2.6030);
    private Marker currentLocationMarker;
    private boolean inDeveloperMode = false;
    private MenuState menuState = MenuState.CLOSED;
    private boolean deviceSupportsAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MapboxAPIKey);
        AppCenter.start(getApplication(), BuildConfig.AppCenterKey, Analytics.class, Crashes.class);
        setContentView(R.layout.activity_map_view);

        menuButton = findViewById(R.id.menuButton);
        developerButton = findViewById(R.id.developerButton);
        backpackButton = findViewById(R.id.backpackButton);
        settingsButton = findViewById(R.id.settingsButton);
        blueprintButton = findViewById(R.id.blueprintButton);
        closeButton = findViewById(R.id.closeButton);
        blurView = findViewById(R.id.blurView);
        mapView = findViewById(R.id.mapView);
        viewGroup = findViewById(R.id.backpackContents);
        inventoryPopupLayout = findViewById(R.id.mapPopupLayout);
        popupBackgroundBlurView = findViewById(R.id.popupBackgroundBlur);
        mapConstraintLayout = findViewById(R.id.mapConstraintLayout);

        // Menu Buttons
        menuButton.setOnClickListener(menuButtonClickListener);
        closeButton.setOnClickListener(menuButtonClickListener);
        developerButton.setOnClickListener(developerButtonClickListener);
        settingsButton.setOnClickListener(settingsButtonClickListener);
        backpackButton.setOnClickListener(backpackButtonClickListener);
        blueprintButton.setOnClickListener(blueprintButtonClickListener);

        blurView.setOnClickListener(blurViewClickListener);
        blurView.setVisibility(View.INVISIBLE);

        SpriteManager.getInstance(this);

        viewGroup.post(() -> {
            backpackView = new BackpackView(MapViewActivity.this, viewGroup, this);
            updateBackpack();
        });

        loginManager = new LoginManager(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Configure audio
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.map);
        mediaPlayer.setLooping(true);
        mediaUtils = new MediaUtils(mediaPlayer);

        // If haven't logged in yet, or have revoked location, redirect
        PermissionManager locationManager = new PermissionManager(0, Manifest.permission.ACCESS_FINE_LOCATION);
        if (!locationManager.hasPermission(this)) {
            ViewUtils.showError(this,
                    getString(R.string.permission_location_title),
                    getString(R.string.permission_location_description),
                    (dialog, which) -> {
                        dialog.dismiss();
                        loginManager.logout();
                        startActivity(new Intent(MapViewActivity.this, OnboardingActivity.class));
                        finish();
                    });
            return;
        }

        NetworkUtils.configureNetworkChecker(this, NETWORK_CHECK_REFRESH);

        // Hide views as necessary
        if (!loginManager.isDeveloper()) {
            developerButton.setVisibility(View.GONE);
        }
        blurView.animate().alpha(0);
        closeButton.animate().scaleX(0).scaleY(0);
        popupBackgroundBlurView.animate().alpha(0);

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
        
        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            dao.setSession(new GameSession(
                    session.getUsername(),
                    session.getAccountType(),
                    session.getHololensIP(),
                    session.isHololensConnected(),
                    session.isTutorialEnabled(),
                    0,
                    session.isHelpEnabled()));
        });

        // Check ARcore availability
        deviceSupportsAR = false;
        try {
            Session arCoreSession = new Session(this);
            Config config = new Config(arCoreSession);
            deviceSupportsAR = arCoreSession.isSupported(config);
            arCoreSession.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        hololensClient = new HololensClient(getApplicationContext());
        hololensCounter = 0;
        hololensClient.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LocationUtils.isLocationEnabled(this)) {
            LocationUtils.displayLocationServicesRequest(this);
        }

        if (markerResourceMap != null) {
            markerResourceMap.forEach((m, r) -> m.hideInfoWindow());
        }

        if (mapView != null) {
            mapView.onResume();
        }

        updateBackpack();

        if (mediaPlayer == null) return;
        mediaPlayer.setVolume(0,0);
        mediaPlayer.start();
        mediaUtils.fadeIn();

        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        helpPopupFragment = new HelpPopupFragment(v -> {
            dao.getSession().ifPresent(session -> {
                dao.setSession(new GameSession(
                        session.getUsername(),
                        session.getAccountType(),
                        session.getHololensIP(),
                        session.isHololensConnected(),
                        session.isTutorialEnabled(),
                        session.getMinigames(),
                        false));
            });

            getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up, R.animator.slide_down)
                .remove(helpPopupFragment)
                .commit();
        });

        dao.getSession().ifPresent(session -> {
            if (session.isHelpEnabled()) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.slide_up, R.animator.slide_down)
                        .add(R.id.mapPopupLayout, helpPopupFragment).commit();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (mediaPlayer == null) {
            return;
        }
        mediaUtils.fadeOut(value -> mediaPlayer.pause());
    }

    private void updateBackpack() {
        // Reload inventory
        if (blueprintAPI == null) return;
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

    //region OnMapReadyCallback
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.setOnMapLongClickListener(point -> {
            if (inDeveloperMode) displayAddResourceDialog(point);
        });

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

                Icon icon = inDeveloperMode
                          ? IconFactory.getInstance(MapViewActivity.this).fromResource(R.drawable.will)
                          : IconFactory.getInstance(MapViewActivity.this).fromBitmap(
                                SpriteManager.getInstance(MapViewActivity.this).fetchPlayerSprite());
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

        blueprintAPI.makeRequest(
            blueprintAPI.resourceService.fetchResources(currentLocation.getLatitude(), currentLocation.getLongitude()),
            new APICallback<ResourceSet>() {
                @Override
                public void success(ResourceSet response) {
                    markerResourceMap.forEach((marker, resource) -> marker.remove());
                    markerResourceMap.clear();
                    for (Resource item : response.getItems()) {
                        LatLng itemLocation = new LatLng(item.getLocation().getLatitude(),
                                item.getLocation().getLongitude());
                        Bitmap image = SpriteManager.getInstance(MapViewActivity.this).fetchMapSprite(item.getId());
                        Icon icon = IconFactory.getInstance(MapViewActivity.this).fromBitmap(image);
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
        if (hololensClient.isPlayingMinigame() || marker.equals(currentLocationMarker)) {
            marker.hideInfoWindow();
            return false;
        }
        markerResourceMap.forEach((m, r) -> m.hideInfoWindow());

        boolean closeEnough = LocationUtils.distanceBetween(marker.getPosition(), currentLocation) <= MAX_COLLECT_DISTANCE;

        Resource resource = markerResourceMap.get(marker);
        InfoWindow infoWindow = marker.showInfoWindow(mapboxMap, mapView);
        ViewUtils.getChildren(infoWindow.getView()).forEach(x -> {
            if (x instanceof BubbleLayout) {
                BubbleLayout layout = (BubbleLayout) x;
                layout.setCornersRadius(100);
                layout.setStrokeColor(getColor(R.color.white));
                layout.setBubbleColor(getColor(R.color.brandPrimaryDark));
                layout.setStrokeWidth(10);
            } else if (x instanceof AppCompatTextView) {
                AppCompatTextView text = (AppCompatTextView) x;
                text.setTypeface(ResourcesCompat.getFont(this, R.font.helveticaneue_medium));
                text.setTextColor(getColor(R.color.white));
                if (!closeEnough) {
                    text.setText("Too far away, get closer!");
                }
            }
        });
        infoWindow.update();

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

        } else if (closeEnough) {
            BlueprintDAO.getInstance(this).getSession().ifPresent(session -> {
                if (session.isHololensConnected()) {
                    // Connect to Hololens
                    if (hololensClient.setIP(session.hololensIP)) {
                        hololensClient.addItem(resource.getId(), resource.getQuantity(), hololensCounter++);
                        hololensClient.setPlayingMinigame(true);
                    }
                } else {
                    Intent intentMinigame;
                    if (deviceSupportsAR) {
                        // Start AR Minigame
                        intentMinigame = new Intent(MapViewActivity.this, ARMinigameActivity.class);
                    } else {
                        // Start non-AR Minigame
                        intentMinigame = new Intent(MapViewActivity.this, MinigameActivity.class);
                    }
                    Bundle resourceToCollect = new Bundle();
                    resourceToCollect.putString("resource", (new Gson()).toJson(resource));
                    intentMinigame.putExtras(resourceToCollect);
                    startActivity(intentMinigame);
                }
            }).ifNotPresent(v -> ViewUtils.showError(MapViewActivity.this,
                                        getResources().getString(R.string.session_error_title),
                                        getResources().getString(R.string.session_error_msg)));
        }
        return true;
    }
    // endregion

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
        resourceNameSpinner.setAdapter(ViewUtils.makeSpinner(this, new ArrayList<>(itemManager.getNames())));
        resourceQuantitySpinner.setAdapter(ViewUtils.makeSpinner(this, quantities));

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

    // region  Menu Button Handlers
    private void transitionMenuOpenToClosed() {
        for (Button button : new Button[]{ backpackButton, settingsButton, blueprintButton, developerButton}) {
            button.animate()
                    .y(menuButton.getY() + menuButton.getHeight() / 8.f)
                    .x(menuButton.getX() + menuButton.getWidth() / 8.f)
                    .setDuration(MENU_ANIMATION_DURATION)
                    .setStartDelay(0)
                    .setInterpolator(new AnticipateInterpolator());
        }

        menuButton.animate()
                .scaleX(1)
                .scaleY(1)
                .setStartDelay(MENU_ANIMATION_DURATION)
                .setDuration(MENU_ANIMATION_DURATION)
                .setInterpolator(new OvershootInterpolator());

        closeButton.animate()
                .scaleX(0)
                .scaleY(0)
                .setStartDelay(0)
                .setDuration(MENU_ANIMATION_DURATION)
                .setInterpolator(new AnticipateInterpolator());

        blurView.postDelayed(() -> blurView.setVisibility(View.INVISIBLE), MENU_ANIMATION_DURATION + 100);
        blurView.animate().alpha(0).setDuration(MENU_ANIMATION_DURATION);
        menuState = MenuState.CLOSED;
    }

    private void transitionMenuClosedToOpen() {
        updateBackpack();
        float developerY = menuButton.getY() - (developerButton.getLayoutParams().height * 2) - 175;
        float settingsBlueprintY = menuButton.getY() - (settingsButton.getLayoutParams().height / 2.f) - 100;
        float screenWidth = ViewUtils.getScreenWidth(this);
        float blueprintX = (screenWidth / 2) - (blueprintButton.getLayoutParams().width * 2);
        float settingsX = (screenWidth / 2) + (settingsButton.getLayoutParams().width);
        float backpackY = menuButton.getY() - backpackButton.getLayoutParams().height - 100;

        blueprintButton.animate()
                .y(settingsBlueprintY)
                .x(blueprintX)
                .setStartDelay(0)
                .setDuration(MENU_ANIMATION_DURATION)
                .setInterpolator(new OvershootInterpolator());

        backpackButton.animate()
                .y(backpackY)
                .setDuration(MENU_ANIMATION_DURATION)
                .setStartDelay(MENU_ANIMATION_DURATION / 3)
                .setInterpolator(new OvershootInterpolator());

        settingsButton.animate()
                .y(settingsBlueprintY)
                .x(settingsX)
                .setStartDelay(MENU_ANIMATION_DURATION * 2 / 3)
                .setDuration(MENU_ANIMATION_DURATION)
                .setInterpolator(new OvershootInterpolator());

        if (loginManager.isDeveloper()) {
            developerButton.animate()
                    .y(developerY)
                    .setStartDelay(MENU_ANIMATION_DURATION)
                    .setDuration(MENU_ANIMATION_DURATION)
                    .setInterpolator(new OvershootInterpolator());
        }

        menuButton.animate()
                .scaleX(0)
                .scaleY(0)
                .setStartDelay(MENU_ANIMATION_DURATION)
                .setDuration(MENU_ANIMATION_DURATION)
                .setInterpolator(new AnticipateInterpolator());

        closeButton.animate()
                .scaleX(1)
                .scaleY(1)
                .setStartDelay(MENU_ANIMATION_DURATION * 2)
                .setDuration(MENU_ANIMATION_DURATION)
                .setInterpolator(new OvershootInterpolator());

        blurView.animate()
                .alpha(1)
                .setDuration(MENU_ANIMATION_DURATION);
        blurView.setVisibility(View.VISIBLE);
        menuState = MenuState.NORMAL;
    }

    private void transitionBackpackOpenToClosed() {
        float backpackY = menuButton.getY() - backpackButton.getLayoutParams().height - 100;
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
                    .setInterpolator(new OvershootInterpolator());
            menuState = MenuState.NORMAL;
            backpackView.remove();
        }, 550);
        menuButton.bringToFront();
    }

    private void transitionBackpackClosedToOpen() {
        menuState = MenuState.BACKPACK;

        // Move backpack to center
        backpackButton.animate()
                .x(mapConstraintLayout.getWidth() / 2.f - backpackButton.getWidth() / 2.f)
                .y(mapConstraintLayout.getHeight() / 2.f - backpackButton.getHeight() / 2.f)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .setStartDelay(0)
                .setDuration(500);

        // Hide everything else
        new ArrayList<>(Arrays.asList(developerButton, settingsButton, blueprintButton)).forEach(b ->
                b.animate().alpha(0).setDuration(500).setStartDelay(0));

        // Spawn cells
        backpackView.animate(550);
    }

    private View.OnClickListener menuButtonClickListener = v -> {
        markerResourceMap.forEach((m, r) -> m.hideInfoWindow());
        switch (menuState) {
            case NORMAL:
                transitionMenuOpenToClosed();
                break;
            case CLOSED:
                transitionMenuClosedToOpen();
                break;
            case BACKPACK:
                transitionBackpackOpenToClosed();
                break;
            case BACKPACK_POPUP:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.slide_up, R.animator.slide_down)
                        .remove(backpackPopupFragment)
                        .commit();
                popupBackgroundBlurView.animate().alpha(0).setDuration(500).setStartDelay(100);
                backpackButton.animate().alpha(1.0f).setDuration(200).setStartDelay(400);
                menuState = MenuState.BACKPACK;
                backpackPopupFragment = null;
        }
    };

    private View.OnClickListener developerButtonClickListener = v -> {
        if (menuState != MenuState.NORMAL) return;
        inDeveloperMode = !inDeveloperMode;
        onMapReady(mapboxMap);
    };

    private View.OnClickListener settingsButtonClickListener = v -> {
        if (menuState != MenuState.NORMAL) return;
        Intent toSettings = new Intent(MapViewActivity.this, SettingsActivity.class);
        startActivity(toSettings);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    };

    private View.OnClickListener backpackButtonClickListener = v -> {
        if (menuState != MenuState.NORMAL) return;
        transitionBackpackClosedToOpen();
    };

    private View.OnClickListener blueprintButtonClickListener = v -> {
        if (menuState != MenuState.NORMAL) return;
        Intent toBlueprint = new Intent(MapViewActivity.this, BlueprintActivity.class);
        startActivity(toBlueprint);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        markerResourceMap.forEach((m, r) -> m.hideInfoWindow());
        mapboxMap.moveCamera(CameraUpdateFactory.bearingTo(mapboxMap.getCameraPosition().bearing - dx));
    }

    @Override
    public void scaleBy(float amount) {
        if (menuState != MenuState.CLOSED) return;
        markerResourceMap.forEach((m, r) -> m.hideInfoWindow());
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

    //region Backpack Delegate
    @Override
    public void didTapBackpackItem(int itemID, int quantity) {
        if (backpackPopupFragment != null) return;
        inventoryPopupLayout.bringToFront();
        backpackButton.animate().alpha(0).setDuration(200).setStartDelay(100);
        backpackPopupFragment = BackpackPopupFragment.newInstance(itemID, quantity);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up, R.animator.slide_down)
                .add(R.id.mapPopupLayout, backpackPopupFragment).commit();
        menuState = MenuState.BACKPACK_POPUP;
        popupBackgroundBlurView.animate().alpha(1).setDuration(500).setStartDelay(100);
    }
    //endregion

    // region Mapbox overrides
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
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
