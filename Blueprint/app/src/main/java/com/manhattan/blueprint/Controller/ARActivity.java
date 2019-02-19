package com.manhattan.blueprint.Controller;

import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_MOVE;

public class ARActivity extends AppCompatActivity {
    private ArFragment arFragment;

    private boolean userRequestedARInstall = false;
    private ModelRenderable ingot;
    private Resource resourceToCollect;
    private Anchor anchor;
    private AnchorNode anchorNode;
    private TransformableNode transformableNode;


    private final int swipesRequired = 5;
    private int collectCounter;
    private boolean itemWasPlaced;
    private boolean planeWasDetected;

    private FloatingActionButton holoButton;
    private Toast arToastMessage;
    private Snackbar arSnackbarMessage;
    private TextView snackbarTextView;
    private FrameLayout snackbarView;

    float rotation;
    float Xp = 0;
    float Yp = 0;
    float X0 = 0;
    float Y0 = 0;
    float X  = 0;
    float Y  = 0;
    boolean failed = true;
    boolean minigameReady = true;
    View vi;
    GradientDrawable drawable;

    int A[] = new int[2];
    int B[] = new int[2];
    int D[] = new int[2];
    int C[] = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        vi = (View) findViewById(R.id.Minigame);
        drawable = (GradientDrawable) getResources().getDrawable(R.drawable.ar_gesture);
        drawable.setStroke(10, Color.argb(255,0,0,255));
        vi.setForeground(drawable);
        rotation = vi.getRotation();
        vi.bringToFront();

        holoButton = findViewById(R.id.HoloButton);
        holoButton.setAlpha(0.35f);
        holoButton.setOnClickListener(v -> {
            // TODO Go to Hololens
            // startActivity(new Intent(ARActivity.this, HOLOLENS.class));
        });

        String jsonResource = (String) getIntent().getExtras().get("resource");
        Gson gson = new GsonBuilder().create();
        resourceToCollect = gson.fromJson(jsonResource, Resource.class);

