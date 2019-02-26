package com.manhattan.blueprint.Controller;

import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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

    private final int swipesRequired = 5;
    private int collectCounter;
    private boolean itemWasPlaced;
    private boolean planeWasDetected;

    private FloatingActionButton holoButton;
    private Toast arToastMessage;
    private Snackbar arSnackbarMessage;
    private TextView snackbarTextView;
    private FrameLayout snackbarView;

    float prevX, prevY = 0; // previous coords
    float initX, initY = 0; // initial  coords
    float currX, currY = 0; // current  coords
    float rotation;
    int maxAngleError = 38;
    float minDistance = 0.75f;
    boolean swipeFailed = true;
    boolean minigameReady = true;
    GradientDrawable drawable;
    View boxView;

    // box corners
    int topLeft[]     = new int[2];
    int topRight[]    = new int[2];
    int bottomLeft[]  = new int[2];
    int bottomRight[] = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        boxView = (View) findViewById(R.id.Minigame);
        drawable = (GradientDrawable) getResources().getDrawable(R.drawable.ar_gesture);
        drawable.setStroke(10, Color.argb(255,0,0,255));
        boxView.setForeground(drawable);
        rotation = boxView.getRotation();

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

        modelsMap.put(1, "wood.sfb");
        modelsMap.put(2, "rocks.sfb");
        modelsMap.put(4, "ingot.sfb");
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
                .setSource(this, Uri.parse( modelsMap.get(resourceToCollect.getId()) ))
                .build()
                .thenAccept(renderable -> {
                    resourceModel = renderable;
                });

        // Start AR:
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById((R.id.ux_fragment));
        Scene arScene = arFragment.getArSceneView().getScene();
        arScene.addOnUpdateListener(this::onSceneUpdate);
        arScene.setOnTouchListener( this::onSceneTouch);

        // TODO: Uncomment to remove icon of a hand with device
        // arFragment.getPlaneDiscoveryController().hide();
        // arFragment.getPlaneDiscoveryController().setInstructionView(null);

        createSnackbar();
    }

    public void onSceneUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);
        Frame frame = arFragment.getArSceneView().getArFrame();

        if (anchorNode != null && minigameReady) {
            Vector3 worldPos = anchorNode.getWorldPosition();
            Vector3 screnPos = arFragment.getArSceneView().getScene().getCamera().worldToScreenPoint(worldPos);
            if ( outOfBounds(new int[] {(int) screnPos.x, (int) screnPos.y}) ) {
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
                transformableNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
                transformableNode.setParent(anchorNode);
                transformableNode.setRenderable(resourceModel);
                transformableNode.select();
                transformableNode.getTranslationController().setEnabled(false);

                // Remove plane renderer
                arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                arSnackbarMessage.setText(getString(R.string.resource_collection_instruction));
                itemWasPlaced = true;
            }

            arSnackbarMessage.setText(getString(R.string.place_resource_instruction));
            boxView.bringToFront();
            planeWasDetected = true;
            break;
        }
    }

    private void newMinigame(boolean completed, boolean newRotation) {
        minigameReady = false;
        if (completed) {
            drawable.setStroke(10, Color.argb(155,0,255,0));
            drawable.setColor(Color.argb(100,0,255,0));
            boxView.setForeground(drawable);
        } else {
            drawable.setStroke(10, Color.argb(155,255,0,0));
            drawable.setColor(Color.argb(100,255,0,0));
            boxView.setForeground(drawable);
        }
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Do something after 800ms
            if (newRotation) {
                Random rand = new Random();
                do {
                    rotation = rand.nextInt(180) - 90;
                } while (rotation == 90);
                boxView.setRotation(rotation);
            }
            drawable.setStroke(10, Color.argb(255,0,0,255));
            drawable.setColor(Color.argb(0,0,0,0));
            boxView.setForeground(drawable);
            minigameReady = true;
        }, 800);
    }

    private void onSuccessfulSwipe() {
        if (collectCounter > 0) {
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
        boxView.bringToFront();
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
        boxView.getLocationOnScreen(topLeft);

        topRight[0] = (int) (topLeft[0] + boxView.getWidth() * Math.cos(rotation * Math.PI / 180));
        topRight[1] = (int) (topLeft[1] + boxView.getWidth() * Math.sin(rotation * Math.PI / 180));

        bottomLeft[0] = (int) (topLeft[0] - boxView.getHeight() * Math.sin(rotation * Math.PI / 180));
        bottomLeft[1] = (int) (topLeft[1] + boxView.getHeight() * Math.cos(rotation * Math.PI / 180));

        bottomRight[0] = (int) (bottomLeft[0] + boxView.getWidth() * Math.cos(rotation * Math.PI / 180));
        bottomRight[1] = (int) (bottomLeft[1] + boxView.getWidth() * Math.sin(rotation * Math.PI / 180));
    }

    private int area(int[] A, int[] B, int[] C) {
        return Math.abs( (A[0] * B[1] + A[1] * C[0] + B[0] * C[1] ) -
                (C[0] * B[1] + A[1] * B[0] + C[1] * A[0] ) ) / 2;
    }

    private boolean outOfBounds(int[] P) {
        int PAB = area(P, topLeft, topRight);
        int PBC = area(P, topRight, bottomRight);
        int PCD = area(P, bottomRight, bottomLeft);
        int PDA = area(P, bottomLeft, topLeft);
        int totalArea = PAB + PBC + PCD + PDA;
        int rectArea = boxView.getWidth() * boxView.getHeight();
        return totalArea > rectArea + 2000;
    }

    private double getAngleError() {
        double angle = Math.atan2(prevY - currY, currX - prevX) * 180 / Math.PI;
        if (angle < 0) {
            angle = 180 + angle;
        }
        double diff;
        diff = Math.abs(angle - (90 - rotation));
        diff = Math.min(diff, 180 - diff);
        return diff;
    }

    private boolean onSceneTouch(HitTestResult hitTestResult, MotionEvent sceneMotionEvent) {
        if (!itemWasPlaced) {
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
                if (outOfBounds(new int[]{(int) currX, (int) currY})) {
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
                setSnackbar("GO!");
                swipeFailed = false;
                getCorners();
                initX = sceneMotionEvent.getX();
                initY = sceneMotionEvent.getY();
                prevX = initX;
                prevY = initY;
                if (outOfBounds(new int[]{(int) initX, (int) initY})) {
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
                setSnackbar("...");
                currX = sceneMotionEvent.getX();
                currY = sceneMotionEvent.getY();
                diff = getAngleError();
                if (outOfBounds(new int[]{(int) currX, (int) currY})) {
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
