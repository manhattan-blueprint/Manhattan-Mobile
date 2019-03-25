package com.manhattan.blueprint.Controller;

import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.Manifest;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.ArMathUtils;
import com.manhattan.blueprint.Utils.ViewUtils;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_MOVE;

public class ARActivity extends AppCompatActivity {
    private ArFragment arFragment;

    private boolean userRequestedARInstall = false;
    private ModelRenderable resourceModel;
    private HashMap<Integer, String> modelsMap = new HashMap<>();
    private Resource resourceToCollect;
    private AnchorNode anchorNode;
    private Anchor anchor;

    private int swipesToCollect;
    private boolean itemWasPlaced;
    private boolean planeWasDetected;

    private Toast arToastMessage;
    private Snackbar arSnackbarMessage;
    private TextView snackbarTextView;
    private TextView countdownIndicator;
    private FrameLayout snackbarView;

    private float prevX, prevY = 0; // previous coords
    private float initX, initY = 0; // initial  coords
    private  float currX, currY = 0; // current  coords
    private float rotation;
    private int maxAngleError = 42;
    private float minDistance = 0.70f;
    private long countdown; // seconds
    private boolean swipeFailed = true;
    private boolean minigameReady = true;
    private  boolean timerOn = false;
    private boolean gameOver = false;
    private GradientDrawable drawable;
    private IndicatorSeekBar progressBar;
    private View boxView;

