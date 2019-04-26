package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.manhattan.blueprint.Utils.SpriteManager;
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
    private HashMap<Integer, String> modelsMap;
    private Resource resourceToCollect;
    private AnchorNode anchorNode;
    private Anchor anchor;

    private TextView infoMessage;
    private TextView countdownIndicator;
    private GradientDrawable drawable;
    private IndicatorSeekBar progressBar;
    private View boxView;
    private View adjustIndicator;
    private View swipeIndicator;
    private Animation swipeAnimation;
    private CountDownTimer countDownTimer;

    private float prevX, prevY = 0; // previous coords
    private float initX, initY = 0; // initial  coords
    private float currX, currY = 0; // current  coords
    private float rotation;
    private int maxAngleError = 42;
    private float minDistance = 0.60f;
    private long countdown; // seconds
    private boolean swipeFailed = true;
    private boolean minigameReady = true;
    private boolean timerOn = false;
    private boolean gameOver = false;
    private int swipesToCollect;
    private boolean itemWasPlaced;
    private boolean planeWasDetected;

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
        int screenWidth = ViewUtils.getScreenWidth(ARActivity.this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                (int) (screenWidth / 3.5f),
                screenWidth - 20);
        params.gravity = Gravity.CENTER;
        boxView.setLayoutParams(params);
        boxView.setForeground(drawable);
        rotation = boxView.getRotation();
        countdownIndicator = (TextView) findViewById(R.id.CounterIndicator);
        countdownIndicator.bringToFront();
        infoMessage = (TextView) findViewById(R.id.InfoMessages);
        infoMessage.setText(getString(R.string.plane_discovery_instruction));
        infoMessage.bringToFront();
        adjustIndicator = (View) findViewById(R.id.AdjustIndicator);
        adjustIndicator.bringToFront();
        adjustIndicator.setVisibility(View.INVISIBLE);
        swipeIndicator = (View) findViewById(R.id.swipeIndicator);

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

        modelsMap = SpriteManager.addModels();

        // R * 2 swipes
        // R * 1.7 + 4 seconds
        swipesToCollect = resourceToCollect.getQuantity() * 2;
        countdown = (int) (4 + resourceToCollect.getQuantity() * 1.7);
        countdownIndicator.setText(String.format("%.1f s", (float) countdown));
        progressBar = findViewById(R.id.ProgressBar);
        progressBar.bringToFront();
        progressBar.setMax(swipesToCollect);
        progressBar.setTickCount((swipesToCollect / 2) + 1);
        progressBar.setTickMarksDrawable(getDrawable(SpriteManager.getSpriteByID(resourceToCollect.getId())));
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
    }

    public void onSceneUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);
        Frame frame = arFragment.getArSceneView().getArFrame();
        getCorners();

        if (anchorNode != null && minigameReady && !gameOver) {
            Vector3 worldPosition = anchorNode.getWorldPosition();
            Vector3 screenPosition = arFragment.getArSceneView().getScene().getCamera().worldToScreenPoint(worldPosition);
            if (ArMathUtils.outOfBounds(new int[]{(int) screenPosition.x, (int) screenPosition.y},
                                        topLeft, topRight, bottomLeft, bottomRight,
                                        boxView.getWidth(), boxView.getHeight())) {
                swipeFailed = true;
                boxView.setVisibility(View.INVISIBLE);
                swipeIndicator.setVisibility(View.INVISIBLE);
                swipeIndicator.clearAnimation();
                adjustIndicator.setVisibility(View.VISIBLE);
                if (!timerOn) {
                    infoMessage.setText(getString(R.string.resource_out_of_view));
                }
            } else if (boxView.getVisibility() == View.INVISIBLE) {
                boxView.setVisibility(View.VISIBLE);
                adjustIndicator.setVisibility(View.INVISIBLE);
                if (!timerOn) {
                    swipeIndicator.setVisibility(View.VISIBLE);
                    swipeIndicator.startAnimation(swipeAnimation);
                    infoMessage.setText(getString(R.string.resource_collection_instruction));
                }
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
                transformableNode.setLocalScale(new Vector3(16f, 16f, 16f));
                transformableNode.setParent(anchorNode);
                transformableNode.setRenderable(resourceModel);
                transformableNode.select();
                transformableNode.getTranslationController().setEnabled(false);
                transformableNode.setOnTouchListener(this::onNodeTouch);

                // Remove plane renderer
                arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                infoMessage.setText(getString(R.string.resource_collection_instruction));
                boxView.bringToFront();
                swipeIndicator.bringToFront();
                swipeAnimation = AnimationUtils.loadAnimation(this, R.anim.swipe_animation);
                swipeIndicator.startAnimation(swipeAnimation);
                itemWasPlaced = true;
            }
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
        }, 250);
    }

    private void onSuccessfulSwipe() {
        int progress = progressBar.getProgress() + 1;
        progressBar.setProgress(progress);

        if (progress == swipesToCollect) {
            finishMinigame(true);
        }
        boxView.bringToFront();
    }

    private void finishMinigame(boolean collectedAll) {
        gameOver = true;
        countDownTimer.cancel();
        boxView.setVisibility(View.INVISIBLE);
        int quantity = progressBar.getProgress() / 2;
        if (quantity == 0) {
            countdownIndicator.setText("0.0 s");
            countdownIndicator.setTextColor(getResources().getColor(R.color.red));
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                Toast.makeText(ARActivity.this,
                        getString(R.string.minigame_collected_none),
                        Toast.LENGTH_LONG).show();
                finish();
            }, 2000);
            return;
        }
        InventoryItem itemCollected = new InventoryItem(resourceToCollect.getId(), quantity);
        BlueprintAPI api = new BlueprintAPI(this);
        Inventory inventoryToAdd = new Inventory(new ArrayList<>(Collections.singletonList(itemCollected)));

        api.makeRequest(api.inventoryService.addToInventory(inventoryToAdd), new APICallback<Void>() {
            @Override
            public void success(Void response) {
                if (collectedAll) {
                    countdownIndicator.setTextColor(getResources().getColor(R.color.green));
                } else {
                    countdownIndicator.setText("0.0 s");
                    countdownIndicator.setTextColor(getResources().getColor(R.color.red));
                }
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    // Show success with "You collected 5 wood", defaulting to "You collected 5 items"
                    String itemName = ItemManager.getInstance(ARActivity.this).getName(resourceToCollect.getId()).withDefault("items");
                    String successMsg = String.format(getString(R.string.collection_success), quantity, itemName);
                    Toast.makeText(ARActivity.this, successMsg, Toast.LENGTH_LONG).show();
                    finish();
                }, 1500);
            }

            @Override
            public void failure(int code, String error) {
                ViewUtils.createDialog(ARActivity.this, getString(R.string.collection_failure_title), error,
                        (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                });
            }
        });
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

    private boolean onNodeTouch(HitTestResult hitTestResult, MotionEvent nodeMotionEvent) {
        return onSceneTouch(hitTestResult, nodeMotionEvent);
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
                    newMinigame(false, true);
                    return true;
                }
                double dist = Math.sqrt((currX - initX) * (currX - initX) + (currY - initY) * (currY - initY));
                if (dist < minDistance * boxView.getHeight()) {
                    swipeFailed = true;
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
                    infoMessage.setVisibility(View.INVISIBLE);
                    countDownTimer = new CountDownTimer(countdown * 1000, 100) {
                        public void onTick(long millisUntilFinished) {
                            String text = String.format("%.1f s", (float) millisUntilFinished / 1000);
                            countdownIndicator.setText(text);
                        }

                        public void onFinish() {
                            if (!gameOver) {
                                finishMinigame(false);
                            }
                        }
                    }.start();
                    timerOn = true;
                    swipeIndicator.setVisibility(View.INVISIBLE);
                    swipeIndicator.clearAnimation();
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
                    newMinigame(false, true);
                    return true;
                }
                if (diff > maxAngleError) {
                    swipeFailed = true;
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