        PermissionManager cameraPermissionManager = new PermissionManager(0, Manifest.permission.CAMERA);
        if (!cameraPermissionManager.hasPermission(this)) {
            createDialog(getString(R.string.permission_camera_title),
                    getString(R.string.permission_camera_description),
                    (dialog, which) -> finish());
        }
        itemWasPlaced = false;
        planeWasDetected = false;
        collectCounter = swipesRequired - 1;
    }

    public void onUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);
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
            createDialog(getString(R.string.ar_install_title),
                    getString(R.string.ar_install_description),
                    (dialog, which) -> finish());
        } catch (Exception e) {
            createDialog(getString(R.string.whoops_title),
                    getString(R.string.whoops_description) + e.toString(),
                    (dialog, which) -> finish());
        }
    }

    private void startAr() {
        // Build renderable object
        ModelRenderable.builder()
                .setSource(this, Uri.parse("Ingot.sfb"))
                .build()
                .thenAccept(renderable -> {
                    ingot = renderable;
                })
                .exceptionally(
                        throwable -> {
                            Log.d("cartita", "Unable to load Renderable.", throwable);
                            return null;
                        });

        // Start AR:
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById((R.id.ux_fragment));
        Scene arScene = arFragment.getArSceneView().getScene();
        arScene.addOnUpdateListener(this::onSceneUpdate);
        arScene.setOnTouchListener(
                (HitTestResult hitTestResult, MotionEvent sceneMotionEvent) -> {

                    if (itemWasPlaced) {
                        double diff;
                        switch(sceneMotionEvent.getAction()) {
                            case ACTION_UP:
                                if (failed || !minigameReady) {
                                    break;
                                }
                                X = sceneMotionEvent.getX();
                                Y = sceneMotionEvent.getY();
                                if (outOfBounds(new int[] {(int) X, (int) Y})) {
                                    failed = true;
                                    setSnackbar("You went outside the box, try again!");
                                    newMinigame(false);
                                    return true;
                                }
                                double dist = Math.sqrt((X - X0) * (X - X0) + (Y - Y0) * (Y - Y0));
                                if (dist  < 0.75f * vi.getHeight()) {
                                    failed = true;
                                    setSnackbar("You didn't swipe far enough, try again!");
                                    newMinigame(false);
                                    return true;
                                }
                                onSuccessfulSwipe();
                                newMinigame(true);
                                break;

                            case ACTION_DOWN:
                                if (!minigameReady) {
                                    break;
                                }
                                minigameReady = true;
                                setSnackbar("GO!");
                                failed = false;
                                getCorners();
                                X0 = sceneMotionEvent.getX();
                                Y0 = sceneMotionEvent.getY();
                                Xp = X0;
                                Yp = Y0;
                                if ( outOfBounds(new int[] {(int) X0, (int) Y0}) ) {
                                    failed = true;
                                    setSnackbar("You went outside the box, try again!");
                                    newMinigame(false);
                                    return true;
                                }
                                break;

                            case ACTION_MOVE:
                                if (failed || !minigameReady) {
                                    break;
                                }
                                setSnackbar("...");
                                X = sceneMotionEvent.getX();
                                Y = sceneMotionEvent.getY();
                                diff = getAngleError();
                                if (outOfBounds(new int[] {(int) X, (int) Y})) {
                                    failed = true;
                                    setSnackbar("You went outside the box, try again!");
                                    newMinigame(false);
                                    return true;
                                }
                                if (diff > 35) {
                                    failed = true;
                                    setSnackbar("You didn't swipe in a straight line, try again!");
                                    newMinigame(false);
                                    return true;
                                }
                                Xp = X;
                                Yp = Y;
                                break;
                        }

                        return true;
                    } else {
                        return false;
                    }
                });

        // TODO: Uncomment to remove icon of a hand with device
        // arFragment.getPlaneDiscoveryController().hide();
        // arFragment.getPlaneDiscoveryController().setInstructionView(null);

        createSnackbar();
    }

    public void onSceneUpdate(FrameTime frameTime) {
        vi.bringToFront();
        arFragment.onUpdate(frameTime);
        Frame frame = arFragment.getArSceneView().getArFrame();

        if (anchorNode != null && minigameReady) {
            Vector3 worldPos = anchorNode.getWorldPosition();
            Vector3 screnPos = arFragment.getArSceneView().getScene().getCamera().worldToScreenPoint(worldPos);
            if ( outOfBounds(new int[] {(int) screnPos.x, (int) screnPos.y}) ) {
                failed = true;
                setSnackbar("Resource is out of view!");
                newMinigame(false);
            }
        }

        if (!planeWasDetected) {
            // Check if a plane was detected
            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    if (anchor == null) {
                        anchor = plane.createAnchor(plane.getCenterPose());
                        anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        // Create the transformable node and add it to the anchor.
                        transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                        transformableNode.setLocalScale(new Vector3(0.02f, 0.02f, 0.02f));
                        transformableNode.setParent(anchorNode);
                        transformableNode.setRenderable(ingot);
                        transformableNode.select();
                        transformableNode.getTranslationController().setEnabled(false);

                        // Remove plane renderer
                        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                        arSnackbarMessage.setText(getString(R.string.resource_collection_instruction));
                        itemWasPlaced = true;
                    }

                    arSnackbarMessage.setText(getString(R.string.place_resource_instruction));
                    planeWasDetected = true;
                    break;
                }
            }
        }
    }

    private void newMinigame(boolean completed) {
        minigameReady = false;
        if (completed) {
            drawable.setStroke(10, Color.argb(155,0,255,0));
            drawable.setColor(Color.argb(100,0,255,0));
            vi.setForeground(drawable);
        } else {
            drawable.setStroke(10, Color.argb(155,255,0,0));
            drawable.setColor(Color.argb(100,255,0,0));
            vi.setForeground(drawable);
        }
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Do something after 2s
            Random rand = new Random();
            do {
                rotation = rand.nextInt(180) - 90;
            } while (rotation == 90);
            vi.setRotation(rotation);
            drawable.setStroke(10, Color.argb(255,0,0,255));
            drawable.setColor(Color.argb(0,0,0,0));
            vi.setForeground(drawable);
            minigameReady = true;
        }, 2000);
    }

    private void onSuccessfulSwipe() {
        if (collectCounter > 0) {
//            if (collectCounter == swipesRequired - 1) {
//                arSnackbarMessage.dismiss();
//            }
//
//            if (arToastMessage != null) {
//                arToastMessage.cancel();
//            }
            int progress = ((swipesRequired - collectCounter) * 100) / swipesRequired;
            String progress_msg = String.format(getString(R.string.collection_progress), progress);
            setSnackbar(progress_msg);

            collectCounter--;
        } else if (collectCounter == 0) {
            // arToastMessage.cancel();
            InventoryItem itemCollected = new InventoryItem(resourceToCollect.getId(), resourceToCollect.getQuantity());
            BlueprintAPI api = new BlueprintAPI(this);
            Inventory inventoryToAdd = new Inventory(new ArrayList<>(Collections.singletonList(itemCollected)));
            api.makeRequest(api.inventoryService.addToInventory(inventoryToAdd), new APICallback<Void>() {
                @Override
                public void success(Void response) {
                    // Show success with "You collected 5 wood", defaulting to "You collected 5 items"
                    String itemName = ItemManager.getInstance(ARActivity.this).getName(resourceToCollect.getId()).getWithDefault("items");
                    String successMsg = String.format(getString(R.string.collection_success), resourceToCollect.getQuantity(), itemName);
                    setSnackbar(successMsg);
                    finish();
                }

                @Override
                public void failure(int code, String error) {
                    createDialog(getString(R.string.collection_failure_title), error, (dialog, which) -> dialog.dismiss());
                }
            });
        }
        vi.bringToFront();
    }

    private void createDialog(String title, String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ARActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(R.string.positive_response, (dialog, which) -> {
            dialog.dismiss();
            onClick.onClick(dialog, which);
        });
        alertDialog.create().show();
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

    private void getCorners() {
        vi.getLocationOnScreen(A);

        B[0] = (int) (A[0] + vi.getWidth() * Math.cos(rotation * Math.PI / 180));
        B[1] = (int) (A[1] + vi.getWidth() * Math.sin(rotation * Math.PI / 180));

        D[0] = (int) (A[0] - vi.getHeight() * Math.sin(rotation * Math.PI / 180));
        D[1] = (int) (A[1] + vi.getHeight() * Math.cos(rotation * Math.PI / 180));

        C[0] = (int) (D[0] + vi.getWidth() * Math.cos(rotation * Math.PI / 180));
        C[1] = (int) (D[1] + vi.getWidth() * Math.sin(rotation * Math.PI / 180));
    }

    private int area(int[] A, int[] B, int[] C) {
        return Math.abs( (A[0] * B[1] + A[1] * C[0] + B[0] * C[1] ) -
                         (C[0] * B[1] + A[1] * B[0] + C[1] * A[0] ) ) / 2;
    }

    private boolean outOfBounds(int[] P) {
        // PAB + PBC + PCD + PDA
        int PAB = area(P, A, B);
        int PBC = area(P, B, C);
        int PCD = area(P, C, D);
        int PDA = area(P, D, A);
        int totalArea = PAB + PBC + PCD + PDA;
        int rectArea = vi.getWidth() * vi.getHeight();
        return totalArea > rectArea + 2000;
    }

    private double getAngleError() {
        double angle = Math.atan2(Yp - Y, X - Xp) * 180 / Math.PI;
        if (angle < 0) {
            angle = 180 + angle;
        }
        double diff;
        diff = Math.abs(angle - (90 - rotation));
        diff = Math.min(diff, 180 - diff);
        Log.d("angle", "Angle: " + angle);
        Log.d("angle", "Error: " + diff);
        return diff;
    }
}