    // box corners
    private int topLeft[]     = new int[2];
    private int topRight[]    = new int[2];
    private int bottomLeft[]  = new int[2];
    private int bottomRight[] = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        boxView = (View) findViewById(R.id.Minigame);
        drawable = (GradientDrawable) getResources().getDrawable(R.drawable.ar_gesture);
        drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_neutral));
        drawable.setColor(getResources().getColor(R.color.minigame_fill_neutral));
        boxView.setForeground(drawable);
        rotation = boxView.getRotation();
        countdownIndicator = (TextView) findViewById(R.id.CounterIndicator);
        countdownIndicator.bringToFront();

        String jsonResource = (String) getIntent().getExtras().get("resource");
        Gson gson = new GsonBuilder().create();
        resourceToCollect = gson.fromJson(jsonResource, Resource.class);

        PermissionManager cameraPermissionManager = new PermissionManager(0, Manifest.permission.CAMERA);
        if (!cameraPermissionManager.hasPermission(this)) {
            ViewUtils.createDialog(ARActivity.this, getString(R.string.permission_camera_title),
                    getString(R.string.permission_camera_description),
                    (dialog, which) -> finish());
        }
        itemWasPlaced = false;
        planeWasDetected = false;

        modelsMap.put(1,  "model_1.sfb");
        modelsMap.put(2,  "model_2.sfb");
        modelsMap.put(3,  "model_3.sfb");
        modelsMap.put(4,  "model_4.sfb");
        modelsMap.put(5,  "model_5.sfb");
        modelsMap.put(6,  "model_6.sfb");
        modelsMap.put(7,  "model_7.sfb");
        modelsMap.put(8,  "model_8.sfb");
        modelsMap.put(9,  "model_9.sfb");
        modelsMap.put(10, "model_10.sfb");

        // R * 2 swipes
        // R * 1.5 + 4 seconds
        swipesToCollect = resourceToCollect.getQuantity() * 2;
        countdown = (int) (4 + resourceToCollect.getQuantity() * 1.5);
        countdownIndicator.setText(String.format("%.1f s", (float) countdown));
        progressBar = findViewById(R.id.ProgressBar);
        progressBar.bringToFront();
        progressBar.setMax(swipesToCollect);
        progressBar.setTickCount(swipesToCollect + 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arFragment != null) return;
        // Ensure latest version of ARCore is installed
        try {
            switch (ArCoreApk.getInstance().requestInstall(this, !userRequestedARInstall)) {
                case INSTALLED:
                    startAr();

                case INSTALL_REQUESTED:
                    // Ensures next call of request install returns INSTALLED or throws
                    userRequestedARInstall = true;
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            ViewUtils.createDialog(ARActivity.this, getString(R.string.ar_install_title),
                    getString(R.string.ar_install_description),
                    (dialog, which) -> finish());
        } catch (Exception e) {
            ViewUtils.createDialog(ARActivity.this, getString(R.string.whoops_title),
                    getString(R.string.whoops_description) + e.toString(),
                    (dialog, which) -> finish());
        }
    }

    private void startAr() {
        // Build renderable object
        ModelRenderable.builder()
                .setSource(this, Uri.parse( modelsMap.get(resourceToCollect.getId()) ))
                .build()
                .thenAccept(renderable -> {
                    resourceModel = renderable;
                });

        // Start AR:
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById((R.id.ux_fragment));
        Scene arScene = arFragment.getArSceneView().getScene();
        arScene.addOnUpdateListener(this::onSceneUpdate);
        arScene.setOnTouchListener(this::onSceneTouch);

        // TODO: Uncomment to remove icon of a hand with device
        // arFragment.getPlaneDiscoveryController().hide();
        // arFragment.getPlaneDiscoveryController().setInstructionView(null);

        createSnackbar();
    }

    public void onSceneUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);
        Frame frame = arFragment.getArSceneView().getArFrame();

        if (anchorNode != null && minigameReady) {
            Vector3 worldPosition = anchorNode.getWorldPosition();
            Vector3 screenPosition = arFragment.getArSceneView().getScene().getCamera().worldToScreenPoint(worldPosition);
            if (ArMathUtils.outOfBounds(new int[]{(int) screenPosition.x, (int) screenPosition.y},
                                        topLeft, topRight, bottomLeft, bottomRight,
                                        boxView.getWidth(), boxView.getHeight())) {
                swipeFailed = true;
                setSnackbar(getString(R.string.resource_out_of_view_failed));
                newMinigame(false, false);
            }
        }

        if (planeWasDetected) {
            return;
        }
        // Check if a plane was detected
        for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING && (anchor == null) ) {
                anchor = plane.createAnchor(plane.getCenterPose());
                anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                // Create the transformable node and add it to the anchor.
                TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                transformableNode.getScaleController().setMaxScale(100000f);
                transformableNode.getScaleController().setMinScale(0.0001f);
                transformableNode.setLocalScale(new Vector3(2.0f, 2.0f, 2.0f));
                transformableNode.setParent(anchorNode);
                transformableNode.setRenderable(resourceModel);
                transformableNode.select();
                transformableNode.getTranslationController().setEnabled(false);

                // Remove plane renderer
                arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                arSnackbarMessage.setText(getString(R.string.resource_collection_instruction));
                itemWasPlaced = true;
            }

            arSnackbarMessage.setText(getString(R.string.resource_collection_instruction));
            boxView.bringToFront();
            planeWasDetected = true;
            break;
        }
    }

    private void newMinigame(boolean completed, boolean newRotation) {
        minigameReady = false;
        if (completed) {
            drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_success));
            drawable.setColor(getResources().getColor(R.color.minigame_fill_success));
            boxView.setForeground(drawable);
        } else {
            drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_fail));
            drawable.setColor(getResources().getColor(R.color.minigame_fill_fail));
            boxView.setForeground(drawable);
        }
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Do something after a delay
            if (newRotation) {
                Random rand = new Random();
                do {
                    rotation = rand.nextInt(180) - 90;
                } while (rotation == 90);
                boxView.setRotation(rotation);
            }
            drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_neutral));
            drawable.setColor(getResources().getColor(R.color.minigame_fill_neutral));
            boxView.setForeground(drawable);
            minigameReady = true;
        }, 200);
    }

    private void onSuccessfulSwipe() {
        setSnackbar("Well done, keep going!");
        int progress = progressBar.getProgress() + 1;
        progressBar.setProgress(progress);

        if (progress == swipesToCollect) {
            // arToastMessage.cancel();
            InventoryItem itemCollected = new InventoryItem(resourceToCollect.getId(), resourceToCollect.getQuantity());
            BlueprintAPI api = new BlueprintAPI(this);
            Inventory inventoryToAdd = new Inventory(new ArrayList<>(Collections.singletonList(itemCollected)));
            api.makeRequest(api.inventoryService.addToInventory(inventoryToAdd), new APICallback<Void>() {
                @Override
                public void success(Void response) {
                    // Show success with "You collected 5 wood", defaulting to "You collected 5 items"
                    String itemName = ItemManager.getInstance(ARActivity.this).getName(resourceToCollect.getId()).withDefault("items");
                    String successMsg = String.format(getString(R.string.collection_success), resourceToCollect.getQuantity(), itemName);
                    Toast.makeText(ARActivity.this, successMsg, Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void failure(int code, String error) {
                    ViewUtils.createDialog(ARActivity.this, getString(R.string.collection_failure_title), error,
                                                 (dialog, which) -> dialog.dismiss());
                    finish();
                }
            });
        }
        boxView.bringToFront();
    }

    private void createSnackbar() {
        arSnackbarMessage = Snackbar.make(findViewById(R.id.ARview), getString(R.string.plane_discovery_instruction), Snackbar.LENGTH_INDEFINITE);
        snackbarTextView = (TextView) (arSnackbarMessage.getView()).findViewById(android.support.design.R.id.snackbar_text);
        snackbarView = (FrameLayout) arSnackbarMessage.getView();
        snackbarTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbarTextView.setTextSize(17);
        snackbarView.setAlpha(0.50f);
        arSnackbarMessage.show();
    }

    private void setSnackbar(String msg) {
        arSnackbarMessage.setText(msg);
    }

    // Store the (x,y) coordinates of each corner of the "gesture box"
    private void getCorners() {
        boxView.getLocationOnScreen(topLeft);

        topRight[0] = (int) (topLeft[0] + boxView.getWidth() * Math.cos(rotation * Math.PI / 180));
        topRight[1] = (int) (topLeft[1] + boxView.getWidth() * Math.sin(rotation * Math.PI / 180));

        bottomLeft[0] = (int) (topLeft[0] - boxView.getHeight() * Math.sin(rotation * Math.PI / 180));
        bottomLeft[1] = (int) (topLeft[1] + boxView.getHeight() * Math.cos(rotation * Math.PI / 180));

        bottomRight[0] = (int) (bottomLeft[0] + boxView.getWidth() * Math.cos(rotation * Math.PI / 180));
        bottomRight[1] = (int) (bottomLeft[1] + boxView.getWidth() * Math.sin(rotation * Math.PI / 180));
    }

    private boolean onSceneTouch(HitTestResult hitTestResult, MotionEvent sceneMotionEvent) {
        if (!itemWasPlaced || gameOver) {
            return false;
        }
        double diff;
        switch (sceneMotionEvent.getAction()) {
            case ACTION_UP:
                if (swipeFailed || !minigameReady) {
                    break;
                }
                currX = sceneMotionEvent.getX();
                currY = sceneMotionEvent.getY();
                if (ArMathUtils.outOfBounds(new int[]{(int) currX, (int) currY},
                                            topLeft, topRight, bottomLeft, bottomRight,
                                            boxView.getWidth(), boxView.getHeight())) {
                    swipeFailed = true;
                    setSnackbar(getString(R.string.out_of_bounds_failed));
                    newMinigame(false, true);
                    return true;
                }
                double dist = Math.sqrt((currX - initX) * (currX - initX) + (currY - initY) * (currY - initY));
                if (dist < minDistance * boxView.getHeight()) {
                    swipeFailed = true;
                    setSnackbar(getString(R.string.swipe_too_short_failed));
                    newMinigame(false, true);
                    return true;
                }
                onSuccessfulSwipe();
                newMinigame(true, true);
                break;

            case ACTION_DOWN:
                if (!minigameReady) {
                    break;
                }
                if (!timerOn) {
                    new CountDownTimer(countdown * 1000, 100) {
                        public void onTick(long millisUntilFinished) {
                            String text = String.format("%.1f s", (float) millisUntilFinished / 1000);
                            countdownIndicator.setText(text);
                        }

                        public void onFinish() {
                            gameOver = true;
                            countdownIndicator.setText("0.0 s");
                            countdownIndicator.setTextColor(getResources().getColor(R.color.red));
                            setSnackbar(getString(R.string.minigame_failed));
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                Toast.makeText(ARActivity.this,
                                        getString(R.string.collection_failure_title),
                                        Toast.LENGTH_LONG).show();
                                finish();
                                }, 1500);
                        }
                    }.start();
                    timerOn = true;
                }

                swipeFailed = false;
                getCorners();
                initX = sceneMotionEvent.getX();
                initY = sceneMotionEvent.getY();
                prevX = initX;
                prevY = initY;
                if (ArMathUtils.outOfBounds(new int[]{(int) initX, (int) initY},
                                            topLeft, topRight, bottomLeft, bottomRight,
                                            boxView.getWidth(), boxView.getHeight())) {
                    swipeFailed = true;
                    setSnackbar(getString(R.string.out_of_bounds_failed));
                    newMinigame(false, true);
                    return true;
                }
                break;

            case ACTION_MOVE:
                if (swipeFailed || !minigameReady) {
                    break;
                }
                currX = sceneMotionEvent.getX();
                currY = sceneMotionEvent.getY();
                diff = ArMathUtils.getAngleError(currX, currY, prevX, prevY, rotation);
                if (ArMathUtils.outOfBounds(new int[]{(int) currX, (int) currY},
                                            topLeft, topRight, bottomLeft, bottomRight,
                                            boxView.getWidth(), boxView.getHeight())) {
                    swipeFailed = true;
                    setSnackbar(getString(R.string.out_of_bounds_failed));
                    newMinigame(false, true);
                    return true;
                }
                if (diff > maxAngleError) {
                    swipeFailed = true;
                    setSnackbar(getString(R.string.out_of_bounds_failed));
                    newMinigame(false, true);
                    return true;
                }
                prevX = currX;
                prevY = currY;
                break;
        }
        return true;
    }
}
