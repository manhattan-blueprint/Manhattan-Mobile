package com.manhattan.blueprint.Controller;

import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.ar.core.HitResult;
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

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_MOVE;

public class ARActivity extends AppCompatActivity {
    private ArFragment arFragment;

    private boolean userRequestedARInstall = false;
    private ModelRenderable manhattanRenderable;
    private ViewRenderable testViewRenderable;
    private Resource resourceToCollect;

    private final int tapsRequired = 5;
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
    View vi;

    int A[] = new int[2];
    int B[] = new int[2];
    int D[] = new int[2];
    int C[] = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        vi = (View) findViewById(R.id.Minigame);
        vi.bringToFront();
        rotation = vi.getRotation();

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
        collectCounter = tapsRequired - 1;
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
        ViewRenderable.builder()
                .setView(this, R.layout.resource_ar)
                .build()
                .thenAccept(renderable -> {
                    testViewRenderable = renderable;
                    TextView resourceView = renderable.getView().findViewById(R.id.Resource_AR);
                    ItemManager itemManager = ItemManager.getInstance(this);
                    String resourceText = itemManager.getName(resourceToCollect.getId()).getWithDefault("Resource");
                    resourceView.setText(resourceText);
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
                                if (failed) {
                                    return true;
                                }
                                X = sceneMotionEvent.getX();
                                Y = sceneMotionEvent.getY();
                                if (outOfBounds(new int[] {(int) X, (int) Y})) {
                                    failed = true;
                                    setSnackbar("Failed, try again! (outside)");
                                    return true;
                                }
                                double dist = Math.sqrt((X - X0) * (X - X0) + (Y - Y0) * (Y - Y0));
                                if (dist  < 0.8f * vi.getHeight()) {
                                    failed = true;
                                    setSnackbar("Failed, try again! (distance)");
                                    return true;
                                }
                                setSnackbar("Well done!");
                                Random rand = new Random();
                                do {
                                    rotation = rand.nextInt(180) - 90;
                                } while (rotation == 90);
                                vi.setRotation(rotation);
                                Log.d("minigame", "new rotation = " + rotation);
                                break;

                            case ACTION_DOWN:
                                setSnackbar("Started ...");
                                failed = false;
                                getCorners();
                                X0 = sceneMotionEvent.getX();
                                Y0 = sceneMotionEvent.getY();
                                Xp = X0;
                                Yp = Y0;
                                getCorners();
                                if ( outOfBounds(new int[] {(int) X0, (int) Y0}) ) {
                                    failed = true;
                                    setSnackbar("Failed, try again! (outside)99");
                                }
                                break;

                            case ACTION_MOVE:
                                if (failed) {
                                    return true;
                                }
                                setSnackbar("Moving ...");
                                X = sceneMotionEvent.getX();
                                Y = sceneMotionEvent.getY();
                                diff = getAngleError();
                                if (outOfBounds(new int[] {(int) X, (int) Y})) {
                                    failed = true;
                                    setSnackbar("Failed, try again! (outside)");
                                    return true;
                                }
                                if (diff > 20) {
                                    failed = true;
                                    setSnackbar("Failed, try again! (angle)");
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

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (testViewRenderable == null) {
                        return;
                    } else if (!itemWasPlaced) {
                        // Create the Anchor.
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        // Create the transformable node and add it to the anchor.
                        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                        node.setParent(anchorNode);
                        node.setRenderable(testViewRenderable);
                        node.select();

                        node.setOnTapListener((hitTestResult, motionEvent1) -> {
                            onResourceTapped();
                        });
                        node.getTranslationController().setEnabled(false);

                        // Remove plane renderer
                        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                        arSnackbarMessage.setText(getString(R.string.resource_collection_instruction));
                        itemWasPlaced = true;
                    }
                });
    }

    public void onSceneUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);
        Frame frame = arFragment.getArSceneView().getArFrame();

        if (!planeWasDetected) {
            // Check if a plane was detected
            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    arSnackbarMessage.setText(getString(R.string.place_resource_instruction));
                    planeWasDetected = true;
                    break;
                }
            }
        }
    }

    private void onResourceTapped() {
        if (collectCounter > 0) {
            if (collectCounter == tapsRequired - 1) {
                arSnackbarMessage.dismiss();
            }
            int progress = ((tapsRequired - collectCounter) * 100) / tapsRequired;
            String progress_msg = String.format(getString(R.string.collection_progress), progress);

            if (arToastMessage != null) {
                arToastMessage.cancel();
            }
            arToastMessage = Toast.makeText(this, progress_msg, Toast.LENGTH_SHORT);
            arToastMessage.show();

            collectCounter--;
        } else if (collectCounter == 0) {
            arToastMessage.cancel();
            InventoryItem itemCollected = new InventoryItem(resourceToCollect.getId(), resourceToCollect.getQuantity());
            BlueprintAPI api = new BlueprintAPI(this);
            Inventory inventoryToAdd = new Inventory(new ArrayList<>(Collections.singletonList(itemCollected)));
            api.makeRequest(api.inventoryService.addToInventory(inventoryToAdd), new APICallback<Void>() {
                @Override
                public void success(Void response) {
                    // Show success with "You collected 5 wood", defaulting to "You collected 5 items"
                    String itemName = ItemManager.getInstance(ARActivity.this).getName(resourceToCollect.getId()).getWithDefault("items");
                    String successMsg = String.format(getString(R.string.collection_success), resourceToCollect.getQuantity(), itemName);
                    Toast.makeText(ARActivity.this, successMsg, Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void failure(int code, String error) {
                    createDialog(getString(R.string.collection_failure_title), error, (dialog, which) -> dialog.dismiss());
                }
            });
        }
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
        return totalArea > rectArea;
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
